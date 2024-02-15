package noppes.npcs.ai.target;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIClearTarget extends EntityAITarget
{
	private EntityNPCInterface npc;
	private EntityLivingBase target;
    public EntityAIClearTarget(EntityNPCInterface npc){
    	super(npc, false);
    	this.npc = npc;
    }

    @Override
    public boolean shouldExecute(){
        target = npc.getAttackTarget();
        if (target == null)
            return false;

        if(npc.getOwner() != null && !npc.isInRange(npc.getOwner(), npc.stats.aggroRange * 2)){
            return true;
        }

        return npc.combatHandler.checkTarget();
    }

    @Override
    public void startExecuting(){
        this.taskOwner.setAttackTarget(null);
        if(target == taskOwner.getAITarget())
        	this.taskOwner.setRevengeTarget(null);
        super.startExecuting();
    }
}
