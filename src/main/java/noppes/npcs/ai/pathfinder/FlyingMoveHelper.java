package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

// Fly Change

public class FlyingMoveHelper extends EntityMoveHelper {
    public double posX;
    public double posY;
    public double posZ;
    protected boolean update;
    private EntityNPCInterface entity;
    private int field_179928_h;

    public FlyingMoveHelper(EntityNPCInterface entity) {
        super(entity);
        this.entity = entity;
    }

    public void onUpdateMoveHelper() {
        if (this.update) {
            double d0 = this.posX - this.entity.posX;
            double d1 = this.posY - this.entity.posY;
            double d2 = this.posZ - this.entity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (this.field_179928_h-- <= 0) {
                this.field_179928_h += this.entity.getRNG().nextInt(5) + 2;
                d3 = (double) MathHelper.sqrt_double(d3);

                if (d3 > 1 && this.func_179926_b(this.posX, this.posY, this.posZ, d3)) {
                    double speed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() / 2.5;
                    this.entity.motionX += d0 / d3 * speed;
                    this.entity.motionY += d1 / d3 * speed;
                    this.entity.motionZ += d2 / d3 * speed;
                    this.entity.renderYawOffset = this.entity.rotationYaw = -((float) Math.atan2(this.entity.motionX, this.entity.motionZ)) * 180.0F / (float) Math.PI;
                } else {
                    this.update = false;
                }
            }
        }
    }

    private boolean func_179926_b(double p_179926_1_, double p_179926_3_, double p_179926_5_, double p_179926_7_) {
        double d4 = (p_179926_1_ - this.entity.posX) / p_179926_7_;
        double d5 = (p_179926_3_ - this.entity.posY) / p_179926_7_;
        double d6 = (p_179926_5_ - this.entity.posZ) / p_179926_7_;
        AxisAlignedBB axisalignedbb = this.entity.getBoundingBox();

        for (int i = 1; (double) i < p_179926_7_; ++i) {
            axisalignedbb = axisalignedbb.offset(d4, d5, d6);

            if (!this.entity.worldObj.getCollidingBoundingBoxes(this.entity, axisalignedbb).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
