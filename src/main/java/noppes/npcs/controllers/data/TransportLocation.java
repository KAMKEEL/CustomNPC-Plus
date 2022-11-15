package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.ITransportLocation;

public class TransportLocation implements ITransportLocation {
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

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public int getDimension() {
		return dimension;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setPosition(int x, int y, int z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	public void setPosition(IPos pos) {
		this.posX = pos.getX();
		this.posY = pos.getY();
		this.posZ = pos.getZ();
	}

	public IPos getPos() {
		return NpcAPI.Instance().getIPos(this.posX, this.posY, this.posZ);
	}

	public double getX() {
		return posX;
	}

	public double getY() {
		return posY;
	}

	public double getZ() {
		return posZ;
	}

	public void save() {
		TransportController.getInstance().saveLocation(this.category.id, this);
	}
}
