package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIRangedAttack extends EntityAIBase
{
    /** The entity the AI instance has been applied to */
    private final EntityNPCInterface entityHost;

    /**
     * The entity (as a RangedAttackMob) the AI instance has been applied to.
     */
    private final IRangedAttackMob rangedAttackEntityHost;
    private EntityLivingBase attackTarget;

    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    private int rangedAttackTime = 0;
    private int field_75318_f = 0;
    private int field_70846_g = 0;
    private int attackTick = 0;
    private boolean hasFired = false;
    private boolean navOverride = false;

    public EntityAIRangedAttack(IRangedAttackMob par1IRangedAttackMob)
    {
        if (!(par1IRangedAttackMob instanceof EntityLivingBase))
        {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        else
        {
            this.rangedAttackEntityHost = par1IRangedAttackMob;
            this.entityHost = (EntityNPCInterface)par1IRangedAttackMob;
            this.rangedAttackTime = this.entityHost.stats.minDelay / 2;
            this.setMutexBits(this.navOverride ? AiMutex.PATHING : AiMutex.LOOK + AiMutex.PASSIVE);
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	EntityLivingBase var1 = this.entityHost.getAttackTarget();

        if (var1 == null || !var1.isEntityAlive()) return false;
        
		if(entityHost.getDistanceToEntity(var1) > entityHost.stats.aggroRange) return false;
		
        if (this.entityHost.inventory.getProjectile() == null)  return false;
        
        double var2 = this.entityHost.getDistanceSq(var1.posX, var1.boundingBox.minY, var1.posZ);
        double var3 = this.entityHost.ai.distanceToMelee * this.entityHost.ai.distanceToMelee;
        
        if (this.entityHost.ai.useRangeMelee >= 1 && var2 <= var3) return false;
        
        else
        {
            this.attackTarget = var1;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return this.shouldExecute() || !this.entityHost.getNavigator().noPath();
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.attackTarget = null;
        this.entityHost.setAttackTarget(null);
        this.entityHost.getNavigator().clearPathEntity();
        this.field_75318_f = 0;
        this.hasFired = false;
        this.rangedAttackTime = this.entityHost.stats.minDelay / 2;
    }
    
    /**
     * Updates the task
     */
    public void updateTask()
    {
    	this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 30.0F);
        double var1 = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.boundingBox.minY, this.attackTarget.posZ);
		float field_82642_h = this.entityHost.stats.rangedRange * this.entityHost.stats.rangedRange;

		if (!navOverride && this.entityHost.ai.directLOS)
		{
			if (this.entityHost.getEntitySenses().canSee(this.attackTarget))
		    {
				++this.field_75318_f;
		    }
			else
			{
				this.field_75318_f = 0;
			}
			int v = this.entityHost.ai.tacticalVariant == EnumNavType.Default ? 20 : 5;
			if (var1 <= (double)field_82642_h && this.field_75318_f >= v)
			{
				this.entityHost.getNavigator().clearPathEntity();
			}
			else
			{
				this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, 1.0D);
			}
        }

        this.rangedAttackTime = Math.max(this.rangedAttackTime - 1, 0);
        
        if (this.rangedAttackTime <= 0)
        {
            if (var1 <= (double)field_82642_h && (this.entityHost.getEntitySenses().canSee(this.attackTarget) || this.entityHost.ai.canFireIndirect == 2))
            {
            	 if (this.field_70846_g++ <= this.entityHost.stats.burstCount)
                 {
                     this.rangedAttackTime = this.entityHost.stats.fireRate;
                 }
                 else
                 {
                	 this.field_70846_g = 0;
                	 this.hasFired = true;
                	 this.rangedAttackTime = (this.entityHost.stats.maxDelay - MathHelper.floor_float(this.entityHost.getRNG().nextFloat() * (this.entityHost.stats.maxDelay - this.entityHost.stats.minDelay)));
                 }
            	 
            	 if (this.field_70846_g > 1)
                 {
            		 boolean indirect = false;
            		 
            		 switch(this.entityHost.ai.canFireIndirect)
            		 {
            		     case 1 : indirect = var1 > (double)field_82642_h / 2; break;
            		     case 2 : indirect = !this.entityHost.getEntitySenses().canSee(this.attackTarget);
            		 }
            		 
            		 this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, indirect ? 1 : 0);
            		 if (this.entityHost.currentAnimation != EnumAnimation.AIMING)
            		 {
            			 this.entityHost.swingItem();
            		 }
                 }
            } 
        }
    }
    
    public boolean hasFired()
    {
    	return this.hasFired;
    }
    
    public void navOverride(boolean nav){
    	this.navOverride = nav;
        this.setMutexBits(this.navOverride ? AiMutex.PATHING : AiMutex.LOOK + AiMutex.PASSIVE);
    }
}