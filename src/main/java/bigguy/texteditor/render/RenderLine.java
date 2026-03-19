package bigguy.texteditor.render;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.interpreter.token.Token;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.ImportData;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodCallInfo;
import noppes.npcs.client.gui.util.script.interpreter.js_parser.TypeParamInfo;
import noppes.npcs.client.gui.util.script.interpreter.ErrorUnderlineRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * The main text renderer for a single line of source code.
 * Replaces {@code ScriptLine} with a decoupled design: no back-reference to
 * {@code ScriptDocument}, no doubly-linked list navigation.
 *
 * <p>Rendering flows through two paths:
 * <ul>
 *   <li>{@link #drawStringHex(int, int)} — primary hex-color renderer
 *       using {@link ClientProxy.Font#drawString(String, int, int, int)}</li>
 *   <li>{@link #drawString(int, int, int)} — legacy Minecraft color-code renderer</li>
 * </ul>
 *
 * <p>Tokens are immutable after {@link #buildTokensFromMarks(List, String)} completes.
 * Error underlines are drawn via caller-supplied {@link DiagnosticRange} records
 * rather than reaching back into a document object.
 */
public class RenderLine {

    private static final char COLOR_CHAR = '\u00A7';

    private final String text;
    private final int globalStart;
    private final int globalEnd;
    private final int lineIndex;

    private final List<Token> tokens = new ArrayList<>();
    private final List<Integer> indentGuides = new ArrayList<>();

    /**
     * A lightweight descriptor for a single error underline region.
     * Provided by the orchestrator/RenderModel so that RenderLine never
     * queries a document object.
     */
    public static final class DiagnosticRange {
        /** Global byte offset where the error starts (inclusive). */
        public final int start;
        /** Global byte offset where the error ends (exclusive). */
        public final int end;
        /** ARGB color for the wavy underline. */
        public final int color;

        /**
         * @param start global start offset (inclusive)
         * @param end   global end offset (exclusive)
         * @param color ARGB underline color
         */
        public DiagnosticRange(int start, int end, int color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }
    }

    /**
     * @param text        the raw text content of this line (no trailing newline)
     * @param globalStart byte offset in the full document where this line begins
     * @param globalEnd   byte offset in the full document where this line ends (exclusive, includes newline position)
     * @param lineIndex   0-based line number
     */
    public RenderLine(String text, int globalStart, int globalEnd, int lineIndex) {
        this.text = text;
        this.globalStart = globalStart;
        this.globalEnd = globalEnd;
        this.lineIndex = lineIndex;
    }

    // ==================== GETTERS ====================

    /** Raw text content of this line (no trailing newline). */
    public String getText() { return text; }

    /** Byte offset in the full document where this line begins. */
    public int getGlobalStart() { return globalStart; }

    /** Byte offset in the full document where this line ends (exclusive). */
    public int getGlobalEnd() { return globalEnd; }

    /** 0-based line number. */
    public int getLineIndex() { return lineIndex; }

    /** Character count of the line text. */
    public int getLength() { return text.length(); }

    /** Unmodifiable view of the tokens on this line. */
    public List<Token> getTokens() { return Collections.unmodifiableList(tokens); }

    /** Unmodifiable view of indent guide column positions. */
    public List<Integer> getIndentGuides() { return Collections.unmodifiableList(indentGuides); }

    // ==================== TOKEN QUERIES ====================

    /**
     * Find the token that contains the given global position.
     *
     * @param globalPosition byte offset in the full document
     * @return the token at that position, or null if none
     */
    public Token getTokenAt(int globalPosition) {
        for (Token t : tokens) {
            if (globalPosition >= t.getGlobalStart() && globalPosition <= t.getGlobalEnd()) {
                return t;
            }
        }
        return null;
    }

    /**
     * Find the token at a global position that also satisfies a predicate.
     *
     * @param globalPosition byte offset in the full document
     * @param condition      additional filter
     * @return the matching token, or null
     */
    public Token getTokenAt(int globalPosition, Predicate<Token> condition) {
        for (Token t : tokens) {
            if (globalPosition >= t.getGlobalStart() && globalPosition <= t.getGlobalEnd()
                    && condition.test(t)) {
                return t;
            }
        }
        return null;
    }

    /** @return the first token on this line, or null if the line has no tokens */
    public Token getFirstToken() {
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    /** @return the last token on this line, or null if the line has no tokens */
    public Token getLastToken() {
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    // ==================== POSITION UTILITIES ====================

    /**
     * Check if a global position falls within this line's range.
     *
     * @param globalPosition byte offset in the full document
     * @return true if {@code globalStart <= globalPosition < globalEnd}
     */
    public boolean containsPosition(int globalPosition) {
        return globalPosition >= globalStart && globalPosition < globalEnd;
    }

    /**
     * Convert a global position to a local column offset within this line.
     *
     * @param globalPosition byte offset in the full document
     * @return clamped column in {@code [0, text.length()]}
     */
    public int toColumn(int globalPosition) {
        return Math.max(0, Math.min(globalPosition - globalStart, text.length()));
    }

    /**
     * Convert a local column offset to a global position.
     *
     * @param column local offset within this line
     * @return global byte offset
     */
    public int toGlobal(int column) {
        return globalStart + Math.max(0, Math.min(column, text.length()));
    }

    // ==================== TOKEN BUILDING ====================

    /**
     * Build tokens from a list of marks (highlight regions).
     * Fills gaps between marks with {@link TokenType#DEFAULT} tokens.
     * Attaches metadata from marks to the created tokens.
     *
     * <p>This is the decoupled replacement for
     * {@code ScriptLine.buildTokensFromMarks(marks, fullText, document)}.
     * No ScriptDocument parameter — removes the coupling.
     *
     * @param marks    list of highlight marks that overlap this line, sorted by start position
     * @param fullText the complete document text
     */
    public void buildTokensFromMarks(List<Mark> marks, String fullText) {
        tokens.clear();
        int cursor = globalStart;

        /*
         * globalEnd includes the newline character position
         * (lineEnd = lineStart + text.length() + 1), but the line's text field
         * does NOT include the newline. Clamp all token boundaries to the
         * displayable text end so that '\n' never leaks into rendered token text.
         */
        int displayEnd = Math.min(globalStart + text.length(), fullText.length());

        for (Mark mark : marks) {
            if (mark.end <= globalStart || mark.start >= displayEnd) {
                continue;
            }

            int tokenStart = Math.max(mark.start, globalStart);
            int tokenEnd = Math.min(mark.end, displayEnd);

            tokenStart = Math.max(0, Math.min(tokenStart, fullText.length()));
            tokenEnd = Math.max(0, Math.min(tokenEnd, fullText.length()));

            if (cursor < tokenStart) {
                int gapEnd = Math.min(tokenStart, fullText.length());
                String gapText = fullText.substring(cursor, gapEnd);
                addToken(Token.defaultToken(gapText, cursor, gapEnd));
            }

            if (tokenStart < tokenEnd) {
                String tokenText = fullText.substring(tokenStart, tokenEnd);
                Token token = new Token(tokenText, tokenStart, tokenEnd, mark.type);

                if (mark.metadata != null) {
                    applyTokenMetadata(token, mark.metadata);
                }

                addToken(token);
            }

            cursor = tokenEnd;
        }

        if (cursor < displayEnd) {
            String trailingText = fullText.substring(cursor, displayEnd);
            addToken(Token.defaultToken(trailingText, cursor, displayEnd));
        }
    }

    /**
     * Apply analysis metadata to a token based on the metadata's runtime type.
     * Handles TypeInfo, TypeParamInfo, MethodCallInfo, FieldInfo.ArgInfo,
     * FieldInfo, MethodInfo, ImportData, FieldAccessInfo, and TokenErrorMessage.
     *
     * @param token    the token to enrich
     * @param metadata the analysis artifact
     */
    public void applyTokenMetadata(Token token, Object metadata) {
        if (metadata instanceof TypeInfo) {
            token.setTypeInfo((TypeInfo) metadata);
        } else if (metadata instanceof TypeParamInfo) {
            TypeParamInfo typeParam = (TypeParamInfo) metadata;
            token.setTypeInfo(TypeInfo.typeParameter(typeParam.getName(), typeParam));
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

    /**
     * Add a token to this line and wire up intra-line navigation links.
     */
    private void addToken(Token token) {
        if (!tokens.isEmpty()) {
            Token last = tokens.get(tokens.size() - 1);
            last.setNext(token);
            token.setPrev(last);
        }
        token.setParentLine(null);
        tokens.add(token);
    }

    // ==================== INDENT GUIDES ====================

    /** Remove all indent guides from this line. */
    public void clearIndentGuides() {
        indentGuides.clear();
    }

    /**
     * Add an indent guide at the specified column if not already present.
     *
     * @param column the column position for the guide
     */
    public void addIndentGuide(int column) {
        if (!indentGuides.contains(column)) {
            indentGuides.add(column);
        }
    }

    // ==================== RENDERING ====================

    /**
     * Compute the rendered pixel width of a substring of this line, accounting for
     * bold/italic token styles. Characters covered by bold or italic tokens are
     * measured with the appropriate font style via
     * {@link ClientProxy.Font#width(String, int)}, producing the same widths as the
     * actual rendered output.
     *
     * <p>Critical for cursor positioning and error underline alignment.
     *
     * @param localStart start index within the line text (inclusive)
     * @param localEnd   end index within the line text (exclusive)
     * @return pixel width matching the rendered output
     */
    public int getRenderedWidth(int localStart, int localEnd) {
        localStart = Math.max(0, Math.min(localStart, text.length()));
        localEnd = Math.max(localStart, Math.min(localEnd, text.length()));
        if (localStart >= localEnd) return 0;

        int width = 0;
        int cursor = localStart;

        for (Token t : tokens) {
            if (cursor >= localEnd) break;

            int tokenLocalStart = t.getGlobalStart() - globalStart;
            int tokenLocalEnd = tokenLocalStart + t.getText().length();

            // Gap before this token (rendered as plain text)
            if (cursor < tokenLocalStart) {
                int gapEnd = Math.min(tokenLocalStart, localEnd);
                if (gapEnd > cursor) {
                    width += ClientProxy.Font.width(text.substring(cursor, gapEnd));
                    cursor = gapEnd;
                }
            }

            if (cursor >= localEnd) break;

            // Token overlap with [localStart, localEnd)
            int overlapStart = Math.max(cursor, tokenLocalStart);
            int overlapEnd = Math.min(tokenLocalEnd, localEnd);
            if (overlapEnd > overlapStart) {
                String substr = text.substring(overlapStart, overlapEnd);
                width += ClientProxy.Font.width(substr, t.getFontStyle());
                cursor = overlapEnd;
            }
        }

        // Trailing text after last token (plain)
        if (cursor < localEnd) {
            width += ClientProxy.Font.width(text.substring(cursor, localEnd));
        }

        return width;
    }

    /**
     * Draw this line with hex colors. THE PRIMARY RENDERER.
     * Builds {@link RenderSegment}s from the token list, then renders each one
     * at the correct pixel offset via
     * {@link ClientProxy.Font#drawString(String, int, int, int)}.
     *
     * <p>Does NOT draw error underlines — call
     * {@link #drawErrorUnderlines(int, int, List)} separately with
     * caller-supplied diagnostics.
     *
     * @param x left pixel coordinate
     * @param y top pixel coordinate
     */
    public void drawStringHex(int x, int y) {
        int lastIndex = 0;
        List<RenderSegment> segments = new ArrayList<>();

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart;

            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                String gap = text.substring(lastIndex, tokenStart);
                segments.add(new RenderSegment(lastIndex, gap, 0xFFFFFFFF, false));
            }

            String styledText = t.getStylePrefix() + t.getText();
            segments.add(new RenderSegment(tokenStart, styledText, t.getHexColor(), true));

            lastIndex = tokenStart + t.getText().length();
        }

        if (lastIndex < text.length()) {
            String remaining = text.substring(lastIndex);
            segments.add(new RenderSegment(lastIndex, remaining, 0xFFFFFFFF, false));
        }

        for (RenderSegment seg : segments) {
            if (!seg.text.isEmpty()) {
                int prefixWidth = getRenderedWidth(0, seg.startPosition);
                ClientProxy.Font.drawString(seg.text, x + prefixWidth, y, seg.hexColor);
            }
        }
    }

    /**
     * Draw this line with legacy Minecraft color codes.
     * Backup rendering path that uses §-prefixed color codes.
     * Also draws error underlines via the supplied diagnostic list.
     *
     * @param x            left pixel coordinate
     * @param y            top pixel coordinate
     * @param defaultColor fallback color for non-token text
     */
    public void drawString(int x, int y, int defaultColor) {
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;

        for (Token t : tokens) {
            int tokenStart = t.getGlobalStart() - globalStart;

            if (tokenStart > lastIndex && tokenStart <= text.length()) {
                String gapText = text.substring(lastIndex, tokenStart);
                builder.append(gapText);
            }

            String stylePrefix = t.getStylePrefix();
            builder.append(COLOR_CHAR)
                   .append(t.getColorCode())
                   .append(stylePrefix)
                   .append(t.getText())
                   .append(COLOR_CHAR)
                   .append('r');

            lastIndex = tokenStart + t.getText().length();
        }

        if (lastIndex < text.length()) {
            builder.append(text.substring(lastIndex));
        }

        ClientProxy.Font.drawString(builder.toString(), x, y, defaultColor);
    }

    /**
     * Draw wavy error underlines for all diagnostic ranges that intersect this line.
     * Replaces the old {@code ScriptLine.drawErrorUnderlines()} that reached
     * into {@code ScriptDocument} — this version receives diagnostics as arguments.
     *
     * @param lineStartX left pixel coordinate where the line starts rendering
     * @param baselineY  Y coordinate for the underline (typically {@code y + fontHeight - 1})
     * @param errors     diagnostic ranges to underline; may be empty but not null
     */
    public void drawErrorUnderlines(int lineStartX, int baselineY, List<DiagnosticRange> errors) {
        for (DiagnosticRange diag : errors) {
            ErrorUnderlineRenderer.drawUnderlineForSpan(
                    diag.start, diag.end,
                    lineStartX, baselineY,
                    text, globalStart, globalEnd,
                    diag.color
            );
        }
    }

    @Override
    public String toString() {
        return "RenderLine{" + lineIndex + ": '" + text + "' [" + globalStart + "-" + globalEnd
                + "], " + tokens.size() + " tokens}";
    }
}
