package noppes.npcs.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAISprintToTarget extends EntityAIBase {
    private EntityNPCInterface npc;

    public EntityAISprintToTarget(EntityNPCInterface par1EntityLiving) {
        this.npc = par1EntityLiving;
        this.setMutexBits(AiMutex.PASSIVE);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase runTarget = this.npc.getAttackTarget();

        if (runTarget == null || this.npc.getNavigator().noPath()) {
            return false;
        }

        switch (this.npc.ais.onAttack) {
            case 0:
                return !this.npc.isInRange(runTarget, 8) ? (!this.npc.onGround ? false : true) : false;
            case 2:
                return this.npc.isInRange(runTarget, 7) ? (!this.npc.onGround ? false : true) : false;
            default:
                return false;
        }

    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return this.npc.isEntityAlive() && this.npc.onGround && this.npc.hurtTime <= 0 && (this.npc.motionX != 0.0D && this.npc.motionZ != 0.0D);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.npc.setSprinting(true);
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        this.npc.setSprinting(false);
    }
}
