package noppes.npcs.controllers;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

public class TransportLocation {
	public int id = -1;
	public String name = "default name";
	public double posX;
	public double posY;
	public double posZ;
	
	public int type = 0;
	public int dimension = 0;
	
	public TransportCategory category;
	
	public void readNBT(NBTTagCompound compound) {
		if(compound == null)
			return;
		id = compound.getInteger("Id");
		posX = compound.getDouble("PosX");
		posY = compound.getDouble("PosY");
		posZ = compound.getDouble("PosZ");
		type = compound.getInteger("Type");
		dimension = compound.getInteger("Dimension");
		name = compound.getString("Name");
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("Id", id);
		compound.setDouble("PosX", posX);
		compound.setDouble("PosY", posY);
		compound.setDouble("PosZ", posZ);
		compound.setInteger("Type", type);
		compound.setInteger("Dimension", dimension);
		compound.setString("Name", name);
		return compound;
	}

	public boolean isDefault() {
		return type == 1;
	}
}
