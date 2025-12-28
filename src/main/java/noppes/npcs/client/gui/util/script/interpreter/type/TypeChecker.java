package noppes.npcs.client.gui.util.script.interpreter.type;

/**
 * Utility class for checking type compatibility between types.
 * Handles primitive widening conversions, boxing/unboxing, and inheritance.
 */
public final class TypeChecker {

    private TypeChecker() {} // Utility class

    /**
     * Check if the actual type is compatible with (assignable to) the expected type.
     * @param expected The expected/target type
     * @param actual The actual/source type
     * @return true if actual can be assigned to expected
     */
    public static boolean isTypeCompatible(TypeInfo expected, TypeInfo actual) {
        if (expected == null) return true; // void can accept anything (shouldn't happen)
        if (actual == null) return true; // Can't verify, assume compatible
        
        // Handle null literal - null is compatible with any reference type (non-primitive)
        if ("<null>".equals(actual.getFullName())) {
            Class<?> expectedClass = expected.getJavaClass();
            if (expectedClass != null && !expectedClass.isPrimitive()) {
                return true; // null can be assigned to any reference type
            }
            // null cannot be assigned to primitive types
            return false;
        }
        
        String expectedName = expected.getSimpleName();
        String actualName = actual.getSimpleName();
        
        if (expectedName == null || actualName == null) return true;
        
        // Exact match by simple name
        if (expectedName.equals(actualName)) return true;
        
        // Exact match by full name
        if (expected.getFullName() != null && actual.getFullName() != null) {
            if (expected.getFullName().equals(actual.getFullName())) return true;
        }
        
        // Primitive widening conversions
        if (isNumericType(expectedName) && isNumericType(actualName)) {
            return canWiden(actualName, expectedName);
        }
        
        // Object type compatibility (check inheritance)
        if (expected.getJavaClass() != null && actual.getJavaClass() != null) {
            Class<?> expectedClass = expected.getJavaClass();
            Class<?> actualClass = actual.getJavaClass();
            
            // Direct assignability
            if (expectedClass.isAssignableFrom(actualClass)) {
                return true;
            }
            
            // Primitive widening with Class objects
            if (isPrimitiveWidening(actualClass, expectedClass)) {
                return true;
            }
            
            // Boxing/unboxing compatibility
            if (isBoxingCompatible(actualClass, expectedClass)) {
                return true;
            }
        }
        
        // Allow boxed/unboxed conversions by name
        if (isPrimitiveOrWrapper(expectedName) && isPrimitiveOrWrapper(actualName)) {
            return getUnboxedName(expectedName).equals(getUnboxedName(actualName));
        }
        
        return false;
    }

    /**
     * Check if the type is a numeric type (including wrappers).
     */
    public static boolean isNumericType(String typeName) {
        switch (typeName) {
            case "byte": case "Byte":
            case "short": case "Short":
            case "int": case "Integer":
            case "long": case "Long":
            case "float": case "Float":
            case "double": case "Double":
            case "char": case "Character":
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if a primitive type can be widened to another type.
     * Follows Java's widening primitive conversion rules.
     */
    public static boolean canWiden(String from, String to) {
        int fromRank = getNumericRank(from);
        int toRank = getNumericRank(to);
        return fromRank <= toRank;
    }

    /**
     * Get the numeric rank for primitive widening conversion.
     * Higher rank can accept lower ranks.
     */
    public static int getNumericRank(String typeName) {
        switch (getUnboxedName(typeName)) {
            case "byte": return 1;
            case "short": case "char": return 2;
            case "int": return 3;
            case "long": return 4;
            case "float": return 5;
            case "double": return 6;
            default: return 0;
        }
    }

    /**
     * Check if the type name is a primitive or its wrapper type.
     */
    public static boolean isPrimitiveOrWrapper(String typeName) {
        switch (typeName) {
            case "byte": case "Byte":
            case "short": case "Short":
            case "int": case "Integer":
            case "long": case "Long":
            case "float": case "Float":
            case "double": case "Double":
            case "char": case "Character":
            case "boolean": case "Boolean":
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the primitive type name from a wrapper type name.
     * Returns the input unchanged if already primitive or not a wrapper.
     */
    public static String getUnboxedName(String typeName) {
        switch (typeName) {
            case "Byte": return "byte";
            case "Short": return "short";
            case "Integer": return "int";
            case "Long": return "long";
            case "Float": return "float";
            case "Double": return "double";
            case "Character": return "char";
            case "Boolean": return "boolean";
            default: return typeName;
        }
    }

    /**
     * Get the wrapper type name from a primitive type name.
     * Returns the input unchanged if already a wrapper or not a primitive.
     */
    public static String getBoxedName(String typeName) {
        switch (typeName) {
            case "byte": return "Byte";
            case "short": return "Short";
            case "int": return "Integer";
            case "long": return "Long";
            case "float": return "Float";
            case "double": return "Double";
            case "char": return "Character";
            case "boolean": return "Boolean";
            default: return typeName;
        }
    }

    /**
     * Check if the type name represents a void type.
     */
    public static boolean isVoidType(String typeName) {
        return typeName == null || typeName.equals("void") || typeName.equals("Void");
    }

    /**
     * Check if the type represents a void type.
     */
    public static boolean isVoidType(TypeInfo type) {
        if (type == null) return true;
        return isVoidType(type.getSimpleName());
    }

    /**
     * Check for primitive widening conversions.
     * byte -> short -> int -> long -> float -> double
     * char -> int -> long -> float -> double
     */
    private static boolean isPrimitiveWidening(Class<?> from, Class<?> to) {
        if (!from.isPrimitive() || !to.isPrimitive()) {
            return false;
        }
        
        if (from == byte.class) {
            return to == short.class || to == int.class || to == long.class || 
                   to == float.class || to == double.class;
        }
        if (from == short.class || from == char.class) {
            return to == int.class || to == long.class || to == float.class || to == double.class;
        }
        if (from == int.class) {
            return to == long.class || to == float.class || to == double.class;
        }
        if (from == long.class) {
            return to == float.class || to == double.class;
        }
        if (from == float.class) {
            return to == double.class;
        }
        return false;
    }

    /**
     * Check for boxing/unboxing compatibility.
     */
    private static boolean isBoxingCompatible(Class<?> from, Class<?> to) {
        if (from.isPrimitive()) {
            Class<?> wrapper = getWrapperClassInternal(from);
            if (wrapper != null && to.isAssignableFrom(wrapper)) {
                return true;
            }
        }
        if (to.isPrimitive()) {
            Class<?> wrapper = getWrapperClassInternal(to);
            if (wrapper != null && wrapper.isAssignableFrom(from)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the wrapper class for a primitive type.
     */
    private static Class<?> getWrapperClassInternal(Class<?> primitive) {
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == char.class) return Character.class;
        if (primitive == short.class) return Short.class;
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == float.class) return Float.class;
        if (primitive == double.class) return Double.class;
        return null;
    }
}

