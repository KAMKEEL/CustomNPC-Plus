package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single line of source code with its tokens.
 * Handles token management, navigation, and rendering.
 * 
 * A ScriptLine is responsible for:
 * - Storing tokens that fall within this line's character range
 * - Managing token navigation (prev/next within line)
 * - Rendering the line with proper syntax highlighting
 * - Computing indent guides for this line
 */
public class ScriptLine {

    private static final char COLOR_CHAR = '\u00A7';

    private final String text;           // The actual text of this line
    private final int globalStart;       // Start offset in the full document
    private final int globalEnd;         // End offset in the full document (exclusive)
    private final int lineIndex;         // 0-based line number

    private final List<Token> tokens = new ArrayList<>();
    private final List<Integer> indentGuides = new ArrayList<>(); // Column positions for indent guides

    // Navigation
    private ScriptLine prev;
    private ScriptLine next;
    private ScriptDocument parent;

    public ScriptLine(String text, int globalStart, int globalEnd, int lineIndex) {
        this.text = text;
        this.globalStart = globalStart;
        this.globalEnd = globalEnd;
        this.lineIndex = lineIndex;
    }

    // ==================== GETTERS ====================

    public String getText() { return text; }
    public int getGlobalStart() { return globalStart; }
    public int getGlobalEnd() { return globalEnd; }
    public int getLineIndex() { return lineIndex; }
    public int getLength() { return text.length(); }
    public List<Token> getTokens() { return Collections.unmodifiableList(tokens); }
    public List<Integer> getIndentGuides() { return Collections.unmodifiableList(indentGuides); }
    public ScriptDocument getParent() { return parent; }

    void setParent(ScriptDocument parent) { this.parent = parent; }
    void setPrev(ScriptLine prev) { this.prev = prev; }
    void setNext(ScriptLine next) { this.next = next; }

    // ==================== NAVIGATION ====================

    public ScriptLine prev() { return prev; }
    public ScriptLine next() { return next; }

    public Token getFirstToken() {
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public Token getLastToken() {
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    public Token getTokenAt(int globalPosition) {
        for (Token t : tokens) {
            if (globalPosition >= t.getGlobalStart() && globalPosition < t.getGlobalEnd()) {
                return t;
            }
        }
        return null;
    }

    // ==================== TOKEN MANAGEMENT ====================

    /**
     * Clear all tokens from this line.
     */
    public void clearTokens() {
        tokens.clear();
    }

    /**
     * Add a token to this line and set up navigation links.
     */
    public void addToken(Token token) {
        if (!tokens.isEmpty()) {
            Token last = tokens.get(tokens.size() - 1);
            last.setNext(token);
            token.setPrev(last);
        }
        token.setParentLine(this);
        tokens.add(token);
    }

    /**
     * Build tokens from a list of marks (highlight regions).
     * Fills gaps between marks with DEFAULT tokens.
     * Attaches metadata from marks to the created tokens.
     * 
     * @param marks List of highlight marks that overlap this line
     * @param fullText The complete document text
     * @param document The parent ScriptDocument (for context lookups)
     */
    public void buildTokensFromMarks(List<Mark> marks, String fullText, ScriptDocument document) {
        clearTokens();
        int cursor = globalStart;

        for (Mark mark : marks) {
            // Skip marks that don't overlap this line
            if (mark.end <= globalStart || mark.start >= globalEnd) {
                continue;
            }

            // Clamp mark to line boundaries
            int tokenStart = Math.max(mark.start, globalStart);
            int tokenEnd = Math.min(mark.end, globalEnd);

            // Validate bounds
            tokenStart = Math.max(0, Math.min(tokenStart, fullText.length()));
            tokenEnd = Math.max(0, Math.min(tokenEnd, fullText.length()));

            // Add default token for any gap before this mark
            if (cursor < tokenStart) {
                int gapEnd = Math.min(tokenStart, fullText.length());
                String gapText = fullText.substring(cursor, gapEnd);
                addToken(Token.defaultToken(gapText, cursor, gapEnd));
            }

            // Add the marked token with metadata
            if (tokenStart < tokenEnd) {
                String tokenText = fullText.substring(tokenStart, tokenEnd);
                Token token = new Token(tokenText, tokenStart, tokenEnd, mark.type);
                
                // Attach metadata based on type
                if (mark.metadata != null) {
                    if (mark.metadata instanceof TypeInfo) {
                        token.setTypeInfo((TypeInfo) mark.metadata);
                    } else if (mark.metadata instanceof FieldInfo.ArgInfo) {
                        FieldInfo.ArgInfo ctx = (FieldInfo.ArgInfo) mark.metadata;
                        token.setFieldInfo(ctx.fieldInfo);
                        token.setMethodCallInfo(ctx.methodCallInfo);
                    } else if (mark.metadata instanceof FieldInfo) {
                        token.setFieldInfo((FieldInfo) mark.metadata);
                    } else if (mark.metadata instanceof MethodInfo) {
                        token.setMethodInfo((MethodInfo) mark.metadata);
                    } else if (mark.metadata instanceof MethodCallInfo) {
                        MethodCallInfo callInfo = (MethodCallInfo) mark.metadata;
                        token.setMethodCallInfo(callInfo);
                        if (callInfo.getErrorType() != MethodCallInfo.ErrorType.NONE) {
                            // For other errors (arg count, static access), underline the method name
                            token.setUnderline(true, 0xFF5555); // Red wavy underline
                        }
                    } else if (mark.metadata instanceof ImportData) {
                        token.setImportData((ImportData) mark.metadata);
                    }
                }
                
                addToken(token);
            }

            cursor = tokenEnd;
        }

        // Add trailing default token if needed
        if (cursor < globalEnd) {
            int end = Math.min(globalEnd, fullText.length());
            if (cursor < end) {
                String trailingText = fullText.substring(cursor, end);
                addToken(Token.defaultToken(trailingText, cursor, end));
            }
        }
    }

    // ==================== INDENT GUIDES ====================

    /**
     * Clear indent guides for this line.
     */
    public void clearIndentGuides() {
        indentGuides.clear();
    }

    /**
     * Add an indent guide at the specified column.
     */
    public void addIndentGuide(int column) {
        if (!indentGuides.contains(column)) {
            indentGuides.add(column);
        }
    }

    // ==================== RENDERING ====================

    /**
     * Draw this line with syntax highlighting using Minecraft color codes.
     * Compatible with the existing rendering system.
     * Also draws curly underlines for tokens with errors (method call validation failures).
     */
    public void drawString(int x, int y, int defaultColor) {
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;

        // Track positions for underlines
        int currentX = x;

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart; // relative position in line
            int tokenWidth = ClientProxy.Font.width(t.getText());

            // Calculate gap width before this token
            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                String gapText = text.substring(lastIndex, tokenStart);
                builder.append(gapText);
                currentX += ClientProxy.Font.width(gapText);
            }

            // Track underline position if token has one
            t.drawUnderline(currentX, y + ClientProxy.Font.height() - 1);
            

            // Append the colored token
            builder.append(COLOR_CHAR)
                   .append(t.getColorCode())
                   .append(t.getText())
                   .append(COLOR_CHAR)
                   .append('f'); // Reset to white

            currentX += tokenWidth;
            lastIndex = tokenStart + t.getText().length();
        }

        // Append any remaining text after the last token
        if (lastIndex < text.length()) {
            builder.append(text.substring(lastIndex));
        }

        // Draw the text
        ClientProxy.Font.drawString(builder.toString(), x, y, defaultColor);
    }

    /**
     * Draw a curly/wavy underline (like IDE error highlighting).
     * @param x Start X position
     * @param y Y position (bottom of text)
     * @param width Width of the underline
     * @param color Color in ARGB format (e.g., 0xFFFF5555 for red)
     */
    protected static void drawCurlyUnderline(int x, int y, int width, int color) {
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
            GL11.glVertex2f(x + i + 3f, y + yOffset);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    /**
     * Draw this line with hex colors instead of Minecraft color codes.
     * More flexible but requires custom font rendering.
     * Also draws wavy underlines for tokens with errors.
     * 
     * @param x X position
     * @param y Y position  
     * @param renderer A renderer for drawing colored text and error underlines
     */
    public void drawStringHex(int x, int y, HexColorRenderer renderer) {
        int currentX = x;
        int lastIndex = 0;

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart;

            // Draw any text before this token in default color
            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                String gap = text.substring(lastIndex, tokenStart);
                currentX = renderer.draw(gap, currentX, y, 0xFFFFFF, false, 0);
            }

            // Draw the colored token (with underline if flagged)
            currentX = renderer.draw(t.getText(), currentX, y, t.getHexColor(), t.hasUnderline(),
                    t.getUnderlineColor());

            lastIndex = tokenStart + t.getText().length();
        }

        // Draw any remaining text in default color
        if (lastIndex < text.length()) {
            renderer.draw(text.substring(lastIndex), currentX, y, 0xFFFFFF, false, 0);
        }
    }

    /**
     * Functional interface for rendering text with hex colors and optional underlines.
     */
    @FunctionalInterface
    public interface HexColorRenderer {
        /**
         * Draw text at the specified position with the given color.
         * @param text The text to draw
         * @param x X position
         * @param y Y position
         * @param hexColor The text color in hex format
         * @param underline Whether to draw an underline (wavy for errors)
         * @param underlineColor The underline color (if underline is true)
         * @return The X position after drawing (for continuation)
         */
        int draw(String text, int x, int y, int hexColor, boolean underline, int underlineColor);
    }

    /**
     * Creates a default HexColorRenderer implementation using ClientProxy.Font.
     * This renderer draws text with hex colors and curly underlines for errors.
     * @return A HexColorRenderer that can be passed to drawStringHex
     */
    public static HexColorRenderer createDefaultHexRenderer() {
        return (text, x, y, hexColor, underline, underlineColor) -> {
            // Draw the text with hex color
            // Minecraft's font renderer uses ARGB, so add full alpha if not present
            int color = (hexColor & 0xFF000000) == 0 ? (0xFF000000 | hexColor) : hexColor;
            ClientProxy.Font.drawString(text, x, y, color);
            int textWidth = ClientProxy.Font.width(text);

            // Draw curly underline if needed
            if (underline && textWidth > 0) {
                int ulColor = (underlineColor & 0xFF000000) == 0 ? (0xFF000000 | underlineColor) : underlineColor;

                float a = ((ulColor >> 24) & 0xFF) / 255f;
                float r = ((ulColor >> 16) & 0xFF) / 255f;
                float g = ((ulColor >> 8) & 0xFF) / 255f;
                float b = (ulColor & 0xFF) / 255f;

                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(r, g, b, a);
                GL11.glLineWidth(1.0f);

                int underlineY = y + ClientProxy.Font.height() - 1;
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= textWidth; i++) {
                    double phase = (double) i / 4 * Math.PI * 2;
                    int yOffset = (int) (Math.sin(phase) * 2);
                    GL11.glVertex2f(x + i, underlineY + yOffset);
                }
                GL11.glEnd();

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }

            return x + textWidth;
        };
    }

    // ==================== UTILITIES ====================

    /**
     * Check if a global position falls within this line.
     */
    public boolean containsPosition(int globalPosition) {
        return globalPosition >= globalStart && globalPosition < globalEnd;
    }

    /**
     * Convert a global position to a column (local offset) within this line.
     */
    public int toColumn(int globalPosition) {
        return Math.max(0, Math.min(globalPosition - globalStart, text.length()));
    }

    /**
     * Convert a column (local offset) to a global position.
     */
    public int toGlobal(int column) {
        return globalStart + Math.max(0, Math.min(column, text.length()));
    }

    @Override
    public String toString() {
        return "ScriptLine{" + lineIndex + ": '" + text + "' [" + globalStart + "-" + globalEnd + "], " + tokens.size() + " tokens}";
    }

    // ==================== MARK CLASS (for token building) ====================

    /**
     * A simple mark representing a highlighted region.
     * Used during token building phase.
     * Can optionally carry metadata (TypeInfo, FieldInfo, MethodInfo, ImportData).
     */
    public static class Mark {
        public final int start;
        public final int end;
        public final TokenType type;
        public final Object metadata;

        public Mark(int start, int end, TokenType type) {
            this.start = start;
            this.end = end;
            this.type = type;
            this.metadata = null;
        }

        public Mark(int start, int end, TokenType type, Object metadata) {
            this.start = start;
            this.end = end;
            this.type = type;
            this.metadata = metadata;
        }

        @Override
        public String toString() {
            return "Mark{" + type + " [" + start + "-" + end + "]" + (metadata != null ? " " + metadata.getClass().getSimpleName() : "") + "}";
        }
    }
}
