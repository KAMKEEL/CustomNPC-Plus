package noppes.npcs.constants;

import noppes.npcs.api.event.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a script context that maps to one or more event interface namespaces.
 *
 * A script context can have multiple namespaces because script editors often
 * support hooks from multiple event types. For example, Player scripts support:
 * - IPlayerEvent (core player hooks)
 * - IAnimationEvent (animation hooks)
 * - IPartyEvent (party hooks)
 * - ICustomGuiEvent (custom GUI hooks)
 * - etc.
 *
 * Each context also has a hookContext string used by ScriptHookController for
 * hook registration and lookup.
 *
 * This is a registerable system - any mod can register their own contexts:
 *
 *   // Register a new context with multiple namespaces:
 *   ScriptContext.register("DBC", "dbc", IDBCEvent.class, IDBCPlayerEvent.class);
 *
 *   // Or add namespaces to an existing context:
 *   ScriptContext.PLAYER.addNamespace("IDBCPlayerEvent");
 */
public class ScriptContext {

    // Registry of all script contexts
    private static final Map<String, ScriptContext> REGISTRY = new ConcurrentHashMap<>();

    // ==================== BUILT-IN CONTEXTS ====================

    public static final ScriptContext NPC = register("NPC", "npc",
       INpcEvent.class,
       IProjectileEvent.class
    );

    public static final ScriptContext PLAYER = register("PLAYER", "player",
       IPlayerEvent.class,
       IAnimationEvent.class,
       IPartyEvent.class,
       IDialogEvent.class,
       IQuestEvent.class,
       IFactionEvent.class,
       ICustomGuiEvent.class
    );

    public static final ScriptContext BLOCK = register("BLOCK", "block",
       IBlockEvent.class
    );

    public static final ScriptContext ITEM = register("ITEM", "item",
       IItemEvent.class
    );

    public static final ScriptContext FORGE = register("FORGE", "forge",
       IForgeEvent.class
    );

    public static final ScriptContext LINKED_ITEM = register("LINKED_ITEM", "linked_item",
       IItemEvent.class
    );

    public static final ScriptContext RECIPE = register("RECIPE", "recipe",
       "Recipe"
    );

    public static final ScriptContext EFFECT = register("EFFECT", "effect",
       "Effect"
    );

    public static final ScriptContext GLOBAL = register("GLOBAL", "",
        "Global"  // Special case: Global namespace doesn't have a corresponding event class
    );

    // ==================== INSTANCE FIELDS ====================

    /** Unique identifier for this context (e.g., "NPC", "PLAYER", "DBC") */
    public final String id;

    /** Hook context identifier used by ScriptHookController (e.g., "npc", "player") */
    public final String hookContext;

    /** The event interface namespaces this context supports */
    private final List<String> namespaces;

    // ==================== CONSTRUCTOR ====================

    private ScriptContext(String id, String hookContext, String... namespaces) {
        this.id = id;
        this.hookContext = hookContext != null ? hookContext : "";
        this.namespaces = new ArrayList<>(Arrays.asList(namespaces));
    }

    // ==================== NAMESPACE ACCESS ====================

    /**
     * Get all namespaces this context supports.
     *
     * @return Unmodifiable list of namespace strings
     */
    public List<String> getNamespaces() {
        return Collections.unmodifiableList(namespaces);
    }

    /**
     * Check if this context includes a specific namespace.
     *
     * @param namespace The namespace to check (e.g., "IPlayerEvent")
     * @return true if this context supports hooks from that namespace
     */
    public boolean hasNamespace(String namespace) {
        return namespaces.contains(namespace);
    }

    /**
     * Add a namespace to this context.
     * Mods can use this to extend built-in contexts with their own event types.
     *
     * @param namespace The namespace to add (e.g., "IDBCPlayerEvent")
     */
    public void addNamespace(String namespace) {
        if (!namespaces.contains(namespace)) {
            namespaces.add(namespace);
        }
    }

    /**
     * Get the primary namespace (first one registered).
     * This is mainly for backward compatibility.
     *
     * @return The first namespace, or "Global" if none
     */
    public String getPrimaryNamespace() {
        return namespaces.isEmpty() ? "Global" : namespaces.get(0);
    }

    // ==================== REGISTRATION API ====================

    /**
     * Register a new script context with a hook context and one or more namespaces.
     *
     * @param id Unique identifier (e.g., "DBC", "CUSTOM")
     * @param hookContext The hook context string for ScriptHookController (e.g., "dbc")
     * @param namespaces The event interface names (e.g., "IDBCEvent", "IDBCPlayerEvent")
     * @return The registered ScriptContext
     */
    public static ScriptContext register(String id, String hookContext, String... namespaces) {
        ScriptContext context = new ScriptContext(id, hookContext, namespaces);
        REGISTRY.put(id, context);
        return context;
    }

    /**
     * Register a new script context using event interface classes.
     * Automatically extracts the simple name from each class.
     *
     * Example:
     *   ScriptContext.register("NPC", "npc", INpcEvent.class, IProjectileEvent.class);
     *
     * @param id Unique identifier (e.g., "DBC", "CUSTOM")
     * @param hookContext The hook context string for ScriptHookController (e.g., "npc")
     * @param eventClasses The event interface classes (simple names will be extracted)
     * @return The registered ScriptContext
     */
    public static ScriptContext register(String id, String hookContext, Class<?>... eventClasses) {
        String[] namespaces = new String[eventClasses.length];
        for (int i = 0; i < eventClasses.length; i++) {
            namespaces[i] = eventClasses[i].getSimpleName();
        }
        return register(id, hookContext, namespaces);
    }

    /**
     * Get a script context by its ID.
     *
     * @param id The context ID (e.g., "NPC", "PLAYER", "DBC")
     * @return The ScriptContext, or GLOBAL if not found
     */
    public static ScriptContext byId(String id) {
        return REGISTRY.getOrDefault(id, GLOBAL);
    }

    /**
     * Find a script context that contains a specific namespace.
     *
     * @param namespace The event interface namespace (e.g., "INpcEvent")
     * @return The first ScriptContext that contains this namespace, or GLOBAL if not found
     */
    public static ScriptContext byNamespace(String namespace) {
        if (namespace == null) return GLOBAL;
        for (ScriptContext ctx : REGISTRY.values()) {
            if (ctx.hasNamespace(namespace)) {
                return ctx;
            }
        }
        return GLOBAL;
    }

    /**
     * Find a script context by its hook context string.
     *
     * @param hookContext The hook context string (e.g., "npc", "player")
     * @return The ScriptContext with that hook context, or GLOBAL if not found
     */
    public static ScriptContext byHookContext(String hookContext) {
        if (hookContext == null || hookContext.isEmpty()) return GLOBAL;
        for (ScriptContext ctx : REGISTRY.values()) {
            if (hookContext.equals(ctx.hookContext)) {
                return ctx;
            }
        }
        return GLOBAL;
    }

    /**
     * Get all registered script contexts.
     *
     * @return Unmodifiable collection of all registered contexts
     */
    public static Collection<ScriptContext> values() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Check if a context with the given ID exists.
     *
     * @param id The context ID to check
     * @return true if registered
     */
    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    // ==================== OBJECT METHODS ====================

    @Override
    public String toString() {
        return id + " -> " + namespaces;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScriptContext)) return false;
        ScriptContext other = (ScriptContext) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
