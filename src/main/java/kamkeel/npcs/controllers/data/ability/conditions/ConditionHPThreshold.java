package kamkeel.npcs.controllers.data.ability.conditions;

import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Collections;
import java.util.List;

public class ConditionHPThreshold extends AbilityCondition{
    private float threshold = 0.5f; // 50%
    private ThresholdType thresholdType = ThresholdType.ABOVE;

    public enum ThresholdType {
        ABOVE {
            @Override
            public boolean test(float value, float threshold) {
                return value > threshold;
            }
        },
        BELOW {
            @Override
            public boolean test(float value, float threshold) {
                return value < threshold;
            }
        };

        public abstract boolean test(float value, float threshold);

        public static ThresholdType fromOrdinal(int ordinal) {
            ThresholdType[] values = values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
            return ABOVE;
        }
    }

    public ConditionHPThreshold() {
        this.typeId = "condition.cnpc.hp_threshold";
        this.name = "Health Threshold";
        this.userType = UserType.NPC_ONLY;
    }

    @Override
    public boolean check(EntityLivingBase caster, EntityLivingBase target) {
        float casterHP = caster.getHealth() / caster.getMaxHealth();
        float targetHP = target.getHealth() / target.getMaxHealth();


        return compare(casterHP, targetHP);
    }

    public boolean compare(float casterHp, float targetHp) {
        boolean casterCheck = thresholdType.test(casterHp, threshold);
        boolean targetCheck = thresholdType.test(targetHp, threshold);

        switch (getUserType()) {
            case BOTH:
                return casterCheck && targetCheck;
            case PLAYER_ONLY:
                return casterCheck;
            default:
                return targetCheck;
        }
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = Math.max(0, threshold);
    }

    public ThresholdType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(ThresholdType thresholdType) {
        this.thresholdType = thresholdType;
    }

    @Override
    public List<FieldDef> getAbilityDefinitions(List<FieldDef> defs) {
        return Collections.emptyList();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("threshold", threshold);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        threshold = nbt.getFloat("threshold");
    }
}
