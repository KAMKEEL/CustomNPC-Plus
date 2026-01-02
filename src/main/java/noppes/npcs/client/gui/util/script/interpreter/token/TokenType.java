package noppes.npcs.client.gui.util.script.interpreter.token;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Defines all token types for syntax highlighting with hex colors and priorities.
 * Priority determines which token type wins when marks overlap.
 * Higher priority = wins conflicts.
 */
public enum TokenType {
    // Comments and strings have highest priority - they override everything inside them
    COMMENT(0x777777, 130),
    STRING(0xCC8855, 120),
    UNUSED_IMPORT(0x666666, 119),       // unused import statements (gray)
    
    // Keywords and modifiers
    CLASS_KEYWORD(0xFF5555, 115),      // 'class', 'interface', 'enum' keywords
    IMPORT_KEYWORD(0xFFAA00, 110),     // 'import' keyword
    KEYWORD(0xFF5555, 100),            // control flow: if, else, for, while, etc.
    MODIFIER(0xFFAA00, 90),            // public, private, static, final, etc.
    
    // Type declarations and references
    INTERFACE_DECL(0x55FFFF, 85),      // interface names (aqua)
    ENUM_DECL(0xFF55FF, 85),           // enum names (magenta)
    ENUM_CONSTANT(0x55FFFF, 84, true, false),  // enum constant values (blue, bold+italic) - like IntelliJ
    CLASS_DECL(0x00AAAA, 85),          // class names in declarations
    IMPORTED_CLASS(0x00AAAA, 75),      // imported class usages
    TYPE_DECL(0x00AAAA, 70),           // package paths, type references

    // Methods
    METHOD_DECL(0x00AA00, 60),         // method declarations (green)
    METHOD_CALL(0x55FF55, 50),         // method calls (bright green)

    // Variables and fields
    UNDEFINED_VAR(0xAA0000, 20),      // unresolved variables (dark red) - high priority
    PARAMETER(0x5555FF, 36),           // method parameters (blue)
    GLOBAL_FIELD(0x55FFFF, 35),        // class-level fields (aqua)
    LOCAL_FIELD(0xFFFF55, 25),         // local variables (yellow)
    STATIC_FINAL_FIELD(0xFF55FF, 36, false, true), // static final fields (magenta, italic)

    // Literals
    LITERAL(0x777777, 40),             // numeric and boolean literals

    // Default
    VARIABLE(0xFFFFFF, 30),            // generic variables
    DEFAULT(0xFFFFFF, 0);              // default text color (white)

    private final int hexColor;
    private final int priority;
    private final boolean bold;
    private final boolean italic;

    TokenType(int hexColor, int priority) {
        this(hexColor, priority, false, false);
    }

    TokenType(int hexColor, int priority, boolean bold, boolean italic) {
        this.hexColor = hexColor;
        this.priority = priority;
        this.bold = bold;
        this.italic = italic;
    }

    public int getHexColor() {
        return hexColor;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public static TokenType getByType(TypeInfo typeInfo) {
        if (typeInfo == null || !typeInfo.isResolved())
            return TokenType.UNDEFINED_VAR;

        switch (typeInfo.getKind()) {
            case INTERFACE:
                return TokenType.INTERFACE_DECL;
            case ENUM:
                return TokenType.ENUM_DECL;
            case CLASS:
                return TokenType.CLASS_DECL;
            default:
                break;
        }

        // Use the TypeInfo's own token type, which handles ScriptTypeInfo correctly
        return typeInfo.getTokenType();
    }
    
    public static int getColor(TypeInfo typeInfo) {
        return getByType(typeInfo).getHexColor();
    }

    /**
     * Convert this token type to a Minecraft color code character.
     * Used for backward compatibility with the existing rendering system.
     */
    public char toColorCode() {
        switch (this) {
            case COMMENT:
            case LITERAL:
            case UNUSED_IMPORT:
                return '7'; // gray
            case STRING:
                return '5'; // purple
            case CLASS_KEYWORD:
            case KEYWORD:
                return 'c'; // red
            case IMPORT_KEYWORD:
            case MODIFIER:
                return '6'; // gold
            case ENUM_DECL:
            case STATIC_FINAL_FIELD:
                return 'd'; // magenta
            case ENUM_CONSTANT:
                return '9'; // blue (same as PARAMETER)
            case INTERFACE_DECL:
            case GLOBAL_FIELD:
                return 'b'; // aqua
            case CLASS_DECL:
            case IMPORTED_CLASS:
            case TYPE_DECL:
                return '3'; // dark aqua
            case METHOD_DECL:
                return '2'; // dark green
            case METHOD_CALL:
                return 'a'; // green
            case LOCAL_FIELD:
                return 'e'; // yellow
            case PARAMETER:
                return '9'; // blue
            case UNDEFINED_VAR:
                return '4'; // dark red
            case VARIABLE:
            case DEFAULT:
            default:
                return 'f'; // white
        }
    }
}
