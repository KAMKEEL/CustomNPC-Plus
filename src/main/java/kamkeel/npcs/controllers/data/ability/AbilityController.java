package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.data.ability.type.*;
import kamkeel.npcs.controllers.SyncController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAbilityHandler;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

public class AbilityController implements IAbilityHandler {

    public static AbilityController Instance = new AbilityController();

    // ── Core Registries ──────────────────────────────────────────────────────
    private final Map<String, Supplier<Ability>> abilityTypes = new LinkedHashMap<>();
    private final Map<String, Ability> builtAbilities = new LinkedHashMap<>();
    private final Map<String, Ability> customAbilities = new LinkedHashMap<>();

    // ── Extension Points ─────────────────────────────────────────────────────
    private final Map<String, List<AbilityVariant>> externalVariants = new LinkedHashMap<>();
    private final List<IAbilityFieldProvider> fieldProviders = new ArrayList<>();
    private final List<IAbilityExtender> extenders = new ArrayList<>();

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

        File dir = getDir();
        File[] files = dir.exists() ? dir.listFiles() : null;
        if (files != null) {
            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".json")) continue;
                try {
                    String filename = file.getName();
                    String uuid = filename.substring(0, filename.length() - 5);

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
        }

        customAbilityRevision++;
        LogWriter.info("Loaded " + customAbilities.size() + " custom abilities");
    }

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
            customAbilityRevision++;
            LogWriter.info("Saved custom ability: " + ability.getName() + " [" + uuid + "]");
            SyncController.syncAllCustomAbilities();
            return true;
        } catch (Exception e) {
            LogWriter.error("Error saving custom ability: " + uuid, e);
            return false;
        }
    }

    public boolean deleteCustomAbility(String uuid) {
        if (uuid == null || uuid.isEmpty()) return false;

        Ability removed = customAbilities.remove(uuid);
        if (removed == null) return false;

        File dir = getDir();
        File file = new File(dir, uuid + ".json");
        if (file.exists()) {
            file.delete();
        }

        customAbilityRevision++;
        LogWriter.info("Deleted custom ability: " + uuid);
        SyncController.syncAllCustomAbilities();
        return true;
    }

    public Ability getCustomAbility(String uuid) {
        return customAbilities.get(uuid);
    }

    public boolean hasCustomAbility(String uuid) {
        return customAbilities.containsKey(uuid);
    }

    public Set<String> getCustomAbilityIds() {
        return new LinkedHashSet<>(customAbilities.keySet());
    }

    public String getCustomAbilityName(String uuid) {
        Ability a = customAbilities.get(uuid);
        return a != null ? a.getName() : null;
    }

    public Map<String, Ability> getCustomAbilities() {
        return customAbilities;
    }

    public void setCustomAbilities(Map<String, Ability> synced) {
        customAbilities.clear();
        customAbilities.putAll(synced);
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

        // Custom: exact UUID
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
        // Use ability names for custom abilities (instead of UUIDs) for readable tab completion
        for (Ability ability : customAbilities.values()) {
            String name = ability.getName();
            if (name != null && !name.isEmpty()) {
                keys.add(name);
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
        // Use ability names for custom abilities (instead of UUIDs) for readable tab completion
        for (Ability ability : customAbilities.values()) {
            if (ability.getAllowedBy().allowsPlayer()) {
                String name = ability.getName();
                if (name != null && !name.isEmpty()) {
                    keys.add(name);
                }
            }
        }
        return keys;
    }

    public boolean hasAbility(String key) {
        return builtAbilities.containsKey(key) || customAbilities.containsKey(key);
    }

    /**
     * Check if a key can be resolved to a valid ability without creating a deep copy.
     * Uses the same lookup chain as {@link #resolveAbility(String)}.
     */
    public boolean canResolveAbility(String key) {
        if (key == null || key.isEmpty()) return false;

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

        // Custom: exact UUID
        if (customAbilities.containsKey(key)) return true;

        // Custom: case-insensitive name
        for (Ability ability : customAbilities.values()) {
            if (ability.getName() != null && ability.getName().equalsIgnoreCase(key)) return true;
        }

        return false;
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

    @Override
    public String[] getAbilityNameArray() {
        return builtAbilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasAbilityName(String name) {
        return builtAbilities.containsKey(name);
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
}
