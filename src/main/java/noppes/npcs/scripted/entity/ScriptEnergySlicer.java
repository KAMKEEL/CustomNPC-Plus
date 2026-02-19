package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergySlicer;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergySlicer;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergySlicer<T extends EntityEnergySlicer> extends ScriptEnergyProjectile<T> implements IEnergySlicer {

    public ScriptEnergySlicer(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_SLICER;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_SLICER || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 4;
    }

    // ==================== SLICER-SPECIFIC ====================

    public float getSliceWidth() {
        return entity.getSliceWidth();
    }

    public void setSliceWidth(float width) {
        entity.setSliceWidth(width);
    }

    public float getSliceThickness() {
        return entity.getSliceThickness();
    }

    public void setSliceThickness(float thickness) {
        entity.setSliceThickness(thickness);
    }

    public boolean isPiercing() {
        return entity.isPiercing();
    }

    public void setPiercing(boolean piercing) {
        entity.setPiercing(piercing);
    }

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
            entity.motionX = (dx / len) * entity.getSpeed();
            entity.motionY = (dy / len) * entity.getSpeed();
            entity.motionZ = (dz / len) * entity.getSpeed();
        }
        entity.startMoving(null);
    }
}
