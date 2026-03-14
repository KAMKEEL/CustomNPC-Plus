package bigguy.treesitter.java;

/**
 * An immutable representation of a contiguous range in source text.
 *
 * <p>A {@code TextSpan} captures both <em>byte offsets</em> (used by tree-sitter
 * internally) and <em>row/column positions</em> (used for human-readable output
 * and editor integration). Every syntax node, highlight capture, and outline
 * entry carries a {@code TextSpan} so callers never need to work with raw
 * tree-sitter offset arithmetic.</p>
 *
 * <h3>Design note (Zed mapping)</h3>
 * <p>In Zed, ranges are represented by {@code Range<Anchor>} in the buffer layer
 * and by {@code TSPoint}/{@code TSRange} in the tree-sitter layer. This class
 * unifies both representations into a single, self-contained value object that
 * is safe to store, compare, and serialize.</p>
 *
 * <h3>Thread safety</h3>
 * <p>Instances are immutable and therefore inherently thread-safe.</p>
 */
public final class TextSpan {

    /** Row index (0-based) of the span's first character. */
    private final int startRow;

    /** Column index (0-based) of the span's first character. */
    private final int startColumn;

    /** Row index (0-based) of the character <em>after</em> the span. */
    private final int endRow;

    /** Column index (0-based) of the character <em>after</em> the span. */
    private final int endColumn;

    /** Byte offset (0-based) of the span's first byte. */
    private final int startByte;

    /** Byte offset (0-based) of the byte <em>after</em> the span. */
    private final int endByte;

    /**
     * Creates a new {@code TextSpan}.
     *
     * @param startRow    row of the first character (0-based)
     * @param startColumn column of the first character (0-based)
     * @param endRow      row after the last character (0-based)
     * @param endColumn   column after the last character (0-based)
     * @param startByte   byte offset of the first byte (0-based)
     * @param endByte     byte offset after the last byte (0-based)
     */
    public TextSpan(int startRow, int startColumn, int endRow, int endColumn,
                    int startByte, int endByte) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;
        this.startByte = startByte;
        this.endByte = endByte;
    }

    /** @return row of the first character (0-based) */
    public int getStartRow() { return startRow; }

    /** @return column of the first character (0-based) */
    public int getStartColumn() { return startColumn; }

    /** @return row after the last character (0-based) */
    public int getEndRow() { return endRow; }

    /** @return column after the last character (0-based) */
    public int getEndColumn() { return endColumn; }

    /** @return byte offset of the first byte (0-based) */
    public int getStartByte() { return startByte; }

    /** @return byte offset after the last byte (0-based) */
    public int getEndByte() { return endByte; }

    /** @return the length in bytes of this span */
    public int getByteLength() { return endByte - startByte; }

    /**
     * Tests whether this span contains the given byte offset.
     *
     * @param byteOffset the offset to test
     * @return {@code true} if {@code startByte <= byteOffset < endByte}
     */
    public boolean containsByte(int byteOffset) {
        return byteOffset >= startByte && byteOffset < endByte;
    }

    /**
     * Tests whether this span fully contains {@code other}.
     *
     * @param other the span to test
     * @return {@code true} if this span encloses {@code other}
     */
    public boolean contains(TextSpan other) {
        return this.startByte <= other.startByte && this.endByte >= other.endByte;
    }

    /**
     * Tests whether this span overlaps with {@code other}.
     *
     * @param other the span to test
     * @return {@code true} if the two spans share at least one byte
     */
    public boolean overlaps(TextSpan other) {
        return this.startByte < other.endByte && other.startByte < this.endByte;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TextSpan)) return false;
        TextSpan that = (TextSpan) obj;
        return startByte == that.startByte && endByte == that.endByte
                && startRow == that.startRow && startColumn == that.startColumn
                && endRow == that.endRow && endColumn == that.endColumn;
    }

    @Override
    public int hashCode() {
        int result = startByte;
        result = 31 * result + endByte;
        result = 31 * result + startRow;
        result = 31 * result + startColumn;
        return result;
    }

    @Override
    public String toString() {
        return String.format("[%d:%d..%d:%d](bytes %d..%d)",
                startRow, startColumn, endRow, endColumn, startByte, endByte);
    }
}
