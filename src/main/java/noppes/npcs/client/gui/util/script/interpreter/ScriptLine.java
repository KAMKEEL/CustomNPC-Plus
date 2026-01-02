package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.interpreter.field.EnumConstantInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.ImportData;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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
    public final int globalStart;       // Start offset in the full document
    public final int globalEnd;         // End offset in the full document (exclusive)
    public final int lineIndex;         // 0-based line number

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
        for (Token t : tokens)
            if (globalPosition >= t.getGlobalStart() && globalPosition <= t.getGlobalEnd()) 
                return t;

        return null;
    }

    public Token getTokenAt(int globalPosition, Predicate<Token> condition) {
        for (Token t : tokens) 
            if (globalPosition >= t.getGlobalStart() && globalPosition <= t.getGlobalEnd() && condition.test(t))
                return t;
        
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
                if (mark.metadata != null) 
                   applyTokenMetadata(token, mark.metadata);
                
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

    public void applyTokenMetadata(Token token, Object metadata) {
        if (metadata instanceof TypeInfo) {
            token.setTypeInfo((TypeInfo) metadata);
        } else if (metadata instanceof MethodCallInfo) {
            MethodCallInfo callInfo = (MethodCallInfo) metadata;
            if (callInfo.isConstructor()) {
                token.setTypeInfo(callInfo.getReceiverType());
                token.setMethodInfo(callInfo.getResolvedMethod());
            }
            token.setMethodCallInfo(callInfo);
        } else if (metadata instanceof FieldInfo.ArgInfo) {
            FieldInfo.ArgInfo ctx = (FieldInfo.ArgInfo) metadata;
            token.setFieldInfo(ctx.fieldInfo);
            token.setMethodCallInfo(ctx.methodCallInfo);
        } else if (metadata instanceof FieldInfo) {
            token.setFieldInfo((FieldInfo) metadata);
        } else if (metadata instanceof MethodInfo) {
            token.setMethodInfo((MethodInfo) metadata);
        } else if (metadata instanceof ImportData) {
            token.setImportData((ImportData) metadata);
        } else if (metadata instanceof FieldAccessInfo) {
            FieldAccessInfo accessInfo = (FieldAccessInfo) metadata;
            token.setFieldAccessInfo(accessInfo);
            token.setFieldInfo(accessInfo.getResolvedField());
        } else if (metadata instanceof TokenErrorMessage) {
            token.setErrorMessage((TokenErrorMessage) metadata);
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
     * Supports bold (§l) and italic (§o) formatting for certain token types.
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
            

            // Append style codes (bold/italic) if applicable
            String stylePrefix = t.getStylePrefix();
            
            // Append the colored token with style
            builder.append(COLOR_CHAR)
                   .append(t.getColorCode())
                   .append(stylePrefix)
                   .append(t.getText())
                   .append(COLOR_CHAR)
                   .append('r'); // Reset all formatting (color + bold/italic)

            currentX += tokenWidth;
            lastIndex = tokenStart + t.getText().length();
        }

        // Append any remaining text after the last token
        if (lastIndex < text.length()) {
            builder.append(text.substring(lastIndex));
        }

        // Draw the text
        ClientProxy.Font.drawString(builder.toString(), x, y, defaultColor);
//        Minecraft.getMinecraft().fontRenderer.drawString(builder.toString(), x, y, defaultColor);


        // Draw underlines for validation errors (method calls and field accesses)
        drawErrorUnderlines(x, y + ClientProxy.Font.height() - 1);
    }
    /**
     * Draw this line with hex colors instead of Minecraft color codes.
     * More flexible but requires custom font rendering.
     * Also draws wavy underlines for tokens with errors.
     */
    public void drawStringHex(int x, int y) {
        // Build the complete text with all tokens and gaps, draw it as ONE string
        // to match the spacing behavior of drawString which draws everything at once
        StringBuilder fullText = new StringBuilder();
        int lastIndex = 0;
        
        // Build segments with token position info
        java.util.List<TextSegment> segments = new java.util.ArrayList<>();

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart;

            // Add any gap before this token
            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                String gap = text.substring(lastIndex, tokenStart);
                segments.add(new TextSegment(fullText.length(), gap, 0xFFFFFFFF, false));
                fullText.append(gap);
            }

            // Add the colored token
            String styledText = t.getStylePrefix() + t.getText();
            segments.add(new TextSegment(fullText.length(), styledText, t.getHexColor(), true));
            fullText.append(styledText);
            
            lastIndex = tokenStart + styledText.length();
        }

        // Add any remaining text after the last token
        if (lastIndex < text.length()) {
            String remaining = text.substring(lastIndex);
            segments.add(new TextSegment(fullText.length(), remaining, 0xFFFFFFFF, false));
            fullText.append(remaining);
        }

        // Draw each segment at the correct position
        // Calculate positions based on the full string to match drawString's spacing
        for (TextSegment seg : segments) {
            if (!seg.text.isEmpty()) {
                // Get the width of everything before this segment
                String prefix = fullText.substring(0, seg.startPos);
                int prefixWidth = ClientProxy.Font.width(prefix);
                int color = (seg.color & 0xFF000000) == 0 ? (0xFF000000 | seg.color) : seg.color;

                // Draw this segment at the correct position
                ClientProxy.Font.drawString(seg.text, x + prefixWidth, y, color);
                // Minecraft.getMinecraft().fontRenderer.drawString(seg.text, x + prefixWidth, y, color);
            }
        }
        
        drawErrorUnderlines(x, y + ClientProxy.Font.height() - 1);
    }

    /**
     * Draw underlines for all validation errors (method calls and field accesses) that intersect this line.
     * Delegates to ErrorUnderlineRenderer for the actual rendering logic.
     */
    private void drawErrorUnderlines(int lineStartX, int baselineY) {
        if (parent == null)
            return;

        ErrorUnderlineRenderer.drawErrorUnderlines(
                parent,
                lineStartX,
                baselineY,
                getText(),
                getGlobalStart(),
                getGlobalEnd()
        );
    }


    // Helper class to track text segments
    private static class TextSegment {
        final int startPos;
        final String text;
        final int color;
        final boolean isToken;
        
        TextSegment(int startPos, String text, int color, boolean isToken) {
            this.startPos = startPos;
            this.text = text;
            this.color = color;
            this.isToken = isToken;
        }
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
