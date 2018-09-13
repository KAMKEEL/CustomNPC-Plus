package noppes.npcs.ai;

import java.util.Random;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAmbushTarget extends EntityAIBase
{
	private EntityNPCInterface theEntity;
    private EntityLivingBase targetEntity;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private double movementSpeed;
    private double distance;
    private int delay = 0;
    private World theWorld;
    private int tick;
    private boolean attackFromBehind;

    public EntityAIAmbushTarget(EntityNPCInterface par1EntityCreature, double par2, double par3, boolean par4)
    {
        this.theEntity = par1EntityCreature;
        this.movementSpeed = par2;
        this.theWorld = par1EntityCreature.worldObj;
        this.distance = par3 * par3;
        this.attackFromBehind = par4;
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
        else if (this.targetEntity.getDistanceSqToEntity(this.theEntity) < this.distance || !this.theEntity.canSee(targetEntity))
        {
        	return false;
        }
        else if (delay > 0)
        {
        	delay--;
        	return false;
        }
        else
        {
        	Vec3 vec3 = this.findHidingSpot();

            if (vec3 == null)
            {
                return false;
            }
            else
            {
                this.shelterX = vec3.xCoord;
                this.shelterY = vec3.yCoord;
                this.shelterZ = vec3.zCoord;
                return true;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
    	boolean shouldHide = this.targetEntity.getDistanceSqToEntity(this.theEntity) > this.distance;
    	boolean isSeen = this.theEntity.canSee(targetEntity);
    	return (!this.theEntity.getNavigator().noPath() && shouldHide) || (!isSeen && (shouldHide || this.theEntity.ai.directLOS));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.theEntity.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
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
        if (this.targetEntity.getDistanceSqToEntity(this.theEntity) < this.distance)
        {
        	this.delay = 60;
        }
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	this.theEntity.getLookHelper().setLookPositionWithEntity(targetEntity, 30.0F, 30.0F);
    }
    
    private Vec3 findHidingSpot()
    {        
    	Random random = this.theEntity.getRNG();
    	Vec3 idealPos = null;

        for (int i = 1; i <= 8; i++)
        {
        	for (int y = -2; y <= 2; y++)
        	{
        		for (int x = -i; x <= i; x++)
        		{
        			for (int z = -i; z <= i; z++)
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
        		            	if (this.shelterX != j && this.shelterY != k && this.shelterZ != l)
        		            	{
        		            		idealPos = Vec3.createVectorHelper(j, k, l);
        		            	}
        		            }
        	            }
        			}
        		}
        	}
        	
        	if (idealPos != null)
        	{
        		return idealPos;
        	}
        }
        delay = 60;
        return null;
    }
}
