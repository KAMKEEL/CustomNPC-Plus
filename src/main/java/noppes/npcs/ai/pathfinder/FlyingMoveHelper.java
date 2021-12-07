package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

// Fly Change

public class FlyingMoveHelper extends EntityMoveHelper{
    private EntityNPCInterface entity;

    private double posX;
    private double posY;
    private double posZ;
    private double speed;
    private boolean update;
    private static final String __OBFID = "CL_00001573";

    public FlyingMoveHelper(EntityNPCInterface entity){
        super(entity);
        this.entity = entity;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
    }

    public void onUpdateMoveHelper() {
        this.entity.setMoveForward(0.0F);

        if (this.update) {
            this.update = false;
            int i = MathHelper.floor_double(this.entity.boundingBox.minY + 0.5D);
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posZ - this.entity.posZ;
            double d2 = this.posY - (double)i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;

            if (d3 >= 2.500000277905201E-7D) {
                float f = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
                this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, 30.0F);
                this.entity.setAIMoveSpeed((float)(this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()));

                if (d2 > 0.0D && d0 * d0 + d1 * d1 < 1.0D) {
                    this.entity.getJumpHelper().setJumping();
                }
            }
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
