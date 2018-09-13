package noppes.npcs.blocks.tiles;

import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.controllers.Availability;

public class TileBorder extends TileEntity implements IEntitySelector{
	public Availability availability = new Availability();
	public AxisAlignedBB boundingbox;
	public int rotation = 0;
	public int height = 10;
	public String message = "availability.areaNotAvailble";

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        readExtraNBT(compound);
    }
    
    public void readExtraNBT(NBTTagCompound compound){
        availability.readFromNBT(compound.getCompoundTag("BorderAvailability"));
        rotation = compound.getInteger("BorderRotation");
        height = compound.getInteger("BorderHeight");
        message = compound.getString("BorderMessage");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	writeExtraNBT(compound);
    }
    
    public void writeExtraNBT(NBTTagCompound compound){
    	compound.setTag("BorderAvailability", availability.writeToNBT(new NBTTagCompound()));
    	compound.setInteger("BorderRotation", rotation);
    	compound.setInteger("BorderHeight", height);
    	compound.setString("BorderMessage", message);
    }
    
    @Override
    public void updateEntity() {
    	if(worldObj.isRemote)
    		return;
    	AxisAlignedBB box = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + height + 1, zCoord + 1);
    	List<Entity> list = worldObj.selectEntitiesWithinAABB(Entity.class, box, this);
    	for(Entity entity : list){
    		if(entity instanceof EntityEnderPearl){
    			EntityEnderPearl pearl = (EntityEnderPearl) entity;
    			if(pearl.getThrower() instanceof EntityPlayer && !availability.isAvailable((EntityPlayer)pearl.getThrower()))
    				entity.isDead = true;
    			continue;
    		}
    		EntityPlayer player = (EntityPlayer) entity;
    		if(availability.isAvailable(player))
    			continue;
    		int posX = this.xCoord;
    		int posZ = this.zCoord;
    		int posY = this.yCoord;
    		if(rotation == 0){
    			posZ--;
    		}
    		else if(rotation == 2){
    			posZ++;
    		}
    		else if(rotation == 1){
    			posX++;
    		}
    		else if(rotation == 3){
    			posX--;
    		}
    		while(!worldObj.isAirBlock(posX, posY, posZ)){
    			posY++;
    		}
    		player.setPositionAndUpdate(posX + 0.5, posY, posZ + 0.5);
    		if(!message.isEmpty())
    			player.addChatComponentMessage(new ChatComponentTranslation(message));
    	}
    }
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
    	NBTTagCompound compound = pkt.func_148857_g();
    	rotation = compound.getInteger("Rotation");
    }
    
    @Override
    public Packet getDescriptionPacket(){
    	NBTTagCompound compound = new NBTTagCompound();
    	compound.setInteger("Rotation", rotation);
    	S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    	return packet;
    	
    }
	
    @Override
    public boolean canUpdate(){
        return true;
    }

	@Override
	public boolean isEntityApplicable(Entity var1) {
		return var1 instanceof EntityPlayerMP || var1 instanceof EntityEnderPearl;
	}
}
