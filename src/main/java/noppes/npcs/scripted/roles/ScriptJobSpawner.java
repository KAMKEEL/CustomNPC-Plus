package noppes.npcs.scripted.roles;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.ScriptLivingBase;
import noppes.npcs.scripted.ScriptPlayer;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobSpawner extends ScriptJobInterface{
	private JobSpawner job;
	public ScriptJobSpawner(EntityNPCInterface npc){
		super(npc);
		this.job = (JobSpawner) npc.jobInterface;
	}
		
	@Override
	public int getType(){
		return JobType.SPAWNER;
	}
	
	/**
	 * Npc needs to be attacking something or be set to Despawn Spawns On Target Lost: No, otherwise it will despawn right away
	 * @param number The entity going to be spawned (1-6)
	 * @return Returns spawned entity
	 */
	public ScriptLivingBase spawnEntity(int number){
		EntityLivingBase base = job.spawnEntity(number);
		if(base == null)
			return null;
		
		return (ScriptLivingBase) ScriptController.Instance.getScriptForEntity(base);
	}
	
	/**
	 * Removes all spawned entities
	 */
	public void removeAllSpawned(){
		for(EntityLivingBase entity : job.spawned){
			entity.isDead = true;
		}
		job.spawned = new ArrayList<EntityLivingBase>();
	}
}
