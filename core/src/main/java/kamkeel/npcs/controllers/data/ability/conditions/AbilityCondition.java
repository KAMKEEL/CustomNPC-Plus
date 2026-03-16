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
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import noppes.npcs.LogWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbilityCondition {
    public static final int MAX_CONDITIONS = 5;

    protected String typeId = "";
    protected String name = "";
    protected UserType userType = UserType.BOTH;
    protected ConditionFilter conditionFilter = ConditionFilter.CASTER;

    public boolean check(IEntityLivingBase caster, IEntityLivingBase target) {
        switch (getFilter()) {
            case CASTER:
                return checkEntity(caster);
            case TARGET:
                return target != null && checkEntity(target);
            case BOTH:
                return checkEntity(caster) && (target != null && checkEntity(target));
            default:
                return checkEntity(caster);
        }
    }

    protected abstract boolean checkEntity(IEntityLivingBase IEntity);

    public boolean requiresTarget() {
        return false;
    }

    /**
     * Returns true if all required fields are filled in.
     * Used to disable the Done button in SubGuiConditionEdit when incomplete.
     */
    public boolean isConfigured() {
        return true;
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

    @ClientOnly
    public abstract void getConditionDefinitions(List<FieldDef> defs);

    /**
     * Returns a human-readable summary of this condition's IConfiguration.
     * Used for tooltips when hovering over condition buttons.
     */
    @ClientOnly
    public abstract String getConditionSummary();

    @ClientOnly
    public final List<FieldDef> getAllDefinitions() {
        List<FieldDef> defs = new ArrayList<>();

        defs.add(FieldDef.labelField("ability.validFor", () ->
            "\u00A7e" + PlatformServiceHolder.get().translateToLocal("ability.userType." + getUserType().name())));
        defs.add(FieldDef.enumField("condition.filter", ConditionFilter.class, this::getFilter, this::setFilter));
        getConditionDefinitions(defs);
        return defs;
    }

    public final INbt writeNBT() {
        INbt nbt = new INbt();
        nbt.setString("typeId", getTypeId());
        nbt.setString("name", getName());
        nbt.setInteger("userType", getUserType().ordinal());
        nbt.setInteger("filter", getFilter().ordinal());
        writeTypeNBT(nbt);
        return nbt;
    }

    public abstract void writeTypeNBT(INbt nbt);

    public abstract void readTypeNBT(INbt nbt);

    public final void readNBT(INbt nbt) {
        typeId = nbt.getString("typeId");
        name = nbt.getString("name");
        userType = UserType.fromOrdinal(nbt.getInteger("userType"));
        conditionFilter = ConditionFilter.fromOrdinal(nbt.getInteger("filter"));
        readTypeNBT(nbt);
    }

    public static AbilityCondition fromNBT(INbt nbt) {
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
