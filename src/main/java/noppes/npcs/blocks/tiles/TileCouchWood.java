package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;


public class TileCouchWood extends TileColorable {
	public boolean hasLeft = false;
	public boolean hasRight = false;
	

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        hasLeft = compound.getBoolean("CouchLeft");
        hasRight = compound.getBoolean("CouchRight");
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setBoolean("CouchLeft", hasLeft);
    	compound.setBoolean("CouchRight", hasRight);
    }
}
