package noppes.npcs.client.gui.util.script.interpreter.method;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores information about a method call for argument validation.
 * This includes the method name, the arguments passed, and validation results.
 */
public class MethodCallInfo {

    /**
     * Represents a single argument in a method call.
     */
    public static class Argument {
        private final String text;           // The text of the argument expression
        private final int startOffset;       // Start position in source
        private final int endOffset;         // End position in source
        private final TypeInfo resolvedType; // The resolved type of the argument (null if unresolved)
        private final boolean valid;         // Whether this arg matches the expected parameter type
        private final String errorMessage;   // Error message if invalid

        public Argument(String text, int startOffset, int endOffset, TypeInfo resolvedType,
                        boolean valid, String errorMessage) {
            this.text = text;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.resolvedType = resolvedType;
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public String getText() {
            return text;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public TypeInfo getResolvedType() {
            return resolvedType;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean equals(Token t) {
            return text.equals(t.getText()) &&
                    startOffset == t.getGlobalStart() &&
                    endOffset == t.getGlobalEnd();
        }

        @Override
        public String toString() {
            return "Arg{" + text + " [" + startOffset + "-" + endOffset + "]" +
                    (resolvedType != null ? " :" + resolvedType.getSimpleName() : "") +
                    (valid ? "" : " INVALID: " + errorMessage) + "}";
        }
    }

    /**
     * Validation error type.
     */
    public enum ErrorType {
        NONE,
        WRONG_ARG_COUNT,       // Number of args doesn't match any overload
        WRONG_ARG_TYPE,        // Specific argument has wrong type
        STATIC_ACCESS_ERROR,   // Trying to call instance method statically or vice versa
        RETURN_TYPE_MISMATCH,  // Return type doesn't match expected type (e.g., assignment LHS)
        UNRESOLVED_METHOD,     // Method doesn't exist
        UNRESOLVED_RECEIVER    // Can't resolve the receiver type
    }

    private final String methodName;
    private final int methodNameStart;       // Position of method name
    private final int methodNameEnd;
    private final int openParenOffset;       // Position of '('
    private final int closeParenOffset;      // Position of ')'
    private final List<Argument> arguments;
    private final TypeInfo receiverType;     // The type on which this method is called (null for standalone)
    private final MethodInfo resolvedMethod; // The resolved method (null if unresolved)
    private final boolean isStaticAccess;    // True if this is Class.method() style access
    private TypeInfo expectedType;           // Expected return type (from assignment LHS, etc.)

    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage;
    private int errorArgIndex = -1;          // Index of the problematic argument (for WRONG_ARG_TYPE)
    private List<ArgumentTypeError> argumentTypeErrors = new ArrayList<>();
    
    private boolean isConstructor;

    public MethodCallInfo(String methodName, int methodNameStart, int methodNameEnd,
                          int openParenOffset, int closeParenOffset,
                          List<Argument> arguments, TypeInfo receiverType,
                          MethodInfo resolvedMethod) {
        this(methodName, methodNameStart, methodNameEnd, openParenOffset, closeParenOffset,
                arguments, receiverType, resolvedMethod, false);
    }

    public MethodCallInfo(String methodName, int methodNameStart, int methodNameEnd,
                          int openParenOffset, int closeParenOffset,
                          List<Argument> arguments, TypeInfo receiverType,
                          MethodInfo resolvedMethod, boolean isStaticAccess) {
        this.methodName = methodName;
        this.methodNameStart = methodNameStart;
        this.methodNameEnd = methodNameEnd;
        this.openParenOffset = openParenOffset;
        this.closeParenOffset = closeParenOffset;
        this.arguments = arguments != null ? new ArrayList<>(arguments) : new ArrayList<>();
        this.receiverType = receiverType;
        this.resolvedMethod = resolvedMethod;
        this.isStaticAccess = isStaticAccess;
    }

    /**
     * Factory method to create a MethodCallInfo for a constructor call.
     * Constructors are represented as method calls where the type itself is the receiver.
     */
    public static MethodCallInfo constructor(TypeInfo typeInfo, MethodInfo constructor,
                                             int typeNameStart, int typeNameEnd,
                                             int openParenOffset, int closeParenOffset,
                                             List<Argument> arguments) {
        return new MethodCallInfo(
            typeInfo.getSimpleName(),  // Constructor name is the type name
            typeNameStart,
            typeNameEnd,
            openParenOffset,
            closeParenOffset,
            arguments,
            typeInfo,                  // The type itself is the receiver
            constructor,               // The constructor MethodInfo
            false                      // Constructors are not static access
        ).setConstructor(true);
    }

    // Getters
    public String getMethodName() {
        return methodName;
    }

    public int getMethodNameStart() {
        return methodNameStart;
    }

    public int getMethodNameEnd() {
        return methodNameEnd;
    }

    public int getOpenParenOffset() {
        return openParenOffset;
    }

    public int getCloseParenOffset() {
        return closeParenOffset;
    }

    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public TypeInfo getReceiverType() {
        return receiverType;
    }

    public MethodInfo getResolvedMethod() {
        return resolvedMethod;
    }

    public boolean isStaticAccess() {
        return isStaticAccess;
    }

    public TypeInfo getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(TypeInfo expectedType) {
        this.expectedType = expectedType;
    }
    
    public boolean isConstructor() {
        return isConstructor;
    }
    
    public MethodCallInfo setConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
        return this;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the full span of the method call including parentheses.
     * Used for underlining the entire call on arg count errors.
     */
    public int getFullCallStart() {
        return methodNameStart;
    }

    public int getFullCallEnd() {
        return closeParenOffset + 1;
    }

    /**
     * Check if this method call has any validation errors.
     */
    public boolean hasError() {
        return errorType != ErrorType.NONE;
    }

    /**
     * Check if this is an arg count error (underline whole call).
     */
    public boolean hasArgCountError() {
        return errorType == ErrorType.WRONG_ARG_COUNT;
    }

    /**
     * Check if this is an arg type error (underline specific arg).
     */
    public boolean hasArgTypeError() {
        return !this.argumentTypeErrors.isEmpty();
    }
    

    /**
     * Check if this is a static access error (underline method name).
     */
    public boolean hasStaticAccessError() {
        return errorType == ErrorType.STATIC_ACCESS_ERROR;
    }

    /**
     * Check if this is a return type mismatch error.
     */
    public boolean hasReturnTypeMismatch() {
        return errorType == ErrorType.RETURN_TYPE_MISMATCH;
    }

    // Setters for validation results
    public void setError(ErrorType type, String message) {
        this.errorType = type;
        this.errorMessage = message;
    }

    public void setArgTypeError(int argIndex, String message) {
        this.argumentTypeErrors.add(new ArgumentTypeError(arguments.get(argIndex), argIndex, message));
    }
    
    public List<ArgumentTypeError> getArgumentTypeErrors() {
        return argumentTypeErrors;
    }

    public class ArgumentTypeError {
        private ErrorType type = ErrorType.WRONG_ARG_TYPE;
        private final Argument arg;
        private final int argIndex;
        private final String message;

        public ArgumentTypeError(Argument arg, int argIndex, String message) {
            this.arg = arg;
            this.argIndex = argIndex;
            this.message = message;
        }

        public int getArgIndex() {
            return argIndex;
        }

        public String getMessage() {
            return message;
        }

        public Argument getArg() {
            return arg;
        }
    }

    /**
     * Validate this method call against the resolved method signature.
     * Sets error information if validation fails.
     */
    public void validate() {
        if (resolvedMethod == null) {
            if (isConstructor) {
                // For constructors, check if the type has any constructors at all
                if (receiverType != null && receiverType.hasConstructors()) {
                    setError(ErrorType.WRONG_ARG_COUNT, 
                            "No constructor in '" + methodName + "' matches " + arguments.size() + " argument(s)");
                } else {
                    setError(ErrorType.UNRESOLVED_METHOD, 
                            "Cannot resolve constructor for '" + methodName + "'");
                }
            } else {
                setError(ErrorType.UNRESOLVED_METHOD, "Cannot resolve method '" + methodName + "'");
            }
            return;
        }
        
        // Check static/instance access (skip for constructors)
        // NO LONGER CHECKED HERE, BUT DIRECTLY AT MARK CREATION
        if (!isConstructor && isStaticAccess && !resolvedMethod.isStatic()) {
            setError(ErrorType.STATIC_ACCESS_ERROR,
                    "Cannot call instance method '" + methodName + "' on a class type");
            return;
        }

        List<FieldInfo> params = resolvedMethod.getParameters();
        int expectedCount = params.size();
        int actualCount = arguments.size();

        // Check arg count
        if (actualCount != expectedCount) {
            setError(ErrorType.WRONG_ARG_COUNT,
                    "Expected " + expectedCount + " argument(s) but got " + actualCount);
            return;
        }

        // Check each argument type
        for (int i = 0; i < actualCount; i++) {
            Argument arg = arguments.get(i);
            FieldInfo para = params.get(i);

            TypeInfo argType = arg.getResolvedType();
            TypeInfo paramType = para.getDeclaredType();
            if (argType != null && paramType != null) {
                if (!TypeChecker.isTypeCompatible(paramType, argType)) {
                    setArgTypeError(i, "Expected " + paramType.getSimpleName() +
                            " but got " + argType.getSimpleName());
                }
            } else if (paramType == null) {
                setArgTypeError(i, "Parameter type of '" + para.getName() + "' is unresolved");
            } else if (argType == null) {
                setArgTypeError(i, "Cannot resolve type of argument '" + arg.getText() + "'");
            }
        }

        // Check return type compatibility with expected type (e.g., assignment LHS)
        if (expectedType != null && resolvedMethod != null) {
            TypeInfo returnType = resolvedMethod.getReturnType();
            if (returnType != null && !TypeChecker.isTypeCompatible(expectedType, returnType)) {
              //  setError(ErrorType.RETURN_TYPE_MISMATCH,
                      //  "Required type: " + expectedType.getSimpleName() + ", Provided: " + returnType.getSimpleName());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MethodCallInfo{");
        sb.append(methodName).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(arguments.get(i).getText());
        }
        sb.append(")");
        if (hasError()) {
            sb.append(" ERROR: ").append(errorType).append(" - ").append(errorMessage);
        }
        sb.append("}");
        return sb.toString();
    }
}
