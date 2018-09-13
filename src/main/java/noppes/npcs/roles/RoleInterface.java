package noppes.npcs.roles;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class RoleInterface {
	public EntityNPCInterface npc;
	public HashMap<String,String> dataString = new HashMap<String,String>();
	public RoleInterface(EntityNPCInterface npc){
		this.npc = npc;
	}
	public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
	public abstract void readFromNBT(NBTTagCompound compound);
	public abstract void interact(EntityPlayer player);
	public void killed(){};
	public void delete(){};
	
	public boolean aiShouldExecute() {
		return false;
	}
	
	public boolean aiContinueExecute() {
		return false;
	}
	public void aiStartExecuting() {}
	public void aiUpdateTask() {}
	public boolean defendOwner() {
		return false;
	}
	public void clientUpdate() {
		
	}
}
