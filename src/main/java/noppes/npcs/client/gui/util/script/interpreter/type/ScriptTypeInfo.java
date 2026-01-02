package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.EnumConstantInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodSignature;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    
    // Enum constants (for enum types only) - name -> EnumConstantInfo
    private final Map<String, EnumConstantInfo> enumConstants = new HashMap<>();
    
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
    
    // ==================== ENUM CONSTANT MANAGEMENT ====================
    
    /**
     * Add an enum constant to this enum type.
     * Only valid for enum types.
     */
    public void addEnumConstant(EnumConstantInfo constant) {
        if (isEnum()) {
            enumConstants.put(constant.getFieldInfo().getName(), constant);
        }
    }
    
    /**
     * Check if this enum has a constant with the given name.
     */
    public boolean hasEnumConstant(String constantName) {
        return enumConstants.containsKey(constantName);
    }
    
    /**
     * Get an enum constant by name.
     */
    public EnumConstantInfo getEnumConstant(String constantName) {
        return enumConstants.get(constantName);
    }
    
    /**
     * Get all enum constants.
     */
    public Map<String, EnumConstantInfo> getEnumConstants() {
        return new HashMap<>(enumConstants);
    }
    
    /**
     * Check if this is an enum type and has any constants.
     */
    public boolean hasEnumConstants() {
        return isEnum() && !enumConstants.isEmpty();
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

    public MethodInfo findConstructor(TypeInfo[] argTypes) {
        for (MethodInfo constructor : constructors) {
            if (constructor.getParameterCount() == argTypes.length) {
                boolean match = true;
                List<FieldInfo> params = constructor.getParameters();
                for (int i = 0; i < argTypes.length; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    if (!TypeChecker.isTypeCompatible(paramType, argTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match)
                    return constructor;
            }
        }
        return null;
    }

    // ==================== INHERITANCE HIERARCHY SEARCH ====================

    /**
     * Check if this type or any of its parent classes has a field with the given name.
     * This recursively searches up the inheritance tree.
     */
    public boolean hasFieldInHierarchy(String fieldName) {
        // Check this class first
        if (hasField(fieldName)) {
            return true;
        }

        // Check parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                return ((ScriptTypeInfo) superClass).hasFieldInHierarchy(fieldName);
            } else {
                // For Java classes, hasField already checks inheritance via reflection
                return superClass.hasField(fieldName);
            }
        }

        return false;
    }

    /**
     * Get field info from this type or any of its parent classes.
     * This recursively searches up the inheritance tree.
     */
    public FieldInfo getFieldInfoInHierarchy(String fieldName) {
        // Check this class first
        FieldInfo field = getFieldInfo(fieldName);
        if (field != null) {
            return field;
        }

        // Check parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                return ((ScriptTypeInfo) superClass).getFieldInfoInHierarchy(fieldName);
            } else {
                // For Java classes, getFieldInfo already checks inheritance via reflection
                return superClass.getFieldInfo(fieldName);
            }
        }

        return null;
    }

    /**
     * Check if this type or any of its parent classes has a method with the given name.
     * This recursively searches up the inheritance tree.
     */
    public boolean hasMethodInHierarchy(String methodName) {
        // Check this class first
        if (hasMethod(methodName)) {
            return true;
        }

        // Check parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                return ((ScriptTypeInfo) superClass).hasMethodInHierarchy(methodName);
            } else {
                // For Java classes, hasMethod already checks inheritance via reflection
                return superClass.hasMethod(methodName);
            }
        }

        return false;
    }

    /**
     * Check if this type or any of its parent classes has a method with the given name and parameter count.
     * This recursively searches up the inheritance tree.
     */
    public boolean hasMethodInHierarchy(String methodName, int paramCount) {
        // Check this class first
        if (hasMethod(methodName, paramCount)) {
            return true;
        }

        // Check parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                return ((ScriptTypeInfo) superClass).hasMethodInHierarchy(methodName, paramCount);
            } else {
                // For Java classes, hasMethod already checks inheritance via reflection
                return superClass.hasMethod(methodName, paramCount);
            }
        }

        return false;
    }

    /**
     * Get method info from this type or any of its parent classes.
     * This recursively searches up the inheritance tree.
     */
    public MethodInfo getMethodInfoInHierarchy(String methodName) {
        // Check this class first
        MethodInfo method = getMethodInfo(methodName);
        if (method != null) {
            return method;
        }

        // Check parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                return ((ScriptTypeInfo) superClass).getMethodInfoInHierarchy(methodName);
            } else {
                // For Java classes, getMethodInfo already checks inheritance via reflection
                return superClass.getMethodInfo(methodName);
            }
        }

        return null;
    }

    /**
     * Get method info with specific parameter count from this type or any of its parent classes.
     * This recursively searches up the inheritance tree.
     */
    public MethodInfo getMethodWithParamCountInHierarchy(String methodName, int paramCount) {
        // Check this class first
        MethodInfo method = getMethodWithParamCount(methodName, paramCount);
        if (method != null) {
            return method;
        }

        // Check parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                return ((ScriptTypeInfo) superClass).getMethodWithParamCountInHierarchy(methodName, paramCount);
            } else {
                // For Java classes, reflection already handles inheritance
                // We need to manually search for matching method
                List<MethodInfo> overloads = superClass.getAllMethodOverloads(methodName);
                for (MethodInfo m : overloads) {
                    if (m.getParameterCount() == paramCount) {
                        return m;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get all method overloads with the given name from this type or any of its parent classes.
     * This recursively searches up the inheritance tree and returns all matching overloads.
     */
    public List<MethodInfo> getAllMethodOverloadsInHierarchy(String methodName) {
        List<MethodInfo> result = new ArrayList<>();

        // Get overloads from this class
        result.addAll(getAllMethodOverloads(methodName));

        // Get overloads from parent class recursively
        if (superClass != null && superClass.isResolved()) {
            if (superClass instanceof ScriptTypeInfo) {
                result.addAll(((ScriptTypeInfo) superClass).getAllMethodOverloadsInHierarchy(methodName));
            } else {
                // For Java classes, getAllMethodOverloads already checks inheritance via reflection
                result.addAll(superClass.getAllMethodOverloads(methodName));
            }
        }

        return result;
    }

    /**
     * Override getBestMethodOverload to search the inheritance hierarchy.
     * This ensures that method overload resolution considers parent class methods.
     */
    @Override
    public MethodInfo getBestMethodOverload(String methodName, TypeInfo[] argTypes) {
        // Get all overloads from this class and parent classes
        List<MethodInfo> allOverloads = getAllMethodOverloadsInHierarchy(methodName);
        if (allOverloads.isEmpty())
            return null;

        // If no arguments provided, try to find zero-arg method
        if (argTypes == null || argTypes.length == 0) {
            for (MethodInfo method : allOverloads) {
                if (method.getParameterCount() == 0) {
                    return method;
                }
            }
            // Fall back to first overload if no zero-arg found
            return allOverloads.get(0);
        }

        // Phase 1: Try exact match
        for (MethodInfo method : allOverloads) {
            if (method.getParameterCount() == argTypes.length) {
                boolean exactMatch = true;
                List<FieldInfo> params = method.getParameters();
                for (int i = 0; i < argTypes.length; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || argType == null || !paramType.equals(argType)) {
                        exactMatch = false;
                        break;
                    }
                }
                if (exactMatch)
                    return method;
            }
        }

        // Phase 2: Try compatible type match (assignability)
        for (MethodInfo method : allOverloads) {
            if (method.getParameterCount() == argTypes.length) {
                boolean compatible = true;
                List<FieldInfo> params = method.getParameters();
                for (int i = 0; i < argTypes.length; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || argType == null) {
                        continue; // Allow null types (unresolved)
                    }
                    // Check if argType can be assigned to paramType
                    if (!noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker.isTypeCompatible(paramType,
                            argType)) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible)
                    return method;
            }
        }

        // Phase 3: Fall back to first overload with matching parameter count
        for (MethodInfo method : allOverloads) {
            if (method.getParameterCount() == argTypes.length) {
                return method;
            }
        }

        // No match found, return first overload
        return allOverloads.get(0);
    }

    /**
     * Override getBestMethodOverload with return type expectation to search the inheritance hierarchy.
     */
    @Override
    public MethodInfo getBestMethodOverload(String methodName, TypeInfo expectedReturnType) {
        List<MethodInfo> allOverloads = getAllMethodOverloadsInHierarchy(methodName);
        if (allOverloads.isEmpty())
            return null;

        // If no expected return type, return first overload
        if (expectedReturnType == null) {
            return allOverloads.get(0);
        }

        // First pass: look for return type compatible overload
        for (MethodInfo method : allOverloads) {
            TypeInfo returnType = method.getReturnType();
            if (returnType != null && noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker.isTypeCompatible(
                    expectedReturnType, returnType)) {
                return method;
            }
        }
        // Second pass: return any overload (first one)
        return allOverloads.get(0);
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
    public void addMissingMethodError(TypeInfo interfaceType, String signature) {
        missingMethodErrors.add(new MissingMethodError(interfaceType, signature));
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

    public static class MissingMethodError {
        private final TypeInfo interfaceType;
        private final String signature;

        public MissingMethodError(TypeInfo interfaceType, String signature) {
            this.interfaceType = interfaceType;
            this.signature = signature;
        }

        public String getMessage() {
            return "Class must implement method '" + signature + "' from interface " + interfaceType.getSimpleName();
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

        public String getMessage() {
            return "Class extends " + parentType.getSimpleName() + " but has no constructor matching " + parentConstructorSignature;
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Validate this script type for missing interface methods and constructor matching.
     */
    @Override
    public void validate() {
        // Check that extending class has a matching constructor
        if (hasSuperClass()) {
            TypeInfo superClass = getSuperClass();
            if (superClass == null || !superClass.isResolved()) {
                setError(ErrorType.UNRESOLVED_PARENT,
                        "Cannot resolve parent class " + getSuperClassName());
            } else {
                validateConstructorChain(superClass);
            }
        }
        
        // Skip validation for interfaces - they don't implement methods
        if (getKind() == Kind.INTERFACE)
            return;

        // Check that all interface methods are implemented
        if (hasImplementedInterfaces()) {
            for (TypeInfo iface : getImplementedInterfaces()) {
                if (iface == null || !iface.isResolved()) {
                    // Mark unresolved interface error
                    setError(ErrorType.UNRESOLVED_INTERFACE, "Cannot resolve interface");
                    continue;
                }
                validateInterfaceImplementation(iface);
            }
        }
    }

    /**
     * Validate that this script type implements all methods from an interface.
     */
    private void validateInterfaceImplementation(TypeInfo iface) {
        // Handle Java interfaces
        Class<?> javaClass = iface.getJavaClass();
        if (javaClass != null && javaClass.isInterface()) {
            try {
                for (Method javaMethod : javaClass.getMethods()) {
                    // Skip static and default methods
                    if (Modifier.isStatic(javaMethod.getModifiers()))
                        continue;

                    // Check if this type has a matching method
                    String methodName = javaMethod.getName();
                    int paramCount = javaMethod.getParameterCount();

                    boolean found = false;
                    List<MethodInfo> overloads = getAllMethodOverloads(methodName);
                    for (MethodInfo method : overloads) {
                        if (parameterTypesMatch(method, javaMethod)) {
                            found = true;
                            break;
                        }
                    }

                    //Limit to one error at a time to not spam all missing methods
                    if (!found && missingMethodErrors.isEmpty())
                        addMissingMethodError(iface, MethodSignature.asString(javaMethod));
                }
            } catch (Exception e) {
            }
            return;
        }

        // Handle script-defined interfaces (ScriptType)
        if (iface instanceof ScriptTypeInfo) {
            ScriptTypeInfo ifaceType = (ScriptTypeInfo) iface;

            // Check all methods declared in the interface
            for (MethodInfo ifaceMethod : ifaceType.getAllMethodsFlat()) {
                MethodSignature ifaceSignature = ifaceMethod.getSignature();
                String methodName = ifaceMethod.getName();

                boolean found = false;
                List<MethodInfo> overloads = getAllMethodOverloads(methodName);
                for (MethodInfo method : overloads) {
                    if (method.getSignature().equals(ifaceSignature)) {
                        found = true;
                        break;
                    }
                }

                //Limit to one error at a time to not spam all missing methods
                if (!found && missingMethodErrors.isEmpty())
                    addMissingMethodError(iface, ifaceSignature.toString());
            }
        }
    }

    /**
     * Validate that this script type has a constructor compatible with its parent class.
     * This checks that for each parent constructor, there's a matching constructor in the child.
     */
    private void validateConstructorChain(TypeInfo superClass) {
        // If no constructors defined in script type, it has an implicit default constructor
        // Check if parent has a no-arg constructor
        if (!hasConstructors()) {
            boolean parentHasNoArg = false;

            if (superClass instanceof ScriptTypeInfo) {
                ScriptTypeInfo parentScript = (ScriptTypeInfo) superClass;
                if (!parentScript.hasConstructors() || parentScript.findConstructor(0) != null) {
                    parentHasNoArg = true;
                }
            } else {
                Class<?> javaClass = superClass.getJavaClass();
                if (javaClass != null) {
                    try {
                        for (java.lang.reflect.Constructor<?> ctor : javaClass.getConstructors()) {
                            if (ctor.getParameterCount() == 0) {
                                parentHasNoArg = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Security error
                    }
                }
            }

            if (!parentHasNoArg) {
                addConstructorMismatchError(superClass, superClass.getSimpleName());
            }
        }
        // If script type has constructors, we'd need to check super() calls - that's more complex
        // For now, we just validate the implicit default constructor case
    }

    /**
     * Check if a MethodInfo's parameter types match a Java reflection Method's parameter types.
     */
    private boolean parameterTypesMatch(MethodInfo methodInfo, Method javaMethod) {
        List<FieldInfo> params = methodInfo.getParameters();
        Class<?>[] javaParams = javaMethod.getParameterTypes();

        if (params.size() != javaParams.length)
            return false;

        for (int i = 0; i < params.size(); i++) {
            TypeInfo paramType = params.get(i).getDeclaredType();
            if (paramType == null)
                continue; // Unresolved param, skip check

            Class<?> javaParamClass = javaParams[i];
            String javaParamName = javaParamClass.getName();

            // Compare type names
            if (!paramType.getFullName().equals(javaParamName) &&
                    !paramType.getSimpleName().equals(javaParamClass.getSimpleName())) {
                return false;
            }
        }

        return true;
    }
}