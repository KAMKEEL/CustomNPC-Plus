package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCFlying;
import noppes.npcs.entity.EntityNPCInterface;

public class FlyingMoveHelper extends EntityMoveHelper{
    private final EntityNPCInterface entity;

    private double posX;
    private double posY;
    private double posZ;
    private double speed;
    public boolean update;

    public FlyingMoveHelper(EntityNPCInterface entity){
        super(entity);
        this.entity = entity;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
    }

    public void onUpdateMoveHelper() {
        if (this.update) {
            this.update = false;

            double speed = this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
            double verticalSpeed = this.speed * this.entity.ai.flySpeed/8.0D;
            this.entity.setAIMoveSpeed((float)speed);

            double d0 = this.posX - this.entity.posX;
            double d1 = this.posY - this.entity.posY;
            double d2 = this.posZ - this.entity.posZ;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            double d5 = MathHelper.sqrt_double(d4);
            speed = Math.min(d5/5.0D,speed);

            if (this.entity.hurtTime == 0 && d4 > 0.5D) {
                this.entity.motionX += (speed * (d0 / d5) - this.entity.motionX) * speed;
                this.entity.motionZ += (speed * (d2 / d5) - this.entity.motionZ) * speed;

                if (((EntityNPCFlying)this.entity).flyLimitAllow || !this.entity.ai.hasFlyLimit) {
                    this.entity.motionY = verticalSpeed * (d1 / d5);
                    if (this.entity.motionY > 0) {
                        this.entity.motionY += 0.1D;
                    }
                }

                this.entity.velocityChanged = true;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw,(float) ((Math.atan2(-d0, -d2) + Math.PI) * -(180F / Math.PI)),20.0F);
            }
        }
    }

    private float limitAngle(float angleIn, float lower, float upper)
    {
        float f = MathHelper.wrapAngleTo180_float(lower - angleIn);

        if (f > upper)
        {
            f = upper;
        }

        if (f < -upper)
        {
            f = -upper;
        }

        float f1 = angleIn + f;

        if (f1 < 0.0F)
        {
            f1 += 360.0F;
        }
        else if (f1 > 360.0F)
        {
            f1 -= 360.0F;
        }

        return f1;
    }

    /**
     * Sets the speed and location to move to
     */
    public void setMoveTo(double p_75642_1_, double p_75642_3_, double p_75642_5_, double p_75642_7_)
    {
        this.posX = p_75642_1_;
        this.posY = p_75642_3_;
        this.posZ = p_75642_5_;
        this.speed = p_75642_7_;
        this.update = true;
    }

    public boolean isUpdating()
    {
        return this.update;
    }

    public double getSpeed()
    {
        return this.speed;
    }
}
