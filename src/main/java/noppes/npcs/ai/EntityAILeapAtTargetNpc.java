package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAILeapAtTargetNpc extends EntityAIBase {
    private final EntityNPCInterface npc;
    private EntityLivingBase leapTarget;
    private final float leapMotionY;

    public EntityAILeapAtTargetNpc(EntityNPCInterface npc, float leapMotionY) {
        this.npc = npc;
        this.leapMotionY = leapMotionY;
        this.setMutexBits(5);
    }

    @Override
    public boolean shouldExecute() {
        this.leapTarget = this.npc.getAttackTarget();
        if (this.leapTarget == null) {
            return false;
        }
        double distanceSq = this.npc.getDistanceSqToEntity(this.leapTarget);
        if (distanceSq < 4.0D || distanceSq > 16.0D) {
            return false;
        }
        if (!this.npc.onGround) {
            return false;
        }
        return this.npc.getRNG().nextInt(5) == 0;
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
        double d0 = this.leapTarget.posX - this.npc.posX;
        double d1 = this.leapTarget.posZ - this.npc.posZ;
        float f = MathHelper.sqrt_double(d0 * d0 + d1 * d1);

        if ((double) f >= 1.0E-4D) {
            this.npc.motionX += d0 / (double) f * 0.5D * 0.8D + this.npc.motionX * 0.2D;
            this.npc.motionZ += d1 / (double) f * 0.5D * 0.8D + this.npc.motionZ * 0.2D;
        }

        this.npc.motionY = this.leapMotionY;
        this.npc.setNpcJumpingState(true);
    }

    @Override
    public void resetTask() {
        this.npc.setNpcJumpingState(false);
    }
}
