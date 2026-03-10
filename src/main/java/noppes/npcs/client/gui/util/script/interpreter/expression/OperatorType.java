package noppes.npcs.client.gui.util.script.interpreter.expression;

public enum OperatorType {

    ADD("+", 11, Associativity.LEFT, Category.ARITHMETIC),
    SUBTRACT("-", 11, Associativity.LEFT, Category.ARITHMETIC),
    MULTIPLY("*", 12, Associativity.LEFT, Category.ARITHMETIC),
    DIVIDE("/", 12, Associativity.LEFT, Category.ARITHMETIC),
    MODULO("%", 12, Associativity.LEFT, Category.ARITHMETIC),
    
    
    ASSIGN("=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    ADD_ASSIGN("+=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    SUBTRACT_ASSIGN("-=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    MULTIPLY_ASSIGN("*=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    DIVIDE_ASSIGN("/=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    MODULO_ASSIGN("%=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    AND_ASSIGN("&=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    OR_ASSIGN("|=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    XOR_ASSIGN("^=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    LEFT_SHIFT_ASSIGN("<<=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    RIGHT_SHIFT_ASSIGN(">>=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    UNSIGNED_RIGHT_SHIFT_ASSIGN(">>>=", 1, Associativity.RIGHT, Category.ASSIGNMENT),
    
    TERNARY_QUESTION("?", 2, Associativity.RIGHT, Category.TERNARY),
    TERNARY_COLON(":", 2, Associativity.RIGHT, Category.TERNARY),
    
    LOGICAL_OR("||", 3, Associativity.LEFT, Category.LOGICAL),
    LOGICAL_AND("&&", 4, Associativity.LEFT, Category.LOGICAL),
    
    BITWISE_OR("|", 5, Associativity.LEFT, Category.BITWISE),
    BITWISE_XOR("^", 6, Associativity.LEFT, Category.BITWISE),
    BITWISE_AND("&", 7, Associativity.LEFT, Category.BITWISE),
    
    EQUALS("==", 8, Associativity.LEFT, Category.RELATIONAL),
    NOT_EQUALS("!=", 8, Associativity.LEFT, Category.RELATIONAL),
    LESS_THAN("<", 9, Associativity.LEFT, Category.RELATIONAL),
    GREATER_THAN(">", 9, Associativity.LEFT, Category.RELATIONAL),
    LESS_THAN_OR_EQUAL("<=", 9, Associativity.LEFT, Category.RELATIONAL),
    GREATER_THAN_OR_EQUAL(">=", 9, Associativity.LEFT, Category.RELATIONAL),
    
    LEFT_SHIFT("<<", 10, Associativity.LEFT, Category.BITWISE),
    RIGHT_SHIFT(">>", 10, Associativity.LEFT, Category.BITWISE),
    UNSIGNED_RIGHT_SHIFT(">>>", 10, Associativity.LEFT, Category.BITWISE),

    UNARY_PLUS("+", 14, Associativity.RIGHT, Category.UNARY),
    UNARY_MINUS("-", 14, Associativity.RIGHT, Category.UNARY),
    LOGICAL_NOT("!", 14, Associativity.RIGHT, Category.UNARY),
    BITWISE_NOT("~", 14, Associativity.RIGHT, Category.UNARY),
    PRE_INCREMENT("++", 14, Associativity.RIGHT, Category.UNARY),
    PRE_DECREMENT("--", 14, Associativity.RIGHT, Category.UNARY),
    POST_INCREMENT("++", 15, Associativity.LEFT, Category.UNARY_POSTFIX),
    POST_DECREMENT("--", 15, Associativity.LEFT, Category.UNARY_POSTFIX),
    
    MEMBER_ACCESS(".", 16, Associativity.LEFT, Category.ACCESS),
    ARRAY_ACCESS("[]", 16, Associativity.LEFT, Category.ACCESS),
    METHOD_CALL("()", 16, Associativity.LEFT, Category.ACCESS),

    CAST("(type)", 14, Associativity.RIGHT, Category.CAST),
    INSTANCEOF("instanceof", 9, Associativity.LEFT, Category.INSTANCEOF);


    private final String symbol;
    private final int precedence;
    private final Associativity associativity;
    private final Category category;

    OperatorType(String symbol, int precedence, Associativity associativity, Category category) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.associativity = associativity;
        this.category = category;
    }

    public String getSymbol() { return symbol; }
    public int getPrecedence() { return precedence; }
    public Associativity getAssociativity() { return associativity; }
    public Category getCategory() { return category; }

    public boolean isBinary() {
        return category == Category.ARITHMETIC || category == Category.RELATIONAL ||
               category == Category.LOGICAL || category == Category.BITWISE;
    }

    public boolean isUnary() {
        return category == Category.UNARY || category == Category.UNARY_POSTFIX;
    }

    public boolean isAssignment() {
        return category == Category.ASSIGNMENT;
    }

    public boolean isComparison() {
        return category == Category.RELATIONAL;
    }

    public enum Associativity { LEFT, RIGHT }

    public enum Category {
        ARITHMETIC, UNARY, UNARY_POSTFIX, RELATIONAL, LOGICAL, BITWISE,
        TERNARY, INSTANCEOF, ASSIGNMENT, CAST, ACCESS
    }

    public static OperatorType fromSymbol(String symbol) {
        for (OperatorType op : values()) {
            if (op.symbol.equals(symbol)) return op;
        }
        return null;
    }

    public static OperatorType fromBinarySymbol(String symbol) {
        switch (symbol) {
            case "+": return ADD;
            case "-": return SUBTRACT;
            case "*": return MULTIPLY;
            case "/": return DIVIDE;
            case "%": return MODULO;
            case "==": return EQUALS;
            case "!=": return NOT_EQUALS;
            case "<": return LESS_THAN;
            case ">": return GREATER_THAN;
            case "<=": return LESS_THAN_OR_EQUAL;
            case ">=": return GREATER_THAN_OR_EQUAL;
            case "&&": return LOGICAL_AND;
            case "||": return LOGICAL_OR;
            case "&": return BITWISE_AND;
            case "|": return BITWISE_OR;
            case "^": return BITWISE_XOR;
            case "<<": return LEFT_SHIFT;
            case ">>": return RIGHT_SHIFT;
            case ">>>": return UNSIGNED_RIGHT_SHIFT;
            case "=": return ASSIGN;
            case "+=": return ADD_ASSIGN;
            case "-=": return SUBTRACT_ASSIGN;
            case "*=": return MULTIPLY_ASSIGN;
            case "/=": return DIVIDE_ASSIGN;
            case "%=": return MODULO_ASSIGN;
            case "&=": return AND_ASSIGN;
            case "|=": return OR_ASSIGN;
            case "^=": return XOR_ASSIGN;
            case "<<=": return LEFT_SHIFT_ASSIGN;
            case ">>=": return RIGHT_SHIFT_ASSIGN;
            case ">>>=": return UNSIGNED_RIGHT_SHIFT_ASSIGN;
            default: return null;
        }
    }
}
