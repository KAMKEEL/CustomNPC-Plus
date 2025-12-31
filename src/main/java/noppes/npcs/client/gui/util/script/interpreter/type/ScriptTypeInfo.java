package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

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
    private final List<MethodInfo> constructors = new ArrayList<>(); // List of constructors
    private final List<ScriptTypeInfo> innerClasses = new ArrayList<>();
    
    // Parent class reference (for inner class resolution)
    private ScriptTypeInfo outerClass;
    
    // ==================== INHERITANCE ====================
    
    /** 
     * The parent/super class for this type (from "extends ParentClass").
     * Can be a resolved TypeInfo or ScriptTypeInfo, or unresolved TypeInfo if the parent is not found.
     * Null if this type doesn't extend anything (or extends Object implicitly).
     */
    private TypeInfo superClass;
    
    /** 
     * The raw string name of the super class as written in the script (e.g., "ParentClass").
     * Stored for display purposes even when the type couldn't be resolved.
     */
    private String superClassName;
    
    /** 
     * All implemented interfaces (from "implements Interface1, Interface2, ...").
     * Each can be resolved or unresolved. The list order matches the declaration order.
     */
    private final List<TypeInfo> implementedInterfaces = new ArrayList<>();
    
    /**
     * The raw string names of implemented interfaces as written in the script.
     * Stored for display purposes even when types couldn't be resolved.
     */
    private final List<String> implementedInterfaceNames = new ArrayList<>();
    
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
    
    // ==================== INHERITANCE ====================
    
    /**
     * Get the super class (from "extends"). Can be resolved or unresolved.
     * @return The parent class TypeInfo, or null if no extends clause
     */
    public TypeInfo getSuperClass() { return superClass; }
    
    /**
     * Get the raw super class name as written in the script.
     * @return The super class name string, or null if no extends clause
     */
    public String getSuperClassName() { return superClassName; }
    
    /**
     * Set the super class info. Call this after resolving types.
     * @param superClass The resolved or unresolved TypeInfo for the parent class
     * @param superClassName The raw class name as written in the script
     */
    public void setSuperClass(TypeInfo superClass, String superClassName) {
        this.superClass = superClass;
        this.superClassName = superClassName;
    }
    
    /**
     * Check if this type has a super class (extends something).
     */
    public boolean hasSuperClass() { return superClass != null || superClassName != null; }
    
    /**
     * Get all implemented interfaces. Each can be resolved or unresolved.
     * @return Unmodifiable list of implemented interface TypeInfos
     */
    public List<TypeInfo> getImplementedInterfaces() { 
        return new ArrayList<>(implementedInterfaces); 
    }
    
    /**
     * Get the raw interface names as written in the script.
     * @return Unmodifiable list of interface name strings
     */
    public List<String> getImplementedInterfaceNames() { 
        return new ArrayList<>(implementedInterfaceNames); 
    }
    
    /**
     * Add an implemented interface. Call this after resolving types.
     * @param interfaceType The resolved or unresolved TypeInfo for the interface
     * @param interfaceName The raw interface name as written in the script
     */
    public void addImplementedInterface(TypeInfo interfaceType, String interfaceName) {
        implementedInterfaces.add(interfaceType);
        implementedInterfaceNames.add(interfaceName);
    }
    
    /**
     * Check if this type implements any interfaces.
     */
    public boolean hasImplementedInterfaces() { return !implementedInterfaces.isEmpty(); }
    
    /**
     * Check if this type implements a specific interface (by simple name).
     */
    public boolean implementsInterface(String interfaceName) {
        for (String name : implementedInterfaceNames) {
            if (name.equals(interfaceName)) return true;
        }
        for (TypeInfo ti : implementedInterfaces) {
            if (ti.getSimpleName().equals(interfaceName)) return true;
        }
        return false;
    }
    
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
    public List<MethodInfo> getAllMethodOverloads(String methodName) {
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
    
    public List<MethodInfo> getAllMethodsFlat() {
        List<MethodInfo> allMethods = new ArrayList<>();
        for (List<MethodInfo> overloads : methods.values()) {
            allMethods.addAll(overloads);
        }
        return allMethods;
    }       
    
    // ==================== CONSTRUCTOR MANAGEMENT ====================
    
    public void addConstructor(MethodInfo constructor) {
        constructors.add(constructor);
    }
    
    @Override
    public List<MethodInfo> getConstructors() {
        return new ArrayList<>(constructors);
    }
    
    @Override
    public boolean hasConstructors() {
        return !constructors.isEmpty();
    }
    
    /**
     * Find the best matching constructor for the given argument count.
     * Returns null if no constructor matches.
     */
    @Override
    public MethodInfo findConstructor(int argCount) {
        for (MethodInfo constructor : constructors) {
            if (constructor.getParameterCount() == argCount) {
                return constructor;
            }
        }
        return null;
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
    
    // ==================== ERROR HANDLING ====================
    
    /**
     * Error types for ScriptTypeInfo validation.
     */
    public enum ErrorType {
        NONE,
        MISSING_INTERFACE_METHOD,    // Class doesn't implement all interface methods
        MISSING_CONSTRUCTOR_MATCH,   // Class extends parent but has no matching constructor
        UNRESOLVED_PARENT,           // Parent class cannot be resolved
        UNRESOLVED_INTERFACE         // Implemented interface cannot be resolved
    }
    
    /**
     * Represents a missing interface method error.
     */
    public static class MissingMethodError {
        private final TypeInfo interfaceType;
        private final String methodName;
        private final String signature;
        
        public MissingMethodError(TypeInfo interfaceType, String methodName, String signature) {
            this.interfaceType = interfaceType;
            this.methodName = methodName;
            this.signature = signature;
        }
        
        public TypeInfo getInterfaceType() { return interfaceType; }
        public String getMethodName() { return methodName; }
        public String getSignature() { return signature; }
        
        public String getMessage() {
            return "Class must implement method '" + methodName + signature + "' from interface " + interfaceType.getSimpleName();
        }
    }
    
    /**
     * Represents a constructor mismatch error.
     */
    public static class ConstructorMismatchError {
        private final TypeInfo parentType;
        private final String parentConstructorSignature;
        
        public ConstructorMismatchError(TypeInfo parentType, String parentConstructorSignature) {
            this.parentType = parentType;
            this.parentConstructorSignature = parentConstructorSignature;
        }
        
        public TypeInfo getParentType() { return parentType; }
        public String getParentConstructorSignature() { return parentConstructorSignature; }
        
        public String getMessage() {
            return "Class extends " + parentType.getSimpleName() + " but has no constructor matching " + parentConstructorSignature;
        }
    }
    
    // Error tracking
    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage;
    private final List<MissingMethodError> missingMethodErrors = new ArrayList<>();
    private final List<ConstructorMismatchError> constructorMismatchErrors = new ArrayList<>();
    
    // Error getters
    public ErrorType getErrorType() { return errorType; }
    public String getErrorMessage() { return errorMessage; }
    public boolean hasError() { return errorType != ErrorType.NONE || !missingMethodErrors.isEmpty() || !constructorMismatchErrors.isEmpty(); }
    public List<MissingMethodError> getMissingMethodErrors() { return new ArrayList<>(missingMethodErrors); }
    public List<ConstructorMismatchError> getConstructorMismatchErrors() { return new ArrayList<>(constructorMismatchErrors); }
    
    /**
     * Set a general error on this type.
     */
    public void setError(ErrorType type, String message) {
        this.errorType = type;
        this.errorMessage = message;
    }
    
    /**
     * Add a missing interface method error.
     */
    public void addMissingMethodError(TypeInfo interfaceType, String methodName, String signature) {
        missingMethodErrors.add(new MissingMethodError(interfaceType, methodName, signature));
    }
    
    /**
     * Add a constructor mismatch error.
     */
    public void addConstructorMismatchError(TypeInfo parentType, String parentConstructorSignature) {
        constructorMismatchErrors.add(new ConstructorMismatchError(parentType, parentConstructorSignature));
    }
    
    /**
     * Clear all errors on this type.
     */
    public void clearErrors() {
        errorType = ErrorType.NONE;
        errorMessage = null;
        missingMethodErrors.clear();
        constructorMismatchErrors.clear();
    }
}
