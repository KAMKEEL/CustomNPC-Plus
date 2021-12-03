package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityNPCFlying extends EntityNPCInterface {

	public EntityNPCFlying(World world) {
		super(world);
        this.getNavigator().setCanSwim(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
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

    public void onLivingUpdate()
    {
        if(!canFly()){
            super.onLivingUpdate();
            return;
        }

        double speed = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
        double speedMult = 0.6000000238418579D/0.5D;

        this.motionY *= speed*speedMult;

        Entity entity = this.getAttackTarget();
        if (!this.worldObj.isRemote)
        {
            if (entity != null)
            {
                if (this.posY < entity.posY)
                {
                    if (this.motionY < 0.0D)
                    {
                        this.motionY = 0.0D;
                    }

                    this.motionY += (speed - this.motionY) * speed*speedMult;
                }

                double d0 = entity.posX - this.posX;
                double d1 = entity.posY - this.posY;
                double d2 = entity.posZ - this.posZ;
                double d3 = d0 * d0 + d2 * d2;

                if (d3 > 9.0D)
                {
                    double d5 = (double)MathHelper.sqrt_double(d3);
                    this.motionX += (d0 / d5 * speed - this.motionX) * speed*speedMult;
                    this.motionZ += (d2 / d5 * speed - this.motionZ) * speed*speedMult;
                }

                this.rotationYaw = (float)((Math.atan2(-d0,-d2)+Math.PI)*-(180F/Math.PI));
            }
        }

        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.05000000074505806D && isEntityAlive())
        {
            this.rotationYaw = (float)Math.atan2(this.motionZ, this.motionX) * (180F / (float)Math.PI) - 90.0F;
        }

        super.onLivingUpdate();
    }

    @Override
    public boolean isOnLadder(){
        return false;
    }




}
