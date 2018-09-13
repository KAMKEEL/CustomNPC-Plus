package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobBard extends ScriptJobInterface{
	private JobBard job;
	public ScriptJobBard(EntityNPCInterface npc){
		super(npc);
		this.job = (JobBard) npc.jobInterface;
	}
	@Override
	public int getType(){
		return JobType.BARD;
	}
	
	/**
	 * @return The song the bard is playing
	 */
	public String getSong(){
		return job.song;
	}
	
	/**
	 * @param song The song you want the bard to play
	 */
	public void setSong(String song){
		job.song = song;
		npc.script.clientNeedsUpdate = true;
	}
}
