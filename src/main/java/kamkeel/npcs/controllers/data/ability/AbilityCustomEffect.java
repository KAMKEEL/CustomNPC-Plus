package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.util.ValueUtil;

/**
 * Represents a custom effect reference that can be applied by abilities.
 * Wraps the CustomEffectController system (player-only effects with script callbacks).
 */
public class AbilityCustomEffect {

    private int effectId = -1;
    private int durationTicks = 60;
    private byte level = 0;
    private int index = 0;

    public AbilityCustomEffect() {
    }

    public AbilityCustomEffect(int effectId, int durationTicks, byte level) {
        this.effectId = effectId;
        this.durationTicks = Math.max(1, durationTicks);
        this.level = (byte) Math.max(0, Math.min(10, level));
        this.index = 0;
    }

    public AbilityCustomEffect(int effectId, int durationTicks, byte level, int index) {
        this(effectId, durationTicks, level);
        this.index = index;
    }

    public AbilityCustomEffect copy() {
        return new AbilityCustomEffect(effectId, durationTicks, level, index);
    }

    /**
     * Applies this custom effect to the given entity.
     * Only works on players (CustomEffectController is player-only).
     */
    public void apply(EntityLivingBase entity) {
        if (entity == null || effectId <= 0) return;
        if (entity instanceof EntityPlayer) {
            CustomEffectController.getInstance().applyEffect(
                (EntityPlayer) entity, effectId, durationTicks, level, index);
        }
    }

    public boolean isValid() {
        return effectId > 0;
    }

    // ── NBT ──

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("effectId", effectId);
        nbt.setInteger("duration", durationTicks);
        nbt.setByte("level", level);
        return nbt;
    }

    public void readNBT(NBTTagCompound nbt) {
        this.effectId = nbt.getInteger("effectId");
        this.durationTicks = nbt.getInteger("duration");
        this.level = nbt.getByte("level");
    }

    public static AbilityCustomEffect fromNBT(NBTTagCompound nbt) {
        AbilityCustomEffect e = new AbilityCustomEffect();
        e.readNBT(nbt);
        return e;
    }

    // ── Getters/Setters ──

    public int getEffectId() {
        return effectId;
    }

    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = (byte) ValueUtil.clamp(level, 0, 10);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = ValueUtil.clamp(index, 0, 1);
    }
}
