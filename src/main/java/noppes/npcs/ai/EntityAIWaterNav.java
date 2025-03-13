package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWaterNav extends EntityAIBase {
    private final EntityNPCInterface theEntity;

    public EntityAIWaterNav(EntityNPCInterface par1EntityNPCInterface) {
        this.theEntity = par1EntityNPCInterface;
        par1EntityNPCInterface.getNavigator().setCanSwim(true);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (this.theEntity.isInWater() || this.theEntity.handleLavaMovement()) {
            if (this.theEntity.ais.canSwim) {
                return true;
            } else return this.theEntity.isCollidedHorizontally;

        }
        return false;
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        if (this.theEntity.getRNG().nextFloat() < 0.8F) {
            this.theEntity.getJumpHelper().setJumping();
        }
    }
}
