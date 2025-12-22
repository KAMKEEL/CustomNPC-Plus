package noppes.npcs.client.gui.util.script.interpreter;

/**
 * Metadata for a field (variable) declaration or reference.
 * Tracks the field name, type, scope, and declaration location.
 */
public final class FieldInfo {

    public enum Scope {
        GLOBAL,     // Class-level field
        LOCAL,      // Local variable inside a method
        PARAMETER   // Method parameter
    }

    private final String name;
    private final Scope scope;
    private final TypeInfo declaredType;     // The declared type (e.g., String, List<Integer>)
    private final int declarationOffset;     // Where this field was declared in the source
    private final boolean resolved;

    // For local/parameter fields, track the containing method
    private final MethodInfo containingMethod;

    private FieldInfo(String name, Scope scope, TypeInfo declaredType, 
                      int declarationOffset, boolean resolved, MethodInfo containingMethod) {
        this.name = name;
        this.scope = scope;
        this.declaredType = declaredType;
        this.declarationOffset = declarationOffset;
        this.resolved = resolved;
        this.containingMethod = containingMethod;
    }

    // Factory methods
    public static FieldInfo globalField(String name, TypeInfo type, int declOffset) {
        return new FieldInfo(name, Scope.GLOBAL, type, declOffset, type != null && type.isResolved(), null);
    }

    public static FieldInfo localField(String name, TypeInfo type, int declOffset, MethodInfo method) {
        return new FieldInfo(name, Scope.LOCAL, type, declOffset, type != null && type.isResolved(), method);
    }

    public static FieldInfo parameter(String name, TypeInfo type, int declOffset, MethodInfo method) {
        return new FieldInfo(name, Scope.PARAMETER, type, declOffset, type != null && type.isResolved(), method);
    }

    public static FieldInfo unresolved(String name, Scope scope) {
        return new FieldInfo(name, scope, null, -1, false, null);
    }

    // Getters
    public String getName() { return name; }
    public Scope getScope() { return scope; }
    public TypeInfo getDeclaredType() { return declaredType; }
    public int getDeclarationOffset() { return declarationOffset; }
    public boolean isResolved() { return resolved; }
    public MethodInfo getContainingMethod() { return containingMethod; }

    public boolean isGlobal() { return scope == Scope.GLOBAL; }
    public boolean isLocal() { return scope == Scope.LOCAL; }
    public boolean isParameter() { return scope == Scope.PARAMETER; }

    /**
     * Check if a reference at the given position can see this field.
     * For local variables, they're only visible after their declaration.
     */
    public boolean isVisibleAt(int position) {
        if (scope == Scope.GLOBAL || scope == Scope.PARAMETER) {
            return true; // Always visible in their scope
        }
        // Local variables are only visible after declaration
        return position >= declarationOffset;
    }

    /**
     * Get the appropriate TokenType for highlighting this field.
     */
    public TokenType getTokenType() {
        if (!resolved) {
            return TokenType.UNDEFINED_VAR;
        }
        switch (scope) {
            case GLOBAL:
                return TokenType.GLOBAL_FIELD;
            case LOCAL:
                return TokenType.LOCAL_FIELD;
            case PARAMETER:
                return TokenType.PARAMETER;
            default:
                return TokenType.VARIABLE;
        }
    }

    @Override
    public String toString() {
        return "FieldInfo{" + name + ", " + scope + ", type=" + declaredType + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldInfo fieldInfo = (FieldInfo) o;
        return name.equals(fieldInfo.name) && scope == fieldInfo.scope;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + scope.ordinal();
    }
}
