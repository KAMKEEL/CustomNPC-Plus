package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCategory;
import noppes.npcs.controllers.data.MagicAssociation;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class MagicController {
    public HashMap<Integer, Magic> magics = new HashMap<>();
    public HashMap<Integer, Magic> magicSync = new HashMap<>();

    public HashMap<Integer, MagicCategory> categoriesSync = new HashMap<>();
    public HashMap<Integer, MagicCategory> categories = new HashMap<>();
    public int lastUsedCategoryID = 0;

    private int lastUsedID = 0;
    private static MagicController instance;

    public MagicController() {
        instance = this;
    }

    public static MagicController getInstance() {
        return instance;
    }

    public Magic getMagic(int magicId) {
        return magics.get(magicId);
    }

    public MagicCategory getCategory(int categoryId) {
        return categories.get(categoryId);
    }

    public void load() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) return;
        try {
            File file = new File(saveDir, "magic.dat");
            if (file.exists()) {
                loadMagicFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "magic.dat_old");
                if (file.exists()) {
                    loadMagicFile(file);
                }
            } catch (Exception ee) { }
        }
    }

    private void loadMagicFile(File file) throws IOException {
        DataInputStream stream = new DataInputStream(
            new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)))
        );
        loadMagic(stream);
        stream.close();
    }

    public void loadMagic(DataInputStream stream) throws IOException {
        NBTTagCompound compound = CompressedStreamTools.read(stream);
        lastUsedID = compound.getInteger("lastID");

        // Load magics
        magics.clear();
        NBTTagList magicList = compound.getTagList("Magics", 10);
        for (int i = 0; i < magicList.tagCount(); i++) {
            NBTTagCompound magCompound = magicList.getCompoundTagAt(i);
            Magic mag = new Magic();
            mag.readNBT(magCompound);
            magics.put(mag.id, mag);
        }

        // Load categories
        categories.clear();
        NBTTagList catList = compound.getTagList("Categories", 10);
        for (int i = 0; i < catList.tagCount(); i++) {
            NBTTagCompound catCompound = catList.getCompoundTagAt(i);
            MagicCategory cat = new MagicCategory();
            cat.readNBT(catCompound);
            categories.put(cat.id, cat);
        }
    }

    public NBTTagCompound getNBT() {
        NBTTagList magicList = new NBTTagList();
        for (Magic mag : magics.values()) {
            NBTTagCompound magCompound = new NBTTagCompound();
            mag.writeNBT(magCompound);
            magicList.appendTag(magCompound);
        }
        NBTTagList catList = new NBTTagList();
        for (MagicCategory cat : categories.values()) {
            NBTTagCompound catCompound = new NBTTagCompound();
            cat.writeNBT(catCompound);
            catList.appendTag(catCompound);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("lastID", lastUsedID);
        compound.setTag("Magics", magicList);
        compound.setTag("Categories", catList);
        return compound;
    }

    public void saveMagicData() {
        try {
            File saveDir = CustomNpcs.getWorldSaveDirectory();
            File fileNew = new File(saveDir, "magic.dat_new");
            File fileOld = new File(saveDir, "magic.dat_old");
            File fileCurrent = new File(saveDir, "magic.dat");
            CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(fileNew));
            if (fileOld.exists()) fileOld.delete();
            fileCurrent.renameTo(fileOld);
            if (fileCurrent.exists()) fileCurrent.delete();
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) fileNew.delete();
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public void saveMagic(Magic mag) {
        if (mag.id < 0) {
            mag.id = getUnusedId();
            while (hasName(mag.name))
                mag.name += "_";
        } else {
            Magic existing = magics.get(mag.id);
            if (existing != null && !existing.name.equals(mag.name))
                while (hasName(mag.name))
                    mag.name += "_";
        }
        magics.put(mag.id, mag);
        saveMagicData();
    }

    public int getUnusedId() {
        if (lastUsedID == 0) {
            for (int id : magics.keySet())
                if (id > lastUsedID)
                    lastUsedID = id;
        }
        lastUsedID++;
        return lastUsedID;
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty()) return true;
        for (Magic mag : magics.values())
            if (mag.name.equals(newName))
                return true;
        return false;
    }

    // === Category management methods ===

    public void addCategory(MagicCategory category) {
        if (category.id < 0) {
            lastUsedCategoryID++;
            category.id = lastUsedCategoryID;
        } else {
            while (containsCategoryName(category.title))
                category.title += "_";
        }
        categories.put(category.id, category);
        saveMagicData();
    }

    public boolean containsCategoryName(String title) {
        title = title.toLowerCase();
        for (MagicCategory cat : categories.values()) {
            if (cat.title.toLowerCase().equals(title))
                return true;
        }
        return false;
    }

    public void removeCategory(int categoryId) {
        if (categories.containsKey(categoryId)) {
            categories.remove(categoryId);
            saveMagicData();
        }
    }

    /**
     * Associates a magic with a category along with its per-category ordering data.
     */
    public void addMagicToCategory(int magicId, int categoryId, int index, int priority) {
        MagicCategory cat = categories.get(categoryId);
        if (cat == null) return;
        MagicAssociation assoc = new MagicAssociation();
        assoc.magicId = magicId;
        assoc.index = index;
        assoc.priority = priority;
        cat.associations.put(magicId, assoc);
        saveMagicData();
    }

    public void removeMagicFromCategory(int magicId, int categoryId) {
        MagicCategory cat = categories.get(categoryId);
        if (cat == null) return;
        cat.associations.remove(magicId);
        saveMagicData();
    }
}
