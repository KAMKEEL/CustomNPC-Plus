package noppes.npcs.client.gui.util.script.autocomplete;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import noppes.npcs.CustomNpcs;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks user autocomplete selections to improve suggestion ranking.
 * Usage counts are persisted to JSON and used to boost frequently selected items.
 * 
 * Key format: "ownerFullName|memberName|KIND"
 * - ownerFullName: Full class name for members, empty for standalone items
 * - memberName: The item name (method, field, type, etc.)
 * - KIND: The item kind (METHOD, FIELD, CLASS, ENUM, etc.)
 * 
 * Examples:
 * - "net.minecraft.client.Minecraft|getMinecraft|METHOD"
 * - "net.minecraft.entity.player.EntityPlayer|inventory|FIELD"
 * - "|EntityPlayer|CLASS" (standalone type suggestion)
 * - "|if|KEYWORD"
 */
public class UsageTracker {
    
    private static final String JAVA_FILE = "java_usages.json";
    private static final String JS_FILE = "js_usages.json";
    private static final long SAVE_INTERVAL_MS = 60_000; // Auto-save every 60 seconds
    private static final int USAGE_SCORE_MULTIPLIER = 50; // Score boost per usage
    private static final int MAX_USAGE_BOOST = 5000; // Cap the boost to prevent runaway scores
    
    private static UsageTracker javaInstance;
    private static UsageTracker jsInstance;
    private static boolean initialized = false;
    
    private final Map<String, Integer> usageCounts = new ConcurrentHashMap<>();
    private final File file;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    
    private static ScheduledExecutorService scheduler;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // ==================== SINGLETON ACCESS ====================
    
    public static synchronized UsageTracker getJavaInstance() {
        ensureInitialized();
        if (javaInstance == null) {
            javaInstance = new UsageTracker(JAVA_FILE);
            javaInstance.load();
        }
        return javaInstance;
    }
    
    public static synchronized UsageTracker getJSInstance() {
        ensureInitialized();
        if (jsInstance == null) {
            jsInstance = new UsageTracker(JS_FILE);
            jsInstance.load();
        }
        return jsInstance;
    }
    
    /**
     * Ensure the tracker system is initialized with auto-save and shutdown hook.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            initialized = true;
            initialize();
            
            // Add shutdown hook to save on JVM exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdown();
            }, "UsageTracker-Shutdown"));
        }
    }
    
    /**
     * Initialize the auto-save scheduler. Call once on client startup.
     */
    public static void initialize() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "UsageTracker-AutoSave");
                t.setDaemon(true);
                return t;
            });
             
            scheduler.scheduleAtFixedRate(() -> {
                saveAllIfDirty();
            }, SAVE_INTERVAL_MS, SAVE_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Shutdown and save all data. Call on client shutdown.
     */
    public static void shutdown() {
        saveAllIfDirty();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
    
    private static void saveAllIfDirty() {
        if (javaInstance != null) {
            javaInstance.saveIfDirty();
        }
        if (jsInstance != null) {
            jsInstance.saveIfDirty();
        }
    }
    
    // ==================== INSTANCE METHODS ====================
    
    private UsageTracker(String filename) {
        File dir = getDir();
        this.file = new File(dir, filename);
    }
    
    private File getDir() {
        File dir = new File(CustomNpcs.Dir, "tracked_usages");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }
    
    /**
     * Record that the user selected an autocomplete item.
     * 
     * @param owner The owner context (full class name for members, null/empty for standalone)
     * @param name The item name
     * @param kind The item kind
     */
    public void recordUsage(String owner, String name, AutocompleteItem.Kind kind) {
        String key = buildKey(owner, name, kind);
        usageCounts.merge(key, 1, Integer::sum);
        dirty.set(true);
    }
    
    /**
     * Record usage directly from an AutocompleteItem.
     * 
     * @param item The selected autocomplete item
     * @param ownerFullName The full class name of the owner (for member access), or null
     */
    public void recordUsage(AutocompleteItem item, String ownerFullName) {
        String name = item.getSearchName() != null ? item.getSearchName() : item.getName();
        
        // For methods, include parameter count to distinguish overloads
        if (item.getKind() == AutocompleteItem.Kind.METHOD) {
            int paramCount = item.getParameterCount();
            name = name + "(" + paramCount + ")";
        }
        
        recordUsage(ownerFullName, name, item.getKind());
    }
    
    /**
     * Get the usage count for an item.
     * 
     * @param owner The owner context
     * @param name The item name
     * @param kind The item kind
     * @return The number of times this item was selected
     */
    public int getUsageCount(String owner, String name, AutocompleteItem.Kind kind) {
        String key = buildKey(owner, name, kind);
        return usageCounts.getOrDefault(key, 0);
    }
    
    /**
     * Get usage count directly from an AutocompleteItem.
     */
    public int getUsageCount(AutocompleteItem item, String ownerFullName) {
        String name = item.getSearchName() != null ? item.getSearchName() : item.getName();
        
        // For methods, include parameter count to distinguish overloads
        if (item.getKind() == AutocompleteItem.Kind.METHOD) {
            int paramCount = item.getParameterCount();
            name = name + "(" + paramCount + ")";
        }
        
        return getUsageCount(ownerFullName, name, item.getKind());
    }
    
    /**
     * Calculate the score boost based on usage count.
     * Uses a logarithmic scale to prevent very frequent items from dominating completely.
     * 
     * @param usageCount The number of times the item was selected
     * @return The score boost to add
     */
    public static int calculateUsageBoost(int usageCount) {
        if (usageCount <= 0) return 0;
        
        // Logarithmic scaling: log2(count + 1) * multiplier
        // This gives diminishing returns for very high counts
        int boost = (int) (Math.log(usageCount + 1) / Math.log(2) * USAGE_SCORE_MULTIPLIER);
        return Math.min(boost, MAX_USAGE_BOOST);
    }
    
    /**
     * Build a unique key for an item.
     */
    public static String buildKey(String owner, String name, AutocompleteItem.Kind kind) {
        String ownerPart = owner != null ? owner : "";
        String kindPart = kind != null ? kind.name() : "UNKNOWN";
        return ownerPart + "|" + name + "|" + kindPart;
    }
    
    // ==================== PERSISTENCE ====================
    
    /**
     * Load usage data from file.
     */
    public void load() {
        if (loaded.get()) return;
        
        if (!file.exists()) {
            loaded.set(true);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            Map<String, Integer> data = GSON.fromJson(reader, type);
            if (data != null) {
                usageCounts.putAll(data);
            }
            loaded.set(true);
        } catch (Exception e) {
            System.err.println("[UsageTracker] Failed to load " + file.getName() + ": " + e.getMessage());
            loaded.set(true); // Mark as loaded even on failure to prevent retry loops
        }
    }
    
    /**
     * Save usage data to file if there are pending changes.
     */
    public void saveIfDirty() {
        if (!dirty.compareAndSet(true, false)) {
            return; // Not dirty, nothing to save
        }
        save();
    }
    
    /**
     * Force save usage data to file.
     */
    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            GSON.toJson(usageCounts, writer);
        } catch (Exception e) {
            System.err.println("[UsageTracker] Failed to save " + file.getName() + ": " + e.getMessage());
            dirty.set(true); // Mark dirty again so we retry later
        }
    }
    
    /**
     * Clear all usage data (for testing/reset purposes).
     */
    public void clear() {
        usageCounts.clear();
        dirty.set(true);
    }
    
    /**
     * Get the number of tracked items.
     */
    public int size() {
        return usageCounts.size();
    }
}
