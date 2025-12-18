package noppes.npcs.client.gui.util.script;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages undo/redo stacks for the text editor.
 */
public class UndoManager {
    
    /**
     * Stores a snapshot of text state for undo/redo
     */
    public static class UndoState {
        public final String text;
        public final int cursorPosition;
        
        public UndoState(String text, int cursorPosition) {
            this.text = text;
            this.cursorPosition = cursorPosition;
        }
    }
    
    private final List<UndoState> undoStack = new ArrayList<>();
    private final List<UndoState> redoStack = new ArrayList<>();
    private boolean isUndoing = false;
    
    // Optional: limit stack size to prevent memory issues
    private static final int MAX_STACK_SIZE = 100;
    
    /**
     * Record current state before making a change
     */
    public void recordState(String text, int cursorPosition) {
        if (isUndoing) return;
        
        undoStack.add(new UndoState(text, cursorPosition));
        redoStack.clear();
        
        // Trim stack if too large
        while (undoStack.size() > MAX_STACK_SIZE) {
            undoStack.remove(0);
        }
    }
    
    /**
     * Undo to previous state
     * @param currentText Current text before undo
     * @param currentCursor Current cursor position
     * @return Previous state, or null if nothing to undo
     */
    public UndoState undo(String currentText, int currentCursor) {
        if (undoStack.isEmpty()) return null;
        
        isUndoing = true;
        redoStack.add(new UndoState(currentText, currentCursor));
        UndoState state = undoStack.remove(undoStack.size() - 1);
        isUndoing = false;
        
        return state;
    }
    
    /**
     * Redo to next state
     * @param currentText Current text before redo
     * @param currentCursor Current cursor position
     * @return Next state, or null if nothing to redo
     */
    public UndoState redo(String currentText, int currentCursor) {
        if (redoStack.isEmpty()) return null;
        
        isUndoing = true;
        undoStack.add(new UndoState(currentText, currentCursor));
        UndoState state = redoStack.remove(redoStack.size() - 1);
        isUndoing = false;
        
        return state;
    }
    
    /**
     * Check if undo is available
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Check if redo is available
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Clear all history
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
    
    /**
     * Check if currently performing an undo/redo operation
     */
    public boolean isUndoing() {
        return isUndoing;
    }
    
    /**
     * Set undoing flag (for external control during setText)
     */
    public void setUndoing(boolean undoing) {
        this.isUndoing = undoing;
    }
    
    // For backward compatibility with existing code
    public List<UndoState> getUndoList() { return undoStack; }
    public List<UndoState> getRedoList() { return redoStack; }
}