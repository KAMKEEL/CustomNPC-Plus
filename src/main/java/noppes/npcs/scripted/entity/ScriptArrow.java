package noppes.npcs.scripted.entity;

import net.minecraft.entity.projectile.EntityArrow;
import noppes.npcs.api.entity.IArrow;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptArrow<T extends EntityArrow> extends ScriptEntity<T> implements IArrow {

    public ScriptArrow(T entity) {
        super(entity);
    }

    @Override
    public IEntity getShooter() {
        if (entity.shootingEntity != null)
            return NpcAPI.Instance().getIEntity(entity.shootingEntity);
        return null;
    }

    @Override
    public double getDamage() {
        return entity.getDamage();
    }

    @Override
    public void kill() {
        entity.setDead();
    }

    @Override
    public int getType() {
        return EntityType.ARROW;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ARROW || super.typeOf(type);
    }
}
