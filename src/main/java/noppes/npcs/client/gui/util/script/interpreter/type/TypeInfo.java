package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.field.EnumConstantInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSFieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSMethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.TypeParamInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents resolved type information for a class/interface/enum.
 * 
 * This is the unified type system that supports:
 * - Java types (via Class<?> reflection)
 * - Script-defined types (via ScriptTypeInfo subclass)
 * - JavaScript/TypeScript types (via JSTypeInfo bridge)
 * 
 * For JavaScript types, the jsTypeInfo field holds the parsed .d.ts data,
 * and methods like hasMethod/getMethodInfo delegate to it.
 */
public class TypeInfo {
    
    public enum Kind {
        CLASS,
        INTERFACE,
        ENUM,
        UNKNOWN
    }
    
    /**
     * Singleton constant for the void type.
     */
    public static final TypeInfo VOID = fromPrimitive("void");
    
    public static final TypeInfo BOOLEAN = fromPrimitive("boolean");
    
    public static final TypeInfo STRING = TypeInfo.fromClass(String.class);
    
    /**
     * Singleton constant for the "any" type (used in JavaScript/TypeScript).
     * The "any" type is universally compatible - it can be assigned to anything
     * and anything can be assigned to it.
     */
    public static final TypeInfo ANY = new TypeInfo("any", "any", "", Kind.CLASS, null, true, null);
    public static final TypeInfo NUMBER = new TypeInfo("number", "number", "", Kind.CLASS, double.class, true, null);
    
    
    private final String simpleName;       // e.g., "List", "ColorType"
    private final String fullName;         // e.g., "java.util.List", "kamkeel...IOverlay$ColorType"
    private final String packageName;      // e.g., "java.util", "kamkeel.npcdbc.api.client.overlay"
    private final Kind kind;               // CLASS, INTERFACE, ENUM
    private final Class<?> javaClass;      // The actual resolved Java class (null if unresolved or JS type)
    private final boolean resolved;        // Whether this type was successfully resolved
    private final TypeInfo enclosingType;  // For inner classes, the outer type (null if top-level)
    
    // JavaScript/TypeScript type info (for types from .d.ts files)
    private final JSTypeInfo jsTypeInfo;   // The JS type info (null if Java type)
    
    // Type parameters (generics)
    private final List<TypeParamInfo> typeParams = new ArrayList<>();

    private TypeInfo(String simpleName, String fullName, String packageName, 
                     Kind kind, Class<?> javaClass, boolean resolved, TypeInfo enclosingType) {
        this(simpleName, fullName, packageName, kind, javaClass, resolved, enclosingType, null);
    }
    
    private TypeInfo(String simpleName, String fullName, String packageName, 
                     Kind kind, Class<?> javaClass, boolean resolved, TypeInfo enclosingType,
                     JSTypeInfo jsTypeInfo) {
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.packageName = packageName;
        this.kind = kind;
        this.javaClass = javaClass;
        this.resolved = resolved;
        this.enclosingType = enclosingType;
        this.jsTypeInfo = jsTypeInfo;
    }
    
    // Protected constructor for subclasses (like ScriptTypeInfo)
    protected TypeInfo(String simpleName, String fullName, String packageName,
                       Kind kind, Class<?> javaClass, boolean resolved, TypeInfo enclosingType,
                       @SuppressWarnings("unused") boolean subclass) {
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.packageName = packageName;
        this.kind = kind;
        this.javaClass = javaClass;
        this.resolved = resolved;
        this.enclosingType = enclosingType;
        this.jsTypeInfo = null;
    }

    // Factory methods
    public static TypeInfo resolved(String simpleName, String fullName, String packageName, 
                                    Kind kind, Class<?> javaClass) {
        return new TypeInfo(simpleName, fullName, packageName, kind, javaClass, true, null);
    }

    public static TypeInfo resolvedInner(String simpleName, String fullName, String packageName,
                                         Kind kind, Class<?> javaClass, TypeInfo enclosing) {
        return new TypeInfo(simpleName, fullName, packageName, kind, javaClass, true, enclosing);
    }

    public static TypeInfo unresolved(String simpleName, String fullPath) {
        int lastDot = fullPath.lastIndexOf('.');
        String pkg = lastDot > 0 ? fullPath.substring(0, lastDot) : "";
        return new TypeInfo(simpleName, fullPath, pkg, Kind.UNKNOWN, null, false, null);
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

        return new TypeInfo(simpleName, fullName, packageName, kind, clazz, true, enclosing);
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
        return new TypeInfo(typeName, typeName, "", Kind.CLASS, primitiveClass, true, null);
    }
    
    /**
     * Create a TypeInfo from a JavaScript/TypeScript type.
     * This bridges JS types parsed from .d.ts files into the unified type system.
     * 
     * @param jsType The parsed JS type info
     * @return A TypeInfo wrapping the JS type
     */
    public static TypeInfo fromJSTypeInfo(JSTypeInfo jsType) {
        if (jsType == null) return null;
        
        String simpleName = jsType.getSimpleName();
        String fullName = jsType.getFullName();
        String namespace = jsType.getNamespace();
        
        // JS interfaces are always Kind.INTERFACE
        return new TypeInfo(simpleName, fullName, namespace != null ? namespace : "", 
                           Kind.INTERFACE, null, true, null, jsType);
    }
    
    /**
     * Create an array type wrapping the given element type.
     * 
     * @param elementType The type of elements in the array
     * @return A TypeInfo representing the array type
     */
    public static TypeInfo arrayOf(TypeInfo elementType) {
        if (elementType == null) {
            return fromClass(Object[].class);
        }
        
        String simpleName = elementType.getSimpleName() + "[]";
        String fullName = elementType.getFullName() + "[]";
        String pkg = elementType.getPackageName();
        
        // Try to get the actual array class if we have a Java class
        Class<?> arrayClass = null;
        if (elementType.getJavaClass() != null) {
            try {
                arrayClass = java.lang.reflect.Array.newInstance(elementType.getJavaClass(), 0).getClass();
            } catch (Exception e) {
                // Fallback to Object array if we can't create the specific array type
            }
        }
        
        return new TypeInfo(simpleName, fullName, pkg, Kind.CLASS, arrayClass, true, null);
    }
    
    /**
     * Check if this is a JavaScript type (backed by JSTypeInfo).
     */
    public boolean isJSType() {
        return jsTypeInfo != null;
    }
    
    /**
     * Get the underlying JSTypeInfo if this is a JS type.
     */
    public JSTypeInfo getJSTypeInfo() {
        return jsTypeInfo;
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
    public boolean isInterface() {return kind == Kind.INTERFACE;}
    public boolean isEnum() {return kind == Kind.ENUM;}
    public boolean isPrimitive() {return javaClass != null && javaClass.isPrimitive();}
    
    /**
     * Get the appropriate TokenType for highlighting this type.
     */
    public TokenType getTokenType() {
        if (!resolved) 
            return TokenType.UNDEFINED_VAR;
        if(isPrimitive())
            return TokenType.KEYWORD;
        
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
        // Check JS type first
        if (jsTypeInfo != null) {
            return jsTypeInfo.hasMethod(methodName);
        }
        
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
        // Check JS type first
        if (jsTypeInfo != null) {
            List<JSMethodInfo> overloads = jsTypeInfo.getMethodOverloads(methodName);
            for (JSMethodInfo m : overloads) {
                if (m.getParameterCount() == paramCount) {
                    return true;
                }
            }
            return false;
        }
        
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
           Constructor<?>[] constructors = javaClass.getConstructors();
            for (Constructor<?> ctor : constructors) {
                if (ctor.getParameterCount() == argCount) {
                    return MethodInfo.fromReflectionConstructor(ctor, this);
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }
    
    public MethodInfo findConstructor(TypeInfo[] argTypes) {
        if (javaClass == null) return null;
        
        try {
            Constructor<?>[] constructors = javaClass.getConstructors();
            for (Constructor<?> ctor : constructors) {
                if (ctor.getParameterCount() == argTypes.length) {
                   Class<?>[] paramTypes = ctor.getParameterTypes();
                    boolean match = true;
                    for (int i = 0; i < argTypes.length; i++) {
                        TypeInfo paramTypeInfo = TypeInfo.fromClass(paramTypes[i]);
                        if (!TypeChecker.isTypeCompatible(paramTypeInfo, argTypes[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return MethodInfo.fromReflectionConstructor(ctor, this);
                    }
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }

    /**
     * Check if this type has a field with the given name.
     */
    public boolean hasField(String fieldName) {
        // Check JS type first
        if (jsTypeInfo != null) {
            return jsTypeInfo.hasField(fieldName);
        }
        
        if (javaClass == null) return false;
        try {
            for (java.lang.reflect.Field f : javaClass.getFields()) {
                if (f.getName().equals(fieldName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return false;
    }

    public boolean hasEnumConstant(String constantName) {
        if (javaClass == null || !javaClass.isEnum()) return false;
        try {
            Object[] constants = javaClass.getEnumConstants();
            for (Object constant : constants) {
                if (constant.toString().equals(constantName)) 
                    return true;
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return false;
    }

    /**
     * Get an enum constant by name.
     */
    public EnumConstantInfo getEnumConstant(String constantName) {
        if (javaClass == null || !javaClass.isEnum()) return null;
        try {
            Object[] constants = javaClass.getEnumConstants();
            for (Object constant : constants) {
                if (constant.toString().equals(constantName)) 
                    return EnumConstantInfo.fromReflection(constantName, this, null);
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }

    /**
     * Get MethodInfo for a method by name. Returns null if not found.
     * Creates a synthetic MethodInfo based on reflection data or JS type data.
     */
    public MethodInfo getMethodInfo(String methodName) {
        // Check JS type first
        if (jsTypeInfo != null) {
            JSMethodInfo jsMethod = jsTypeInfo.getMethod(methodName);
            if (jsMethod != null) {
                return MethodInfo.fromJSMethod(jsMethod, this);
            }
            return null;
        }
        
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
        
        // Check JS type first
        if (jsTypeInfo != null) {
            for (JSMethodInfo jsMethod : jsTypeInfo.getMethodOverloads(methodName)) {
                // Type parameter resolution now happens inside fromJSMethod using this TypeInfo
                overloads.add(MethodInfo.fromJSMethod(jsMethod, this));
            }
            return overloads;
        }
        
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
     * Get FieldInfo for a field by name. Returns null if not found.
     * Creates a synthetic FieldInfo based on reflection data or JS type data.
     */
    public FieldInfo getFieldInfo(String fieldName) {
        // Check JS type first
        if (jsTypeInfo != null) {
            JSFieldInfo jsField = jsTypeInfo.getField(fieldName);
            if (jsField != null) {
                return FieldInfo.fromJSField(jsField, this);
            }
            return null;
        }
        
        if (javaClass == null) return null;
        try {
            for (java.lang.reflect.Field f : javaClass.getFields()) {
                if (f.getName().equals(fieldName)) {
                    // Create a synthetic FieldInfo from reflection
                    return FieldInfo.fromReflection(f, this);
                }
            }
        } catch (Exception e) {
            // Security or linkage error
        }
        return null;
    }

    /**
     * Validate this type. Default implementation does nothing (for Java types).
     * Override in ScriptTypeInfo to validate script-defined types.
     */
    public void validate() {
        // Default: no validation for Java types
    }
    
    // ==================== Type Parameter Methods ====================
    
    /**
     * Add a type parameter to this type.
     * Used during parsing/construction of types with generics.
     */
    public void addTypeParam(TypeParamInfo param) {
        // If this is a JS type, delegate to JSTypeInfo
        if (jsTypeInfo != null) {
            jsTypeInfo.addTypeParam(param);
        } else {
            typeParams.add(param);
        }
    }
    
    /**
     * Get all type parameters for this type.
     * @return List of type parameters (empty if none)
     */
    public List<TypeParamInfo> getTypeParams() {
        // If this is a JS type, delegate to JSTypeInfo
        if (jsTypeInfo != null) {
            return jsTypeInfo.getTypeParams();
        }
        return typeParams;
    }
    
    /**
     * Resolve all type parameters for this type.
     * Called during Phase 2 after all types are loaded into the registry.
     */
    public void resolveTypeParameters() {
        // If this is a JS type, delegate to JSTypeInfo
        if (jsTypeInfo != null) {
            jsTypeInfo.resolveTypeParameters();
        } else {
            for (TypeParamInfo param : typeParams) {
                param.resolveBoundType();
            }
        }
    }
    
    /**
     * Get the type parameter info for a given parameter name (e.g., "T").
     * @return TypeParamInfo or null if not found
     */
    public TypeParamInfo getTypeParam(String name) {
        // If this is a JS type, delegate to JSTypeInfo
        if (jsTypeInfo != null) {
            return jsTypeInfo.getTypeParam(name);
        }
        
        for (TypeParamInfo param : typeParams) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }
    
    /**
     * Resolves a type parameter to its bound TypeInfo.
     * For example, if this type has "T extends EntityPlayerMP", resolveTypeParamToTypeInfo("T") returns the TypeInfo for EntityPlayerMP.
     * If no type parameter is found with that name, returns null.
     */
    public TypeInfo resolveTypeParamToTypeInfo(String typeName) {
        TypeParamInfo param = getTypeParam(typeName);
        if (param != null) {
            return param.getBoundTypeInfo();
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
