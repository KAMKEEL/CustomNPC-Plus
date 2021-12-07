package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
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
        if(!canFly() || this.isJumping){
            super.onLivingUpdate();
            return;
        }

        double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
        double speedMult = 0.6000000238418579D/0.5D;

        if(isEntityAlive()) {
            if (!this.worldObj.isRemote) {
                Entity target = this.getAttackTarget();
                if (target != null && target.isEntityAlive()) {
                    double d0 = target.posX - this.posX;
                    double d1 = target.posY - this.posY;
                    double d2 = target.posZ - this.posZ;
                    double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                    if(this.hurtTime == 0) {
                        if (this.posY != target.posY) {
                            if (this.motionY * Math.signum(this.posY - target.posY) > 0.0D) {
                                this.motionY = 0.0D;
                            }

                            this.motionY -= Math.signum(this.posY - target.posY) * (speed * speedMult) * this.ai.flySpeed;
                        }

                        if (d4 > 0.5D) {
                            double d5 = MathHelper.sqrt_double(d4);
                            this.motionX += (d0 / d5 * speed - this.motionX) * speed * speedMult;
                            this.motionZ += (d2 / d5 * speed - this.motionZ) * speed * speedMult;
                        }
                    }

                    this.rotationYaw = (float) ((Math.atan2(-d0, -d2) + Math.PI) * -(180F / Math.PI));
                } else {
                    if (!this.onGround && this.motionY < 0.0D)
                    {
                        this.motionY *= 0.6D;
                    }
                }
            }

            if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.05000000074505806D && isEntityAlive()) {
                this.rotationYaw = (float) Math.atan2(this.motionZ, this.motionX) * (180F / (float) Math.PI) - 90.0F;
            }
        }

        super.onLivingUpdate();
    }

    @Override
    public boolean isOnLadder(){
        return false;
    }




}
