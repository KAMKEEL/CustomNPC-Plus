package kamkeel.npcs.controllers.data.ability.data.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import noppes.npcs.constants.EnumPotionType;

/**
 * Represents a configurable potion effect that can be applied by abilities.
 * Uses the unified EnumPotionType enum shared across melee, ranged, and ability systems.
 */
public class AbilityPotionEffect {

    private EnumPotionType type = EnumPotionType.None;
    private int manualPotionId = 0;
    private int durationTicks = 60;
    private int amplifier = 0;  // 0-255 (Manual), 0-10 (preset)

    public AbilityPotionEffect() {
    }

    public AbilityPotionEffect(EnumPotionType type, int durationTicks, int amplifier) {
        this.type = type;
        this.durationTicks = Math.max(1, durationTicks);
        this.amplifier = Math.max(0, Math.min(255, amplifier));
    }

    /**
     * Creates a copy of this effect.
     */
    public AbilityPotionEffect copy() {
        AbilityPotionEffect copy = new AbilityPotionEffect(type, durationTicks, amplifier);
        copy.manualPotionId = this.manualPotionId;
        return copy;
    }

    /**
     * Applies this effect to the given entity.
     * Does nothing if type is None. Validates potion IDs before applying.
     */
    public void apply(EntityLivingBase entity) {
        if (entity == null || type == EnumPotionType.None) {
            return;
        }

        if (type == EnumPotionType.Fire) {
            entity.setFire(Math.max(1, durationTicks / 20));
            return;
        }

        int potionId = type.getResolvedPotionId(manualPotionId);
        if (EnumPotionType.isValidPotionId(potionId)) {
            entity.addPotionEffect(new PotionEffect(potionId, durationTicks, amplifier));
        }
    }

    /**
     * Writes this effect to NBT.
     */
    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("potionType", type.ordinal());
        nbt.setInteger("duration", durationTicks);
        nbt.setInteger("amplifier", amplifier);
        if (type == EnumPotionType.Manual) {
            nbt.setInteger("manualPotionId", manualPotionId);
        }
        return nbt;
    }

    /**
     * Reads this effect from NBT.
     */
    public void readNBT(NBTTagCompound nbt) {
        this.type = EnumPotionType.fromOrdinal(nbt.getInteger("potionType"));
        this.durationTicks = nbt.getInteger("duration");
        this.amplifier = nbt.getInteger("amplifier");
        if (type == EnumPotionType.Manual) {
            this.manualPotionId = nbt.getInteger("manualPotionId");
        }
    }

    /**
     * Creates an AbilityPotionEffect from NBT.
     */
    public static AbilityPotionEffect fromNBT(NBTTagCompound nbt) {
        AbilityPotionEffect effect = new AbilityPotionEffect();
        effect.readNBT(nbt);
        return effect;
    }

    // Getters & Setters

    public EnumPotionType getType() {
        return type;
    }

    public void setType(EnumPotionType type) {
        this.type = type != null ? type : EnumPotionType.None;
    }

    public int getManualPotionId() {
        return manualPotionId;
    }

    public void setManualPotionId(int manualPotionId) {
        this.manualPotionId = Math.max(0, manualPotionId);
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = Math.max(0, Math.min(255, amplifier));
    }

    /**
     * Returns true if this effect is configured (not None).
     */
    public boolean isValid() {
        return type != EnumPotionType.None;
    }
}
