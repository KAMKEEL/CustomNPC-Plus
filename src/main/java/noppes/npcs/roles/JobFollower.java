package noppes.npcs.roles;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public class JobFollower extends JobInterface{
	public EntityNPCInterface following = null;
	private int ticks = 40;
	private int range = 20;
	public String name = "";

	public JobFollower(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("FollowingEntityName", name);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		name = compound.getString("FollowingEntityName");
		
	}
	@Override
	public boolean aiShouldExecute() {
		if(npc.isAttacking())
			return false;
		
		ticks--;
		if(ticks > 0)
			return false;
		
		ticks = 10;
		following = null;
		List<EntityNPCInterface> list = npc.worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, npc.boundingBox.expand(getRange(), getRange(), getRange()));
		for(EntityNPCInterface entity : list){
			if(entity == npc || entity.isKilled())
				continue;
			if(entity.display.name.equalsIgnoreCase(name)){
				following = entity;
				break;
			}
		}
		
		return false;
	}
	
	private int getRange(){
		if(range > CustomNpcs.NpcNavRange)
			return CustomNpcs.NpcNavRange;
		return range;
	}
	
	public boolean isFollowing(){
		return following != null;
	}

	public void reset() {
	}
	public void resetTask() {
		following = null;
	}

	public boolean hasOwner() {
		return !name.isEmpty();
	}
}
