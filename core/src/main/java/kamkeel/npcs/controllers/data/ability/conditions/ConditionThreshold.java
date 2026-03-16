package kamkeel.npcs.controllers.data.ability.conditions;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import java.util.List;

/**
 * Abstract base for threshold-based conditions (HP, Ki, etc.).
 * Provides shared percent/flat toggle, ThresholdType enum, and FieldDef generation.
 */
public abstract class ConditionThreshold extends AbilityCondition {

    public enum ThresholdType {
        ABOVE {
            @Override
            public boolean test(float value, float threshold) {
                return value >= threshold;
            }
        },
        BELOW {
            @Override
            public boolean test(float value, float threshold) {
                return value <= threshold;
            }
        },
        EQUAL {
            @Override
            public boolean test(float value, float threshold) {
                return Math.abs(value - threshold) < 0.001f;
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
                    return "condition.above";
                case BELOW:
                    return "condition.below";
                case EQUAL:
                    return "condition.equal";
                default:
                    return name();
            }
        }
    }

    protected float thresholdFlat = 100f;
    protected float thresholdPercent = 0.5f;
    protected boolean percent = true;
    protected ThresholdType thresholdType = ThresholdType.ABOVE;

    /**
     * Returns the raw stat value for the IEntity (e.g. current HP, current Ki).
     */
    protected abstract float getEntityValue(IEntityLivingBase IEntity);

    /**
     * Returns the maximum stat value for the IEntity (e.g. max HP, max Ki).
     * Used to compute the percentage ratio.
     */
    protected abstract float getEntityMaxValue(IEntityLivingBase IEntity);

    /**
     * Returns the display name for the stat being checked (e.g. "HP", "Ki").
     * Used in getConditionSummary().
     */
    protected abstract String getStatName();

    @Override
    protected boolean checkEntity(IEntityLivingBase IEntity) {
        float value;
        float threshold;
        if (percent) {
            float max = getEntityMaxValue(IEntity);
            value = max > 0 ? getEntityValue(IEntity) / max : 0;
            threshold = thresholdPercent;
        } else {
            value = getEntityValue(IEntity);
            threshold = thresholdFlat;
        }
        return thresholdType.test(value, threshold);
    }

    @ClientOnly
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("condition.threshold_flat", this::getThresholdFlat, this::setThresholdFlat)
            .min(0).visibleWhen(() -> !isPercent()));
        defs.add(FieldDef.floatField("condition.threshold_percent", this::getThresholdPercent, this::setThresholdPercent)
            .min(0).max(1).visibleWhen(this::isPercent));
        defs.add(FieldDef.boolField("condition.percent", this::isPercent, this::setIsPercent));
        defs.add(FieldDef.enumField("condition.threshold_type", ThresholdType.class,
            this::getThresholdType, this::setThresholdType));
    }

    @Override
    public void writeTypeNBT(INbt nbt) {
        nbt.setFloat("thresholdFlat", thresholdFlat);
        nbt.setFloat("thresholdPercent", thresholdPercent);
        nbt.setBoolean("percent", percent);
        nbt.setInteger("thresholdType", thresholdType.ordinal());
    }

    @Override
    public void readTypeNBT(INbt nbt) {
        thresholdFlat = nbt.getFloat("thresholdFlat");
        thresholdPercent = nbt.getFloat("thresholdPercent");
        percent = nbt.getBoolean("percent");
        thresholdType = ThresholdType.fromOrdinal(nbt.getInteger("thresholdType"));
    }

    @ClientOnly
    @Override
    public String getConditionSummary() {
        String filterLabel = PlatformServiceHolder.get().translateToLocal(getFilter().toString());
        String typeName = getStatName();
        String typeLabel = PlatformServiceHolder.get().translateToLocal(thresholdType.toString());
        String value = percent
            ? String.format("%.0f%%", thresholdPercent * 100)
            : String.format("%.0f", thresholdFlat);
        return "[" + filterLabel + "] " + typeName + " " + typeLabel + " " + value;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public float getThresholdFlat() {
        return thresholdFlat;
    }

    public void setThresholdFlat(float value) {
        this.thresholdFlat = Math.max(0, value);
    }

    public float getThresholdPercent() {
        return thresholdPercent;
    }

    public void setThresholdPercent(float value) {
        this.thresholdPercent = Math.max(0, Math.min(1, value));
    }

    public boolean isPercent() {
        return percent;
    }

    public void setIsPercent(boolean percent) {
        this.percent = percent;
    }

    public ThresholdType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(ThresholdType type) {
        this.thresholdType = type;
    }
}
