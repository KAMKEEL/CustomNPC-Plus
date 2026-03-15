package kamkeel.npcs.controllers.data.ability.conditions;

import net.minecraft.entity.EntityLivingBase;

public class ConditionHPThreshold extends ConditionThreshold {

    public ConditionHPThreshold() {
        this.typeId = "condition.cnpc.hp_threshold";
        this.name = "condition.hp_threshold";
    }

    @Override
    protected float getEntityValue(EntityLivingBase entity) {
        return entity.getHealth();
    }

    @Override
    protected float getEntityMaxValue(EntityLivingBase entity) {
        return entity.getMaxHealth();
    }

    @Override
    protected String getStatName() {
        return "HP";
    }
}
