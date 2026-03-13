package noppes.npcs.client.gui.util.script.interpreter.token;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

/**
 * Defines all token types for syntax highlighting with hex colors and priorities.
 * Priority determines which token type wins when marks overlap.
 * Higher priority = wins conflicts.
 *
 * Style properties (color, priority, bold, italic) can be overridden at runtime
 * via {@link ScriptColorScheme}. The enum constructor values serve as compile-time defaults.
 */
public enum TokenType {
    // Comments and strings have highest priority - they override everything inside them
    COMMENT(0xFF777777, 140),
    STRING(0xFFCC8855, 130),
    
    // JSDoc elements - lower priority but will fill gaps left by fragmented comment marking
    JSDOC_TAG(0xFFCC9933, 125),           // @param, @type, @return etc. (gold/orange)
    JSDOC_TYPE(0xFF00AAAA, 124),          // {TypeName} in JSDoc (aqua like types)
    
    UNUSED_IMPORT(0xFF666666, 119),       // unused import statements (gray)
    
    // Keywords and modifiers
    KEYWORD(0xFFFF5555, 100,true, false),            // control flow: if, else, for, while, etc.
    
    // Type declarations and references
    INTERFACE_DECL(0xFF55FFFF, 85),      // interface names (aqua)
    ENUM_DECL(0xFFFF55FF, 85),           // enum names (magenta)
    ENUM_CONSTANT(0xFF55FFFF, 84, true, false),  // enum constant values (blue, bold+italic) - like IntelliJ
    CLASS_DECL(0xFF00AAAA, 85),          // class names in declarations
    IMPORTED_CLASS(0xFF00AAAA, 75),      // imported class usages
    GENERIC_TYPE_PARAM(0xFF00FA9A, 76), // generic type parameters like T, E, K, V (light green)
    TYPE_DECL(0xFF00AAAA, 70),           // package paths, type references

    // Methods
    METHOD_DECL(0xFF00AA00, 60),         // method declarations (green)
    METHOD_CALL(0xFF55FF55, 50),         // method calls (bright green)

    // Variables and fields
    UNDEFINED_VAR(0xFFAA0000, 20),      // unresolved variables (dark red) - high priority
    PARAMETER(0xFF5555FF, 36),           // method parameters (blue)
    GLOBAL_FIELD(0xFF55FFFF, 35),        // class-level fields (aqua)
    LOCAL_FIELD(0xFFFFFF55, 25),         // local variables (yellow)
    STATIC_FINAL_FIELD(0xFFFF55FF, 36, false, true), // static final fields (magenta, italic)

    // Literals
    LITERAL(0xFF777777, 40),             // numeric and boolean literals

    // Default
    VARIABLE(0xFFFFFFFF, 30),            // generic variables
    DEFAULT(0xFFFFFFFF, 0);              // default text color (white)

    private final int defaultHexColor;
    private final int defaultPriority;
    private final boolean defaultBold;
    private final boolean defaultItalic;

    TokenType(int hexColor, int priority) {
        this(hexColor, priority, false, false);
    }

    TokenType(int hexColor, int priority, boolean bold, boolean italic) {
        this.defaultHexColor = hexColor;
        this.defaultPriority = priority;
        this.defaultBold = bold;
        this.defaultItalic = italic;
    }

    public int getHexColor() {
        return ScriptColorScheme.styles[ordinal()].hexColor;
    }

    public int getPriority() {
        return ScriptColorScheme.styles[ordinal()].priority;
    }

    public boolean isBold() {
        return ScriptColorScheme.styles[ordinal()].bold;
    }

    public boolean isItalic() {
        return ScriptColorScheme.styles[ordinal()].italic;
    }

    int getDefaultHexColor() {
        return defaultHexColor;
    }

    int getDefaultPriority() {
        return defaultPriority;
    }

    boolean getDefaultBold() {
        return defaultBold;
    }

    boolean getDefaultItalic() {
        return defaultItalic;
    }

    public static TokenType getByType(TypeInfo typeInfo) {
        if (typeInfo == null || !typeInfo.isResolved())
            return TokenType.UNDEFINED_VAR;

        if ("any".equals(typeInfo.getFullName()))
            return TokenType.KEYWORD;
        
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
            case JSDOC_TAG:
                return '6'; // gold
            case JSDOC_TYPE:
                return '3'; // dark aqua (like types)
            case KEYWORD:
                return 'c'; // red
            case ENUM_DECL:
            case STATIC_FINAL_FIELD:
                return 'd'; // magenta
            case ENUM_CONSTANT:
                return '9'; // blue (same as PARAMETER)
            case INTERFACE_DECL:
            case GLOBAL_FIELD:
                return 'b'; // aqua
            case GENERIC_TYPE_PARAM:
                return 'a'; // green (distinct from class types)
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
