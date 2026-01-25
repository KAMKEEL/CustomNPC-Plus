package noppes.npcs.client.gui.util.script.interpreter.method;

import noppes.npcs.client.gui.util.script.interpreter.*;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.bridge.DtsJavaBridge;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSMethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeRegistry;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

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
        MISSING_RETURN,        // Non-void method missing return statement,
        INTERFACE_METHOD_BODY, // Interface method has a body
        MISSING_BODY,        // Non-void method missing return statement
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
    private JSDocInfo jsDocInfo;        // Parsed JSDoc info for this method (may be null)
    private final java.lang.reflect.Method javaMethod;  // The Java reflection Method, if this was created from reflection

    // Error tracking for method declarations
    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage;
    private List<ParameterError> parameterErrors = new ArrayList<>();
    private List<ReturnStatementError> returnStatementErrors = new ArrayList<>();
    
    // ==================== OVERRIDE/IMPLEMENTS TRACKING ====================
    
    /**
     * The type containing the method that this method overrides (the super class).
     * Null if this method doesn't override anything.
     */
    private TypeInfo overridesFrom;
    
    /**
     * The type containing the interface method that this method implements.
     * Null if this method doesn't implement an interface method.
     */
    private TypeInfo implementsFrom;

    private MethodInfo(String name, TypeInfo returnType, TypeInfo containingType,
                       List<FieldInfo> parameters, int fullDeclarationOffset, int typeOffset, int nameOffset,
                       int bodyStart, int bodyEnd, boolean resolved, boolean isDeclaration,
                       int modifiers, String documentation, java.lang.reflect.Method javaMethod) {
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
        this.javaMethod = javaMethod;
    }

    public static MethodInfo declaration(String name, TypeInfo containingType, TypeInfo returnType, List<FieldInfo> params,
                                         int fullDeclOffset, int typeOffset, int nameOffset,
                                         int bodyStart, int bodyEnd, int modifiers, String documentation) {
        return new MethodInfo(name, returnType, containingType, params, fullDeclOffset, typeOffset, nameOffset, bodyStart, bodyEnd, true, true, modifiers, documentation, null);
    }

    public static MethodInfo call(String name, TypeInfo containingType, int paramCount) {
        boolean resolved = containingType != null && containingType.isResolved() && 
                          containingType.hasMethod(name);
        List<FieldInfo> params = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            params.add(FieldInfo.unresolved("arg" + i, FieldInfo.Scope.PARAMETER));
        }
        return new MethodInfo(name, null, containingType, params, -1, -1, -1, -1, -1, resolved, false, 0, null, null);
    }

    public static MethodInfo unresolvedCall(String name, int paramCount) {
        List<FieldInfo> params = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            params.add(FieldInfo.unresolved("arg" + i, FieldInfo.Scope.PARAMETER));
        }
        return new MethodInfo(name, null, null, params, -1, -1, -1, -1, -1, false, false, 0, null, null);
    }

    /**
     * Create a MethodInfo for a synthetic/external method.
     * Used for built-in types like Nashorn's Java object.
     */
    public static MethodInfo external(String name, TypeInfo returnType, TypeInfo containingType, 
                                       List<FieldInfo> params, int modifiers, String documentation) {
        return new MethodInfo(name, returnType, containingType, params, -1, -1, -1, -1, -1, true, false, modifiers, documentation, null);
    }

    /**
     * Create a MethodInfo from reflection data.
     * Used when resolving method calls on known types.
     */
    public static MethodInfo fromReflection(Method method, TypeInfo containingType) {
        String name = method.getName();
        TypeInfo returnType = TypeInfo.fromClass(method.getReturnType());
        int modifiers = method.getModifiers();

        // Try to find matching JSMethodInfo to bridge parameter names/docs and allow return overrides
        JSMethodInfo jsMethod = DtsJavaBridge.findMatchingMethod(method, containingType);
        if (jsMethod != null) {
            // If .d.ts provides a more specific return type, use it for editor typing.
            // (e.g., .d.ts override like IDBCPlayer -> IDBCAddon in npcdbc).
            TypeInfo overrideReturnType = DtsJavaBridge.resolveReturnTypeOverride(method, containingType, jsMethod);
            if (overrideReturnType != null && overrideReturnType.getJavaClass() != null) {
                Class<?> reflectedReturn = method.getReturnType();
                Class<?> overrideClass = overrideReturnType.getJavaClass();
                if (reflectedReturn.isAssignableFrom(overrideClass)) {
                    if (reflectedReturn != overrideClass) {
                        returnType = overrideReturnType;
                        System.out.println("[DtsJavaBridge] Overrode return type for "
                                + method.getDeclaringClass().getName() + "." + name
                                + "(" + method.getParameterCount() + ") from "
                                + reflectedReturn.getName() + " to " + overrideClass.getName());
                    }
                }
            }
        }
        List<FieldInfo> params = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();
        List<JSMethodInfo.JSParameterInfo> jsParams = jsMethod != null ? jsMethod.getParameters() : null;
        for (int i = 0; i < paramTypes.length; i++) {
            TypeInfo paramType = TypeInfo.fromClass(paramTypes[i]);
            String paramName = "arg" + i;
            if (jsParams != null && i < jsParams.size()) {
                String jsName = jsParams.get(i).getName();
                if (jsName != null && !jsName.isEmpty()) {
                    paramName = jsName;
                }
            }
            params.add(FieldInfo.reflectionParam(paramName, paramType));
        }

        String documentation = null;
        JSDocInfo jsDocInfo = null;
        if (jsMethod != null) {
            jsDocInfo = jsMethod.getJsDocInfo();
            String jsDocDesc = jsDocInfo != null ? jsDocInfo.getDescription() : null;
            documentation = jsDocDesc != null ? jsDocDesc : jsMethod.getDocumentation();
        }

        MethodInfo methodInfo = new MethodInfo(name, returnType, containingType, params, -1, -1, -1, -1, -1, true, false, modifiers, documentation, method);
        if (jsDocInfo != null) {
            methodInfo.setJSDocInfo(jsDocInfo);
        }
        return methodInfo;
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
        
        return new MethodInfo(name, returnType, containingType, params, -1, -1, -1, -1, -1, true, true, modifiers, null, null);
    }

    /**
     * Create a MethodInfo from a JSMethodInfo (parsed from .d.ts files).
     * Used when resolving method calls on JavaScript types.
     * 
     * @param jsMethod The JavaScript method info from the type registry
     * @param containingType The TypeInfo that owns this method
     * @return A MethodInfo representing the JavaScript method
     */
    public static MethodInfo fromJSMethod(JSMethodInfo jsMethod, TypeInfo containingType) {
        String name = jsMethod.getName();
        
        // Use the new getResolvedReturnType method
        TypeInfo returnType = jsMethod.getResolvedReturnType(containingType);
        
        // Convert JS parameters to FieldInfo
        List<FieldInfo> params = new ArrayList<>();
        List<JSMethodInfo.JSParameterInfo> jsParams = jsMethod.getParameters();
        
        for (JSMethodInfo.JSParameterInfo param : jsParams) {
            String paramName = param.getName();
            
            // Use the new getResolvedType method
            TypeInfo paramType = param.getResolvedType(containingType);
            
            params.add(FieldInfo.reflectionParam(paramName, paramType));
        }
        
        // JS methods are always public (no access modifiers in .d.ts)
        int modifiers = Modifier.PUBLIC;
        
        // Use the documentation from the method if available
        JSDocInfo jsDocInfo = jsMethod.getJsDocInfo();
        String jsDocDesc = jsDocInfo != null ? jsDocInfo.getDescription() : null;
        String documentation = jsDocDesc != null ? jsDocDesc : jsMethod.getDocumentation();

        MethodInfo methodInfo = new MethodInfo(name, returnType, containingType, params, -1, -1, -1, -1, -1, true,
                false, modifiers, documentation, null);
        methodInfo.setJSDocInfo(jsDocInfo);
        return methodInfo;
    }
    


    // Getters
    public String getName() { return name; }
    public TypeInfo getReturnType() { return returnType; }
    public TypeInfo getContainingType() { return containingType; }
    public List<FieldInfo> getParameters() { return Collections.unmodifiableList(parameters); }
    public int getParameterCount() { return parameters.size(); }
    public Method getJavaMethod() { return javaMethod; }
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
    public JSDocInfo getJSDocInfo() { return jsDocInfo; }
    public void setJSDocInfo(JSDocInfo jsDocInfo) { this.jsDocInfo = jsDocInfo; }
    
    // ==================== OVERRIDE/IMPLEMENTS GETTERS/SETTERS ====================
    
    /**
     * Check if this method overrides a parent class method.
     */
    public boolean isOverride() { return overridesFrom != null; }
    
    /**
     * Get the type containing the method this overrides.
     * @return The parent class TypeInfo, or null if not an override
     */
    public TypeInfo getOverridesFrom() { return overridesFrom; }
    
    /**
     * Mark this method as overriding a parent class method.
     * @param parentType The type containing the overridden method
     */
    public void setOverridesFrom(TypeInfo parentType) { this.overridesFrom = parentType; }
    
    /**
     * Check if this method implements an interface method.
     */
    public boolean isImplements() { return implementsFrom != null; }
    
    /**
     * Get the interface containing the method this implements.
     * @return The interface TypeInfo, or null if not implementing
     */
    public TypeInfo getImplementsFrom() { return implementsFrom; }
    
    /**
     * Mark this method as implementing an interface method.
     * @param interfaceType The interface containing the implemented method
     */
    public void setImplementsFrom(TypeInfo interfaceType) { this.implementsFrom = interfaceType; }
    
    /**
     * Check if this method either overrides or implements something.
     */
    public boolean hasInheritanceMarker() { return isOverride() || isImplements(); }

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
                paramTypes.add(param.getTypeInfo());
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
     * Validate this method declaration with type resolution for return statements.
     * Checks for parameter errors, missing return statements, and return type mismatches.
     * 
     * @param methodBodyText The text content of the method body (between { and })
     * @param typeResolver Optional callback to resolve expression types (for return type checking)
     */
    public void validate(String methodBodyText, boolean hasBody, TypeResolver typeResolver) {
        if (!isDeclaration) return;

        // Don't error if return type unresolved
        if(returnType != null && !returnType.isResolved()) 
            return;
        
        // Validate parameters
        validateParameters();
        boolean interfaceMember = containingType != null && containingType.isInterface();
        if (hasBody && (interfaceMember || isAbstract() || isNative())) {
            setError(MethodInfo.ErrorType.INTERFACE_METHOD_BODY, "Interface or abstract methods cannot have a body");
            return;
        }
        
        //No need to check for return types/statements 
        if(interfaceMember)
            return;

        //If no body and not interface or abstract/native
        if (bodyStart == bodyEnd && !isAbstract() && !isNative()) {
            setError(ErrorType.MISSING_BODY, "Method must have a body or be declared abstract/native.");
            return;
        }
        
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
            TypeInfo paramType = param.getTypeInfo();
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
