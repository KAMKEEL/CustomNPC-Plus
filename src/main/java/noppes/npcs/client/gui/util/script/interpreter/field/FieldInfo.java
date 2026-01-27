package noppes.npcs.client.gui.util.script.interpreter.field;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSFieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeRegistry;
import noppes.npcs.client.gui.util.script.interpreter.bridge.DtsJavaBridge;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeSubstitutor;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;

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
        PARAMETER,  // Method parameter
        ENUM_CONSTANT // Enum constant value
    }

    private final String name;
    private final Scope scope;
    private final TypeInfo declaredType;     // The declared type (e.g., String, List<Integer>)
    private final int declarationOffset;     // Where this field was declared in the source
    private final boolean resolved;
    private final String documentation;      // Javadoc/comment documentation for this field
    private JSDocInfo jsDocInfo;        // Parsed JSDoc info for this method (may be null)

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

    // Enum constant specific info
    private EnumConstantInfo enumConstantInfo;
    
    // Type inference support (for JavaScript "any" typed variables)
    // When a variable is declared without type (var x;), it starts as "any" but can be
    // refined through assignments or JSDoc comments
    private TypeInfo inferredType;
    
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
     * Create a FieldInfo for a synthetic/external field.
     * Used for built-in types like Nashorn's Java object.
     */
    public static FieldInfo external(String name, TypeInfo type, String documentation, int modifiers) {
        return new FieldInfo(name, Scope.GLOBAL, type, -1, true, null, documentation, -1, -1, modifiers, null);
    }

    /**
     * Create a FieldInfo from reflection data for a class field.
     * Preserves generic type information like List<String>.
     */
    public static FieldInfo fromReflection(Field field, TypeInfo containingType) {
        String name = field.getName();
        // Use getGenericType() to preserve generic information
        TypeInfo type = TypeInfo.fromGenericType(field.getGenericType());
        if (type == null) {
            type = TypeInfo.fromClass(field.getType());
        }

        // If the receiver is parameterized, substitute class type variables in the field type
        java.util.Map<String, TypeInfo> receiverBindings = TypeSubstitutor.createBindingsFromReceiver(containingType);
        if (!receiverBindings.isEmpty()) {
            type = TypeSubstitutor.substitute(type, receiverBindings);
        }

        // Check if this is an enum constant
        if (field.isEnumConstant()) {
            // Create enum constant FieldInfo
            EnumConstantInfo constantInfo = EnumConstantInfo.fromReflection(name, containingType,field);
            if (constantInfo != null)
                return constantInfo.getFieldInfo();
        }
        
        // Try to find matching JSFieldInfo to bridge over documentation
        JSFieldInfo jsField = DtsJavaBridge.findMatchingField(field, containingType);
        String documentation = null;
        JSDocInfo jsDocInfo = null;
        if (jsField != null) {
            jsDocInfo = jsField.getJsDocInfo();
            String jsDocDesc = jsDocInfo != null ? jsDocInfo.getDescription() : null;
            documentation = jsDocDesc != null ? jsDocDesc : jsField.getDocumentation();
        }

        FieldInfo fieldInfo = new FieldInfo(name, Scope.GLOBAL, type, -1, true, null, documentation, -1, -1, field.getModifiers(), field);
        if (jsDocInfo != null) {
            fieldInfo.setJSDocInfo(jsDocInfo);
        }
        return fieldInfo;
    }

    /**
     * Create a FieldInfo from a JSFieldInfo (parsed from .d.ts files).
     * Used when resolving field access on JavaScript types.
     * 
     * @param jsField The JavaScript field info from the type registry
     * @param containingType The TypeInfo that owns this field
     * @return A FieldInfo representing the JavaScript field
     */
    public static FieldInfo fromJSField(JSFieldInfo jsField, TypeInfo containingType) {
        String name = jsField.getName();
        
        // Resolve the type from the JS type registry
        TypeInfo type = resolveJSType(jsField.getType());
        
        // JS fields are public by default, readonly maps to final
        int modifiers = Modifier.PUBLIC;
        if (jsField.isReadonly()) {
            modifiers |= Modifier.FINAL;
        }
        
        // Use documentation if available
        JSDocInfo jsDocInfo = jsField.getJsDocInfo();
        String jsDocDesc = jsDocInfo != null ? jsDocInfo.getDescription() : null;
        String documentation = jsDocDesc != null ? jsDocDesc : jsField.getDocumentation();
        
        FieldInfo fieldInfo = new FieldInfo(name, Scope.GLOBAL, type, -1, true, null, documentation, -1, -1, modifiers, null);
        fieldInfo.setJSDocInfo(jsDocInfo);
        return fieldInfo;
    }
    
    /**
     * Resolves a JavaScript type name to a TypeInfo.
     * Handles primitives, mapped types, and custom types from the registry.
     */
    private static TypeInfo resolveJSType(String jsTypeName) {
        if (jsTypeName == null || jsTypeName.isEmpty() || "void".equals(jsTypeName)) {
            return TypeInfo.fromPrimitive("void");
        }
        
        // Handle JS primitives
        switch (jsTypeName) {
            case "string":
                return TypeInfo.fromClass(String.class);
            case "number":
                return TypeInfo.fromClass(double.class);
            case "boolean":
                return TypeInfo.fromClass(boolean.class);
            case "any":
                return TypeInfo.fromClass(Object.class);
            case "void":
                return TypeInfo.fromPrimitive("void");
        }
        
        // Handle array types
        if (jsTypeName.endsWith("[]")) {
            String elementType = jsTypeName.substring(0, jsTypeName.length() - 2);
            TypeInfo elementTypeInfo = resolveJSType(elementType);
            return TypeInfo.arrayOf(elementTypeInfo);
        }
        
        // Try to resolve from the JS type registry
        JSTypeRegistry registry = JSTypeRegistry.getInstance();
        if (registry != null) {
            JSTypeInfo jsTypeInfo = registry.getType(jsTypeName);
            if (jsTypeInfo != null) {
                return TypeInfo.fromJSTypeInfo(jsTypeInfo);
            }
        }
        
        // Fallback: unresolved type
        return TypeInfo.unresolved(jsTypeName, jsTypeName);
    }
    
    /**
     * Create a FieldInfo for an enum constant.
     * @param name The constant name (e.g., "NORTH")
     * @param type The enum type itself
     * @param declOffset The declaration position
     * @param initStart The position of '(' if args present, else -1
     * @param initEnd The position after ')' if args present, else -1
     */
    public static FieldInfo enumConstant(String name, TypeInfo type, int declOffset, int initStart, int initEnd,Field javaField) {
        // Enum constants are implicitly public static final
        int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        return new FieldInfo(name, Scope.ENUM_CONSTANT, type, declOffset, true, null, null, initStart, initEnd,
                modifiers, javaField);
    }

    // ==================== ENUM HANDLING ====================

    public void setEnumConstantInfo(EnumConstantInfo enumConstantInfo) {
        this.enumConstantInfo = enumConstantInfo;
    }

    public EnumConstantInfo getEnumInfo() {
        return enumConstantInfo;
    }
    /**
     * Check if this is an enum constant.
     */
    public boolean isEnumConstant() {
        return scope == Scope.ENUM_CONSTANT;
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
     * Prioritizes LHS matches over RHS matches.
     */
    public AssignmentInfo findAssignmentAtPosition(int position) {
        // Check declaration assignment first
        if (declarationAssignment != null && declarationAssignment.containsPosition(position)) {
            // Prioritize LHS match
            if (declarationAssignment.containsLhsPosition(position)) {
                return declarationAssignment;
            }
        }

        // Check other assignments for LHS matches first (more specific)
        for (AssignmentInfo assign : assignments) {
            if (assign.containsLhsPosition(position)) {
                return assign;
            }
        }

        // Then check RHS matches (less specific - might just be a reference in the value)
        if (declarationAssignment != null && declarationAssignment.containsRhsPosition(position)) {
            return declarationAssignment;
        }

        for (AssignmentInfo assign : assignments) {
            if (assign.containsRhsPosition(position)) {
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

    // ==================== TYPE INFERENCE ====================
    
    /**
     * Get the inferred type for this field.
     * This is set when a variable declared as "any" has its type refined through
     * assignment analysis or JSDoc comments.
     */
    public TypeInfo getInferredType() {
        return inferredType;
    }
    
    /**
     * Set the inferred type for this field.
     * Used when type inference determines a more specific type than the declared type.
     * @param inferredType The refined type based on assignments or JSDoc
     */
    public void setInferredType(TypeInfo inferredType) {
        this.inferredType = inferredType;
    }
    
    /**
     * Check if this field has a type that can be refined (currently "any" type).
     */
    public boolean canInferType() {
        return declaredType != null && "any".equals(declaredType.getFullName());
    }
    
    /**
     * Get the effective type for this field, considering inference.
     * Returns inferredType if available, otherwise declaredType.
     */
    public TypeInfo getEffectiveType() {
        if (inferredType != null) {
            return inferredType;
        }
        return declaredType;
    }

    // ==================== BASIC GETTERS ====================
    
    public String getName() { return name; }
    public Scope getScope() { return scope; }
    public TypeInfo getDeclaredType() { return declaredType; }
    public TypeInfo getTypeInfo() { 
        // Return the effective type (inferred if available, otherwise declared)
        return getEffectiveType(); 
    }
    public int getDeclarationOffset() { return declarationOffset; }
    public boolean isResolved() { return resolved; }
    public MethodInfo getContainingMethod() { return containingMethod; }
    public String getDocumentation() { return documentation; }
    public JSDocInfo getJSDocInfo() { return jsDocInfo; }
    public void setJSDocInfo(JSDocInfo jsDocInfo) { this.jsDocInfo = jsDocInfo; }
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
                // Static final fields get special highlighting
                if (isStatic() && isFinal()) {
                    return TokenType.STATIC_FINAL_FIELD;
                }
                return TokenType.GLOBAL_FIELD;
            case LOCAL:
                return TokenType.LOCAL_FIELD;
            case PARAMETER:
                return TokenType.PARAMETER;
            case ENUM_CONSTANT:
                return TokenType.ENUM_CONSTANT;
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
