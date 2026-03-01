package kamkeel.npcs.controllers.data.ability;

import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.ICustomAbility;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * A blank-slate ability that does nothing by itself.
 * All behavior is driven by script event hooks (abilityStart, abilityTick, abilityComplete,
 * abilityToggle, abilityToggleTick).
 * <p>
 * Supports two modes based on toggle configuration:
 * <ul>
 *   <li><b>Instant mode</b> (toggleStates = 0): Uses standard execution lifecycle with
 *       configurable duration. Script hooks: abilityStart, abilityTick, abilityComplete.</li>
 *   <li><b>Toggle mode</b> (toggleStates >= 1): Uses toggle lifecycle.
 *       Script hooks: abilityToggle, abilityToggleTick.</li>
 * </ul>
 */
public class CustomAbility extends Ability implements ICustomAbility {

    private int durationTicks = 20;

    public CustomAbility() {
        this.typeId = "ability.cnpc.custom";
        this.name = "Custom";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = LockMode.NO;
        this.cooldownTicks = 0;
        this.windUpTicks = 0;
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    // ==================== EXECUTION ====================

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (durationTicks <= 0) {
            signalCompletion();
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (durationTicks > 0 && tick >= durationTicks) {
            signalCompletion();
        }
    }

    // ==================== PROPERTIES ====================

    @Override
    public boolean hasDamage() {
        return false;
    }

    @Override
    public boolean isConcurrentCapable() {
        return true;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return false;
    }

    @Override
    public boolean allowFreeOnCast() {
        return true;
    }

    // ==================== FIELDS ====================

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void setDurationTicks(int ticks) {
        this.durationTicks = Math.max(0, ticks);
    }

    // ==================== ICustomAbility ====================

    @Override
    public int getTelegraphShapeType() {
        return getTelegraphType().ordinal();
    }

    @Override
    public void setTelegraphShapeType(int type) {
        TelegraphType[] values = TelegraphType.values();
        if (type >= 0 && type < values.length) {
            setTelegraphType(values[type]);
        }
    }

    @Override
    public int getTargetingModeType() {
        return getTargetingMode().ordinal();
    }

    @Override
    public void setTargetingModeType(int type) {
        TargetingMode[] values = TargetingMode.values();
        if (type >= 0 && type < values.length) {
            setTargetingMode(values[type]);
        }
    }

    // ==================== NBT ====================

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.getInteger("durationTicks");
    }

    // ==================== GUI ====================

    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        // User Type (NPC / Player / Both)
        defs.add(FieldDef.enumField("ability.allowedBy", UserType.class,
            this::getAllowedBy, this::setAllowedBy));

        // Targeting Mode
        defs.add(FieldDef.enumField("ability.targetingMode", TargetingMode.class,
            this::getTargetingMode, this::setTargetingMode));

        // Telegraph Shape
        defs.add(FieldDef.enumField("ability.telegraphShape", TelegraphType.class,
            this::getTelegraphType, this::setTelegraphType));

        // Toggle section
        defs.add(FieldDef.section("ability.section.toggle"));
        defs.add(FieldDef.boolField("ability.toggleEnabled",
            this::isToggleable, enabled -> setToggleStates(enabled ? Math.max(1, getToggleStates()) : 0)));
        defs.add(FieldDef.intField("ability.toggleStates", this::getToggleStates, this::setToggleStates)
            .range(1, 10)
            .visibleWhen(this::isToggleable));
        defs.add(FieldDef.boolField("ability.hasActiveToggle", this::hasActiveToggle, this::setHasActiveToggle)
            .visibleWhen(this::isToggleable));

        // Duration (instant mode only)
        defs.add(FieldDef.section("ability.section.execution")
            .visibleWhen(() -> !this.isToggleable()));
        defs.add(FieldDef.intField("ability.duration", this::getDurationTicks, this::setDurationTicks)
            .range(0, 6000)
            .visibleWhen(() -> !this.isToggleable()));
    }
}
