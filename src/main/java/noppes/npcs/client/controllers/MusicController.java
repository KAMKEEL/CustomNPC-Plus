package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class MusicController {
	public static MusicController Instance;
    public ScriptClientSound sound;

    private Entity entity;
    private int offRange;

	public MusicController(){
		Instance = this;
	}

	public void stopMusic(){
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.stopSounds();
        if (this.isPlaying()) {
            this.sound.stopSound();
            this.sound = null;
        }
	}

	public void playMusicJukebox(String music, Entity entity, int offRange){
		if (this.isPlaying(music)) {
			return;
		}
        this.stopMusic();

        ScriptClientSound clientSound = new ScriptClientSound(music);
        clientSound.setPos((float)entity.posX, (float)entity.posY, (float)entity.posZ);
        clientSound.setVolume(4.0F);
        clientSound.setAttenuationType(ISound.AttenuationType.LINEAR);
        clientSound.setRepeat(true);
        this.sound = clientSound;
        this.entity = entity;
        this.offRange = offRange;

        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.playSound(clientSound);
	}

	public void playMusicBackground(String music, Entity entity, int offRange) {
        if (this.isPlaying(music)) {
            return;
        }
        this.stopMusic();

        ScriptClientSound clientSound = new ScriptClientSound(music);
        clientSound.setEntity(Minecraft.getMinecraft().thePlayer);
        clientSound.setRepeat(true);
        this.sound = clientSound;
        this.entity = entity;
        this.offRange = offRange;

        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.playSound(clientSound);
	}

	public boolean isPlaying(String music) {
        if (this.sound != null && this.sound.sound.equals(music)) {
            ResourceLocation resource = new ResourceLocation(music);
            if (!this.sound.getPositionedSoundLocation().equals(resource)) {
                return false;
            }
            return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this.sound);
        } else {
            return false;
        }
	}

    public boolean isPlaying() {
        return this.sound != null;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public int getOffRange() {
        return this.offRange;
    }

	public void playSound(String music, float x, float y, float z) {
		Minecraft.getMinecraft().theWorld.playSound(x, y, z, music, 1, 1, false);
	}
}
