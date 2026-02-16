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

    public float getLaserWidth() { return entity.getLaserWidth(); }
    public void setLaserWidth(float width) { entity.setLaserWidth(width); }

    public float getExpansionSpeed() { return entity.getExpansionSpeed(); }
    public void setExpansionSpeed(float speed) { entity.setExpansionSpeed(speed); }

    public int getLingerTicks() { return entity.getLingerTicks(); }
    public void setLingerTicks(int ticks) { entity.setLingerTicks(ticks); }

    public float getCurrentLength() { return entity.getCurrentLength(); }
    public boolean isFullyExtended() { return entity.isFullyExtended(); }

    public double getDirX() { return entity.getDirX(); }
    public double getDirY() { return entity.getDirY(); }
    public double getDirZ() { return entity.getDirZ(); }
    public void setDirection(double x, double y, double z) { entity.setDirection(x, y, z); }

    public double getEndX() { return entity.getEndX(); }
    public double getEndY() { return entity.getEndY(); }
    public double getEndZ() { return entity.getEndZ(); }

    public float getLingerAlpha() { return entity.getLingerAlpha(); }

    public boolean isLockVerticalDirection() { return entity.isLockVerticalDirection(); }
    public void setLockVerticalDirection(boolean lock) { entity.setLockVerticalDirection(lock); }

    // ==================== FIRE ====================

    @Override
    public void fire(IEntity target) {
        ensureSpawned();
        EntityLivingBase mcTarget = null;
        if (target != null && target.getMCEntity() instanceof EntityLivingBase) {
            mcTarget = (EntityLivingBase) target.getMCEntity();
        }
        entity.startMoving(mcTarget);
    }

    @Override
    public void fire(double x, double y, double z) {
        ensureSpawned();
        double dx = x - entity.posX;
        double dy = y - entity.posY;
        double dz = z - entity.posZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            entity.setDirection(dx / len, dy / len, dz / len);
        }
        entity.startMoving(null);
    }
}
