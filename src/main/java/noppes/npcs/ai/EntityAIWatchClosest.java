package noppes.npcs.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWatchClosest extends EntityAIBase
{
    private EntityNPCInterface theWatcher;

    /** The closest entity which is being watched by this one. */
    protected Entity closestEntity;
    private float field_75333_c;
    private int lookTime;
    private float field_75331_e;
    private Class watchedClass;

    public EntityAIWatchClosest(EntityNPCInterface par1EntityLiving, Class par2Class, float par3){
        this.theWatcher = par1EntityLiving;
        this.watchedClass = par2Class;
        this.field_75333_c = par3;
        this.field_75331_e = 0.002F;
        this.setMutexBits(AiMutex.LOOK);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        if (this.theWatcher.getRNG().nextFloat() >= this.field_75331_e || theWatcher.isInteracting()){
            return false;
        }
        else{
            if (this.theWatcher.getAttackTarget() != null)
            {
                this.closestEntity = this.theWatcher.getAttackTarget();
            }

            if (this.watchedClass == EntityPlayer.class)
            {
                this.closestEntity = this.theWatcher.worldObj.getClosestPlayerToEntity(this.theWatcher, (double)this.field_75333_c);
            }
            else
            {
                this.closestEntity = this.theWatcher.worldObj.findNearestEntityWithinAABB(this.watchedClass, this.theWatcher.boundingBox.expand((double)this.field_75333_c, 3.0D, (double)this.field_75333_c), this.theWatcher);
                if (this.closestEntity != null)
                {
                	return this.theWatcher.canSee(this.closestEntity);
                }
            }

            return this.closestEntity != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting()
    {
    	if(theWatcher.isInteracting() || theWatcher.isAttacking() || !this.closestEntity.isEntityAlive() || !theWatcher.isEntityAlive())
    		return false;
        return this.theWatcher.getDistanceSqToEntity(this.closestEntity) > (double)(this.field_75333_c * this.field_75333_c) ? false : this.lookTime > 0;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting(){
        this.lookTime = 60 + this.theWatcher.getRNG().nextInt(60);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask(){
        this.closestEntity = null;
    }

    /**
     * Updates the task
     */
    @Override
    public void updateTask()
    {
        this.theWatcher.getLookHelper().setLookPosition(this.closestEntity.posX, this.closestEntity.posY + (double)this.closestEntity.getEyeHeight(), this.closestEntity.posZ, 10.0F, (float)this.theWatcher.getVerticalFaceSpeed());
        --this.lookTime;
    }
}
