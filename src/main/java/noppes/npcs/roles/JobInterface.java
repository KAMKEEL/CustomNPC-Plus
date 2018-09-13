package noppes.npcs.roles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class JobInterface {
	public EntityNPCInterface npc;
	
	public boolean overrideMainHand = false;
	public boolean overrideOffHand = false;

	public ItemStack mainhand = null;
	public ItemStack offhand = null;
	
	public JobInterface(EntityNPCInterface npc){
		this.npc = npc;
	}
	public abstract NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound);
	public abstract void readFromNBT(NBTTagCompound nbttagcompound);
	public void killed(){};
	public void delete(){};
	
	public boolean aiShouldExecute() {
		return false;
	}
	
	public boolean aiContinueExecute() {
		return aiShouldExecute();
	}
	public void aiStartExecuting() {}
	public void aiUpdateTask() {}
	public void reset() {}
	public void resetTask() {}
	
	
}
