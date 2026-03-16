package kamkeel.npcs.controllers.data.ability.data.effect;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
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
     * Applies this custom effect to the given IEntity.
     * Only works on players (CustomEffectController is player-only).
     */
    public void apply(IEntityLivingBase IEntity) {
        if (IEntity == null || effectId <= 0) return;
        if (IEntity instanceof IPlayer) {
            CustomEffectController.getInstance().applyEffect(
                (IPlayer) IEntity, effectId, durationTicks, level, index);
        }
    }

    public boolean isValid() {
        return effectId > 0;
    }

    // ── NBT ──

    public INbt writeNBT() {
        INbt nbt = new INbt();
        nbt.setInteger("effectId", effectId);
        nbt.setInteger("duration", durationTicks);
        nbt.setByte("level", level);
        nbt.setInteger("index", index);
        return nbt;
    }

    public void readNBT(INbt nbt) {
        this.effectId = nbt.getInteger("effectId");
        this.durationTicks = nbt.getInteger("duration");
        this.level = nbt.getByte("level");
        this.index = nbt.getInteger("index");
    }

    public static AbilityCustomEffect fromNBT(INbt nbt) {
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
        this.index = Math.max(0, index);
    }
}
