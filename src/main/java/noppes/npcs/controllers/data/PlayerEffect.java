package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IPlayerEffect;
import noppes.npcs.controllers.StatusEffectController;

public class PlayerEffect implements IPlayerEffect {
    public int id;
    public int duration;
    public byte level;
    public int index;

    public PlayerEffect(int id, int duration, byte level) {
        this.id = id;
        this.duration = duration;
        this.level = level;
    }

    public static PlayerEffect readEffectData(NBTTagCompound nbt) {
        int id = nbt.getInteger("Id");
        int index = nbt.getInteger("Index");
        if (id > 0) {
            StatusEffectController controller = StatusEffectController.getInstance();
            boolean found = controller.has(id);
//                StatusEffectController.getInstance().customEffects.containsKey(id);
            if (found) {
                byte level = nbt.getByte("Level");
                int dur = nbt.getInteger("Dur");
                return new PlayerEffect(id, dur, level);
            }
        }
        return null;
    }

    public NBTTagCompound writeEffectData(NBTTagCompound nbt) {
        nbt.setInteger("Id", this.id);
        nbt.setByte("Level", this.level);
        nbt.setInteger("Dur", this.duration);
        nbt.setInteger("Index", this.index);
        return nbt;
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
        CustomEffect effect = StatusEffectController.getInstance().get(this.id);
        if(effect != null)
            return effect.getName();

        return "UNKNOWN";
    }

    @Override
    public void performEffect(IPlayer player) {
        if (player != null && player.getMCEntity() != null && player.getMCEntity() instanceof EntityPlayer) {
            CustomEffect effect = StatusEffectController.getInstance().get(this.id);
            if(effect != null)
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
