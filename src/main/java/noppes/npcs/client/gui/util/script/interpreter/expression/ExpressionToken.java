package noppes.npcs.client.gui.util.script.interpreter.expression;

public class ExpressionToken {
    
    public enum TokenKind {
        INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL,
        BOOLEAN_LITERAL, CHAR_LITERAL, STRING_LITERAL, NULL_LITERAL,
        IDENTIFIER, NEW, INSTANCEOF,
        OPERATOR,
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
        DOT, COMMA, QUESTION, COLON, SEMICOLON,
        EOF
    }
    
    private final TokenKind kind;
    private final String text;
    private final int start;
    private final int end;
    private final OperatorType operatorType;
    
    public ExpressionToken(TokenKind kind, String text, int start, int end) {
        this(kind, text, start, end, null);
    }
    
    public ExpressionToken(TokenKind kind, String text, int start, int end, OperatorType operatorType) {
        this.kind = kind;
        this.text = text;
        this.start = start;
        this.end = end;
        this.operatorType = operatorType;
    }
    
    public TokenKind getKind() { return kind; }
    public String getText() { return text; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public OperatorType getOperatorType() { return operatorType; }
    
    public static ExpressionToken operator(String symbol, int start, int end) {
        OperatorType op = OperatorType.fromBinarySymbol(symbol);
        if (op == null) op = OperatorType.fromSymbol(symbol);
        return new ExpressionToken(TokenKind.OPERATOR, symbol, start, end, op);
    }
    
    public static ExpressionToken identifier(String name, int start, int end) {
        if ("true".equals(name) || "false".equals(name)) {
            return new ExpressionToken(TokenKind.BOOLEAN_LITERAL, name, start, end);
        }
        if ("null".equals(name)) {
            return new ExpressionToken(TokenKind.NULL_LITERAL, name, start, end);
        }
        if ("new".equals(name)) {
            return new ExpressionToken(TokenKind.NEW, name, start, end);
        }
        if ("instanceof".equals(name)) {
            return new ExpressionToken(TokenKind.INSTANCEOF, name, start, end);
        }
        return new ExpressionToken(TokenKind.IDENTIFIER, name, start, end);
    }
}
