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
 * Abstract base for float comparison-based conditions.
 * Provides shared CompareType enum, NBT serialization, and FieldDef generation.
 */
public abstract class ConditionCompare extends AbilityCondition {

    public enum CompareType {
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
                return value == threshold;
            }
        };

        public abstract boolean test(float value, float threshold);

        public static CompareType fromOrdinal(int ordinal) {
            CompareType[] values = values();
            return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : ABOVE;
        }

        @Override
        public String toString() {
            return "condition." + name().toLowerCase();
        }
    }

    protected CompareType compareType = CompareType.ABOVE;

    /**
     * Returns the value to be compared for this IEntity (e.g. level, skill level).
     * Return -1 to indicate the condition should fail immediately.
     */
    protected abstract float getEntityValue(IEntityLivingBase IEntity);

    /**
     * Returns the threshold that getEntityValue() is compared against.
     */
    protected abstract float getThreshold();

    /**
     * Appends subclass-specific FieldDefs (e.g. skill picker, level input).
     * Called before the shared compareType field is added.
     */
    @ClientOnly
    protected abstract void getExtraDefinitions(List<FieldDef> defs);

    /**
     * Subclasses write their own NBT fields here.
     */
    protected abstract void writeExtraNBT(INbt nbt);

    /**
     * Subclasses read their own NBT fields here.
     */
    protected abstract void readExtraNBT(INbt nbt);

    @Override
    protected boolean checkEntity(IEntityLivingBase IEntity) {
        float value = getEntityValue(IEntity);
        if (value < 0) return false;
        return compareType.test(value, getThreshold());
    }

    @ClientOnly
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.enumField("condition.compare_type", CompareType.class, this::getCompareType, this::setCompareType));
        getExtraDefinitions(defs);
    }

    @Override
    public void writeTypeNBT(INbt nbt) {
        nbt.setInteger("compareType", compareType.ordinal());
        writeExtraNBT(nbt);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
        compareType = CompareType.fromOrdinal(nbt.getInteger("compareType"));
        readExtraNBT(nbt);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public CompareType getCompareType() {
        return compareType;
    }

    public void setCompareType(CompareType compareType) {
        this.compareType = compareType;
    }
}
