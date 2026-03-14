package bigguy.treesitter.java;

/**
 * A single highlight capture result produced by running a highlight query
 * against a parsed syntax tree.
 *
 * <p>Captures are the primary output of {@link JavaQueryEngine#highlights(SyntaxTree, String)}.
 * Each capture associates a region of source text ({@link #getSpan()}) with a
 * semantic {@link HighlightGroup} (e.g. {@code KEYWORD}, {@code FUNCTION}).</p>
 *
 * <p>Instances are immutable and safe to cache, sort, or pass between threads.</p>
 */
public final class HighlightCapture {

    private final TextSpan span;
    private final HighlightGroup group;
    private final String nodeType;
    private final int patternIndex;

    public HighlightCapture(TextSpan span, HighlightGroup group, String nodeType, int patternIndex) {
        this.span = span;
        this.group = group;
        this.nodeType = nodeType;
        this.patternIndex = patternIndex;
    }

    /** @return the source range of this capture */
    public TextSpan getSpan() { return span; }

    /** @return the semantic highlight group (e.g. {@code KEYWORD}) */
    public HighlightGroup getGroup() { return group; }

    /** @return the tree-sitter node type string (e.g. {@code "method_declaration"}) */
    public String getNodeType() { return nodeType; }

    /** @return the index of the pattern in the query that produced this capture */
    public int getPatternIndex() { return patternIndex; }

    @Override
    public String toString() {
        return String.format("Highlight{%s %s %s}", group.getCaptureName(), nodeType, span);
    }
}
