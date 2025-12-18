package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles bracket matching, brace span computation, and indent guide generation.
 */
public class BracketMatcher {
    
    /**
     * Find matching bracket at cursor position.
     * @return int[2] with {startPos, endPos} or null if no bracket found
     */
    public static int[] findBracketSpanAt(String text, int pos) {
        if (text == null || text.isEmpty()) return null;
        
        // Try at current position
        if (pos >= 0 && pos < text.length()) {
            char c = text.charAt(pos);
            int found = 0;
            if (c == '{') found = findClosingBracket(text.substring(pos), '{', '}');
            else if (c == '[') found = findClosingBracket(text.substring(pos), '[', ']');
            else if (c == '(') found = findClosingBracket(text.substring(pos), '(', ')');
            else if (c == '}') found = findOpeningBracket(text.substring(0, pos + 1), '{', '}');
            else if (c == ']') found = findOpeningBracket(text.substring(0, pos + 1), '[', ']');
            else if (c == ')') found = findOpeningBracket(text.substring(0, pos + 1), '(', ')');
            if (found != 0) return new int[]{pos, pos + found};
        }

        // Scan backwards, skipping whitespace on same line
        if (pos > 0) {
            int scan = pos - 1;
            while (scan >= 0) {
                char sc = text.charAt(scan);
                if (sc == ' ' || sc == '\t') { scan--; continue; }
                if (sc == '\n') break; // Don't cross line boundary

                int found2 = 0;
                if (sc == '{') found2 = findClosingBracket(text.substring(scan), '{', '}');
                else if (sc == '[') found2 = findClosingBracket(text.substring(scan), '[', ']');
                else if (sc == '(') found2 = findClosingBracket(text.substring(scan), '(', ')');
                else if (sc == '}') found2 = findOpeningBracket(text.substring(0, scan + 1), '{', '}');
                else if (sc == ']') found2 = findOpeningBracket(text.substring(0, scan + 1), '[', ']');
                else if (sc == ')') found2 = findOpeningBracket(text.substring(0, scan + 1), '(', ')');

                if (found2 != 0) return new int[]{scan, scan + found2};
                break; // Not a bracket; stop
            }
        }
        return null;
    }

    /**
     * Find closing bracket, returns offset from start or 0 if not found
     */
    public static int findClosingBracket(String str, char open, char close) {
        int depth = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return 0;
    }

    /**
     * Find opening bracket, returns negative offset from end or 0 if not found
     */
    public static int findOpeningBracket(String str, char open, char close) {
        int depth = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (c == close) depth++;
            else if (c == open) {
                depth--;
                if (depth == 0) return i - str.length() + 1;
            }
        }
        return 0;
    }

    /**
     * Compute brace spans for indent guides.
     * Returns list of int[4]: {originalDepth, openLineIdx, closeLineIdx, adjustedDepth}
     */
    public static List<int[]> computeBraceSpans(String text, List<LineData> lines) {
        List<int[]> spans = new ArrayList<>();
        if (text == null || text.isEmpty() || lines == null || lines.isEmpty())
            return spans;

        int lineIdx = 0;
        int lineEnd = lines.get(0).end;
        List<Integer> openStack = new ArrayList<>();

        boolean inLineComment = false;
        boolean inBlockComment = false;
        boolean inString = false;
        boolean escape = false;
        char stringDelimiter = 0;

        for (int pos = 0; pos < text.length(); pos++) {
            // Advance line index
            while (lineIdx < lines.size() - 1 && pos >= lineEnd) {
                lineIdx++;
                lineEnd = lines.get(lineIdx).end;
            }

            char c = text.charAt(pos);
            char next = pos + 1 < text.length() ? text.charAt(pos + 1) : 0;

            // String handling
            if (inString) {
                if (escape) { escape = false; }
                else if (c == '\\') { escape = true; }
                else if (c == stringDelimiter) { inString = false; }
                else if (c == '\n') { inString = false; } // Unterminated string
                continue;
            }

            // Block comment handling
            if (inBlockComment) {
                if (c == '*' && next == '/') { inBlockComment = false; pos++; }
                continue;
            }

            // Line comment handling
            if (inLineComment) {
                if (c == '\n') { inLineComment = false; }
                continue;
            }

            // Start of comments
            if (c == '/' && next == '/') { inLineComment = true; pos++; continue; }
            if (c == '/' && next == '*') { inBlockComment = true; pos++; continue; }
            
            // Start of string
            if (c == '"' || c == '\'') {
                inString = true;
                stringDelimiter = c;
                escape = false;
                continue;
            }

            // Brace matching
            if (c == '{') {
                openStack.add(lineIdx);
            } else if (c == '}') {
                if (!openStack.isEmpty()) {
                    int openLine = openStack.remove(openStack.size() - 1);
                    int spanDepth = openStack.size() + 1;
                    spans.add(new int[]{spanDepth, openLine, lineIdx});
                }
            }
        }

        // Normalize depths for unmatched braces
        if (!openStack.isEmpty()) {
            int baseline = openStack.size();
            List<int[]> adjusted = new ArrayList<>(spans.size());
            for (int[] span : spans) {
                int adjustedDepth = Math.max(1, span[0] - baseline);
                adjusted.add(new int[]{span[0], span[1], span[2], adjustedDepth});
            }
            return adjusted;
        } else {
            for (int i = 0; i < spans.size(); i++) {
                int[] span = spans.get(i);
                spans.set(i, new int[]{span[0], span[1], span[2], span[0]});
            }
        }
        return spans;
    }

    /**
     * Find which brace span should be highlighted based on bracket at position
     * @return int[2] {openLine, closeLine} or null
     */
    public static int[] findHighlightedSpan(int bracketPos, List<LineData> lines, List<int[]> spans) {
        if (bracketPos < 0 || lines == null || spans == null) return null;
        
        // Find line containing bracket
        int bracketLineIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            LineData ld = lines.get(i);
            if (bracketPos >= ld.start && bracketPos < ld.end) {
                bracketLineIdx = i;
                break;
            }
        }
        
        if (bracketLineIdx >= 0) {
            for (int[] span : spans) {
                int openLine = span[1];
                int closeLine = span[2];
                if (bracketLineIdx >= openLine && bracketLineIdx <= closeLine) {
                    return new int[]{openLine, closeLine};
                }
            }
        }
        return null;
    }
    
    /**
     * Check if a brace at given position has a matching close with same indent
     */
    public static boolean hasMatchingCloseWithSameIndent(String text, int bracePos, List<LineData> lines, int currentIndent) {
        if (text == null || lines == null || bracePos < 0) return false;
        
        // Find line containing the brace
        int openLineIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            LineData ld = lines.get(i);
            if (bracePos >= ld.start && bracePos < ld.end) {
                openLineIdx = i;
                break;
            }
        }
        
        if (openLineIdx < 0) return false;
        
        List<int[]> spans = computeBraceSpans(text, lines);
        for (int[] span : spans) {
            if (span[1] == openLineIdx) {
                int closeLineIdx = span[2];
                if (closeLineIdx < lines.size()) {
                    String closeLine = lines.get(closeLineIdx).text;
                    int closeIndent = getLineIndent(closeLine);
                    return closeIndent == currentIndent;
                }
            }
        }
        return false;
    }
    
    /**
     * Get leading whitespace count of a line
     */
    private static int getLineIndent(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ' || c == '\t') indent++;
            else break;
        }
        return indent;
    }
}