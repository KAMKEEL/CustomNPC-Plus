package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.scripted.ScriptNpc;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobFollower extends ScriptJobInterface{
	private JobFollower job;
	public ScriptJobFollower(EntityNPCInterface npc){
		super(npc);
		this.job = (JobFollower) npc.jobInterface;
	}
	
	public String getFollowingName(){
		return job.name;
	}
	
	public void setFollowingName(String name){
		job.name = name;
	}
	
	public ScriptNpc getFollowingNpc(){
		if(!isFollowing())
			return null;
		return job.following.script.dummyNpc;
	}
	
	public boolean isFollowing(){
		return job.isFollowing();
	}
	
	@Override
	public int getType(){
		return JobType.FOLLOWER;
	}
	
}
