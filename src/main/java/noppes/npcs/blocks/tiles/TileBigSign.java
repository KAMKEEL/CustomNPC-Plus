package noppes.npcs.blocks.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IChatComponent;
import noppes.npcs.TextBlock;

public class TileBigSign extends TileEntity {
	
	public int rotation;
	public boolean canEdit = true;
	public boolean hasChanged = true;

    private String signText = "";
    public TextBlock block;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        rotation = compound.getInteger("SignRotation");
        setText(compound.getString("SignText"));
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setInteger("SignRotation", rotation);
    	compound.setString("SignText", signText);
    }
	
    public boolean canUpdate(){
        return false;
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
    	NBTTagCompound compound = pkt.func_148857_g();
    	readFromNBT(compound);
    }
    
    public void setText(String text){
    	this.signText = text;
    	hasChanged = true;
    }
    
    public String getText(){
    	return signText;
    }
    
    @Override
    public Packet getDescriptionPacket(){
    	NBTTagCompound compound = new NBTTagCompound();
    	writeToNBT(compound);
    	S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    	return packet;
    }
}

