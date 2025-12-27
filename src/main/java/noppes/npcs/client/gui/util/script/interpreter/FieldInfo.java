package noppes.npcs.client.gui.util.script.interpreter;

import scala.annotation.meta.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for a field (variable) declaration or reference.
 * Tracks the field name, type, scope, and declaration location.
 * Also tracks all assignments made to this field throughout the script.
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
    private final String documentation;      // Javadoc/comment documentation for this field
    
    // Initialization value range (for displaying "= value" in hover info)
    private final int initStart;             // Position of '=' or -1 if no initializer
    private final int initEnd;               // Position after initializer (before ';') or -1

    // For local/parameter fields, track the containing method
    private final MethodInfo containingMethod;

    // Modifiers for script-defined fields
    private final int modifiers;             // Java Modifier flags (e.g., Modifier.FINAL)

    // Reflection field for external types
    private final Field reflectionField;

    // Assignments to this field (populated after parsing)
    private final List<AssignmentInfo> assignments = new ArrayList<>();

    // Declaration assignment (for initial value validation)
    private AssignmentInfo declarationAssignment;

    private FieldInfo(String name, Scope scope, TypeInfo declaredType, 
                      int declarationOffset, boolean resolved, MethodInfo containingMethod,
                      String documentation, int initStart, int initEnd, int modifiers,
                      Field reflectionField) {
        this.name = name;
        this.scope = scope;
        this.declaredType = declaredType;
        this.declarationOffset = declarationOffset;
        this.resolved = resolved;
        this.containingMethod = containingMethod;
        this.documentation = documentation;
        this.initStart = initStart;
        this.initEnd = initEnd;
        this.modifiers = modifiers;
        this.reflectionField = reflectionField;
    }

    // Factory methods
    public static FieldInfo globalField(String name, TypeInfo type, int declOffset) {
        return new FieldInfo(name, Scope.GLOBAL, type, declOffset, type != null && type.isResolved(), null, null, -1,
                -1, 0, null);
    }
    
    public static FieldInfo globalField(String name, TypeInfo type, int declOffset, String documentation) {
        return new FieldInfo(name, Scope.GLOBAL, type, declOffset, type != null && type.isResolved(), null,
                documentation, -1, -1, 0, null);
    }
    
    public static FieldInfo globalField(String name, TypeInfo type, int declOffset, String documentation, int initStart, int initEnd) {
        return new FieldInfo(name, Scope.GLOBAL, type, declOffset, type != null && type.isResolved(), null,
                documentation, initStart, initEnd, 0, null);
    }

    public static FieldInfo globalField(String name, TypeInfo type, int declOffset, String documentation, int initStart,
                                        int initEnd, int modifiers) {
        return new FieldInfo(name, Scope.GLOBAL, type, declOffset, type != null && type.isResolved(), null,
                documentation, initStart, initEnd, modifiers, null);
    }

    public static FieldInfo localField(String name, TypeInfo type, int declOffset, MethodInfo method) {
        return new FieldInfo(name, Scope.LOCAL, type, declOffset, type != null && type.isResolved(), method, null, -1,
                -1, 0, null);
    }
    
    public static FieldInfo localField(String name, TypeInfo type, int declOffset, MethodInfo method, int initStart, int initEnd) {
        return new FieldInfo(name, Scope.LOCAL, type, declOffset, type != null && type.isResolved(), method, null,
                initStart, initEnd, 0, null);
    }

    public static FieldInfo localField(String name, TypeInfo type, int declOffset, MethodInfo method, int initStart,
                                       int initEnd, int modifiers) {
        return new FieldInfo(name, Scope.LOCAL, type, declOffset, type != null && type.isResolved(), method, null,
                initStart, initEnd, modifiers, null);
    }

    public static FieldInfo parameter(String name, TypeInfo type, int declOffset, MethodInfo method) {
        return new FieldInfo(name, Scope.PARAMETER, type, declOffset, type != null && type.isResolved(), method, null,
                -1, -1, 0, null);
    }

    public static FieldInfo unresolved(String name, Scope scope) {
        return new FieldInfo(name, scope, null, -1, false, null, null, -1, -1, 0, null);
    }

    /**
     * Create a FieldInfo from reflection data for method parameters.
     */
    public static FieldInfo reflectionParam(String name, TypeInfo type) {
        return new FieldInfo(name, Scope.PARAMETER, type, -1, true, null, null, -1, -1, 0, null);
    }

    /**
     * Create a FieldInfo from reflection data for a class field.
     */
    public static FieldInfo fromReflection(Field field, TypeInfo containingType) {
        String name = field.getName();
        TypeInfo type = TypeInfo.fromClass(field.getType());
        return new FieldInfo(name, Scope.GLOBAL, type, -1, true, null, null, -1, -1, field.getModifiers(), field);
    }

    // ==================== ASSIGNMENT MANAGEMENT ====================

    /**
     * Add an assignment to this field.
     */
    public void addAssignment(AssignmentInfo assignment) {
        assignments.add(assignment);
    }

    /**
     * Get all assignments to this field (unmodifiable).
     */
    public List<AssignmentInfo> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    /**
     * Set the declaration assignment (initial value assignment).
     */
    public void setDeclarationAssignment(AssignmentInfo assignment) {
        this.declarationAssignment = assignment;
    }

    /**
     * Get the declaration assignment (initial value assignment).
     * Returns null if there was no initializer or it hasn't been validated yet.
     */
    public AssignmentInfo getDeclarationAssignment() {
        return declarationAssignment;
    }

    /**
     * Find an assignment that contains the given position.
     * Returns null if no assignment contains this position.
     */
    public AssignmentInfo findAssignmentAtPosition(int position) {
        // Check declaration assignment first
        if (declarationAssignment != null && declarationAssignment.containsPosition(position)) {
            return declarationAssignment;
        }

        // Check other assignments
        for (AssignmentInfo assign : assignments) {
            if (assign.containsPosition(position)) {
                return assign;
            }
        }
        return null;
    }

    /**
     * Get all errored assignments for this field.
     */
    public List<AssignmentInfo> getErroredAssignments() {
        List<AssignmentInfo> errored = new ArrayList<>();

        // Include declaration assignment if it has an error
        if (declarationAssignment != null && declarationAssignment.hasError()) {
            errored.add(declarationAssignment);
        }

        // Include all other assignments with errors
        for (AssignmentInfo assign : assignments) {
            if (assign.hasError()) {
                errored.add(assign);
            }
        }
        return errored;
    }

    /**
     * Clear all assignments (for re-parsing).
     */
    public void clearAssignments() {
        assignments.clear();
        declarationAssignment = null;
    }

    // ==================== MODIFIER CHECKS ====================

    /**
     * Check if this field is declared as final.
     * Works for both script-defined fields (via modifiers) and reflection fields.
     */
    public boolean isFinal() {
        if (reflectionField != null)
            return Modifier.isFinal(reflectionField.getModifiers());

        return Modifier.isFinal(modifiers);
    }

    /**
     * Check if this field is declared as static.
     */
    public boolean isStatic() {
        if (reflectionField != null)
            return Modifier.isStatic(reflectionField.getModifiers());

        return Modifier.isStatic(modifiers);
    }

    /**
     * Check if this field is declared as private.
     */
    public boolean isPrivate() {
        if (reflectionField != null)
            return Modifier.isPrivate(reflectionField.getModifiers());

        return Modifier.isPrivate(modifiers);
    }

    /**
     * Check if this field is declared as protected.
     */
    public boolean isProtected() {
        if (reflectionField != null)
            return Modifier.isProtected(reflectionField.getModifiers());

        return Modifier.isProtected(modifiers);
    }

    /**
     * Check if this field is declared as public.
     */
    public boolean isPublic() {
        if (reflectionField != null)
            return Modifier.isPublic(reflectionField.getModifiers());

        return Modifier.isPublic(modifiers);
    }

    /**
     * Get the raw modifiers value.
     */
    public int getModifiers() {
        if (reflectionField != null)
            return reflectionField.getModifiers();

        return modifiers;
    }

    /**
     * Get the reflection field, if available.
     */
    public java.lang.reflect.Field getReflectionField() {
        return reflectionField;
    }

    // ==================== BASIC GETTERS ====================
    
    public String getName() { return name; }
    public Scope getScope() { return scope; }
    public TypeInfo getDeclaredType() { return declaredType; }
    public TypeInfo getTypeInfo() { return declaredType; }  // Alias for getDeclaredType
    public int getDeclarationOffset() { return declarationOffset; }
    public boolean isResolved() { return resolved; }
    public MethodInfo getContainingMethod() { return containingMethod; }
    public String getDocumentation() { return documentation; }
    public int getInitStart() { return initStart; }
    public int getInitEnd() { return initEnd; }
    public boolean hasInitializer() { return initStart >= 0 && initEnd > initStart; }

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
        return "FieldInfo{" + name + ", " + scope + ", type=" + declaredType + ", final=" + isFinal() + "}";
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

    /**
     * Wrapper class that holds both FieldInfo and MethodCallInfo for a variable usage.
     * This is used when a variable is used as an argument to a method call.
     */
    public static class ArgInfo {
        public final FieldInfo fieldInfo;
        public final MethodCallInfo methodCallInfo;

        public ArgInfo(FieldInfo fieldInfo, MethodCallInfo methodCallInfo) {
            this.fieldInfo = fieldInfo;
            this.methodCallInfo = methodCallInfo;
        }
    }
}
