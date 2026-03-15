package noppes.npcs.api.handler;

/**
 * Defines metadata for a script hook, enabling rich hook registration with proper
 * type information for stub generation, imports, and documentation.
 *
 * <h3>Design Decision</h3>
 * Uses {@code String eventClassName} instead of {@code Class<?>} in the API to avoid
 * classloading issues with addons (addon classes may not be loaded when hooks are registered).
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Addon registration with full metadata
 * IScriptHookHandler hooks = NpcAPI.Instance().getScriptHooks();
 * hooks.registerHookDefinition("player", HookDefinition.builder("onDBCTransform")
 *     .eventClass("com.dbc.api.event.IDBCEvent$TransformEvent")
 *     .paramNames("event")
 *     .requiredImports("com.dbc.api.event.IDBCEvent")
 *     .cancelable(true)
 *     .build());
 * }</pre>
 */
public interface IHookDefinition {

    /**
     * The hook name (function name in scripts).
     *
     * @return Hook name, e.g., "questStart", "onDBCTransform"
     */
    String hookName();

    /**
     * Full qualified class name of the event type.
     * Uses '$' for nested classes (e.g., "noppes.npcs.api.event.IQuestEvent$QuestStartEvent").
     *
     * @return Fully qualified event class name, or null if unknown
     */
    String eventClassName();

    /**
     * Parameter names for stub generation.
     *
     * @return Array of parameter names, defaults to ["event"]
     */
    String[] paramNames();

    /**
     * Imports required for this hook's event type.
     * These will be added to the script's default imports.
     *
     * @return Array of fully qualified class/package names to import
     */
    String[] requiredImports();

    /**
     * Whether the event is cancelable (has @Cancelable annotation).
     *
     * @return true if event can be canceled
     */
    boolean isCancelable();

    /**
     * Lazily resolve the event class. Returns null if class is not available
     * (e.g., addon not loaded).
     *
     * @return The event class, or null if unavailable
     */
    default Class<?> getEventClass() {
        String className = eventClassName();
        if (className == null || className.isEmpty()) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Generate a usable type name for stub generation.
     * Handles nested classes by converting '$' to '.'.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>"noppes.npcs.api.event.IQuestEvent$QuestStartEvent" → "IQuestEvent.QuestStartEvent"</li>
     *   <li>"com.example.MyEvent" → "MyEvent"</li>
     * </ul>
     *
     * @return The usable type name for code generation, or null if no event class
     */
    default String getUsableTypeName() {
        String className = eventClassName();
        if (className == null || className.isEmpty()) {
            return null;
        }

        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return className.replace('$', '.');
        }

        String simplePart = className.substring(lastDot + 1);
        return simplePart.replace('$', '.');
    }
}
