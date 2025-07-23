package noppes.npcs.controllers;

import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.network.enums.EnumSyncType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IMagicHandler;
import noppes.npcs.constants.EnumDiagramLayout;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicAssociation;
import noppes.npcs.controllers.data.MagicCycle;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class MagicController implements IMagicHandler {
    public HashMap<Integer, Magic> magics = new HashMap<>();
    public HashMap<Integer, Magic> magicSync = new HashMap<>();

    public HashMap<Integer, MagicCycle> cycles = new HashMap<>();
    public HashMap<Integer, MagicCycle> cyclesSync = new HashMap<>();

    public int lastUsedCycleID = 0;
    private int lastUsedMagicID = 0;

    private static MagicController instance;

    public MagicController() {
        instance = this;
    }

    public static MagicController getInstance() {
        return instance;
    }

    @Override
    public Magic getMagic(int magicId) {
        return magics.get(magicId);
    }

    @Override
    public MagicCycle getCycle(int cycleID) {
        return cycles.get(cycleID);
    }

    public void load() {
        magics.clear();
        cycles.clear();

        lastUsedCycleID = 0;
        lastUsedMagicID = 0;

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
            } catch (Exception ee) {
            }
        }

        if (magics.isEmpty() && cycles.isEmpty()) {
            // Create default magics
            Magic earth = new Magic(getUnusedId(), "Earth", 0x00DD00);
            earth.setItem(new ItemStack(CustomItems.earthElement));

            Magic water = new Magic(getUnusedId(), "Water", 0xF2DD00);
            water.setItem(new ItemStack(CustomItems.waterElement));

            Magic fire = new Magic(getUnusedId(), "Fire", 0xDD0000);
            fire.setItem(new ItemStack(CustomItems.spellFire));

            Magic air = new Magic(getUnusedId(), "Air", 0xDD0000);
            air.setItem(new ItemStack(CustomItems.airElement));

            Magic dark = new Magic(getUnusedId(), "Dark", 0xDD0000);
            dark.setItem(new ItemStack(CustomItems.spellDark));

            Magic holy = new Magic(getUnusedId(), "Holy", 0xDD0000);
            holy.setItem(new ItemStack(CustomItems.spellHoly));

            Magic nature = new Magic(getUnusedId(), "Nature", 0xDD0000);
            nature.setItem(new ItemStack(CustomItems.spellNature));

            Magic arcane = new Magic(getUnusedId(), "Arcane", 0xDD0000);
            arcane.setItem(new ItemStack(CustomItems.spellArcane));

            // Insiders
            earth.interactions.put(air.id, -0.50f);
            air.interactions.put(earth.id, 0.50f);

            water.interactions.put(earth.id, -0.50f);
            earth.interactions.put(water.id, 0.50f);

            fire.interactions.put(water.id, -0.50f);
            water.interactions.put(fire.id, 0.50f);

            air.interactions.put(fire.id, -0.50f);
            fire.interactions.put(air.id, 0.50f);

            // Outsiders
            dark.interactions.put(nature.id, -0.50f);
            nature.interactions.put(dark.id, 0.50f);

            nature.interactions.put(holy.id, -0.50f);
            holy.interactions.put(nature.id, 0.50f);

            holy.interactions.put(arcane.id, -0.50f);
            arcane.interactions.put(holy.id, 0.50f);

            arcane.interactions.put(dark.id, -0.50f);
            dark.interactions.put(arcane.id, 0.50f);

            // Cross Interactions
            earth.interactions.put(nature.id, -0.25f);
            nature.interactions.put(earth.id, 0.25f);

            water.interactions.put(holy.id, -0.25f);
            holy.interactions.put(water.id, 0.25f);

            fire.interactions.put(arcane.id, -0.25f);
            arcane.interactions.put(fire.id, 0.25f);

            air.interactions.put(dark.id, -0.25f);
            dark.interactions.put(air.id, 0.25f);

            dark.interactions.put(fire.id, -0.25f);
            fire.interactions.put(dark.id, 0.25f);

            nature.interactions.put(air.id, -0.25f);
            air.interactions.put(nature.id, 0.25f);

            holy.interactions.put(earth.id, -0.25f);
            earth.interactions.put(holy.id, 0.25f);

            arcane.interactions.put(water.id, -0.25f);
            water.interactions.put(arcane.id, 0.25f);

            // Add them to the registry
            magics.put(earth.id, earth);
            magics.put(water.id, water);
            magics.put(fire.id, fire);
            magics.put(air.id, air);
            magics.put(dark.id, dark);
            magics.put(holy.id, holy);
            magics.put(nature.id, nature);
            magics.put(arcane.id, arcane);

            MagicCycle defaultCycle = new MagicCycle();
            defaultCycle.id = getUnusedCycleId();
            defaultCycle.name = "Universal";
            defaultCycle.layout = EnumDiagramLayout.CIRCULAR_MANUAL;
            defaultCycle.displayName = "&6Elementa Cycle";
            cycles.put(defaultCycle.id, defaultCycle);

            // Add magic associations using your old index and priority values:
            addMagicToCycle(earth.id, defaultCycle.id, 1, 1); // Earth: index 1, priority 1
            addMagicToCycle(water.id, defaultCycle.id, 1, 2); // Water: index 1, priority 2
            addMagicToCycle(fire.id, defaultCycle.id, 1, 3); // Fire: index 1, priority 3
            addMagicToCycle(air.id, defaultCycle.id, 1, 0); // Air: index 1, priority 0
            addMagicToCycle(dark.id, defaultCycle.id, 0, 3); // Dark: index 0, priority 3
            addMagicToCycle(holy.id, defaultCycle.id, 0, 1); // Holy: index 0, priority 1
            addMagicToCycle(nature.id, defaultCycle.id, 0, 0); // Nature: index 0, priority 0
            addMagicToCycle(arcane.id, defaultCycle.id, 0, 2); // Arcane: index 0, priority 2

            saveMagicData();
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
        lastUsedMagicID = compound.getInteger("lastID");
        lastUsedCycleID = compound.getInteger("lastCycleID");

        magics.clear();
        NBTTagList magicList = compound.getTagList("Magics", 10);
        for (int i = 0; i < magicList.tagCount(); i++) {
            NBTTagCompound magCompound = magicList.getCompoundTagAt(i);
            Magic mag = new Magic();
            mag.readNBT(magCompound);
            magics.put(mag.id, mag);
        }

        cycles.clear();
        NBTTagList cycleList = compound.getTagList("Cycles", 10);
        for (int i = 0; i < cycleList.tagCount(); i++) {
            NBTTagCompound catCompound = cycleList.getCompoundTagAt(i);
            MagicCycle cycle = new MagicCycle();
            cycle.readNBT(catCompound);
            cycles.put(cycle.id, cycle);
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
        for (MagicCycle cat : cycles.values()) {
            NBTTagCompound catCompound = new NBTTagCompound();
            cat.writeNBT(catCompound);
            catList.appendTag(catCompound);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("lastID", lastUsedMagicID);
        compound.setInteger("lastCycleID", lastUsedCycleID);
        compound.setTag("Magics", magicList);
        compound.setTag("Cycles", catList);
        return compound;
    }

    public void saveMagicData() {
        try {
            File saveDir = CustomNpcs.getWorldSaveDirectory();
            File fileNew = new File(saveDir, "magic.dat_new");
            File fileOld = new File(saveDir, "magic.dat_old");
            File fileCurrent = new File(saveDir, "magic.dat");
            CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(fileNew));
            if (fileOld.exists())
                fileOld.delete();
            fileCurrent.renameTo(fileOld);

            if (fileCurrent.exists())
                fileCurrent.delete();
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) fileNew.delete();
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    // Enforce unique names and IDs for Magics
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

        NBTTagCompound magicCompound = new NBTTagCompound();
        mag.writeNBT(magicCompound);
        SyncController.syncUpdate(EnumSyncType.MAGIC, -1, magicCompound);

        saveMagicData();
    }

    public void removeMagic(int magicID) {
        if (magics.containsKey(magicID)) {
            magics.remove(magicID);
            SyncController.syncRemove(EnumSyncType.MAGIC, magicID);
            saveMagicData();
        }
    }

    public int getUnusedId() {
        if (lastUsedMagicID == 0) {
            for (int id : magics.keySet())
                if (id > lastUsedMagicID)
                    lastUsedMagicID = id;
        }
        lastUsedMagicID++;
        return lastUsedMagicID;
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty()) return true;
        for (Magic mag : magics.values())
            if (mag.name.equalsIgnoreCase(newName))
                return true;
        return false;
    }

    // === Cycle Management Methods ===

    // Returns a new unique cycle ID
    public int getUnusedCycleId() {
        if (lastUsedCycleID == 0) {
            for (int id : cycles.keySet())
                if (id > lastUsedCycleID)
                    lastUsedCycleID = id;
        }
        lastUsedCycleID++;
        return lastUsedCycleID;
    }

    // Checks if a cycle title already exists (case-insensitive)
    public boolean containsCategoryName(String title) {
        title = title.toLowerCase();
        for (MagicCycle cat : cycles.values()) {
            if (cat.name.toLowerCase().equals(title))
                return true;
        }
        return false;
    }

    // Saves a cycle ensuring a unique title and ID
    public void saveCycle(MagicCycle cycle) {
        if (cycle.id < 0) {
            cycle.id = getUnusedCycleId();
            while (containsCategoryName(cycle.name))
                cycle.name += "_";
        } else {
            MagicCycle existing = cycles.get(cycle.id);
            if (existing != null && !existing.name.equals(cycle.name))
                while (containsCategoryName(cycle.name))
                    cycle.name += "_";
        }
        cycles.put(cycle.id, cycle);

        NBTTagCompound cycleCompound = new NBTTagCompound();
        cycle.writeNBT(cycleCompound);
        SyncController.syncUpdate(EnumSyncType.MAGIC_CYCLE, -1, cycleCompound);

        saveMagicData();
    }

    public void removeCycle(int categoryId) {
        if (cycles.containsKey(categoryId)) {
            cycles.remove(categoryId);
            SyncController.syncRemove(EnumSyncType.MAGIC_CYCLE, categoryId);
            saveMagicData();
        }
    }

    /**
     * Associates a magic with a category along with its per-category ordering data.
     */
    @Override
    public void addMagicToCycle(int magicId, int cycleId, int index, int priority) {
        MagicCycle cat = cycles.get(cycleId);
        if (cat == null)
            return;

        Magic magic = magics.get(magicId);
        if (magic == null)
            return;

        MagicAssociation assoc = new MagicAssociation();
        assoc.magicId = magicId;
        assoc.index = index;
        assoc.priority = priority;
        cat.associations.put(magicId, assoc);

        saveCycle(cat);
    }

    @Override
    public void removeMagicFromCycle(int magicId, int cycleId) {
        MagicCycle cat = cycles.get(cycleId);
        if (cat == null) return;
        cat.associations.remove(magicId);

        saveCycle(cat);
    }
}
