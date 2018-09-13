package noppes.npcs.blocks.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileBanner extends TileColorable {
	
	public ItemStack icon;
	public long time = 0;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        icon = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("BannerIcon"));
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	if(icon != null)
    		compound.setTag("BannerIcon", icon.writeToNBT(new NBTTagCompound()));
    }

	@Override
    public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }
	
	public boolean canEdit(){
		return System.currentTimeMillis() - time  < 10000;
	}
}
