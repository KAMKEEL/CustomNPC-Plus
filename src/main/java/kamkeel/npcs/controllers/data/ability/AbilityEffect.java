package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * Represents a configurable potion effect that can be applied by abilities.
 * Provides a unified system for managing debuffs like slowness, weakness, poison, etc.
 */
public class AbilityEffect {

    public enum EffectType {
        NONE("gui.none", -1),
        SLOWNESS("potion.moveSlowdown", Potion.moveSlowdown.id),
        WEAKNESS("potion.weakness", Potion.weakness.id),
        POISON("potion.poison", Potion.poison.id),
        WITHER("potion.wither", Potion.wither.id),
        BLINDNESS("potion.blindness", Potion.blindness.id),
        NAUSEA("potion.confusion", Potion.confusion.id),
        HUNGER("potion.hunger", Potion.hunger.id),
        MINING_FATIGUE("potion.digSlowDown", Potion.digSlowdown.id);

        private final String langKey;
        private final int potionId;

        EffectType(String langKey, int potionId) {
            this.langKey = langKey;
            this.potionId = potionId;
        }

        public String getLangKey() {
            return langKey;
        }

        public int getPotionId() {
            return potionId;
        }

        public static EffectType fromOrdinal(int ordinal) {
            EffectType[] values = values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
            return NONE;
        }
    }

    private EffectType type = EffectType.NONE;
    private int durationTicks = 60;
    private int amplifier = 0;  // 0-10

    public AbilityEffect() {
    }

    public AbilityEffect(EffectType type, int durationTicks, int amplifier) {
        this.type = type;
        this.durationTicks = Math.max(1, durationTicks);
        this.amplifier = Math.max(0, Math.min(10, amplifier));
    }

    /**
     * Creates a copy of this effect.
     */
    public AbilityEffect copy() {
        return new AbilityEffect(type, durationTicks, amplifier);
    }

    /**
     * Applies this effect to the given entity.
     * Does nothing if type is NONE.
     */
    public void apply(EntityLivingBase entity) {
        if (entity == null || type == EffectType.NONE) {
            return;
        }

        int potionId = type.getPotionId();
        if (potionId >= 0) {
            entity.addPotionEffect(new PotionEffect(potionId, durationTicks, amplifier));
        }
    }

    /**
     * Writes this effect to NBT.
     */
    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", type.ordinal());
        nbt.setInteger("duration", durationTicks);
        nbt.setInteger("amplifier", amplifier);
        return nbt;
    }

    /**
     * Reads this effect from NBT.
     */
    public void readNBT(NBTTagCompound nbt) {
        this.type = EffectType.fromOrdinal(nbt.getInteger("type"));
        this.durationTicks = nbt.hasKey("duration") ? nbt.getInteger("duration") : 60;
        this.amplifier = nbt.hasKey("amplifier") ? nbt.getInteger("amplifier") : 0;
    }

    /**
     * Creates an AbilityEffect from NBT.
     */
    public static AbilityEffect fromNBT(NBTTagCompound nbt) {
        AbilityEffect effect = new AbilityEffect();
        effect.readNBT(nbt);
        return effect;
    }

    // Getters & Setters

    public EffectType getType() {
        return type;
    }

    public void setType(EffectType type) {
        this.type = type != null ? type : EffectType.NONE;
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
        this.amplifier = Math.max(0, Math.min(10, amplifier));
    }

    /**
     * Returns true if this effect is configured (not NONE).
     */
    public boolean isValid() {
        return type != EffectType.NONE;
    }
}
