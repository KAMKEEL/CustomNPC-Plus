package noppes.npcs.controllers;

import noppes.npcs.constants.EnumDiagramLayout;
import noppes.npcs.core.NBT;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicAssociation;
import noppes.npcs.controllers.data.MagicCycle;
import kamkeel.npcs.platform.PlatformServiceHolder;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

import java.io.File;
import java.util.HashMap;

public class MagicController {
    public HashMap<Integer, Magic> magics = new HashMap<>();
    public HashMap<Integer, Magic> magicSync = new HashMap<>();

    public HashMap<Integer, MagicCycle> cycles = new HashMap<>();
    public HashMap<Integer, MagicCycle> cyclesSync = new HashMap<>();

    public int lastUsedCycleID = 0;
    private int lastUsedMagicID = 0;

    private static MagicController instance;

    // TODO: mc1710 version implements IMagicHandler and adds:
    // OLD: public Magic getMagic(int magicId) — @Override from IMagicHandler
    // OLD: public MagicCycle getCycle(int cycleID) — @Override from IMagicHandler
    // OLD: public void addMagicToCycle(int magicId, int cycleId, int index, int priority) — @Override
    // OLD: public void removeMagicFromCycle(int magicId, int cycleId) — @Override

    public MagicController() {
        instance = this;
    }

    public static MagicController getInstance() {
        return instance;
    }

    public Magic getMagic(int magicId) {
        return magics.get(magicId);
    }

    public MagicCycle getCycle(int cycleID) {
        return cycles.get(cycleID);
    }

    public void load() {
        magics.clear();
        cycles.clear();

        lastUsedCycleID = 0;
        lastUsedMagicID = 0;

        File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
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
            createDefaults();
        }
    }

    /**
     * Creates default magic entries. Override in mc1710 to set ItemStack items.
     */
    protected void createDefaults() {
        // TODO: mc1710 version sets ItemStack items on each magic:
        // OLD: earth.setItem(new ItemStack(CustomItems.earthElement));
        // OLD: water.setItem(new ItemStack(CustomItems.waterElement));
        // OLD: fire.setItem(new ItemStack(CustomItems.spellFire));
        // OLD: air.setItem(new ItemStack(CustomItems.airElement));
        // OLD: dark.setItem(new ItemStack(CustomItems.spellDark));
        // OLD: holy.setItem(new ItemStack(CustomItems.spellHoly));
        // OLD: nature.setItem(new ItemStack(CustomItems.spellNature));
        // OLD: arcane.setItem(new ItemStack(CustomItems.spellArcane));

        Magic earth = new Magic(getUnusedId(), "Earth", 0x00DD00);
        Magic water = new Magic(getUnusedId(), "Water", 0xF2DD00);
        Magic fire = new Magic(getUnusedId(), "Fire", 0xDD0000);
        Magic air = new Magic(getUnusedId(), "Air", 0xDD0000);
        Magic dark = new Magic(getUnusedId(), "Dark", 0xDD0000);
        Magic holy = new Magic(getUnusedId(), "Holy", 0xDD0000);
        Magic nature = new Magic(getUnusedId(), "Nature", 0xDD0000);
        Magic arcane = new Magic(getUnusedId(), "Arcane", 0xDD0000);

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

        // Add magic associations
        addMagicToCycle(earth.id, defaultCycle.id, 1, 1);
        addMagicToCycle(water.id, defaultCycle.id, 1, 2);
        addMagicToCycle(fire.id, defaultCycle.id, 1, 3);
        addMagicToCycle(air.id, defaultCycle.id, 1, 0);
        addMagicToCycle(dark.id, defaultCycle.id, 0, 3);
        addMagicToCycle(holy.id, defaultCycle.id, 0, 1);
        addMagicToCycle(nature.id, defaultCycle.id, 0, 0);
        addMagicToCycle(arcane.id, defaultCycle.id, 0, 2);

        saveMagicData();
    }

    private void loadMagicFile(File file) throws Exception {
        // OLD: DataInputStream stream = new DataInputStream(
        // OLD:     new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)))
        // OLD: );
        // OLD: loadMagic(stream);
        // OLD: stream.close();
        INbt compound = PlatformServiceHolder.get().readCompressedNBT(file);
        loadMagic(compound);
    }

    public void loadMagic(INbt compound) {
        lastUsedMagicID = compound.getInteger("lastID");
        lastUsedCycleID = compound.getInteger("lastCycleID");

        magics.clear();
        INbtList magicList = compound.getTagList("Magics", 10);
        for (int i = 0; i < magicList.size(); i++) {
            INbt magCompound = magicList.getCompound(i);
            Magic mag = new Magic();
            mag.readNBT(magCompound);
            magics.put(mag.id, mag);
        }

        cycles.clear();
        INbtList cycleList = compound.getTagList("Cycles", 10);
        for (int i = 0; i < cycleList.size(); i++) {
            INbt catCompound = cycleList.getCompound(i);
            MagicCycle cycle = new MagicCycle();
            // OLD: cycle.readNBT(new NBTWrapper(catCompound));
            cycle.readNBT(catCompound);
            cycles.put(cycle.id, cycle);
        }
    }

    public INbt getNBT() {
        INbtList magicList = NBT.list();
        for (Magic mag : magics.values()) {
            INbt magCompound = NBT.compound();
            mag.writeNBT(magCompound);
            magicList.addCompound(magCompound);
        }
        INbtList catList = NBT.list();
        for (MagicCycle cat : cycles.values()) {
            INbt catCompound = NBT.compound();
            // OLD: cat.writeNBT(new NBTWrapper(catCompound));
            cat.writeNBT(catCompound);
            catList.addCompound(catCompound);
        }
        INbt compound = NBT.compound();
        compound.setInteger("lastID", lastUsedMagicID);
        compound.setInteger("lastCycleID", lastUsedCycleID);
        compound.setTagList("Magics", magicList);
        compound.setTagList("Cycles", catList);
        return compound;
    }

    public void saveMagicData() {
        try {
            File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
            File fileNew = new File(saveDir, "magic.dat_new");
            File fileOld = new File(saveDir, "magic.dat_old");
            File fileCurrent = new File(saveDir, "magic.dat");
            // OLD: CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(fileNew));
            PlatformServiceHolder.get().writeCompressedNBT(getNBT(), fileNew);
            if (fileOld.exists())
                fileOld.delete();
            fileCurrent.renameTo(fileOld);

            if (fileCurrent.exists())
                fileCurrent.delete();
            fileNew.renameTo(fileCurrent);
            if (fileNew.exists()) fileNew.delete();
        } catch (Exception e) {
            // OLD: LogWriter.except(e);
            PlatformServiceHolder.get().logError("Error saving magic data", e);
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

        // TODO: mc1710 version also calls:
        // OLD: NBTTagCompound magicCompound = new NBTTagCompound();
        // OLD: mag.writeNBT(magicCompound);
        // OLD: SyncController.syncUpdate(EnumSyncType.MAGIC, -1, magicCompound);

        saveMagicData();
    }

    public void removeMagic(int magicID) {
        if (magics.containsKey(magicID)) {
            magics.remove(magicID);
            // TODO: mc1710 version also calls:
            // OLD: SyncController.syncRemove(EnumSyncType.MAGIC, magicID);
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

    public int getUnusedCycleId() {
        if (lastUsedCycleID == 0) {
            for (int id : cycles.keySet())
                if (id > lastUsedCycleID)
                    lastUsedCycleID = id;
        }
        lastUsedCycleID++;
        return lastUsedCycleID;
    }

    public boolean containsCategoryName(String title) {
        title = title.toLowerCase();
        for (MagicCycle cat : cycles.values()) {
            if (cat.name.toLowerCase().equals(title))
                return true;
        }
        return false;
    }

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

        // TODO: mc1710 version also calls:
        // OLD: NBTTagCompound cycleCompound = new NBTTagCompound();
        // OLD: cycle.writeNBT(new NBTWrapper(cycleCompound));
        // OLD: SyncController.syncUpdate(EnumSyncType.MAGIC_CYCLE, -1, cycleCompound);

        saveMagicData();
    }

    public void removeCycle(int categoryId) {
        if (cycles.containsKey(categoryId)) {
            cycles.remove(categoryId);
            // TODO: mc1710 version also calls:
            // OLD: SyncController.syncRemove(EnumSyncType.MAGIC_CYCLE, categoryId);
            saveMagicData();
        }
    }

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

    public void removeMagicFromCycle(int magicId, int cycleId) {
        MagicCycle cat = cycles.get(cycleId);
        if (cat == null) return;
        cat.associations.remove(magicId);

        saveCycle(cat);
    }
}
