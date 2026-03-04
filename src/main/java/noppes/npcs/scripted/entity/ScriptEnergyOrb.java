package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergyOrb;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyOrb<T extends EntityAbilityOrb> extends ScriptEnergyProjectile<T> implements IEnergyOrb {

    public ScriptEnergyOrb(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_ORB;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_ORB || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 0;
    }

    @Override
    public void fireAt(IEntity target) {
        ensureSpawned();
        EntityLivingBase mcTarget = null;
        if (target != null && target.getMCEntity() instanceof EntityLivingBase) {
            mcTarget = (EntityLivingBase) target.getMCEntity();
        }
        entity.startMoving(mcTarget);
    }

    @Override
    public void fireAt(double x, double y, double z) {
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
