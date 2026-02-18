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
 * JSON file storage in {@code abilities/chained/}, dual-index by name and UUID,
 * revision tracking for cache invalidation, and client sync.
 */
public class ChainedAbilityController {

    public static ChainedAbilityController Instance = new ChainedAbilityController();

    private final Map<String, ChainedAbility> chainedAbilities = new LinkedHashMap<>();      // name → ChainedAbility
    private final Map<String, ChainedAbility> chainedAbilitiesById = new LinkedHashMap<>();  // UUID → ChainedAbility

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
        chainedAbilitiesById.clear();
        int migrated = 0;

        File dir = getDir();

        // Legacy migration: move files from old chained_abilities/ directory
        File legacyDir = new File(CustomNpcs.getWorldSaveDirectory(), "chained_abilities");
        if (legacyDir.exists() && legacyDir.isDirectory()) {
            File[] legacyFiles = legacyDir.listFiles();
            if (legacyFiles != null) {
                for (File legacyFile : legacyFiles) {
                    if (!legacyFile.isFile() || !legacyFile.getName().endsWith(".json")) continue;
                    File dest = new File(dir, legacyFile.getName());
                    if (!dest.exists()) {
                        if (legacyFile.renameTo(dest)) {
                            LogWriter.info("Migrated chained ability file: chained_abilities/" + legacyFile.getName() + " -> abilities/chained/" + legacyFile.getName());
                            migrated++;
                        }
                    }
                }
                // Remove legacy directory if empty
                if (legacyDir.list() != null && legacyDir.list().length == 0) {
                    legacyDir.delete();
                }
            }
        }

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

                    // Ensure chain has a UUID; generate one for legacy entries
                    String uuid = chain.getId();
                    if (uuid == null || uuid.isEmpty() || uuid.equals(key)) {
                        uuid = UUID.randomUUID().toString();
                        chain.setId(uuid);
                        // Re-save file with the new UUID
                        try {
                            NBTJsonUtil.SaveFile(file, chain.writeNBT());
                        } catch (Exception ex) {
                            LogWriter.error("Failed to save UUID for chained ability: " + key, ex);
                        }
                        migrated++;
                    }

                    // Use filename as authoritative name key
                    chain.setName(key);
                    chainedAbilities.put(key, chain);
                    chainedAbilitiesById.put(uuid, chain);
                } catch (Exception e) {
                    LogWriter.error("Error loading chained ability: " + file.getAbsolutePath(), e);
                }
            }
        }

        revision++;
        if (migrated > 0) {
            LogWriter.info("Migrated " + migrated + " chained abilities (legacy dir or missing UUIDs)");
        }
        LogWriter.info("Loaded " + chainedAbilities.size() + " chained abilities");
    }

    /**
     * Save a chained ability to disk. Handles UUID assignment, renames, and uniqueness.
     */
    public boolean save(ChainedAbility chain) {
        if (chain == null) return false;

        String name = chain.getName();
        if (name == null || name.isEmpty()) return false;

        String uuid = chain.getId();
        boolean isNew = (uuid == null || uuid.isEmpty());

        if (isNew) {
            // Generate UUID for new chain
            uuid = UUID.randomUUID().toString();
            chain.setId(uuid);

            // Ensure unique name
            while (hasName(name)) {
                name = name + "_";
            }
            chain.setName(name);
        } else {
            // Existing chain — check for rename via UUID lookup
            ChainedAbility existing = chainedAbilitiesById.get(uuid);
            if (existing != null) {
                String oldName = existing.getName();
                if (!oldName.equals(name)) {
                    // Name changed! Ensure new name is unique (excluding self)
                    String testName = name;
                    while (chainedAbilities.containsKey(testName) && !testName.equals(oldName)) {
                        testName = testName + "_";
                    }
                    if (!testName.equals(name)) {
                        name = testName;
                        chain.setName(name);
                    }

                    // Delete old file and remove old map entry
                    File oldFile = new File(getDir(), oldName + ".json");
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    chainedAbilities.remove(oldName);
                }
            }
        }

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
            chainedAbilitiesById.put(uuid, chain);
            revision++;
            LogWriter.info("Saved chained ability: " + name + " [" + uuid + "]");
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

        // Also remove from UUID index
        String uuid = removed.getId();
        if (uuid != null && !uuid.isEmpty()) {
            chainedAbilitiesById.remove(uuid);
        }

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
     * Resolve a chained ability by key, returning a deep copy.
     * Checks UUID first, then exact name, then case-insensitive name.
     */
    public ChainedAbility resolve(String key) {
        if (key == null || key.isEmpty()) return null;

        // UUID lookup (most common for persistent references)
        ChainedAbility byId = chainedAbilitiesById.get(key);
        if (byId != null) return byId.deepCopy();

        // Exact name match
        ChainedAbility chain = chainedAbilities.get(key);
        if (chain != null) return chain.deepCopy();

        // Case-insensitive name match
        for (Map.Entry<String, ChainedAbility> entry : chainedAbilities.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue().deepCopy();
            }
        }

        return null;
    }

    /**
     * Check if a key can be resolved without creating a deep copy.
     */
    public boolean canResolve(String key) {
        if (key == null || key.isEmpty()) return false;

        if (chainedAbilitiesById.containsKey(key)) return true;

        if (chainedAbilities.containsKey(key)) return true;

        for (String name : chainedAbilities.keySet()) {
            if (name.equalsIgnoreCase(key)) return true;
        }

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════════════════

    public ChainedAbility get(String name) {
        return chainedAbilities.get(name);
    }

    public ChainedAbility getByUUID(String uuid) {
        return chainedAbilitiesById.get(uuid);
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

    public boolean hasKey(String key) {
        return chainedAbilities.containsKey(key) || chainedAbilitiesById.containsKey(key);
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
        chainedAbilitiesById.clear();
        chainedAbilities.putAll(synced);
        for (ChainedAbility chain : synced.values()) {
            String uuid = chain.getId();
            if (uuid != null && !uuid.isEmpty()) {
                chainedAbilitiesById.put(uuid, chain);
            }
        }
        revision++;
    }

    // ═══════════════════════════════════════════════════════════════════
    // STORAGE
    // ═══════════════════════════════════════════════════════════════════

    private File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "abilities" + File.separator + "chained");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
