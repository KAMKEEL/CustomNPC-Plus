package noppes.npcs.scripted.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import noppes.npcs.api.entity.*;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptThrowable<T extends EntityThrowable> extends ScriptEntity<T> implements IThrowable {

    public ScriptThrowable(T entity) {
        super(entity);
    }

    @Override
    public IEntityLivingBase getThrower() {
        if(entity.getThrower() != null)
            return (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity.getThrower());
        return null;
    }

    @Override
    public void kill() {
        entity.setDead();
    }

    @Override
    public int getType() {
        return EntityType.THROWABLE;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.THROWABLE || super.typeOf(type);
    }
}
