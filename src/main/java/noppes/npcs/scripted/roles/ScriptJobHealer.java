package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;
import noppes.npcs.scripted.interfaces.jobs.IJobHealer;

public class ScriptJobHealer extends ScriptJobInterface implements IJobHealer {
	private JobHealer job;
	public ScriptJobHealer(EntityNPCInterface npc){
		super(npc);
		this.job = (JobHealer) npc.jobInterface;
	}
	
	public void heal(IEntityLivingBase entity, float amount){
		job.heal(entity.getMCEntity(), amount);
	}
	
	@Override
	public int getType(){
		return JobType.HEALER;
	}
	
}
