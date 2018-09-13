package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;


public class TileCouchWool extends TileColorable {
	public boolean hasLeft = false;
	public boolean hasRight = false;
	public boolean hasCornerLeft = false;
	public boolean hasCornerRight = false;
	

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        hasLeft = compound.getBoolean("CouchLeft");
        hasRight = compound.getBoolean("CouchRight");
        hasCornerLeft = compound.getBoolean("CouchCornerLeft");
        hasCornerRight = compound.getBoolean("CouchCornerRight");
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setBoolean("CouchLeft", hasLeft);
    	compound.setBoolean("CouchRight", hasRight);
    	compound.setBoolean("CouchCornerLeft", hasCornerLeft);
    	compound.setBoolean("CouchCornerRight", hasCornerRight);
    }
}
