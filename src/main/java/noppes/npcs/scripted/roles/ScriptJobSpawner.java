package noppes.npcs.scripted.roles;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;
import noppes.npcs.scripted.interfaces.jobs.IJobSpawner;

public class ScriptJobSpawner extends ScriptJobInterface implements IJobSpawner {
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
	public IEntityLivingBase spawnEntity(int number){
		EntityLivingBase base = job.spawnEntity(number);
		if(base == null)
			return null;
		
		return (IEntityLivingBase) NpcAPI.Instance().getIEntity(base);
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
