package noppes.npcs.ai;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIStalkTarget extends EntityAIBase
{
	private EntityNPCInterface theEntity;
    private EntityLivingBase targetEntity;
    private Vec3 movePosition;
    private double distance;
    private boolean overRide;
    private World theWorld;
    private int delay;
    private int tick = 0;

    public EntityAIStalkTarget(EntityNPCInterface par1EntityCreature, double par2)
    {
        this.theEntity = par1EntityCreature;
        this.theWorld = par1EntityCreature.worldObj;
        this.distance = par2 * par2;
        this.overRide = false;
        this.delay = 0;
        this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	this.targetEntity = this.theEntity.getAttackTarget();

        if (this.targetEntity == null)
        {
            return false;
        }
        else if (this.tick > 0)
        {
        	this.tick--;
        	return false;
        }
        else
        {
        	return this.targetEntity.getDistanceSqToEntity(this.theEntity) > this.distance;
        }
    }
    
    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.theEntity.getNavigator().clearPathEntity();
        if (this.theEntity.getAttackTarget() == null && this.targetEntity != null)
        {
        	this.theEntity.setAttackTarget(targetEntity);
        }
        
        if (this.theEntity.getRangedTask() != null)
        {
        	this.theEntity.getRangedTask().navOverride(false);
        }
    }
    
    public void startExecuting()
    {
    	if (this.theEntity.getRangedTask() != null)
        {
        	this.theEntity.getRangedTask().navOverride(true);
        }
    }
    
    /**
     * Updates the task
     */
    public void updateTask()
    {
    	this.theEntity.getLookHelper().setLookPositionWithEntity(targetEntity, 30.0F, 30.0F);
    	
    	if (this.theEntity.getNavigator().noPath() || this.overRide)
    	{
    		if (this.isLookingAway())
    		{
    			this.movePosition = this.stalkTarget();
    			if (this.movePosition != null)
    			{
    				this.theEntity.getNavigator().tryMoveToXYZ(this.movePosition.xCoord, this.movePosition.yCoord, this.movePosition.zCoord, 1.0D);
    				this.overRide = false;
    			}
    			else
    			{
    				this.tick = 100;
    			}
    			
    		}
    		else if (this.theEntity.canSee(targetEntity))
    		{
    			this.movePosition = this.hideFromTarget();
    			if (this.movePosition != null)
    			{
    				this.theEntity.getNavigator().tryMoveToXYZ(this.movePosition.xCoord, this.movePosition.yCoord, this.movePosition.zCoord, 1.33D);
    				this.overRide = false;
    			}
    			else
    			{
    				this.tick = 100;
    			}
    		}
    	}
    	
    	if (this.delay > 0) this.delay--;
    	
    	if (!this.isLookingAway() && this.theEntity.canSee(targetEntity) && this.delay == 0)
    	{
    		this.overRide = true;
    		this.delay = 60;
    	}
    	
    }

    private Vec3 hideFromTarget()
    {
    	Vec3 vec;
    	for (int i = 1; i <= 8; i++)
    	{
    		vec = findSecludedXYZ(i, false);
    		if (vec != null) {
    			return vec;
    		}
    	}
    	return null;
    }
    
    private Vec3 stalkTarget()
    {
    	Vec3 vec;
    	for (int i = 8; i >= 1; i--)
    	{
    		vec = findSecludedXYZ(i, true);
    		if (vec != null) {
    			return vec;
    		}
    	}
    	return null;
    }
    
    private Vec3 findSecludedXYZ(int radius, boolean nearest)
    {        
    	Vec3 idealPos = null;
    	boolean weight;
    	double dist = this.targetEntity.getDistanceSqToEntity(this.theEntity);
    	double u = 0, v = 0, w = 0;
    	
    	if (this.movePosition != null)
    	{
    		u = this.movePosition.xCoord;
    		v = this.movePosition.yCoord;
    		w = this.movePosition.zCoord;
    	}

    	for (int y = -2; y <= 2; y++)
    	{
    		for (int x = -radius; x <= radius; x++)
    		{
    			for (int z = -radius; z <= radius; z++)
    			{
    				double j = MathHelper.floor_double(this.theEntity.posX + x) + 0.5D;
    	            double k = MathHelper.floor_double(this.theEntity.boundingBox.minY + y);
    	            double l = MathHelper.floor_double(this.theEntity.posZ + z) + 0.5D;
    	            
    	            if (this.theWorld.getBlock((int)j, (int)k, (int)l).isOpaqueCube() && !this.theWorld.getBlock((int)j, (int)k + 1, (int)l).isOpaqueCube() && !this.theWorld.getBlock((int)j, (int)k + 2, (int)l).isOpaqueCube())
    	            {
    		            Vec3 vec1 = Vec3.createVectorHelper(this.targetEntity.posX, this.targetEntity.posY + (double)this.targetEntity.getEyeHeight(), this.targetEntity.posZ);
    		            Vec3 vec2 = Vec3.createVectorHelper(j, k + (double)this.theEntity.getEyeHeight(), l);
    		            MovingObjectPosition movingobjectposition = this.theWorld.rayTraceBlocks(vec1, vec2);
    		            if (movingobjectposition != null)
    		            {
    		            	weight = nearest ? this.targetEntity.getDistanceSq(j, k, l) <= dist : true;    	
    		            	if (weight && (j != u || k != v || l != w))
    		            	{
    		            		idealPos = Vec3.createVectorHelper(j, k, l);
    		            		if (nearest) dist = this.targetEntity.getDistanceSq(j, k, l);
    		            	}
    		            }
    	            }
    			}
    		}
    	}
    	return idealPos;
    }
    
    private boolean isLookingAway()
    {
    	Vec3 vec3 = this.targetEntity.getLook(1.0F).normalize();
        Vec3 vec31 = Vec3.createVectorHelper(this.theEntity.posX - this.targetEntity.posX, this.theEntity.boundingBox.minY + (double)(this.theEntity.height / 2.0F) - (this.targetEntity.posY + (double)this.targetEntity.getEyeHeight()), this.theEntity.posZ - this.targetEntity.posZ);
        double d0 = vec31.lengthVector();
        vec31 = vec31.normalize();
        double d1 = vec3.dotProduct(vec31);
        return d1 < 0.6;
    }
}