package bigguy.texteditor.render;

/**
 * An individual renderable text chunk within a {@link RenderLine}.
 * Each segment represents either a styled token or an unstyled gap between tokens.
 *
 * <p>Immutable value type consumed during {@link RenderLine#drawStringHex(int, int)}
 * rendering. Segments are built on each draw call from the line's token list and
 * are not cached.
 */
public final class RenderSegment {

    /** Local offset within the line text where this segment starts. */
    public final int startPosition;

    /** The displayable text content (may include style prefix codes for tokens). */
    public final String text;

    /** ARGB hex color for rendering (e.g. 0xFFFF5555 for red). */
    public final int hexColor;

    /** True if this segment was produced from a Token; false for gap/trailing text. */
    public final boolean isToken;

    /**
     * @param startPosition local offset within the line text
     * @param text          displayable text content
     * @param hexColor      ARGB hex color
     * @param isToken       true when this segment originates from a Token
     */
    public RenderSegment(int startPosition, String text, int hexColor, boolean isToken) {
        this.startPosition = startPosition;
        this.text = text;
        this.hexColor = hexColor;
        this.isToken = isToken;
    }

    @Override
    public String toString() {
        return "RenderSegment{pos=" + startPosition
                + ", text='" + text + "'"
                + ", color=0x" + Integer.toHexString(hexColor)
                + (isToken ? ", token" : ", gap")
                + "}";
    }
}
