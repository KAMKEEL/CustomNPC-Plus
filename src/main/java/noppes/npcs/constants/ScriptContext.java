package noppes.npcs.constants;

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
 * This is a registerable system - any mod can register their own contexts:
 *
 *   // Register a new context with multiple namespaces:
 *   ScriptContext.register("DBC", "IDBCEvent", "IDBCPlayerEvent", "IDBCFormEvent");
 *
 *   // Or add namespaces to an existing context:
 *   ScriptContext.PLAYER.addNamespace("IDBCPlayerEvent");
 */
public class ScriptContext {

    // Registry of all script contexts
    private static final Map<String, ScriptContext> REGISTRY = new ConcurrentHashMap<>();

    // ==================== BUILT-IN CONTEXTS ====================

    public static final ScriptContext NPC = register("NPC",
        "INpcEvent",
        "IProjectileEvent"
    );

    public static final ScriptContext PLAYER = register("PLAYER",
        "IPlayerEvent",
        "IAnimationEvent",
        "IPartyEvent",
        "IDialogEvent",
        "IQuestEvent",
        "IFactionEvent",
        "ICustomGuiEvent",
        "IEffectEvent"
    );

    public static final ScriptContext BLOCK = register("BLOCK",
        "IBlockEvent"
    );

    public static final ScriptContext ITEM = register("ITEM",
        "IItemEvent"
    );

    public static final ScriptContext FORGE = register("FORGE",
        "IForgeEvent"
    );

    public static final ScriptContext GLOBAL = register("GLOBAL",
        "Global"
    );

    // ==================== INSTANCE FIELDS ====================

    /** Unique identifier for this context (e.g., "NPC", "PLAYER", "DBC") */
    public final String id;

    /** The event interface namespaces this context supports */
    private final List<String> namespaces;

    // ==================== CONSTRUCTOR ====================

    private ScriptContext(String id, String... namespaces) {
        this.id = id;
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
     * Register a new script context with one or more namespaces.
     *
     * @param id Unique identifier (e.g., "DBC", "CUSTOM")
     * @param namespaces The event interface names (e.g., "IDBCEvent", "IDBCPlayerEvent")
     * @return The registered ScriptContext
     */
    public static ScriptContext register(String id, String... namespaces) {
        ScriptContext context = new ScriptContext(id, namespaces);
        REGISTRY.put(id, context);
        return context;
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
