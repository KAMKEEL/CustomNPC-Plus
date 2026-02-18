package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.data.ability.type.*;
import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityBeam;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityDisc;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityDome;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityLaserShot;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityOrb;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityShield;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilitySlicer;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilitySweeper;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityWall;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAbilityHandler;
import noppes.npcs.util.NBTJsonUtil;

import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class AbilityController implements IAbilityHandler {

    public static AbilityController Instance = new AbilityController();

    // ── Core Registries ──────────────────────────────────────────────────────
    private final Map<String, Supplier<Ability>> abilityTypes = new LinkedHashMap<>();
    private final Map<String, Ability> builtAbilities = new LinkedHashMap<>();
    private final Map<String, Ability> customAbilities = new LinkedHashMap<>();      // name → Ability
    private final Map<String, Ability> customAbilitiesById = new LinkedHashMap<>();  // UUID → Ability

    // ── Extension Points ─────────────────────────────────────────────────────
    private final Map<String, List<AbilityVariant>> externalVariants = new LinkedHashMap<>();
    private final List<IAbilityFieldProvider> fieldProviders = new ArrayList<>();
    private final List<IAbilityExtender> extenders = new ArrayList<>();
    private final List<Predicate<EntityPlayer>> flightCheckers = new ArrayList<>();

    // ── Legacy Migration ─────────────────────────────────────────────────────
    private static final Pattern UUID_PATTERN =
        Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            Pattern.CASE_INSENSITIVE);

    // ── Derived State ────────────────────────────────────────────────────────
    private final Set<String> builtInTypeIds = new HashSet<>();

    public AbilityController() {
        registerBuiltinTypes();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE REGISTRATION
    // ═══════════════════════════════════════════════════════════════════════════

    public void registerType(String factoryKey, Supplier<Ability> factory) {
        Ability temp = factory.get();
        String typeId = temp.getTypeId();
        if (abilityTypes.containsKey(typeId)) {
            LogWriter.info("AbilityController: Overwriting type: " + typeId);
        }
        abilityTypes.put(typeId, factory);
        if (temp.isBuiltIn()) {
            builtInTypeIds.add(typeId);
        }
    }

    public Ability create(String typeId) {
        Supplier<Ability> factory = abilityTypes.get(typeId);
        return factory != null ? factory.get() : null;
    }

    public Ability fromNBT(NBTTagCompound nbt) {
        if (nbt == null || !nbt.hasKey("typeId")) {
            return null;
        }

        String typeId = nbt.getString("typeId");
        Supplier<Ability> factory = abilityTypes.get(typeId);
        if (factory == null) {
            LogWriter.info("AbilityController: Unknown ability type: " + typeId);
            return null;
        }

        Ability ability = factory.get();
        ability.readNBT(nbt);
        return ability;
    }

    private void registerBuiltinTypes() {
        registerType("cnpc:slam", AbilitySlam::new);
        registerType("cnpc:heavy_hit", AbilityHeavyHit::new);
        registerType("cnpc:cutter", AbilityCutter::new);

        registerType("cnpc:sweeper", AbilitySweeper::new);
        registerType("cnpc:projectile", AbilityProjectile::new);
        registerType("cnpc:orb", AbilityOrb::new);
        registerType("cnpc:disc", AbilityDisc::new);
        registerType("cnpc:laser_shot", AbilityLaserShot::new);
        registerType("cnpc:beam", AbilityBeam::new);

        registerType("cnpc:charge", AbilityCharge::new);
        registerType("cnpc:dash", AbilityDash::new);
        registerType("cnpc:teleport", AbilityTeleport::new);
        registerType("cnpc:vortex", AbilityVortex::new);
        registerType("cnpc:shockwave", AbilityShockwave::new);

        registerType("cnpc:guard", AbilityGuard::new);
        registerType("cnpc:heal", AbilityHeal::new);

        registerType("cnpc:hazard", AbilityHazard::new);
        registerType("cnpc:trap", AbilityTrap::new);

        registerType("cnpc:dome", AbilityDome::new);
        registerType("cnpc:wall", AbilityWall::new);
        registerType("cnpc:shield", AbilityShield::new);
        registerType("cnpc:slicer", AbilitySlicer::new);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUILT-IN ABILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    public void registerAbility(String name, Ability ability) {
        if (builtAbilities.containsKey(name)) {
            LogWriter.info("AbilityController: Overwriting built-in ability: " + name);
        }
        if (!ability.isBuiltIn()) {
            ability.setName(name);
        }
        builtAbilities.put(name, ability);
        LogWriter.info("Registered ability: " + name);
    }

    public Ability getAbility(String name) {
        return builtAbilities.get(name);
    }

    public Ability getAbilityByDisplayName(String displayName) {
        for (Ability ability : builtAbilities.values()) {
            if (ability.getName().equals(displayName)) {
                return ability;
            }
        }
        return null;
    }

    public Set<String> getAbilityNames() {
        return new LinkedHashSet<>(builtAbilities.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM ABILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    // Incremented on any custom ability change; used by AbilitySlot for cache invalidation
    private int customAbilityRevision = 0;

    public void load() {
        customAbilities.clear();
        customAbilitiesById.clear();
        int migrated = 0;

        File dir = getDir();
        File[] files = dir.exists() ? dir.listFiles() : null;
        if (files != null) {
            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".json")) continue;
                try {
                    String filename = file.getName();
                    String key = filename.substring(0, filename.length() - 5);

                    NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
                    Ability ability = fromNBT(nbt);
                    if (ability == null) continue;

                    // Skip built-in abilities that were erroneously saved as custom files
                    if (ability.isBuiltIn()) {
                        file.delete();
                        LogWriter.info("Removed stale built-in ability file: " + filename);
                        continue;
                    }

                    // Legacy migration: UUID-named files get renamed to name-based
                    if (UUID_PATTERN.matcher(key).matches()) {
                        String name = ability.getName();
                        if (name == null || name.isEmpty()) {
                            name = key; // Fallback to UUID if no name stored
                        }

                        // Ensure unique name
                        while (hasCustomAbilityName(name) || nameFileExists(dir, name)) {
                            name = name + "_";
                        }
                        ability.setName(name);

                        // Rename file from UUID.json to name.json
                        File newFile = new File(dir, name + ".json");
                        if (file.renameTo(newFile)) {
                            LogWriter.info("Migrated ability file: " + key + ".json -> " + name + ".json");
                        } else {
                            LogWriter.error("Failed to rename ability file: " + key + ".json -> " + name + ".json");
                        }

                        key = name;
                        migrated++;
                    }

                    // Ensure ability has a UUID; generate one for legacy abilities
                    String uuid = ability.getId();
                    if (uuid == null || uuid.isEmpty() || uuid.equals(key)) {
                        // Legacy: id was the name or empty — generate a real UUID
                        uuid = UUID.randomUUID().toString();
                        ability.setId(uuid);
                        // Re-save file with the new UUID
                        try {
                            NBTJsonUtil.SaveFile(file, ability.writeNBT());
                        } catch (Exception ex) {
                            LogWriter.error("Failed to save UUID for ability: " + key, ex);
                        }
                        migrated++;
                    }

                    ability.setName(key);
                    customAbilities.put(key, ability);
                    customAbilitiesById.put(uuid, ability);
                } catch (Exception e) {
                    LogWriter.error("Error loading custom ability: " + file.getAbsolutePath(), e);
                }
            }
        }

        customAbilityRevision++;
        if (migrated > 0) {
            LogWriter.info("Migrated " + migrated + " abilities (UUID-named files or missing UUIDs)");
        }
        LogWriter.info("Loaded " + customAbilities.size() + " custom abilities");
    }

    private boolean nameFileExists(File dir, String name) {
        return new File(dir, name + ".json").exists();
    }

    public boolean saveCustomAbility(Ability ability) {
        if (ability == null) return false;
        if (ability.isBuiltIn()) return false;

        String name = ability.getName();
        if (name == null || name.isEmpty()) return false;

        String uuid = ability.getId();
        boolean isNew = (uuid == null || uuid.isEmpty());

        if (isNew) {
            // Generate UUID for new ability
            uuid = UUID.randomUUID().toString();
            ability.setId(uuid);

            // Ensure unique name
            while (hasCustomAbilityName(name)) {
                name = name + "_";
            }
            ability.setName(name);
        } else {
            // Existing ability — check for rename via UUID lookup
            Ability existing = customAbilitiesById.get(uuid);
            if (existing != null) {
                String oldName = existing.getName();
                if (!oldName.equals(name)) {
                    // Name changed! Ensure new name is unique (excluding self)
                    String testName = name;
                    while (customAbilities.containsKey(testName) && !testName.equals(oldName)) {
                        testName = testName + "_";
                    }
                    if (!testName.equals(name)) {
                        name = testName;
                        ability.setName(name);
                    }

                    // Delete old file and remove old map entry
                    File oldFile = new File(getDir(), oldName + ".json");
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    customAbilities.remove(oldName);
                }
            }
        }

        File dir = getDir();
        File fileNew = new File(dir, name + ".json_new");
        File fileCurrent = new File(dir, name + ".json");

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

            customAbilities.put(name, ability);
            customAbilitiesById.put(uuid, ability);
            customAbilityRevision++;
            LogWriter.info("Saved custom ability: " + name + " [" + uuid + "]");
            SyncController.syncAllCustomAbilities();
            return true;
        } catch (Exception e) {
            LogWriter.error("Error saving custom ability: " + name, e);
            return false;
        }
    }

    public boolean deleteCustomAbility(String name) {
        if (name == null || name.isEmpty()) return false;

        Ability removed = customAbilities.remove(name);
        if (removed == null) return false;

        // Also remove from UUID index
        String uuid = removed.getId();
        if (uuid != null && !uuid.isEmpty()) {
            customAbilitiesById.remove(uuid);
        }

        File dir = getDir();
        File file = new File(dir, name + ".json");
        if (file.exists()) {
            file.delete();
        }

        customAbilityRevision++;
        LogWriter.info("Deleted custom ability: " + name);
        SyncController.syncAllCustomAbilities();
        return true;
    }

    public Ability getCustomAbility(String name) {
        return customAbilities.get(name);
    }

    public Set<String> getCustomAbilityNames() {
        return new LinkedHashSet<>(customAbilities.keySet());
    }

    public Map<String, Ability> getCustomAbilities() {
        return customAbilities;
    }

    public void setCustomAbilities(Map<String, Ability> synced) {
        customAbilities.clear();
        customAbilitiesById.clear();
        customAbilities.putAll(synced);
        for (Ability ability : synced.values()) {
            String uuid = ability.getId();
            if (uuid != null && !uuid.isEmpty()) {
                customAbilitiesById.put(uuid, ability);
            }
        }
        customAbilityRevision++;
    }

    public int getCustomAbilityRevision() {
        return customAbilityRevision;
    }

    private File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "abilities");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESOLUTION
    // ═══════════════════════════════════════════════════════════════════════════

    public Ability resolveAbility(String key) {
        if (key == null || key.isEmpty()) return null;

        // Custom: UUID lookup (most common for persistent references)
        Ability byUuid = customAbilitiesById.get(key);
        if (byUuid != null) return byUuid.deepCopy();

        // Built-in: exact name
        Ability builtIn = builtAbilities.get(key);
        if (builtIn != null) return builtIn.deepCopy();

        // Built-in: case-insensitive name
        for (Map.Entry<String, Ability> entry : builtAbilities.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue().deepCopy();
            }
        }

        // Built-in: registry key / ID
        for (Ability ability : builtAbilities.values()) {
            if (ability.getId() != null && ability.getId().equalsIgnoreCase(key)) {
                return ability.deepCopy();
            }
        }

        // Custom: exact name (fallback for legacy references)
        Ability custom = customAbilities.get(key);
        if (custom != null) return custom.deepCopy();

        // Custom: case-insensitive name
        for (Ability ability : customAbilities.values()) {
            if (ability.getName() != null && ability.getName().equalsIgnoreCase(key)) {
                return ability.deepCopy();
            }
        }

        return null;
    }

    public Set<String> getAbilityKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Ability ability : builtAbilities.values()) {
            String id = ability.getId();
            if (id != null && !id.isEmpty()) {
                keys.add(id);
            }
        }
        // Custom abilities keyed by UUID (stable references)
        for (Ability ability : customAbilities.values()) {
            String id = ability.getId();
            if (id != null && !id.isEmpty()) {
                keys.add(id);
            }
        }
        return keys;
    }

    public Set<String> getPlayerAbilityKeys() {
        Set<String> keys = new LinkedHashSet<>();
        for (Ability ability : builtAbilities.values()) {
            if (ability.getAllowedBy().allowsPlayer()) {
                String id = ability.getId();
                if (id != null && !id.isEmpty()) {
                    keys.add(id);
                }
            }
        }
        // Custom abilities keyed by UUID (stable references)
        for (Ability ability : customAbilities.values()) {
            if (ability.getAllowedBy().allowsPlayer()) {
                String id = ability.getId();
                if (id != null && !id.isEmpty()) {
                    keys.add(id);
                }
            }
        }
        return keys;
    }

    public boolean hasAbility(String key) {
        return builtAbilities.containsKey(key) || customAbilities.containsKey(key)
            || customAbilitiesById.containsKey(key);
    }

    public Ability getCustomAbilityByUUID(String uuid) {
        return customAbilitiesById.get(uuid);
    }

    public Ability getCustomAbilityByName(String name) {
        return customAbilities.get(name);
    }

    /**
     * Check if a key can be resolved to a valid ability without creating a deep copy.
     * Uses the same lookup chain as {@link #resolveAbility(String)}.
     */
    public boolean canResolveAbility(String key) {
        if (key == null || key.isEmpty()) return false;

        // Custom: UUID lookup (most common for persistent references)
        if (customAbilitiesById.containsKey(key)) return true;

        // Built-in: exact name
        if (builtAbilities.containsKey(key)) return true;

        // Built-in: case-insensitive name
        for (String name : builtAbilities.keySet()) {
            if (name.equalsIgnoreCase(key)) return true;
        }

        // Built-in: registry key / ID
        for (Ability ability : builtAbilities.values()) {
            if (ability.getId() != null && ability.getId().equalsIgnoreCase(key)) return true;
        }

        // Custom: exact name (fallback for legacy references)
        if (customAbilities.containsKey(key)) return true;

        // Custom: case-insensitive name
        for (Ability ability : customAbilities.values()) {
            if (ability.getName() != null && ability.getName().equalsIgnoreCase(key)) return true;
        }

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UNIFIED ACTION RESOLUTION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Resolve any action key to an IAbilityAction.
     * Checks abilities first, then chained abilities.
     */
    public IAbilityAction resolveAction(String key) {
        if (key == null || key.isEmpty()) return null;

        Ability ability = resolveAbility(key);
        if (ability != null) return ability;

        if (ChainedAbilityController.Instance != null) {
            return ChainedAbilityController.Instance.resolve(key);
        }

        return null;
    }

    /**
     * Check if a key can be resolved as any action (ability or chain).
     */
    public boolean canResolveAction(String key) {
        if (canResolveAbility(key)) return true;
        return ChainedAbilityController.Instance != null
            && ChainedAbilityController.Instance.canResolve(key);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EXTENSION POINTS
    // ═══════════════════════════════════════════════════════════════════════════

    public void registerVariant(String typeId, AbilityVariant variant) {
        externalVariants.computeIfAbsent(typeId, k -> new ArrayList<>()).add(variant);
    }

    public List<AbilityVariant> getVariantsForType(String typeId) {
        List<AbilityVariant> result = new ArrayList<>();
        Ability temp = create(typeId);
        if (temp != null) {
            result.addAll(temp.getVariants());
        }
        List<AbilityVariant> ext = externalVariants.get(typeId);
        if (ext != null) {
            // If no built-in variants exist but external ones do,
            // inject a "Base" variant so the user always gets a choice
            if (result.isEmpty()) {
                result.add(new AbilityVariant("ability.variant.base", a -> {}));
            }
            result.addAll(ext);
        }
        return result;
    }

    public void registerFieldProvider(IAbilityFieldProvider provider) {
        fieldProviders.add(provider);
    }

    public List<IAbilityFieldProvider> getFieldProviders() {
        return fieldProviders;
    }

    public void registerExtender(IAbilityExtender extender) {
        extenders.add(extender);
    }

    public List<IAbilityExtender> getExtenders() {
        return extenders;
    }

    public void registerFlightChecker(Predicate<EntityPlayer> checker) {
        flightCheckers.add(checker);
    }

    public boolean isPlayerFlying(EntityPlayer player) {
        if (player.capabilities.isFlying) return true;
        for (Predicate<EntityPlayer> checker : flightCheckers) {
            if (checker.test(player)) return true;
        }
        return false;
    }

    /**
     * Fire onAbilityStart on all extenders. Returns false if ANY extender cancels.
     */
    public boolean fireOnAbilityStart(Ability ability, EntityLivingBase caster, EntityLivingBase target) {
        for (IAbilityExtender ext : extenders) {
            if (!ext.onAbilityStart(ability, caster, target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fire onAbilityTick on all extenders. Returns false if ANY extender says to interrupt.
     */
    public boolean fireOnAbilityTick(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                                     AbilityPhase phase, int tick) {
        for (IAbilityExtender ext : extenders) {
            if (!ext.onAbilityTick(ability, caster, target, phase, tick)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fire onAbilityComplete on all extenders.
     */
    public void fireOnAbilityComplete(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                                      boolean interrupted) {
        for (IAbilityExtender ext : extenders) {
            ext.onAbilityComplete(ability, caster, target, interrupted);
        }
    }

    /**
     * Fire onAbilityDamage on all extenders. Chain of responsibility — first true wins.
     */
    public boolean fireOnAbilityDamage(Ability ability, EntityLivingBase caster, EntityLivingBase target,
                                       float damage, float knockback, float knockbackUp,
                                       double knockbackDirX, double knockbackDirZ) {
        for (IAbilityExtender ext : extenders) {
            if (ext.onAbilityDamage(ability, caster, target, damage, knockback, knockbackUp,
                                    knockbackDirX, knockbackDirZ)) {
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // IAbilityHandler
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public String[] getTypes() {
        return abilityTypes.keySet().toArray(new String[0]);
    }

    public boolean isBuiltInType(String typeId) {
        return builtInTypeIds.contains(typeId);
    }

    @Override
    public boolean hasType(String typeId) {
        return abilityTypes.containsKey(typeId);
    }

    public boolean isAllowedByPlayer(String typeId) {
        if (abilityTypes.containsKey(typeId)) {
            Ability temp = abilityTypes.get(typeId).get();
            return temp.getAllowedBy() == UserType.PLAYER_ONLY;
        }

        return false;
    }

    public boolean isAllowedByNPC(String typeId) {
        if (abilityTypes.containsKey(typeId)) {
            Ability temp = abilityTypes.get(typeId).get();
            return temp.getAllowedBy() == UserType.NPC_ONLY;
        }

        return false;
    }

    public boolean isAllowedByBoth(String typeId) {
        if (abilityTypes.containsKey(typeId)) {
            Ability temp = abilityTypes.get(typeId).get();
            return temp.getAllowedBy() == UserType.BOTH;
        }

        return false;
    }

    @Override
    public String[] getAbilityNameArray() {
        return builtAbilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasAbilityName(String name) {
        return builtAbilities.containsKey(name);
    }

    @Override
    public String[] getCustomAbilityNameArray() {
        return customAbilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasCustomAbilityName(String name) {
        return customAbilities.containsKey(name);
    }

    @Override
    public boolean deleteCustomAbilityByName(String name) {
        return deleteCustomAbility(name);
    }

    @Override
    public String[] getChainedAbilityNames() {
        return ChainedAbilityController.Instance.getNames().toArray(new String[0]);
    }

    @Override
    public boolean hasChainedAbilityName(String name) {
        return ChainedAbilityController.Instance.hasName(name);
    }

    @Override
    public boolean deleteChainedAbilityByName(String name) {
        return ChainedAbilityController.Instance.delete(name);
    }
}
