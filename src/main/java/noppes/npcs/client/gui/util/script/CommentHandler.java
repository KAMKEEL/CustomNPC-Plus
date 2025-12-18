package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import java.util.List;

/**
 * Handles line comment toggling for the script editor.
 */
public class CommentHandler {

    /**
     * Result of comment toggle operation
     */
    public static class ToggleResult {
        public final String text;
        public final int delta;  // Change in length (negative if comment removed)
        public final int nonWsIndex;  // Index of first non-whitespace character
        
        public ToggleResult(String text, int delta, int nonWsIndex) {
            this.text = text;
            this.delta = delta;
            this.nonWsIndex = nonWsIndex;
        }
        
        public boolean wasCommented() {
            return delta < 0;
        }
    }
    
    /**
     * Toggle line comment on a single line
     */
    public static ToggleResult toggleLine(String lineText) {
        // Find first non-whitespace
        int nonWs = 0;
        while (nonWs < lineText.length() && Character.isWhitespace(lineText.charAt(nonWs))) {
            nonWs++;
        }
        
        boolean hasContent = nonWs < lineText.length();
        boolean hasComment = hasContent && lineText.startsWith("//", nonWs);
        String newText;
        
        if (hasComment) {
            // Remove comment
            int cut = Math.min(nonWs + 2, lineText.length());
            newText = lineText.substring(0, nonWs) + lineText.substring(cut);
        } else if (hasContent) {
            // Add comment
            newText = lineText.substring(0, nonWs) + "//" + lineText.substring(nonWs);
        } else {
            // Empty/whitespace line - no change
            newText = lineText;
        }
        
        return new ToggleResult(newText, newText.length() - lineText.length(), nonWs);
    }
    
    /**
     * Result of toggling comments on a selection
     */
    public static class SelectionToggleResult {
        public final String newText;
        public final int newStartSelection;
        public final int newEndSelection;
        
        public SelectionToggleResult(String text, int start, int end) {
            this.newText = text;
            this.newStartSelection = start;
            this.newEndSelection = end;
        }
    }
    
    /**
     * Toggle comments on all lines in a selection
     */
    public static SelectionToggleResult toggleCommentSelection(
            String text, 
            List<LineData> lines, 
            int startSelection, 
            int endSelection) {
        
        StringBuilder newText = new StringBuilder(text.length() + 100);
        int prevEnd = 0;
        int newStart = -1;
        int newEnd = -1;
        int newIndex = 0;
        
        for (LineData line : lines) {
            int safeLineStart = Math.max(0, Math.min(line.start, text.length()));
            int safeLineEnd = Math.max(0, Math.min(line.end, text.length()));
            
            // Handle gap between lines (shouldn't happen normally)
            if (safeLineStart > prevEnd) {
                if (newStart == -1 && startSelection >= prevEnd && startSelection <= safeLineStart) {
                    newStart = newIndex + (startSelection - prevEnd);
                }
                if (newEnd == -1 && endSelection >= prevEnd && endSelection <= safeLineStart) {
                    newEnd = newIndex + (endSelection - prevEnd);
                }
                newText.append(text, prevEnd, safeLineStart);
                newIndex += safeLineStart - prevEnd;
            }
            
            // Check if this line is in selection
            if (safeLineEnd > startSelection && safeLineStart < endSelection) {
                String lineText = text.substring(safeLineStart, safeLineEnd);
                ToggleResult tr = toggleLine(lineText);
                String newLineText = tr.text;
                int nonWs = tr.nonWsIndex;
                int delta = tr.delta;
                
                // Update start selection position
                if (newStart == -1 && startSelection >= safeLineStart && startSelection <= safeLineEnd) {
                    int offsetInOld = startSelection - safeLineStart;
                    int newOffset = offsetInOld;
                    if (offsetInOld >= nonWs) {
                        newOffset = offsetInOld + delta;
                    }
                    newStart = newIndex + Math.max(0, newOffset);
                }
                
                // Update end selection position
                if (newEnd == -1 && endSelection >= safeLineStart && endSelection <= safeLineEnd) {
                    int offsetInOld = endSelection - safeLineStart;
                    int newOffset = offsetInOld;
                    if (offsetInOld >= nonWs) {
                        newOffset = offsetInOld + delta;
                    }
                    newEnd = newIndex + Math.max(0, newOffset);
                }
                
                newText.append(newLineText);
                newIndex += newLineText.length();
            } else {
                // Line not in selection - keep unchanged
                if (newStart == -1 && startSelection >= safeLineStart && startSelection <= safeLineEnd) {
                    newStart = newIndex + (startSelection - safeLineStart);
                }
                if (newEnd == -1 && endSelection >= safeLineStart && endSelection <= safeLineEnd) {
                    newEnd = newIndex + (endSelection - safeLineStart);
                }
                newText.append(text, safeLineStart, safeLineEnd);
                newIndex += safeLineEnd - safeLineStart;
            }
            prevEnd = safeLineEnd;
        }
        
        // Handle remaining text after last line
        if (prevEnd < text.length()) {
            if (newStart == -1 && startSelection >= prevEnd && startSelection <= text.length()) {
                newStart = newIndex + (startSelection - prevEnd);
            }
            if (newEnd == -1 && endSelection >= prevEnd && endSelection <= text.length()) {
                newEnd = newIndex + (endSelection - prevEnd);
            }
            newText.append(text, prevEnd, text.length());
            newIndex += text.length() - prevEnd;
        }
        
        // Fallback for positions not found
        if (newStart == -1) newStart = Math.max(0, Math.min(startSelection, newText.length()));
        if (newEnd == -1) newEnd = Math.max(0, Math.min(endSelection, newText.length()));
        
        return new SelectionToggleResult(newText.toString(), newStart, newEnd);
    }
    
    /**
     * Toggle comment on a single line at cursor position
     */
    public static SingleLineToggleResult toggleCommentAtCursor(
            String text, 
            List<LineData> lines, 
            int cursorPosition) {
        
        for (LineData line : lines) {
            int lineStart = Math.max(0, Math.min(line.start, text.length()));
            int lineEnd = Math.max(0, Math.min(line.end, text.length()));
            
            if (cursorPosition >= lineStart && cursorPosition <= lineEnd) {
                String lineText = text.substring(lineStart, lineEnd);
                ToggleResult tr = toggleLine(lineText);
                boolean hadComment = tr.delta < 0;
                
                String before = text.substring(0, lineStart);
                String after = lineEnd <= text.length() ? text.substring(lineEnd) : "";
                String newText = before + tr.text + after;
                
                // Calculate new cursor position
                int cursorDelta = hadComment ? -2 : 2;
                int newCursor = cursorPosition + cursorDelta;
                if (cursorPosition < lineStart + tr.nonWsIndex + (hadComment ? 2 : 0)) {
                    newCursor = cursorPosition;
                }
                newCursor = Math.max(lineStart, newCursor);
                
                return new SingleLineToggleResult(newText, newCursor);
            }
        }
        
        // No line found at cursor - return unchanged
        return new SingleLineToggleResult(text, cursorPosition);
    }
    
    /**
     * Result of toggling comment on single line
     */
    public static class SingleLineToggleResult {
        public final String newText;
        public final int newCursorPosition;
        
        public SingleLineToggleResult(String text, int cursor) {
            this.newText = text;
            this.newCursorPosition = cursor;
        }
    }
}