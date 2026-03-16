package noppes.npcs.controllers.data;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IPlayerEffect;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

public class PlayerEffect implements IPlayerEffect {
    public int id;
    public int duration;
    public byte level;
    public int index;

    public PlayerEffect(int id, int duration, byte level, int index) {
        this.id = id;
        this.duration = duration;
        this.level = level;
        this.index = index;
    }

    @Override
    public void kill() {
        duration = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public byte getLevel() {
        return level;
    }

    @Override
    public void setLevel(byte level) {
        this.level = level;
    }

    @Override
    public String getName() {
        // TODO: Requires CustomEffectController - implement via callback or platform service
        // OLD CODE:
        // CustomEffect effect = CustomEffectController.getInstance().get(this.id, this.index);
        // if (effect != null)
        //     return effect.getName();
        return "UNKNOWN";
    }

    /**
     * Applies this effect's tick logic to the given player.
     * This method is not part of the core IPlayerEffect interface because
     * it depends on IPlayer which has MC IEntity dependencies.
     * Override in mc1710 to restore original behavior.
     */
    public void performEffect(IPlayer player) {
        // Base implementation is a no-op. mc1710 shadow overrides to use
        // CustomEffectController for effect tick logic.

        // TODO: Requires CustomEffectController + IPlayer - implement in mc1710 override
        // OLD CODE:
        // if (player != null && player.getMCIEntity() != null && player.getMCIEntity() instanceof IPlayer) {
        //     CustomEffect effect = CustomEffectController.getInstance().get(this.id, this.index);
        //     if (effect != null)
        //         effect.onTick((IPlayer) player.getMCIEntity(), this);
        // }
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }
}
