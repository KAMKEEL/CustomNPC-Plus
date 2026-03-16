package noppes.npcs.controllers;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

public class ScriptEntityData implements IExtendedEntityProperties {
    public IEntity base;

    public ScriptEntityData(IEntity base) {
        this.base = base;
    }

    @Override
    public void saveNBTData(INbt compound) {
    }

    @Override
    public void loadNBTData(INbt compound) {
    }

    @Override
    public void init(IEntity IEntity, IWorld IWorld) {
    }
}
