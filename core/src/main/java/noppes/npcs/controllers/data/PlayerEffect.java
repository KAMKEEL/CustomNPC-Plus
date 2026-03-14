package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.IPlayerEffect;

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
     * it depends on IPlayer which has MC entity dependencies.
     * Override in mc1710 to restore original behavior.
     */
    public void performEffect(Object player) {
        // TODO: Requires CustomEffectController + IPlayer - implement in mc1710 override
        // OLD CODE:
        // if (player != null && player.getMCEntity() != null && player.getMCEntity() instanceof EntityPlayer) {
        //     CustomEffect effect = CustomEffectController.getInstance().get(this.id, this.index);
        //     if (effect != null)
        //         effect.onTick((EntityPlayer) player.getMCEntity(), this);
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
