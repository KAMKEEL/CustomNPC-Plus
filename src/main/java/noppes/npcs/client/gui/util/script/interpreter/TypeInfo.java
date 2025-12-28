package noppes.npcs.client.gui.util.script.interpreter;

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

    private TypeInfo(String simpleName, String fullName, String packageName, 
                     Kind kind, Class<?> javaClass, boolean resolved, TypeInfo enclosingType) {
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.packageName = packageName;
        this.kind = kind;
        this.javaClass = javaClass;
        this.resolved = resolved;
        this.enclosingType = enclosingType;
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

    // Getters
    public String getSimpleName() { return simpleName; }
    public String getFullName() { return fullName; }
    public String getPackageName() { return packageName; }
    public Kind getKind() { return kind; }
    public Class<?> getJavaClass() { return javaClass; }
    public boolean isResolved() { return resolved; }
    public TypeInfo getEnclosingType() { return enclosingType; }
    public boolean isInnerClass() { return enclosingType != null; }

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
     * Check if this type has a field with the given name.
     */
    public boolean hasField(String fieldName) {
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
     * Get FieldInfo for a field by name. Returns null if not found.
     * Creates a synthetic FieldInfo based on reflection data.
     */
    public FieldInfo getFieldInfo(String fieldName) {
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
