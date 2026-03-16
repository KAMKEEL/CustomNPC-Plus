package kamkeel.npcs.controllers.data.ability.conditions;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
public class ConditionHPThreshold extends ConditionThreshold {

    public ConditionHPThreshold() {
        this.typeId = "condition.cnpc.hp_threshold";
        this.name = "condition.hp_threshold";
    }

    @Override
    protected float getEntityValue(IEntityLivingBase IEntity) {
        return IEntity.getHealth();
    }

    @Override
    protected float getEntityMaxValue(IEntityLivingBase IEntity) {
        return IEntity.getMaxHealth();
    }

    @Override
    protected String getStatName() {
        return "HP";
    }
}
