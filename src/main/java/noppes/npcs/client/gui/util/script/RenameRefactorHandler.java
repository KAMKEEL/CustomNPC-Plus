package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;

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
    private int cursorInWord = 0; // Cursor position within the word being renamed
    private boolean wordFullySelected = true; // Start with word fully selected

    // For restoring original text on cancel
    private String originalText = "";
    private int originalCursorPos = 0;

    // Callback interface
    private RenameCallback callback;

    // ==================== CURSOR BLINK ====================
    private int cursorCounter = 0;
    private long lastInputTime = 0;

    // Pattern for identifiers
    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    /**
     * Callback interface for rename operations
     */
    public interface RenameCallback {
        String getText();

        void setText(String text);

        void setTextWithoutUndo(String text);  // Set text without creating undo entry

        void pushUndoState(String text);       // Push an undo state

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
        setSelection(wordBounds[0], wordBounds[1]);

        // Start with FULL WORD SELECTED (cursor at end, word selected)
        cursorInWord = currentWord.length();
        wordFullySelected = true;

        // Determine scope for this identifier - MUST check if local shadows global
        scope = determineScope(text, wordBounds[0], originalWord, callback.getContainer());

        // Find all occurrences within scope
        findOccurrences(text, originalWord);

        if (allOccurrences.isEmpty())
            return false;

        active = true;
        markActivity();
        callback.unfocusMainEditor();

        // Push initial undo state so we can restore to this point
        callback.pushUndoState(text);

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

        // Final text is already applied - push as single undo state
        // The text was modified via setTextWithoutUndo, so push final state
        String finalText = callback.getText();
        callback.pushUndoState(finalText);

        // Changes are already applied live, just exit rename mode
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
        scope = null;
        originalText = "";
        originalCursorPos = 0;
        wordFullySelected = false;
    }

    /**
     * Handle keyboard input during rename
     * @return true if input was consumed
     */
    public boolean keyTyped(char c, int keyCode) {
        if (!active)
            return false;

        // ESC - cancel
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            cancel();
            return true;
        }

        // Enter - confirm
        if (keyCode == org.lwjgl.input.Keyboard.KEY_RETURN) {
            confirm();
            return true;
        }

        // Tab - confirm and move on
        if (keyCode == org.lwjgl.input.Keyboard.KEY_TAB) {
            confirm();
            return true;
        }

        // Any navigation or editing key clears full selection mode
        boolean wasFullySelected = wordFullySelected;

        // Backspace
        if (keyCode == org.lwjgl.input.Keyboard.KEY_BACK) {
            if (wasFullySelected) {
                // Delete entire word
                currentWord = "";
                cursorInWord = 0;
                wordFullySelected = false;
                applyLiveRename();
                markActivity();
            } else if (cursorInWord > 0 && currentWord.length() > 0) {
                String before = currentWord.substring(0, cursorInWord - 1);
                String after = currentWord.substring(cursorInWord);
                currentWord = before + after;
                cursorInWord--;
                applyLiveRename();
                markActivity();
            }
            return true;
        }

        // Delete
        if (keyCode == org.lwjgl.input.Keyboard.KEY_DELETE) {
            if (wasFullySelected) {
                // Delete entire word
                currentWord = "";
                cursorInWord = 0;
                wordFullySelected = false;
                applyLiveRename();
                markActivity();
            } else if (cursorInWord < currentWord.length()) {
                String before = currentWord.substring(0, cursorInWord);
                String after = currentWord.substring(cursorInWord + 1);
                currentWord = before + after;
                applyLiveRename();
                markActivity();
            }
            return true;
        }

        // Left arrow
        if (keyCode == org.lwjgl.input.Keyboard.KEY_LEFT) {
            wordFullySelected = false;
            if (cursorInWord > 0) {
                cursorInWord--;
                markActivity();
            }
            return true;
        }

        // Right arrow
        if (keyCode == org.lwjgl.input.Keyboard.KEY_RIGHT) {
            wordFullySelected = false;
            if (cursorInWord < currentWord.length()) {
                cursorInWord++;
                markActivity();
            }
            return true;
        }

        // Home
        if (keyCode == org.lwjgl.input.Keyboard.KEY_HOME) {
            wordFullySelected = false;
            cursorInWord = 0;
            markActivity();
            return true;
        }

        // End
        if (keyCode == org.lwjgl.input.Keyboard.KEY_END) {
            wordFullySelected = false;
            cursorInWord = currentWord.length();
            markActivity();
            return true;
        }

        // Ctrl+A - select all
        if (keyCode == org.lwjgl.input.Keyboard.KEY_A &&
                org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LCONTROL)) {
            wordFullySelected = true;
            cursorInWord = currentWord.length();
            markActivity();
            return true;
        }

        // Character input - only valid identifier characters
        if (isValidIdentifierChar(c, (wasFullySelected || currentWord.isEmpty()) && cursorInWord == 0)) {
            if (wasFullySelected) {
                // Replace entire word with this character
                currentWord = String.valueOf(c);
                cursorInWord = 1;
                wordFullySelected = false;
            } else {
                String before = currentWord.substring(0, cursorInWord);
                String after = currentWord.substring(cursorInWord);
                currentWord = before + c + after;
                cursorInWord++;
            }
            applyLiveRename();
            markActivity();
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

        // Apply rename (even if currentWord is empty - we allow temporary empty state)
        String newText = applyRename(text, currentWord);
        callback.setTextWithoutUndo(newText);  // Don't create undo entry for each keystroke

        // Update tracking for current word
        originalWord = currentWord;

        // Recalculate occurrences based on the new word
        // If empty, we still keep the positions tracked (just with empty strings)
        if (!currentWord.isEmpty()) {
            findOccurrences(newText, currentWord);
        } else {
            // For empty word, update positions based on length difference
            updateOccurrencePositionsForEmpty(newPrimaryStart);
        }

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
        // Just keep the primary occurrence position for rendering the empty box
        allOccurrences.clear();
        allOccurrences.add(new int[]{newPrimaryStart, newPrimaryStart});
    }

    /**
     * Apply rename to all occurrences in text
     */
    private String applyRename(String text, String newName) {
        if (allOccurrences.isEmpty() || newName.isEmpty())
            return text;

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        // Sort occurrences by position
        List<int[]> sorted = new ArrayList<>(allOccurrences);
        sorted.sort((a, b) -> Integer.compare(a[0], b[0]));

        for (int[] occ : sorted) {
            if (occ[0] >= lastEnd && occ[1] <= text.length()) {
                result.append(text, lastEnd, occ[0]);
                result.append(newName);
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
            if (scope.isGlobal || scope.containsPosition(start)) {
                allOccurrences.add(new int[]{start, end});
            }
        }
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
        return cursorInWord;
    }

    /**
     * Check if word is fully selected (for visual rendering)
     */
    public boolean isWordFullySelected() {
        return wordFullySelected;
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
        if (!active)
            return false;

        // Check if click is within the primary occurrence bounds
        if (clickPosInText >= primaryOccurrenceStart &&
                clickPosInText <= primaryOccurrenceEnd) {

            // Calculate position within the word
            int relativePos = clickPosInText - primaryOccurrenceStart;

            // Clamp to valid range
            cursorInWord = Math.max(0, Math.min(relativePos, currentWord.length()));

            // Clear full selection mode on click
            wordFullySelected = false;

            markActivity();
            return true;
        }

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

    public void updateCursor() {
        cursorCounter++;
    }

    private void markActivity() {
        lastInputTime = System.currentTimeMillis();
    }

    public boolean shouldShowCursor() {
        boolean recentInput = System.currentTimeMillis() - lastInputTime < 500;
        return recentInput || (cursorCounter / 10) % 2 == 0;
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
}
