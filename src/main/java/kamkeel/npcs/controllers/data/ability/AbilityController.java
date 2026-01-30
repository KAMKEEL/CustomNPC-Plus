package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilitySweeper;
import kamkeel.npcs.controllers.data.ability.type.AbilityCharge;
import kamkeel.npcs.controllers.data.ability.type.AbilityCutter;
import kamkeel.npcs.controllers.data.ability.type.AbilityDash;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import kamkeel.npcs.controllers.data.ability.type.AbilityHazard;
import kamkeel.npcs.controllers.data.ability.type.AbilityHeal;
import kamkeel.npcs.controllers.data.ability.type.AbilityHeavyHit;
import kamkeel.npcs.controllers.data.ability.type.AbilityOrb;
import kamkeel.npcs.controllers.data.ability.type.AbilityDisc;
import kamkeel.npcs.controllers.data.ability.type.AbilityLaserShot;
import kamkeel.npcs.controllers.data.ability.type.AbilityEnergyBeam;
import kamkeel.npcs.controllers.data.ability.type.AbilityProjectile;
import kamkeel.npcs.controllers.data.ability.type.AbilityShockwave;
import kamkeel.npcs.controllers.data.ability.type.AbilitySlam;
import kamkeel.npcs.controllers.data.ability.type.AbilityTeleport;
import kamkeel.npcs.controllers.data.ability.type.AbilityTrap;
import kamkeel.npcs.controllers.data.ability.type.AbilityVortex;
import kamkeel.npcs.controllers.SyncController;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAbilityHandler;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

/**
 * Central controller for ability types and presets.
 * <p>
 * Three maps:
 * <ul>
 *   <li>{@code typeFactories} — keyed by typeId (lang key), creates new instances of each ability type</li>
 *   <li>{@code abilities} — built-in pre-configured abilities, keyed by unique name</li>
 *   <li>{@code customAbilities} — user-created presets, keyed by UUID</li>
 * </ul>
 */
public class AbilityController implements IAbilityHandler {

    public static AbilityController Instance = new AbilityController();

    /** Type factories keyed by typeId (lang key, e.g. "ability.cnpc.slam"). */
    private final Map<String, Supplier<Ability>> typeFactories = new LinkedHashMap<>();

    /** Built-in pre-configured abilities keyed by unique name. */
    private final Map<String, Ability> abilities = new LinkedHashMap<>();

    /** User-created custom ability presets keyed by UUID. */
    private final Map<String, Ability> customAbilities = new LinkedHashMap<>();

    /** Version counter — incremented on any custom ability add/modify/delete.
     *  Used by AbilitySlot for cache invalidation. */
    private int version = 0;

    public AbilityController() {
        registerBuiltinTypes();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOADING / SAVING CUSTOM ABILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Load custom ability presets from world directory.
     * Called when world loads. Files are named {@code <uuid>.json}.
     */
    public void load() {
        customAbilities.clear();
        File dir = getDir();
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) continue;
            try {
                String filename = file.getName();
                String uuid = filename.substring(0, filename.length() - 5); // strip .json

                NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
                Ability ability = fromNBT(nbt);
                if (ability != null) {
                    ability.setId(uuid);
                    customAbilities.put(uuid, ability);
                }
            } catch (Exception e) {
                LogWriter.error("Error loading custom ability: " + file.getAbsolutePath(), e);
            }
        }
        LogWriter.info("Loaded " + customAbilities.size() + " custom abilities");
    }

    private File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "abilities");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Save a custom ability preset. If the ability has no id, generates a UUID.
     * Stores in customAbilities map and writes to disk.
     */
    public boolean saveCustomAbility(Ability ability) {
        if (ability == null) return false;

        String uuid = ability.getId();
        if (uuid == null || uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            ability.setId(uuid);
        }

        File dir = getDir();
        File fileNew = new File(dir, uuid + ".json_new");
        File fileCurrent = new File(dir, uuid + ".json");

        try {
            NBTTagCompound nbt = ability.writeNBT();
            NBTJsonUtil.SaveFile(fileNew, nbt);

            if (fileCurrent.exists()) {
                fileCurrent.delete();
            }
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) {
                fileNew.delete();
            }

            customAbilities.put(uuid, ability);
            version++;
            LogWriter.info("Saved custom ability: " + ability.getName() + " [" + uuid + "]");
            SyncController.syncAllCustomAbilities();
            return true;
        } catch (Exception e) {
            LogWriter.error("Error saving custom ability: " + uuid, e);
            return false;
        }
    }

    /**
     * Delete a custom ability preset by UUID.
     */
    public boolean deleteCustomAbility(String uuid) {
        if (uuid == null || uuid.isEmpty()) return false;

        Ability removed = customAbilities.remove(uuid);
        if (removed == null) return false;

        File dir = getDir();
        File file = new File(dir, uuid + ".json");
        if (file.exists()) {
            file.delete();
        }

        version++;
        LogWriter.info("Deleted custom ability: " + uuid);
        SyncController.syncAllCustomAbilities();
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUILT-IN ABILITY REGISTRY
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Register a pre-configured built-in ability by name.
     * These are looked up by name in {@link #resolveAbility(String)}.
     *
     * @param name    Unique name (e.g., "Jump Attack", "Ki Blast")
     * @param ability Pre-configured ability instance
     */
    public void registerAbility(String name, Ability ability) {
        if (abilities.containsKey(name)) {
            LogWriter.info("AbilityController: Overwriting built-in ability: " + name);
        }
        ability.setName(name);
        abilities.put(name, ability);
        LogWriter.info("Registered ability: " + name);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESOLUTION (used by AbilitySlot, PlayerAbilityData, etc.)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Resolve an ability by key. Checks built-in abilities first (by name),
     * then custom abilities (by UUID). Returns a copy safe to modify.
     *
     * @param key The ability name (built-in) or UUID (custom)
     * @return A copy of the resolved ability, or null if not found
     */
    public Ability resolveAbility(String key) {
        if (key == null || key.isEmpty()) return null;

        // Check built-in abilities first
        Ability builtIn = abilities.get(key);
        if (builtIn != null) {
            return fromNBT(builtIn.writeNBT());
        }

        // Check custom abilities
        Ability custom = customAbilities.get(key);
        if (custom != null) {
            return fromNBT(custom.writeNBT());
        }

        return null;
    }

    /**
     * Check if an ability key exists (either built-in or custom).
     */
    public boolean hasAbility(String key) {
        return abilities.containsKey(key) || customAbilities.containsKey(key);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ACCESSORS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Get a built-in ability by name. Returns the stored instance (not a copy). */
    public Ability getAbility(String name) {
        return abilities.get(name);
    }

    /** Get all built-in ability names. */
    public Set<String> getAbilityNames() {
        return new LinkedHashSet<>(abilities.keySet());
    }

    /** Get a custom ability by UUID. Returns the stored instance (not a copy). */
    public Ability getCustomAbility(String uuid) {
        return customAbilities.get(uuid);
    }

    /** Check if a custom ability exists by UUID. */
    public boolean hasCustomAbility(String uuid) {
        return customAbilities.containsKey(uuid);
    }

    /** Get all custom ability UUIDs. */
    public Set<String> getCustomAbilityIds() {
        return new LinkedHashSet<>(customAbilities.keySet());
    }

    /** Get the display name for a custom ability by UUID. */
    public String getCustomAbilityName(String uuid) {
        Ability a = customAbilities.get(uuid);
        return a != null ? a.getName() : null;
    }

    /** Get the custom abilities map (used by SyncController). */
    public Map<String, Ability> getCustomAbilities() {
        return customAbilities;
    }

    /** Replace the custom abilities map (used by client-side sync). */
    public void setCustomAbilities(Map<String, Ability> synced) {
        customAbilities.clear();
        customAbilities.putAll(synced);
        version++;
    }

    /** Get the version counter (incremented on custom ability changes). */
    public int getVersion() {
        return version;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // IAbilityHandler INTERFACE IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public String[] getTypes() {
        return typeFactories.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasType(String typeId) {
        return typeFactories.containsKey(typeId);
    }

    @Override
    public String[] getAbilityNameArray() {
        return abilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasAbilityName(String name) {
        return abilities.containsKey(name);
    }

    @Override
    public String[] getCustomAbilityIdArray() {
        return customAbilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasCustomAbilityId(String uuid) {
        return customAbilities.containsKey(uuid);
    }

    @Override
    public boolean deleteCustomAbilityById(String uuid) {
        return deleteCustomAbility(uuid);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE REGISTRATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Register a new ability type.
     * Extracts the typeId from a temporary instance and stores in typeFactories.
     *
     * @param factoryKey Unused legacy key — the typeId from the created instance is used
     * @param factory    Factory lambda that creates a new instance
     */
    public void registerType(String factoryKey, Supplier<Ability> factory) {
        Ability temp = factory.get();
        String typeId = temp.getTypeId();
        if (typeFactories.containsKey(typeId)) {
            LogWriter.info("AbilityController: Overwriting type: " + typeId);
        }
        typeFactories.put(typeId, factory);
    }

    /**
     * Create an ability from NBT data.
     * Uses the "typeId" field to look up in typeFactories.
     */
    public Ability fromNBT(NBTTagCompound nbt) {
        if (nbt == null || !nbt.hasKey("typeId")) {
            return null;
        }

        String typeId = nbt.getString("typeId");
        Supplier<Ability> factory = typeFactories.get(typeId);
        if (factory == null) {
            LogWriter.info("AbilityController: Unknown ability type: " + typeId);
            return null;
        }

        Ability ability = factory.get();
        ability.readNBT(nbt);
        return ability;
    }

    /**
     * Create a new empty ability of the given type.
     *
     * @param typeId The ability typeId (e.g., "ability.cnpc.slam")
     */
    public Ability create(String typeId) {
        Supplier<Ability> factory = typeFactories.get(typeId);
        if (factory == null) {
            return null;
        }
        return factory.get();
    }

    /**
     * Register built-in ability types.
     */
    private void registerBuiltinTypes() {
        // Melee/AOE damage abilities
        registerType("cnpc:slam", AbilitySlam::new);
        registerType("cnpc:heavy_hit", AbilityHeavyHit::new);
        registerType("cnpc:cutter", AbilityCutter::new);

        // Ranged/Projectile abilities
        registerType("cnpc:sweeper", AbilitySweeper::new);
        registerType("cnpc:projectile", AbilityProjectile::new);
        registerType("cnpc:orb", AbilityOrb::new);
        registerType("cnpc:disc", AbilityDisc::new);
        registerType("cnpc:laser_shot", AbilityLaserShot::new);
        registerType("cnpc:beam", AbilityEnergyBeam::new);

        // Movement abilities
        registerType("cnpc:charge", AbilityCharge::new);
        registerType("cnpc:dash", AbilityDash::new);
        registerType("cnpc:teleport", AbilityTeleport::new);
        registerType("cnpc:vortex", AbilityVortex::new);
        registerType("cnpc:shockwave", AbilityShockwave::new);

        // Defensive abilities
        registerType("cnpc:guard", AbilityGuard::new);
        registerType("cnpc:heal", AbilityHeal::new);

        // Zone/Trap abilities
        registerType("cnpc:hazard", AbilityHazard::new);
        registerType("cnpc:trap", AbilityTrap::new);
    }
}
