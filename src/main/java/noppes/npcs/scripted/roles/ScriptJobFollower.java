package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.jobs.IJobFollower;

public class ScriptJobFollower extends ScriptJobInterface implements IJobFollower {
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
	
	public ICustomNpc getFollowingNpc() {
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
