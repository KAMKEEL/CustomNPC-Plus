package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents resolved type information for a class/interface/enum.
 * Base class for type metadata. Extended by ScriptTypeInfo for script-defined types.
 */
public class TypeInfo {
    
    public enum Kind {
        CLASS,
        INTERFACE,
        ENUM,
        UNKNOWN
    }

    private final String simpleName;       // e.g., "List", "ColorType"
    private final String fullName;         // e.g., "java.util.List", "kamkeel...IOverlay$ColorType"
    private final String packageName;      // e.g., "java.util", "kamkeel.npcdbc.api.client.overlay"
    private final Kind kind;               // CLASS, INTERFACE, ENUM
    private final Class<?> javaClass;      // The actual resolved Java class (null if unresolved)
    private final boolean resolved;        // Whether this type was successfully resolved
    private final TypeInfo enclosingType;  // For inner classes, the outer type (null if top-level)
    private final boolean staticContext;   // true if this is a class reference (can only access static members), false if instance

    private TypeInfo(String simpleName, String fullName, String packageName, 
                     Kind kind, Class<?> javaClass, boolean resolved, TypeInfo enclosingType, boolean staticContext) {
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.packageName = packageName;
        this.kind = kind;
        this.javaClass = javaClass;
        this.resolved = resolved;
        this.enclosingType = enclosingType;
        this.staticContext = staticContext;
    }
    
    // Protected constructor for subclasses (like ScriptTypeInfo)
    protected TypeInfo(String simpleName, String fullName, String packageName,
                       Kind kind, Class<?> javaClass, boolean resolved, TypeInfo enclosingType,
                       boolean staticContext, @SuppressWarnings("unused") boolean subclass) {
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.packageName = packageName;
        this.kind = kind;
        this.javaClass = javaClass;
        this.resolved = resolved;
        this.enclosingType = enclosingType;
        this.staticContext = staticContext;
    }

    // Factory methods
    public static TypeInfo resolved(String simpleName, String fullName, String packageName, 
                                    Kind kind, Class<?> javaClass) {
        return new TypeInfo(simpleName, fullName, packageName, kind, javaClass, true, null, false);
    }

    public static TypeInfo resolvedInner(String simpleName, String fullName, String packageName,
                                         Kind kind, Class<?> javaClass, TypeInfo enclosing) {
        return new TypeInfo(simpleName, fullName, packageName, kind, javaClass, true, enclosing, false);
    }

    public static TypeInfo unresolved(String simpleName, String fullPath) {
        int lastDot = fullPath.lastIndexOf('.');
        String pkg = lastDot > 0 ? fullPath.substring(0, lastDot) : "";
        return new TypeInfo(simpleName, fullPath, pkg, Kind.UNKNOWN, null, false, null, false);
    }

    public static TypeInfo fromClass(Class<?> clazz) {
        if (clazz == null) return null;
        
        Kind kind;
        if (clazz.isInterface()) {
            kind = Kind.INTERFACE;
        } else if (clazz.isEnum()) {
            kind = Kind.ENUM;
        } else {
            kind = Kind.CLASS;
        }

        String fullName = clazz.getName();
        String simpleName = clazz.getSimpleName();
        Package pkg = clazz.getPackage();
        String packageName = "";
        if (pkg != null) {
            packageName = pkg.getName();
        } else if (!fullName.equals(simpleName)) {
            int lastDot = fullName.lastIndexOf('.');
            if (lastDot > 0) {
                packageName = fullName.substring(0, lastDot);
            }
        }

        TypeInfo enclosing = null;
        if (clazz.getEnclosingClass() != null) {
            enclosing = fromClass(clazz.getEnclosingClass());
        }

        return new TypeInfo(simpleName, fullName, packageName, kind, clazz, true, enclosing, false);
    }

    /**
     * Create a TypeInfo for a primitive type.
     */
    public static TypeInfo fromPrimitive(String typeName) {
        Class<?> primitiveClass = null;
        switch (typeName) {
            case "boolean": primitiveClass = boolean.class; break;
            case "byte": primitiveClass = byte.class; break;
            case "char": primitiveClass = char.class; break;
            case "short": primitiveClass = short.class; break;
            case "int": primitiveClass = int.class; break;
            case "long": primitiveClass = long.class; break;
            case "float": primitiveClass = float.class; break;
            case "double": primitiveClass = double.class; break;
            case "void": primitiveClass = void.class; break;
        }
        return new TypeInfo(typeName, typeName, "", Kind.CLASS, primitiveClass, true, null, false);
    }

    // Getters
    public String getSimpleName() { return simpleName; }
    public String getFullName() { return fullName; }
    public String getPackageName() { return packageName; }
    public Kind getKind() { return kind; }
    public Class<?> getJavaClass() { return javaClass; }
    public boolean isResolved() { return resolved; }
    public TypeInfo getEnclosingType() { return enclosingType; }
    public boolean isInnerClass() { return enclosingType != null; }
    public boolean isStaticContext() { return staticContext; }

    /**
     * Create a new TypeInfo with static context (for class references).
     * Used when referencing a class name directly, which can only access static members.
     */
    public TypeInfo asStaticContext() {
        if (staticContext) return this;
        return new TypeInfo(simpleName, fullName, packageName, kind, javaClass, resolved, enclosingType, true);
    }

    /**
     * Create a new TypeInfo with instance context (for object instances).
     * Used when referencing an instance of a class, which can access both static and instance members.
     */
    public TypeInfo asInstanceContext() {
        if (!staticContext) return this;
        return new TypeInfo(simpleName, fullName, packageName, kind, javaClass, resolved, enclosingType, false);
    }

    /**
     * Get the appropriate TokenType for highlighting this type.
     */
    public TokenType getTokenType() {
        if (!resolved) {
            return TokenType.UNDEFINED_VAR;
        }
        switch (kind) {
            case INTERFACE:
                return TokenType.INTERFACE_DECL;
            case ENUM:
                return TokenType.ENUM_DECL;
            case CLASS:
            default:
                return TokenType.IMPORTED_CLASS;
        }
    }

    /**
     * Check if this type has a method with the given name.
     */
    public boolean hasMethod(String methodName) {
        if (javaClass == null) return false;
        try {
            for (java.lang.reflect.Method m : javaClass.getMethods()) {
                if (m.getName().equals(methodName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return false;
    }

    /**
     * Check if this type has a method with the given name and parameter count.
     */
    public boolean hasMethod(String methodName, int paramCount) {
        if (javaClass == null) return false;
        try {
            for (java.lang.reflect.Method m : javaClass.getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return false;
    }
    
    /**
     * Check if this type has constructors.
     * Override in ScriptTypeInfo for script-defined types.
     */
    public boolean hasConstructors() {
        if (javaClass == null) return false;
        try {
            return javaClass.getConstructors().length > 0;
        } catch (Exception e) {
            // Security or linkage error
            return false;
        }
    }
    
    /**
     * Get the constructors for this type.
     * Override in ScriptTypeInfo for script-defined types.
     */
    public List<MethodInfo> getConstructors() {
        List<MethodInfo> result = new ArrayList<>();
        if (javaClass == null) return result;
        
        try {
            java.lang.reflect.Constructor<?>[] constructors = javaClass.getConstructors();
            for (java.lang.reflect.Constructor<?> ctor : constructors) {
                result.add(MethodInfo.fromReflectionConstructor(ctor, this));
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return result;
    }
    
    /**
     * Find the best matching constructor for the given argument count.
     * Override in ScriptTypeInfo for script-defined types.
     */
    public MethodInfo findConstructor(int argCount) {
        if (javaClass == null) return null;
        
        try {
            java.lang.reflect.Constructor<?>[] constructors = javaClass.getConstructors();
            for (java.lang.reflect.Constructor<?> ctor : constructors) {
                if (ctor.getParameterCount() == argCount) {
                    return MethodInfo.fromReflectionConstructor(ctor, this);
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }

    /**
     * Check if this type has a field with the given name that is accessible in the current context.
     * In static context (class reference), only static fields are accessible.
     * In instance context, both static and instance fields are accessible.
     */
    public boolean hasField(String fieldName) {
        if (javaClass == null) return false;
        try {
            for (java.lang.reflect.Field f : javaClass.getFields()) {
                if (f.getName().equals(fieldName)) {
                    // In static context, only static fields are accessible
                    if (staticContext && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return false;
    }

    /**
     * Get MethodInfo for a method by name. Returns null if not found.
     * Creates a synthetic MethodInfo based on reflection data.
     */
    public MethodInfo getMethodInfo(String methodName) {
        if (javaClass == null) return null;
        try {
            for (java.lang.reflect.Method m : javaClass.getMethods()) {
                if (m.getName().equals(methodName)) {
                    // Create a synthetic MethodInfo from reflection
                    return MethodInfo.fromReflection(m, this);
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }

    /**
     * Get all MethodInfo overloads for a method by name.
     * Returns an empty list if not found.
     */
    public java.util.List<MethodInfo> getAllMethodOverloads(String methodName) {
        java.util.List<MethodInfo> overloads = new java.util.ArrayList<>();
        if (javaClass == null) return overloads;
        try {
            for (java.lang.reflect.Method m : javaClass.getMethods()) {
                if (m.getName().equals(methodName)) {
                    overloads.add(MethodInfo.fromReflection(m, this));
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return overloads;
    }

    /**
     * Find the best matching method overload considering return type.
     * First tries to find a match with compatible return type, then falls back to any match.
     * 
     * @param methodName The name of the method
     * @param expectedReturnType The expected return type (can be null)
     * @return The best matching MethodInfo, or null if not found
     */
    public MethodInfo getBestMethodOverload(String methodName, TypeInfo expectedReturnType) {
        java.util.List<MethodInfo> overloads = getAllMethodOverloads(methodName);
        if (overloads.isEmpty()) return null;
        
        // If no expected return type, return first overload
        if (expectedReturnType == null) {
            return overloads.get(0);
        }
        
        // First pass: look for return type compatible overload
        for (MethodInfo method : overloads) {
            TypeInfo returnType = method.getReturnType();
            if (returnType != null && TypeChecker.isTypeCompatible(expectedReturnType, returnType)) {
                return method;
            }
        }
        // Second pass: return any overload (first one)
        return overloads.get(0);
    }

    /**
     * Find the best matching method overload based on argument types.
     * Uses Java's method resolution rules: exact match, then numeric promotion, then widening conversion, then autoboxing.
     * 
     * @param methodName The name of the method
     * @param argTypes The types of the arguments being passed
     * @return The best matching MethodInfo, or null if not found
     */
    public MethodInfo getBestMethodOverload(String methodName, TypeInfo[] argTypes) {
        java.util.List<MethodInfo> overloads = getAllMethodOverloads(methodName);
        if (overloads.isEmpty()) return null;
        
        // If no arguments provided, try to find zero-arg method
        if (argTypes == null || argTypes.length == 0) {
            for (MethodInfo method : overloads) {
                if (method.getParameterCount() == 0) {
                    return method;
                }
            }
            // Fall back to first overload if no zero-arg found
            return overloads.get(0);
        }
        
        // Phase 1: Try exact match
        for (MethodInfo method : overloads) {
            if (method.getParameterCount() == argTypes.length) {
                boolean exactMatch = true;
                java.util.List<FieldInfo> params = method.getParameters();
                for (int i = 0; i < argTypes.length; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || argType == null || !paramType.equals(argType)) {
                        exactMatch = false;
                        break;
                    }
                }
                if (exactMatch) return method;
            }
        }
        
        // Phase 2: Try numeric promotion match
        // For methods with all numeric parameters, find the narrowest common type that all args can promote to
        MethodInfo bestNumericMatch = null;
        int bestNumericRank = Integer.MAX_VALUE;
        
        for (MethodInfo method : overloads) {
            if (method.getParameterCount() == argTypes.length) {
                java.util.List<FieldInfo> params = method.getParameters();
                
                // Check if all parameters and arguments are numeric primitives
                boolean allNumeric = true;
                for (int i = 0; i < argTypes.length; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || argType == null || 
                        !TypeChecker.isNumericPrimitive(paramType) || !TypeChecker.isNumericPrimitive(argType)) {
                        allNumeric = false;
                        break;
                    }
                }
                
                if (allNumeric) { 
                    // Check if all parameters are the same numeric type
                    TypeInfo commonParamType = params.get(0).getTypeInfo();
                    boolean allParamsSame = true;
                    for (int i = 1; i < params.size(); i++) {
                        TypeInfo paramType = params.get(i).getTypeInfo();
                        if (!paramType.equals(commonParamType)) {
                            allParamsSame = false;
                            break;
                        }
                    }
                    
                    // If all parameters are the same numeric type, check if args can promote to it
                    if (allParamsSame) {
                        boolean canPromote = true;
                        for (int i = 0; i < argTypes.length; i++) {
                            if (!TypeChecker.canPromoteNumeric(argTypes[i], commonParamType)) {
                                canPromote = false;
                                break;
                            }
                        }
                        
                        // If all args can promote, check if this is the narrowest match so far
                        if (canPromote) {
                            int paramRank = TypeChecker.getNumericRank(commonParamType.getJavaClass());
                            if (paramRank < bestNumericRank) {
                                bestNumericRank = paramRank;
                                bestNumericMatch = method;
                            }
                        }
                    }
                }
            }
        }
        
        if (bestNumericMatch != null) {
            return bestNumericMatch;
        }
        
        // Phase 3: Try compatible match (widening, autoboxing, subtyping)
        for (MethodInfo method : overloads) {
            if (method.getParameterCount() == argTypes.length) {
                boolean compatible = true;
                java.util.List<FieldInfo> params = method.getParameters();
                for (int i = 0; i < argTypes.length; i++) {
                    TypeInfo paramType = params.get(i).getTypeInfo();
                    TypeInfo argType = argTypes[i];
                    if (paramType == null || argType == null || !TypeChecker.isTypeCompatible(paramType, argType)) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible) return method;
            }
        }
        
        // Phase 4: Return first overload as fallback
        return overloads.get(0);
    }

    /**
     * Get FieldInfo for a field by name. Returns null if not found or not accessible.
     * In static context (class reference), only static fields are accessible.
     * In instance context, both static and instance fields are accessible.
     * Creates a synthetic FieldInfo based on reflection data.
     */
    public FieldInfo getFieldInfo(String fieldName) {
        if (javaClass == null) return null;
        try {
            for (java.lang.reflect.Field f : javaClass.getFields()) {
                if (f.getName().equals(fieldName)) {
                    // In static context, only static fields are accessible
                    if (staticContext && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        return null; // Non-static field not accessible from static context
                    }
                    // Create a synthetic FieldInfo from reflection
                    // Always return instance context for the field's type (field values are instances)
                    FieldInfo fieldInfo = FieldInfo.fromReflection(f, this);
                    // The field's type should be in instance context since we're accessing a value
                    return fieldInfo;
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }

    @Override
    public String toString() {
        return "TypeInfo{" + fullName + ", " + kind + ", resolved=" + resolved + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeInfo typeInfo = (TypeInfo) o;
        return fullName.equals(typeInfo.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }
}
