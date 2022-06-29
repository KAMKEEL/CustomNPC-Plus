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
import noppes.npcs.scripted.interfaces.entity.ICustomNpc;
import noppes.npcs.scripted.interfaces.entity.IEntity;
import noppes.npcs.scripted.interfaces.handler.data.INaturalSpawn;

public class SpawnData extends WeightedRandom.Item implements INaturalSpawn {
	public List<String> biomes = new ArrayList<String>();
	public int id = -1;
	public String name = "";
	public NBTTagCompound compound1 = new NBTTagCompound();
	public boolean liquid = false;
	public boolean air = false;
	
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

		liquid = compound.getBoolean("Liquid");
		air = compound.getBoolean("Air");
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("SpawnId", id);
		compound.setString("SpawnName", name);
		compound.setInteger("SpawnWeight", itemWeight);
		
		compound.setTag("SpawnBiomes", NBTTags.nbtStringList(biomes));
		compound.setTag("SpawnCompound1", compound1);

		compound.setBoolean("Liquid", liquid);
		compound.setBoolean("Air", air);
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
		if (!entity.getMCEntity().isEntityAlive()) {
			throw new CustomNPCsException("Cannot save dead entities");
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

	public void spawnsInLiquid(boolean spawns) {
		this.liquid = spawns;
	}

	public boolean spawnsInLiquid() {
		return this.liquid;
	}

	public String[] getBiomes() {
		return biomes.toArray(new String[]{});
	}

	public void setBiomes(String[] biomes) {
		this.biomes = new ArrayList<>(Arrays.asList(biomes));
	}
}
