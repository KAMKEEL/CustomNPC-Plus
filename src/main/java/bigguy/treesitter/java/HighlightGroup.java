package bigguy.treesitter.java;

/**
 * Highlight groups corresponding to Zed's capture name conventions.
 *
 * <p>Each constant maps directly to a {@code @capture_name} used in the
 * tree-sitter highlight query ({@code highlights.scm}). The mapping follows
 * Zed's naming conventions exactly, enabling downstream renderers to produce
 * identical colorization.</p>
 *
 * @see QueryPatterns#HIGHLIGHTS
 */
public enum HighlightGroup {

    PROPERTY("property"),
    VARIABLE("variable"),
    PARAMETER("parameter"),
    FUNCTION("function"),
    FUNCTION_CALL("function.call"),
    SUPER("function.builtin"),
    TYPE("type"),
    TYPE_PARAMETER("type.parameter"),
    INTERFACE("interface"),
    TYPE_BUILTIN("type.builtin"),
    CONSTRUCTOR("constructor"),
    CONSTANT("constant"),
    CONSTANT_BUILTIN("constant.builtin"),
    KEYWORD("keyword"),
    KEYWORD_CONTROL("keyword.control"),
    KEYWORD_OPERATOR("keyword.operator"),
    KEYWORD_FUNCTION("keyword.function"),
    KEYWORD_TYPE("keyword.type"),
    KEYWORD_MODIFIER("keyword.modifier"),
    OPERATOR("operator"),
    STRING("string"),
    STRING_ESCAPE("string.escape"),
    STRING_SPECIAL("string.special"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    COMMENT("comment"),
    COMMENT_DOC("comment.doc"),
    ATTRIBUTE("attribute"),
    LABEL("label"),
    ENUM("enum"),
    PUNCTUATION_BRACKET("punctuation.bracket"),
    PUNCTUATION_DELIMITER("punctuation.delimiter"),
    PUNCTUATION_SPECIAL("punctuation.special"),
    EMBEDDED("embedded"),
    UNKNOWN("unknown");

    private final String captureName;

    HighlightGroup(String captureName) {
        this.captureName = captureName;
    }

    /** @return the tree-sitter capture name (e.g. {@code "function"} for {@code @function}) */
    public String getCaptureName() {
        return captureName;
    }

    /**
     * Resolves a tree-sitter capture name to its corresponding {@code HighlightGroup}.
     *
     * @param captureName the capture name without the leading {@code @}
     * @return the matching group, or {@link #UNKNOWN} if no mapping exists
     */
    public static HighlightGroup fromCaptureName(String captureName) {
        for (HighlightGroup group : values()) {
            if (group.captureName.equals(captureName)) {
                return group;
            }
        }
        return UNKNOWN;
    }
}
