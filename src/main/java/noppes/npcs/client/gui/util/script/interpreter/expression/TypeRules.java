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
                    return TypeInfo.fromClass(String.class);
                }
                if (isNumeric(left) && isNumeric(right)) {
                    return binaryNumericPromotion(left, right);
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
                        return unaryNumericPromotion(left);
                    }
                }
                if (isIntegral(left) && isIntegral(right)) {
                    return binaryNumericPromotion(left, right);
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
                if (isNumeric(operand)) return unaryNumericPromotion(operand);
                return null;
                
            case BITWISE_NOT:
                if (isIntegral(operand)) return unaryNumericPromotion(operand);
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
    
    public static TypeInfo resolveTernaryType(TypeInfo thenType, TypeInfo elseType) {
        if (thenType == null && elseType == null) return null;
        if (thenType == null) return elseType;
        if (elseType == null) return thenType;
        if (!thenType.isResolved()) return elseType.isResolved() ? elseType : null;
        if (!elseType.isResolved()) return thenType;
        
        if (thenType.equals(elseType)) return thenType;
        if (isNumeric(thenType) && isNumeric(elseType)) {
            return binaryNumericPromotion(thenType, elseType);
        }
        
        return thenType;
    }
}
