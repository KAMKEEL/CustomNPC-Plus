package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.interfaces.entity.IEntity;
import noppes.npcs.scripted.interfaces.handler.data.INaturalSpawn;

public class SpawnData extends WeightedRandom.Item implements INaturalSpawn {
	public List<String> biomes = new ArrayList<String>();
	public int id = -1;
	public String name = "";
	public NBTTagCompound compound1 = new NBTTagCompound();

	public boolean animalSpawning = true;
	public boolean monsterSpawning = false;
	public boolean liquidSpawning = false;
	public boolean airSpawning = false;

	public int spawnHeightMin;
	public int spawnHeightMax;

	public SpawnData() {
		super(10);
	}

	public void readNBT(NBTTagCompound compound) {
		id = compound.getInteger("SpawnId");
		name = compound.getString("SpawnName");
		itemWeight = compound.getInteger("SpawnWeight");
		if(itemWeight == 0)
			itemWeight = 1;

		biomes = NBTTags.getStringList(compound.getTagList("SpawnBiomes", 10));
		compound1 = compound.getCompoundTag("SpawnCompound1");

		animalSpawning = compound.getBoolean("AnimalSpawning");
		monsterSpawning = compound.getBoolean("MonsterSpawning");
		liquidSpawning = compound.getBoolean("LiquidSpawning");
		airSpawning = compound.getBoolean("CaveSpawning");

		spawnHeightMin = compound.getInteger("HeightMin");
		if (compound.hasKey("HeightMax")) {
			spawnHeightMax = 100;
		} else {
			spawnHeightMax = compound.getInteger("HeightMax");
		}
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("SpawnId", id);
		compound.setString("SpawnName", name);
		compound.setInteger("SpawnWeight", itemWeight);
		
		compound.setTag("SpawnBiomes", NBTTags.nbtStringList(biomes));
		compound.setTag("SpawnCompound1", compound1);

		compound.setBoolean("AnimalSpawning", animalSpawning);
		compound.setBoolean("MonsterSpawning", monsterSpawning);
		compound.setBoolean("LiquidSpawning", liquidSpawning);
		compound.setBoolean("CaveSpawning", airSpawning);

		compound.setInteger("HeightMin", spawnHeightMin);
		compound.setInteger("HeightMax", spawnHeightMax);
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

	public void setEntity(IEntity entity) {
		NBTTagCompound compound = new NBTTagCompound();
		if (!entity.getMCEntity().writeToNBTOptional(compound)) {
			throw new CustomNPCsException("Entity could not be written to NBT");
		} else {
			ServerCloneController.Instance.cleanTags(compound1);
			compound1 = compound;
		}
	}

	public IEntity getEntity(IWorld world) {
		try {
			Entity entity = EntityList.createEntityFromNBT(compound1, world.getMCWorld());
			return NpcAPI.Instance().getIEntity(entity);
		} catch (Exception e) {
			throw new CustomNPCsException("Error creating entity from spawn data:\n" + e.getMessage());
		}
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
}
