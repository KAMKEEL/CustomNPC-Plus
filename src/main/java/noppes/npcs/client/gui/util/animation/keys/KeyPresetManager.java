package noppes.npcs.client.gui.util.animation.keys;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KeyPresetManager {
    public List<KeyPreset> keys = new ArrayList<>();
    public String fileName;

    public KeyPresetManager(String fileName) {
        this.fileName = fileName;
        load();
    }

    public KeyPreset add(String description, int keyCode, boolean isMouseKey) {
        KeyPreset preset = new KeyPreset(description, keyCode, isMouseKey);
        keys.add(preset);
        return preset;
    }

    public void tick() {
        for (KeyPreset key : keys)
            key.tick();
    }

    public void load() {
        try {
            File dir = getDir();
            File file = new File(dir, fileName + ".json");
            if (!file.exists())
                return;

            NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
            for (KeyPreset key : keys)
                key.readFromNbt(compound);

        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public void save() {
        try {
            File dir = getDir();
            String filename = fileName + ".json";

            File newFile = new File(dir, filename + "_new");
            File originalFile = new File(dir, filename);

            NBTTagCompound compound = new NBTTagCompound();
            for (KeyPreset key : keys)
                key.writeToNbt(compound);

            NBTJsonUtil.SaveFile(newFile, compound);

            if (originalFile.exists())
                originalFile.delete();

            newFile.renameTo(originalFile);
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public File getDir() {
        File dir = new File(CustomNpcs.Dir, "keypresets");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }
}
