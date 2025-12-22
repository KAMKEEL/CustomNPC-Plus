package noppes.npcs.client.gui.util.script.interpreter;

/**
 * Defines all token types for syntax highlighting with hex colors and priorities.
 * Priority determines which token type wins when marks overlap.
 * Higher priority = wins conflicts.
 */
public enum TokenType {
    // Comments and strings have highest priority - they override everything inside them
    COMMENT(0x777777, 130),
    STRING(0xCC8855, 120),
    
    // Keywords and modifiers
    CLASS_KEYWORD(0xFF5555, 115),      // 'class', 'interface', 'enum' keywords
    IMPORT_KEYWORD(0xFFAA00, 110),     // 'import' keyword
    KEYWORD(0xFF5555, 100),            // control flow: if, else, for, while, etc.
    MODIFIER(0xFFAA00, 90),            // public, private, static, final, etc.
    
    // Type declarations and references
    NEW_TYPE(0xFF55FF, 80),            // type after 'new' keyword
    INTERFACE_DECL(0x55FFFF, 85),      // interface names (aqua)
    ENUM_DECL(0xFF55FF, 85),           // enum names (magenta)
    CLASS_DECL(0x00AAAA, 85),          // class names in declarations
    IMPORTED_CLASS(0x00AAAA, 75),      // imported class usages
    TYPE_DECL(0x00AAAA, 70),           // package paths, type references
    
    // Methods
    METHOD_DECL(0x00AA00, 60),         // method declarations (green)
    METHOD_CALL(0x55FF55, 50),         // method calls (bright green)
    
    // Variables and fields
    UNDEFINED_VAR(0xAA0000, 105),      // unresolved variables (dark red) - high priority
    PARAMETER(0x5555FF, 36),           // method parameters (blue)
    GLOBAL_FIELD(0x55FFFF, 35),        // class-level fields (aqua)
    LOCAL_FIELD(0xFFFF55, 25),         // local variables (yellow)
    
    // Literals
    NUMBER(0x777777, 40),              // numeric literals
    
    // Default
    VARIABLE(0xFFFFFF, 30),            // generic variables
    DEFAULT(0xFFFFFF, 0);              // default text color (white)

    private final int hexColor;
    private final int priority;

    TokenType(int hexColor, int priority) {
        this.hexColor = hexColor;
        this.priority = priority;
    }

    public int getHexColor() {
        return hexColor;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * Convert this token type to a Minecraft color code character.
     * Used for backward compatibility with the existing rendering system.
     */
    public char toColorCode() {
        switch (this) {
            case COMMENT:
            case NUMBER:
                return '7'; // gray
            case STRING:
                return '5'; // purple
            case CLASS_KEYWORD:
            case KEYWORD:
                return 'c'; // red
            case IMPORT_KEYWORD:
            case MODIFIER:
                return '6'; // gold
            case NEW_TYPE:
            case ENUM_DECL:
                return 'd'; // magenta
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
