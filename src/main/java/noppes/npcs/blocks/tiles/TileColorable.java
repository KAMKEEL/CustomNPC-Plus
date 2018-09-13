package noppes.npcs.blocks.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileColorable extends TileEntity {
	
	public int color = 14;
	public int rotation;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        color = compound.getInteger("BannerColor");
        rotation = compound.getInteger("BannerRotation");
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setInteger("BannerColor", color);
    	compound.setInteger("BannerRotation", rotation);
    }
	
    public boolean canUpdate(){
        return false;
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
    	NBTTagCompound compound = pkt.func_148857_g();
    	readFromNBT(compound);
    }
    
    @Override
    public Packet getDescriptionPacket(){
    	NBTTagCompound compound = new NBTTagCompound();
    	writeToNBT(compound);
    	compound.removeTag("Items");
    	S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    	return packet;
    }
    
	@Override
    public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
	
	public int powerProvided(){
		return 0;
	}
}
