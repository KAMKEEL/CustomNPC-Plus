package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import noppes.npcs.scripted.ScriptSound;

public class ScriptClientSound extends MovingSound implements ITickableSound {
    public final String sound;
    private Entity entity;
    public boolean paused;

    public ScriptClientSound(String sound) {
        super(new ResourceLocation(sound));
        this.sound = sound;
    }

    public static ScriptClientSound fromScriptSound(NBTTagCompound compound, World world) {
        ScriptSound sound = ScriptSound.fromNBT(compound);
        ScriptClientSound clientSound = new ScriptClientSound(sound.directory);
        clientSound.xPosF = sound.xPosF;
        clientSound.yPosF = sound.yPosF;
        clientSound.zPosF = sound.zPosF;
        clientSound.volume = sound.volume;
        clientSound.field_147663_c = sound.pitch;
        clientSound.repeat = sound.repeat;
        clientSound.field_147665_h = sound.repeatDelay;
        if (compound.hasKey("EntityID")) {
            clientSound.setEntity(world.getEntityByID(compound.getInteger("EntityID")));
            clientSound.field_147666_i = AttenuationType.NONE;
        }
        return clientSound;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        if (entity != null) {
            this.xPosF = (float) (this.entity.posX);
            this.yPosF = (float) (this.entity.posY);
            this.zPosF = (float) (this.entity.posZ);
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void stopSound() {
        this.repeat = false;
        this.volume = 0;
    }

    @Override
    public void update() {
        if (entity != null) {
            if (this.entity.isDead) {
                this.donePlaying = true;
            } else {
                this.xPosF = (float) (this.entity.posX);
                this.yPosF = (float) (this.entity.posY);
                this.zPosF = (float) (this.entity.posZ);
            }
        }
    }

    public void setPos(float x, float y, float z) {
        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;
    }

    public double getDistance() {
        return Minecraft.getMinecraft().thePlayer.getDistance(this.xPosF, this.yPosF, this.zPosF);
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setAttenuationType(AttenuationType attenuationType) {
        this.field_147666_i = attenuationType;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
