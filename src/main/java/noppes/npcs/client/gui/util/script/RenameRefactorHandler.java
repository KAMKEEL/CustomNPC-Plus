package noppes.npcs.client.gui.util.script;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles IntelliJ-like rename refactoring with scope-aware renaming.
 *
 * Features:
 * - Scope detection: local variables rename only within their block, global fields rename everywhere
 * - Visual feedback: primary occurrence has white border, others just highlighted
 * - ESC cancels, Enter confirms
 * - Live preview as you type the new name
 * - Full word selection by default when starting rename
 * - Allows empty word during editing (shows empty box)
 * - Click support within rename box
 */
public class RenameRefactorHandler {

    // ==================== STATE ====================
    private boolean active = false;
    private String initialWord = "";      // The word when rename started (for undo grouping)
    private String originalWord = "";     // Current baseline for live rename
    private String currentWord = "";      // Current word being typed
    private int primaryOccurrenceStart = -1;
    private int primaryOccurrenceEnd = -1;
    private List<int[]> allOccurrences = new ArrayList<>();
    private ScopeInfo scope = null;
    
    // For global scope, track positions that have local shadowing (to exclude them)
    private List<int[]> localShadowedPositions = new ArrayList<>();

    // For restoring original text on cancel
    private String originalText = "";
    private int originalCursorPos = 0;

    // Callback interface
    private RenameCallback callback;

    // Pattern for identifiers
    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    // Placeholder to show when the rename box is empty
    private static final String EMPTY_PLACEHOLDER = "|";

    /**
     * Callback interface for rename operations
     */
    public interface RenameCallback {
        String getText();

        void setText(String text);

        void setTextWithoutUndo(String text);  // Set text without creating undo entry

        void pushUndoState(String text, int cursor);       // Push an undo state

        List<LineData> getLines();

        int getCursorPosition();

        SelectionState getSelectionState();

        void setCursorPosition(int pos);

        void unfocusMainEditor();

        void focusMainEditor();

        int getGutterWidth();

        int getLineHeight();

        int getScrolledLine();

        double getFractionalOffset();

        void scrollToPosition(int pos);

        JavaTextContainer getContainer();

        int getViewportWidth();  // For determining if clicks are in rename box
    }

    /**
     * Scope information for a variable
     */
    public static class ScopeInfo {
        public final int startOffset;
        public final int endOffset;
        public final boolean isGlobal;
        public final String scopeType; // "global", "method", "block"

        public ScopeInfo(int start, int end, boolean isGlobal, String type) {
            this.startOffset = start;
            this.endOffset = end;
            this.isGlobal = isGlobal;
            this.scopeType = type;
        }

        public boolean containsPosition(int pos) {
            return pos >= startOffset && pos < endOffset;
        }
    }

    public void setCallback(RenameCallback callback) {
        this.callback = callback;
    }

    /**
     * Start rename refactoring at the current cursor position.
     * Selects the word under cursor and finds all occurrences within scope.
     *
     * @return true if rename mode was successfully started
     */
    public boolean startRename() {
        if (callback == null)
            return false;

        String text = callback.getText();
        int cursor = callback.getCursorPosition();

        if (text == null || text.isEmpty())
            return false;

        // Find word under cursor
        int[] wordBounds = findWordAtPosition(text, cursor);
        if (wordBounds == null)
            return false;

        // Store original state for cancel AND for undo grouping
        originalText = text;
        originalCursorPos = cursor;

        initialWord = text.substring(wordBounds[0], wordBounds[1]);  // Remember for undo
        originalWord = initialWord;
        currentWord = initialWord;
        
        // Determine scope for this identifier - MUST check if local shadows global
        scope = determineScope(text, wordBounds[0], originalWord, callback.getContainer());
        
        // If global scope, find all positions where local variables shadow this name
        localShadowedPositions.clear();
        if (scope != null && scope.isGlobal) {
            findLocalShadowedPositions(text, originalWord);
        }

        // Find all occurrences within scope
        findOccurrences(text, originalWord);

        if (allOccurrences.isEmpty())
            return false;

        active = true;
        
        // Set selection to full word (uses GuiScriptTextArea's selection)
        primaryOccurrenceStart = wordBounds[0];
        primaryOccurrenceEnd = wordBounds[1];
        callback.getSelectionState().setSelection(wordBounds[0], wordBounds[1]);
       // callback.setCursorPosition(wordBounds[1]);
        callback.getSelectionState().markActivity();

        return true;
    }

    /**
     * Cancel rename operation and restore original state
     */
    public void cancel() {
        if (!active)
            return;

        // Restore original text
        if (callback != null && !originalText.isEmpty()) {
            callback.setText(originalText);
            callback.setCursorPosition(originalCursorPos);
        }

        resetState();

        if (callback != null) {
            callback.focusMainEditor();
        }
    }

    /**
     * Confirm rename operation and apply changes
     */
    public void confirm() {
        if (!active || callback == null)
            return;

        // If word is empty, restore original
        if (currentWord.isEmpty()) {
            cancel();
            return;
        }

        // Push the original text (pre-rename) so the first undo will restore the
        // document to its state before the refactor.
        // During live editing we used setTextWithoutUndo to avoid creating intermediate
        // undo entries; pushing the original snapshot here gives a single predictable
        // undo step back to the pre-rename state.
        if (originalText != null && !originalText.isEmpty()) 
            callback.pushUndoState(originalText, originalCursorPos);

        // Calculate cursor position relative to primary occurrence
        int cursorInWord = callback.getCursorPosition() - primaryOccurrenceStart;
        cursorInWord = Math.max(0, Math.min(cursorInWord, currentWord.length()));
        int newCursorPos = primaryOccurrenceStart + cursorInWord;

        resetState();

        callback.focusMainEditor();
        callback.setCursorPosition(Math.max(0, newCursorPos));
    }

    private void resetState() {
        active = false;
        initialWord = "";
        originalWord = "";
        currentWord = "";
        primaryOccurrenceStart = -1;
        primaryOccurrenceEnd = -1;
        allOccurrences.clear();
        localShadowedPositions.clear();
        scope = null;
        originalText = "";
        originalCursorPos = 0;
    }

    /**
     * Handle keyboard input during rename
     * @return true if input was consumed
     */
    public boolean keyTyped(char c, int keyCode) {
        if (!active || callback == null)
            return false;

        // ESC - cancel
        if (keyCode == Keyboard.KEY_ESCAPE) {
            cancel();
            return true;
        }

        // Enter - confirm
        if (keyCode == Keyboard.KEY_RETURN) {
            confirm();
            return true;
        }

        // Tab - confirm and move on
        if (keyCode == Keyboard.KEY_TAB) {
            confirm();
            return true;
        }

        // Get selection state
        SelectionState sel = callback.getSelectionState();
        boolean hasSelection = sel.hasSelection();
        
        // Calculate cursor position within the word
        int cursorInWord = callback.getCursorPosition() - primaryOccurrenceStart;
        cursorInWord = Math.max(0, Math.min(cursorInWord, currentWord.length()));

        // Backspace
        if (keyCode == Keyboard.KEY_BACK) {
            if (hasSelection || GuiScreen.isCtrlKeyDown()) {
                // Delete selected text (entire word if fully selected)
                currentWord = "";
                applyLiveRename();
                sel.setSelection(primaryOccurrenceStart, primaryOccurrenceStart);
                callback.setCursorPosition(primaryOccurrenceStart);
            } else if (cursorInWord > 0) {
                String before = currentWord.substring(0, cursorInWord - 1);
                String after = currentWord.substring(cursorInWord);
                currentWord = before + after;
                applyLiveRename();
                // Update cursor after the text change
                callback.setCursorPosition(primaryOccurrenceStart + cursorInWord - 1);
            }
            sel.markActivity();
            return true;
        }

        // Delete
        if (keyCode == Keyboard.KEY_DELETE) {
            if (hasSelection) {
                // Delete selected text (entire word if fully selected)
                currentWord = "";
                applyLiveRename();
                sel.setSelection(primaryOccurrenceStart, primaryOccurrenceStart);
                callback.setCursorPosition(primaryOccurrenceStart);
            } else if (cursorInWord < currentWord.length()) {
                String before = currentWord.substring(0, cursorInWord);
                String after = currentWord.substring(cursorInWord + 1);
                currentWord = before + after;
                applyLiveRename();
            }
            sel.markActivity();
            return true;
        }

        // Backspace
        if (keyCode == Keyboard.KEY_SPACE && hasSelection) {
            // Delete selected text (entire word if fully selected)
            currentWord = "";
            applyLiveRename();
            sel.setSelection(primaryOccurrenceStart, primaryOccurrenceStart);
            callback.setCursorPosition(primaryOccurrenceStart);
        }
        
        // Left arrow
        if (keyCode == Keyboard.KEY_LEFT) {
            if (cursorInWord > 0) {
                callback.setCursorPosition(primaryOccurrenceStart + cursorInWord - 1);
                sel.setSelection(0, 0); // Clear selection
            }
            sel.markActivity();
            return true;
        }

        // Right arrow
        if (keyCode == Keyboard.KEY_RIGHT) {
            if (cursorInWord < currentWord.length()) {
                callback.setCursorPosition(primaryOccurrenceStart + cursorInWord + 1);
                sel.setSelection(0, 0); // Clear selection
            }
            sel.markActivity();
            return true;
        }

        // Home
        if (keyCode == Keyboard.KEY_HOME) {
            callback.setCursorPosition(primaryOccurrenceStart);
            sel.setSelection(0, 0); // Clear selection
            sel.markActivity();
            return true;
        }

        // End
        if (keyCode == Keyboard.KEY_END) {
            callback.setCursorPosition(primaryOccurrenceStart + currentWord.length());
            sel.setSelection(0, 0); // Clear selection
            sel.markActivity();
            return true;
        }

        // Ctrl+A - select all (select the whole word)
        if (keyCode == Keyboard.KEY_A &&
                Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            sel.setSelection(primaryOccurrenceStart, primaryOccurrenceStart + currentWord.length());
            callback.setCursorPosition(primaryOccurrenceStart + currentWord.length());
            sel.markActivity();
            return true;
        }

        // Character input - only valid identifier characters
        boolean isFirstChar = hasSelection || (currentWord.isEmpty() && cursorInWord == 0);
        if (isValidIdentifierChar(c, isFirstChar)) {
            if (hasSelection) {
                // Replace entire selection with this character
                currentWord = String.valueOf(c);
                applyLiveRename();
                callback.setCursorPosition(primaryOccurrenceStart + 1);
                sel.setSelection(0, 0); // Clear selection
            } else {
                String before = currentWord.substring(0, cursorInWord);
                String after = currentWord.substring(cursorInWord);
                currentWord = before + c + after;
                applyLiveRename();
                callback.setCursorPosition(primaryOccurrenceStart + cursorInWord + 1);
            }
            sel.markActivity();
            return true;
        }

        // Block other input while in rename mode
        return true;
    }

    /**
     * Check if string is a valid identifier
     */
    private boolean isValidIdentifier(String s) {
        if (s == null || s.isEmpty())
            return false;
        return IDENTIFIER.matcher(s).matches();
    }

    /**
     * Check if character is valid for an identifier
     */
    private boolean isValidIdentifierChar(char c, boolean isFirst) {
        if (isFirst) {
            return Character.isLetter(c) || c == '_';
        }
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Apply rename changes live as user types (without creating undo entries)
     */
    private void applyLiveRename() {
        if (callback == null)
            return;

        String text = callback.getText();

        // Calculate new primary position after rename
        int occurrencesBefore = 0;
        for (int[] occ : allOccurrences) {
            if (occ[0] < primaryOccurrenceStart) {
                occurrencesBefore++;
            }
        }
        int lengthDiff = currentWord.length() - originalWord.length();
        int newPrimaryStart = primaryOccurrenceStart + (occurrencesBefore * lengthDiff);

        // If the current typed word is empty, do NOT modify the document's other occurrences.
        // We treat empty as a temporary state: preserve the editor text and the previously
        // discovered occurrences so they will re-appear when the user types again.
        if (currentWord.isEmpty()) {
            // Keep the text unchanged and preserve allOccurrences (do not clear).
            callback.setTextWithoutUndo(text);
            // Keep originalWord as the last non-empty word so length calculations remain correct
            // and place the caret at the primary position for editing.
            setSelection(newPrimaryStart, newPrimaryStart);
            return;
        }

        // Apply rename for non-empty currentWord
        String newText = applyRename(text, currentWord);
        callback.setTextWithoutUndo(newText);  // Don't create undo entry for each keystroke

        // Update tracking for current word (only when non-empty)
        originalWord = currentWord;

        // Recalculate occurrences based on the new word
        findOccurrences(newText, currentWord);

        // Update primary occurrence tracking
        if (!allOccurrences.isEmpty()) {
            for (int[] occ : allOccurrences) {
                if (Math.abs(occ[0] - newPrimaryStart) < 2) {
                    setSelection(occ[0], occ[1]);
                    break;
                }
            }
        } else if (currentWord.isEmpty()) {
            // Keep tracking the position even when empty
            setSelection(newPrimaryStart, newPrimaryStart);
        }
    }

    private void setSelection(int start, int end) {
        primaryOccurrenceStart = start;
        primaryOccurrenceEnd = end;
        if (callback != null)
            callback.getSelectionState().setSelection(start, end);
    }

    /**
     * Update occurrence positions when word is empty (for visual tracking)
     */
    private void updateOccurrencePositionsForEmpty(int newPrimaryStart) {
        // No-op: we preserve the previously discovered occurrences while the user
        // temporarily clears the rename box. Clearing occurrences here caused them
        // to be removed from the document during live-editing. Rendering will use
        // primary occurrence position set by setSelection().
        // Intentionally left blank.
    }

    /**
     * Apply rename to all occurrences in text
     */
    private String applyRename(String text, String newName) {
        if (allOccurrences.isEmpty())
            return text;

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        // Sort occurrences by position
        List<int[]> sorted = new ArrayList<>(allOccurrences);
        sorted.sort((a, b) -> Integer.compare(a[0], b[0]));

        for (int[] occ : sorted) {
            if (occ[0] >= lastEnd && occ[1] <= text.length()) {
                result.append(text, lastEnd, occ[0]);
                result.append(newName);  // Append newName, even if empty (which deletes the occurrence)
                lastEnd = occ[1];
            }
        }
        result.append(text.substring(lastEnd));

        return result.toString();
    }

    /**
     * Find all occurrences of the identifier within scope
     */
    private void findOccurrences(String text, String word) {
        allOccurrences.clear();

        if (word.isEmpty() || scope == null)
            return;

        // Get excluded ranges (strings and comments)
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);

        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
        Matcher m = pattern.matcher(text);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            // Skip if in string or comment
            if (isInExcludedRange(start, excluded))
                continue;

            // Check if within scope
            if (scope.isGlobal) {
                // For global scope, skip positions where local variables shadow the global
                if (!isInLocalShadowedRange(start)) {
                    allOccurrences.add(new int[]{start, end});
                }
            } else if (scope.containsPosition(start)) {
                allOccurrences.add(new int[]{start, end});
            }
        }
    }
    
    /**
     * Find all positions where local variables with the same name shadow the global variable.
     * This populates localShadowedPositions with ranges where local declarations exist.
     */
    private void findLocalShadowedPositions(String text, String varName) {
        localShadowedPositions.clear();
        
        List<MethodBlock> methods = MethodBlock.collectMethodBlocks(text);
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);
        
        for (MethodBlock method : methods) {
            // Check if this method has a local variable or parameter with the same name
            boolean hasLocalDecl = method.localVariables.contains(varName);
            boolean hasParamDecl = isParameterInMethod(text, varName, method);
            
            if (hasLocalDecl || hasParamDecl) {
                // Find where the local/param scope starts
                int scopeStart;
                if (hasParamDecl) {
                    // Parameter scope starts at method start
                    scopeStart = method.startOffset;
                } else {
                    // Local variable scope starts at declaration
                    scopeStart = findLocalDeclarationPosition(text, varName, method);
                    if (scopeStart < 0) scopeStart = method.startOffset;
                }
                
                // Find all occurrences of varName within this method's shadowed range
                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\b");
                Matcher m = pattern.matcher(text);
                
                while (m.find()) {
                    int pos = m.start();
                    
                    // Skip if in string or comment
                    if (isInExcludedRange(pos, excluded))
                        continue;
                    
                    // Check if within the shadowed scope of this method
                    if (pos >= scopeStart && pos < method.endOffset) {
                        localShadowedPositions.add(new int[]{m.start(), m.end()});
                    }
                }
            }
        }
    }
    
    /**
     * Check if a position is within a locally shadowed range
     */
    private boolean isInLocalShadowedRange(int pos) {
        for (int[] range : localShadowedPositions) {
            if (pos >= range[0] && pos < range[1])
                return true;
        }
        return false;
    }
    
    /**
     * Check if varName is a parameter in the method
     */
    private boolean isParameterInMethod(String text, String varName, MethodBlock method) {
        String methodHeader = text.substring(method.startOffset,
                Math.min(method.startOffset + 500, method.endOffset));
        int parenStart = methodHeader.indexOf('(');
        int parenEnd = methodHeader.indexOf(')');
        if (parenStart >= 0 && parenEnd > parenStart) {
            String params = methodHeader.substring(parenStart + 1, parenEnd);
            Pattern paramPattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\b");
            return paramPattern.matcher(params).find();
        }
        return false;
    }

    /**
     * Check if position is in an excluded range
     */
    private boolean isInExcludedRange(int pos, List<int[]> ranges) {
        for (int[] range : ranges) {
            if (pos >= range[0] && pos < range[1])
                return true;
        }
        return false;
    }

    /**
     * Find word boundaries at a given position
     */
    private int[] findWordAtPosition(String text, int pos) {
        if (pos < 0 || pos > text.length())
            return null;

        // Find start of word
        int start = pos;
        while (start > 0 && isIdentifierChar(text.charAt(start - 1))) {
            start--;
        }

        // Find end of word
        int end = pos;
        while (end < text.length() && isIdentifierChar(text.charAt(end))) {
            end++;
        }

        if (start == end)
            return null;

        // Validate it's a proper identifier
        String word = text.substring(start, end);
        if (!IDENTIFIER.matcher(word).matches())
            return null;

        return new int[]{start, end};
    }

    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Determine the scope for a variable at the given position.
     * Key insight: Local variables shadow global variables within their scope.
     * We need to check if the cursor is within the scope of a local declaration.
     */
    private ScopeInfo determineScope(String text, int position, String varName, JavaTextContainer container) {
        List<MethodBlock> methods = MethodBlock.collectMethodBlocks(text);

        // First, check if this is a global field declaration (outside any method)
        boolean isGlobalField = isGlobalFieldDeclaration(text, varName, methods);

        // Check if cursor is outside all methods - definitely global scope
        boolean insideMethod = false;
        MethodBlock containingMethod = null;
        for (MethodBlock method : methods) {
            if (method.containsPosition(position)) {
                insideMethod = true;
                containingMethod = method;
                break;
            }
        }

        if (!insideMethod || containingMethod == null) {
            // Cursor is outside methods - must be global scope
            return new ScopeInfo(0, text.length(), true, "global");
        }

        // Cursor is inside a method
        // Check if it's a method parameter first
        String methodHeader = text.substring(containingMethod.startOffset,
                Math.min(containingMethod.startOffset + 500, containingMethod.endOffset));
        int parenStart = methodHeader.indexOf('(');
        int parenEnd = methodHeader.indexOf(')');
        if (parenStart >= 0 && parenEnd > parenStart) {
            String params = methodHeader.substring(parenStart + 1, parenEnd);
            Pattern paramPattern = Pattern.compile("\\b" + Pattern.quote(varName) + "\\b");
            if (paramPattern.matcher(params).find()) {
                // It's a parameter - scope to method
                return new ScopeInfo(containingMethod.startOffset, containingMethod.endOffset, false, "parameter");
            }
        }

        // Check if there's a local variable with this name in the method
        if (containingMethod.localVariables.contains(varName)) {
            // Find where the local variable is declared
            int localDeclPosition = findLocalDeclarationPosition(text, varName, containingMethod);

            if (localDeclPosition >= 0 && position >= localDeclPosition) {
                // Cursor is at or after the local declaration - this is the local variable
                int[] blockBounds = findInnermostBlockWithDeclaration(text, position, varName, containingMethod);
                if (blockBounds != null) {
                    return new ScopeInfo(blockBounds[0], blockBounds[1], false, "block");
                }
                return new ScopeInfo(containingMethod.startOffset, containingMethod.endOffset, false, "local");
            } else if (isGlobalField) {
                // Cursor is BEFORE the local declaration - must be referring to global
                // This is the shadowing case
                return new ScopeInfo(0, text.length(), true, "global");
            }
        }

        // No local variable with this name - check if it's a global field
        if (isGlobalField) {
            return new ScopeInfo(0, text.length(), true, "global");
        }

        // Unknown variable - default to method scope as fallback
        return new ScopeInfo(containingMethod.startOffset, containingMethod.endOffset, false, "method");
    }

    /**
     * Check if a variable is declared as a global field (outside any method)
     */
    private boolean isGlobalFieldDeclaration(String text, String varName, List<MethodBlock> methods) {
        Matcher m = JavaTextContainer.GLOBAL_FIELD_DECL.matcher(text);
        while (m.find()) {
            if (m.group(2).equals(varName)) {
                int pos = m.start(2);
                // Verify it's not inside a method
                boolean insideMethod = false;
                for (MethodBlock method : methods) {
                    if (method.containsPosition(pos)) {
                        insideMethod = true;
                        break;
                    }
                }
                if (!insideMethod) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find the position of the first local variable declaration with the given name
     */
    private int findLocalDeclarationPosition(String text, String varName, MethodBlock method) {
        // Pattern for local variable declaration: Type varName = or Type varName;
        Pattern localDecl = Pattern.compile("\\b\\w+\\s+" + Pattern.quote(varName) + "\\s*[=;,)]");
        String methodText = text.substring(method.startOffset, Math.min(method.endOffset, text.length()));
        Matcher m = localDecl.matcher(methodText);

        // Skip past the method header (parameters) to find true local declarations
        int bodyStart = methodText.indexOf('{');
        if (bodyStart < 0)
            bodyStart = 0;

        while (m.find()) {
            if (m.start() >= bodyStart) {
                return method.startOffset + m.start();
            }
        }
        return -1;
    }

    /**
     * Find the innermost brace block that contains both the position and a declaration of varName
     */
    private int[] findInnermostBlockWithDeclaration(String text, int position, String varName, MethodBlock method) {
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);

        // Find all brace blocks within the method
        List<int[]> blocks = new ArrayList<>();
        List<Integer> braceStack = new ArrayList<>();

        for (int i = method.startOffset; i < method.endOffset && i < text.length(); i++) {
            if (isInExcludedRange(i, excluded))
                continue;

            char c = text.charAt(i);
            if (c == '{') {
                braceStack.add(i);
            } else if (c == '}') {
                if (!braceStack.isEmpty()) {
                    int openPos = braceStack.remove(braceStack.size() - 1);
                    blocks.add(new int[]{openPos, i + 1});
                }
            }
        }

        // Find smallest block containing position that has a declaration of varName
        int[] best = null;
        int bestSize = Integer.MAX_VALUE;

        Pattern declPattern = Pattern.compile("\\b\\w+\\s+" + Pattern.quote(varName) + "\\s*[=;,)]");

        for (int[] block : blocks) {
            if (block[0] <= position && block[1] >= position) {
                int size = block[1] - block[0];
                if (size < bestSize) {
                    // Check if this block contains a declaration of varName
                    String blockText = text.substring(block[0], Math.min(block[1], text.length()));
                    if (declPattern.matcher(blockText).find()) {
                        best = block;
                        bestSize = size;
                    }
                }
            }
        }

        return best;
    }

    // ==================== RENDERING ====================

    /**
     * Get all occurrences for rendering highlights
     */
    public List<int[]> getOccurrences() {
        return allOccurrences;
    }

    /**
     * Get primary occurrence (the one under cursor when rename started)
     */
    public int[] getPrimaryOccurrence() {
        if (!active)
            return null;
        return new int[]{primaryOccurrenceStart, primaryOccurrenceEnd};
    }

    /**
     * Check if a given occurrence is the primary one
     */
    public boolean isPrimaryOccurrence(int start) {
        return start == primaryOccurrenceStart;
    }

    /**
     * Get cursor position within the rename word for rendering
     */
    public int getCursorInWord() {
        if (callback == null) return 0;
        int cursorPos = callback.getCursorPosition();
        return Math.max(0, Math.min(cursorPos - primaryOccurrenceStart, currentWord.length()));
    }

    /**
     * Check if word is fully selected (for visual rendering)
     */
    public boolean isWordFullySelected() {
        if (callback == null) return false;
        SelectionState selection = callback.getSelectionState();
        return selection.hasSelection() && 
               selection.getStartSelection() == primaryOccurrenceStart && 
               selection.getEndSelection() == primaryOccurrenceEnd;
    }

    /**
     * Get the current word being renamed
     */
    public String getCurrentWord() {
        return currentWord;
    }

    // ==================== MOUSE HANDLING ====================

    /**
     * Handle mouse click within the rename box area.
     *
     * @param clickPosInText The text position where the click occurred
     * @return true if the click was within the primary occurrence box, false otherwise
     */
    public boolean handleClick(int clickPosInText) {
        if (!active || callback == null)
            return false;
        
        return false;
    }

    /**
     * Check if a text position is within the primary rename occurrence
     */
    public boolean isPositionInPrimaryOccurrence(int pos) {
        if (!active)
            return false;
        return pos >= primaryOccurrenceStart && pos <= primaryOccurrenceEnd;
    }

    // ==================== STATUS ====================

    public boolean isActive() {
        return active;
    }

    /**
     * @deprecated Use GuiScriptTextArea's cursor rendering instead
     */
    public void updateCursor() {
        // No-op - cursor is now handled by GuiScriptTextArea
    }

    /**
     * Check if cursor should be visible based on blink timing.
     * Uses SelectionState's recent input check.
     */
    public boolean shouldShowCursor() {
        if (callback != null) {
            return callback.getSelectionState().hadRecentInput();
        }
        return true;
    }

    /**
     * Get scope type description for status display
     */
    public String getScopeDescription() {
        if (scope == null)
            return "";
        switch (scope.scopeType) {
            case "global":
                return "Renaming in file (" + allOccurrences.size() + " occurrences)";
            case "parameter":
                return "Renaming parameter (" + allOccurrences.size() + " occurrences)";
            case "local":
                return "Renaming local variable (" + allOccurrences.size() + " occurrences)";
            case "method":
                return "Renaming in method (" + allOccurrences.size() + " occurrences)";
            case "block":
                return "Renaming in block (" + allOccurrences.size() + " occurrences)";
            default:
                return "Renaming (" + allOccurrences.size() + " occurrences)";
        }
    }

    /**
     * Get number of occurrences that will be renamed
     */
    public int getOccurrenceCount() {
        return allOccurrences.size();
    }

    public void drawStatusBox() {
        //        String status = renameHandler.getScopeDescription();
        //        String hint = "Enter \u2713 | Esc \u2715"; // Enter to confirm, Esc to cancel
        //        int statusWidth = Math.max(ClientProxy.Font.width(status), ClientProxy.Font.width(hint)) + 8;
        //        int statusX = x + width - statusWidth - 8;
        //        int statusY = y + height - 30;
        //        // Semi-transparent background
        //        drawRect(statusX - 2, statusY - 2, statusX + statusWidth + 2, statusY + 24, 0xDD1a1a2e);
        //        // Border (purple/blue for rename mode)
        //        int borderColor = 0xFF6677dd;
        //        drawRect(statusX - 2, statusY - 2, statusX + statusWidth + 2, statusY - 1, borderColor);
        //        drawRect(statusX - 2, statusY + 23, statusX + statusWidth + 2, statusY + 24, borderColor);
        //        drawRect(statusX - 2, statusY - 2, statusX - 1, statusY + 24, borderColor);
        //        drawRect(statusX + statusWidth + 1, statusY - 2, statusX + statusWidth + 2, statusY + 24, borderColor);
        //        // Status text (top line)
        //        ClientProxy.Font.drawString(status, statusX + 4, statusY + 2, 0xFFccddff);
        //        // Hint text (bottom line - dimmer)
        //        ClientProxy.Font.drawString(hint, statusX + 4, statusY + 13, 0xAA88aacc);
    }
}
