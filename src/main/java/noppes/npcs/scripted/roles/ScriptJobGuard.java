package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobGuard extends ScriptJobInterface{
	private JobGuard job;
	public ScriptJobGuard(EntityNPCInterface npc){
		super(npc);
		this.job = (JobGuard) npc.jobInterface;
	}
	
	@Override
	public int getType(){
		return JobType.GUARD;
	}
	
}
