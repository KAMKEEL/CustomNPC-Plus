package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIJob extends EntityAIBase {

	private EntityNPCInterface npc;
	public EntityAIJob(EntityNPCInterface npc){
		this.npc = npc;
	}
	
	@Override
	public boolean shouldExecute() {
		if(npc.isKilled() || npc.jobInterface == null)
			return false;
		return npc.jobInterface.aiShouldExecute();
	}

	@Override
    public void startExecuting(){
    	npc.jobInterface.aiStartExecuting();
    }
    
	@Override
    public boolean continueExecuting()
    {
		if(npc.isKilled() || npc.jobInterface == null)
			return false;
		return npc.jobInterface.aiContinueExecute();
    }

	@Override
    public void updateTask(){
    	if(npc.jobInterface != null)
    		npc.jobInterface.aiUpdateTask();
    }

	@Override
    public void resetTask() {
    	if(npc.jobInterface != null)
    		npc.jobInterface.resetTask();
    }
}
