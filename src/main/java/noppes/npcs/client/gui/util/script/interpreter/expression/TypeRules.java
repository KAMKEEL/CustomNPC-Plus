package noppes.npcs.client.gui.util.script.interpreter.expression;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

public class TypeRules {
    
    public static boolean isNumeric(TypeInfo type) {
        if (type == null || !type.isResolved()) return false;
        String name = type.getSimpleName();
        return "int".equals(name) || "long".equals(name) || "float".equals(name) || 
               "double".equals(name) || "byte".equals(name) || "short".equals(name) || "char".equals(name);
    }
    
    public static boolean isIntegral(TypeInfo type) {
        if (type == null || !type.isResolved()) return false;
        String name = type.getSimpleName();
        return "int".equals(name) || "long".equals(name) || "byte".equals(name) || 
               "short".equals(name) || "char".equals(name);
    }
    
    public static boolean isFloatingPoint(TypeInfo type) {
        if (type == null || !type.isResolved()) return false;
        String name = type.getSimpleName();
        return "float".equals(name) || "double".equals(name);
    }
    
    public static boolean isBoolean(TypeInfo type) {
        if (type == null || !type.isResolved()) return false;
        return "boolean".equals(type.getSimpleName());
    }
    
    public static boolean isString(TypeInfo type) {
        if (type == null || !type.isResolved()) return false;
        String name = type.getSimpleName();
        String fullName = type.getFullName();
        return "String".equals(name) || "java.lang.String".equals(fullName);
    }
    
    public static TypeInfo binaryNumericPromotion(TypeInfo left, TypeInfo right) {
        if (left == null || right == null || !left.isResolved() || !right.isResolved()) {
            return null;
        }
        String l = left.getSimpleName();
        String r = right.getSimpleName();
        if ("double".equals(l) || "double".equals(r)) return TypeInfo.fromPrimitive("double");
        if ("float".equals(l) || "float".equals(r)) return TypeInfo.fromPrimitive("float");
        if ("long".equals(l) || "long".equals(r)) return TypeInfo.fromPrimitive("long");
        return TypeInfo.fromPrimitive("int");
    }
    
    public static TypeInfo unaryNumericPromotion(TypeInfo type) {
        if (type == null || !type.isResolved()) return null;
        String name = type.getSimpleName();
        if ("double".equals(name) || "float".equals(name) || "long".equals(name)) return type;
        if ("byte".equals(name) || "short".equals(name) || "char".equals(name) || "int".equals(name)) {
            return TypeInfo.fromPrimitive("int");
        }
        return null;
    }
    
    public static TypeInfo resolveBinaryOperatorType(OperatorType op, TypeInfo left, TypeInfo right) {
        if (op == null) return null;
        
        switch (op.getCategory()) {
            case ARITHMETIC:
                if (op == OperatorType.ADD && (isString(left) || isString(right))) {
                    return validateAgainstExpectedType(TypeInfo.fromClass(String.class));
                }
                if (isNumeric(left) && isNumeric(right)) {
                    TypeInfo promoted = binaryNumericPromotion(left, right);
                    return validateAgainstExpectedType(promoted);
                }
                return null;
                
            case RELATIONAL:
                if (op == OperatorType.EQUALS || op == OperatorType.NOT_EQUALS) {
                    return TypeInfo.fromPrimitive("boolean");
                }
                if (isNumeric(left) && isNumeric(right)) {
                    return TypeInfo.fromPrimitive("boolean");
                }
                return null;
                
            case LOGICAL:
                if (isBoolean(left) && isBoolean(right)) {
                    return TypeInfo.fromPrimitive("boolean");
                }
                return null;
                
            case BITWISE:
                if (op == OperatorType.LEFT_SHIFT || op == OperatorType.RIGHT_SHIFT || 
                    op == OperatorType.UNSIGNED_RIGHT_SHIFT) {
                    if (isIntegral(left)) {
                        TypeInfo promoted = unaryNumericPromotion(left);
                        return validateAgainstExpectedType(promoted);
                    }
                }
                if (isIntegral(left) && isIntegral(right)) {
                    TypeInfo promoted = binaryNumericPromotion(left, right);
                    return validateAgainstExpectedType(promoted);
                }
                if (isBoolean(left) && isBoolean(right)) {
                    return TypeInfo.fromPrimitive("boolean");
                }
                return null;
                
            case ASSIGNMENT:
                return left;
                
            default:
                return null;
        }
    }
    
    public static TypeInfo resolveUnaryOperatorType(OperatorType op, TypeInfo operand) {
        if (op == null || operand == null || !operand.isResolved()) {
            return null;
        }
        
        switch (op) {
            case UNARY_PLUS:
            case UNARY_MINUS:
                if (isNumeric(operand)) {
                    TypeInfo promoted = unaryNumericPromotion(operand);
                    return validateAgainstExpectedType(promoted);
                }
                return null;
                
            case BITWISE_NOT:
                if (isIntegral(operand)) {
                    TypeInfo promoted = unaryNumericPromotion(operand);
                    return validateAgainstExpectedType(promoted);
                }
                return null;
                
            case LOGICAL_NOT:
                if (isBoolean(operand)) return TypeInfo.fromPrimitive("boolean");
                return null;
                
            case PRE_INCREMENT:
            case PRE_DECREMENT:
            case POST_INCREMENT:
            case POST_DECREMENT:
                if (isNumeric(operand)) return operand;
                return null;
                
            default:
                return null;
        }
    }

    /**
     * Check if sourceType can be assigned to targetType.
     * Handles primitives, numeric conversions, null compatibility, and reference types.
     */
    public static boolean isAssignmentCompatible(TypeInfo sourceType, TypeInfo targetType) {
        if (sourceType == null || targetType == null)
            return false;
        if (!targetType.isResolved())
            return true; // Can't validate against unresolved type

        // Null literal can be assigned to any reference type
        if ("<null>".equals(sourceType.getFullName())) {
            return !isPrimitive(targetType);
        }

        if (!sourceType.isResolved())
            return false;

        // Exact match
        if (sourceType.equals(targetType))
            return true;
        if (sourceType.getFullName().equals(targetType.getFullName()))
            return true;

        // Numeric conversions (widening primitive conversions)
        String source = sourceType.getSimpleName();
        String target = targetType.getSimpleName();

        // byte -> short, int, long, float, double
        if ("byte".equals(source)) {
            return "short".equals(target) || "int".equals(target) || "long".equals(target) ||
                    "float".equals(target) || "double".equals(target);
        }
        // short -> int, long, float, double
        if ("short".equals(source)) {
            return "int".equals(target) || "long".equals(target) ||
                    "float".equals(target) || "double".equals(target);
        }
        // char -> int, long, float, double
        if ("char".equals(source)) {
            return "int".equals(target) || "long".equals(target) ||
                    "float".equals(target) || "double".equals(target);
        }
        // int -> long, float, double
        if ("int".equals(source)) {
            return "long".equals(target) || "float".equals(target) || "double".equals(target);
        }
        // long -> float, double
        if ("long".equals(source)) {
            return "float".equals(target) || "double".equals(target);
        }
        // float -> double
        if ("float".equals(source)) {
            return "double".equals(target);
        }

        // For reference types, we'd need inheritance/interface checking
        // For now, return false for incompatible types
        return false;
    }

    /**
     * Check if a type is a primitive type.
     */
    public static boolean isPrimitive(TypeInfo type) {
        if (type == null || !type.isResolved())
            return false;
        String name = type.getSimpleName();
        return "int".equals(name) || "long".equals(name) || "float".equals(name) ||
                "double".equals(name) || "byte".equals(name) || "short".equals(name) ||
                "char".equals(name) || "boolean".equals(name) || "void".equals(name);
    }
    
    /**
     * Validate a computed type against the current expected type context.
     * If there's an expected type and the computed type is compatible, returns the expected type.
     * If incompatible, returns the computed type so the error can be properly reported.
     * If no expected type context, returns the computed type unchanged.
     * 
     * @param computedType The type computed by normal type rules
     * @return The type to use (either expectedType if compatible, or computedType)
     */
    public static TypeInfo validateAgainstExpectedType(TypeInfo computedType) {
        if (computedType == null) return null;
        
        TypeInfo expectedType = ExpressionTypeResolver.CURRENT_EXPECTED_TYPE;
        if (expectedType != null && expectedType.isResolved() && computedType.isResolved()) {
            if (isAssignmentCompatible(computedType, expectedType)) {
                return expectedType;
            }
            // Return computed type so error can be detected (incompatible with expected)
            return computedType;
        }
        
        return computedType;
    }
    
    public static TypeInfo resolveTernaryType(TypeInfo thenType, TypeInfo elseType) {
        if (thenType == null && elseType == null) return null;
        if (thenType == null) return elseType;
        if (elseType == null) return thenType;
        
        // Handle null literal type
        boolean thenIsNull = "<null>".equals(thenType.getFullName());
        boolean elseIsNull = "<null>".equals(elseType.getFullName());
        
        if (thenIsNull && elseIsNull) return thenType; // both null -> null
        if (thenIsNull) return elseType; // null : T -> T
        if (elseIsNull) return thenType; // T : null -> T
        
        if (!thenType.isResolved()) return elseType.isResolved() ? elseType : null;
        if (!elseType.isResolved()) return thenType;

        // If there's an expected type context, validate both branches against it
        TypeInfo expectedType = ExpressionTypeResolver.CURRENT_EXPECTED_TYPE;
        if (expectedType != null && expectedType.isResolved()) {
            boolean thenCompatible = isAssignmentCompatible(thenType, expectedType);
            boolean elseCompatible = isAssignmentCompatible(elseType, expectedType);

            // If both branches are compatible with expected type, return expected type
            if (thenCompatible && elseCompatible) {
                return expectedType;
            } else if (!elseCompatible) {
                // Return the incompatible type so error can be detected
                return elseType;
            } else {
                // thenType is incompatible
                return thenType;
            }
        }
        
        // Normal ternary type resolution without expected type context
        if (thenType.equals(elseType)) return thenType;
        if (isNumeric(thenType) && isNumeric(elseType)) {
            return binaryNumericPromotion(thenType, elseType);
        }
        
        return thenType;
    }
}
