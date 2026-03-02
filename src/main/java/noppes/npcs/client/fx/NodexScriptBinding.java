package noppes.npcs.client.fx;

import java.util.List;

/**
 * Bridge object that carries script tab data from the Minecraft thread to the JavaFX Nodex IDE.
 * All string data is copied at creation time to avoid cross-thread issues.
 */
public final class NodexScriptBinding {

    /** Display label for this binding group (e.g., "NPC Scripts", "Player Scripts") */
    public final String label;

    /** Script context identifier (e.g., "NPC", "PLAYER", "FORGE") */
    public final String contextId;

    /** Individual script tab data */
    public final List<ScriptTabData> tabs;

    /** Callback to save a specific tab back to the handler */
    public final SaveCallback saveCallback;

    /** Callback invoked when Nodex window closes - syncs all tab contents back to handler */
    public final CloseCallback closeCallback;

    /** Whether the user can add/delete tabs (true for Player/Forge, false for NPC) */
    public final boolean canModifyTabs;

    public NodexScriptBinding(String label, String contextId, List<ScriptTabData> tabs,
                              SaveCallback saveCallback, CloseCallback closeCallback, boolean canModifyTabs) {
        this.label = label;
        this.contextId = contextId;
        this.tabs = tabs;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;
        this.canModifyTabs = canModifyTabs;
    }

    public static final class ScriptTabData {
        public final int index;
        public final String name;
        public final String content;
        public final String language;

        public ScriptTabData(int index, String name, String content, String language) {
            this.index = index;
            this.name = name;
            this.content = content != null ? content : "";
            this.language = language != null ? language : "ECMAScript";
        }
    }

    /**
     * Callback interface for saving a virtual tab back to its handler.
     * Implementations must dispatch to the Minecraft main thread.
     */
    public interface SaveCallback {
        void onSave(int tabIndex, String newContent);
    }

    /**
     * Callback invoked when the Nodex window is closing.
     * Receives the complete list of tab snapshots (index + current content).
     * The handler should reconcile its script list with this data and refresh the in-game GUI.
     */
    public interface CloseCallback {
        void onClose(List<ScriptTabData> allTabs);
    }
}
