package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityNPCFlying extends EntityNPCInterface {

	public EntityNPCFlying(World world) {
		super(world);
	}

	@Override
	public boolean canFly(){
		return ai.movementType == 1;
	}

    @Override
    public void fall(float distance) {
    	if(!canFly())
    		super.fall(distance);
    }

    @Override
    protected void updateFallState(double p_180433_1_, boolean p_180433_3_) {
    	if(!canFly())
    		super.updateFallState(p_180433_1_, p_180433_3_);
    }

    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if(!canFly()){
            return;
        }

        int blockY = (int) this.posY;
        double heightOffGround = 0;
        if(this.ai.hasFlyLimit) {
            for (blockY = (int) this.posY; blockY > 0; blockY--) {
                heightOffGround = this.posY - blockY;
                if (this.worldObj.getBlock((int) this.posX, blockY, (int) this.posZ) != Blocks.air || heightOffGround > this.ai.flyHeightLimit){
                    break;
                }
            }
        }

        if (!this.onGround && !this.velocityChanged && this.motionY < 0 && this.hurtTime == 0)
        {
            if(heightOffGround >= this.ai.flyHeightLimit && this.ai.hasFlyLimit){
                this.motionY *= 0.8D;
                this.velocityChanged = true;
            } else {
                this.motionY *= this.ai.flyGravity;
                this.velocityChanged = true;
            }
        }
    }

    @Override
    public boolean isOnLadder(){
        return false;
    }




}
