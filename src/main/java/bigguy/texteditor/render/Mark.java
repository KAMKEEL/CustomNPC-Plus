package bigguy.texteditor.render;

import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

/**
 * A highlight region produced by the analysis pipeline.
 * Marks bridge the gap between CST/semantic analysis and token rendering.
 *
 * <p>Marks are produced by:
 * <ul>
 *   <li>Tree-sitter highlight queries (syntax marks)</li>
 *   <li>Semantic analysis (type resolution, scope coloring)</li>
 *   <li>Error detection (diagnostic marks)</li>
 * </ul>
 *
 * <p>Multiple marks can overlap. {@link MarkLayer} resolves conflicts using
 * {@link TokenType#getPriority()}.
 *
 * <p>Sorting order (used by {@link MarkLayer#merge()}):
 * <ol>
 *   <li>By start position ascending</li>
 *   <li>By span length descending (longest first)</li>
 *   <li>By priority descending (highest first)</li>
 * </ol>
 */
public final class Mark implements Comparable<Mark> {

    /** Byte offset where this mark begins (inclusive). */
    public final int start;

    /** Byte offset where this mark ends (exclusive). */
    public final int end;

    /** The token type that determines color, priority, and style. */
    public final TokenType type;

    /**
     * Optional metadata attached by the analysis pipeline.
     * May be {@link noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo},
     * {@link noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo},
     * {@link noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo},
     * {@link noppes.npcs.client.gui.util.script.interpreter.type.ImportData},
     * {@link noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage},
     * or any other analysis artifact. Null when no metadata is available.
     */
    public final Object metadata;

    /**
     * Create a mark with no metadata (pure syntax mark).
     *
     * @param start byte offset where this mark begins (inclusive)
     * @param end   byte offset where this mark ends (exclusive)
     * @param type  the token type for coloring and priority
     */
    public Mark(int start, int end, TokenType type) {
        this(start, end, type, null);
    }

    /**
     * Create a mark with metadata (semantic or diagnostic mark).
     *
     * @param start    byte offset where this mark begins (inclusive)
     * @param end      byte offset where this mark ends (exclusive)
     * @param type     the token type for coloring and priority
     * @param metadata optional analysis metadata, may be null
     */
    public Mark(int start, int end, TokenType type, Object metadata) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.metadata = metadata;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Create a syntax-only mark (no metadata).
     *
     * @param start byte offset where this mark begins (inclusive)
     * @param end   byte offset where this mark ends (exclusive)
     * @param type  the token type for coloring and priority
     * @return a new Mark with no metadata
     */
    public static Mark syntax(int start, int end, TokenType type) {
        return new Mark(start, end, type, null);
    }

    /**
     * Create a semantic mark with attached metadata.
     *
     * @param start    byte offset where this mark begins (inclusive)
     * @param end      byte offset where this mark ends (exclusive)
     * @param type     the token type for coloring and priority
     * @param metadata analysis metadata (TypeInfo, MethodCallInfo, etc.)
     * @return a new Mark with the given metadata
     */
    public static Mark semantic(int start, int end, TokenType type, Object metadata) {
        return new Mark(start, end, type, metadata);
    }

    // ==================== QUERIES ====================

    /**
     * Check whether this mark's range overlaps with the given range.
     *
     * @param otherStart start of the other range (inclusive)
     * @param otherEnd   end of the other range (exclusive)
     * @return true if the ranges overlap
     */
    public boolean overlaps(int otherStart, int otherEnd) {
        return this.start < otherEnd && this.end > otherStart;
    }

    /**
     * Get the length of this mark in bytes.
     *
     * @return end - start
     */
    public int length() {
        return end - start;
    }

    /**
     * Check whether this mark carries metadata.
     *
     * @return true if metadata is non-null
     */
    public boolean hasMetadata() {
        return metadata != null;
    }

    // ==================== Comparable ====================

    /**
     * Sort marks by start position ascending, then by span length descending
     * (longest first), then by priority descending (highest first).
     * This ordering ensures that when two marks start at the same position,
     * the one covering more text (or with higher priority) is processed first.
     */
    @Override
    public int compareTo(Mark other) {
        // Primary: start position ascending
        int cmp = Integer.compare(this.start, other.start);
        if (cmp != 0) return cmp;

        // Secondary: span length descending (longest first)
        cmp = Integer.compare(other.length(), this.length());
        if (cmp != 0) return cmp;

        // Tertiary: priority descending (highest first)
        return Integer.compare(other.type.getPriority(), this.type.getPriority());
    }

    // ==================== equals / hashCode ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Mark)) return false;
        Mark other = (Mark) obj;
        return this.start == other.start
                && this.end == other.end
                && this.type == other.type;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + type.hashCode();
        return result;
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "Mark{" + type + " [" + start + "-" + end + "]"
                + (metadata != null ? " " + metadata.getClass().getSimpleName() : "")
                + "}";
    }
}
