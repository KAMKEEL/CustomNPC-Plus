package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;

public interface ICompatibilty {
	public int getVersion();
	public void setVersion(int version);
	public NBTTagCompound writeToNBT(NBTTagCompound compound);
}
