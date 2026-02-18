package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.AbilityController;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A single entry in a {@link ChainedAbility}, representing one ability
 * to execute in sequence along with its delay configuration.
 * <p>
 * Entries can be either a <b>reference</b> to a registered ability (by name/key)
 * or an <b>inline</b> ability that is owned directly by this entry.
 */
public class ChainedAbilityEntry {

    public enum EntryType { REFERENCE, INLINE }

    private EntryType entryType = EntryType.REFERENCE;

    /** Reference key (built-in name or custom ability name). Used when entryType == REFERENCE. */
    private String abilityReference = "";

    /** Inline ability data. Used when entryType == INLINE. */
    private Ability inlineAbility = null;

    /** Delay in ticks BEFORE this ability starts (after previous entry completes). */
    private int delayTicks = 0;

    public ChainedAbilityEntry() {}

    public ChainedAbilityEntry(String abilityReference, int delayTicks) {
        this.entryType = EntryType.REFERENCE;
        this.abilityReference = abilityReference != null ? abilityReference : "";
        this.delayTicks = Math.max(0, delayTicks);
    }

    // ═══════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═══════════════════════════════════════════════════════════════════

    public static ChainedAbilityEntry reference(String ref, int delay) {
        ChainedAbilityEntry entry = new ChainedAbilityEntry();
        entry.entryType = EntryType.REFERENCE;
        entry.abilityReference = ref != null ? ref : "";
        entry.delayTicks = Math.max(0, delay);
        return entry;
    }

    public static ChainedAbilityEntry inline(Ability ability, int delay) {
        ChainedAbilityEntry entry = new ChainedAbilityEntry();
        entry.entryType = EntryType.INLINE;
        entry.inlineAbility = ability;
        entry.delayTicks = Math.max(0, delay);
        return entry;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TYPE QUERIES
    // ═══════════════════════════════════════════════════════════════════

    public EntryType getEntryType() {
        return entryType;
    }

    public boolean isInline() {
        return entryType == EntryType.INLINE;
    }

    public boolean isReference() {
        return entryType == EntryType.REFERENCE;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public String getAbilityReference() {
        return abilityReference;
    }

    public void setAbilityReference(String abilityReference) {
        this.abilityReference = abilityReference != null ? abilityReference : "";
    }

    public Ability getInlineAbility() {
        return inlineAbility;
    }

    public void setInlineAbility(Ability ability) {
        this.inlineAbility = ability;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = Math.max(0, delayTicks);
    }

    // ═══════════════════════════════════════════════════════════════════
    // RESOLUTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Resolve this entry to an Ability instance.
     * Inline entries return their ability directly.
     * Reference entries resolve via AbilityController.
     */
    public Ability resolve() {
        if (entryType == EntryType.INLINE) {
            return inlineAbility;
        }
        AbilityController ctrl = AbilityController.Instance;
        if (ctrl == null) return null;
        return ctrl.resolveAbility(abilityReference);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONVERSION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Convert a reference entry to inline by cloning the resolved ability.
     * Returns false if already inline or the reference cannot be resolved.
     */
    public boolean convertToInline() {
        if (entryType != EntryType.REFERENCE) return false;

        AbilityController ctrl = AbilityController.Instance;
        if (ctrl == null) return false;

        Ability resolved = ctrl.resolveAbility(abilityReference);
        if (resolved == null) return false;

        inlineAbility = ctrl.fromNBT(resolved.writeNBT());
        entryType = EntryType.INLINE;
        abilityReference = "";
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DEEP COPY
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Create a deep copy of this entry. Inline abilities are copied via NBT round-trip.
     */
    public ChainedAbilityEntry deepCopy() {
        ChainedAbilityEntry copy = new ChainedAbilityEntry();
        copy.entryType = this.entryType;
        copy.abilityReference = this.abilityReference;
        copy.delayTicks = this.delayTicks;
        if (this.inlineAbility != null) {
            copy.inlineAbility = AbilityController.Instance.fromNBT(this.inlineAbility.writeNBT());
        }
        return copy;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT (backwards compatible)
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("Delay", delayTicks);

        if (entryType == EntryType.INLINE && inlineAbility != null) {
            nbt.setTag("InlineAbility", inlineAbility.writeNBT());
        } else {
            nbt.setString("Reference", abilityReference);
        }
        return nbt;
    }

    public static ChainedAbilityEntry fromNBT(NBTTagCompound nbt) {
        if (nbt == null) return null;

        ChainedAbilityEntry entry = new ChainedAbilityEntry();
        entry.delayTicks = Math.max(0, nbt.getInteger("Delay"));

        // Inline entry (new format)
        if (nbt.hasKey("InlineAbility")) {
            NBTTagCompound abilityNBT = nbt.getCompoundTag("InlineAbility");
            entry.entryType = EntryType.INLINE;
            entry.inlineAbility = AbilityController.Instance != null
                ? AbilityController.Instance.fromNBT(abilityNBT) : null;
        } else {
            // Reference entry (old + new format)
            entry.entryType = EntryType.REFERENCE;
            entry.abilityReference = nbt.getString("Reference");
        }

        return entry;
    }
}
