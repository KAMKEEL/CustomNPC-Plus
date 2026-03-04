package kamkeel.npcs.controllers.data.ability.conditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.builder.FieldDef;

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
     * Returns the value to be compared for this entity (e.g. level, skill level).
     * Return -1 to indicate the condition should fail immediately.
     */
    protected abstract float getEntityValue(EntityLivingBase entity);

    /**
     * Returns the threshold that getEntityValue() is compared against.
     */
    protected abstract float getThreshold();

    /**
     * Appends subclass-specific FieldDefs (e.g. skill picker, level input).
     * Called before the shared compareType field is added.
     */
    @SideOnly(Side.CLIENT)
    protected abstract void getExtraDefinitions(List<FieldDef> defs);

    /**
     * Subclasses write their own NBT fields here.
     */
    protected abstract void writeExtraNBT(NBTTagCompound nbt);

    /**
     * Subclasses read their own NBT fields here.
     */
    protected abstract void readExtraNBT(NBTTagCompound nbt);

    @Override
    protected boolean checkEntity(EntityLivingBase entity) {
        float value = getEntityValue(entity);
        if (value < 0) return false;
        return compareType.test(value, getThreshold());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.enumField("condition.compare_type", CompareType.class, this::getCompareType, this::setCompareType));
        getExtraDefinitions(defs);
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("compareType", compareType.ordinal());
        writeExtraNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
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
