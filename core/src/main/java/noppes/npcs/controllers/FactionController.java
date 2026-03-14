package noppes.npcs.controllers;

import noppes.npcs.core.NBT;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.platform.PlatformServiceHolder;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FactionController {
    public HashMap<Integer, Faction> factionsSync = new HashMap<Integer, Faction>();
    public HashMap<Integer, Faction> factions;

    private static FactionController instance = new FactionController();

    private int lastUsedID = 0;

    // TODO: mc1710 version implements IFactionHandler and adds:
    // OLD: public List<IFaction> list()
    // OLD: public IFaction delete(int id) — also calls SyncController.syncRemove(EnumSyncType.FACTION, id)
    // OLD: public IFaction create(String name, int defaultPoints)

    public FactionController() {
        instance = this;
        factions = new HashMap<Integer, Faction>();
    }

    public static FactionController getInstance() {
        return instance;
    }

    public Faction getFaction(int faction) {
        return factions.get(faction);
    }

    public void load() {
        factions = new HashMap<Integer, Faction>();
        lastUsedID = 0;
        File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }
        try {
            File file = new File(saveDir, "factions.dat");
            if (file.exists()) {
                loadFactionsFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "factions.dat_old");
                if (file.exists()) {
                    loadFactionsFile(file);
                }

            } catch (Exception ee) {
            }
        }

        if (factions.isEmpty()) {
            factions.put(0, new Faction(0, "Friendly", 0x00DD00, 2000));
            factions.put(1, new Faction(1, "Neutral", 0xF2DD00, 1000));
            factions.put(2, new Faction(2, "Aggressive", 0xDD0000, 0));
        }
    }

    private void loadFactionsFile(File file) throws Exception {
        // OLD: DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        // OLD: loadFactions(var1);
        // OLD: var1.close();
        INBTCompound nbttagcompound1 = PlatformServiceHolder.get().readCompressedNBT(file);
        loadFactions(nbttagcompound1);
    }

    public void loadFactions(INBTCompound nbttagcompound1) {
        HashMap<Integer, Faction> factions = new HashMap<Integer, Faction>();
        lastUsedID = nbttagcompound1.getInteger("lastID");
        INBTList list = nbttagcompound1.getList("NPCFactions", 10);

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                INBTCompound nbttagcompound = list.getCompound(i);
                Faction faction = new Faction();
                faction.readNBT(nbttagcompound);
                factions.put(faction.id, faction);
            }
        }
        this.factions = factions;
    }

    public INBTCompound getNBT() {
        INBTList list = NBT.list();
        for (int slot : factions.keySet()) {
            Faction faction = factions.get(slot);
            INBTCompound nbtfactions = NBT.compound();
            faction.writeNBT(nbtfactions);
            list.addCompound(nbtfactions);
        }
        INBTCompound nbttagcompound = NBT.compound();
        nbttagcompound.setInteger("lastID", lastUsedID);
        nbttagcompound.setList("NPCFactions", list);
        return nbttagcompound;
    }

    public void saveFactions() {
        try {
            File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
            File file = new File(saveDir, "factions.dat_new");
            File file1 = new File(saveDir, "factions.dat_old");
            File file2 = new File(saveDir, "factions.dat");
            // OLD: CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(file));
            PlatformServiceHolder.get().writeCompressedNBT(getNBT(), file);
            if (file1.exists()) {
                file1.delete();
            }
            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            // OLD: LogWriter.except(e);
            PlatformServiceHolder.get().logError("Error saving faction data", e);
        }
    }

    public Faction get(int faction) {
        return factions.get(faction);
    }

    public void saveFaction(Faction faction) {

        if (faction.id < 0) {
            faction.id = getUnusedId();
            while (hasName(faction.name))
                faction.name += "_";
        } else {
            Faction existing = factions.get(faction.id);
            if (existing != null && !existing.name.equals(faction.name))
                while (hasName(faction.name))
                    faction.name += "_";
        }
        factions.remove(faction.id);
        factions.put(faction.id, faction);

        // TODO: mc1710 version also calls:
        // OLD: NBTTagCompound facCompound = new NBTTagCompound();
        // OLD: faction.writeNBT(facCompound);
        // OLD: SyncController.syncUpdate(EnumSyncType.FACTION, -1, facCompound);
        saveFactions();
    }

    public Faction create(Faction faction) {
        this.saveFaction(faction);
        return faction;
    }

    public Faction create(String name, int color) {
        Faction faction = new Faction();
        faction.name = name;
        faction.color = color;
        this.saveFaction(faction);
        return faction;
    }

    public int getUnusedId() {
        if (lastUsedID == 0) {
            for (int catid : factions.keySet())
                if (catid > lastUsedID)
                    lastUsedID = catid;
        }
        lastUsedID++;
        return lastUsedID;
    }

    public void delete(int id) {
        if (id >= 0 && this.factions.size() > 1) {
            Faction faction = this.factions.remove(id);
            if (faction != null) {
                this.saveFactions();
                faction.id = -1;
                // TODO: mc1710 version also calls:
                // OLD: SyncController.syncRemove(EnumSyncType.FACTION, id);
            }
        }
    }

    public int getFirstFactionId() {
        return factions.keySet().iterator().next();
    }

    public Faction getFirstFaction() {
        return factions.values().iterator().next();
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty())
            return true;
        for (Faction faction : factions.values())
            if (faction.name.equals(newName))
                return true;
        return false;
    }

    public Faction getFactionFromName(String factioname) {
        for (Map.Entry<Integer, Faction> entryfaction : FactionController.getInstance().factions.entrySet()) {
            if (entryfaction.getValue().name.equalsIgnoreCase(factioname)) {
                return entryfaction.getValue();
            }
        }
        return null;
    }

    public String[] getNames() {
        String[] names = new String[factions.size()];
        int i = 0;
        for (Faction faction : factions.values()) {
            names[i] = faction.name.toLowerCase();
            i++;
        }
        return names;
    }
}
