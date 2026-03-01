package noppes.npcs.client.gui.util.script;

/**
 * Scope information for a variable
 */
public class ScopeInfo {
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
    
    public boolean isWithin(ScopeInfo other) {
        return this.startOffset >= other.startOffset && this.endOffset <= other.endOffset;
    }
}
