package noppes.npcs.client.gui.util.script.interpreter;

/**
 * Represents a single token in the source code with its type and metadata.
 * Tokens are the atomic units of the syntax highlighting system.
 * 
 * Each token knows:
 * - Its text content
 * - Its position in the global source
 * - Its type (for coloring)
 * - Type-specific metadata (resolved class info, declaration info, etc.)
 */
public class Token {

    private final String text;
    private final int globalStart;    // start offset in the full document
    private final int globalEnd;      // end offset in the full document
    private TokenType type;
    
    // Optional metadata based on token type
    private TypeInfo typeInfo;        // For class references, type declarations
    private FieldInfo fieldInfo;      // For field references
    private MethodInfo methodInfo;    // For method calls/declarations
    private MethodCallInfo methodCallInfo; // For method calls with argument info
    private ImportData importData;    // For import statements
    
    // Rendering flags
    private boolean hasUnderline;     // True if this token should be underlined (for errors)
    private int underlineColor = 0xFF5555;       // Color of the underline (if any)

    // Navigation - set by ScriptLine
    private Token prev;
    private Token next;
    private ScriptLine parentLine;

    public Token(String text, int globalStart, int globalEnd, TokenType type) {
        this.text = text;
        this.globalStart = globalStart;
        this.globalEnd = globalEnd;
        this.type = type;
    }

    // ==================== FACTORY METHODS ====================

    public static Token defaultToken(String text, int start, int end) {
        return new Token(text, start, end, TokenType.DEFAULT);
    }

    public static Token keyword(String text, int start, int end) {
        return new Token(text, start, end, TokenType.KEYWORD);
    }

    public static Token modifier(String text, int start, int end) {
        return new Token(text, start, end, TokenType.MODIFIER);
    }

    public static Token comment(String text, int start, int end) {
        return new Token(text, start, end, TokenType.COMMENT);
    }

    public static Token string(String text, int start, int end) {
        return new Token(text, start, end, TokenType.STRING);
    }

    public static Token number(String text, int start, int end) {
        return new Token(text, start, end, TokenType.NUMBER);
    }

    public static Token typeReference(String text, int start, int end, TypeInfo info) {
        Token t = new Token(text, start, end, info != null ? info.getTokenType() : TokenType.TYPE_DECL);
        t.typeInfo = info;
        return t;
    }

    public static Token methodDecl(String text, int start, int end, MethodInfo info) {
        Token t = new Token(text, start, end, TokenType.METHOD_DECL);
        t.methodInfo = info;
        return t;
    }

    public static Token methodCall(String text, int start, int end, MethodInfo info, boolean resolved) {
        Token t = new Token(text, start, end, resolved ? TokenType.METHOD_CALL : TokenType.DEFAULT);
        t.methodInfo = info;
        return t;
    }

    public static Token globalField(String text, int start, int end, FieldInfo info) {
        Token t = new Token(text, start, end, TokenType.GLOBAL_FIELD);
        t.fieldInfo = info;
        return t;
    }

    public static Token localField(String text, int start, int end, FieldInfo info) {
        Token t = new Token(text, start, end, TokenType.LOCAL_FIELD);
        t.fieldInfo = info;
        return t;
    }

    public static Token parameter(String text, int start, int end, FieldInfo info) {
        Token t = new Token(text, start, end, TokenType.PARAMETER);
        t.fieldInfo = info;
        return t;
    }

    public static Token undefined(String text, int start, int end) {
        return new Token(text, start, end, TokenType.UNDEFINED_VAR);
    }

    // ==================== GETTERS ====================

    public String getText() { return text; }
    public int getGlobalStart() { return globalStart; }
    public int getGlobalEnd() { return globalEnd; }
    public int getLength() { return globalEnd - globalStart; }
    public TokenType getType() { return type; }
    public TypeInfo getTypeInfo() { return typeInfo; }
    public FieldInfo getFieldInfo() { return fieldInfo; }
    public MethodInfo getMethodInfo() { return methodInfo; }
    public MethodCallInfo getMethodCallInfo() { return methodCallInfo; }
    public ImportData getImportData() { return importData; }
    public ScriptLine getParentLine() { return parentLine; }

    public boolean hasUnderline() {
        if (methodCallInfo != null) {
            if (methodCallInfo.hasArgCountError(this))
                return true;
            else if (methodCallInfo.hasArgTypeError(this)) {
                return true;
            }
        }
        return false;
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    // ==================== SETTERS ====================

    public void setType(TokenType type) { this.type = type; }
    public void setTypeInfo(TypeInfo info) { this.typeInfo = info; }
    public void setFieldInfo(FieldInfo info) { this.fieldInfo = info; }
    public void setMethodInfo(MethodInfo info) { this.methodInfo = info; }
    public void setMethodCallInfo(MethodCallInfo info) { this.methodCallInfo = info; }
    public void setImportData(ImportData data) { this.importData = data; }
    
    public void setUnderline(boolean hasUnderline, int color) {
        this.hasUnderline = hasUnderline;
        this.underlineColor = color;
    }

    void setParentLine(ScriptLine line) { this.parentLine = line; }
    void setPrev(Token prev) { this.prev = prev; }
    void setNext(Token next) { this.next = next; }

    // ==================== NAVIGATION ====================

    /**
     * Get the previous token (may be on a previous line).
     */
    public Token prev() {
        if (prev != null) return prev;
        if (parentLine == null) return null;
        
        ScriptLine prevLine = parentLine.prev();
        while (prevLine != null) {
            Token last = prevLine.getLastToken();
            if (last != null) return last;
            prevLine = prevLine.prev();
        }
        return null;
    }

    /**
     * Get the next token (may be on a following line).
     */
    public Token next() {
        if (next != null) return next;
        if (parentLine == null) return null;
        
        ScriptLine nextLine = parentLine.next();
        while (nextLine != null) {
            Token first = nextLine.getFirstToken();
            if (first != null) return first;
            nextLine = nextLine.next();
        }
        return null;
    }

    /**
     * Get the previous token on the same line only.
     */
    public Token prevOnLine() {
        return prev;
    }

    /**
     * Get the next token on the same line only.
     */
    public Token nextOnLine() {
        return next;
    }

    // ==================== TYPE CHECKS ====================

    public boolean isKeyword() {
        return type == TokenType.KEYWORD || type == TokenType.MODIFIER || 
               type == TokenType.CLASS_KEYWORD || type == TokenType.IMPORT_KEYWORD;
    }

    public boolean isTypeReference() {
        return type == TokenType.TYPE_DECL || type == TokenType.IMPORTED_CLASS ||
               type == TokenType.CLASS_DECL || type == TokenType.INTERFACE_DECL ||
               type == TokenType.ENUM_DECL || type == TokenType.NEW_TYPE;
    }

    public boolean isResolved() {
        if (typeInfo != null) return typeInfo.isResolved();
        if (fieldInfo != null) return fieldInfo.isResolved();
        if (methodInfo != null) return methodInfo.isResolved();
        return type != TokenType.UNDEFINED_VAR;
    }

    public boolean isIdentifier() {
        if (text.isEmpty()) return false;
        char first = text.charAt(0);
        return Character.isJavaIdentifierStart(first);
    }

    public boolean startsWithUpperCase() {
        return !text.isEmpty() && Character.isUpperCase(text.charAt(0));
    }

    public boolean isMethodCall() {
        return type == TokenType.METHOD_CALL || 
               (type == TokenType.DEFAULT && methodInfo != null);
    }

    public boolean isField() {
        return type == TokenType.GLOBAL_FIELD || type == TokenType.LOCAL_FIELD || 
               type == TokenType.PARAMETER;
    }

    // ==================== RENDERING ====================

    /**
     * Get the hex color for this token.
     */
    public int getHexColor() {
        return type.getHexColor();
    }

    /**
     * Get the Minecraft color code character.
     */
    public char getColorCode() {
        return type.toColorCode();
    }

    @Override
    public String toString() {
        return "Token{'" + text + "', " + type + ", [" + globalStart + "-" + globalEnd + "]}";
    }
}
