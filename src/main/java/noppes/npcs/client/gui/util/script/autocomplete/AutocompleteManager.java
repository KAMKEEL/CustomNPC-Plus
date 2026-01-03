package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.JavaTextContainer.LineData;
import noppes.npcs.client.gui.util.script.interpreter.ScriptDocument;
import noppes.npcs.client.gui.util.script.interpreter.ScriptTextContainer;

import java.util.ArrayList;
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
         * Get current cursor position.
         */
        int getCursorPosition();
        
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
                // Potentially start new autocomplete
                maybeStartAutocomplete(text, cursorPosition, false);
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
        int dotPos = cursorPosition - 1;
        if (dotPos < 0 || text.charAt(dotPos) != '.') {
            dotPos = text.lastIndexOf('.', cursorPosition - 1);
        }
        
        if (dotPos < 0) return;
        
        String receiverExpr = findReceiverExpression(text, dotPos);
        String prefix = findCurrentWord(text, cursorPosition);
        
        prefixStartPosition = dotPos + 1;
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
        
        // Check if after dot
        boolean isMemberAccess = prefixStartPosition > 0 && 
            text.charAt(prefixStartPosition - 1) == '.';
        
        String receiverExpr = null;
        if (isMemberAccess) {
            receiverExpr = findReceiverExpression(text, prefixStartPosition - 1);
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
        List<AutocompleteItem> suggestions = provider.getSuggestions(context);
        
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
        
        String insertText = item.getInsertText();
        insertCallback.insertText(insertText, prefixStartPosition);
        
        active = false;
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
                break;
            }
        }
        
        String expr = text.substring(start, end).trim();
        
        // Clean up the expression
        expr = expr.replaceAll("\\s+", "");
        
        return expr;
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
