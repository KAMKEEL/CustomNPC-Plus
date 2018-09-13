package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIRole extends EntityAIBase {

	private EntityNPCInterface npc;
	public EntityAIRole(EntityNPCInterface npc){
		this.npc = npc;
	}
	
	@Override
	public boolean shouldExecute() {
		if(npc.isKilled() || npc.roleInterface == null)
			return false;
		return npc.roleInterface.aiShouldExecute();
	}
	
    public void startExecuting()
    {
    	npc.roleInterface.aiStartExecuting();
    }
    
	@Override
    public boolean continueExecuting()
    {
		if(npc.isKilled() || npc.roleInterface == null)
			return false;
		return npc.roleInterface.aiContinueExecute();
    }
	
    public void updateTask(){
    	if(npc.roleInterface != null)
    		npc.roleInterface.aiUpdateTask();
    }
}
