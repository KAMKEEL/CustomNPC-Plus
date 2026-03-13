package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import org.lwjgl.opengl.GL11;

/**
 * Helper class for rendering error underlines in script editor lines.
 * Renders underlines from the document's centralized error list ({@link DocumentError}).
 * 
 * <p>Error collection happens during analysis in {@link ScriptDocument#formatCodeText()}.
 * This renderer only draws; it does not detect or collect errors.
 */
public class ErrorUnderlineRenderer {
    private static final int ERROR_COLOR = 0xFF5555;

    // Holds the calculation result for an underline
    private static class UnderlinePosition {
        final int x;
        final int width;

        UnderlinePosition(int x, int width) {
            this.x = x;
            this.width = width;
        }

        boolean isValid() {
            return width > 0;
        }
    }

    /**
     * Draw error underlines for all validation errors in the document that intersect the given line.
     * Iterates over the document's centralized error list and draws underlines for each error
     * whose span intersects this line.
     *
     * @param doc The script document containing all errors
     * @param lineStartX X coordinate where the line starts rendering
     * @param baselineY Y coordinate for the underline baseline
     * @param lineText The text content of the line
     * @param lineStart Global offset where this line starts
     * @param lineEnd Global offset where this line ends
     */
    public static void drawErrorUnderlines(
            ScriptDocument doc,
            int lineStartX, int baselineY,
            String lineText, int lineStart, int lineEnd) {
        drawErrorUnderlines(doc, null, lineStartX, baselineY, lineText, lineStart, lineEnd);
    }

    /**
     * Draw error underlines for all validation errors in the document that intersect the given line.
     * When a ScriptLine is provided, uses style-aware width calculation so underlines align
     * correctly under bold/italic tokens.
     */
    public static void drawErrorUnderlines(
            ScriptDocument doc, ScriptLine line,
            int lineStartX, int baselineY,
            String lineText, int lineStart, int lineEnd) {

        if (doc == null)
            return;

        for (DocumentError error : doc.getErrors()) {
            drawUnderlineForSpan(error.getStartPos(), error.getEndPos(),
                    lineStartX, baselineY, lineText, lineStart, lineEnd, ERROR_COLOR, line);
        }
    }

    /**
     * Calculate underline position for a span of text within a line.
     * Handles clipping to line boundaries and pixel width calculation.
     * When a ScriptLine is provided, uses style-aware width calculation.
     *
     * @param spanStart Global offset where the span starts
     * @param spanEnd Global offset where the span ends
     * @param lineStartX X coordinate where the line starts rendering
     * @param lineText The text content of the line
     * @param lineStart Global offset where this line starts
     * @param lineEnd Global offset where this line ends
     * @param line Optional ScriptLine for style-aware width; null falls back to plain width
     * @return UnderlinePosition with x and width, or null if span doesn't intersect line
     */
    private static UnderlinePosition calculateUnderlinePosition(
            int spanStart, int spanEnd,
            int lineStartX, String lineText, int lineStart, int lineEnd,
            ScriptLine line) {

        // Skip if span doesn't intersect this line
        if (spanEnd < lineStart || spanStart > lineEnd)
            return null;

        // Clip to line boundaries
        int clipStart = Math.max(spanStart, lineStart);
        int clipEnd = Math.min(spanEnd, lineEnd);

        if (clipStart >= clipEnd)
            return null;

        // Convert to line-local coordinates
        int lineLocalStart = clipStart - lineStart;
        int lineLocalEnd = clipEnd - lineStart;

        // Bounds check
        if (lineLocalStart < 0 || lineLocalStart >= lineText.length())
            return null;

        // Compute pixel position using style-aware width when available
        int beforeWidth;
        int spanWidth;

        if (line != null) {
            beforeWidth = line.getRenderedWidth(0, lineLocalStart);
            int clampedEnd = Math.min(lineLocalEnd, lineText.length());
            spanWidth = line.getRenderedWidth(lineLocalStart, clampedEnd);
        } else {
            String beforeSpan = lineText.substring(0, lineLocalStart);
            beforeWidth = ClientProxy.Font.width(beforeSpan);

            if (lineLocalEnd > lineText.length()) {
                spanWidth = ClientProxy.Font.width(lineText.substring(lineLocalStart));
            } else {
                String spanTextOnLine = lineText.substring(lineLocalStart, lineLocalEnd);
                spanWidth = ClientProxy.Font.width(spanTextOnLine);
            }
        }

        return new UnderlinePosition(lineStartX + beforeWidth, spanWidth);
    }

    /**
     * Draw an underline for a simple span if it intersects the line.
     *
     * @param spanStart Global offset where the span starts
     * @param spanEnd Global offset where the span ends
     * @param lineStartX X coordinate where the line starts rendering
     * @param baselineY Y coordinate for the underline baseline
     * @param lineText The text content of the line
     * @param lineStart Global offset where this line starts
     * @param lineEnd Global offset where this line ends
     * @param color Underline color
     */
    public static void drawUnderlineForSpan(
            int spanStart, int spanEnd,
            int lineStartX, int baselineY,
            String lineText, int lineStart, int lineEnd,
            int color) {
        drawUnderlineForSpan(spanStart, spanEnd, lineStartX, baselineY,
                lineText, lineStart, lineEnd, color, null);
    }

    /**
     * Draw an underline for a simple span if it intersects the line.
     * When a ScriptLine is provided, uses style-aware width calculation.
     */
    public static void drawUnderlineForSpan(
            int spanStart, int spanEnd,
            int lineStartX, int baselineY,
            String lineText, int lineStart, int lineEnd,
            int color, ScriptLine line) {

        UnderlinePosition pos = calculateUnderlinePosition(
                spanStart, spanEnd, lineStartX, lineText, lineStart, lineEnd, line);

        if (pos != null && pos.isValid()) {
            drawCurlyUnderline(pos.x, baselineY, pos.width, color);
        }
    }

    /**
     * Draw a curly/wavy underline (like IDE error highlighting).
     * @param x Start X position
     * @param y Y position (bottom of text)
     * @param width Width of the underline
     * @param color Color in ARGB format (e.g., 0xFFFF5555 for red)
     */
    public static void drawCurlyUnderline(int x, int y, int width, int color) {
        if (width <= 0)
            return;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // If alpha is 0, assume full opacity
        if (a == 0)
            a = 1.0f;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(1.0f);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        // Wave parameters: 2 pixels amplitude, 4 pixels wavelength
        int waveHeight = 1;
        float waveLength = 4f;
        for (float i = -0.5f; i <= width - 1; i += 0.125f) {
            // Create a sine-like wave pattern
            double phase = (double) i / waveLength * Math.PI * 2;
            float yOffset = (float) (Math.sin(phase) * waveHeight) - 0.25f;
            GL11.glVertex2f(x + i + 2f, y + yOffset);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
