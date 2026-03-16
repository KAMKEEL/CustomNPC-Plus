package noppes.npcs.controllers;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.INaturalSpawnsHandler;
import noppes.npcs.api.handler.data.INaturalSpawn;
import noppes.npcs.controllers.data.SpawnData;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class SpawnController implements INaturalSpawnsHandler {
    public HashMap<String, List<SpawnData>> biomes = new HashMap<String, List<SpawnData>>();
    public ArrayList<SpawnData> data = new ArrayList<SpawnData>();
    public Random random = new Random();

    public static SpawnController Instance;

    private int lastUsedID = 0;

    public SpawnController() {
        Instance = this;
        loadData();
    }

    private void loadData() {

        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }
        try {
            File file = new File(saveDir, "spawns.dat");
            if (file.exists()) {
                loadDataFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "spawns.dat_old");
                if (file.exists()) {
                    loadDataFile(file);
                }

            } catch (Exception ee) {
            }
        }
    }

    private void loadDataFile(File file) throws IOException {
        DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        loadData(var1);
        var1.close();
    }

    public void loadData(DataInputStream stream) throws IOException {
        ArrayList<SpawnData> data = new ArrayList<SpawnData>();
        INbt nbttagcompound1 = CompressedStreamTools.read(stream);
        lastUsedID = nbttagcompound1.getInteger("lastID");
        INbtList nbtlist = nbttagcompound1.getTagList("NPCSpawnData", 10);

        if (nbtlist != null) {
            for (int i = 0; i < nbtlist.tagCount(); i++) {
                INbt INbt = nbtlist.getCompoundTagAt(i);
                SpawnData spawn = new SpawnData();
                spawn.readNBT(INbt);
                data.add(spawn);
            }
        }
        this.data = data;
        fillBiomeData();
    }

    public INbt getNBT() {
        INbtList list = new INbtList();
        for (SpawnData spawn : data) {
            INbt nbtfactions = new INbt();
            spawn.writeNBT(nbtfactions);
            list.appendTag(nbtfactions);
        }
        INbt INbt = new INbt();
        INbt.setInteger("lastID", lastUsedID);
        INbt.setTag("NPCSpawnData", list);
        return INbt;
    }

    public void saveData() {
        try {
            File saveDir = CustomNpcs.getWorldSaveDirectory();
            File file = new File(saveDir, "spawns.dat_new");
            File file1 = new File(saveDir, "spawns.dat_old");
            File file2 = new File(saveDir, "spawns.dat");
            NBTIO.writeCompressed(getNBT(), new FileOutputStream(file));
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
            LogWriter.except(e);
        }
    }

    public SpawnData getSpawnData(int id) {
        for (SpawnData spawn : data)
            if (spawn.id == id)
                return spawn;
        return null;
    }

    public void saveSpawnData(SpawnData spawn) {
        if (spawn.id < 0)
            spawn.id = getUnusedId();
        SpawnData original = getSpawnData(spawn.id);
        if (original == null)
            data.add(spawn);
        else {
            original.readNBT(spawn.writeNBT(new INbt()));
        }
        fillBiomeData();
        saveData();
    }

    private void fillBiomeData() {
        HashMap<String, List<SpawnData>> biomes = new HashMap<String, List<SpawnData>>();
        for (SpawnData spawn : data) {
            for (String s : spawn.biomes) {
                List<SpawnData> list = biomes.get(s);
                if (list == null)
                    biomes.put(s, (list = new ArrayList<SpawnData>()));
                list.add(spawn);
            }
        }
        this.biomes = biomes;
    }

    public int getUnusedId() {
        lastUsedID++;
        return lastUsedID;
    }

    public void removeSpawnData(int id) {
        ArrayList<SpawnData> data = new ArrayList<SpawnData>();

        for (SpawnData spawn : this.data) {
            if (spawn.id == id)
                continue;
            data.add(spawn);
        }
        this.data = data;

        fillBiomeData();
        saveData();
    }

    public List<SpawnData> getSpawnList(String biome) {
        return biomes.get(biome);
    }

    public SpawnData getRandomSpawnData(String biome, int dimensionId) {
        List<SpawnData> biomeList = getSpawnList(biome);
        if (biomeList == null)
            return null;

        ArrayList<SpawnData> list = new ArrayList<>();
        for (SpawnData data : biomeList) {
            if (data.dimensions.contains(dimensionId)) {
                list.add(data);
            }
        }
        if (list.isEmpty())
            return null;

        return (SpawnData) WeightedRandom.getRandomItem(this.random, list);
    }

    public Map<String, Integer> getScroll() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (SpawnData spawn : data) {
            map.put(spawn.name, spawn.id);
        }
        return map;
    }

    public void save() {
        for (SpawnData spawn : this.data) {
            if (spawn.id < 0) {
                spawn.id = getUnusedId();
            }
            SpawnData original = getSpawnData(spawn.id);
            if (original == null) {
                this.data.add(spawn);
            } else {
                original.readNBT(spawn.writeNBT(new INbt()));
            }
        }

        this.fillBiomeData();
        this.saveData();
    }

    public INaturalSpawn[] getSpawns() {
        return data.toArray(new INaturalSpawn[]{});
    }

    public INaturalSpawn[] getSpawns(String biome) {
        List<SpawnData> biomeSpawns = getSpawnList(biome);
        if (biomeSpawns == null)
            return new INaturalSpawn[]{};
        return biomeSpawns.toArray(new INaturalSpawn[]{});
    }

    public void addSpawn(INaturalSpawn spawn) {
        ((SpawnData) spawn).id = -1;
        this.saveSpawnData((SpawnData) spawn);
    }

    public void removeSpawn(INaturalSpawn spawn) {
        this.removeSpawnData(((SpawnData) spawn).id);
    }

    public INaturalSpawn createSpawn() {
        return new SpawnData();
    }
}
