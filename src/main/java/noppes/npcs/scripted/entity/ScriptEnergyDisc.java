package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityDisc;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergyDisc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyDisc<T extends EntityAbilityDisc> extends ScriptEnergyProjectile<T> implements IEnergyDisc {

    public ScriptEnergyDisc(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_DISC;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_DISC || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 2;
    }

    // ==================== DISC-SPECIFIC ====================

    public float getDiscRadius() { return entity.getDiscRadius(); }
    public void setDiscRadius(float radius) { entity.setDiscRadius(radius); }

    public float getDiscThickness() { return entity.getDiscThickness(); }
    public void setDiscThickness(float thickness) { entity.setDiscThickness(thickness); }

    public boolean isVertical() { return entity.isVertical(); }
    public void setVertical(boolean vertical) { entity.setVertical(vertical); }

    public boolean isBoomerang() { return entity.isBoomerang(); }
    public void setBoomerang(boolean boomerang) { entity.setBoomerang(boomerang); }

    public int getBoomerangDelay() { return entity.getBoomerangDelay(); }
    public void setBoomerangDelay(int ticks) { entity.setBoomerangDelay(ticks); }

    public boolean isReturning() { return entity.isReturning(); }

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
