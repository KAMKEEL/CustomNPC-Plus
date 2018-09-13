package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAISprintToTarget extends EntityAIBase
{
    /** The entity that is leaping. */
    EntityNPCInterface runner;

    /** The entity that the leaper is leaping towards. */
    EntityLivingBase runTarget;

    public EntityAISprintToTarget(EntityNPCInterface par1EntityLiving)
    {
        this.runner = par1EntityLiving;
        this.setMutexBits(AiMutex.PASSIVE + AiMutex.PATHING);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        this.runTarget = this.runner.getAttackTarget();

        if (this.runTarget == null)
        {
            return false;
        }
        else if (this.runner.getNavigator().noPath())
        {
        	return false;
        }
        else
        {
        	switch(this.runner.ai.onAttack)
        	{       	
        		case 0 : return this.runner.getDistanceSqToEntity(this.runTarget) >= 64.0D ? (!this.runner.onGround ? false : true) : false;
        		case 2 : return this.runner.getDistanceSqToEntity(this.runTarget) <= 49.0D ? (!this.runner.onGround ? false : true) : false;
        		default : return false;
        	}
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return this.runner.isEntityAlive() && this.runner.onGround && this.runner.hurtTime <= 0 && (this.runner.motionX != 0.0D && this.runner.motionZ != 0.0D);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
    	this.runner.setSprinting(true);
    }
    
    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.runner.setSprinting(false);
    }
}
