package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScriptSoundController {
    public HashMap<Integer, ScriptClientSound> sounds = new HashMap<>();
    public static ScriptSoundController Instance;

    public ScriptSoundController() { 
        Instance = this;
    }
    
    public void onUpdate() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        final Set<Map.Entry<Integer, ScriptClientSound>> entrySet = sounds.entrySet();
        for (Map.Entry<Integer, ScriptClientSound> entry : entrySet) {
            ScriptClientSound sound = entry.getValue();
            if (!soundHandler.isSoundPlaying(sound) && !sound.canRepeat() && !sound.paused) {
                sounds.remove(entry.getKey());
            }
        }
    }

    public void playSound(int id, ScriptClientSound sound) {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        if (sounds.containsKey(id)) {
            soundHandler.stopSound(sound);
            sounds.get(id).stopSound();
        }
        sounds.put(id,sound);
        soundHandler.playSound(sound);
    }

    public void stopSound(int id) {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        if (sounds.containsKey(id)) {
            soundHandler.stopSound(sounds.get(id));
            sounds.get(id).stopSound();
            sounds.remove(id);
        }
    }

    public void pauseAllSounds() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.pauseSounds();
        for (ScriptClientSound sound : sounds.values()) {
            sound.paused = true;
        }
    }

    public void continueAllSounds() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.resumeSounds();
        for (ScriptClientSound sound : sounds.values()) {
            sound.paused = false;
        }
    }

    public void stopAllSounds() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        soundHandler.stopSounds();
        sounds.clear();
    }
}
