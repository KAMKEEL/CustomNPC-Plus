package noppes.npcs.client.gui.util.script.interpreter.token;

public class TokenErrorMessage {
    private final String message;
    public boolean clearOtherErrors;

    public TokenErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static TokenErrorMessage from(String message) {
        return new TokenErrorMessage(message);
    }
    
    public TokenErrorMessage clearOtherErrors() {
        this.clearOtherErrors = true;
        return this;
    }
}
