package bigguy.texteditor.treesitter.java;

import java.util.Collections;
import java.util.List;

/**
 * A symbol entry extracted from the code outline query.
 *
 * <p>Maps directly to Zed's outline query results, where each entry
 * has a {@code @name} capture, optional {@code @context} captures
 * (modifier keywords like {@code public static}), and an {@code @item}
 * capture for the full declaration body.</p>
 *
 * @see JavaQueryEngine#outline(SyntaxTree, String)
 */
public final class OutlineEntry {

    /**
     * Categorization of the outlined symbol, derived from the tree-sitter
     * node type of the {@code @item} capture.
     */
    public enum Kind {
        CLASS, INTERFACE, ENUM, RECORD, ANNOTATION_TYPE,
        METHOD, CONSTRUCTOR, FIELD, ENUM_CONSTANT,
        STATIC_INITIALIZER, UNKNOWN
    }

    private final String name;
    private final Kind kind;
    private final TextSpan nameSpan;
    private final TextSpan bodySpan;
    private final List<String> contextKeywords;
    private final int depth;

    public OutlineEntry(String name, Kind kind, TextSpan nameSpan, TextSpan bodySpan,
                        List<String> contextKeywords, int depth) {
        this.name = name;
        this.kind = kind;
        this.nameSpan = nameSpan;
        this.bodySpan = bodySpan;
        this.contextKeywords = contextKeywords != null
                ? Collections.unmodifiableList(contextKeywords)
                : Collections.<String>emptyList();
        this.depth = depth;
    }

    /** @return the symbol's display name */
    public String getName() { return name; }

    /** @return the kind of symbol (class, method, field, etc.) */
    public Kind getKind() { return kind; }

    /** @return the source span of the symbol's name token */
    public TextSpan getNameSpan() { return nameSpan; }

    /** @return the source span of the full declaration body */
    public TextSpan getBodySpan() { return bodySpan; }

    /** @return modifier keywords like {@code ["public", "static", "final"]} */
    public List<String> getContextKeywords() { return contextKeywords; }

    /** @return the nesting depth of this entry (0 = top-level) */
    public int getDepth() { return depth; }

    @Override
    public String toString() {
        String ctx = contextKeywords.isEmpty() ? "" : String.join(" ", contextKeywords) + " ";
        return String.format("Outline{%s%s %s %s}", ctx, kind, name, nameSpan);
    }
}
