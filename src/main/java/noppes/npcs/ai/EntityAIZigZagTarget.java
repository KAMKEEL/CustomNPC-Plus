package noppes.npcs.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.AiMutex;

public class EntityAIZigZagTarget extends EntityAIBase {
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

    public EntityAIZigZagTarget(EntityCreature par1EntityCreature, double par2, float par4) {
        this.theEntity = par1EntityCreature;
        this.speed = par2;
        this.tacticalRange = par4;
        this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        this.targetEntity = this.theEntity.getAttackTarget();

        if (this.targetEntity == null) {
            return false;
        } else if (this.targetEntity.getDistanceSqToEntity(this.theEntity) < (double) (this.tacticalRange * this.tacticalRange)) {
            return false;
        } else {
            PathEntity pathentity = this.theEntity.getNavigator().getPathToEntityLiving(targetEntity);

            if (pathentity != null) {
                if (pathentity.getCurrentPathLength() >= this.tacticalRange) {
                    PathPoint pathpoint = pathentity.getPathPointFromIndex(MathHelper.floor_double(this.tacticalRange / 2.0D));
                    this.entityPosX = pathpoint.xCoord;
                    this.entityPosY = pathpoint.yCoord;
                    this.entityPosZ = pathpoint.zCoord;

                    Vec3 vec3 = RandomPositionGeneratorAlt.findRandomTargetBlockTowards(this.theEntity, (int) tacticalRange, 3, Vec3.createVectorHelper(this.entityPosX, this.entityPosY, this.entityPosZ));

                    if (vec3 != null) {
                        if (this.targetEntity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) < this.targetEntity.getDistanceSq(entityPosX, entityPosY, entityPosZ)) {
                            this.movePosX = vec3.xCoord;
                            this.movePosY = vec3.yCoord;
                            this.movePosZ = vec3.zCoord;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !this.theEntity.getNavigator().noPath() && this.targetEntity.isEntityAlive() && this.targetEntity.getDistanceSqToEntity(this.theEntity) > (double) (this.tacticalRange * this.tacticalRange);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.theEntity.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        this.theEntity.getLookHelper().setLookPositionWithEntity(targetEntity, 30.0F, 30.0F);
    }
}
