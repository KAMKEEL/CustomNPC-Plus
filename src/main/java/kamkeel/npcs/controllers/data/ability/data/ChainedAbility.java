package kamkeel.npcs.controllers.data.ability.data;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import noppes.npcs.controllers.data.ChainedAbilityScript;
import kamkeel.npcs.controllers.data.ability.data.entry.ChainedAbilityEntry;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.ability.IChainedAbility;
import noppes.npcs.controllers.ScriptContainer;
import kamkeel.npcs.util.FileNameHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A chained ability is an ordered sequence of ability references that execute
 * one after another. It has its own conditions, cooldown, weight, and per-entry
 * delay configuration. Individual ability conditions are skipped during chain execution.
 * <p>
 * All entries must be ability references (by name/key). Inline abilities are not supported.
 * When saving, all references are validated against {@link AbilityController}.
 */
public class ChainedAbility implements IChainedAbility, IAbilityAction {

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION (saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    private String id = "";               // UUID (stable persistent reference)
    private String name = "";             // Unique file key / identifier
    private String displayName = "";      // Cosmetic name (falls back to name when empty)

    private boolean enabled = true;

    /**
     * Weight for NPC weighted random selection.
     */
    private int weight = 10;

    /**
     * Whether each ability in the chain uses its own windup phase.
     */
    private boolean windUpAll = true;

    /**
     * Chain-level cooldown in ticks (applied after entire chain completes).
     */
    private int cooldownTicks = 100;

    /**
     * Minimum range for NPC eligibility check.
     */
    private float minRange = 0;

    /**
     * Maximum range for NPC eligibility check.
     */
    private float maxRange = 20;

    /**
     * Chain-level conditions (individual ability conditions are ignored).
     */
    private List<AbilityCondition> conditions = new ArrayList<>();

    /**
     * Ordered list of ability entries to execute.
     */
    private List<ChainedAbilityEntry> entries = new ArrayList<>();

    /**
     * Extension data for external mods (e.g., icons, DBC stats).
     */
    private NBTTagCompound customData = new NBTTagCompound();

    // Per-instance script (isolated per execution, not saved to NBT)
    private transient ChainedAbilityScript instanceScript;

    public ChainedAbility() {
    }

    public ChainedAbility(String name) {
        setName(name);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String fallback = (this.name != null && !this.name.isEmpty()) ? this.name : "Chain";
        this.name = FileNameHelper.sanitizeName(name, fallback);
    }

    /**
     * Get the display name for this chained ability.
     * Returns displayName if set, otherwise falls back to name.
     * Converts &amp; color codes to § for rendering.
     */
    public String getDisplayName() {
        String result = (displayName != null && !displayName.isEmpty())
            ? displayName
            : FileNameHelper.toDisplayName(name);
        return result != null ? result.replaceAll("&([0-9a-fk-or])", "\u00A7$1") : "";
    }

    public String getRawDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName : "";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = Math.max(1, weight);
    }

    public boolean isWindUpAll() {
        return windUpAll;
    }

    public void setWindUpAll(boolean windUpAll) {
        this.windUpAll = windUpAll;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = Math.max(0, cooldownTicks);
    }

    public float getMinRange() {
        return minRange;
    }

    public void setMinRange(float minRange) {
        this.minRange = Math.max(0, minRange);
    }

    public float getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(float maxRange) {
        this.maxRange = Math.max(0, maxRange);
    }

    /**
     * Compute the allowed user type from child abilities.
     * A chain allows players if ALL resolved entries allow players.
     * A chain allows NPCs if ALL resolved entries allow NPCs.
     * Unresolvable entries are skipped (don't restrict).
     */
    public UserType getAllowedBy() {
        if (entries.isEmpty()) return UserType.BOTH;
        boolean allAllowPlayer = true;
        boolean allAllowNpc = true;
        for (ChainedAbilityEntry entry : entries) {
            Ability a = entry.resolve();
            if (a == null) continue;
            UserType ut = a.getAllowedBy();
            if (!ut.allowsPlayer()) allAllowPlayer = false;
            if (!ut.allowsNpc()) allAllowNpc = false;
        }
        if (allAllowPlayer && allAllowNpc) return UserType.BOTH;
        if (allAllowPlayer) return UserType.PLAYER_ONLY;
        if (allAllowNpc) return UserType.NPC_ONLY;
        return UserType.NONE;
    }

    public List<AbilityCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<AbilityCondition> conditions) {
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }

    public List<ChainedAbilityEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ChainedAbilityEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
    }

    public NBTTagCompound getCustomData() {
        return customData;
    }

    // ═══════════════════════════════════════════════════════════════════
    // IChainedAbility INTERFACE
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public int getEntryCount() {
        return entries.size();
    }

    @Override
    public String getEntryReference(int index) {
        if (index < 0 || index >= entries.size()) return null;
        ChainedAbilityEntry entry = entries.get(index);
        if (entry.isInline()) {
            Ability a = entry.getInlineAbility();
            return a != null ? a.getName() : "";
        }
        return entry.getAbilityReference();
    }

    @Override
    public boolean isEntryInline(int index) {
        if (index < 0 || index >= entries.size()) return false;
        return entries.get(index).isInline();
    }

    @Override
    public int getEntryDelay(int index) {
        if (index < 0 || index >= entries.size()) return 0;
        return entries.get(index).getDelayTicks();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ENTRY MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    public void addEntry(ChainedAbilityEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    public void addEntry(String abilityReference, int delayTicks) {
        entries.add(new ChainedAbilityEntry(abilityReference, delayTicks));
    }

    public void removeEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
        }
    }

    public void moveEntryUp(int index) {
        if (index > 0 && index < entries.size()) {
            ChainedAbilityEntry entry = entries.remove(index);
            entries.add(index - 1, entry);
        }
    }

    public void moveEntryDown(int index) {
        if (index >= 0 && index < entries.size() - 1) {
            ChainedAbilityEntry entry = entries.remove(index);
            entries.add(index + 1, entry);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONDITIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check all chain-level conditions against the caster and target.
     */
    public boolean checkConditions(EntityLivingBase caster, EntityLivingBase target) {
        for (AbilityCondition c : conditions) {
            if (!c.getUserType().allowsNpc()) continue;
            Boolean extendedCondition = AbilityController.Instance.fireCheckConditions(c, caster, target);

            if (extendedCondition != null) {
                if (!extendedCondition) return false;
                continue;
            }

            if (!c.check(caster, target)) return false;
        }
        return true;
    }

    /**
     * Check conditions for player use (skip target-requiring conditions).
     */
    public boolean checkConditionsForPlayer(EntityLivingBase caster) {
        for (AbilityCondition c : conditions) {
            if (!c.getUserType().allowsPlayer()) continue;
            if (c.requiresTarget()) continue;
            Boolean extendedCondition = AbilityController.Instance.fireCheckConditionsForPlayer(c, caster);

            if (extendedCondition != null) {
                if (!extendedCondition) return false;
                continue;
            }

            if (!c.check(caster, null)) return false;
        }
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Validate this chained ability. Returns a list of error messages.
     * An empty list means valid.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isEmpty()) {
            errors.add("Name is required");
        }

        if (entries.isEmpty()) {
            errors.add("At least one ability entry is required");
        }

        AbilityController controller = AbilityController.Instance;
        if (controller != null) {
            for (int i = 0; i < entries.size(); i++) {
                ChainedAbilityEntry entry = entries.get(i);
                if (entry.isInline()) {
                    if (entry.getInlineAbility() == null) {
                        errors.add("Entry " + (i + 1) + ": inline ability is null");
                    }
                } else {
                    String ref = entry.getAbilityReference();
                    if (ref == null || ref.isEmpty()) {
                        errors.add("Entry " + (i + 1) + ": ability reference is empty");
                    } else if (!controller.canResolveAbility(ref)) {
                        errors.add("Entry " + (i + 1) + ": cannot resolve ability '" + ref + "'");
                    }
                }
            }
        }

        return errors;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DEEP COPY
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Create a deep copy via NBT round-trip.
     */
    public ChainedAbility deepCopy() {
        ChainedAbility copy = new ChainedAbility();
        copy.readNBT(this.writeNBT(false));
        return copy;
    }

    @Override
    public boolean isChain() {
        return true;
    }

    @Override
    public IAbilityAction deepCopyAction() {
        return deepCopy();
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT(boolean saveScripts) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Id", id);
        nbt.setString("Name", name);
        nbt.setString("DisplayName", displayName);
        nbt.setBoolean("Enabled", enabled);
        nbt.setInteger("Weight", weight);
        nbt.setBoolean("WindUpAll", windUpAll);
        nbt.setInteger("CooldownTicks", cooldownTicks);
        nbt.setFloat("MinRange", minRange);
        nbt.setFloat("MaxRange", maxRange);

        // Conditions
        NBTTagList condList = new NBTTagList();
        for (AbilityCondition c : conditions) {
            condList.appendTag(c.writeNBT());
        }
        nbt.setTag("Conditions", condList);

        // Entries
        NBTTagList entryList = new NBTTagList();
        for (ChainedAbilityEntry entry : entries) {
            entryList.appendTag(entry.writeNBT(saveScripts));
        }
        nbt.setTag("Entries", entryList);

        // Custom data (for external mods)
        nbt.setTag("customData", (NBTTagCompound) customData.copy());

        // Script handler data (matching CustomEffect.writeToNBT pattern)
        if (saveScripts) {
            ChainedAbilityScript handler = getScriptHandler();
            if (handler != null) {
                NBTTagCompound scriptData = new NBTTagCompound();
                handler.writeToNBT(scriptData);
                nbt.setTag("ScriptData", scriptData);
            }
        }

        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        id = nbt.getString("Id");
        setName(nbt.getString("Name"));
        displayName = nbt.getString("DisplayName");
        enabled = !nbt.hasKey("Enabled") || nbt.getBoolean("Enabled");
        weight = nbt.hasKey("Weight") ? nbt.getInteger("Weight") : 10;
        windUpAll = !nbt.hasKey("WindUpAll") || nbt.getBoolean("WindUpAll");
        cooldownTicks = nbt.hasKey("CooldownTicks") ? nbt.getInteger("CooldownTicks") : 100;
        minRange = nbt.getFloat("MinRange");
        maxRange = nbt.hasKey("MaxRange") ? nbt.getFloat("MaxRange") : 20;
        // AllowedBy is computed from child abilities

        // Conditions
        conditions.clear();
        NBTTagList condList = nbt.getTagList("Conditions", 10);
        for (int i = 0; i < condList.tagCount(); i++) {
            AbilityCondition c = AbilityCondition.fromNBT(condList.getCompoundTagAt(i));
            if (c != null) {
                conditions.add(c);
            }
        }

        // Entries
        entries.clear();
        NBTTagList entryList = nbt.getTagList("Entries", 10);
        for (int i = 0; i < entryList.tagCount(); i++) {
            ChainedAbilityEntry entry = ChainedAbilityEntry.fromNBT(entryList.getCompoundTagAt(i));
            if (entry != null) {
                entries.add(entry);
            }
        }

        // Custom data (for external mods)
        customData = (NBTTagCompound) nbt.getCompoundTag("customData").copy();

        // Script handler data (matching CustomEffect.readFromNBT pattern)
        if (nbt.hasKey("ScriptData", 10)) {
            if (getScriptHandler() == null) {
                ChainedAbilityScript handler = new ChainedAbilityScript(this.id);
                handler.readFromNBT(nbt.getCompoundTag("ScriptData"));
                setScriptHandler(handler);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SCRIPT HANDLER
    // ═══════════════════════════════════════════════════════════════════

    public ChainedAbilityScript getScriptHandler() {
        return AbilityController.Instance.chainedAbilityScriptHandlers.get(this.id);
    }

    public void setScriptHandler(ChainedAbilityScript handler) {
        AbilityController.Instance.chainedAbilityScriptHandlers.put(this.id, handler);
    }

    public ChainedAbilityScript getOrCreateScriptHandler() {
        ChainedAbilityScript handler = getScriptHandler();
        if (handler == null) {
            handler = new ChainedAbilityScript(this.id);
            AbilityController.Instance.chainedAbilityScriptHandlers.put(this.id, handler);
        }
        return handler;
    }

    /**
     * Get or create a per-instance script handler for this chain execution.
     * Shares the template's engine but creates isolated Bindings for variable state.
     * Returns null if no script is configured on the template.
     */
    public ChainedAbilityScript getOrCreateInstanceScript() {
        if (instanceScript != null) return instanceScript;

        ChainedAbilityScript template = getScriptHandler();
        if (template == null || !template.getEnabled() || template.container == null) return null;

        instanceScript = new ChainedAbilityScript(this.id);
        instanceScript.setLanguage(template.getLanguage());
        instanceScript.setEnabled(true);

        ScriptContainer clone = ((ScriptContainer) template.container).createInstanceScope(instanceScript);
        instanceScript.addScriptUnit(clone);

        return instanceScript;
    }

    /**
     * Clear the per-instance script handler (call on chain completion/reset).
     */
    public void clearInstanceScript() {
        this.instanceScript = null;
    }
}
