package noppes.npcs.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.AiMutex;

public class EntityAITwistTarget extends EntityAIBase
{
    private EntityCreature theEntity;
    private EntityLivingBase targetEntity;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
    private int entityPosX;
    private int entityPosY;
    private int entityPosZ;
    private double speed;
    private float tacticalRange;
    private boolean strafeLeft; // Flag to alternate between left and right strafes
    private int strafeTimer; // Timer to control the strafing frequency

    public EntityAITwistTarget(EntityCreature par1EntityCreature, double par2, float par4)
    {
        this.theEntity = par1EntityCreature;
        this.speed = par2;
        this.tacticalRange = par4;
        this.strafeLeft = true; // Start with strafing to the left
        this.strafeTimer = 0;
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
        else if (this.targetEntity.getDistanceSqToEntity(this.theEntity) < (double)(this.tacticalRange * this.tacticalRange))
        {
            return false;
        }
        else
        {
            PathEntity pathentity = this.theEntity.getNavigator().getPathToEntityLiving(targetEntity);

            if (pathentity != null)
            {
                if (pathentity.getCurrentPathLength() >= this.tacticalRange)
                {
                    PathPoint pathpoint = pathentity.getPathPointFromIndex(MathHelper.floor_double(this.tacticalRange / 2.0D));
                    this.entityPosX = pathpoint.xCoord;
                    this.entityPosY = pathpoint.yCoord;
                    this.entityPosZ = pathpoint.zCoord;

                    // Enhanced movement logic with delay
                    Vec3 enhancedMovement = calculateEnhancedMovement(this.theEntity, this.targetEntity, this.tacticalRange);

                    if (this.targetEntity.getDistanceSq(enhancedMovement.xCoord, enhancedMovement.yCoord, enhancedMovement.zCoord) < this.targetEntity.getDistanceSq(entityPosX, entityPosY, entityPosZ)) {
                        this.movePosX = enhancedMovement.xCoord;
                        this.movePosY = enhancedMovement.yCoord;
                        this.movePosZ = enhancedMovement.zCoord;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.theEntity.getNavigator().noPath() && this.targetEntity.isEntityAlive() && this.targetEntity.getDistanceSqToEntity(this.theEntity) > (double)(this.tacticalRange * this.tacticalRange);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.theEntity.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.theEntity.getLookHelper().setLookPositionWithEntity(targetEntity, 30.0F, 30.0F);
    }

    /**
     * Calculate enhanced movement logic with delay
     */
    private Vec3 calculateEnhancedMovement(EntityCreature entity, EntityLivingBase target, float tacticalRange)
    {
        // Increment the strafe timer
        this.strafeTimer++;

        // Check if it's time to change strafe direction
        if (this.strafeTimer >= 20) // Adjust the number to control the delay between strafes
        {
            // Reset the timer and toggle strafe direction
            this.strafeTimer = 0;
            this.strafeLeft = !this.strafeLeft;
        }

        double deltaX = target.posX - entity.posX;
        double deltaZ = target.posZ - entity.posZ;

        // Calculate the perpendicular direction to the target
        double perpendicularX = -deltaZ;
        double perpendicularZ = deltaX;

        // Determine whether to strafe left or right
        if (strafeLeft)
        {
            // Strafe to the left
            return Vec3.createVectorHelper(entity.posX + perpendicularX, entity.posY, entity.posZ + perpendicularZ);
        }
        else
        {
            // Strafe to the right
            return Vec3.createVectorHelper(entity.posX - perpendicularX, entity.posY, entity.posZ - perpendicularZ);
        }
    }
}
