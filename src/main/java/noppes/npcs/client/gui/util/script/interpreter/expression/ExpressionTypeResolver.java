package noppes.npcs.client.gui.util.script.interpreter.expression;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import java.util.List;

public class ExpressionTypeResolver {
    private final ExpressionNode.TypeResolverContext context;
    
    /**
     * Static field to track the expected/desired type for the current expression being resolved.
     * This allows TypeRules to validate type compatibility in context (e.g., ternary operator branches
     * must be compatible with the assignment target type).
     * Should be set before calling resolve() and cleared after.
     */
    public static TypeInfo CURRENT_EXPECTED_TYPE = null;
    
    public ExpressionTypeResolver(ExpressionNode.TypeResolverContext context) {
        this.context = context;
    }
    
    public TypeInfo resolve(String expression) {
        if (expression == null || expression.trim().isEmpty()) return null;
        
        try {
            List<ExpressionToken> tokens = ExpressionTokenizer.tokenize(expression);
            if (tokens.isEmpty()) return null;
            
            ExpressionParser parser = new ExpressionParser(tokens);
            ExpressionNode ast = parser.parse();
            if (ast == null) return null;
            
            return resolveNodeType(ast);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean containsOperators(String expression) {
        if (expression == null) return false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            switch (c) {
                case '+': case '-': case '*': case '/': case '%':
                case '&': case '|': case '^': case '~':
                case '<': case '>': case '=': case '!':
                case '?': case ':': return true;
            }
        }
        return false;
    }
    
    private TypeInfo resolveNodeType(ExpressionNode node) {
        if (node == null) return null;
        
        if (node instanceof ExpressionNode.IntLiteralNode) return TypeInfo.fromPrimitive("int");
        if (node instanceof ExpressionNode.LongLiteralNode) return TypeInfo.fromPrimitive("long");
        if (node instanceof ExpressionNode.FloatLiteralNode) return TypeInfo.fromPrimitive("float");
        if (node instanceof ExpressionNode.DoubleLiteralNode) return TypeInfo.fromPrimitive("double");
        if (node instanceof ExpressionNode.BooleanLiteralNode) return TypeInfo.fromPrimitive("boolean");
        if (node instanceof ExpressionNode.CharLiteralNode) return TypeInfo.fromPrimitive("char");
        if (node instanceof ExpressionNode.StringLiteralNode) return TypeInfo.fromClass(String.class);
        if (node instanceof ExpressionNode.NullLiteralNode) return TypeInfo.unresolved("null", "<null>");
        
        if (node instanceof ExpressionNode.IdentifierNode) {
            return context.resolveIdentifier(((ExpressionNode.IdentifierNode) node).getName());
        }
        
        if (node instanceof ExpressionNode.MemberAccessNode) {
            ExpressionNode.MemberAccessNode ma = (ExpressionNode.MemberAccessNode) node;
            TypeInfo targetType = resolveNodeType(ma.getTarget());
            if (targetType == null || !targetType.isResolved()) return null;
            return context.resolveMemberAccess(targetType, ma.getMemberName());
        }
        
        if (node instanceof ExpressionNode.MethodCallNode) {
            ExpressionNode.MethodCallNode mc = (ExpressionNode.MethodCallNode) node;
            TypeInfo targetType = mc.getTarget() == null ? context.resolveIdentifier("this") : resolveNodeType(mc.getTarget());
            if (targetType == null || !targetType.isResolved()) return null;
            TypeInfo[] argTypes = new TypeInfo[mc.getArguments().size()];
            for (int i = 0; i < argTypes.length; i++) argTypes[i] = resolveNodeType(mc.getArguments().get(i));
            return context.resolveMethodCall(targetType, mc.getMethodName(), argTypes);
        }
        
        if (node instanceof ExpressionNode.ArrayAccessNode) {
            ExpressionNode.ArrayAccessNode aa = (ExpressionNode.ArrayAccessNode) node;
            TypeInfo arrayType = resolveNodeType(aa.getArray());
            if (arrayType == null || !arrayType.isResolved()) return null;
            String typeName = arrayType.getFullName();
            if (typeName.endsWith("[]")) {
                String elementTypeName = typeName.substring(0, typeName.length() - 2);
                return context.resolveTypeName(elementTypeName);
            }
            return context.resolveArrayAccess(arrayType);
        }
        
        if (node instanceof ExpressionNode.NewNode) {
            return context.resolveTypeName(((ExpressionNode.NewNode) node).getTypeName());
        }
        
        if (node instanceof ExpressionNode.BinaryOpNode) {
            ExpressionNode.BinaryOpNode bin = (ExpressionNode.BinaryOpNode) node;
            TypeInfo leftType = resolveNodeType(bin.getLeft());
            TypeInfo rightType = resolveNodeType(bin.getRight());
            return TypeRules.resolveBinaryOperatorType(bin.getOperator(), leftType, rightType);
        }
        
        if (node instanceof ExpressionNode.UnaryOpNode) {
            ExpressionNode.UnaryOpNode un = (ExpressionNode.UnaryOpNode) node;
            TypeInfo operandType = resolveNodeType(un.getOperand());
            return TypeRules.resolveUnaryOperatorType(un.getOperator(), operandType);
        }
        
        if (node instanceof ExpressionNode.TernaryNode) {
            ExpressionNode.TernaryNode tern = (ExpressionNode.TernaryNode) node;
            TypeInfo thenType = resolveNodeType(tern.getThenExpr());
            TypeInfo elseType = resolveNodeType(tern.getElseExpr());
            return TypeRules.resolveTernaryType(thenType, elseType);
        }
        
        if (node instanceof ExpressionNode.CastNode) {
            return context.resolveTypeName(((ExpressionNode.CastNode) node).getTypeName());
        }
        
        if (node instanceof ExpressionNode.InstanceofNode) {
            return TypeInfo.fromPrimitive("boolean");
        }
        
        if (node instanceof ExpressionNode.AssignmentNode) {
            return resolveNodeType(((ExpressionNode.AssignmentNode) node).getTarget());
        }
        
        if (node instanceof ExpressionNode.ParenthesizedNode) {
            return resolveNodeType(((ExpressionNode.ParenthesizedNode) node).getInner());
        }
        
        return null;
    }
    
    public static ExpressionNode.TypeResolverContext createBasicContext() {
        return new ExpressionNode.TypeResolverContext() {
            public TypeInfo resolveIdentifier(String name) {
                if ("true".equals(name) || "false".equals(name)) return TypeInfo.fromPrimitive("boolean");
                if ("null".equals(name)) return TypeInfo.unresolved("null", "<null>");
                return null;
            }
            public TypeInfo resolveMemberAccess(TypeInfo targetType, String memberName) { return null; }
            public TypeInfo resolveMethodCall(TypeInfo targetType, String methodName, TypeInfo[] argTypes) { return null; }
            public TypeInfo resolveArrayAccess(TypeInfo arrayType) {
                String typeName = arrayType.getFullName();
                if (typeName.endsWith("[]")) {
                    // For primitive arrays, we need to map back to primitive TypeInfo
                    String elementType = typeName.substring(0, typeName.length() - 2);
                    switch (elementType) {
                        case "int": case "long": case "float": case "double": 
                        case "byte": case "short": case "char": case "boolean":
                            return TypeInfo.fromPrimitive(elementType);
                        default:
                            return TypeInfo.unresolved(elementType, elementType);
                    }
                }
                return null;
            }
            public TypeInfo resolveTypeName(String typeName) { 
                // Handle primitives
                switch (typeName) {
                    case "int": case "long": case "float": case "double": 
                    case "byte": case "short": case "char": case "boolean": case "void":
                        return TypeInfo.fromPrimitive(typeName);
                    default:
                        return TypeInfo.unresolved(typeName, typeName);
                }
            }
        };
    }
}
