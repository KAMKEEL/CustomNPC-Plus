package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergySweeper;
import noppes.npcs.api.entity.IEnergySweeper;

public class ScriptEnergySweeper<T extends EntityEnergySweeper> extends ScriptEnergyAbility<T> implements IEnergySweeper {

    public ScriptEnergySweeper(T entity) {
        super(entity);
    }

    // ==================== BEAM DIMENSIONS ====================

    @Override
    public float getBeamLength() {
        return entity.getBeamLength();
    }

    @Override
    public void setBeamLength(float length) {
        entity.setBeamLength(length);
    }

    @Override
    public float getBeamWidth() {
        return entity.getBeamWidth();
    }

    @Override
    public void setBeamWidth(float width) {
        entity.setBeamWidth(width);
    }

    @Override
    public float getBeamHeight() {
        return entity.getBeamHeight();
    }

    @Override
    public void setBeamHeight(float height) {
        entity.setBeamHeight(height);
    }

    // ==================== ROTATION ====================

    @Override
    public float getSweepSpeed() {
        return entity.getSweepSpeed();
    }

    @Override
    public void setSweepSpeed(float degreesPerTick) {
        entity.setSweepSpeed(degreesPerTick);
    }

    @Override
    public int getNumberOfRotations() {
        return entity.getNumberOfRotations();
    }

    @Override
    public void setNumberOfRotations(int rotations) {
        entity.setNumberOfRotations(rotations);
    }

    @Override
    public boolean isLockOnTarget() {
        return entity.isLockOnTarget();
    }

    @Override
    public void setLockOnTarget(boolean lock) {
        entity.setLockOnTarget(lock);
    }

    // ==================== DAMAGE ====================

    @Override
    public float getDamage() {
        return entity.getDamage();
    }

    @Override
    public void setDamage(float damage) {
        entity.setDamage(damage);
    }

    @Override
    public int getDamageInterval() {
        return entity.getDamageInterval();
    }

    @Override
    public void setDamageInterval(int ticks) {
        entity.setDamageInterval(ticks);
    }

    @Override
    public boolean isPiercing() {
        return entity.isPiercing();
    }

    @Override
    public void setPiercing(boolean piercing) {
        entity.setPiercing(piercing);
    }

    // ==================== SPAWNING ====================

    @Override
    public void spawn() {
        if (!entity.addedToChunk && entity.worldObj != null) {
            entity.worldObj.spawnEntityInWorld(entity);
        }
    }
}
