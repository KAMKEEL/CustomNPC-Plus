package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIPounceTarget extends EntityAIBase {
    private EntityNPCInterface npc;
    private EntityLivingBase leapTarget;
    private float leapSpeed = 1.2F;

    public EntityAIPounceTarget(EntityNPCInterface leapingEntity) {
        this.npc = leapingEntity;
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.npc.onGround)
            return false;

        this.leapTarget = this.npc.getAttackTarget();

        if (this.leapTarget == null || !this.npc.getEntitySenses().canSee(leapTarget))
            return false;

        return !npc.isInRange(leapTarget, 4) && npc.isInRange(leapTarget, 8) && this.npc.getRNG().nextInt(5) == 0;
    }

    @Override
    public boolean continueExecuting() {
        boolean airborne = !this.npc.onGround;
        if (!airborne) {
            this.npc.setNpcJumpingState(false);
        }
        return airborne;
    }

    @Override
    public void startExecuting() {
        double varX = this.leapTarget.posX - this.npc.posX;
        double varY = this.leapTarget.boundingBox.minY - this.npc.boundingBox.minY;
        double varZ = this.leapTarget.posZ - this.npc.posZ;
        float varF = MathHelper.sqrt_double(varX * varX + varZ * varZ);
        float angle = this.getAngleForXYZ(varX, varY, varZ, varF);
        float yaw = (float) (Math.atan2(varX, varZ) * 180.0D / Math.PI);
        this.npc.motionX = (double) (MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(angle / 180.0F * (float) Math.PI));
        this.npc.motionZ = (double) (MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(angle / 180.0F * (float) Math.PI));
        this.npc.motionY = (double) (MathHelper.sin((angle + 1.0F) / 180.0F * (float) Math.PI));
        this.npc.motionX *= this.leapSpeed;
        this.npc.motionZ *= this.leapSpeed;
        this.npc.motionY *= this.leapSpeed;
        this.npc.setNpcJumpingState(true);
    }

    @Override
    public void resetTask() {
        this.npc.setNpcJumpingState(false);
    }

    public float getAngleForXYZ(double varX, double varY, double varZ, double horiDist) {
        float g = 0.1F;
        float var1 = this.leapSpeed * this.leapSpeed;
        double var2 = (g * horiDist);
        double var3 = ((g * horiDist * horiDist) + (2 * varY * var1));
        double var4 = (var1 * var1) - (g * var3);
        if (var4 < 0) return 90.0F;
        float var6 = var1 - MathHelper.sqrt_double(var4);
        float var7 = (float) (Math.atan2(var6, var2) * 180.0D / Math.PI);
        return var7;
    }
}
