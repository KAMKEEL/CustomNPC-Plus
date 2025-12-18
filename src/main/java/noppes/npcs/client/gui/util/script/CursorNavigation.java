package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles cursor navigation logic (up, down, left, right, word movement).
 */
public class CursorNavigation {
    
    /**
     * Move cursor up one line, maintaining column position
     */
    public static int cursorUp(int cursorPosition, List<LineData> lines, String text) {
        if (lines == null || lines.isEmpty()) return 0;
        
        for (int i = 0; i < lines.size(); i++) {
            LineData data = lines.get(i);
            boolean isLastLine = (i == lines.size() - 1);
            boolean isOnLine = isLastLine 
                    ? (cursorPosition >= data.start && cursorPosition <= data.end)
                    : (cursorPosition >= data.start && cursorPosition < data.end);
            
            if (isOnLine) {
                if (i == 0) {
                    return 0; // Already on first line
                }
                
                int column = Math.min(cursorPosition - data.start, data.text.length());
                LineData target = lines.get(i - 1);
                int targetColumn = Math.min(column, target.text.length());
                int targetIndent = getLineIndent(target.text);
                int minPos = target.start + Math.min(targetIndent, target.text.length());
                int targetPos = target.start + targetColumn;
                return Math.max(minPos, targetPos);
            }
        }
        return 0;
    }
    
    /**
     * Move cursor down one line, maintaining column position
     */
    public static int cursorDown(int cursorPosition, List<LineData> lines, String text) {
        if (lines == null || lines.isEmpty()) return text != null ? text.length() : 0;
        if (text != null && cursorPosition == text.length()) return cursorPosition;
        
        for (int i = 0; i < lines.size(); i++) {
            LineData data = lines.get(i);
            boolean isLastLine = (i == lines.size() - 1);
            boolean isOnLine = isLastLine 
                    ? (cursorPosition >= data.start && cursorPosition <= data.end)
                    : (cursorPosition >= data.start && cursorPosition < data.end);
            
            if (isOnLine) {
                if (i >= lines.size() - 1) {
                    return cursorPosition; // Already on last line
                }
                
                int column = Math.min(cursorPosition - data.start, data.text.length());
                LineData target = lines.get(i + 1);
                int targetColumn = Math.min(column, target.text.length());
                int targetIndent = getLineIndent(target.text);
                int minPos = target.start + Math.min(targetIndent, target.text.length());
                int targetPos = target.start + targetColumn;
                return Math.max(minPos, targetPos);
            }
        }
        return text != null ? text.length() : 0;
    }
    
    /**
     * Move cursor left, optionally by word
     */
    public static int cursorLeft(int cursorPosition, String text, Pattern wordPattern, boolean byWord) {
        if (text == null || cursorPosition <= 0) return 0;
        
        int moveAmount = 1;
        if (byWord && wordPattern != null) {
            Matcher m = wordPattern.matcher(text.substring(0, cursorPosition));
            while (m.find()) {
                if (m.start() != m.end()) {
                    moveAmount = cursorPosition - m.start();
                }
            }
        }
        return Math.max(cursorPosition - moveAmount, 0);
    }
    
    /**
     * Move cursor right, optionally by word
     */
    public static int cursorRight(int cursorPosition, String text, Pattern wordPattern, boolean byWord) {
        if (text == null) return 0;
        int textLen = text.length();
        if (cursorPosition >= textLen) return textLen;
        
        int moveAmount = 1;
        if (byWord && wordPattern != null) {
            Matcher m = wordPattern.matcher(text.substring(cursorPosition));
            if (m.find() && m.start() > 0) {
                moveAmount = m.start();
            } else if (m.find()) {
                moveAmount = m.start();
            }
        }
        return Math.min(cursorPosition + moveAmount, textLen);
    }
    
    /**
     * Get cursor position from mouse click coordinates
     */
    public static int getPositionFromMouse(
            int mouseX, int mouseY,
            int areaX, int areaY, 
            int gutterWidth,
            int lineHeight,
            int scrolledLine,
            double scrollFracOffset,
            List<LineData> lines,
            String text,
            FontWidthProvider fontWidth) {
        
        int xMouse = mouseX - (areaX + gutterWidth + 1);
        int yMouse = mouseY - areaY - 1;
        
        // Account for fractional scroll offset
        double fracPixels = scrollFracOffset * lineHeight;
        yMouse = (int) Math.round(yMouse + fracPixels);
        
        if (lines == null || lines.isEmpty()) {
            return text != null ? text.length() : 0;
        }
        
        int visibleLines = Math.min(lines.size(), scrolledLine + 50); // Reasonable limit
        
        for (int i = 0; i < lines.size(); i++) {
            LineData data = lines.get(i);
            
            if (i >= scrolledLine && i <= visibleLines) {
                int yPos = (i - scrolledLine) * lineHeight;
                
                if (yMouse >= yPos && yMouse < yPos + lineHeight) {
                    // Found the line, now find character position
                    int lineWidth = 0;
                    char[] chars = data.text.toCharArray();
                    
                    for (int j = 1; j <= chars.length; j++) {
                        int w = fontWidth.getWidth(data.text.substring(0, j));
                        if (xMouse < lineWidth + (w - lineWidth) / 2) {
                            return data.start + j - 1;
                        }
                        lineWidth = w;
                    }
                    
                    // Click past end of line - place cursor at end of text (not on newline)
                    int posAfterChars = data.start + chars.length;
                    return Math.min(posAfterChars, text.length());
                }
            }
        }
        
        return text != null ? text.length() : 0;
    }
    
    /**
     * Interface for font width measurement
     */
    public interface FontWidthProvider {
        int getWidth(String text);
    }
    
    /**
     * Get leading whitespace count of a line
     */
    private static int getLineIndent(String lineText) {
        int indent = 0;
        for (int i = 0; i < lineText.length(); i++) {
            char c = lineText.charAt(i);
            if (c == ' ' || c == '\t') indent++;
            else break;
        }
        return indent;
    }
    
    /**
     * Find line index containing given position
     */
    public static int findLineIndex(int position, List<LineData> lines) {
        if (lines == null || lines.isEmpty()) return 0;
        
        for (int i = 0; i < lines.size(); i++) {
            LineData line = lines.get(i);
            boolean isLastLine = (i == lines.size() - 1);
            boolean isOnLine = isLastLine 
                    ? (position >= line.start && position <= line.end)
                    : (position >= line.start && position < line.end);
            if (isOnLine) {
                return i;
            }
        }
        return lines.size() - 1;
    }
    
    /**
     * Find line data containing given position
     */
    public static LineData findLineAt(int position, List<LineData> lines) {
        int idx = findLineIndex(position, lines);
        if (idx >= 0 && idx < lines.size()) {
            return lines.get(idx);
        }
        return null;
    }
}