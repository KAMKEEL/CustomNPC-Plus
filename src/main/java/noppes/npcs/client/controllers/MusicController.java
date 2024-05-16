package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class MusicController {
	public static MusicController Instance;
    private final HashSet<ScriptClientSound> sounds = new HashSet<>();
    private int playDelay;
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

        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        this.sounds.removeIf(sound -> !soundHandler.isSoundPlaying(sound) && !sound.canRepeat() && !sound.paused);

        if (this.playDelay > 0) {
            this.playDelay--;
        } else if (Minecraft.getMinecraft().currentScreen != null) {
            this.playDelay = 20;
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
        if (this.playDelay > 0) {
            return;
        }
        this.playDelay = 10;

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
        this.sounds.add(clientSound);

        try {
            SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
            soundHandler.playSound(clientSound);
        } catch (Exception e) {
            this.stopAllSounds();
        }
	}

	public void playMusicBackground(String music, Entity entity, int offRange) {
        if (this.playDelay > 0) {
            return;
        }
        this.playDelay = 10;

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
        this.sounds.add(clientSound);

        try {
            SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
            soundHandler.playSound(clientSound);
        } catch (Exception e) {
            this.stopAllSounds();
        }
	}

    public void playSound(String music, float x, float y, float z) {
        if (this.playDelay > 0) {
            return;
        }
        this.playDelay = 10;

        ScriptClientSound clientSound = new ScriptClientSound(music);
        clientSound.setPos(x, y, z);
        clientSound.setVolume(1.0F);
        clientSound.setAttenuationType(ISound.AttenuationType.NONE);
        clientSound.setRepeat(false);

        try {
            SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
            soundHandler.playSound(clientSound);
        } catch (Exception e) {
            this.stopAllSounds();
            return;
        }
        this.sounds.add(clientSound);
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

    public void stopAllSounds() {
        this.stopMusic();
        for (ScriptClientSound sound : this.sounds) {
            sound.stopSound();
        }
        this.sounds.clear();
    }
}
