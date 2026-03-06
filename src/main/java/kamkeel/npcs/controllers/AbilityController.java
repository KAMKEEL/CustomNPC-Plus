package kamkeel.npcs.controllers;

import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import kamkeel.npcs.controllers.data.ability.conditions.ConditionHPThreshold;
import kamkeel.npcs.controllers.data.ability.conditions.ConditionHitCount;
import kamkeel.npcs.controllers.data.ability.conditions.ConditionItem;
import kamkeel.npcs.controllers.data.ability.conditions.ConditionHasEffect;
import kamkeel.npcs.controllers.data.ability.conditions.ConditionQuestCompleted;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import kamkeel.npcs.controllers.data.ability.data.effect.IEffectAction;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.entry.ChainedAbilityEntry;
import kamkeel.npcs.controllers.data.ability.enums.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.ability.extender.IAbilityExtender;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.CustomAbility;
import kamkeel.npcs.controllers.data.ability.type.AbilityCharge;
import kamkeel.npcs.controllers.data.ability.type.AbilityCutter;
import kamkeel.npcs.controllers.data.ability.type.AbilityDash;
import kamkeel.npcs.controllers.data.ability.type.AbilityCounter;
import kamkeel.npcs.controllers.data.ability.type.AbilityDodge;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import kamkeel.npcs.controllers.data.ability.type.AbilityHazard;
import kamkeel.npcs.controllers.data.ability.type.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityHeavyHit;
import kamkeel.npcs.controllers.data.ability.type.AbilityShockwave;
import kamkeel.npcs.controllers.data.ability.type.AbilitySlam;
import kamkeel.npcs.controllers.data.ability.type.AbilityTeleport;
import kamkeel.npcs.controllers.data.ability.type.AbilityTrap;
import kamkeel.npcs.controllers.data.ability.type.AbilityVortex;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityBeam;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityDisc;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityDome;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityLaser;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityOrb;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityShield;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilitySweeper;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityWall;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.controllers.data.AbilityScript;
import noppes.npcs.controllers.data.ChainedAbilityScript;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ability.IChainedAbility;
import noppes.npcs.api.handler.IAbilityHandler;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.CategoryManager;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.NBTJsonUtil;
import kamkeel.npcs.util.FileNameHelper;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AbilityController implements IAbilityHandler {

    public static AbilityController Instance = new AbilityController();

    // ── Core Registries ──────────────────────────────────────────────────────
    private final Map<String, Supplier<Ability>> abilityTypes = new LinkedHashMap<>();
    private final Map<String, Ability> builtAbilities = new LinkedHashMap<>();
    private final Map<String, Ability> customAbilities = new LinkedHashMap<>();      // name → Ability
    private final Map<String, Ability> customAbilitiesById = new LinkedHashMap<>();  // UUID → Ability

    // ── Script Handlers ────────────────────────────────────────────────────
    public final HashMap<String, AbilityScript> abilityScriptHandlers = new HashMap<>();
    public final HashMap<String, ChainedAbilityScript> chainedAbilityScriptHandlers = new HashMap<>();

    // ── Extension Points ─────────────────────────────────────────────────────
    private final Map<String, List<AbilityVariant>> externalVariants = new LinkedHashMap<>();
    private final List<IAbilityFieldProvider> fieldProviders = new ArrayList<>();
    private final List<IChainedAbilityFieldProvider> chainedFieldProviders = new ArrayList<>();
    private final List<IAbilityExtender> extenders = new ArrayList<>();
    private final Map<String, Supplier<AbilityCondition>> conditionTypes = new HashMap<>();
    private final List<Predicate<EntityPlayer>> flightCheckers = new ArrayList<>();
    private final List<Predicate<EntityPlayer>> activationCheckers = new ArrayList<>();

    // ── Chained Abilities ──────────────────────────────────────────────────
    private final Map<String, ChainedAbility> chainedAbilities = new LinkedHashMap<>();      // name → ChainedAbility
    private final Map<String, ChainedAbility> chainedAbilitiesById = new LinkedHashMap<>();  // UUID → ChainedAbility
    private int chainedAbilityRevision = 0;

    // ── Category Managers ─────────────────────────────────────────────────
    public final CategoryManager customAbilityCategories = new CategoryManager();
    public final CategoryManager chainedAbilityCategories = new CategoryManager();
    private final Map<String, Integer> customAbilityCatMap = new HashMap<>();   // abilityName → catId
    private final Map<String, Integer> chainedAbilityCatMap = new HashMap<>();  // chainName → catId

    // ── Effect Action Registry ──────────────────────────────────────────────
    private final Map<String, IEffectAction> effectActions = new LinkedHashMap<>();

    // ── Derived State ────────────────────────────────────────────────────────
    private final Set<String> builtInTypeIds = new HashSet<>();

    public AbilityController() {
        registerBuiltinTypes();
        registerBuiltinConditionTypes();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE REGISTRATION
    // ═══════════════════════════════════════════════════════════════════════════

    public void registerType(Supplier<Ability> factory) {
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
        registerType(CustomAbility::new);

        registerType(AbilitySlam::new);
        registerType(AbilityCharge::new);

        registerType(AbilityHeavyHit::new);
        registerType(AbilityCutter::new);

        registerType(AbilitySweeper::new);
        registerType(AbilityOrb::new);
        registerType(AbilityDisc::new);
        registerType(AbilityLaser::new);
        registerType(AbilityBeam::new);

        registerType(AbilityDash::new);
        registerType(AbilityTeleport::new);

        registerType(AbilityVortex::new);
        registerType(AbilityShockwave::new);

        registerType(AbilityGuard::new);
        registerType(AbilityCounter::new);
        registerType(AbilityDodge::new);
        registerType(AbilityEffect::new);

        registerType(AbilityHazard::new);
        registerType(AbilityTrap::new);

        registerType(AbilityDome::new);
        registerType(AbilityWall::new);
        registerType(AbilityShield::new);
    }

    private void registerBuiltinConditionTypes() {
        registerCondition(ConditionHPThreshold::new);
        registerCondition(ConditionHitCount::new);
        registerCondition(ConditionItem::new);
        registerCondition(ConditionQuestCompleted::new);
        registerCondition(ConditionHasEffect::new);
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
            if (ability.getDisplayName().equals(displayName)) {
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
        abilityScriptHandlers.clear();
        customAbilityCatMap.clear();

        File dir = getDir();
        customAbilityCategories.loadCategories(dir, "chained");

        // Load from root directory (Uncategorized)
        loadAbilitiesFromDir(dir, CategoryManager.UNCATEGORIZED_ID);

        // Load from category subdirectories
        for (Map.Entry<Integer, noppes.npcs.controllers.data.Category> entry : customAbilityCategories.getCategories().entrySet()) {
            File catDir = new File(dir, entry.getValue().title);
            loadAbilitiesFromDir(catDir, entry.getKey());
        }

        customAbilityRevision++;
        LogWriter.info("Loaded " + customAbilities.size() + " custom abilities");

        loadChainedAbilities();
    }

    private void loadAbilitiesFromDir(File dir, int catId) {
        File[] files = dir.exists() ? dir.listFiles() : null;
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) continue;
            try {
                String filename = file.getName();
                String fileKey = filename.substring(0, filename.length() - 5);

                NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
                Ability ability = fromNBT(nbt);
                if (ability == null) continue;

                String name = FileNameHelper.sanitizeName(fileKey, "Ability");
                name = makeUniqueNameForLoad(customAbilities, dir, file, name, fileKey);

                boolean dirty = false;
                if (!name.equals(fileKey)) {
                    File renamedFile = new File(dir, name + ".json");
                    if (file.renameTo(renamedFile)) {
                        file = renamedFile;
                        dirty = true;
                    }
                }

                ability.setName(name);

                String uuid = ability.getId();
                if (uuid == null || uuid.isEmpty()) {
                    uuid = UUID.randomUUID().toString();
                    ability.setId(uuid);
                    dirty = true;
                }

                if (dirty) {
                    NBTJsonUtil.SaveFile(file, ability.writeNBT(true));
                }

                customAbilities.put(name, ability);
                customAbilitiesById.put(uuid, ability);
                if (catId > CategoryManager.UNCATEGORIZED_ID) {
                    customAbilityCatMap.put(name, catId);
                }
            } catch (Exception e) {
                LogWriter.error("Error loading custom ability: " + file.getAbsolutePath(), e);
            }
        }
    }

    private boolean nameFileExists(File dir, String name, File ignoredFile) {
        File existing = new File(dir, name + ".json");
        if (!existing.exists()) return false;
        return ignoredFile == null || !existing.getAbsolutePath().equals(ignoredFile.getAbsolutePath());
    }

    private String makeUniqueNameForLoad(Map<String, ?> loadedMap, File dir, File currentFile, String baseName, String selfKey) {
        String candidate = baseName;
        while ((loadedMap.containsKey(candidate) && !candidate.equals(selfKey))
            || nameFileExists(dir, candidate, currentFile)) {
            candidate = candidate + "_";
        }
        return candidate;
    }

    private String makeUniqueNameForMap(Map<String, ?> map, String baseName, String selfName) {
        String candidate = baseName;
        while (map.containsKey(candidate) && (selfName == null || !candidate.equals(selfName))) {
            candidate = candidate + "_";
        }
        return candidate;
    }

    public boolean saveCustomAbility(Ability ability) {
        if (ability == null) return false;
        if (ability.isBuiltIn()) return false;

        String name = FileNameHelper.sanitizeName(ability.getName(), "Ability");
        ability.setName(name);

        String uuid = ability.getId();
        boolean isNew = (uuid == null || uuid.isEmpty());
        int catId = CategoryManager.UNCATEGORIZED_ID;

        if (isNew) {
            // Generate UUID for new ability
            uuid = UUID.randomUUID().toString();
            ability.setId(uuid);

            // Ensure unique name
            name = makeUniqueNameForMap(customAbilities, name, null);
            ability.setName(name);
        } else {
            // Existing ability — check for rename via UUID lookup
            Ability existing = customAbilitiesById.get(uuid);
            if (existing != null) {
                String oldName = existing.getName();
                catId = customAbilityCatMap.getOrDefault(oldName, CategoryManager.UNCATEGORIZED_ID);
                name = makeUniqueNameForMap(customAbilities, name, oldName);
                ability.setName(name);

                if (!oldName.equals(name)) {
                    // Delete old file and remove old map entry
                    File oldDir = getCustomAbilityDir(oldName);
                    File oldFile = new File(oldDir, oldName + ".json");
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    customAbilities.remove(oldName);
                    // Update catMap key
                    customAbilityCatMap.remove(oldName);
                    if (catId > CategoryManager.UNCATEGORIZED_ID) {
                        customAbilityCatMap.put(name, catId);
                    }
                }
            } else {
                name = makeUniqueNameForMap(customAbilities, name, null);
                ability.setName(name);
            }
        }

        File dir = getCustomAbilityDir(name);
        File fileNew = new File(dir, name + ".json_new");
        File fileCurrent = new File(dir, name + ".json");

        try {
            NBTTagCompound nbt = ability.writeNBT(true);

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
            LogWriter.script("Saved custom ability: " + name + " [" + uuid + "]");
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
        if (removed == null) {
            String sanitized = FileNameHelper.sanitizeTextInput(name);
            if (!sanitized.equals(name)) {
                removed = customAbilities.remove(sanitized);
                if (removed != null) {
                    name = sanitized;
                }
            }
        }
        if (removed == null) return false;

        // Also remove from UUID index and script handler
        String uuid = removed.getId();
        if (uuid != null && !uuid.isEmpty()) {
            customAbilitiesById.remove(uuid);
            abilityScriptHandlers.remove(uuid);
        }

        File dir = getCustomAbilityDir(name);
        File file = new File(dir, name + ".json");
        if (file.exists()) {
            file.delete();
        }
        customAbilityCatMap.remove(name);

        // Clean up online players' unlocked ability lists
        if (PlayerDataController.Instance != null) {
            List<PlayerData> snapshot = new ArrayList<>(PlayerDataController.Instance.getAllPlayerData());
            for (PlayerData pData : snapshot) {
                if (pData.abilityData != null) {
                    boolean changed = false;
                    if (uuid != null && !uuid.isEmpty() && pData.abilityData.hasUnlockedAbility(uuid)) {
                        pData.abilityData.lockAbility(uuid);
                        changed = true;
                    }
                    if (pData.abilityData.hasUnlockedAbility(name)) {
                        pData.abilityData.lockAbility(name);
                        changed = true;
                    }
                    if (changed) {
                        pData.save();
                    }
                }
            }
        }

        customAbilityRevision++;
        LogWriter.script("Deleted custom ability: " + name);
        SyncController.syncAllCustomAbilities();
        return true;
    }

    public Ability getCustomAbility(String name) {
        Ability ability = customAbilities.get(name);
        if (ability != null) return ability;
        String sanitized = FileNameHelper.sanitizeTextInput(name);
        if (!sanitized.equals(name)) {
            return customAbilities.get(sanitized);
        }
        return null;
    }

    public Set<String> getCustomAbilityNames() {
        return new LinkedHashSet<>(customAbilities.keySet());
    }

    public Map<String, Ability> getCustomAbilities() {
        return customAbilities;
    }

    public synchronized void setCustomAbilities(Map<String, Ability> synced) {
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

    private File getCustomAbilityDir(String name) {
        int catId = customAbilityCatMap.getOrDefault(name, CategoryManager.UNCATEGORIZED_ID);
        return customAbilityCategories.getCategoryDir(catId);
    }

    private File getChainedAbilityDir(String name) {
        int catId = chainedAbilityCatMap.getOrDefault(name, CategoryManager.UNCATEGORIZED_ID);
        return chainedAbilityCategories.getCategoryDir(catId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM ABILITY CATEGORY HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    public Map<String, Integer> getCustomAbilityCategoryScrollData() {
        return customAbilityCategories.getCategoryScrollData();
    }

    public Map<String, Integer> getCustomAbilityItemsByCategoryScrollData(int catId) {
        Map<String, Integer> map = new HashMap<>();
        for (Map.Entry<String, Ability> entry : customAbilities.entrySet()) {
            int assignedCat = customAbilityCatMap.getOrDefault(entry.getKey(), CategoryManager.UNCATEGORIZED_ID);
            if (assignedCat == catId) {
                map.put(entry.getKey(), entry.getValue().getAllowedBy().ordinal());
            }
        }
        return map;
    }

    public void moveCustomAbilityToCategory(String name, int destCatId) {
        Ability ability = customAbilities.get(name);
        if (ability == null) return;

        int oldCatId = customAbilityCatMap.getOrDefault(name, CategoryManager.UNCATEGORIZED_ID);
        if (oldCatId == destCatId) return;

        File oldDir = customAbilityCategories.getCategoryDir(oldCatId);
        File newDir = customAbilityCategories.getCategoryDir(destCatId);
        if (!newDir.exists()) newDir.mkdirs();

        File oldFile = new File(oldDir, name + ".json");
        File newFile = new File(newDir, name + ".json");
        if (oldFile.exists()) oldFile.renameTo(newFile);

        if (destCatId == CategoryManager.UNCATEGORIZED_ID) {
            customAbilityCatMap.remove(name);
        } else {
            customAbilityCatMap.put(name, destCatId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CHAINED ABILITY CATEGORY HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    public Map<String, Integer> getChainedAbilityCategoryScrollData() {
        return chainedAbilityCategories.getCategoryScrollData();
    }

    public Map<String, Integer> getChainedAbilityItemsByCategoryScrollData(int catId) {
        Map<String, Integer> map = new HashMap<>();
        for (Map.Entry<String, ChainedAbility> entry : chainedAbilities.entrySet()) {
            int assignedCat = chainedAbilityCatMap.getOrDefault(entry.getKey(), CategoryManager.UNCATEGORIZED_ID);
            if (assignedCat == catId) {
                map.put(entry.getKey(), entry.getValue().getAllowedBy().ordinal());
            }
        }
        return map;
    }

    public void moveChainedAbilityToCategory(String name, int destCatId) {
        ChainedAbility chain = chainedAbilities.get(name);
        if (chain == null) return;

        int oldCatId = chainedAbilityCatMap.getOrDefault(name, CategoryManager.UNCATEGORIZED_ID);
        if (oldCatId == destCatId) return;

        File oldDir = chainedAbilityCategories.getCategoryDir(oldCatId);
        File newDir = chainedAbilityCategories.getCategoryDir(destCatId);
        if (!newDir.exists()) newDir.mkdirs();

        File oldFile = new File(oldDir, name + ".json");
        File newFile = new File(newDir, name + ".json");
        if (oldFile.exists()) oldFile.renameTo(newFile);

        if (destCatId == CategoryManager.UNCATEGORIZED_ID) {
            chainedAbilityCatMap.remove(name);
        } else {
            chainedAbilityCatMap.put(name, destCatId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CHAINED ABILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadChainedAbilities() {
        chainedAbilities.clear();
        chainedAbilitiesById.clear();
        chainedAbilityScriptHandlers.clear();
        chainedAbilityCatMap.clear();

        File dir = getChainedDir();
        chainedAbilityCategories.loadCategories(dir);

        // Load from root directory (Uncategorized)
        loadChainedFromDir(dir, CategoryManager.UNCATEGORIZED_ID);

        // Load from category subdirectories
        for (Map.Entry<Integer, noppes.npcs.controllers.data.Category> entry : chainedAbilityCategories.getCategories().entrySet()) {
            File catDir = new File(dir, entry.getValue().title);
            loadChainedFromDir(catDir, entry.getKey());
        }

        chainedAbilityRevision++;
        LogWriter.info("Loaded " + chainedAbilities.size() + " chained abilities");
    }

    private void loadChainedFromDir(File dir, int catId) {
        File[] files = dir.exists() ? dir.listFiles() : null;
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) continue;
            try {
                String filename = file.getName();
                String fileKey = filename.substring(0, filename.length() - 5);

                NBTTagCompound nbt = NBTJsonUtil.LoadFile(file);
                ChainedAbility chain = new ChainedAbility();
                chain.readNBT(nbt);

                String name = FileNameHelper.sanitizeName(fileKey, "Chain");
                name = makeUniqueNameForLoad(chainedAbilities, dir, file, name, fileKey);

                boolean dirty = false;
                if (!name.equals(fileKey)) {
                    File renamedFile = new File(dir, name + ".json");
                    if (file.renameTo(renamedFile)) {
                        file = renamedFile;
                        dirty = true;
                    }
                }

                chain.setName(name);

                String uuid = chain.getId();
                if (uuid == null || uuid.isEmpty()) {
                    uuid = UUID.randomUUID().toString();
                    chain.setId(uuid);
                    dirty = true;
                }

                if (dirty) {
                    NBTJsonUtil.SaveFile(file, chain.writeNBT(true));
                }

                chainedAbilities.put(name, chain);
                chainedAbilitiesById.put(uuid, chain);
                if (catId > CategoryManager.UNCATEGORIZED_ID) {
                    chainedAbilityCatMap.put(name, catId);
                }
            } catch (Exception e) {
                LogWriter.error("Error loading chained ability: " + file.getAbsolutePath(), e);
            }
        }
    }

    public boolean saveChainedAbility(ChainedAbility chain) {
        if (chain == null) return false;

        String name = FileNameHelper.sanitizeName(chain.getName(), "Chain");
        chain.setName(name);

        String uuid = chain.getId();
        boolean isNew = (uuid == null || uuid.isEmpty());

        if (isNew) {
            uuid = UUID.randomUUID().toString();
            chain.setId(uuid);

            name = makeUniqueNameForMap(chainedAbilities, name, null);
            chain.setName(name);
        } else {
            ChainedAbility existing = chainedAbilitiesById.get(uuid);
            if (existing != null) {
                String oldName = existing.getName();
                int catId = chainedAbilityCatMap.getOrDefault(oldName, CategoryManager.UNCATEGORIZED_ID);
                name = makeUniqueNameForMap(chainedAbilities, name, oldName);
                chain.setName(name);

                if (!oldName.equals(name)) {
                    File oldDir = getChainedAbilityDir(oldName);
                    File oldFile = new File(oldDir, oldName + ".json");
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    chainedAbilities.remove(oldName);
                    chainedAbilityCatMap.remove(oldName);
                    if (catId > CategoryManager.UNCATEGORIZED_ID) {
                        chainedAbilityCatMap.put(name, catId);
                    }
                }
            } else {
                name = makeUniqueNameForMap(chainedAbilities, name, null);
                chain.setName(name);
            }
        }

        File dir = getChainedAbilityDir(name);
        File fileNew = new File(dir, name + ".json_new");
        File fileCurrent = new File(dir, name + ".json");

        try {
            NBTTagCompound nbt = chain.writeNBT(true);

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
            chainedAbilityRevision++;
            LogWriter.script("Saved chained ability: " + name + " [" + uuid + "]");
            SyncController.syncAllChainedAbilities();
            return true;
        } catch (Exception e) {
            LogWriter.error("Error saving chained ability: " + name, e);
            return false;
        }
    }

    public boolean deleteChainedAbility(String name) {
        if (name == null || name.isEmpty()) return false;

        ChainedAbility removed = chainedAbilities.remove(name);
        if (removed == null) {
            String sanitized = FileNameHelper.sanitizeTextInput(name);
            if (!sanitized.equals(name)) {
                removed = chainedAbilities.remove(sanitized);
                if (removed != null) {
                    name = sanitized;
                }
            }
        }
        if (removed == null) return false;

        String uuid = removed.getId();
        if (uuid != null && !uuid.isEmpty()) {
            chainedAbilitiesById.remove(uuid);
            chainedAbilityScriptHandlers.remove(uuid);
        }

        File dir = getChainedAbilityDir(name);
        File file = new File(dir, name + ".json");
        if (file.exists()) {
            file.delete();
        }
        chainedAbilityCatMap.remove(name);

        // Clean up online players' unlocked ability lists (chain keys use "chain:" prefix)
        if (PlayerDataController.Instance != null) {
            List<PlayerData> snapshot = new ArrayList<>(PlayerDataController.Instance.getAllPlayerData());
            for (PlayerData pData : snapshot) {
                if (pData.abilityData != null) {
                    String chainKey = "chain:" + (uuid != null && !uuid.isEmpty() ? uuid : name);
                    if (pData.abilityData.hasUnlockedAbility(chainKey)) {
                        pData.abilityData.lockAbility(chainKey);
                    }
                    String chainNameKey = "chain:" + name;
                    if (!chainNameKey.equals(chainKey) && pData.abilityData.hasUnlockedAbility(chainNameKey)) {
                        pData.abilityData.lockAbility(chainNameKey);
                    }
                }
            }
        }

        chainedAbilityRevision++;
        LogWriter.script("Deleted chained ability: " + name);
        SyncController.syncAllChainedAbilities();
        return true;
    }

    public ChainedAbility resolveChainedAbility(String key) {
        if (key == null || key.isEmpty()) return null;

        ChainedAbility byId = chainedAbilitiesById.get(key);
        if (byId != null) return byId.deepCopy();

        ChainedAbility chain = chainedAbilities.get(key);
        if (chain != null) return chain.deepCopy();

        for (Map.Entry<String, ChainedAbility> entry : chainedAbilities.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue().deepCopy();
            }
        }

        String sanitizedKey = FileNameHelper.sanitizeTextInput(key);
        if (!sanitizedKey.equals(key)) {
            ChainedAbility sanitized = chainedAbilities.get(sanitizedKey);
            if (sanitized != null) return sanitized.deepCopy();
        }

        return null;
    }

    public boolean canResolveChainedAbility(String key) {
        if (key == null || key.isEmpty()) return false;
        if (chainedAbilitiesById.containsKey(key)) return true;
        if (chainedAbilities.containsKey(key)) return true;
        for (String name : chainedAbilities.keySet()) {
            if (name.equalsIgnoreCase(key)) return true;
        }
        String sanitizedKey = FileNameHelper.sanitizeTextInput(key);
        if (!sanitizedKey.equals(key) && chainedAbilities.containsKey(sanitizedKey)) return true;
        return false;
    }

    @Override
    public ChainedAbility getChainedAbility(String name) {
        ChainedAbility chain = chainedAbilities.get(name);
        if (chain == null) {
            String sanitized = FileNameHelper.sanitizeTextInput(name);
            if (!sanitized.equals(name)) {
                chain = chainedAbilities.get(sanitized);
            }
        }
        return chain != null ? chain.deepCopy() : null;
    }

    public ChainedAbility getChainedAbilityByUUID(String uuid) {
        return chainedAbilitiesById.get(uuid);
    }

    public Set<String> getChainedAbilityNamesSet() {
        return new LinkedHashSet<>(chainedAbilities.keySet());
    }

    public Map<String, ChainedAbility> getChainedAbilities() {
        return chainedAbilities;
    }

    @Override
    public String[] getChainedAbilityNames() {
        return chainedAbilities.keySet().toArray(new String[0]);
    }

    @Override
    public boolean hasChainedAbilityName(String name) {
        if (chainedAbilities.containsKey(name)) return true;
        String sanitized = FileNameHelper.sanitizeTextInput(name);
        return !sanitized.equals(name) && chainedAbilities.containsKey(sanitized);
    }

    @Override
    public boolean deleteChainedAbilityByName(String name) {
        return deleteChainedAbility(name);
    }

    @Override
    public boolean saveChainedAbility(IChainedAbility chain) {
        if (chain instanceof ChainedAbility) {
            return saveChainedAbility((ChainedAbility) chain);
        }
        return false;
    }

    public int getChainedAbilityRevision() {
        return chainedAbilityRevision;
    }

    public synchronized void setChainedAbilities(Map<String, ChainedAbility> synced) {
        chainedAbilities.clear();
        chainedAbilitiesById.clear();
        chainedAbilities.putAll(synced);
        for (ChainedAbility chain : synced.values()) {
            String uuid = chain.getId();
            if (uuid != null && !uuid.isEmpty()) {
                chainedAbilitiesById.put(uuid, chain);
            }
        }
        chainedAbilityRevision++;
    }

    private File getChainedDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "abilities" + File.separator + "chained");
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

        // Built-in: exact key
        Ability builtIn = builtAbilities.get(key);
        if (builtIn != null) return builtIn.deepCopy();

        // Built-in: case-insensitive key
        String lowerKey = key.toLowerCase();
        for (Map.Entry<String, Ability> entry : builtAbilities.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(lowerKey)) {
                return entry.getValue().deepCopy();
            }
        }

        // Built-in: by ID (case-insensitive)
        for (Ability ability : builtAbilities.values()) {
            if (key.equalsIgnoreCase(ability.getId())) {
                return ability.deepCopy();
            }
        }

        // Custom: exact name
        Ability custom = customAbilities.get(key);
        if (custom != null) return custom.deepCopy();

        // Custom: case-insensitive name
        for (Map.Entry<String, Ability> entry : customAbilities.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue().deepCopy();
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
     * Resolve an ability by key and return the live (non-copied) reference.
     * For validation/permission checks only — do NOT expose or modify the returned ability.
     * Uses the same lookup chain as {@link #resolveAbility(String)}.
     */
    public Ability peekAbility(String key) {
        if (key == null || key.isEmpty()) return null;

        Ability byUuid = customAbilitiesById.get(key);
        if (byUuid != null) return byUuid;

        Ability builtIn = builtAbilities.get(key);
        if (builtIn != null) return builtIn;

        for (Ability ability : builtAbilities.values()) {
            if (key.equals(ability.getId())) return ability;
        }

        Ability custom = customAbilities.get(key);
        if (custom != null) return custom;

        return null;
    }

    /**
     * Check if a key can be resolved to a valid ability without creating a deep copy.
     * Uses the same lookup chain as {@link #resolveAbility(String)}.
     */
    public boolean canResolveAbility(String key) {
        if (key == null || key.isEmpty()) return false;

        // Custom: UUID lookup
        if (customAbilitiesById.containsKey(key)) return true;

        // Built-in: exact key
        if (builtAbilities.containsKey(key)) return true;

        // Built-in: by ID
        for (Ability ability : builtAbilities.values()) {
            if (key.equals(ability.getId())) return true;
        }

        // Custom: exact name
        if (customAbilities.containsKey(key)) return true;

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

        return resolveChainedAbility(key);
    }

    /**
     * Check if a key can be resolved as any action (ability or chain).
     */
    public boolean canResolveAction(String key) {
        if (canResolveAbility(key)) return true;
        return canResolveChainedAbility(key);
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
                result.add(new AbilityVariant("ability.variant.base", a -> {
                }));
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

    public void registerChainedFieldProvider(IChainedAbilityFieldProvider provider) {
        chainedFieldProviders.add(provider);
    }

    public List<IChainedAbilityFieldProvider> getChainedFieldProviders() {
        return chainedFieldProviders;
    }

    public void registerExtender(IAbilityExtender extender) {
        extenders.add(extender);
    }

    public List<IAbilityExtender> getExtenders() {
        return extenders;
    }

    public void registerCondition(Supplier<AbilityCondition> conditionFactory) {
        AbilityCondition temp = conditionFactory.get();
        String typeId = temp.getTypeId();
        if (conditionTypes.containsKey(typeId)) {
            LogWriter.info("AbilityController: Overwriting Condition type: " + typeId);
        }
        conditionTypes.put(typeId, conditionFactory);
    }

    public Supplier<AbilityCondition> getConditionType(String key) {
        return conditionTypes.get(key);
    }

    public String[] getConditionTypes() {
        return conditionTypes.keySet().toArray(new String[0]);
    }

    public List<String> getConditionNamespaces() {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String typeId : conditionTypes.keySet()) {
            String[] parts = typeId.split("\\.", 3);
            if (parts.length >= 2) {
                seen.add(parts[1]);
            }
        }
        return new ArrayList<>(seen);
    }

    public String[] getConditionTypesByNamespace(String namespace) {
        String prefix = "condition." + namespace + ".";
        List<String> result = new ArrayList<>();
        for (String typeId : conditionTypes.keySet()) {
            if (typeId.startsWith(prefix)) {
                result.add(typeId);
            }
        }
        return result.toArray(new String[0]);
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

    public void registerActivationChecker(Predicate<EntityPlayer> checker) {
        activationCheckers.add(checker);
    }

    public boolean canPlayerActivate(EntityPlayer player) {
        for (Predicate<EntityPlayer> checker : activationCheckers) {
            if (!checker.test(player)) return false;
        }
        return true;
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
                                       double knockbackDirX, double knockbackDirZ,
                                       float damageMultiplier) {
        for (IAbilityExtender ext : extenders) {
            if (ext.onAbilityDamage(ability, caster, target, damage, knockback, knockbackUp,
                knockbackDirX, knockbackDirZ, damageMultiplier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fire modifyProjectileDamage on all extenders. Cumulative — each extender's output feeds the next.
     */
    public float fireModifyProjectileDamage(Ability ability, EntityLivingBase caster, float baseDamage) {
        float damage = baseDamage;
        for (IAbilityExtender ext : extenders) {
            damage = ext.modifyProjectileDamage(ability, caster, damage);
        }
        return damage;
    }

    /**
     * Fire modifyBarrierHealth on all extenders. Cumulative — each extender's output feeds the next.
     */
    public float fireModifyBarrierHealth(Ability ability, EntityLivingBase caster, float baseHealth) {
        float health = baseHealth;
        for (IAbilityExtender ext : extenders) {
            health = ext.modifyBarrierHealth(ability, caster, health);
        }
        return health;
    }

    /**
     * Fire onAbilityHeal on all extenders. Chain of responsibility — first true wins.
     */
    public boolean fireOnAbilityHeal(Ability ability, EntityLivingBase caster, EntityLivingBase target, float healAmount) {
        for (IAbilityExtender ext : extenders) {
            if (ext.onAbilityHeal(ability, caster, target, healAmount)) {
                return true;
            }
        }
        return false;
    }

    public Boolean fireCheckConditions(AbilityCondition condition, EntityLivingBase caster, EntityLivingBase target) {
        for (IAbilityExtender ext : extenders) {
            Boolean result = ext.onCheckCondition(condition, caster, target);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public Boolean fireCheckConditionsForPlayer(AbilityCondition condition, EntityLivingBase player) {
        for (IAbilityExtender ext : extenders) {
            Boolean result = ext.onCheckConditionForPlayer(condition, player);
            if (result != null) {
                return result;
            }
        }
        return null;
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

    public boolean isConcurrentCapableType(String typeId) {
        Supplier<Ability> factory = abilityTypes.get(typeId);
        if (factory == null) return false;
        return factory.get().isConcurrentCapable();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EFFECT ACTION REGISTRY
    // ═══════════════════════════════════════════════════════════════════════════

    public void registerEffectAction(IEffectAction action) {
        if (action == null || action.getId() == null) return;
        effectActions.put(action.getId(), action);
    }

    public IEffectAction getEffectAction(String id) {
        return effectActions.get(id);
    }

    public String[] getEffectActionIds() {
        return effectActions.keySet().toArray(new String[0]);
    }

    public java.util.Collection<IEffectAction> getEffectActions() {
        return effectActions.values();
    }

    public boolean hasEffectActions() {
        return !effectActions.isEmpty();
    }

    @Override
    public boolean hasType(String typeId) {
        return abilityTypes.containsKey(typeId);
    }

    public boolean isAllowedByPlayer(String typeId) {
        if (abilityTypes.containsKey(typeId)) {
            Ability temp = abilityTypes.get(typeId).get();
            return temp.getAllowedBy().allowsPlayer();
        }

        return false;
    }

    public boolean isAllowedByNPC(String typeId) {
        if (abilityTypes.containsKey(typeId)) {
            Ability temp = abilityTypes.get(typeId).get();
            return temp.getAllowedBy().allowsNpc();
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
        if (customAbilities.containsKey(name)) return true;
        String sanitized = FileNameHelper.sanitizeTextInput(name);
        return !sanitized.equals(name) && customAbilities.containsKey(sanitized);
    }

    @Override
    public boolean deleteCustomAbilityByName(String name) {
        return deleteCustomAbility(name);
    }

}
