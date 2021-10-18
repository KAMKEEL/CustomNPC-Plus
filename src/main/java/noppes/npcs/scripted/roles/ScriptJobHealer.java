package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.scripted.entity.ScriptLivingBase;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobHealer extends ScriptJobInterface{
	private JobHealer job;
	public ScriptJobHealer(EntityNPCInterface npc){
		super(npc);
		this.job = (JobHealer) npc.jobInterface;
	}
	
	public void heal(ScriptLivingBase entity, float amount){
		job.heal(entity.getMCEntity(), amount);
	}
	
	@Override
	public int getType(){
		return JobType.HEALER;
	}
	
}
