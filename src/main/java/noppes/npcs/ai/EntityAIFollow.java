package noppes.npcs.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIFollow extends EntityAIBase
{
    private EntityNPCInterface npc;
    private EntityLivingBase owner;
    private double distance;
	public int updateTick = 0;

    public EntityAIFollow(EntityNPCInterface npc){
        this.npc = npc;
        this.setMutexBits(AiMutex.PASSIVE + AiMutex.LOOK);
    }

    @Override
    public boolean shouldExecute(){
    	if(!excute())
    		return false;
    	return distance > npc.followRange();
    }
    
    public boolean excute(){
    	if(!npc.isEntityAlive() || !npc.isFollower() || npc.isAttacking() || (owner = npc.getOwner()) == null || npc.ai.animationType == EnumAnimation.SITTING)
    		return false;
    	distance = npc.getDistanceSqToEntity(owner);
    	return true;
    }

    @Override
    public void startExecuting(){
		updateTick = 10;
    }

    @Override
    public boolean continueExecuting(){
        return !this.npc.getNavigator().noPath() && distance > 4 && excute();
    }
    @Override
    public void resetTask(){
        this.owner = null;
        this.npc.getNavigator().clearPathEntity();
    }

    @Override
    public void updateTask(){
    	updateTick++;
    	if(updateTick < 10)
    		return;
    	updateTick = 0;
        this.npc.getLookHelper().setLookPositionWithEntity(owner, 10.0F, (float)this.npc.getVerticalFaceSpeed());

		double speed = 1 + distance / 150;
		if(speed > 3)
			speed = 3;
        if (this.npc.getNavigator().tryMoveToEntityLiving(owner, speed) || distance < 225.0D)
        	return;
        
        int i = MathHelper.floor_double(this.owner.posX) - 2;
        int j = MathHelper.floor_double(this.owner.posZ) - 2;
        int k = MathHelper.floor_double(this.owner.boundingBox.minY);

        for (int l = 0; l <= 4; ++l)
        {
            for (int i1 = 0; i1 <= 4; ++i1)
            {
                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(npc.worldObj, i + l, k - 1, j + i1) && !npc.worldObj.getBlock(i + l, k, j + i1).isNormalCube() && !npc.worldObj.getBlock(i + l, k + 1, j + i1).isNormalCube())
                {
                    this.npc.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.npc.rotationYaw, this.npc.rotationPitch);
                    this.npc.getNavigator().clearPathEntity();
                    return;
                }
            }
        }
    }
}
