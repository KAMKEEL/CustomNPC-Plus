package noppes.npcs.scripted;

import kamkeel.npcs.controllers.TelegraphController;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import net.minecraft.entity.Entity;
import noppes.npcs.api.ITelegraphInstance;
import noppes.npcs.api.entity.IEntity;

/**
 * Script wrapper for TelegraphInstance.
 * Implements ITelegraphInstance API interface.
 */
public class ScriptTelegraphInstance implements ITelegraphInstance {

    private final TelegraphInstance instance;

    public ScriptTelegraphInstance(TelegraphInstance instance) {
        this.instance = instance;
    }

    /**
     * Get the underlying TelegraphInstance object.
     */
    public TelegraphInstance getMCInstance() {
        return instance;
    }

    @Override
    public String getInstanceId() {
        return instance.getInstanceId();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // POSITION
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public double getX() {
        return instance.getX();
    }

    @Override
    public double getY() {
        return instance.getY();
    }

    @Override
    public double getZ() {
        return instance.getZ();
    }

    @Override
    public float getYaw() {
        return instance.getYaw();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        instance.setX(x);
        instance.setY(y);
        instance.setZ(z);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENTITY FOLLOWING
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void followEntity(IEntity entity) {
        if (entity == null || entity.getMCEntity() == null) return;
        Entity mcEntity = (Entity) entity.getMCEntity();
        instance.setEntityIdToFollow(mcEntity.getEntityId());
    }

    @Override
    public void stopFollowing() {
        instance.lockPosition();
    }

    @Override
    public boolean isFollowing() {
        return instance.getEntityIdToFollow() >= 0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMING
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public int getRemainingTicks() {
        return instance.getRemainingTicks();
    }

    @Override
    public int getTotalTicks() {
        return instance.getTotalTicks();
    }

    @Override
    public float getProgress() {
        return instance.getProgress();
    }

    @Override
    public boolean isWarning() {
        return instance.isWarning();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTROL
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void remove() {
        TelegraphController.Instance.remove(instance);
    }

    @Override
    public void lockPosition() {
        instance.lockPosition();
    }
}
