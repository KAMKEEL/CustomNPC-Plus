package noppes.npcs.api.handler;

import java.util.List;

/**
 * Handler for registering and retrieving script hooks.
 * Addon mods can register custom hooks that will appear in the script editor GUIs.
 *
 * <h3>Available contexts:</h3>
 * <ul>
 *   <li>"npc" - NPC script editor</li>
 *   <li>"player" - Player scripts editor</li>
 *   <li>"block" - Scripted block editor</li>
 *   <li>"item" - Scripted item editor</li>
 *   <li>"linked_item" - Linked item editor</li>
 *   <li>"forge" - Forge scripts editor</li>
 *   <li>"global_npc" - Global NPC scripts editor</li>
 *   <li>"recipe" - Recipe scripts editor</li>
 *   <li>"effect" - Custom effect scripts editor</li>
 * </ul>
 *
 * <h3>Hook Registration</h3>
 * Use {@link #registerHookDefinition(String, IHookDefinition)} to register hooks:
 * <pre>{@code
 * hooks.registerHookDefinition("player",
 *     HookDefinition.of("onDBCTransform", IDBCEvent.TransformEvent.class));
 * }</pre>
 */
public interface IScriptHookHandler {

    /**
     * Register a hook with full metadata including event type and imports.
     *
     * @param context    The context identifier (e.g., "npc", "player", "block")
     * @param definition The hook definition containing all metadata
     */
    void registerHookDefinition(String context, IHookDefinition definition);

    /**
     * Get the hook definition for a specific hook.
     *
     * @param context  The context identifier
     * @param hookName The hook function name
     * @return The hook definition, or null if not found
     */
    IHookDefinition getHookDefinition(String context, String hookName);

    /**
     * Get all hook definitions for a context.
     *
     * @param context The context identifier
     * @return List of all hook definitions (may be empty, never null)
     */
    List<IHookDefinition> getAllHookDefinitions(String context);

    /**
     * Get all hook names for a context.
     *
     * @param context The context identifier
     * @return List of all hook function names
     */
    List<String> getAllHooks(String context);

    /**
     * Check if a hook is registered in a specific context.
     *
     * @param context  The context to check
     * @param hookName The function name to look for
     * @return true if the hook is registered
     */
    boolean hasHook(String context, String hookName);

    /**
     * Get the current hook revision number.
     * This increments each time a hook is registered, allowing caches to invalidate.
     *
     * @return The current revision number
     */
    int getHookRevision();

    /**
     * Get all available context identifiers.
     *
     * @return Array of context strings
     */
    String[] getContexts();
}
