package noppes.npcs.entity;

import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityNPCFlying extends EntityNPCInterface {

	public EntityNPCFlying(World world) {
		super(world);
	}

	// FLY CHANGE  [WIP]

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
    public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_){
    	if(!canFly()){
    		super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
    		return;
    	}

        if (this.isInWater()){
            this.moveFlying(p_70612_1_, p_70612_2_, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.800000011920929D;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= 0.800000011920929D;
        }
        else if (this.isInsideOfMaterial(Material.lava)){
            this.moveFlying(p_70612_1_, p_70612_2_, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }
        else{
            float f2 = 0.91F;

            if (this.onGround){
                f2 = this.worldObj.getBlock((int)this.posX, (int)this.boundingBox.minY - 1, (int)this.posZ).slipperiness * 0.91F;
            }

            float f3 = 0.16277136F / (f2 * f2 * f2);
            this.moveFlying(p_70612_1_, p_70612_2_, this.onGround ? 0.1F * f3 : 0.02F);
            f2 = 0.91F;

            if (this.onGround){
                f2 = this.worldObj.getBlock((int)this.posX, (int)this.boundingBox.minY - 1, (int)this.posZ).slipperiness * 0.91F;
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double)f2;
            this.motionY *= (double)f2;
            this.motionZ *= (double)f2;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d1 = this.posX - this.prevPosX;
        double d0 = this.posZ - this.prevPosZ;
        float f4 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

        if (f4 > 1.0F){
            f4 = 1.0F;
        }

        this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    public boolean isOnLadder(){
        return false;
    }




}
