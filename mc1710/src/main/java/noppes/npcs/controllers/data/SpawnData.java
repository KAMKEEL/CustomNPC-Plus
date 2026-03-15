package noppes.npcs.controllers.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.NBTTags;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.data.INaturalSpawn;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpawnData extends WeightedRandom.Item implements INaturalSpawn {
    public static final int DESPAWN_FORCE_NATURAL = INaturalSpawn.DESPAWN_FORCE_NATURAL;
    public static final int DESPAWN_PRESERVE_TEMPLATE = INaturalSpawn.DESPAWN_PRESERVE_TEMPLATE;
    public static final int DESPAWN_FORCE_PERSISTENT = INaturalSpawn.DESPAWN_FORCE_PERSISTENT;

    public List<String> biomes = new ArrayList<>();
    public HashSet<Integer> dimensions = new HashSet<>();
    public int id = -1;
    public String name = "";

    public HashMap<Integer, NBTTagCompound> spawnCompounds = new HashMap<>();

    public boolean animalSpawning = true;
    public boolean monsterSpawning = false;
    public boolean liquidSpawning = false;
    public boolean airSpawning = false;

    public int spawnHeightMin;
    public int spawnHeightMax = 100;
    public int maxAlive = 0;
    public int cooldownTicks = 0;
    public int attemptsPerCycle = 1;
    public int playerMinDistance = 24;
    public int despawnMode = DESPAWN_FORCE_NATURAL;

    public SpawnData() {
        super(10);
    }

    public void readNBT(NBTTagCompound compound) {
        id = compound.getInteger("SpawnId");
        name = compound.getString("SpawnName");
        itemWeight = compound.getInteger("SpawnWeight");
        if (itemWeight == 0)
            itemWeight = 1;

        biomes = NBTTags.getStringList(compound.getTagList("SpawnBiomes", 10));
        if (!compound.hasKey("SpawnDimensions")) {
            dimensions.addAll(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
        } else {
            dimensions = NBTTags.getIntegerSet(compound.getTagList("SpawnDimensions", 10));
        }

        this.spawnCompounds.clear();
        Set<?> keys = compound.func_150296_c();
        for (Object key : keys) {
            if (((String) key).startsWith("SpawnCompound")) {
                int i = Integer.parseInt(((String) key).replace("SpawnCompound", ""));
                this.spawnCompounds.put(i, compound.getCompoundTag("SpawnCompound" + i));
            }
        }

        animalSpawning = compound.getBoolean("AnimalSpawning");
        monsterSpawning = compound.getBoolean("MonsterSpawning");
        liquidSpawning = compound.getBoolean("LiquidSpawning");
        airSpawning = compound.getBoolean("CaveSpawning");

        spawnHeightMin = compound.getInteger("HeightMin");
        if (!compound.hasKey("HeightMax")) {
            spawnHeightMax = 100;
        } else {
            spawnHeightMax = compound.getInteger("HeightMax");
        }

        maxAlive = compound.hasKey("MaxAlive") ? Math.max(0, compound.getInteger("MaxAlive")) : 0;
        cooldownTicks = compound.hasKey("CooldownTicks") ? Math.max(0, compound.getInteger("CooldownTicks")) : 0;
        attemptsPerCycle = compound.hasKey("AttemptsPerCycle") ? Math.max(1, compound.getInteger("AttemptsPerCycle")) : 1;
        playerMinDistance = compound.hasKey("PlayerMinDistance") ? Math.max(0, compound.getInteger("PlayerMinDistance")) : 24;
        if (compound.hasKey("DespawnMode")) {
            despawnMode = compound.getInteger("DespawnMode");
        } else {
            despawnMode = DESPAWN_FORCE_NATURAL;
        }
        if (despawnMode < DESPAWN_FORCE_NATURAL || despawnMode > DESPAWN_FORCE_PERSISTENT) {
            despawnMode = DESPAWN_FORCE_NATURAL;
        }
    }

    public NBTTagCompound writeNBT(NBTTagCompound compound) {
        compound.setInteger("SpawnId", id);
        compound.setString("SpawnName", name);
        compound.setInteger("SpawnWeight", itemWeight);

        compound.setTag("SpawnBiomes", NBTTags.nbtStringList(biomes));
        if (!this.dimensions.isEmpty()) {
            compound.setTag("SpawnDimensions", NBTTags.nbtIntegerSet(dimensions));
        }

        Set<Map.Entry<Integer, NBTTagCompound>> entries = this.spawnCompounds.entrySet();
        for (Map.Entry<Integer, NBTTagCompound> entry : entries) {
            compound.setTag("SpawnCompound" + entry.getKey(), entry.getValue() != null ? entry.getValue() : new NBTTagCompound());
        }

        compound.setBoolean("AnimalSpawning", animalSpawning);
        compound.setBoolean("MonsterSpawning", monsterSpawning);
        compound.setBoolean("LiquidSpawning", liquidSpawning);
        compound.setBoolean("CaveSpawning", airSpawning);

        compound.setInteger("HeightMin", spawnHeightMin);
        compound.setInteger("HeightMax", spawnHeightMax);
        compound.setInteger("MaxAlive", maxAlive);
        compound.setInteger("CooldownTicks", cooldownTicks);
        compound.setInteger("AttemptsPerCycle", attemptsPerCycle);
        compound.setInteger("PlayerMinDistance", playerMinDistance);
        compound.setInteger("DespawnMode", despawnMode);
        return compound;
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public String getName() {
        return this.name;
    }

    public void setEntity(IEntity entity, int slot) {
        NBTTagCompound compound = new NBTTagCompound();
        if (entity != null && !entity.getMCEntity().writeToNBTOptional(compound)) {
            throw new CustomNPCsException("Entity could not be written to NBT");
        } else {
            this.spawnCompounds.put(slot, compound);
        }
    }

    public IEntity getEntity(IWorld world, int slot) {
        if (!this.spawnCompounds.containsKey(slot)) {
            return null;
        }
        try {
            Entity entity = EntityList.createEntityFromNBT(this.spawnCompounds.get(slot), world.getMCWorld());
            return NpcAPI.Instance().getIEntity(entity);
        } catch (Exception e) {
            throw new CustomNPCsException("Error creating entity from spawn data:\n" + e.getMessage());
        }
    }

    public Integer[] getSlots() {
        return this.spawnCompounds.keySet().toArray(new Integer[0]);
    }

    public void setWeight(int weight) {
        if (weight < 1)
            weight = 1;
        if (weight > 100)
            weight = 100;

        this.itemWeight = weight;
    }

    public int getWeight() {
        return this.itemWeight;
    }

    public void setMinHeight(int height) {
        this.spawnHeightMin = height;
    }

    public int getMinHeight() {
        return this.spawnHeightMin;
    }

    public void setMaxHeight(int height) {
        this.spawnHeightMax = height;
    }

    public int getMaxHeight() {
        return this.spawnHeightMax;
    }

    public void spawnsLikeAnimal(boolean spawns) {
        this.animalSpawning = spawns;
    }

    public boolean spawnsLikeAnimal() {
        return animalSpawning;
    }

    public void spawnsLikeMonster(boolean spawns) {
        this.monsterSpawning = spawns;
    }

    public boolean spawnsLikeMonster() {
        return monsterSpawning;
    }

    public void spawnsInLiquid(boolean spawns) {
        this.liquidSpawning = spawns;
    }

    public boolean spawnsInLiquid() {
        return this.liquidSpawning;
    }

    public void spawnsInAir(boolean spawns) {
        this.airSpawning = spawns;
    }

    public boolean spawnsInAir() {
        return this.airSpawning;
    }

    public String[] getBiomes() {
        return biomes.toArray(new String[]{});
    }

    public void setBiomes(String[] biomes) {
        this.biomes = new ArrayList<>(Arrays.asList(biomes));
    }

    public void setMaxAlive(int maxAlive) {
        this.maxAlive = Math.max(0, maxAlive);
    }

    public int getMaxAlive() {
        return this.maxAlive;
    }

    public void setCooldownTicks(int ticks) {
        this.cooldownTicks = Math.max(0, ticks);
    }

    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    public void setAttemptsPerCycle(int attempts) {
        this.attemptsPerCycle = Math.max(1, attempts);
    }

    public int getAttemptsPerCycle() {
        return this.attemptsPerCycle;
    }

    public void setPlayerMinDistance(int distance) {
        this.playerMinDistance = Math.max(0, distance);
    }

    public int getPlayerMinDistance() {
        return this.playerMinDistance;
    }

    public void setDespawnMode(int mode) {
        if (mode < DESPAWN_FORCE_NATURAL || mode > DESPAWN_FORCE_PERSISTENT)
            mode = DESPAWN_FORCE_NATURAL;
        this.despawnMode = mode;
    }

    public int getDespawnMode() {
        return this.despawnMode;
    }
}
