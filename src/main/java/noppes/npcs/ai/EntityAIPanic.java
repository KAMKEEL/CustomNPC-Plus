package noppes.npcs.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.AiMutex;

public class EntityAIPanic extends EntityAIBase
{
    private EntityCreature theEntityCreature;
    private float speed;
    private double randPosX;
    private double randPosY;
    private double randPosZ;

    public EntityAIPanic(EntityCreature par1EntityCreature, float par2)
    {
        this.theEntityCreature = par1EntityCreature;
        this.speed = par2;
        this.setMutexBits(AiMutex.PASSIVE);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute(){
        if (this.theEntityCreature.getAttackTarget() == null && !this.theEntityCreature.isBurning()){
            return false;
        }
        else
        {
            Vec3 var1 = RandomPositionGeneratorAlt.findRandomTarget(this.theEntityCreature, 5, 4);

            if (var1 == null){
                return false;
            }
            else{
                this.randPosX = var1.xCoord;
                this.randPosY = var1.yCoord;
                this.randPosZ = var1.zCoord;
                return true;
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting(){
        this.theEntityCreature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting(){
    	if(this.theEntityCreature.getAttackTarget() == null)
    		return false;
        return !this.theEntityCreature.getNavigator().noPath();
    }
}