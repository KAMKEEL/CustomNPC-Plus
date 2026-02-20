package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.AbilityController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.LogWriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface IAbilityCondition {
    /**
     * Check if this condition is met.
     *
     * @param caster The entity that would use the ability (NPC or Player)
     * @param target The current target (may be null for self-targeting abilities or players)
     * @return true if the condition is satisfied
     */
    boolean check(EntityLivingBase caster, EntityLivingBase target);

    UserType getUserType();

    String getTypeId();

    List<ConditionField<?>> getFields();

    default NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("userType", getUserType().ordinal());
        nbt.setString("type", getTypeId());
        for (ConditionField<?> field : getFields()) {
            field.writeNBT(nbt);
        }
        return nbt;
    }

    default void readNBT(NBTTagCompound nbt) {
        for (ConditionField<?> field : getFields()) {
            field.readNBT(nbt);
        }
    }

    static IAbilityCondition fromNBT(NBTTagCompound nbt) {
        String typeId = nbt.getString("type");
        Supplier<IAbilityCondition> factory = AbilityController.Instance.getConditionType(typeId);
        if (factory == null) {
            LogWriter.info("AbilityController: Unknown condition type: " + typeId);
            return null;
        }
        IAbilityCondition condition = factory.get();
        condition.readNBT(nbt);

        return condition;
    }

    class ConditionHPAbove implements IAbilityCondition {
        ConditionField<Float> threshold = ConditionField.floatField("threshold", "threshold", 0.5f);

        public ConditionHPAbove() {
        }

        @Override
        public boolean check(EntityLivingBase caster, EntityLivingBase target) {
            return caster.getHealth() / caster.getMaxHealth() > threshold.getValue();
        }

        @Override
        public UserType getUserType() {
            return UserType.BOTH;
        }

        @Override
        public String getTypeId() {
            return "hp_above";
        }

        public float getThreshold() {
            return threshold.getValue();
        }

        public void setThreshold(float threshold) {
            this.threshold.setValue(threshold);
        }

        @Override
        public List<ConditionField<?>> getFields() {
            return Arrays.asList(
                threshold
            );
        }
    }
}
