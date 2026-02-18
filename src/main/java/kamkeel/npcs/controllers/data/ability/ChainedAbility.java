package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.ability.IChainedAbility;

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

    private String name = "";

    private boolean enabled = true;

    /** Weight for NPC weighted random selection. */
    private int weight = 10;

    /** Whether each ability in the chain uses its own windup phase. */
    private boolean windUpAll = true;

    /** Chain-level cooldown in ticks (applied after entire chain completes). */
    private int cooldownTicks = 100;

    /** Minimum range for NPC eligibility check. */
    private float minRange = 0;

    /** Maximum range for NPC eligibility check. */
    private float maxRange = 20;

    /** Which entity types can use this chained ability. */
    private UserType allowedBy = UserType.BOTH;

    /** Chain-level conditions (individual ability conditions are ignored). */
    private List<Condition> conditions = new ArrayList<>();

    /** Ordered list of ability entries to execute. */
    private List<ChainedAbilityEntry> entries = new ArrayList<>();

    public ChainedAbility() {}

    public ChainedAbility(String name) {
        this.name = name != null ? name : "";
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
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

    public UserType getAllowedBy() {
        return allowedBy;
    }

    public void setAllowedBy(UserType allowedBy) {
        this.allowedBy = allowedBy != null ? allowedBy : UserType.BOTH;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }

    public List<ChainedAbilityEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ChainedAbilityEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
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
        return entries.get(index).getAbilityReference();
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
        for (Condition c : conditions) {
            if (!c.check(caster, target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check conditions for player use (skip target-requiring conditions).
     */
    public boolean checkConditionsForPlayer(EntityLivingBase caster) {
        for (Condition c : conditions) {
            if (c.requiresTarget()) continue;
            if (!c.check(caster, null)) {
                return false;
            }
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
                String ref = entry.getAbilityReference();
                if (ref == null || ref.isEmpty()) {
                    errors.add("Entry " + (i + 1) + ": ability reference is empty");
                } else if (!controller.canResolveAbility(ref)) {
                    errors.add("Entry " + (i + 1) + ": cannot resolve ability '" + ref + "'");
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
        copy.readNBT(this.writeNBT());
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

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Name", name);
        nbt.setBoolean("Enabled", enabled);
        nbt.setInteger("Weight", weight);
        nbt.setBoolean("WindUpAll", windUpAll);
        nbt.setInteger("CooldownTicks", cooldownTicks);
        nbt.setFloat("MinRange", minRange);
        nbt.setFloat("MaxRange", maxRange);
        nbt.setInteger("AllowedBy", allowedBy.ordinal());

        // Conditions
        NBTTagList condList = new NBTTagList();
        for (Condition c : conditions) {
            condList.appendTag(c.writeNBT());
        }
        nbt.setTag("Conditions", condList);

        // Entries
        NBTTagList entryList = new NBTTagList();
        for (ChainedAbilityEntry entry : entries) {
            entryList.appendTag(entry.writeNBT());
        }
        nbt.setTag("Entries", entryList);

        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        name = nbt.getString("Name");
        enabled = !nbt.hasKey("Enabled") || nbt.getBoolean("Enabled");
        weight = nbt.hasKey("Weight") ? nbt.getInteger("Weight") : 10;
        windUpAll = !nbt.hasKey("WindUpAll") || nbt.getBoolean("WindUpAll");
        cooldownTicks = nbt.hasKey("CooldownTicks") ? nbt.getInteger("CooldownTicks") : 100;
        minRange = nbt.getFloat("MinRange");
        maxRange = nbt.hasKey("MaxRange") ? nbt.getFloat("MaxRange") : 20;
        allowedBy = UserType.fromOrdinal(nbt.getInteger("AllowedBy"));

        // Conditions
        conditions.clear();
        NBTTagList condList = nbt.getTagList("Conditions", 10);
        for (int i = 0; i < condList.tagCount(); i++) {
            Condition c = Condition.fromNBT(condList.getCompoundTagAt(i));
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
    }
}
