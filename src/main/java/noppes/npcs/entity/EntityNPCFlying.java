package noppes.npcs.entity;

import net.minecraft.init.Blocks;
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

    @Override
    public boolean isOnLadder(){
        return false;
    }

    public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_)
    {
        if(!this.canFly() || this.hurtTime != 0) {
            super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
            return;
        }

        double heightOffGround = 0;
        if(this.ai.hasFlyLimit) {
            for (int blockY = (int) this.posY; blockY > 0; blockY--) {
                heightOffGround = this.posY - blockY;
                if (this.worldObj.getBlock((int) this.posX, blockY, (int) this.posZ) != Blocks.air || heightOffGround > this.ai.flyHeightLimit){
                    break;
                }
            }
        }

        if (heightOffGround > this.ai.flyHeightLimit && this.ai.hasFlyLimit) {
            super.moveEntityWithHeading(p_70612_1_,p_70612_2_);
            return;
        }

        double d3 = this.motionY;
        super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
        this.motionY = d3;

        if(this.getNavigator().noPath())
            this.motionY = 0.0D;

        this.velocityChanged = true;
    }
}
