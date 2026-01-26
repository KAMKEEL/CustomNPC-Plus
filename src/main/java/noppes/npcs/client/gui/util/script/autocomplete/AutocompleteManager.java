package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.ScriptTextContainer;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages autocomplete state and coordinates between providers and UI.
 * This is the main entry point for autocomplete functionality.
 */
public class AutocompleteManager {
    
    // ==================== CONSTANTS ====================
    
    /** Characters that trigger autocomplete */
    private static final String TRIGGER_CHARS = ".";
    
    /** Characters that should close autocomplete */
    private static final String CLOSE_CHARS = ";{}()[]<>,\"'`";
    
    /** Minimum characters before showing suggestions (for non-dot triggers) */
    private static final int MIN_PREFIX_LENGTH = 1;

    /** Maximum number of suggestions to show (performance/UX) */
    private static final int MAX_SUGGESTIONS = 150;
    
    /** Pattern for identifier characters */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*");
    
    // ==================== STATE ====================
    
    private final AutocompleteMenu menu;
    private final JavaAutocompleteProvider javaProvider;
    private final JSAutocompleteProvider jsProvider;
    
    private ScriptTextContainer container;
    private ScriptDocument document;
    
    /** Whether autocomplete is currently active */
    private boolean active = false;
    
    /** The prefix being typed */
    private String currentPrefix = "";
    
    /** Start position of the current prefix */
    private int prefixStartPosition = -1;
    
    /** Whether this was an explicit trigger (Ctrl+Space) */
    private boolean explicitTrigger = false;
    
    /** The full class name of the current receiver type (for member access tracking) */
    private String currentReceiverFullName = null;
    
    /** Whether current context is member access */
    private boolean currentIsMemberAccess = false;
    
    /** Callback for text insertion */
    private InsertCallback insertCallback;
    
    /**
     * Callback interface for inserting autocomplete results.
     */
    public interface InsertCallback {
        /**
         * Insert text, replacing from start to current cursor position.
         * @param text Text to insert
         * @param startPosition Position to start replacing from
         */
        void insertText(String text, int startPosition);
        
        /**
         * Replace text in a specific range.
         * @param text Text to insert
         * @param startPosition Position to start replacing from
         * @param endPosition Position to end replacing at
         */
        void replaceTextRange(String text, int startPosition, int endPosition);
        
        /**
         * Add an import statement and sort all imports.
         * @param importPath Full path of the class to import (e.g., "net.minecraft.client.Minecraft")
         */
        void addImport(String importPath);
        
        /**
         * Get current cursor position.
         */
        int getCursorPosition();
        
        /**
         * Set cursor position.
         * @param position New cursor position
         */
        void setCursorPosition(int position);
        
        /**
         * Get current document text.
         */
        String getText();
        
        /**
         * Get cursor screen coordinates for menu positioning.
         */
        int[] getCursorScreenPosition();
        
        /**
         * Get viewport dimensions.
         */
        int[] getViewportDimensions();
    }
    
    // ==================== CONSTRUCTOR ====================
    
    public AutocompleteManager() {
        this.menu = new AutocompleteMenu();
        this.javaProvider = new JavaAutocompleteProvider();
        this.jsProvider = new JSAutocompleteProvider();
        
        // Set up menu callback
        menu.setCallback(new AutocompleteMenu.AutocompleteCallback() {
            @Override
            public void onItemSelected(AutocompleteItem item) {
                handleItemSelected(item);
            }
            
            @Override
            public void onDismiss() {
                active = false;
            }
        });
    }
    
    // ==================== CONFIGURATION ====================
    
    /**
     * Set the script container for type resolution.
     */
    public void setContainer(ScriptTextContainer container) {
        this.container = container;
        if (container != null) {
            this.document = container.getDocument();
            javaProvider.setDocument(document);
            jsProvider.setDocument(document);
        }
    }
    
    /**
     * Set the callback for text insertion.
     */
    public void setInsertCallback(InsertCallback callback) {
        this.insertCallback = callback;
    }
    
    // ==================== TRIGGER LOGIC ====================
    
    /**
     * Called when a character is typed.
     * Determines whether to show, update, or hide autocomplete.
     */
    public void onCharTyped(char c, String text, int cursorPosition) {
        // Check if we should close autocomplete
        if (CLOSE_CHARS.indexOf(c) >= 0) {
            dismiss();
            return;
        }
        
        // Check if this is a trigger character
        if (TRIGGER_CHARS.indexOf(c) >= 0) {
            // Trigger after dot
            triggerAfterDot(text, cursorPosition);
            return;
        }
        
        // Check if we're typing an identifier
        if (Character.isJavaIdentifierPart(c)) {
            if (active) {
                // Update existing autocomplete
                updatePrefix(text, cursorPosition);
            } else if (Character.isJavaIdentifierStart(c)) {
                // Check if we're after a dot with whitespace (e.g., "obj. |" where | is cursor)
                int dotPos = findDotBeforeWhitespace(text, cursorPosition - 1);
                if (dotPos >= 0) {
                    // We're typing after a dot (with possible whitespace), trigger member access
                    triggerAfterDot(text, cursorPosition);
                } else {
                    // Potentially start new autocomplete
                    maybeStartAutocomplete(text, cursorPosition, false);
                }
            }
            return;
        }
        
        // Any other character closes autocomplete
        if (active && !Character.isWhitespace(c)) {
            dismiss();
        }
    }
    
    /**
     * Called when backspace/delete is pressed.
     */
    public void onDeleteKey(String text, int cursorPosition) {
        if (active) {
            updatePrefix(text, cursorPosition);
            
            // Close if prefix is now empty and not after dot
            if (currentPrefix.isEmpty() && !isAfterDot(text, cursorPosition)) {
                dismiss();
            }
        }
    }

    /**
     * Called when cursor position changes (e.g., arrow keys).
     * Updates the autocomplete prefix based on the new cursor position.
     */
    public void onCursorMove(String text, int cursorPosition) {
        if (!active)
            return;

        // Check if we're still in a valid identifier position
        if (cursorPosition < 0 || cursorPosition > text.length()) {
            dismiss();
            return;
        }

        // Find the word boundaries at the current position
        int wordStart = cursorPosition;
        while (wordStart > 0 && Character.isJavaIdentifierPart(text.charAt(wordStart - 1))) {
            wordStart--;
        }

        int wordEnd = cursorPosition;
        while (wordEnd < text.length() && Character.isJavaIdentifierPart(text.charAt(wordEnd))) {
            wordEnd++;
        }

        // Check if we moved out of the current word
        if (cursorPosition < prefixStartPosition || cursorPosition > prefixStartPosition + currentPrefix.length()) {
            // Check if we're in a different word that we can autocomplete
            boolean isMemberAccess = wordStart > 0 && text.charAt(wordStart - 1) == '.';

            if (isMemberAccess || wordStart < cursorPosition) {
                // Update to the new word
                prefixStartPosition = wordStart;
                updatePrefix(text, cursorPosition);
            } else {
                // Moved to empty space or invalid position
                dismiss();
            }
        } else {
            // Still in same word, just update the prefix
            updatePrefix(text, cursorPosition);
        }
    }
    
    /**
     * Explicitly trigger autocomplete (Ctrl+Space).
     */
    public void triggerExplicit() {
        if (insertCallback == null) return;
        
        String text = insertCallback.getText();
        int cursorPosition = insertCallback.getCursorPosition();
        
        explicitTrigger = true;
        
        // Check if after dot
        if (isAfterDot(text, cursorPosition)) {
            triggerAfterDot(text, cursorPosition);
        } else {
            maybeStartAutocomplete(text, cursorPosition, true);
        }
    }
    
    /**
     * Trigger autocomplete after a dot is typed.
     */
    private void triggerAfterDot(String text, int cursorPosition) {
        // Find the receiver expression before the dot
        // First check if immediately before cursor
        int dotPos = cursorPosition - 1;
        if (dotPos < 0 || text.charAt(dotPos) != '.') {
            // Look backwards skipping whitespace to find dot
            dotPos = findDotBeforeWhitespace(text, cursorPosition - 1);
        }
        
        if (dotPos < 0) return;
        
        String receiverExpr = findReceiverExpression(text, dotPos);
        String prefix = findCurrentWord(text, cursorPosition);
        
        // Find where the prefix actually starts (after dot + any whitespace)
        int prefixStart = dotPos + 1;
        while (prefixStart < cursorPosition && Character.isWhitespace(text.charAt(prefixStart))) {
            prefixStart++;
        }
        
        prefixStartPosition = prefixStart;
        currentPrefix = prefix;
        
        showSuggestions(text, cursorPosition, prefix, prefixStartPosition, true, receiverExpr);
    }
    
    /**
     * Maybe start autocomplete based on current context.
     */
    private void maybeStartAutocomplete(String text, int cursorPosition, boolean force) {
        String prefix = findCurrentWord(text, cursorPosition);
        int prefixStart = cursorPosition - prefix.length();
        
        // Check minimum prefix length (unless forced)
        if (!force && prefix.length() < MIN_PREFIX_LENGTH) {
            return;
        }
        
        // Don't auto-trigger if we're in the middle of a word
        if (!force && prefixStart > 0 && Character.isJavaIdentifierPart(text.charAt(prefixStart - 1))) {
            return;
        }
        
        prefixStartPosition = prefixStart;
        currentPrefix = prefix;
        
        showSuggestions(text, cursorPosition, prefix, prefixStart, false, null);
    }
    
    /**
     * Update the prefix and refresh suggestions.
     */
    private void updatePrefix(String text, int cursorPosition) {
        // Re-calculate prefix from the original start position
        if (prefixStartPosition < 0 || prefixStartPosition > cursorPosition) {
            dismiss();
            return;
        }
        
        String newPrefix = text.substring(prefixStartPosition, cursorPosition);
        
        // Validate prefix (should be valid identifier or empty)
        if (!newPrefix.isEmpty() && !isValidPrefix(newPrefix)) {
            dismiss();
            return;
        }
        
        currentPrefix = newPrefix;
        
        // Check if after dot (skipping whitespace)
        int dotPos = findDotBeforeWhitespace(text, prefixStartPosition - 1);
        boolean isMemberAccess = dotPos >= 0;
        
        String receiverExpr = null;
        if (isMemberAccess) {
            receiverExpr = findReceiverExpression(text, dotPos);
        }
        
        showSuggestions(text, cursorPosition, currentPrefix, prefixStartPosition, 
            isMemberAccess, receiverExpr);
    }
    
    // ==================== SUGGESTION LOGIC ====================
    
    /**
     * Show autocomplete suggestions.
     */
    private void showSuggestions(String text, int cursorPosition, String prefix, 
                                  int prefixStart, boolean isMemberAccess, String receiverExpr) {
        if (insertCallback == null || document == null) return;
        
        // Track context for usage recording when item is selected
        currentIsMemberAccess = isMemberAccess;
        currentReceiverFullName = null;
        
        // Resolve receiver type if member access
        if (isMemberAccess && receiverExpr != null) {
            TypeInfo receiverType = document.resolveExpressionType(receiverExpr, prefixStart);
            if (receiverType != null && receiverType.isResolved()) {
                currentReceiverFullName = receiverType.getFullName();
            }
        }
        
        // Build context
        int lineNumber = getLineNumber(text, cursorPosition);
        String currentLine = getCurrentLine(text, cursorPosition);
        int columnPosition = getColumnPosition(text, cursorPosition);
        
        AutocompleteProvider.Context context = new AutocompleteProvider.Context(
            text, cursorPosition, lineNumber, columnPosition, currentLine,
            prefix, prefixStart, isMemberAccess, receiverExpr, explicitTrigger
        );
        
        // Get suggestions from appropriate provider
        AutocompleteProvider provider = document.isJavaScript() ? jsProvider : javaProvider;
        
        // No need to update variable types - JSAutocompleteProvider now gets them directly from ScriptDocument
        
        List<AutocompleteItem> suggestions = provider.getSuggestions(context);

        // Limit suggestions to prevent overwhelming the UI and improve performance
        // Items are already sorted by relevance in the provider
        if (suggestions.size() > MAX_SUGGESTIONS) {
            // Disabled for now to show all suggestions
            //  suggestions = suggestions.subList(0, MAX_SUGGESTIONS);
        }
        
        // Show menu
        if (suggestions.isEmpty()) {
            if (explicitTrigger) {
                // Show "No suggestions" message
                suggestions.add(new AutocompleteItem.Builder()
                    .name("No suggestions")
                    .kind(AutocompleteItem.Kind.SNIPPET)
                    .typeLabel("")
                    .build());
            } else {
                dismiss();
                return;
            }
        }
        
        // Get screen position for menu
        int[] screenPos = insertCallback.getCursorScreenPosition();
        int[] viewport = insertCallback.getViewportDimensions();
        
        menu.show(screenPos[0], screenPos[1] + 15, suggestions, viewport[0], viewport[1]);
        active = true;
        explicitTrigger = false;
    }
    
    // ==================== KEY HANDLING ====================
    
    /**
     * Handle key press.
     * @return true if the key was consumed
     */
    public boolean keyPressed(int keyCode) {
        if (!active || !menu.isVisible()) return false;
        
        switch (keyCode) {
            case 200: // UP
                menu.selectPrevious();
                return true;
                
            case 208: // DOWN
                menu.selectNext();
                return true;
                
            case 28:  // ENTER
            case 15:  // TAB
                if (menu.hasItems()) {
                    menu.confirmSelection();
                    return true;
                }
                return false;
                
            case 1:   // ESCAPE
                dismiss();
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handle mouse click.
     * @return true if the click was consumed
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!active) return false;
        return menu.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * Handle mouse scroll.
     * @return true if the scroll was consumed
     */
    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        if (!active) return false;
        return menu.mouseScrolled(mouseX, mouseY, delta);
    }

    /**
     * Handle mouse release.
     * @return true if the release was consumed
     */
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (!active)
            return false;
        return menu.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Handle mouse drag.
     * @return true if the drag was consumed
     */
    public boolean mouseDragged(int mouseX, int mouseY) {
        if (!active)
            return false;
        return menu.mouseDragged(mouseX, mouseY);
    }
    
    // ==================== ITEM SELECTION ====================
    
    /**
     * Handle when an item is selected from the menu.
     */
    private void handleItemSelected(AutocompleteItem item) {
        if (insertCallback == null || item == null) return;
        
        // Don't insert "No suggestions" placeholder
        if (item.getName().equals("No suggestions")) {
            return;
        }
        
        // Record usage for learning
        recordUsage(item);
        
        String insertText = item.getInsertText();
        
        // Smart tab completion: replace till next separator
        // Find the end position (current word till next separator)
        String text = insertCallback.getText();
        int cursorPos = insertCallback.getCursorPosition();
        int endPos = cursorPos;
        
        // Extend to consume rest of current word
        while (endPos < text.length() && Character.isJavaIdentifierPart(text.charAt(endPos))) {
            endPos++;
        }
        
        // Also consume following parentheses and their content if present
        if (endPos < text.length() && text.charAt(endPos) == '(') {
            int parenDepth = 1;
            endPos++; // Skip opening paren
            while (endPos < text.length() && parenDepth > 0) {
                char c = text.charAt(endPos);
                if (c == '(') parenDepth++;
                else if (c == ')') parenDepth--;
                endPos++;
            }
        }
        
        // IMPORTANT: Do the text replacement FIRST, then add import
        // This prevents position corruption since import adds text at the TOP
        insertCallback.replaceTextRange(insertText, prefixStartPosition, endPos);
        
        // If the inserted text ends with (), move cursor inside only when parameters exist
        if (insertText.endsWith("()") && item.getParameterCount() > 0) {
            int currentCursor = insertCallback.getCursorPosition();
            insertCallback.setCursorPosition(currentCursor - 1);
        }
        
        // If this item requires an import, add it AFTER the text replacement
        if (item.requiresImport() && item.getImportPath() != null) {
            insertCallback.addImport(item.getImportPath());
        }
        
        active = false;
    }
    
    /**
     * Record that the user selected an autocomplete item for usage tracking.
     */
    private void recordUsage(AutocompleteItem item) {
        if (document == null) return;
        
        UsageTracker tracker = document.isJavaScript() ? 
            UsageTracker.getJSInstance() : UsageTracker.getJavaInstance();
        
        // For member access, use the resolved receiver type
        // For standalone items (types, keywords, variables), owner is null
        String owner = currentIsMemberAccess ? currentReceiverFullName : null;
        
        tracker.recordUsage(item, owner);
    }
    
    // ==================== DISMISS ====================
    
    /**
     * Dismiss autocomplete.
     */
    public void dismiss() {
        if (active) {
            menu.hide();
            active = false;
            currentPrefix = "";
            prefixStartPosition = -1;
            explicitTrigger = false;
        }
    }
    
    // ==================== DRAWING ====================
    
    /**
     * Draw the autocomplete menu.
     */
    public void draw(int mouseX, int mouseY) {
        if (active) {
            menu.draw(mouseX, mouseY);

            if (Mouse.isButtonDown(0))
                mouseDragged(mouseX, mouseY);
            else 
                mouseReleased(mouseX, mouseY, 0);
        }
    }
    
    // ==================== STATE QUERIES ====================
    
    public boolean isActive() {
        return active;
    }
    
    public boolean isVisible() {
        return active && menu.isVisible();
    }
    
    public AutocompleteMenu getMenu() {
        return menu;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Find the word being typed at the cursor position.
     */
    private String findCurrentWord(String text, int cursorPos) {
        if (text == null || cursorPos <= 0) return "";
        
        int start = cursorPos;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        
        return text.substring(start, cursorPos);
    }
    
    /**
     * Find the receiver expression before a dot.
     */
    private String findReceiverExpression(String text, int dotPos) {
        if (text == null || dotPos <= 0) return "";
        
        int end = dotPos;
        int start = end;
        int parenDepth = 0;
        int bracketDepth = 0;
        
        // Walk backwards to find the start of the expression
        while (start > 0) {
            char c = text.charAt(start - 1);
            
            if (c == ')') {
                parenDepth++;
                start--;
            } else if (c == '(') {
                if (parenDepth > 0) {
                    parenDepth--;
                    start--;
                } else {
                    break;
                }
            } else if (c == ']') {
                bracketDepth++;
                start--;
            } else if (c == '[') {
                if (bracketDepth > 0) {
                    bracketDepth--;
                    start--;
                } else {
                    break;
                }
            } else if (c == '"' || c == '\'') {
                // Skip over string literals
                char quote = c;
                start--;
                while (start > 0) {
                    char strChar = text.charAt(start - 1);
                    if (strChar == quote && (start < 2 || text.charAt(start - 2) != '\\')) {
                        // Found unescaped closing quote
                        start--;
                        break;
                    }
                    start--;
                }
            } else if (Character.isJavaIdentifierPart(c) || c == '.') {
                start--;
            } else if (Character.isWhitespace(c)) {
                // Skip whitespace within expression (e.g., after method call)
                if (parenDepth > 0 || bracketDepth > 0) {
                    start--;
                } else {
                    break;
                }
            } else {
                // Within parentheses or brackets, allow any character (commas, operators, etc.)
                if (parenDepth > 0 || bracketDepth > 0) {
                    start--;
                } else {
                    break;
                }
            }
        }
        
        String expr = text.substring(start, end).trim();
        
        // Clean up the expression
        expr = expr.replaceAll("\\s+", "");
        
        return expr;
    }
    
    /**
     * Find dot position before cursor, skipping whitespace.
     * Returns -1 if no dot found.
     */
    private int findDotBeforeWhitespace(String text, int fromPos) {
        int pos = fromPos;
        // Skip backwards over whitespace

        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }

        // Check if we found a dot
        if (pos >= 0 && text.charAt(pos) == '.') {
            return pos;
        } else {
            while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos)))
                pos--;

            if (pos >= 0 && text.charAt(pos) == '.')
                return pos;
        }
        return -1;
    }
    
    /**
     * Check if cursor is after a dot.
     */
    private boolean isAfterDot(String text, int cursorPos) {
        // Look backwards, skipping any identifier characters
        int pos = cursorPos - 1;
        while (pos >= 0 && Character.isJavaIdentifierPart(text.charAt(pos))) {
            pos--;
        }
        // Also skip whitespace to check for dot
        while (pos >= 0 && Character.isWhitespace(text.charAt(pos))) {
            pos--;
        }
        return pos >= 0 && text.charAt(pos) == '.';
    }
    
    /**
     * Check if a prefix is valid (identifier characters only).
     */
    private boolean isValidPrefix(String prefix) {
        if (prefix.isEmpty()) return true;
        if (!Character.isJavaIdentifierStart(prefix.charAt(0))) return false;
        for (int i = 1; i < prefix.length(); i++) {
            if (!Character.isJavaIdentifierPart(prefix.charAt(i))) return false;
        }
        return true;
    }
    
    /**
     * Get line number (0-indexed) for a position.
     */
    private int getLineNumber(String text, int position) {
        int line = 0;
        for (int i = 0; i < position && i < text.length(); i++) {
            if (text.charAt(i) == '\n') line++;
        }
        return line;
    }
    
    /**
     * Get the current line text.
     */
    private String getCurrentLine(String text, int position) {
        int lineStart = text.lastIndexOf('\n', position - 1) + 1;
        int lineEnd = text.indexOf('\n', position);
        if (lineEnd < 0) lineEnd = text.length();
        return text.substring(lineStart, lineEnd);
    }
    
    /**
     * Get column position within the current line.
     */
    private int getColumnPosition(String text, int position) {
        int lineStart = text.lastIndexOf('\n', position - 1) + 1;
        return position - lineStart;
    }
}
