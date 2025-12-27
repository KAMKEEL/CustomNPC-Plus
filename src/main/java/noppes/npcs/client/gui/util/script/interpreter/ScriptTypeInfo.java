package noppes.npcs.client.gui.util.script.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a type defined within the script itself (not from Java classpath).
 * This handles classes, interfaces, and enums declared in the user's script.
 * 
 * Unlike TypeInfo which wraps a Java Class<?>, ScriptTypeInfo stores all metadata
 * about fields, methods, and other type information parsed from the script.
 */
public class ScriptTypeInfo extends TypeInfo {
    
    private final String scriptClassName;
    private final int declarationOffset;
    private final int bodyStart;
    private final int bodyEnd;
    private final int modifiers;  // Java reflection modifiers (public, static, final, etc.)
    
    // Script-defined members
    private final Map<String, FieldInfo> fields = new HashMap<>();
    private final Map<String, List<MethodInfo>> methods = new HashMap<>(); // name -> list of overloads
    private final List<ScriptTypeInfo> innerClasses = new ArrayList<>();
    
    // Parent class reference (for inner class resolution)
    private ScriptTypeInfo outerClass;
    
    private ScriptTypeInfo(String simpleName, String fullName, Kind kind,
                           int declarationOffset, int bodyStart, int bodyEnd, int modifiers) {
        super(simpleName, fullName, "", kind, null, true, null, true);
        this.scriptClassName = simpleName;
        this.declarationOffset = declarationOffset;
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
        this.modifiers = modifiers;
    }
    
    // Factory method
    public static ScriptTypeInfo create(String simpleName, Kind kind, 
                                        int declarationOffset, int bodyStart, int bodyEnd, int modifiers) {
        return new ScriptTypeInfo(simpleName, simpleName, kind, declarationOffset, bodyStart, bodyEnd, modifiers);
    }
    
    public static ScriptTypeInfo createInner(String simpleName, Kind kind, ScriptTypeInfo outer,
                                             int declarationOffset, int bodyStart, int bodyEnd, int modifiers) {
        String fullName = outer.getFullName() + "$" + simpleName;
        ScriptTypeInfo inner = new ScriptTypeInfo(simpleName, fullName, kind, declarationOffset, bodyStart, bodyEnd, modifiers);
        inner.outerClass = outer;
        outer.innerClasses.add(inner);
        return inner;
    }
    
    // Getters
    public String getScriptClassName() { return scriptClassName; }
    public int getDeclarationOffset() { return declarationOffset; }
    public int getBodyStart() { return bodyStart; }
    public int getBodyEnd() { return bodyEnd; }
    public int getModifiers() { return modifiers; }
    public ScriptTypeInfo getOuterClass() { return outerClass; }
    public List<ScriptTypeInfo> getInnerClasses() { return innerClasses; }
    
    /**
     * Check if a position is inside this type's body.
     */
    public boolean containsPosition(int position) {
        return position >= bodyStart && position < bodyEnd;
    }
    
    // ==================== FIELD MANAGEMENT ====================
    
    public void addField(FieldInfo field) {
        fields.put(field.getName(), field);
    }
    
    @Override
    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }
    
    @Override
    public FieldInfo getFieldInfo(String fieldName) {
        return fields.get(fieldName);
    }
    
    public Map<String, FieldInfo> getFields() {
        return new HashMap<>(fields);
    }
    
    // ==================== METHOD MANAGEMENT ====================
    
    public void addMethod(MethodInfo method) {
        methods.computeIfAbsent(method.getName(), k -> new ArrayList<>()).add(method);
    }
    
    @Override
    public boolean hasMethod(String methodName) {
        return methods.containsKey(methodName);
    }
    
    @Override
    public boolean hasMethod(String methodName, int paramCount) {
        List<MethodInfo> overloads = methods.get(methodName);
        if (overloads == null) return false;
        for (MethodInfo m : overloads) {
            if (m.getParameterCount() == paramCount) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public MethodInfo getMethodInfo(String methodName) {
        List<MethodInfo> overloads = methods.get(methodName);
        return (overloads != null && !overloads.isEmpty()) ? overloads.get(0) : null;
    }
    
    /**
     * Get all method overloads with the given name.
     */
    public List<MethodInfo> getMethodOverloads(String methodName) {
        return methods.getOrDefault(methodName, new ArrayList<>());
    }
    
    /**
     * Get a method with specific parameter count.
     */
    public MethodInfo getMethodWithParamCount(String methodName, int paramCount) {
        List<MethodInfo> overloads = methods.get(methodName);
        if (overloads == null) return null;
        for (MethodInfo m : overloads) {
            if (m.getParameterCount() == paramCount) {
                return m;
            }
        }
        return null;
    }
    
    public Map<String, List<MethodInfo>> getMethods() {
        return new HashMap<>(methods);
    }
    
    // ==================== INNER CLASS LOOKUP ====================
    
    /**
     * Find an inner class by name.
     */
    public ScriptTypeInfo getInnerClass(String name) {
        for (ScriptTypeInfo inner : innerClasses) {
            if (inner.getSimpleName().equals(name)) {
                return inner;
            }
        }
        return null;
    }
    
    @Override
    public TokenType getTokenType() {
        switch (getKind()) {
            case INTERFACE:
                return TokenType.INTERFACE_DECL;
            case ENUM:
                return TokenType.ENUM_DECL;
            case CLASS:
            default:
                return TokenType.CLASS_DECL;
        }
    }
    
    // Script types are always considered resolved since they're defined in the script
    @Override
    public boolean isResolved() {
        return true;
    }
    
    // Script types don't have a Java class
    @Override
    public Class<?> getJavaClass() {
        return null;
    }
    
    @Override
    public String toString() {
        return "ScriptTypeInfo{" + scriptClassName + ", " + getKind() + 
               ", fields=" + fields.size() + ", methods=" + methods.size() + "}";
    }
}
