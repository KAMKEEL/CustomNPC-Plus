package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IPlayerEffect;
import noppes.npcs.controllers.CustomEffectController;

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
        CustomEffect effect = CustomEffectController.getInstance().get(this.id, this.index);
        if (effect != null)
            return effect.getName();

        return "UNKNOWN";
    }

    @Override
    public void performEffect(IPlayer player) {
        if (player != null && player.getMCEntity() != null && player.getMCEntity() instanceof EntityPlayer) {
            CustomEffect effect = CustomEffectController.getInstance().get(this.id, this.index);
            if (effect != null)
                effect.onTick((EntityPlayer) player.getMCEntity(), this);
        }
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
