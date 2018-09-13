package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobConversation;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.ScriptPlayer;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobConversation extends ScriptJobInterface{
	private JobConversation job;
	public ScriptJobConversation(EntityNPCInterface npc){
		super(npc);
		this.job = (JobConversation) npc.jobInterface;
	}
		
	@Override
	public int getType(){
		return JobType.SPAWNER;
	}
	
}
