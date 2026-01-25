package noppes.npcs.client.gui.util.script.autocomplete;

import java.util.List;

/**
 * Abstract provider for autocomplete suggestions.
 * Implementations handle different contexts (after dot, in identifier, etc.)
 */
public interface AutocompleteProvider {
    
    /**
     * Context information for autocomplete requests.
     */
    class Context {
        /** Full text of the document */
        public final String text;
        /** Current cursor position in the document */
        public final int cursorPosition;
        /** The current line number (0-indexed) */
        public final int lineNumber;
        /** Position of cursor within the current line */
        public final int columnPosition;
        /** The current line text */
        public final String currentLine;
        /** The word being typed (prefix before cursor, after last separator) */
        public final String prefix;
        /** Start position of the prefix in the document */
        public final int prefixStart;
        /** Whether this is after a dot (member access) */
        public final boolean isMemberAccess;
        /** Expression before the dot (for member access) */
        public final String receiverExpression;
        /** Whether autocomplete was explicitly triggered (CTRL+Space) */
        public final boolean explicitTrigger;
        
        public Context(String text, int cursorPosition, int lineNumber, int columnPosition,
                       String currentLine, String prefix, int prefixStart,
                       boolean isMemberAccess, String receiverExpression, boolean explicitTrigger) {
            this.text = text;
            this.cursorPosition = cursorPosition;
            this.lineNumber = lineNumber;
            this.columnPosition = columnPosition;
            this.currentLine = currentLine;
            this.prefix = prefix;
            this.prefixStart = prefixStart;
            this.isMemberAccess = isMemberAccess;
            this.receiverExpression = receiverExpression;
            this.explicitTrigger = explicitTrigger;
        }
    }
    
    /**
     * Get autocomplete suggestions for the given context.
     * @param context The context containing cursor position, text, etc.
     * @return List of autocomplete items, sorted by relevance
     */
    List<AutocompleteItem> getSuggestions(Context context);
    
    /**
     * Check if this provider can handle the given context.
     */
    boolean canProvide(Context context);
}
