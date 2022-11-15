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
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.data.INaturalSpawn;

public class SpawnData extends WeightedRandom.Item implements INaturalSpawn {
	public List<String> biomes = new ArrayList<String>();
	public int id = -1;
	public String name = "";
	public NBTTagCompound compound1 = new NBTTagCompound();
	public NBTTagCompound compound2 = new NBTTagCompound();
	public NBTTagCompound compound3 = new NBTTagCompound();
	public NBTTagCompound compound4 = new NBTTagCompound();
	public NBTTagCompound compound5 = new NBTTagCompound();

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
		compound2 = compound.getCompoundTag("SpawnCompound2");
		compound3 = compound.getCompoundTag("SpawnCompound3");
		compound4 = compound.getCompoundTag("SpawnCompound4");
		compound5 = compound.getCompoundTag("SpawnCompound5");

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
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("SpawnId", id);
		compound.setString("SpawnName", name);
		compound.setInteger("SpawnWeight", itemWeight);
		
		compound.setTag("SpawnBiomes", NBTTags.nbtStringList(biomes));
		compound.setTag("SpawnCompound1", compound1);
		compound.setTag("SpawnCompound2", compound2);
		compound.setTag("SpawnCompound3", compound3);
		compound.setTag("SpawnCompound4", compound4);
		compound.setTag("SpawnCompound5", compound5);

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

	public void setEntity(IEntity entity, int slot) {
		if (slot < 1)
			slot = 1;
		if (slot > 5)
			slot = 5;

		NBTTagCompound compound = new NBTTagCompound();
		if (entity != null && !entity.getMCEntity().writeToNBTOptional(compound)) {
			throw new CustomNPCsException("Entity could not be written to NBT");
		} else {
			switch (slot) {
				case 1:
					ServerCloneController.Instance.cleanTags(compound1);
					compound1 = compound;
					break;
				case 2:
					ServerCloneController.Instance.cleanTags(compound2);
					compound2 = compound;
					break;
				case 3:
					ServerCloneController.Instance.cleanTags(compound3);
					compound3 = compound;
					break;
				case 4:
					ServerCloneController.Instance.cleanTags(compound4);
					compound4 = compound;
					break;
				case 5:
					ServerCloneController.Instance.cleanTags(compound5);
					compound5 = compound;
					break;
			}
		}
	}

	public IEntity getEntity(IWorld world, int slot) {
		if (slot < 1)
			slot = 1;
		if (slot > 5)
			slot = 5;

		try {
			NBTTagCompound compound = new NBTTagCompound();
			switch (slot) {
				case 1:
					compound = compound1;
					break;
				case 2:
					compound = compound2;
					break;
				case 3:
					compound = compound3;
					break;
				case 4:
					compound = compound4;
					break;
				case 5:
					compound = compound5;
					break;
			}

			Entity entity = EntityList.createEntityFromNBT(compound, world.getMCWorld());
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
