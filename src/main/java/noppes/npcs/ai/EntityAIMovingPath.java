package noppes.npcs.ai;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIMovingPath extends EntityAIBase
{
    private EntityNPCInterface npc;
    private int[] pos;

    public EntityAIMovingPath(EntityNPCInterface par1EntityNPCInterface){
        this.npc = par1EntityNPCInterface;
        this.setMutexBits(AiMutex.PASSIVE);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        if (npc.isAttacking() || npc.isInteracting() || npc.getRNG().nextInt(40) != 0 && npc.ai.movingPause || !npc.getNavigator().noPath() || npc.isInteracting())
            return false;
        
        List<int[]> list = npc.ai.getMovingPath();
        if(list.size() < 2)
        	return false;
        
        npc.ai.incrementMovingPath();
    	pos = npc.ai.getCurrentMovingPath();
        
        return true;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
    	if(npc.isAttacking() || npc.isInteracting()){
    		npc.ai.decreaseMovingPath();
    		return false;
    	}
        return !this.npc.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting(){
        this.npc.getNavigator().tryMoveToXYZ(pos[0] + 0.5, pos[1], pos[2] + 0.5, 1.0D);
    }
}
