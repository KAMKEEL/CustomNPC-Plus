package noppes.npcs.entity;

import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityNPCFlying extends EntityNPCInterface {

    public boolean flyLimitAllow = false;

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
        if(!this.canFly() || this.hurtTime != 0 || !this.canBreathe()) {
            super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
            return;
        }

        boolean aboveLimit = false;
        double heightOffGround = this.posY - this.worldObj.getTopSolidOrLiquidBlock((int) this.posX, (int) this.posZ);
        if (heightOffGround < 0) {
            Vec3 pos = Vec3.createVectorHelper(this.posX,this.posY,this.posZ);
            Vec3 posLimit = Vec3.createVectorHelper(this.posX,this.posY - this.ai.flyHeightLimit,this.posZ);
            MovingObjectPosition mob = this.worldObj.rayTraceBlocks(pos,posLimit,true);
            if (mob == null || mob.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                aboveLimit = true;
            }
        } else if (heightOffGround > this.ai.flyHeightLimit) {
            aboveLimit = true;
        }
        if (aboveLimit && this.ai.hasFlyLimit || (heightOffGround < Math.ceil(this.height) && this.motionY == 0)) {
            this.flyLimitAllow = false;
            if (!this.getNavigator().noPath() && this.motionY > 0) {
                this.motionY = 0;
            } else {
                super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
                return;
            }
        }
        this.flyLimitAllow = true;

        double d3 = this.motionY;
        super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
        this.motionY = d3;

        if (this.getNavigator().noPath()) {
            this.motionY = -Math.abs(this.ai.flyGravity);
        }

        this.updateLimbSwing();
        this.velocityChanged = true;
    }

    public void updateLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double distanceX = this.posX - this.prevPosX;
        double distanceZ = this.posZ - this.prevPosZ;
        float distance = MathHelper.sqrt_double(distanceX * distanceX + distanceZ * distanceZ) * 4.0F;
        if (distance > 1.0F) {
            distance = 1.0F;
        }
        this.limbSwingAmount += (distance - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }
}
