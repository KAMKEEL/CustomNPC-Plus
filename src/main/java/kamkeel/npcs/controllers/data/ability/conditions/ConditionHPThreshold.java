package kamkeel.npcs.controllers.data.ability.conditions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

public class ConditionHPThreshold extends AbilityCondition{
    private float threshold = 0.5f; // 50%
    private boolean percent = true;
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
        },
        EQUAL {
            @Override
            public boolean test(float value, float threshold) {
                return value == threshold;
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

        @Override
        public String toString() {
            switch (this) {
                case ABOVE:
                    return "condition.hp_above";
                case BELOW:
                    return "condition.hp_below";
                case EQUAL:
                    return "condition.hp_equal";
                default:
                    return name();
            }
        }
    }

    public ConditionHPThreshold() {
        this.typeId = "condition.cnpc.hp_threshold";
        this.name = "condition.hp_threshold";
    }

    @Override
    public boolean check(EntityLivingBase caster, EntityLivingBase target) {
        float casterHP = isPercent() ? caster.getHealth() / caster.getMaxHealth() : caster.getHealth();
        float targetHP = isPercent() ? target.getHealth() / target.getMaxHealth() : target.getHealth();


        return compare(casterHP, targetHP);
    }

    public boolean compare(float casterHp, float targetHp) {
        boolean casterCheck = thresholdType.test(casterHp, threshold);
        boolean targetCheck = thresholdType.test(targetHp, threshold);

        switch (getFilter()) {
            case BOTH:
                return casterCheck && targetCheck;
            case CASTER:
                return casterCheck;
            default:
                return targetCheck;
        }
    }

    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("condition.threshold", this::getThreshold, this::setThreshold).min(0));
        defs.add(FieldDef.boolField("condition.percent", this::isPercent, this::setIsPercent));
        defs.add(FieldDef.enumField("condition.threshold_type", ThresholdType.class,
            this::getThresholdType, this::setThresholdType));
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("threshold", threshold);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        threshold = nbt.getFloat("threshold");
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

    public boolean isPercent() {
        return percent;
    }

    public void setIsPercent(boolean percent) {
        this.percent = percent;
    }
}
