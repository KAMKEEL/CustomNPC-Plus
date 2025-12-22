package noppes.npcs.client.gui.util.script.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for a method declaration or method call.
 * Tracks method name, parameters, return type, and containing class.
 */
public final class MethodInfo {

    private final String name;
    private final TypeInfo returnType;
    private final TypeInfo containingType;    // The class/interface that owns this method
    private final List<FieldInfo> parameters;
    private final int declarationOffset;      // -1 for external methods (calls to library methods)
    private final int bodyStart;              // Start of method body (after {)
    private final int bodyEnd;                // End of method body (before })
    private final boolean resolved;
    private final boolean isDeclaration;      // true if this is a declaration, false if it's a call

    private MethodInfo(String name, TypeInfo returnType, TypeInfo containingType,
                       List<FieldInfo> parameters, int declarationOffset,
                       int bodyStart, int bodyEnd, boolean resolved, boolean isDeclaration) {
        this.name = name;
        this.returnType = returnType;
        this.containingType = containingType;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
        this.declarationOffset = declarationOffset;
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
        this.resolved = resolved;
        this.isDeclaration = isDeclaration;
    }

    // Factory methods
    public static MethodInfo declaration(String name, TypeInfo returnType, List<FieldInfo> params,
                                         int declOffset, int bodyStart, int bodyEnd) {
        return new MethodInfo(name, returnType, null, params, declOffset, bodyStart, bodyEnd, true, true);
    }

    public static MethodInfo call(String name, TypeInfo containingType, int paramCount) {
        boolean resolved = containingType != null && containingType.isResolved() && 
                          containingType.hasMethod(name);
        List<FieldInfo> params = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            params.add(FieldInfo.unresolved("arg" + i, FieldInfo.Scope.PARAMETER));
        }
        return new MethodInfo(name, null, containingType, params, -1, -1, -1, resolved, false);
    }

    public static MethodInfo unresolvedCall(String name, int paramCount) {
        List<FieldInfo> params = new ArrayList<>();
        for (int i = 0; i < paramCount; i++) {
            params.add(FieldInfo.unresolved("arg" + i, FieldInfo.Scope.PARAMETER));
        }
        return new MethodInfo(name, null, null, params, -1, -1, -1, false, false);
    }

    // Getters
    public String getName() { return name; }
    public TypeInfo getReturnType() { return returnType; }
    public TypeInfo getContainingType() { return containingType; }
    public List<FieldInfo> getParameters() { return Collections.unmodifiableList(parameters); }
    public int getParameterCount() { return parameters.size(); }
    public int getDeclarationOffset() { return declarationOffset; }
    public int getBodyStart() { return bodyStart; }
    public int getBodyEnd() { return bodyEnd; }
    public boolean isResolved() { return resolved; }
    public boolean isDeclaration() { return isDeclaration; }
    public boolean isCall() { return !isDeclaration; }

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
