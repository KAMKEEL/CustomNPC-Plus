package noppes.npcs.client.gui.util.script.interpreter.expression;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ExpressionNode {
    protected final int start;
    protected final int end;
    
    protected ExpressionNode(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public abstract TypeInfo resolveType(TypeResolverContext resolver);

    public static class IntLiteralNode extends ExpressionNode {
        private final String value;
        public IntLiteralNode(String value, int start, int end) { super(start, end); this.value = value; }
        public String getValue() { return value; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("int"); }
    }
    
    public static class LongLiteralNode extends ExpressionNode {
        public LongLiteralNode(String value, int start, int end) { super(start, end); }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("long"); }
    }
    
    public static class FloatLiteralNode extends ExpressionNode {
        public FloatLiteralNode(String value, int start, int end) { super(start, end); }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("float"); }
    }
    
    public static class DoubleLiteralNode extends ExpressionNode {
        public DoubleLiteralNode(String value, int start, int end) { super(start, end); }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("double"); }
    }
    
    public static class BooleanLiteralNode extends ExpressionNode {
        public BooleanLiteralNode(boolean value, int start, int end) { super(start, end); }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("boolean"); }
    }
    
    public static class CharLiteralNode extends ExpressionNode {
        public CharLiteralNode(String value, int start, int end) { super(start, end); }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("char"); }
    }
    
    public static class StringLiteralNode extends ExpressionNode {
        private final String value;
        public StringLiteralNode(String value, int start, int end) { super(start, end); this.value = value; }
        public String getValue() { return value; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromClass(String.class); }
    }
    
    public static class NullLiteralNode extends ExpressionNode {
        public NullLiteralNode(int start, int end) { super(start, end); }
        public TypeInfo resolveType(TypeResolverContext resolver) { 
            return TypeInfo.unresolved("null", "<null>"); 
        }
    }
    
    public static class IdentifierNode extends ExpressionNode {
        private final String name;
        public IdentifierNode(String name, int start, int end) { super(start, end); this.name = name; }
        public String getName() { return name; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return resolver.resolveIdentifier(name); }
    }
    
    public static class MemberAccessNode extends ExpressionNode {
        private final ExpressionNode target;
        private final String memberName;
        public MemberAccessNode(ExpressionNode target, String memberName, int start, int end) {
            super(start, end); this.target = target; this.memberName = memberName;
        }
        public ExpressionNode getTarget() { return target; }
        public String getMemberName() { return memberName; }
        public TypeInfo resolveType(TypeResolverContext resolver) {
            TypeInfo targetType = target.resolveType(resolver);
            if (targetType == null || !targetType.isResolved()) return null;
            return resolver.resolveMemberAccess(targetType, memberName);
        }
    }
    
    public static class MethodCallNode extends ExpressionNode {
        private final ExpressionNode target;
        private final String methodName;
        private final List<ExpressionNode> arguments;
        public MethodCallNode(ExpressionNode target, String methodName, List<ExpressionNode> arguments, int start, int end) {
            super(start, end); this.target = target; this.methodName = methodName;
            this.arguments = arguments != null ? new ArrayList<>(arguments) : new ArrayList<>();
        }
        public ExpressionNode getTarget() { return target; }
        public String getMethodName() { return methodName; }
        public List<ExpressionNode> getArguments() { return Collections.unmodifiableList(arguments); }
        public TypeInfo resolveType(TypeResolverContext resolver) {
            TypeInfo targetType = target != null ? target.resolveType(resolver) : null;
            TypeInfo[] argTypes = new TypeInfo[arguments.size()];
            for (int i = 0; i < arguments.size(); i++) argTypes[i] = arguments.get(i).resolveType(resolver);
            return resolver.resolveMethodCall(targetType, methodName, argTypes);
        }
    }
    
    public static class ArrayAccessNode extends ExpressionNode {
        private final ExpressionNode array;
        private final ExpressionNode index;
        public ArrayAccessNode(ExpressionNode array, ExpressionNode index, int start, int end) {
            super(start, end); this.array = array; this.index = index;
        }
        public ExpressionNode getArray() { return array; }
        public ExpressionNode getIndex() { return index; }
        public TypeInfo resolveType(TypeResolverContext resolver) {
            TypeInfo arrayType = array.resolveType(resolver);
            if (arrayType == null || !arrayType.isResolved()) return null;
            return resolver.resolveArrayAccess(arrayType);
        }
    }
    
    public static class NewNode extends ExpressionNode {
        private final String typeName;
        private final List<ExpressionNode> arguments;
        public NewNode(String typeName, List<ExpressionNode> arguments, int start, int end) {
            super(start, end); this.typeName = typeName;
            this.arguments = arguments != null ? new ArrayList<>(arguments) : new ArrayList<>();
        }
        public String getTypeName() { return typeName; }
        public List<ExpressionNode> getArguments() { return Collections.unmodifiableList(arguments); }
        public TypeInfo resolveType(TypeResolverContext resolver) { return resolver.resolveTypeName(typeName); }
    }
    
    public static class BinaryOpNode extends ExpressionNode {
        private final ExpressionNode left;
        private final OperatorType operator;
        private final ExpressionNode right;
        public BinaryOpNode(ExpressionNode left, OperatorType operator, ExpressionNode right, int start, int end) {
            super(start, end); this.left = left; this.operator = operator; this.right = right;
        }
        public ExpressionNode getLeft() { return left; }
        public OperatorType getOperator() { return operator; }
        public ExpressionNode getRight() { return right; }
        public TypeInfo resolveType(TypeResolverContext resolver) {
            TypeInfo leftType = left.resolveType(resolver);
            TypeInfo rightType = right.resolveType(resolver);
            return TypeRules.resolveBinaryOperatorType(operator, leftType, rightType);
        }
    }
    
    public static class UnaryOpNode extends ExpressionNode {
        private final OperatorType operator;
        private final ExpressionNode operand;
        private final boolean prefix;
        public UnaryOpNode(OperatorType operator, ExpressionNode operand, boolean prefix, int start, int end) {
            super(start, end); this.operator = operator; this.operand = operand; this.prefix = prefix;
        }
        public OperatorType getOperator() { return operator; }
        public ExpressionNode getOperand() { return operand; }
        public boolean isPrefix() { return prefix; }
        public TypeInfo resolveType(TypeResolverContext resolver) {
            TypeInfo operandType = operand.resolveType(resolver);
            return TypeRules.resolveUnaryOperatorType(operator, operandType);
        }
    }
    
    public static class TernaryNode extends ExpressionNode {
        private final ExpressionNode condition;
        private final ExpressionNode thenExpr;
        private final ExpressionNode elseExpr;
        public TernaryNode(ExpressionNode condition, ExpressionNode thenExpr, ExpressionNode elseExpr, int start, int end) {
            super(start, end); this.condition = condition; this.thenExpr = thenExpr; this.elseExpr = elseExpr;
        }
        public ExpressionNode getCondition() { return condition; }
        public ExpressionNode getThenExpr() { return thenExpr; }
        public ExpressionNode getElseExpr() { return elseExpr; }
        public TypeInfo resolveType(TypeResolverContext resolver) {
            TypeInfo thenType = thenExpr.resolveType(resolver);
            TypeInfo elseType = elseExpr.resolveType(resolver);
            return TypeRules.resolveTernaryType(thenType, elseType);
        }
    }
    
    public static class InstanceofNode extends ExpressionNode {
        private final ExpressionNode expression;
        private final String typeName;
        public InstanceofNode(ExpressionNode expression, String typeName, int start, int end) {
            super(start, end); this.expression = expression; this.typeName = typeName;
        }
        public ExpressionNode getExpression() { return expression; }
        public String getTypeName() { return typeName; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return TypeInfo.fromPrimitive("boolean"); }
    }
    
    public static class CastNode extends ExpressionNode {
        private final String typeName;
        private final ExpressionNode expression;
        public CastNode(String typeName, ExpressionNode expression, int start, int end) {
            super(start, end); this.typeName = typeName; this.expression = expression;
        }
        public String getTypeName() { return typeName; }
        public ExpressionNode getExpression() { return expression; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return resolver.resolveTypeName(typeName); }
    }
    
    public static class AssignmentNode extends ExpressionNode {
        private final ExpressionNode target;
        private final OperatorType operator;
        private final ExpressionNode value;
        public AssignmentNode(ExpressionNode target, OperatorType operator, ExpressionNode value, int start, int end) {
            super(start, end); this.target = target; this.operator = operator; this.value = value;
        }
        public ExpressionNode getTarget() { return target; }
        public OperatorType getOperator() { return operator; }
        public ExpressionNode getValue() { return value; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return target.resolveType(resolver); }
    }
    
    public static class ParenthesizedNode extends ExpressionNode {
        private final ExpressionNode inner;
        public ParenthesizedNode(ExpressionNode inner, int start, int end) { super(start, end); this.inner = inner; }
        public ExpressionNode getInner() { return inner; }
        public TypeInfo resolveType(TypeResolverContext resolver) { return inner.resolveType(resolver); }
    }
    
    public interface TypeResolverContext {
        TypeInfo resolveIdentifier(String name);
        TypeInfo resolveMemberAccess(TypeInfo targetType, String memberName);
        TypeInfo resolveMethodCall(TypeInfo targetType, String methodName, TypeInfo[] argTypes);
        TypeInfo resolveArrayAccess(TypeInfo arrayType);
        TypeInfo resolveTypeName(String typeName);
    }
}
