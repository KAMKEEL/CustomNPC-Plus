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
     * Format entire text with proper indentation and operator spacing
     */
    public static FormatResult formatText(String text, int cursorPosition) {
        return formatText(text, cursorPosition, 0);
    }
    
    /**
     * Format entire text with proper indentation, operator spacing, and optional line wrapping
     * @param text The text to format
     * @param cursorPosition Current cursor position
     * @param viewportWidth Viewport width for line wrapping (0 = no wrapping)
     */
    public static FormatResult formatText(String text, int cursorPosition, int viewportWidth) {
        // First pass: fix indentation
        String indented = formatIndentation(text, cursorPosition);
        
        // Second pass: apply operator spacing and whitespace normalization
        FormatHelper helper = new FormatHelper();
        FormatHelper.FormatSettings settings = helper.getSettings();
        
        // Enable wrapping if viewport width is provided
        if (viewportWidth > 0) {
            settings.wrapLongLines = true;
            settings.wrapComments = true;
            // Use 75% of viewport width as max line length for some margin
            int charWidth = 6; // Approximate character width
            settings.maxLineLength = Math.max(60, (int)(viewportWidth * 0.8f / charWidth));
        }
        
        String formatted = helper.format(indented);
        
        // Apply wrapping if enabled
        if (viewportWidth > 0 && settings.wrapLongLines) {
            formatted = helper.wrapLines(formatted, settings.maxLineLength);
        }
        
        // Recalculate cursor position (try to maintain relative position)
        int newCursorPos = Math.min(cursorPosition, formatted.length());
        
        // Try to find cursor on same line with same content context
        String[] origLines = text.split("\n", -1);
        String[] newLines = formatted.split("\n", -1);
        
        int lineStartPos = 0;
        int cursorLine = -1;
        int cursorColInLine = 0;
        
        for (int li = 0; li < origLines.length; li++) {
            int lineEndPos = lineStartPos + origLines[li].length();
            if (cursorPosition >= lineStartPos && cursorPosition <= lineEndPos) {
                cursorLine = li;
                cursorColInLine = cursorPosition - lineStartPos;
                break;
            }
            lineStartPos = lineEndPos + 1;
        }
        
        if (cursorLine >= 0 && cursorLine < newLines.length) {
            int newLineStart = 0;
            for (int li = 0; li < cursorLine; li++) {
                newLineStart += newLines[li].length() + 1;
            }
            // Clamp cursor to new line length
            newCursorPos = newLineStart + Math.min(cursorColInLine, newLines[cursorLine].length());
        }
        
        return new FormatResult(formatted, newCursorPos);
    }
    
    /**
     * Format indentation only (first pass)
     */
    private static String formatIndentation(String text, int cursorPosition) {
        String[] linesArr = text.split("\n", -1);
        StringBuilder out = new StringBuilder(text.length() + 32);
        
        boolean inString = false;
        boolean escape = false;
        boolean inBlockComment = false;
        int depth = 0;
        
        for (int li = 0; li < linesArr.length; li++) {
            String line = linesArr[li];
            String trimmedLeading = line.replaceAll("^[ \\t]+", "");
            int originalIndent = getLineIndent(line);
            
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
            
            // Check if this is a continuation line:
            // 1. Line starts with operator/dot (explicit continuation)
            // 2. Previous line ends with operator (implicit continuation - this line continues it)
            // If so, preserve extra indentation beyond the base level
            boolean isContinuation = isContinuationLine(trimmedLeading);
            if (!isContinuation && li > 0) {
                // Check if previous line ends with an operator (indicating this line continues it)
                String prevLine = linesArr[li - 1].trim();
                isContinuation = lineEndsWithContinuationOperator(prevLine);
            }
            
            if (isContinuation && originalIndent > targetIndent) {
                // This is a continuation line - preserve its extra indent
                out.append(spaces(originalIndent)).append(trimmedLeading);
            } else {
                out.append(spaces(targetIndent)).append(trimmedLeading);
            }
            if (li < linesArr.length - 1) out.append('\n');
            
            depth = Math.max(0, depth + opens - closes);
        }
        
        return out.toString();
    }
    
    /**
     * Check if a line appears to be a continuation of a previous line
     * (i.e., starts with an operator, dot, or other continuation character)
     */
    private static boolean isContinuationLine(String trimmedContent) {
        if (trimmedContent.isEmpty()) return false;
        char firstChar = trimmedContent.charAt(0);
        // Lines starting with these are likely continuations
        if (firstChar == '.' || firstChar == '+' || firstChar == '-' || 
            firstChar == '*' || firstChar == '/' || firstChar == '%' ||
            firstChar == '&' || firstChar == '|' || firstChar == '^' ||
            firstChar == '?' || firstChar == ':' || firstChar == ',') {
            return true;
        }
        // Also check for && and ||
        if (trimmedContent.startsWith("&&") || trimmedContent.startsWith("||")) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if a line ends with an operator that indicates the next line is a continuation
     */
    private static boolean lineEndsWithContinuationOperator(String trimmedLine) {
        if (trimmedLine.isEmpty()) return false;
        // Remove trailing comments
        int commentIdx = trimmedLine.indexOf("//");
        if (commentIdx >= 0) {
            trimmedLine = trimmedLine.substring(0, commentIdx).trim();
        }
        if (trimmedLine.isEmpty()) return false;
        
        // Check if ends with operator
        char lastChar = trimmedLine.charAt(trimmedLine.length() - 1);
        if (lastChar == '+' || lastChar == '-' || lastChar == '*' || 
            lastChar == '/' || lastChar == '%' || lastChar == '=' ||
            lastChar == '&' || lastChar == '|' || lastChar == '^' ||
            lastChar == '?' || lastChar == ':' || lastChar == ',' ||
            lastChar == '(' || lastChar == '[') {
            // But not if it's ; or } which end statements
            return true;
        }
        // Check for && and ||
        if (trimmedLine.endsWith("&&") || trimmedLine.endsWith("||")) {
            return true;
        }
        return false;
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