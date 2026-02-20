package kamkeel.npcs.controllers.data.ability.conditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbilityCondition {
    protected String typeId = "";
    protected String name = "";
    protected UserType userType = UserType.BOTH;

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

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getName() {
        return name;
    }

    @SideOnly(Side.CLIENT)
    public abstract List<FieldDef> getAbilityDefinitions(List<FieldDef> defs);

    public final NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("userType", getUserType().ordinal());
        nbt.setString("type", getTypeId());
        nbt.setString("name", getName());
        writeTypeNBT(nbt);
        return nbt;
    }

    public abstract void writeTypeNBT(NBTTagCompound nbt);

    public abstract void readTypeNBT(NBTTagCompound nbt);

    public final void readNBT(NBTTagCompound nbt) {
        typeId = nbt.getString("type");
        name = nbt.getString("name");
        userType = UserType.fromOrdinal(nbt.getInteger("userType"));
        readTypeNBT(nbt);
    }

    public static AbilityCondition fromNBT(NBTTagCompound nbt) {
        String typeId = nbt.getString("type");
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
