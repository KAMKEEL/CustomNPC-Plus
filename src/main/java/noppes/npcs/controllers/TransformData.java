package noppes.npcs.controllers;

import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.DataAI;
import noppes.npcs.DataAdvanced;
import noppes.npcs.DataDisplay;
import noppes.npcs.DataInventory;
import noppes.npcs.DataStats;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class TransformData {
	public NBTTagCompound display;
	public NBTTagCompound ai;
	public NBTTagCompound advanced;
	public NBTTagCompound inv;
	public NBTTagCompound stats;
	public NBTTagCompound role;
	public NBTTagCompound job;
	
	public boolean hasDisplay, hasAi, hasAdvanced, hasInv, hasStats, hasRole, hasJob, isActive;
	
	private EntityNPCInterface npc;
	
	public boolean editingModus = false;
	
	public TransformData(EntityNPCInterface npc){
		this.npc = npc;
	}


    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	compound.setBoolean("TransformIsActive", isActive);
    	writeOptions(compound);
    	if(hasDisplay)
    		compound.setTag("TransformDisplay", display);
    	if(hasAi)
    		compound.setTag("TransformAI", ai);
    	if(hasAdvanced)
    		compound.setTag("TransformAdvanced", advanced);
    	if(hasInv)
    		compound.setTag("TransformInv", inv);
    	if(hasStats)
    		compound.setTag("TransformStats", stats);
    	if(hasRole)
    		compound.setTag("TransformRole", role);
    	if(hasJob)
    		compound.setTag("TransformJob", job);
    	
    	return compound;
    }
    
	public Object writeOptions(NBTTagCompound compound) {
    	compound.setBoolean("TransformHasDisplay", hasDisplay);
    	compound.setBoolean("TransformHasAI", hasAi);
    	compound.setBoolean("TransformHasAdvanced", hasAdvanced);
    	compound.setBoolean("TransformHasInv", hasInv);
    	compound.setBoolean("TransformHasStats", hasStats);
    	compound.setBoolean("TransformHasRole", hasRole);
    	compound.setBoolean("TransformHasJob", hasJob);
    	compound.setBoolean("TransformEditingModus", editingModus);
		return compound;
	}
    

    public void readToNBT(NBTTagCompound compound) {
    	isActive = compound.getBoolean("TransformIsActive");
    	readOptions(compound);
    	display = hasDisplay?compound.getCompoundTag("TransformDisplay"): getDisplay();
    	ai = hasAi?compound.getCompoundTag("TransformAI"): npc.ai.writeToNBT(new NBTTagCompound());
    	advanced = hasAdvanced?compound.getCompoundTag("TransformAdvanced"): getAdvanced();
    	inv = hasInv?compound.getCompoundTag("TransformInv"): npc.inventory.writeEntityToNBT(new NBTTagCompound());
    	stats = hasStats?compound.getCompoundTag("TransformStats"): npc.stats.writeToNBT(new NBTTagCompound());
    	job = hasJob?compound.getCompoundTag("TransformJob"): getJob();
    	role = hasRole?compound.getCompoundTag("TransformRole"): getRole();
    }
    
    public NBTTagCompound getJob() {
    	NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("NpcJob", npc.advanced.job.ordinal());
        if (npc.advanced.job != EnumJobType.None && npc.jobInterface != null) {
            npc.jobInterface.writeToNBT(compound);
        }
    	
    	return compound;
	}
    public NBTTagCompound getRole() {
    	NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("Role", npc.advanced.role.ordinal());        
        if (npc.advanced.role != EnumRoleType.None && npc.roleInterface != null) {
            npc.roleInterface.writeToNBT(compound);
        }
    	
    	return compound;
	}

    public NBTTagCompound getDisplay() {
    	NBTTagCompound compound = npc.display.writeToNBT(new NBTTagCompound());
    	if(npc instanceof EntityCustomNpc){
    		compound.setTag("ModelData", ((EntityCustomNpc)npc).modelData.writeToNBT());
    	}
    	
    	return compound;
    }

    public NBTTagCompound getAdvanced() {
		EnumJobType jopType = npc.advanced.job;
		EnumRoleType roleType = npc.advanced.role;

		npc.advanced.job = EnumJobType.None;
		npc.advanced.role = EnumRoleType.None;

    	NBTTagCompound compound = npc.advanced.writeToNBT(new NBTTagCompound());
    	compound.removeTag("Role");
    	compound.removeTag("NpcJob");

    	npc.advanced.job = jopType;
    	npc.advanced.role = roleType;
    	
    	return compound;
	}


	public void readOptions(NBTTagCompound compound){
		boolean hadDisplay = hasDisplay;
		boolean hadAI = hasAi;
		boolean hadAdvanced = hasAdvanced;
		boolean hadInv = hasInv;
		boolean hadStats = hasStats;
		boolean hadRole = hasRole;
		boolean hadJob = hasJob;
		
    	hasDisplay = compound.getBoolean("TransformHasDisplay");
    	hasAi = compound.getBoolean("TransformHasAI");
    	hasAdvanced = compound.getBoolean("TransformHasAdvanced");
    	hasInv = compound.getBoolean("TransformHasInv");
    	hasStats = compound.getBoolean("TransformHasStats");
    	hasRole = compound.getBoolean("TransformHasRole");
    	hasJob = compound.getBoolean("TransformHasJob");
    	editingModus = compound.getBoolean("TransformEditingModus");

    	if(hasDisplay && !hadDisplay){
    		display = getDisplay();
    	}
    	if(hasAi && !hadAI)
    		ai = npc.ai.writeToNBT(new NBTTagCompound());
    	if(hasStats && !hadStats)
    		stats = npc.stats.writeToNBT(new NBTTagCompound());
    	if(hasInv && !hadInv)
    		inv = npc.inventory.writeEntityToNBT(new NBTTagCompound());
    	if(hasAdvanced && !hadAdvanced)
    		advanced = getAdvanced();
    	if(hasJob && !hadJob)
    		job = getJob();
    	if(hasRole && !hadRole)
    		role = getRole();
    }
    
    public boolean isValid(){
    	return hasAdvanced || hasAi || hasDisplay || hasInv || hasStats || hasJob || hasRole;
    }


	public NBTTagCompound processAdvanced(NBTTagCompound compoundAdv,
			NBTTagCompound compoundRole, NBTTagCompound compoundJob) {
		
		if(hasAdvanced)
			compoundAdv = advanced;
		if(hasRole)
			compoundRole = role;
		if(hasJob)
			compoundJob = job;
		
		Set<String> names = compoundRole.func_150296_c();
		for(String name : names)
			compoundAdv.setTag(name, compoundRole.getTag(name));

		names = compoundJob.func_150296_c();
		for(String name : names)
			compoundAdv.setTag(name, compoundJob.getTag(name));
		
		return compoundAdv;
	}
	
	public void transform(boolean isActive){
		if(this.isActive == isActive)
			return;
    	if(hasDisplay){
			NBTTagCompound compound = getDisplay();
			npc.display.readToNBT(NBTTags.NBTMerge(compound, display));
			if(npc instanceof EntityCustomNpc){
				((EntityCustomNpc)npc).modelData.readFromNBT(NBTTags.NBTMerge(compound.getCompoundTag("ModelData"), display.getCompoundTag("ModelData")));
			}
			display = compound;
    	}
    	if(hasStats){
			NBTTagCompound compound = npc.stats.writeToNBT(new NBTTagCompound());
			npc.stats.readToNBT(NBTTags.NBTMerge(compound, stats));
			stats = compound;
    	}
    	if(hasAdvanced || hasJob || hasRole){
			NBTTagCompound compoundAdv = getAdvanced();
			NBTTagCompound compoundRole = getRole();
			NBTTagCompound compoundJob = getJob();
			
			NBTTagCompound compound = processAdvanced(compoundAdv, compoundRole, compoundJob);
			npc.advanced.readToNBT(compound);
	        if (npc.advanced.role != EnumRoleType.None && npc.roleInterface != null) 
	        	npc.roleInterface.readFromNBT(NBTTags.NBTMerge(compoundRole, compound));
	        if (npc.advanced.job != EnumJobType.None && npc.jobInterface != null)
	        	npc.jobInterface.readFromNBT(NBTTags.NBTMerge(compoundJob, compound));   
	        
			if(hasAdvanced)
				advanced = compoundAdv;
			if(hasRole)
				role = compoundRole;
			if(hasJob)
				job = compoundJob;
    	}
    	if(hasAi){
			NBTTagCompound compound = npc.ai.writeToNBT(new NBTTagCompound());
			npc.ai.readToNBT(NBTTags.NBTMerge(compound, ai));
			ai = compound;
	    	npc.setCurrentAnimation(EnumAnimation.NONE);
    	}
    	if(hasInv){
			NBTTagCompound compound = npc.inventory.writeEntityToNBT(new NBTTagCompound());
			npc.inventory.readEntityFromNBT(NBTTags.NBTMerge(compound, inv));
			inv = compound;
    	}
    	npc.updateHitbox();
    	this.isActive = isActive;
		npc.updateAI= true;
		npc.updateClient = true;
	}
}
