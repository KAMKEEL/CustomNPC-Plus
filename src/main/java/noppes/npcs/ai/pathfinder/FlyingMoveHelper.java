package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

// Fly Change

public class FlyingMoveHelper extends EntityMoveHelper{
    private final EntityNPCInterface entity;

    private double posX;
    private double posY;
    private double posZ;
    private double speed;
    private boolean update;

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

            double speed = this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
            double speedMult = 0.6000000238418579D / 0.5D;

            double d0 = this.posX - this.entity.posX;
            double d1 = this.posY - this.entity.posY;
            double d2 = this.posZ - this.entity.posZ;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (this.entity.hurtTime == 0 && d4 > 0.5D) {
                if (this.posY != this.entity.posY) {
                    if (this.entity.motionY * Math.signum(this.entity.posY - this.posY) > 0.0D) {
                        this.entity.motionY = 0.0D;
                    }

                    double verticalSpeed = Math.abs(this.entity.posY - this.posY);
                    if(verticalSpeed > speed * speedMult)
                        verticalSpeed = speed * speedMult;
                    verticalSpeed += this.entity.ai.flySpeed/2.0D;

                    if(verticalSpeed > Math.abs(this.entity.posY - this.posY))
                        verticalSpeed = Math.abs(this.entity.posY - this.posY);

                    this.entity.motionY -= Math.signum(this.entity.posY - this.posY) * verticalSpeed;
                }

                double d5 = MathHelper.sqrt_double(d4);
                this.entity.motionX += (d0 / d5 * speed - this.entity.motionX) * speed * speedMult;
                this.entity.motionZ += (d2 / d5 * speed - this.entity.motionZ) * speed * speedMult;
            }

            this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw,(float) ((Math.atan2(-d0, -d2) + Math.PI) * -(180F / Math.PI)),30.0F);
        }
    }

    private float limitAngle(float p_75639_1_, float p_75639_2_, float p_75639_3_)
    {
        float f3 = MathHelper.wrapAngleTo180_float(p_75639_2_ - p_75639_1_);

        if (f3 > p_75639_3_)
        {
            f3 = p_75639_3_;
        }

        if (f3 < -p_75639_3_)
        {
            f3 = -p_75639_3_;
        }

        return p_75639_1_ + f3;
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
