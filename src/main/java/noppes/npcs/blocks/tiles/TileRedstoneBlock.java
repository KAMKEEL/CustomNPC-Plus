package noppes.npcs.blocks.tiles;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.controllers.Availability;

public class TileRedstoneBlock extends TileEntity {
	public int onRange = 6;
	public int offRange = 10;

	public int onRangeX = 6;
	public int onRangeY = 6;
	public int onRangeZ = 6;

	public int offRangeX = 10;
	public int offRangeY = 10;
	public int offRangeZ = 10;
	
	public boolean isDetailed = false;

	public Availability availability = new Availability();
	
	public boolean isActivated = false;
	
	private int ticks = 10;
	
	@Override
	public void updateEntity(){
		if(this.worldObj.isRemote)
			return;
		ticks--;
		if(ticks > 0)
			return;
		ticks = 20;
		Block block = worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord);
		if(block == null || block instanceof BlockNpcRedstone == false){
			return;
		}

		if(CustomNpcs.FreezeNPCs){
			if(isActivated)
				setActive(block,false);
			return;
		}
		if(!isActivated){
			int x = isDetailed?onRangeX:onRange;
			int y = isDetailed?onRangeY:onRange;
			int z = isDetailed?onRangeZ:onRange;
			List<EntityPlayer> list = getPlayerList(x,y,z);
			if(list.isEmpty())
				return;
			for(EntityPlayer player : list){
				if(availability.isAvailable(player)){
					setActive(block,true);
					return;
				}
			}
		}
		else{
			int x = isDetailed?offRangeX:offRange;
			int y = isDetailed?offRangeY:offRange;
			int z = isDetailed?offRangeZ:offRange;
			List<EntityPlayer> list = getPlayerList(x,y,z);
			for(EntityPlayer player : list){
				if(availability.isAvailable(player))
					return;
			}
			setActive(block,false);
		}
	
	}
	private void setActive(Block block, boolean bo){
		isActivated = bo;
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, isActivated?1:0,2);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		block.onBlockAdded(worldObj, xCoord, yCoord, zCoord);
	}
	private List<EntityPlayer> getPlayerList(int x, int y, int z){
		return worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1).expand(x, y, z));	
	}
	
	@Override
    public void readFromNBT(NBTTagCompound compound){
    	super.readFromNBT(compound);
    	onRange = compound.getInteger("BlockOnRange");
    	offRange = compound.getInteger("BlockOffRange");
    	
    	isDetailed = compound.getBoolean("BlockIsDetailed");
    	if(compound.hasKey("BlockOnRangeX")){
    		isDetailed = true;
    		onRangeX = compound.getInteger("BlockOnRangeX");
    		onRangeY = compound.getInteger("BlockOnRangeY");
    		onRangeZ = compound.getInteger("BlockOnRangeZ");

    		offRangeX = compound.getInteger("BlockOffRangeX");
    		offRangeY = compound.getInteger("BlockOffRangeY");
    		offRangeZ = compound.getInteger("BlockOffRangeZ");
    	}
    	
    	isActivated = compound.getBoolean("BlockActivated");
    	
    	availability.readFromNBT(compound);
    	
    	if(worldObj != null)
    		setActive(getBlockType(), isActivated);
    }

	@Override
    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setInteger("BlockOnRange", onRange);
    	compound.setInteger("BlockOffRange", offRange);
    	compound.setBoolean("BlockActivated", isActivated);
    	compound.setBoolean("BlockIsDetailed", isDetailed);

    	if(isDetailed){
	    	compound.setInteger("BlockOnRangeX", onRangeX);
	    	compound.setInteger("BlockOnRangeY", onRangeY);
	    	compound.setInteger("BlockOnRangeZ", onRangeZ);

	    	compound.setInteger("BlockOffRangeX", offRangeX);
	    	compound.setInteger("BlockOffRangeY", offRangeY);
	    	compound.setInteger("BlockOffRangeZ", offRangeZ);
    	}
    	
    	
    	availability.writeToNBT(compound);
    }
	
    public boolean canUpdate(){
        return true;
    }
}
