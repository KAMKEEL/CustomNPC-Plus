package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for indentation-related operations.
 */
public class IndentHelper {
    
    public static final int TAB_SIZE = 4;
    
    /**
     * Get leading whitespace count of a line
     */
    public static int getLineIndent(String lineText) {
        int leading = 0;
        for (int i = 0; i < lineText.length(); i++) {
            char c = lineText.charAt(i);
            if (c == ' ' || c == '\t') leading++;
            else break;
        }
        return leading;
    }
    
    /**
     * Extract leading whitespace as string
     */
    public static String getIndentString(String lineText) {
        int indent = getLineIndent(lineText);
        return lineText.substring(0, indent);
    }
    
    /**
     * Create a string of n spaces
     */
    public static String spaces(int n) {
        if (n <= 0) return "";
        char[] arr = new char[n];
        Arrays.fill(arr, ' ');
        return new String(arr);
    }
    
    /**
     * Calculate expected indent for a line based on context
     */
    public static int getExpectedIndent(LineData currentLine, List<LineData> allLines) {
        if (currentLine == null || allLines == null) return 0;
        
        int idx = allLines.indexOf(currentLine);
        if (idx <= 0) return 0;
        
        // Find previous non-empty line
        LineData prevLine = null;
        for (int i = idx - 1; i >= 0; i--) {
            LineData line = allLines.get(i);
            if (line.text.trim().length() > 0) {
                prevLine = line;
                break;
            }
        }
        
        if (prevLine == null) return 0;
        
        int prevIndent = getLineIndent(prevLine.text);
        String prevTrimmed = prevLine.text.trim();
        String currTrimmed = currentLine.text.trim();
        
        // If current line starts with }, expected is one level back
        if (currTrimmed.startsWith("}")) {
            return Math.max(0, prevIndent - TAB_SIZE);
        }
        
        // If previous line ends with {, expected is one level forward
        if (prevTrimmed.endsWith("{")) {
            return prevIndent + TAB_SIZE;
        }
        
        return prevIndent;
    }
    
    /**
     * Calculate auto-indent for Enter key
     */
    public static String getAutoIndentForEnter(String lineText, int cursorInLine) {
        int leading = 0;
        while (leading < lineText.length() && 
               (lineText.charAt(leading) == ' ' || lineText.charAt(leading) == '\t')) {
            leading++;
        }
        
        String baseIndent = lineText.substring(0, leading);
        int relativeCursor = Math.max(0, Math.min(cursorInLine, lineText.length()));
        String beforeCursor = lineText.substring(0, relativeCursor);
        
        // Find last non-whitespace before cursor
        int lastNonWs = -1;
        for (int i = beforeCursor.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(beforeCursor.charAt(i))) {
                lastNonWs = i;
                break;
            }
        }
        
        // Add extra indent if last char is {
        // Only add extra indent if cursor is actually after the opening brace (not just in trailing whitespace)
        // Check that the last non-whitespace is at or after the line's indent position
        boolean opensBlock = lastNonWs >= 0 && lastNonWs >= leading && beforeCursor.charAt(lastNonWs) == '{';
        if (opensBlock) {
            return baseIndent + spaces(TAB_SIZE);
        }
        
        return baseIndent;
    }
    
    /**
     * Calculate target indent for Tab key
     */
    public static int calculateTabTargetIndent(int currentIndent, int cursorColumn, boolean atTextStart) {
        if (atTextStart) {
            // At text start, move to next tab stop
            return ((currentIndent / TAB_SIZE) + 1) * TAB_SIZE;
        } else {
            // Inside indent, snap to nearest tab stop
            int remainder = currentIndent % TAB_SIZE;
            if (remainder == 0) {
                return currentIndent + TAB_SIZE;
            } else {
                int down = currentIndent - remainder;
                int up = currentIndent + (TAB_SIZE - remainder);
                int distDown = remainder;
                int distUp = TAB_SIZE - remainder;
                if (distUp <= distDown) return up;
                else return down;
            }
        }
    }
    
    /**
     * Calculate target indent for Shift+Tab
     */
    public static int calculateShiftTabTargetIndent(int currentIndent) {
        return Math.max(0, ((currentIndent - 1) / TAB_SIZE) * TAB_SIZE);
    }
    
    /**
     * Format entire text with proper indentation
     */
    public static FormatResult formatText(String text, int cursorPosition) {
        String[] linesArr = text.split("\n", -1);
        StringBuilder out = new StringBuilder(text.length() + 32);
        
        // Track cursor position in formatted output
        int cursorLine = -1;
        int cursorColInContent = 0;
        int lineStartPos = 0;
        
        // Find cursor's line and column
        for (int li = 0; li < linesArr.length; li++) {
            String line = linesArr[li];
            int lineEndPos = lineStartPos + line.length();
            
            if (cursorPosition >= lineStartPos && cursorPosition <= lineEndPos) {
                cursorLine = li;
                int leadingSpaces = line.length() - line.replaceAll("^[ \\t]+", "").length();
                cursorColInContent = Math.max(0, cursorPosition - lineStartPos - leadingSpaces);
                break;
            }
            lineStartPos = lineEndPos + 1;
        }
        
        boolean inString = false;
        boolean escape = false;
        boolean inBlockComment = false;
        int depth = 0;
        int newCursorPos = 0;
        
        for (int li = 0; li < linesArr.length; li++) {
            String line = linesArr[li];
            String trimmedLeading = line.replaceAll("^[ \\t]+", "");
            
            int opens = 0, closes = 0;
            boolean startsWithClose = false;
            
            // Parse line for braces
            for (int idx = 0; idx < line.length(); idx++) {
                char c = line.charAt(idx);
                char next = idx + 1 < line.length() ? line.charAt(idx + 1) : 0;
                
                if (inString) {
                    if (escape) { escape = false; }
                    else if (c == '\\') { escape = true; }
                    else if (c == '"') { inString = false; }
                    continue;
                }
                
                if (inBlockComment) {
                    if (c == '*' && next == '/') { inBlockComment = false; idx++; }
                    continue;
                }
                
                if (c == '/' && next == '/') break;
                if (c == '/' && next == '*') { inBlockComment = true; idx++; continue; }
                if (c == '"') { inString = true; escape = false; continue; }
                
                if (c == '{') {
                    opens++;
                } else if (c == '}') {
                    closes++;
                    if (!startsWithClose && line.substring(0, idx).trim().isEmpty()) {
                        startsWithClose = true;
                    }
                }
            }
            
            int indentLevel = depth;
            if (startsWithClose) indentLevel = Math.max(0, indentLevel - 1);
            int targetIndent = indentLevel * TAB_SIZE;
            
            if (li == cursorLine) {
                newCursorPos = out.length() + targetIndent + Math.min(cursorColInContent, trimmedLeading.length());
            }
            
            out.append(spaces(targetIndent)).append(trimmedLeading);
            if (li < linesArr.length - 1) out.append('\n');
            
            depth = Math.max(0, depth + opens - closes);
        }
        
        return new FormatResult(out.toString(), newCursorPos);
    }
    
    /**
     * Result of text formatting
     */
    public static class FormatResult {
        public final String text;
        public final int cursorPosition;
        
        public FormatResult(String text, int cursorPosition) {
            this.text = text;
            this.cursorPosition = cursorPosition;
        }
    }
}