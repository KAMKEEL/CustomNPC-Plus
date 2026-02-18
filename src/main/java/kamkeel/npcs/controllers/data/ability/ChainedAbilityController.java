package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.SyncController;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;

/**
 * Global registry and storage for {@link ChainedAbility} definitions.
 * Follows the same patterns as {@link AbilityController} for custom abilities:
 * JSON file storage, revision tracking for cache invalidation, and client sync.
 */
public class ChainedAbilityController {

    public static ChainedAbilityController Instance = new ChainedAbilityController();

    private final Map<String, ChainedAbility> chainedAbilities = new LinkedHashMap<>();

    /** Incremented on any change; used for cache invalidation. */
    private int revision = 0;

    // ═══════════════════════════════════════════════════════════════════
    // LOAD / SAVE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Load all chained abilities from disk.
     */
    public void load() {
        chainedAbilities.clear();

        File dir = getDir();
        File[] files = dir.exists() ? dir.listFiles() : null;
        if (files != null) {
            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".json")) continue;
                try {
                    String filename = file.getName();
                    String key = filename.substring(0, filename.length() - 5);

                    NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
                    ChainedAbility chain = new ChainedAbility();
                    chain.readNBT(nbt);

                    // Use filename as authoritative key
                    chain.setName(key);
                    chainedAbilities.put(key, chain);
                } catch (Exception e) {
                    LogWriter.error("Error loading chained ability: " + file.getAbsolutePath(), e);
                }
            }
        }

        revision++;
        LogWriter.info("Loaded " + chainedAbilities.size() + " chained abilities");
    }

    /**
     * Save a chained ability to disk. Handles renames and uniqueness.
     */
    public boolean save(ChainedAbility chain) {
        if (chain == null) return false;

        String name = chain.getName();
        if (name == null || name.isEmpty()) return false;

        File dir = getDir();
        File fileNew = new File(dir, name + ".json_new");
        File fileCurrent = new File(dir, name + ".json");

        try {
            NBTTagCompound nbt = chain.writeNBT();
            NBTJsonUtil.SaveFile(fileNew, nbt);

            if (fileCurrent.exists()) {
                fileCurrent.delete();
            }
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) {
                fileNew.delete();
            }

            chainedAbilities.put(name, chain);
            revision++;
            LogWriter.info("Saved chained ability: " + name);
            SyncController.syncAllChainedAbilities();
            return true;
        } catch (Exception e) {
            LogWriter.error("Error saving chained ability: " + name, e);
            return false;
        }
    }

    /**
     * Delete a chained ability by name.
     */
    public boolean delete(String name) {
        if (name == null || name.isEmpty()) return false;

        ChainedAbility removed = chainedAbilities.remove(name);
        if (removed == null) return false;

        File dir = getDir();
        File file = new File(dir, name + ".json");
        if (file.exists()) {
            file.delete();
        }

        revision++;
        LogWriter.info("Deleted chained ability: " + name);
        SyncController.syncAllChainedAbilities();
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // RESOLUTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Resolve a chained ability by name, returning a deep copy.
     * Tries exact match first, then case-insensitive.
     */
    public ChainedAbility resolve(String name) {
        if (name == null || name.isEmpty()) return null;

        // Exact match
        ChainedAbility chain = chainedAbilities.get(name);
        if (chain != null) return chain.deepCopy();

        // Case-insensitive match
        for (Map.Entry<String, ChainedAbility> entry : chainedAbilities.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue().deepCopy();
            }
        }

        return null;
    }

    /**
     * Check if a name can be resolved without creating a deep copy.
     */
    public boolean canResolve(String name) {
        if (name == null || name.isEmpty()) return false;

        if (chainedAbilities.containsKey(name)) return true;

        for (String key : chainedAbilities.keySet()) {
            if (key.equalsIgnoreCase(name)) return true;
        }

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════════════

    public ChainedAbility get(String name) {
        return chainedAbilities.get(name);
    }

    public Set<String> getNames() {
        return new LinkedHashSet<>(chainedAbilities.keySet());
    }

    public Map<String, ChainedAbility> getChainedAbilities() {
        return chainedAbilities;
    }

    public boolean hasName(String name) {
        return chainedAbilities.containsKey(name);
    }

    public int getRevision() {
        return revision;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CLIENT SYNC
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Set chained abilities from sync payload (client-side).
     */
    public void setChainedAbilities(Map<String, ChainedAbility> synced) {
        chainedAbilities.clear();
        chainedAbilities.putAll(synced);
        revision++;
    }

    // ═══════════════════════════════════════════════════════════════════
    // STORAGE
    // ═══════════════════════════════════════════════════════════════════

    private File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "chained_abilities");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
