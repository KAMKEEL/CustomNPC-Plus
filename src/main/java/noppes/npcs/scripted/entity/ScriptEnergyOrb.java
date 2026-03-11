package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergyOrb;
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
    protected void launchFromOwner(EntityLivingBase target) {
        entity.startMoving(target);
    }
}
