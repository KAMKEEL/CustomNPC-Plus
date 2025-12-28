package noppes.npcs.client.gui.util.script.interpreter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Metadata for a method declaration or method call.
 * Tracks method name, parameters, return type, and containing class.
 * 
 * <p>This class delegates to helper classes for specific functionality:</p>
 * <ul>
 *   <li>{@link ControlFlowAnalyzer} - Control flow analysis for missing return detection</li>
 *   <li>{@link CodeParser} - Code parsing utilities (brace matching, comment removal)</li>
 *   <li>{@link TypeChecker} - Type compatibility checking</li>
 * </ul>
 */
public final class MethodInfo {

    /**
     * Validation error types for method declarations.
     */
    public enum ErrorType {
        NONE,
        MISSING_RETURN,        // Non-void method missing return statement
        RETURN_TYPE_MISMATCH,  // Return statement type doesn't match method return type
        VOID_METHOD_RETURNS_VALUE,  // Void method returns a value
        DUPLICATE_METHOD,      // Method with same signature already defined in scope
        DUPLICATE_PARAMETER,   // Two parameters have the same name
        PARAMETER_UNDEFINED    // Parameter type cannot be resolved
    }

    private final String name;
    private final TypeInfo returnType;
    private final TypeInfo containingType;    // The class/interface that owns this method
    private final List<FieldInfo> parameters;
    private final int fullDeclarationOffset;  // Start of full declaration (including modifiers), -1 for external
    private final int typeOffset;             // Start of return type
    private final int nameOffset;             // Start of method name
    private final int bodyStart;              // Start of method body (after {)
    private final int bodyEnd;                // End of method body (before })
    private final boolean resolved;
    private final boolean isDeclaration;      // true if this is a declaration, false if it's a call
    private final int modifiers;              // Java Modifier flags (e.g., Modifier.PUBLIC | Modifier.STATIC)
    private final String documentation;       // Javadoc/comment documentation for this method

    // Error tracking for method declarations
    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage;
    private List<ParameterError> parameterErrors = new ArrayList<>();
    private List<ReturnStatementError> returnStatementErrors = new ArrayList<>();

    private MethodInfo(String name, TypeInfo returnType, TypeInfo containingType,
                       List<FieldInfo> parameters, int fullDeclarationOffset, int typeOffset, int nameOffset,
                       int bodyStart, int bodyEnd, boolean resolved, boolean isDeclaration,
                       int modifiers, String documentation) {
        this.name = name;
        this.returnType = returnType;
        this.containingType = containingType;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
        this.fullDeclarationOffset = fullDeclarationOffset;
        this.typeOffset = typeOffset;
        this.nameOffset = nameOffset;
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
        this.resolved = resolved;
        this.isDeclaration = isDeclaration;
        this.modifiers = modifiers;
        this.documentation = documentation;
    }

    // Factory methods
    public static MethodInfo declaration(String name, TypeInfo returnType, List<FieldInfo> params,
                                         int fullDeclOffset, int typeOffset, int nameOffset,
                                         int bodyStart, int bodyEnd) {
        return new MethodInfo(name, returnType, null, params, fullDeclOffset, typeOffset, nameOffset, bodyStart, bodyEnd, true, true, 0, null);
    }
    
    public static MethodInfo declaration(String name, TypeInfo returnType, List<FieldInfo> params,
                                         int fullDeclOffset, int typeOffset, int nameOffset,
                                         int bodyStart, int bodyEnd, boolean isStatic) {
        int modifiers = isStatic ? Modifier.STATIC : 0;
        return new MethodInfo(name, returnType, null, params, fullDeclOffset, typeOffset, nameOffset, bodyStart, bodyEnd, true, true, modifiers, null);
    }
    
    public static MethodInfo declaration(String name, TypeInfo returnType, List<FieldInfo> params,
                                         int fullDeclOffset, int typeOffset, int nameOffset,
                                         int bodyStart, int bodyEnd, boolean isStatic, String documentation) {
        int modifiers = isStatic ? Modifier.STATIC : 0;
        return new MethodInfo(name, returnType, null, params, fullDeclOffset, typeOffset, nameOffset, bodyStart, bodyEnd, true, true, modifiers, documentation);
    }

    public static MethodInfo declaration(String name, TypeInfo returnType, List<FieldInfo> params,
                                         int fullDeclOffset, int typeOffset, int nameOffset,
                                         int bodyStart, int bodyEnd, int modifiers, String documentation) {
        return new MethodInfo(name, returnType, null, params, fullDeclOffset, typeOffset, nameOffset, bodyStart, bodyEnd, true, true, modifiers, documentation);
    }

    public static MethodInfo call(String name, TypeInfo containingType, int paramCount) {
        boolean resolved = containingType != null && containingType.isResolved() && 
                          containingType.hasMethod(name);
        List<FieldInfo> params = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            params.add(FieldInfo.unresolved("arg" + i, FieldInfo.Scope.PARAMETER));
        }
        return new MethodInfo(name, null, containingType, params, -1, -1, -1, -1, -1, resolved, false, 0, null);
    }

    public static MethodInfo unresolvedCall(String name, int paramCount) {
        List<FieldInfo> params = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            params.add(FieldInfo.unresolved("arg" + i, FieldInfo.Scope.PARAMETER));
        }
        return new MethodInfo(name, null, null, params, -1, -1, -1, -1, -1, false, false, 0, null);
    }

    /**
     * Create a MethodInfo from reflection data.
     * Used when resolving method calls on known types.
     */
    public static MethodInfo fromReflection(Method method, TypeInfo containingType) {
        String name = method.getName();
        TypeInfo returnType = TypeInfo.fromClass(method.getReturnType());
        int modifiers = method.getModifiers();
        
        List<FieldInfo> params = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            TypeInfo paramType = TypeInfo.fromClass(paramTypes[i]);
            params.add(FieldInfo.reflectionParam("arg" + i, paramType));
        }
        
        return new MethodInfo(name, returnType, containingType, params, -1, -1, -1, -1, -1, true, false, modifiers, null);
    }

    /**
     * Create a MethodInfo from a Constructor via reflection.
     * Used when resolving constructor calls on external types.
     */
    public static MethodInfo fromReflectionConstructor(Constructor<?> constructor, TypeInfo containingType) {
        String name = containingType.getSimpleName(); // Constructor name is the type name
        TypeInfo returnType = containingType; // Constructor "returns" an instance of the type
        int modifiers = constructor.getModifiers();
        
        List<FieldInfo> params = new ArrayList<>();
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            TypeInfo paramType = TypeInfo.fromClass(paramTypes[i]);
            params.add(FieldInfo.reflectionParam("arg" + i, paramType));
        }
        
        return new MethodInfo(name, returnType, containingType, params, -1, -1, -1, -1, -1, true, true, modifiers, null);
    }

    // Getters
    public String getName() { return name; }
    public TypeInfo getReturnType() { return returnType; }
    public TypeInfo getContainingType() { return containingType; }
    public List<FieldInfo> getParameters() { return Collections.unmodifiableList(parameters); }
    public int getParameterCount() { return parameters.size(); }
    /** @deprecated Use getTypeOffset() or getNameOffset() instead */
    @Deprecated
    public int getDeclarationOffset() { return typeOffset; }
    public int getFullDeclarationOffset() { return fullDeclarationOffset; }
    public int getTypeOffset() { return typeOffset; }
    public int getNameOffset() { return nameOffset; }
    public int getBodyStart() { return bodyStart; }
    public int getBodyEnd() { return bodyEnd; }
    public boolean isResolved() { return resolved; }
    public boolean isDeclaration() { return isDeclaration; }
    public boolean isCall() { return !isDeclaration; }
    public int getModifiers() { return modifiers; }
    public boolean isStatic() { return Modifier.isStatic(modifiers); }
    public boolean isFinal() { return Modifier.isFinal(modifiers); }
    public boolean isAbstract() { return Modifier.isAbstract(modifiers); }
    public boolean isSynchronized() { return Modifier.isSynchronized(modifiers); }
    public boolean isNative() { return Modifier.isNative(modifiers); }
    public boolean isPublic() { return Modifier.isPublic(modifiers); }
    public boolean isPrivate() { return Modifier.isPrivate(modifiers); }
    public boolean isProtected() { return Modifier.isProtected(modifiers); }
    public String getDocumentation() { return documentation; }

    /**
     * Check if a position is inside this method's body.
     */
    public boolean containsPosition(int position) {
        return position >= bodyStart && position < bodyEnd;
    }

    /**
     * Get the end of the method declaration (closing paren position).
     * This is used for error highlighting of duplicate methods.
     */
    public int getDeclarationEnd() {
        // The declaration ends just before the opening brace
        // We find the closing paren by searching backwards from bodyStart
        return bodyStart > 0 ? bodyStart - 1 : nameOffset + name.length();
    }

    /**
     * Check if this method has a parameter with the given name.
     */
    public boolean hasParameter(String paramName) {
        for (FieldInfo p : parameters) {
            if (p.getName().equals(paramName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get parameter info by name.
     */
    public FieldInfo getParameter(String paramName) {
        for (FieldInfo p : parameters) {
            if (p.getName().equals(paramName)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Get method signature for duplicate detection.
     * Signature = name + parameter types (ignoring parameter names).
     */
    public MethodSignature cachedSignature;

    public MethodSignature getSignature() {
        if (cachedSignature == null) {
            List<TypeInfo> paramTypes = new ArrayList<>();
            for (FieldInfo param : parameters)
                paramTypes.add(param.getDeclaredType());
            cachedSignature = new MethodSignature(name, paramTypes);
        }
        return cachedSignature;
    }

    /**
     * Get the appropriate TokenType for highlighting this method.
     */
    public TokenType getTokenType() {
        if (isDeclaration) {
            return TokenType.METHOD_DECL;
        }
        return resolved ? TokenType.METHOD_CALL : TokenType.DEFAULT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MethodInfo{");
        sb.append(name).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).getName());
        }
        sb.append(")");
        if (returnType != null) {
            sb.append(" -> ").append(returnType.getSimpleName());
        }
        sb.append(", ").append(isDeclaration ? "decl" : "call");
        sb.append(", ").append(resolved ? "resolved" : "unresolved");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return name.equals(that.name) && parameters.size() == that.parameters.size();
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + parameters.size();
    }

    // ==================== ERROR HANDLING ====================

    /**
     * Represents an error with a specific parameter.
     */
    public static class ParameterError {
        private final FieldInfo parameter;
        private final int paramIndex;
        private final ErrorType errorType;
        private final String message;

        public ParameterError(FieldInfo parameter, int paramIndex, ErrorType errorType, String message) {
            this.parameter = parameter;
            this.paramIndex = paramIndex;
            this.errorType = errorType;
            this.message = message;
        }

        public FieldInfo getParameter() { return parameter; }
        public int getParamIndex() { return paramIndex; }
        public ErrorType getErrorType() { return errorType; }
        public String getMessage() { return message; }
    }

    /**
     * Represents an error with a return statement (type mismatch).
     */
    public static class ReturnStatementError {
        private final int startOffset;  // Start of "return" keyword (absolute position)
        private final int endOffset;    // End of return statement (after semicolon)
        private final String message;
        private final TypeInfo expectedType;
        private final TypeInfo actualType;

        public ReturnStatementError(int startOffset, int endOffset, String message, TypeInfo expectedType, TypeInfo actualType) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.message = message;
            this.expectedType = expectedType;
            this.actualType = actualType;
        }

        public int getStartOffset() { return startOffset; }
        public int getEndOffset() { return endOffset; }
        public String getMessage() { return message; }
        public TypeInfo getExpectedType() { return expectedType; }
        public TypeInfo getActualType() { return actualType; }
    }

    /**
     * Check if this method declaration has any errors.
     */
    public boolean hasError() {
        return errorType != ErrorType.NONE || !parameterErrors.isEmpty() || !returnStatementErrors.isEmpty();
    }

    /**
     * Check if this has a missing return error.
     */
    public boolean hasMissingReturnError() {
        return errorType == ErrorType.MISSING_RETURN;
    }

    /**
     * Check if this has parameter errors.
     */
    public boolean hasParameterErrors() {
        return !parameterErrors.isEmpty();
    }

    /**
     * Check if this has return statement type errors.
     */
    public boolean hasReturnStatementErrors() {
        return !returnStatementErrors.isEmpty();
    }

    public ErrorType getErrorType() { return errorType; }
    public String getErrorMessage() { return errorMessage; }
    public List<ParameterError> getParameterErrors() { return Collections.unmodifiableList(parameterErrors); }
    public List<ReturnStatementError> getReturnStatementErrors() { return Collections.unmodifiableList(returnStatementErrors); }

    /**
     * Set the main error for this method.
     */
    public void setError(ErrorType type, String message) {
        this.errorType = type;
        this.errorMessage = message;
    }

    /**
     * Add a parameter-specific error.
     */
    public void addParameterError(FieldInfo param, int index, ErrorType type, String message) {
        parameterErrors.add(new ParameterError(param, index, type, message));
    }

    /**
     * Add a return statement error.
     */
    public void addReturnStatementError(int startOffset, int endOffset, String message, TypeInfo expectedType, TypeInfo actualType) {
        returnStatementErrors.add(new ReturnStatementError(startOffset, endOffset, message, expectedType, actualType));
    }

    /**
     * Functional interface for type resolution callback.
     */
    @FunctionalInterface
    public interface TypeResolver {
        TypeInfo resolveExpression(String expression, int position);
    }

    /**
     * Validate this method declaration.
     * Checks for parameter errors and missing return statements.
     * 
     * @param methodBodyText The text content of the method body (between { and })
     */
    public void validate(String methodBodyText) {
        validate(methodBodyText, null);
    }

    /**
     * Validate this method declaration with type resolution for return statements.
     * Checks for parameter errors, missing return statements, and return type mismatches.
     * 
     * @param methodBodyText The text content of the method body (between { and })
     * @param typeResolver Optional callback to resolve expression types (for return type checking)
     */
    public void validate(String methodBodyText, TypeResolver typeResolver) {
        if (!isDeclaration) return;

        // Validate parameters
        validateParameters();

        // Validate return statement types FIRST if we have a type resolver
        // This ensures type errors are shown even if there's also a missing return
        if (typeResolver != null) {
            validateReturnTypes(methodBodyText, typeResolver);
        }
        
        // Validate return statement for non-void methods
        // This may set the main error, but return type errors are already recorded
        validateReturnStatement(methodBodyText);
    }

    /**
     * Check for duplicate and unresolved parameters.
     */
    private void validateParameters() {
        Set<String> seenNames = new HashSet<>();
        
        for (int i = 0; i < parameters.size(); i++) {
            FieldInfo param = parameters.get(i);
            String paramName = param.getName();
            
            // Check for duplicate parameter names
            if (seenNames.contains(paramName)) {
                addParameterError(param, i, ErrorType.DUPLICATE_PARAMETER,
                        "Duplicate parameter name '" + paramName + "'");
            } else {
                seenNames.add(paramName);
            }
            
            // Check for unresolved parameter types
            TypeInfo paramType = param.getDeclaredType();
            if (paramType == null || !paramType.isResolved()) {
                String typeName = paramType != null ? paramType.getSimpleName() : "unknown";
                addParameterError(param, i, ErrorType.PARAMETER_UNDEFINED,
                        "Cannot resolve parameter type '" + typeName + "'");
            }
        }
    }

    /**
     * Validate that a non-void method has a guaranteed return statement.
     * Delegates to {@link ControlFlowAnalyzer} for control flow analysis.
     */
    private void validateReturnStatement(String bodyText) {
        if (bodyText == null || bodyText.isEmpty()) return;
        
        // void methods don't need return statements
        if (TypeChecker.isVoidType(returnType)) return;
        
        // Abstract/native methods don't have bodies
        if (isAbstract() || isNative()) return;

        // Check if the method has a guaranteed return using the control flow analyzer
        if (!ControlFlowAnalyzer.hasGuaranteedReturn(bodyText)) {
            setError(ErrorType.MISSING_RETURN, "Missing return statement");
        }
    }

    /**
     * Validate that all return statements have compatible types.
     * Delegates to {@link CodeParser} for parsing and {@link TypeChecker} for type checks.
     */
    private void validateReturnTypes(String bodyText, TypeResolver typeResolver) {
        if (bodyText == null || bodyText.isEmpty()) return;
        
        boolean isVoid = TypeChecker.isVoidType(returnType);
        String expectedTypeName = isVoid ? "void" : returnType.getSimpleName();
        
        // Remove comments but keep strings (we need accurate positions)
        String cleanBody = CodeParser.removeComments(bodyText);
        
        // Find all return statements
        int pos = 0;
        while (pos < cleanBody.length()) {
            // Look for "return" keyword
            int returnPos = CodeParser.findReturnKeyword(cleanBody, pos);
            if (returnPos < 0) break;
            
            // Find the semicolon ending this return statement
            int semiPos = CodeParser.findReturnSemicolon(cleanBody, returnPos + 6);
            if (semiPos < 0) {
                pos = returnPos + 6;
                continue;
            }
            
            // Extract the return expression
            String returnExpr = cleanBody.substring(returnPos + 6, semiPos).trim();
            
            // Calculate absolute position (bodyStart + 1 is after the opening brace)
            int absoluteReturnStart = bodyStart + 1 + returnPos;
            int absoluteSemiEnd = bodyStart + 1 + semiPos + 1; // +1 to include semicolon
            
            // Check void method returning a value
            if (isVoid && !returnExpr.isEmpty()) {
                String message = "Cannot return a value from a method with void result type";
                addReturnStatementError(absoluteReturnStart, absoluteSemiEnd, message, returnType, null);
            }
            // Check non-void method return type compatibility
            else if (!isVoid && !returnExpr.isEmpty() && typeResolver != null) {
                // Resolve the expression type
                TypeInfo actualType = typeResolver.resolveExpression(returnExpr, absoluteReturnStart);
                
                // Check type compatibility
                if (!TypeChecker.isTypeCompatible(returnType, actualType)) {
                    String actualTypeName = actualType != null ? actualType.getSimpleName() : "null";
                    String message = "Incompatible types.\nRequired: " + expectedTypeName + "\nFound: " + actualTypeName;
                    addReturnStatementError(absoluteReturnStart, absoluteSemiEnd, message, returnType, actualType);
                }
            }
            
            pos = semiPos + 1;
        }
    }
}
