package noppes.npcs.client.gui.util.script.interpreter.expression;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    private final List<ExpressionToken> tokens;
    private int pos;
    
    public ExpressionParser(List<ExpressionToken> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }
    
    public ExpressionNode parse() {
        if (tokens.isEmpty()) return null;
        return parseExpression(0);
    }
    
    private ExpressionToken current() {
        if (pos >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(pos);
    }
    
    private ExpressionToken advance() {
        ExpressionToken tok = current();
        if (pos < tokens.size()) pos++;
        return tok;
    }
    
    private boolean check(ExpressionToken.TokenKind kind) { return current().getKind() == kind; }
    
    private boolean match(ExpressionToken.TokenKind kind) {
        if (check(kind)) { advance(); return true; }
        return false;
    }
    
    private ExpressionNode parseExpression(int minPrecedence) {
        ExpressionNode left = parsePrefixExpression();
        if (left == null) return null;
        
        while (true) {
            if (check(ExpressionToken.TokenKind.QUESTION) && minPrecedence <= 2) {
                left = parseTernary(left);
                continue;
            }
            
            OperatorType op = getCurrentBinaryOperator();
            if (op == null) break;
            
            int precedence = op.getPrecedence();
            if (precedence < minPrecedence) break;
            
            advance();
            
            int nextMinPrecedence = (op.getAssociativity() == OperatorType.Associativity.LEFT) 
                                   ? precedence + 1 : precedence;
            
            ExpressionNode right = parseExpression(nextMinPrecedence);
            if (right == null) break;
            
            if (op == OperatorType.INSTANCEOF) {
                String typeName = extractTypeName(right);
                left = new ExpressionNode.InstanceofNode(left, typeName, left.getStart(), right.getEnd());
            } else if (op.isAssignment()) {
                left = new ExpressionNode.AssignmentNode(left, op, right, left.getStart(), right.getEnd());
            } else {
                left = new ExpressionNode.BinaryOpNode(left, op, right, left.getStart(), right.getEnd());
            }
        }
        
        return parsePostfixExpression(left);
    }
    
    private String extractTypeName(ExpressionNode node) {
        if (node instanceof ExpressionNode.IdentifierNode) {
            return ((ExpressionNode.IdentifierNode) node).getName();
        }
        if (node instanceof ExpressionNode.MemberAccessNode) {
            ExpressionNode.MemberAccessNode ma = (ExpressionNode.MemberAccessNode) node;
            return extractTypeName(ma.getTarget()) + "." + ma.getMemberName();
        }
        return "Object";
    }
    
    private ExpressionNode parseTernary(ExpressionNode condition) {
        int start = condition.getStart();
        advance();
        ExpressionNode thenExpr = parseExpression(0);
        if (thenExpr == null) return condition;
        if (!match(ExpressionToken.TokenKind.COLON)) return condition;
        ExpressionNode elseExpr = parseExpression(2);
        if (elseExpr == null) return condition;
        return new ExpressionNode.TernaryNode(condition, thenExpr, elseExpr, start, elseExpr.getEnd());
    }
    
    private OperatorType getCurrentBinaryOperator() {
        ExpressionToken tok = current();
        if (tok.getKind() == ExpressionToken.TokenKind.OPERATOR) {
            OperatorType op = tok.getOperatorType();
            if (op != null && (op.isBinary() || op.isAssignment())) return op;
        }
        if (tok.getKind() == ExpressionToken.TokenKind.INSTANCEOF) return OperatorType.INSTANCEOF;
        return null;
    }
    
    private ExpressionNode parsePrefixExpression() {
        ExpressionToken tok = current();
        int start = tok.getStart();
        
        if (tok.getKind() == ExpressionToken.TokenKind.OPERATOR) {
            OperatorType op = tok.getOperatorType();
            if (op != null && isUnaryOperator(op)) {
                advance();
                ExpressionNode operand = parsePrefixExpression();
                if (operand == null) return null;
                OperatorType unaryOp = toUnaryOperator(op);
                return new ExpressionNode.UnaryOpNode(unaryOp, operand, true, start, operand.getEnd());
            }
        }
        
        if (tok.getKind() == ExpressionToken.TokenKind.LEFT_PAREN) {
            return parseCastOrParenthesized();
        }
        
        return parsePrimaryExpression();
    }
    
    private boolean isUnaryOperator(OperatorType op) {
        switch (op) {
            case ADD: case SUBTRACT: case LOGICAL_NOT: case BITWISE_NOT:
            case PRE_INCREMENT: case PRE_DECREMENT: return true;
            default: return false;
        }
    }
    
    private OperatorType toUnaryOperator(OperatorType op) {
        switch (op) {
            case ADD: return OperatorType.UNARY_PLUS;
            case SUBTRACT: return OperatorType.UNARY_MINUS;
            default: return op;
        }
    }
    
    private ExpressionNode parseCastOrParenthesized() {
        int start = current().getStart();
        advance();
        
        if (check(ExpressionToken.TokenKind.IDENTIFIER)) {
            String possibleType = current().getText();
            int savedPos = pos;
            advance();
            
            while (check(ExpressionToken.TokenKind.DOT)) {
                advance();
                if (check(ExpressionToken.TokenKind.IDENTIFIER)) {
                    possibleType += "." + current().getText();
                    advance();
                }
            }
            
            while (check(ExpressionToken.TokenKind.LEFT_BRACKET)) {
                advance();
                if (!match(ExpressionToken.TokenKind.RIGHT_BRACKET)) { pos = savedPos; break; }
                possibleType += "[]";
            }
            
            if (check(ExpressionToken.TokenKind.RIGHT_PAREN)) {
                advance();
                if (canStartExpression()) {
                    ExpressionNode expr = parsePrefixExpression();
                    if (expr != null) {
                        return new ExpressionNode.CastNode(possibleType, expr, start, expr.getEnd());
                    }
                }
            }
            pos = savedPos;
        }
        
        ExpressionNode inner = parseExpression(0);
        if (inner != null && match(ExpressionToken.TokenKind.RIGHT_PAREN)) {
            return new ExpressionNode.ParenthesizedNode(inner, start, current().getStart());
        }
        return inner;
    }
    
    private boolean canStartExpression() {
        ExpressionToken.TokenKind kind = current().getKind();
        switch (kind) {
            case IDENTIFIER: case INT_LITERAL: case LONG_LITERAL: case FLOAT_LITERAL:
            case DOUBLE_LITERAL: case BOOLEAN_LITERAL: case CHAR_LITERAL: case STRING_LITERAL:
            case NULL_LITERAL: case NEW: case LEFT_PAREN: case OPERATOR: return true;
            default: return false;
        }
    }
    
    private ExpressionNode parsePrimaryExpression() {
        ExpressionToken tok = current();
        int start = tok.getStart();
        
        switch (tok.getKind()) {
            case INT_LITERAL: advance(); return new ExpressionNode.IntLiteralNode(tok.getText(), start, tok.getEnd());
            case LONG_LITERAL: advance(); return new ExpressionNode.LongLiteralNode(tok.getText(), start, tok.getEnd());
            case FLOAT_LITERAL: advance(); return new ExpressionNode.FloatLiteralNode(tok.getText(), start, tok.getEnd());
            case DOUBLE_LITERAL: advance(); return new ExpressionNode.DoubleLiteralNode(tok.getText(), start, tok.getEnd());
            case BOOLEAN_LITERAL: advance(); return new ExpressionNode.BooleanLiteralNode("true".equals(tok.getText()), start, tok.getEnd());
            case CHAR_LITERAL: advance(); return new ExpressionNode.CharLiteralNode(tok.getText(), start, tok.getEnd());
            case STRING_LITERAL: advance(); return new ExpressionNode.StringLiteralNode(tok.getText(), start, tok.getEnd());
            case NULL_LITERAL: advance(); return new ExpressionNode.NullLiteralNode(start, tok.getEnd());
            case NEW: return parseNewExpression();
            case IDENTIFIER: return parseIdentifierOrMethodCall();
            case LEFT_PAREN: return parseCastOrParenthesized();
            default: return null;
        }
    }
    
    private ExpressionNode parseNewExpression() {
        int start = current().getStart();
        advance();
        if (!check(ExpressionToken.TokenKind.IDENTIFIER)) return null;
        
        StringBuilder typeName = new StringBuilder(current().getText());
        advance();
        
        while (check(ExpressionToken.TokenKind.DOT)) {
            advance();
            if (check(ExpressionToken.TokenKind.IDENTIFIER)) {
                typeName.append(".").append(current().getText());
                advance();
            }
        }
        
        List<ExpressionNode> args = new ArrayList<>();
        if (match(ExpressionToken.TokenKind.LEFT_PAREN)) {
            args = parseArgumentList();
            match(ExpressionToken.TokenKind.RIGHT_PAREN);
        }
        
        return new ExpressionNode.NewNode(typeName.toString(), args, start, current().getStart());
    }
    
    private ExpressionNode parseIdentifierOrMethodCall() {
        int start = current().getStart();
        String name = current().getText();
        advance();
        
        ExpressionNode result = new ExpressionNode.IdentifierNode(name, start, current().getStart());
        return parseAccessChain(result);
    }
    
    private ExpressionNode parseAccessChain(ExpressionNode base) {
        while (true) {
            if (check(ExpressionToken.TokenKind.LEFT_PAREN)) {
                advance();
                List<ExpressionNode> args = parseArgumentList();
                int end = current().getStart();
                match(ExpressionToken.TokenKind.RIGHT_PAREN);
                
                String methodName;
                ExpressionNode target;
                
                if (base instanceof ExpressionNode.IdentifierNode) {
                    methodName = ((ExpressionNode.IdentifierNode) base).getName();
                    target = null;
                } else if (base instanceof ExpressionNode.MemberAccessNode) {
                    ExpressionNode.MemberAccessNode ma = (ExpressionNode.MemberAccessNode) base;
                    methodName = ma.getMemberName();
                    target = ma.getTarget();
                } else {
                    methodName = "apply";
                    target = base;
                }
                
                base = new ExpressionNode.MethodCallNode(target, methodName, args, base.getStart(), end);
            } else if (check(ExpressionToken.TokenKind.DOT)) {
                advance();
                if (!check(ExpressionToken.TokenKind.IDENTIFIER)) break;
                String memberName = current().getText();
                int end = current().getEnd();
                advance();
                base = new ExpressionNode.MemberAccessNode(base, memberName, base.getStart(), end);
            } else if (check(ExpressionToken.TokenKind.LEFT_BRACKET)) {
                advance();
                ExpressionNode index = parseExpression(0);
                int end = current().getStart();
                match(ExpressionToken.TokenKind.RIGHT_BRACKET);
                if (index != null) {
                    base = new ExpressionNode.ArrayAccessNode(base, index, base.getStart(), end);
                }
            } else {
                break;
            }
        }
        return base;
    }
    
    private List<ExpressionNode> parseArgumentList() {
        List<ExpressionNode> args = new ArrayList<>();
        if (check(ExpressionToken.TokenKind.RIGHT_PAREN)) return args;
        
        ExpressionNode first = parseExpression(0);
        if (first != null) args.add(first);
        
        while (match(ExpressionToken.TokenKind.COMMA)) {
            ExpressionNode arg = parseExpression(0);
            if (arg != null) args.add(arg);
        }
        return args;
    }
    
    private ExpressionNode parsePostfixExpression(ExpressionNode expr) {
        if (expr == null) return null;
        
        while (true) {
            ExpressionToken tok = current();
            if (tok.getKind() != ExpressionToken.TokenKind.OPERATOR) break;
            
            OperatorType op = tok.getOperatorType();
            if (op == OperatorType.PRE_INCREMENT) {
                advance();
                expr = new ExpressionNode.UnaryOpNode(OperatorType.POST_INCREMENT, expr, false, expr.getStart(), tok.getEnd());
            } else if (op == OperatorType.PRE_DECREMENT) {
                advance();
                expr = new ExpressionNode.UnaryOpNode(OperatorType.POST_DECREMENT, expr, false, expr.getStart(), tok.getEnd());
            } else {
                break;
            }
        }
        return expr;
    }
}
