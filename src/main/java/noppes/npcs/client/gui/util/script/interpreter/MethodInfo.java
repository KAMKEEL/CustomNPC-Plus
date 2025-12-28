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
}
