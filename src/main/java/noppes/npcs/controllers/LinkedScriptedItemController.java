package noppes.npcs.controllers;


import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LinkedScriptedItemController {
    public static LinkedScriptedItemController Instance;
    public List<LinkedScriptedItemData> list = new ArrayList<>();

    public LinkedScriptedItemController() {
        Instance = this;
        load();
    }

    public File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "linkeditems");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public void load() {
        File dir = getDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if(files == null) return;
        for (File file : files) {
            try {
                NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
                LinkedScriptedItemData data = new LinkedScriptedItemData();
                data.loadFromNBT(compound);
                list.add(data);
            } catch (Exception e) {
                LogWriter.error("Error loading linked items: " + file.getAbsolutePath(), e);
            }
        }
    }

    public void save() {
        File dir = getDir();
        for (LinkedScriptedItemData data : list) {
            try {
                File file = new File(dir, data.name + ".json_new");
                NBTJsonUtil.SaveFile(file, data.saveToNBT());
            } catch (IOException e) {
                LogWriter.except(e);
            } catch (JsonException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public LinkedScriptedItemData getData(String name) {
        for (LinkedScriptedItemData data : list) {
            if (data.name.equalsIgnoreCase(name)) {
                return data;
            }
        }
        return null;
    }

    public void addData(String name, NBTTagCompound scriptData) {
        if (name.isEmpty() || getData(name) != null)
            return;
        LinkedScriptedItemData data = new LinkedScriptedItemData();
        data.name = name;
        data.scriptData = scriptData;
        data.time = System.currentTimeMillis();
        list.add(data);
        save();
    }

    public void updateData(String name, NBTTagCompound scriptData) {
        LinkedScriptedItemData data = getData(name);
        if (data != null) {
            data.scriptData = scriptData;
            data.time = System.currentTimeMillis();
            save();
        }
    }

    public static class LinkedScriptedItemData {
        public String name = "LinkedScriptedItem";
        public long time;
        public NBTTagCompound scriptData = new NBTTagCompound();

        public LinkedScriptedItemData() {
            time = System.currentTimeMillis();
        }

        public void loadFromNBT(NBTTagCompound compound) {
            name = compound.getString("LinkedScriptName");
            scriptData = compound.getCompoundTag("ScriptData");
            time = compound.getLong("Time");
        }

        public NBTTagCompound saveToNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("LinkedScriptName", name);
            compound.setTag("ScriptData", scriptData);
            compound.setLong("Time", time);
            return compound;
        }
    }
}
