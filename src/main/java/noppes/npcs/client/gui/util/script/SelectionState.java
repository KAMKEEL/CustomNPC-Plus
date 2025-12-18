package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages cursor position, text selection, and related operations.
 */
public class SelectionState {
    private int startSelection = 0;
    private int endSelection = 0;
    private int cursorPosition = 0;
    private long lastInputTime = 0L;
    
    /**
     * Reset all cursor selections to a single position
     */
    public void reset(int position) {
        startSelection = endSelection = cursorPosition = position;
    }
    
    /**
     * Clamp all selection bounds to valid text range
     */
    public void clamp(int textLength) {
        if (textLength <= 0) {
            startSelection = endSelection = cursorPosition = 0;
            return;
        }
        startSelection = Math.max(0, Math.min(startSelection, textLength));
        endSelection = Math.max(0, Math.min(endSelection, textLength));
        cursorPosition = Math.max(0, Math.min(cursorPosition, textLength));
    }
    
    /**
     * Set cursor position, optionally extending selection
     */
    public void setCursor(int position, int textLength, boolean extendSelection) {
        position = Math.max(0, Math.min(position, textLength));
        
        if (position != cursorPosition) {
            if (!extendSelection) {
                endSelection = startSelection = cursorPosition = position;
            } else {
                int diff = cursorPosition - position;
                if (cursorPosition == startSelection) {
                    startSelection -= diff;
                } else if (cursorPosition == endSelection) {
                    endSelection -= diff;
                }
                
                // Ensure start <= end
                if (startSelection > endSelection) {
                    int temp = endSelection;
                    endSelection = startSelection;
                    startSelection = temp;
                }
                cursorPosition = position;
            }
            clamp(textLength);
            markActivity();
        }
    }
    
    /**
     * Select a range of text
     */
    public void setSelection(int start, int end) {
        startSelection = Math.min(start, end);
        endSelection = Math.max(start, end);
        cursorPosition = endSelection;
    }
    
    /**
     * Select all text
     */
    public void selectAll(int textLength) {
        startSelection = cursorPosition = 0;
        endSelection = textLength;
    }
    
    /**
     * Check if there's an active selection (not just cursor)
     */
    public boolean hasSelection() {
        return startSelection != endSelection;
    }
    
    /**
     * Get selected text
     */
    public String getSelectedText(String text) {
        if (!hasSelection() || text == null) return "";
        int start = Math.max(0, Math.min(startSelection, text.length()));
        int end = Math.max(0, Math.min(endSelection, text.length()));
        return text.substring(start, end);
    }
    
    /**
     * Get text before selection/cursor
     */
    public String getTextBefore(String text) {
        if (text == null || startSelection <= 0) return "";
        return text.substring(0, Math.min(startSelection, text.length()));
    }
    
    /**
     * Get text after selection/cursor
     */
    public String getTextAfter(String text) {
        if (text == null) return "";
        int pos = Math.min(endSelection, text.length());
        return text.substring(pos);
    }
    
    /**
     * Find which line the cursor is on (0-indexed)
     */
    public int getCursorLineIndex(List<LineData> lines, int textLength) {
        if (lines == null || lines.isEmpty()) return 0;
        
        for (int i = 0; i < lines.size(); i++) {
            LineData ld = lines.get(i);
            boolean isLastLine = (i == lines.size() - 1);
            boolean isOnLine = isLastLine 
                    ? (cursorPosition >= ld.start && cursorPosition <= ld.end)
                    : (cursorPosition >= ld.start && cursorPosition < ld.end);
            if (isOnLine) {
                return i;
            }
        }
        return lines.size() - 1;
    }
    
    /**
     *  Check if cursor is on a specific line, handling the last line specially
     *  (last line doesn't have trailing newline, so use <= for end boundary)
     */
    public boolean isCursorOnLine(int lineIndex, LineData line, int totalLines) {
        boolean isLastLine = (lineIndex == totalLines - 1);
        return isLastLine 
                ? (cursorPosition >= line.start && cursorPosition <= line.end)
                : (cursorPosition >= line.start && cursorPosition < line.end);
    }
    
    /**
     * Find line containing the cursor
     */
    public LineData findCurrentLine(List<LineData> lines) {
        if (lines == null) return null;
        for (int i = 0; i < lines.size(); i++) {
            LineData line = lines.get(i);
            if (isCursorOnLine(i, line, lines.size())) {
                return line;
            }
        }
        return null;
    }
    
    /**
     * Mark user activity (for cursor blink timing)
     */
    public void markActivity() {
        lastInputTime = System.currentTimeMillis();
    }
    
    /**
     * Check if there was recent input (for cursor blink pause)
     */
    public boolean hadRecentInput() {
        return System.currentTimeMillis() - lastInputTime < 500;
    }
    
    // Getters
    public int getStartSelection() { return startSelection; }
    public int getEndSelection() { return endSelection; }
    public int getCursorPosition() { return cursorPosition; }
    public long getLastInputTime() { return lastInputTime; }
    
    // Direct setters (use with caution, prefer higher-level methods)
    public void setStartSelection(int pos) { this.startSelection = pos; }
    public void setEndSelection(int pos) { this.endSelection = pos; }
    public void setCursorPositionDirect(int pos) { this.cursorPosition = pos; }
    
    /**
     * After text modification, update selection to new position
     */
    public void afterTextInsert(int newPosition) {
        endSelection = startSelection = cursorPosition = newPosition;
        markActivity();
    }

    /**
     * Select the word surrounding the current cursor position using the provided regex.
     * The regex should match word tokens; the implementation finds the token which
     * contains the cursor and selects its start..end.
     */
    public void selectWordAtCursor(String text, Pattern wordPattern) {
        if (text == null || wordPattern == null) return;
        java.util.regex.Matcher m = wordPattern.matcher(text);
        while (m.find()) {
            if (cursorPosition >= m.start() && cursorPosition <= m.end()) {
                setSelection(m.start(), m.end());
                return;
            }
        }
    }

    /**
     * Select the entire line containing the cursor. `lines` is the parsed
     * line data from `JavaTextContainer` where each LineData has start/end indices.
     */
    public void selectLineAtCursor(List<LineData> lines) {
        if (lines == null) return;
        for (LineData line : lines) {
            if (isCursorOnLine(lines.indexOf(line), line, lines.size())) {
                setSelection(line.start, line.end);
                return;
            }
        }
    }
}