package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.js_parser.JSTypeInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for checking type compatibility between types.
 * Handles primitive widening conversions, boxing/unboxing, and inheritance.
 */
public final class TypeChecker {

    private TypeChecker() {} // Utility class

    private static boolean CURRENT_JAVASCRIPT_MODE;

    public static void enterTypeCheckingContext(boolean javaScriptMode) {
        CURRENT_JAVASCRIPT_MODE = javaScriptMode;
    }

    static boolean isJavaScriptMode() {
        return CURRENT_JAVASCRIPT_MODE;
    }

    /**
     * Check if the actual type is compatible with (assignable to) the expected type.
     * @param expected The expected/target type
     * @param actual The actual/source type
     * @return true if actual can be assigned to expected
     */
    public static boolean isTypeCompatible(TypeInfo expected, TypeInfo actual) {
        if (expected == null) return true; // void can accept anything (shouldn't happen)
        if (actual == null) return true; // Can't verify, assume compatible

        // Exact match by reference equality
        if(expected.equals(actual)) return true;
        
        // Handle array types: compatible if element types are compatible
        if (expected.isArray() && actual.isArray()) {
            TypeInfo expectedElement = expected.getElementType();
            TypeInfo actualElement = actual.getElementType();
            if (expectedElement != null && actualElement != null) 
                return isTypeCompatible(expectedElement, actualElement);
        }
        
        // Script method reference placeholder: only compatible with functional interface params.
        // Used to help overload selection choose SAM overloads in JavaScript.
        if ("__script_method_ref__".equals(actual.getFullName())) {
            return expected.isFunctionalInterface();
        }
        
        // Handle "any" type - universally compatible (JavaScript/TypeScript)
        if ("any".equals(expected.getFullName()) || "any".equals(actual.getFullName())) {
            return true;
        }
        
        if (isJavaScriptMode()) {
            // Treat java.lang.Object as "any", universally compatible
            if ("Object".equals(expected.getSimpleName()) || "Object".equals(actual.getSimpleName()) )
                return true;
            
            // Treat String as universally compatible, since JavaScript string literals can be assigned to String 
            // parameters and String is commonly used as a catch-all type in JS APIs
            if("String".equals(expected.getSimpleName()))
                return true;
        }
        
        // Handle null literal - null is compatible with any reference type (non-primitive)
        if ("<null>".equals(actual.getFullName())) {
            Class<?> expectedClass = expected.getJavaClass();
            if (expectedClass != null && !expectedClass.isPrimitive() || "<null>".equals(expected.getFullName())) {
                return true; // null can be assigned to any reference type and null
            }
            // null cannot be assigned to primitive types
            return false;
        }
        
        String expectedName = expected.getSimpleName();
        String actualName = actual.getSimpleName();
        
        if (expectedName == null || actualName == null) return true;
        
        
        // Primitive widening conversions
        if ("number".equals(expectedName) && isNumericType(actualName)) {
            return true;
        }
        if ("number".equals(actualName) && isNumericType(expectedName)) {
            return true;
        }
        if (isNumericType(expectedName) && isNumericType(actualName)) {
            if (isJavaScriptMode()) return true;
            return canWiden(actualName, expectedName);
        }

        // JS/.d.ts type compatibility (check inheritance chain from actual -> parent)
        if (expected.isJSType() && actual.isJSType()) {
            if (isJSTypeAssignableFrom(expected.getJSTypeInfo(), actual.getJSTypeInfo())) {
                return true;
            }
        }

        // Same-name match: handles ScriptTypeInfo, non-Java types, and same-class Java types.
        // Must run before isAssignableFrom so type arg checking isn't skipped for same-class pairs.
        if (expected.getFullName().equals(actual.getFullName())) {
            if (expected.isParameterized() && actual.isParameterized()) {
                return areTypeArgumentsCompatible(expected, actual);
            }
            return true;
        }

        // ScriptTypeInfo inheritance: actual is a script-defined type that may extend/implement expected.
        // ScriptTypeInfo.getJavaClass() returns null, so the Java isAssignableFrom gate below won't fire.
        if (actual instanceof ScriptTypeInfo) {
            if (isScriptTypeAssignableTo((ScriptTypeInfo) actual, expected)) {
                return true;
            }
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
     * Check assignability for JS/.d.ts types by walking the resolved parent chain.
     */
    private static boolean isJSTypeAssignableFrom(JSTypeInfo expected, JSTypeInfo actual) {
        if (expected == null || actual == null) {
            return false;
        }

        Set<String> visited = new HashSet<>();
        JSTypeInfo current = actual;
        while (current != null) {
            String currentFullName = current.getFullName();
            if (currentFullName == null || !visited.add(currentFullName)) {
                break;
            }

            if (expected.getFullName() != null && expected.getFullName().equals(currentFullName)) {
                return true;
            }

            current = current.getResolvedParent();
        }

        return false;
    }

    /**
     * Check assignability for ScriptTypeInfo by iteratively walking the extends/implements chain.
     * Handles both direct and indirect inheritance (A extends B extends C implements D).
     */
    private static boolean isScriptTypeAssignableTo(ScriptTypeInfo actual, TypeInfo expected) {
        String expectedFull = expected.getFullName();
        String expectedSimple = expected.getSimpleName();

        Deque<TypeInfo> worklist = new ArrayDeque<>();
        worklist.add(actual);

        while (!worklist.isEmpty()) {
            TypeInfo current = worklist.poll();
            if (current == null || !current.isResolved()) continue;

            String currentFull = current.getFullName();
            if (currentFull != null) {
                if (expectedFull != null && expectedFull.equals(currentFull)) return true;
                if (expectedSimple != null && expectedSimple.equals(current.getSimpleName())) return true;
            }

            if (current instanceof ScriptTypeInfo) {
                ScriptTypeInfo scriptCurrent = (ScriptTypeInfo) current;
                TypeInfo superClass = scriptCurrent.getSuperClass();
                if (superClass != null) {
                    worklist.add(superClass);
                }
                for (TypeInfo iface : scriptCurrent.getImplementedInterfaces()) {
                    worklist.add(iface);
                }
            } else if (current.getJavaClass() != null && expected.getJavaClass() != null) {
                if (expected.getJavaClass().isAssignableFrom(current.getJavaClass())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean areTypeArgumentsCompatible(TypeInfo expected, TypeInfo actual) {
        java.util.List<TypeInfo> expectedArgs = expected.getAppliedTypeArgs();
        java.util.List<TypeInfo> actualArgs = actual.getAppliedTypeArgs();

        // Actual has no type args — it's a raw type being assigned to a parameterized type.
        // In Java this is always valid (unchecked warning, not an error).
        if (actualArgs.isEmpty()) {
            return true;
        }

        // If all of actual's type args are unresolved type parameters (e.g., Box<T>
        // from diamond "new Box<>()"), treat as raw/inferred — always compatible.
        boolean allActualAreTypeParams = true;
        for (TypeInfo actualArg : actualArgs) {
            if (!actualArg.isTypeParameter()) {
                allActualAreTypeParams = false;
                break;
            }
        }
        if (allActualAreTypeParams) {
            return true;
        }

        for (int i = 0; i < expectedArgs.size(); i++) {
            TypeInfo expectedArg = expectedArgs.get(i);
            TypeInfo actualArg = actualArgs.get(i);

            if (expectedArg.isTypeParameter() || actualArg.isTypeParameter()) {
                continue;
            }

            if (!isTypeCompatible(expectedArg, actualArg) && !isTypeCompatible(actualArg, expectedArg)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the type is a numeric type (including wrappers).
     */
    public static boolean isNumericType(String typeName) {
        switch (typeName) {
            case "number":
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
            case "number":
            case "double": return 6;
            default: return 0;
        }
    }

    /**
     * Check if a type is a numeric primitive (byte, short, int, long, float, double)
     */
    public static boolean isNumericPrimitive(TypeInfo type) {
        if (type == null || type.getJavaClass() == null) return false;
        Class<?> cls = type.getJavaClass();
        return cls == byte.class || cls == Byte.class ||
                cls == short.class || cls == Short.class ||
                cls == int.class || cls == Integer.class ||
                cls == long.class || cls == Long.class ||
                cls == float.class || cls == Float.class ||
                cls == double.class || cls == Double.class;
    }

    /**
     * Check if a numeric type can be promoted to a target numeric type.
     * Follows Java's numeric promotion hierarchy: byte -> short -> int -> long -> float -> double
     */
    public static boolean canPromoteNumeric(TypeInfo from, TypeInfo to) {
        if (from == null || to == null || from.getJavaClass() == null || to.getJavaClass() == null) return false;

        Class<?> fromClass = from.getJavaClass();
        Class<?> toClass = to.getJavaClass();

        // Unbox if necessary
        if (fromClass == Byte.class) fromClass = byte.class;
        if (fromClass == Short.class) fromClass = short.class;
        if (fromClass == Integer.class) fromClass = int.class;
        if (fromClass == Long.class) fromClass = long.class;
        if (fromClass == Float.class) fromClass = float.class;
        if (fromClass == Double.class) fromClass = double.class;

        if (toClass == Byte.class) toClass = byte.class;
        if (toClass == Short.class) toClass = short.class;
        if (toClass == Integer.class) toClass = int.class;
        if (toClass == Long.class) toClass = long.class;
        if (toClass == Float.class) toClass = float.class;
        if (toClass == Double.class) toClass = double.class;

        // Get numeric ranks (higher = wider type)
        int fromRank = getNumericRank(fromClass);
        int toRank = getNumericRank(toClass);

        return fromRank >= 0 && toRank >= 0 && fromRank <= toRank;
    }

    /**
     * Get the numeric rank for promotion hierarchy.
     * byte=0, short=1, int=2, long=3, float=4, double=5
     */
    public static int getNumericRank(Class<?> cls) {
        if (cls == byte.class) return 0;
        if (cls == short.class) return 1;
        if (cls == int.class) return 2;
        if (cls == long.class) return 3;
        if (cls == float.class) return 4;
        if (cls == double.class) return 5;
        return -1;
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

    /**
     * Attempt to narrow an integer literal to byte, short, or char when the
     * expected type requires it. The expression resolver normally infers integer
     * literals as {@code int}; this helper allows callers to re-type a literal
     * when {@code ExpressionTypeResolver.CURRENT_EXPECTED_TYPE} is set (e.g.
     * method-argument second pass or assignment RHS).
     *
     * <p>Supported literal formats:</p>
     * <ul>
     *   <li>Decimal: {@code 42}, {@code -128}, {@code +127}</li>
     *   <li>Hexadecimal: {@code 0xFF}, {@code 0XAB}</li>
     *   <li>Binary: {@code 0b1010}, {@code 0B1111_0000}</li>
     *   <li>Underscores as digit separators: {@code 1_000}</li>
     *   <li>Optional trailing {@code l}/{@code L} suffix (stripped before parsing)</li>
     * </ul>
     *
     * <p>Range checks:</p>
     * <ul>
     *   <li>byte: [-128 .. 127]</li>
     *   <li>short: [-32768 .. 32767]</li>
     *   <li>char: [0 .. 65535]</li>
     * </ul>
     *
     * @param literalText  the raw text of the integer literal (e.g. {@code "0xFF"})
     * @param expectedType the target type at the call/assignment site
     * @return a {@link TypeInfo} for the narrowed primitive type, or {@code null}
     *         if the expected type is not byte/short/char, the literal is not a
     *         valid integer, or the value falls outside the target range
     */
    public static TypeInfo narrowLiteralToExpectedType(String literalText, TypeInfo expectedType) {
        if (literalText == null || literalText.isEmpty() || expectedType == null) {
            return null;
        }

        // Determine the narrowing target from the expected type
        String expectedSimple = expectedType.getSimpleName();
        if (expectedSimple == null) {
            return null;
        }
        String unboxed = getUnboxedName(expectedSimple);
        if (!"byte".equals(unboxed) && !"short".equals(unboxed) && !"char".equals(unboxed)) {
            return null; // Only narrow to byte, short, or char
        }

        // Parse the literal value
        Long value = parseIntegerLiteral(literalText);
        if (value == null) {
            return null;
        }

        // Range check for the target type
        long v = value;
        switch (unboxed) {
            case "byte":
                if (v < -128 || v > 127) return null;
                break;
            case "short":
                if (v < -32768 || v > 32767) return null;
                break;
            case "char":
                if (v < 0 || v > 65535) return null;
                break;
            default:
                return null;
        }

        return TypeInfo.fromPrimitive(unboxed);
    }

    /**
     * Parse an integer literal string into a {@code Long} value.
     * Supports decimal, hexadecimal (0x/0X), and binary (0b/0B) formats,
     * optional leading sign (+/-), underscores as digit separators,
     * and an optional trailing l/L suffix.
     *
     * @param text the raw literal text
     * @return the parsed value, or {@code null} on parse failure
     */
    private static Long parseIntegerLiteral(String text) {
        String s = text.trim();
        if (s.isEmpty()) {
            return null;
        }

        // Determine sign
        boolean negative = false;
        if (s.charAt(0) == '-') {
            negative = true;
            s = s.substring(1);
        } else if (s.charAt(0) == '+') {
            s = s.substring(1);
        }

        if (s.isEmpty()) {
            return null;
        }

        // Strip optional trailing l/L suffix
        if (s.charAt(s.length() - 1) == 'l' || s.charAt(s.length() - 1) == 'L') {
            s = s.substring(0, s.length() - 1);
        }

        if (s.isEmpty()) {
            return null;
        }

        // Remove underscores (Java digit separators)
        s = s.replace("_", "");
        if (s.isEmpty()) {
            return null;
        }

        try {
            long value;
            if (s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                // Hexadecimal
                value = Long.parseUnsignedLong(s.substring(2), 16);
            } else if (s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'b' || s.charAt(1) == 'B')) {
                // Binary
                value = Long.parseUnsignedLong(s.substring(2), 2);
            } else {
                // Decimal
                value = Long.parseLong(s);
            }
            return negative ? -value : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String[] getJavaKeywords() {
        String[] keywords = {
                "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
                "return", "try", "catch", "finally", "throw", "throws", "new", "this", "super",
                "true", "false", "null", "instanceof", "import", "class", "interface", "enum",
                "extends", "implements", "public", "private", "protected", "static", "final",
                "abstract", "synchronized", "volatile", "transient", "native", "void",
                "boolean", "byte", "short", "int", "long", "float", "double", "char"
        };
        return keywords;
    }

    public static String[] getJavaScriptKeywords() {
        String[] keywords = {
                "function", "var", "let", "const", "if", "else", "for", "while", "do",
                "switch", "case", "break", "continue", "return", "try", "catch", "finally",
                "throw", "delete", "new", "typeof", "instanceof", "in", "of", "this", "null",
                "undefined", "true", "false", "async", "await", "yield", "class", "extends",
                "import", "export", "default"
        };
        
        
        return keywords;
    }
    
    public static boolean isJavaScriptKeyword(String keyword) {
        for (String k : getJavaScriptKeywords()) {
            if (k.equals(keyword)) {
                return true;
            }
        }
        return false;
    }
}
