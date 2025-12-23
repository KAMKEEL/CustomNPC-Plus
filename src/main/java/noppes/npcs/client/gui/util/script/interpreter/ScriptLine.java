package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;

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
                    } else if (mark.metadata instanceof FieldInfo) {
                        token.setFieldInfo((FieldInfo) mark.metadata);
                    } else if (mark.metadata instanceof MethodInfo) {
                        token.setMethodInfo((MethodInfo) mark.metadata);
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
     */
    public void drawString(int x, int y, int defaultColor) {
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart; // relative position in line
            
            // Append any text before this token (spaces, punctuation, etc.)
            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                builder.append(text, lastIndex, tokenStart);
            }

            // Append the colored token
            builder.append(COLOR_CHAR)
                   .append(t.getColorCode())
                   .append(t.getText())
                   .append(COLOR_CHAR)
                   .append('f'); // Reset to white

            lastIndex = tokenStart + t.getText().length();
        }

        // Append any remaining text after the last token
        if (lastIndex < text.length()) {
            builder.append(text.substring(lastIndex));
        }

        ClientProxy.Font.drawString(builder.toString(), x, y, defaultColor);
    }

    /**
     * Draw this line with hex colors instead of Minecraft color codes.
     * More flexible but requires custom font rendering.
     * 
     * @param x X position
     * @param y Y position  
     * @param renderer A functional interface for drawing colored text segments
     */
    public void drawStringHex(int x, int y, HexColorRenderer renderer) {
        int currentX = x;
        int lastIndex = 0;

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart;

            // Draw any text before this token in default color
            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                String gap = text.substring(lastIndex, tokenStart);
                currentX = renderer.draw(gap, currentX, y, 0xFFFFFF);
            }

            // Draw the colored token
            currentX = renderer.draw(t.getText(), currentX, y, t.getHexColor());

            lastIndex = tokenStart + t.getText().length();
        }

        // Draw any remaining text in default color
        if (lastIndex < text.length()) {
            renderer.draw(text.substring(lastIndex), currentX, y, 0xFFFFFF);
        }
    }

    /**
     * Functional interface for rendering text with hex colors.
     */
    @FunctionalInterface
    public interface HexColorRenderer {
        /**
         * Draw text at the specified position with the given color.
         * @return The X position after drawing (for continuation)
         */
        int draw(String text, int x, int y, int hexColor);
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
