package noppes.npcs.scripted.entity;

import net.minecraft.entity.projectile.EntityFishHook;
import noppes.npcs.api.entity.IFishHook;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptFishHook<T extends EntityFishHook> extends ScriptEntity<T> implements IFishHook {

    public ScriptFishHook(T entity) {
        super(entity);
    }

    @Override
    public IPlayer getCaster() {
        if(entity.field_146042_b != null)
            return (IPlayer) NpcAPI.Instance().getIEntity(entity.field_146042_b);
        return null;
    }

    @Override
    public void kill() {
        entity.setDead();
    }

    @Override
    public int getType() {
        return EntityType.FISH_HOOK;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.FISH_HOOK || super.typeOf(type);
    }
}
