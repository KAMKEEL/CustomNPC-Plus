package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityLaser;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergyLaser;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyLaser<T extends EntityAbilityLaser> extends ScriptEnergyProjectile<T> implements IEnergyLaser {

    public ScriptEnergyLaser(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_LASER;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_LASER || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 3;
    }

    // ==================== LASER-SPECIFIC ====================

    public float getLaserWidth() {
        return entity.getLaserWidth();
    }

    public void setLaserWidth(float width) {
        entity.setLaserWidth(width);
    }

    public float getExpansionSpeed() {
        return entity.getExpansionSpeed();
    }

    public void setExpansionSpeed(float speed) {
        entity.setExpansionSpeed(speed);
    }

    public float getMaxLength() {
        return entity.getMaxLength();
    }

    public void setMaxLength(float maxLength) {
        entity.setMaxLength(maxLength);
    }

    public float getCurrentLength() {
        return entity.getCurrentLength();
    }

    public boolean isFullyExtended() {
        return entity.isFullyExtended();
    }

    public double getDirX() {
        return entity.getDirX();
    }

    public double getDirY() {
        return entity.getDirY();
    }

    public double getDirZ() {
        return entity.getDirZ();
    }

    public void setDirection(double x, double y, double z) {
        entity.setDirection(x, y, z);
    }

    public double getEndX() {
        return entity.getEndX();
    }

    public double getEndY() {
        return entity.getEndY();
    }

    public double getEndZ() {
        return entity.getEndZ();
    }

    // ==================== FIRE ====================

    @Override
    protected void launchFromOwner(EntityLivingBase target) {
        entity.startMoving(target);
    }

    @Override
    public void fireAt(IEntity target) {
        entity.setTrackOwnerOrigin(false);
        super.fireAt(target);
    }

    @Override
    public void fireAt(double x, double y, double z) {
        entity.setTrackOwnerOrigin(false);
        double dx = x - entity.posX;
        double dy = y - entity.posY;
        double dz = z - entity.posZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            entity.setDirection(dx / len, dy / len, dz / len);
        }
        ensureSpawned();
    }

    @Override
    public void fireDirection(float yaw, float pitch) {
        entity.setTrackOwnerOrigin(false);
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        entity.setDirection(
            -Math.sin(yawRad) * Math.cos(pitchRad),
            -Math.sin(pitchRad),
            Math.cos(yawRad) * Math.cos(pitchRad)
        );
        ensureSpawned();
    }
}
