/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

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
  * @javaFqn noppes.npcs.api.handler.IHookDefinition
*/
export interface IHookDefinition {
    /**
     * The hook name (function name in scripts).
     *
     * @return Hook name, e.g., "questStart", "onDBCTransform"
     */
    hookName(): String;
    /**
     * Full qualified class name of the event type.
     * Uses '$' for nested classes (e.g., "noppes.npcs.api.event.IQuestEvent$QuestStartEvent").
     *
     * @return Fully qualified event class name, or null if unknown
     */
    eventClassName(): String;
    /**
     * Parameter names for stub generation.
     *
     * @return Array of parameter names, defaults to ["event"]
     */
    paramNames(): String[];
    /**
     * Imports required for this hook's event type.
     * These will be added to the script's default imports.
     *
     * @return Array of fully qualified class/package names to import
     */
    requiredImports(): String[];
    /**
     * Whether the event is cancelable (has @Cancelable annotation).
     *
     * @return true if event can be canceled
     */
    isCancelable(): import('./boolean').boolean;
    /**
     * Lazily resolve the event class. Returns null if class is not available
     * (e.g., addon not loaded).
     *
     * @return The event class, or null if unavailable
     */
    getEventClass(): Class<any>;
    /**
     * Generate a usable type name for stub generation.
     * Handles nested classes by converting '$' to '.'.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>"noppes.npcs.api.event.IQuestEvent$QuestStartEvent" ? "IQuestEvent.QuestStartEvent"</li>
     *   <li>"com.example.MyEvent" ? "MyEvent"</li>
     * </ul>
     *
     * @return The usable type name for code generation, or null if no event class
     */
    getUsableTypeName(): String;
}
