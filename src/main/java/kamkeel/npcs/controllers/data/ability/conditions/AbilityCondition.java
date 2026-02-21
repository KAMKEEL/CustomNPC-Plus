package kamkeel.npcs.controllers.data.ability.conditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbilityCondition {
    public static final int MAX_CONDITIONS = 5;

    protected String typeId = "";
    protected String name = "";
    protected UserType userType = UserType.BOTH;
    protected ConditionFilter conditionFilter = ConditionFilter.CASTER;

    /**
     * Check if this condition is met.
     *
     * @param caster The entity that would use the ability (NPC or Player)
     * @param target The current target (may be null for self-targeting abilities or players)
     * @return true if the condition is satisfied
     */
    public abstract boolean check(EntityLivingBase caster, EntityLivingBase target);

    public boolean requiresTarget() {
        return false;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getName() {
        return name;
    }

    public ConditionFilter getFilter() {
        return conditionFilter;
    }

    public void setFilter(ConditionFilter filter) {
        this.conditionFilter = filter;
    }

    @SideOnly(Side.CLIENT)
    public abstract void getConditionDefinitions(List<FieldDef> defs);

    @SideOnly(Side.CLIENT)
    public final List<FieldDef> getAllDefinitions() {
        List<FieldDef> defs = new ArrayList<>();

        defs.add(FieldDef.enumField("condition.filter", ConditionFilter.class, this::getFilter, this::setFilter));
        getConditionDefinitions(defs);
        return defs;
    }

    public final NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("typeId", getTypeId());
        nbt.setString("name", getName());
        nbt.setInteger("userType", getUserType().ordinal());
        nbt.setInteger("filter", getFilter().ordinal());
        writeTypeNBT(nbt);
        return nbt;
    }

    public abstract void writeTypeNBT(NBTTagCompound nbt);

    public abstract void readTypeNBT(NBTTagCompound nbt);

    public final void readNBT(NBTTagCompound nbt) {
        typeId = nbt.getString("typeId");
        name = nbt.getString("name");
        userType = UserType.fromOrdinal(nbt.getInteger("userType"));
        conditionFilter = ConditionFilter.fromOrdinal(nbt.getInteger("filter"));
        readTypeNBT(nbt);
    }

    public static AbilityCondition fromNBT(NBTTagCompound nbt) {
        String typeId = nbt.getString("typeId");
        Supplier<AbilityCondition> factory = AbilityController.Instance.getConditionType(typeId);
        if (factory == null) {
            LogWriter.info("AbilityController: Unknown condition type: " + typeId);
            return null;
        }
        AbilityCondition condition = factory.get();
        condition.readNBT(nbt);

        return condition;
    }
}
