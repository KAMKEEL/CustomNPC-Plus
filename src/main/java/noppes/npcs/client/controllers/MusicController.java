package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class MusicController {
	public static MusicController Instance;
    public ScriptClientSound playingSound;

    private Entity entity;
    private int offRange;

	public MusicController(){
		Instance = this;
	}

    public void onUpdate() {
        if (this.playingSound != null
            && !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this.playingSound)) {
            Entity playingEntity = this.playingSound.getEntity();
            String sound = this.playingSound.sound;
            this.playingSound.stopSound();
            this.stopMusic();
            if (playingEntity == Minecraft.getMinecraft().thePlayer) {
                this.playMusicBackground(sound, this.entity, this.offRange);
            } else {
                this.playMusicJukebox(sound, this.entity, this.offRange);
            }
        }
    }

	public void stopMusic(){
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.stopSounds();
        if (this.isPlaying()) {
            this.playingSound.stopSound();
            this.playingSound = null;
        }
	}

	public void playMusicJukebox(String music, Entity entity, int offRange){
		if (this.isPlaying(music)) {
			return;
		}
        this.stopMusic();

        ScriptClientSound clientSound = new ScriptClientSound(music);
        clientSound.setEntity(entity);
        clientSound.setVolume(Math.max(1, offRange/16.0F));
        clientSound.setAttenuationType(ISound.AttenuationType.LINEAR);
        clientSound.setRepeat(true);
        this.playingSound = clientSound;
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
        this.playingSound = clientSound;
        this.entity = entity;
        this.offRange = offRange;

        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.playSound(clientSound);
	}

	public boolean isPlaying(String music) {
        if (this.playingSound != null && this.playingSound.sound.equals(music)) {
            ResourceLocation resource = new ResourceLocation(music);
            if (!this.playingSound.getPositionedSoundLocation().equals(resource)) {
                return false;
            }
            return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this.playingSound);
        } else {
            return false;
        }
	}

    public boolean isPlaying() {
        return this.playingSound != null;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public int getOffRange() {
        return this.offRange;
    }

    public double getDistance() {
        Entity player = Minecraft.getMinecraft().thePlayer;
        if (this.entity != null && this.entity.dimension == player.dimension) {
            return player.getDistanceToEntity(this.entity);
        }
        return Double.MAX_VALUE;
    }

	public void playSound(String music, float x, float y, float z) {
		Minecraft.getMinecraft().theWorld.playSound(x, y, z, music, 1, 1, false);
	}
}
