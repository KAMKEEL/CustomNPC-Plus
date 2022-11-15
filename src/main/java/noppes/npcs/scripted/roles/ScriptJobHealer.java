package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.jobs.IJobHealer;

public class ScriptJobHealer extends ScriptJobInterface implements IJobHealer {
	private JobHealer job;
	public ScriptJobHealer(EntityNPCInterface npc){
		super(npc);
		this.job = (JobHealer) npc.jobInterface;
	}
	
	public void heal(IEntityLivingBase entity, float amount){
		job.heal(entity.getMCEntity(), amount);
	}

	public void setRange(int range) {
		this.job.range = range;
	}
	public int getRange() {
		return this.job.range;
	}

	public void setSpeed(int speed) {
		this.job.speed = speed;
	}
	public int getSpeed() {
		return this.job.speed;
	}
	
	@Override
	public int getType(){
		return JobType.HEALER;
	}
	
}
