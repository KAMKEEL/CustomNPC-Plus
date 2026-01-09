package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.data.ability.type.*;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAbilityHandler;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

/**
 * Controller for ability types. Allows external mods to register custom ability types.
 * Initialize during FMLInitializationEvent.
 *
 * Also manages saved ability presets that can be reused across NPCs.
 */
public class AbilityController implements IAbilityHandler {

    public static AbilityController Instance;

    private final Map<String, Supplier<Ability>> factories = new LinkedHashMap<>();
    private final Map<String, Ability> savedAbilities = new LinkedHashMap<>();

    public AbilityController() {
        Instance = this;
        registerBuiltins();
    }

    /**
     * Load saved abilities from world directory.
     * Called when world loads.
     */
    public void load() {
        savedAbilities.clear();
        File dir = getDir();
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) continue;
            try {
                NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
                Ability ability = fromNBT(nbt);
                if (ability != null && ability.getName() != null && !ability.getName().isEmpty()) {
                    savedAbilities.put(ability.getName(), ability);
                    LogWriter.info("Loaded ability preset: " + ability.getName());
                }
            } catch (Exception e) {
                LogWriter.error("Error loading ability preset: " + file.getAbsolutePath(), e);
            }
        }
        LogWriter.info("Loaded " + savedAbilities.size() + " ability presets");
    }

    /**
     * Get the ability save directory.
     */
    private File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "abilities");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Save an ability preset to disk.
     *
     * @param ability The ability to save
     * @return true if successful
     */
    public boolean saveAbility(Ability ability) {
        if (ability == null || ability.getName() == null || ability.getName().isEmpty()) {
            return false;
        }

        File dir = getDir();
        String safeName = ability.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
        File fileNew = new File(dir, safeName + ".json_new");
        File fileCurrent = new File(dir, safeName + ".json");

        try {
            NBTTagCompound nbt = ability.writeNBT();
            NBTJsonUtil.SaveFile(fileNew, nbt);

            // Atomic rename
            if (fileCurrent.exists()) {
                fileCurrent.delete();
            }
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) {
                fileNew.delete();
            }

            savedAbilities.put(ability.getName(), ability);
            LogWriter.info("Saved ability preset: " + ability.getName());
            return true;
        } catch (Exception e) {
            LogWriter.error("Error saving ability preset: " + ability.getName(), e);
            return false;
        }
    }

    /**
     * Delete a saved ability preset.
     *
     * @param name The name of the ability to delete
     * @return true if successful
     */
    public boolean deleteAbility(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        Ability removed = savedAbilities.remove(name);
        if (removed == null) {
            return false;
        }

        File dir = getDir();
        String safeName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        File file = new File(dir, safeName + ".json");
        if (file.exists()) {
            file.delete();
        }

        LogWriter.info("Deleted ability preset: " + name);
        return true;
    }

    /**
     * Get a saved ability preset by name.
     *
     * @param name The name of the ability
     * @return A copy of the ability, or null if not found
     */
    public Ability getSavedAbility(String name) {
        Ability saved = savedAbilities.get(name);
        if (saved == null) {
            return null;
        }
        // Return a copy so modifications don't affect the saved version
        return fromNBT(saved.writeNBT());
    }

    /**
     * Check if a saved ability exists.
     */
    public boolean hasSavedAbility(String name) {
        return savedAbilities.containsKey(name);
    }

    /**
     * Get all saved ability names.
     */
    public Set<String> getSavedAbilityNames() {
        return new LinkedHashSet<>(savedAbilities.keySet());
    }

    /**
     * Get all saved abilities.
     */
    public Collection<Ability> getSavedAbilities() {
        return savedAbilities.values();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // IAbilityHandler INTERFACE IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public String[] getTypes() {
        return factories.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasType(String typeId) {
        return factories.containsKey(typeId);
    }

    @Override
    public String[] getSavedNames() {
        return savedAbilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean has(String name) {
        return savedAbilities.containsKey(name);
    }

    @Override
    public boolean delete(String name) {
        return deleteAbility(name);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE REGISTRATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Register a new ability type.
     * Call during FMLInitializationEvent.
     *
     * @param typeId The unique type ID (e.g., "cnpc:slam", "mymod:custom_ability")
     * @param factory Factory lambda that creates a new instance
     */
    public void registerType(String typeId, Supplier<Ability> factory) {
        if (factories.containsKey(typeId)) {
            LogWriter.info("AbilityController: Overwriting existing ability type: " + typeId);
        }
        factories.put(typeId, factory);
    }

    /**
     * Create an ability from NBT data.
     * Uses the "typeId" field to determine which type to create.
     */
    public Ability fromNBT(NBTTagCompound nbt) {
        if (nbt == null || !nbt.hasKey("typeId")) {
            return null;
        }

        String typeId = nbt.getString("typeId");
        Supplier<Ability> factory = factories.get(typeId);
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
     */
    public Ability create(String typeId) {
        Supplier<Ability> factory = factories.get(typeId);
        if (factory == null) {
            return null;
        }
        Ability ability = factory.get();
        ability.typeId = typeId;
        return ability;
    }

    /**
     * Register built-in ability types.
     */
    private void registerBuiltins() {
        // Melee/AOE damage abilities
        registerType("cnpc:slam", AbilitySlam::new);
        registerType("cnpc:heavy_hit", AbilityHeavyHit::new);
        registerType("cnpc:cutter", AbilityCutter::new);

        // Ranged/Projectile abilities
        registerType("cnpc:beam", AbilityBeam::new);
        registerType("cnpc:projectile", AbilityProjectile::new);
        registerType("cnpc:orb", AbilityOrb::new);

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
