//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted;

public class CustomNPCsException extends RuntimeException {
    public CustomNPCsException(String message, Object... obs) {
        super(String.format(message, obs));
    }

    public CustomNPCsException(Exception ex, String message, Object... obs) {
        super(String.format(message, obs), ex);
    }
}
