package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityBeam;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergyBeam;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyBeam<T extends EntityAbilityBeam> extends ScriptEnergyProjectile<T> implements IEnergyBeam {

    public ScriptEnergyBeam(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_BEAM;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_BEAM || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 1;
    }

    // ==================== BEAM-SPECIFIC ====================

    public float getBeamWidth() {
        return entity.getBeamWidth();
    }

    public void setBeamWidth(float width) {
        entity.setBeamWidth(width);
    }

    public float getHeadSize() {
        return entity.getHeadSize();
    }

    public void setHeadSize(float size) {
        entity.setHeadSize(size);
    }

    public boolean isAttachedToOwner() {
        return entity.isAttachedToOwner();
    }

    public void setAttachedToOwner(boolean attached) {
        entity.setAttachedToOwner(attached);
    }

    public boolean shouldRenderTailOrb() {
        return entity.shouldRenderTailOrb();
    }

    // ==================== FIRE ====================

    @Override
    public void fire(IEntity target) {
        ensureSpawned();
        EntityLivingBase mcTarget = null;
        if (target != null && target.getMCEntity() instanceof EntityLivingBase) {
            mcTarget = (EntityLivingBase) target.getMCEntity();
        }
        entity.startFiring(mcTarget);
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
        entity.startFiring(null);
    }
}
