package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.gui.util.script.interpreter.token.Token;

/**
 * Centralized error representation for the script document.
 * Captures all validation errors (method calls, assignments, method declarations, type declarations)
 * into a single list for generic processing.
 *
 * <p>Each error tracks:
 * <ul>
 *   <li>{@code token} - the associated token (may be null for span-based errors)</li>
 *   <li>{@code startPos} - global start offset of the error span</li>
 *   <li>{@code endPos} - global end offset of the error span</li>
 *   <li>{@code message} - human-readable error description</li>
 * </ul>
 */
public class DocumentError {

    private final Token token;
    private final int startPos;
    private final int endPos;
    private final String message;

    /**
     * Create a new document error.
     *
     * @param token    The associated token, or null if this error is span-based
     * @param startPos Global start offset of the error span
     * @param endPos   Global end offset of the error span
     * @param message  Human-readable error description
     */
    public DocumentError(Token token, int startPos, int endPos, String message) {
        this.token = token;
        this.startPos = startPos;
        this.endPos = endPos;
        this.message = message;
    }
    
    public DocumentError(int startPos, int endPos, String message) {
        this.token = null;
        this.startPos = startPos;
        this.endPos = endPos;
        this.message = message;
    }

    public Token getToken() {
        return token;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DocumentError{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                ", message='" + message + '\'' +
                ", token=" + (token != null ? token.getText() : "null") +
                '}';
    }
}
