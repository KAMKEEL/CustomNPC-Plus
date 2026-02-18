package kamkeel.npcs.controllers.data.ability;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A single entry in a {@link ChainedAbility}, representing one ability
 * to execute in sequence along with its delay configuration.
 */
public class ChainedAbilityEntry {

    /** Reference key (built-in name or custom ability name). */
    private String abilityReference = "";

    /** Delay in ticks BEFORE this ability starts (after previous entry completes). */
    private int delayTicks = 0;

    public ChainedAbilityEntry() {}

    public ChainedAbilityEntry(String abilityReference, int delayTicks) {
        this.abilityReference = abilityReference != null ? abilityReference : "";
        this.delayTicks = Math.max(0, delayTicks);
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

    public int getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = Math.max(0, delayTicks);
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Reference", abilityReference);
        nbt.setInteger("Delay", delayTicks);
        return nbt;
    }

    public static ChainedAbilityEntry fromNBT(NBTTagCompound nbt) {
        if (nbt == null) return null;

        ChainedAbilityEntry entry = new ChainedAbilityEntry();
        entry.abilityReference = nbt.getString("Reference");
        entry.delayTicks = Math.max(0, nbt.getInteger("Delay"));
        return entry;
    }
}
