package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.jobs.IJobGuard;

public class ScriptJobGuard extends ScriptJobInterface implements IJobGuard {
	private JobGuard job;
	public ScriptJobGuard(EntityNPCInterface npc){
		super(npc);
		this.job = (JobGuard) npc.jobInterface;
	}

	public boolean attackCreepers() {
		return job.attackCreepers;
	}
	public void attackCreepers(boolean value) {
		job.attackCreepers = value;
	}

	public boolean attacksAnimals() {
		return job.attacksAnimals;
	}
	public void attacksAnimals(boolean value) {
		job.attacksAnimals = value;
	}

	public boolean attackHostileMobs() {
		return job.attackHostileMobs;
	}
	public void attackHostileMobs(boolean value) {
		job.attackHostileMobs = value;
	}
	
	@Override
	public int getType(){
		return JobType.GUARD;
	}
	
}
