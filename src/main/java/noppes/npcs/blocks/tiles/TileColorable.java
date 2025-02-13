package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileColorable extends TileVariant {

    public int color = 0xFFFFFF;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        color = compound.getInteger("BrushColor");
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setInteger("BrushColor", color);
    }
}
