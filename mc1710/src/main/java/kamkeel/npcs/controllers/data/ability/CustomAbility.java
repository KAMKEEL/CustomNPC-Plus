package kamkeel.npcs.controllers.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
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

    // Telegraph dimension fields
    private float tRadius = 5.0f;
    private float tInnerRadius = 0.0f;
    private float tLength = 5.0f;
    private float tWidth = 2.0f;
    private float tAngle = 45.0f;

    // Telegraph timing
    private int telegraphActiveTicks = 0;
    private boolean syncTelegraphWithDuration = true;

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

    @Override
    public boolean hasOwnTelegraphControls() {
        return true;
    }

    // ==================== TELEGRAPH OVERRIDES ====================

    @Override
    public boolean keepTelegraphDuringActive() {
        return getResolvedTelegraphActiveTicks() > 0;
    }

    /**
     * Returns the effective telegraph active ticks: synced from duration or manual value.
     */
    public int getResolvedTelegraphActiveTicks() {
        if (syncTelegraphWithDuration) {
            return durationTicks;
        }
        return telegraphActiveTicks;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!isShowTelegraph() || getTelegraphType() == TelegraphType.NONE) {
            return null;
        }

        Telegraph telegraph;
        double x, y, z;
        float yaw = caster.rotationYaw;
        TelegraphType tType = getTelegraphType();

        boolean positionAtCaster = targetingMode == TargetingMode.AOE_SELF ||
            targetingMode == TargetingMode.SELF ||
            tType == TelegraphType.LINE ||
            tType == TelegraphType.CONE;

        if (positionAtCaster) {
            x = caster.posX;
            y = findGroundLevel(caster.worldObj, caster.posX, caster.posY, caster.posZ);
            z = caster.posZ;
        } else if (target != null) {
            x = target.posX;
            y = findGroundLevel(caster.worldObj, target.posX, target.posY, target.posZ);
            z = target.posZ;
        } else {
            x = caster.posX;
            y = findGroundLevel(caster.worldObj, caster.posX, caster.posY, caster.posZ);
            z = caster.posZ;
        }

        switch (tType) {
            case CIRCLE:
                telegraph = Telegraph.circle(tRadius);
                break;
            case RING:
                telegraph = Telegraph.ring(tRadius, tInnerRadius);
                break;
            case LINE:
                telegraph = Telegraph.line(tLength, tWidth);
                break;
            case CONE:
                telegraph = Telegraph.cone(tLength, tAngle, tInnerRadius);
                break;
            case POINT:
                telegraph = new Telegraph("", TelegraphType.POINT);
                break;
            case SQUARE:
                telegraph = Telegraph.square(tRadius);
                break;
            default:
                return null;
        }

        int activeTicks = getResolvedTelegraphActiveTicks();
        int totalDuration = windUpTicks + activeTicks;
        telegraph.setDurationTicks(Math.max(1, totalDuration));
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        if (windUpTicks > 0) {
            telegraph.setWarningStartTick(windUpTicks);
        } else {
            telegraph.setWarningStartTick(0);
        }
        telegraph.setHeightOffset(telegraphHeightOffset);

        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        instance.setCasterEntityId(caster.getEntityId());

        if (positionAtCaster) {
            instance.setEntityIdToFollow(caster.getEntityId());
            if (tType == TelegraphType.LINE || tType == TelegraphType.CONE) {
                if (!isRotationLockedDuringWindup()) {
                    if (target != null) {
                        instance.setTargetEntityId(target.getEntityId());
                    } else {
                        instance.setTrackFollowedEntityYaw(true);
                    }
                }
            }
        } else if (target != null) {
            instance.setEntityIdToFollow(target.getEntityId());
        }

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return tRadius;
    }

    @Override
    public float getTelegraphInnerRadius() {
        return tInnerRadius;
    }

    @Override
    public float getTelegraphLength() {
        return tLength;
    }

    @Override
    public float getTelegraphWidth() {
        return tWidth;
    }

    @Override
    public float getTelegraphAngle() {
        return tAngle;
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

    public float getTRadius() {
        return tRadius;
    }

    public void setTRadius(float radius) {
        this.tRadius = Math.max(0, radius);
    }

    public float getTInnerRadius() {
        return tInnerRadius;
    }

    public void setTInnerRadius(float innerRadius) {
        this.tInnerRadius = Math.max(0, innerRadius);
    }

    public float getTLength() {
        return tLength;
    }

    public void setTLength(float length) {
        this.tLength = Math.max(0, length);
    }

    public float getTWidth() {
        return tWidth;
    }

    public void setTWidth(float width) {
        this.tWidth = Math.max(0, width);
    }

    public float getTAngle() {
        return tAngle;
    }

    public void setTAngle(float angle) {
        this.tAngle = Math.max(0, angle);
    }

    public int getTelegraphActiveTicks() {
        return telegraphActiveTicks;
    }

    public void setTelegraphActiveTicks(int ticks) {
        this.telegraphActiveTicks = Math.max(0, ticks);
    }

    public boolean isSyncTelegraphWithDuration() {
        return syncTelegraphWithDuration;
    }

    public void setSyncTelegraphWithDuration(boolean sync) {
        this.syncTelegraphWithDuration = sync;
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
    public void setTelegraphRadius(float radius) {
        this.tRadius = Math.max(0, radius);
    }

    @Override
    public void setTelegraphInnerRadius(float innerRadius) {
        this.tInnerRadius = Math.max(0, innerRadius);
    }

    @Override
    public void setTelegraphLength(float length) {
        this.tLength = Math.max(0, length);
    }

    @Override
    public void setTelegraphWidth(float width) {
        this.tWidth = Math.max(0, width);
    }

    @Override
    public void setTelegraphAngle(float angle) {
        this.tAngle = Math.max(0, angle);
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
        nbt.setFloat("tRadius", tRadius);
        nbt.setFloat("tInnerRadius", tInnerRadius);
        nbt.setFloat("tLength", tLength);
        nbt.setFloat("tWidth", tWidth);
        nbt.setFloat("tAngle", tAngle);
        nbt.setInteger("telegraphActiveTicks", telegraphActiveTicks);
        nbt.setBoolean("syncTelegraphWithDuration", syncTelegraphWithDuration);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = Math.max(0, nbt.getInteger("durationTicks"));
        this.tRadius = nbt.hasKey("tRadius") ? nbt.getFloat("tRadius") : 5.0f;
        this.tInnerRadius = nbt.hasKey("tInnerRadius") ? nbt.getFloat("tInnerRadius") : 0.0f;
        this.tLength = nbt.hasKey("tLength") ? nbt.getFloat("tLength") : 5.0f;
        this.tWidth = nbt.hasKey("tWidth") ? nbt.getFloat("tWidth") : 2.0f;
        this.tAngle = nbt.hasKey("tAngle") ? nbt.getFloat("tAngle") : 45.0f;
        this.telegraphActiveTicks = nbt.hasKey("telegraphActiveTicks") ? nbt.getInteger("telegraphActiveTicks") : 0;
        this.syncTelegraphWithDuration = nbt.hasKey("syncTelegraphWithDuration") ? nbt.getBoolean("syncTelegraphWithDuration") : true;
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        // User Type (NPC / Player / Both)
        defs.add(FieldDef.enumField("ability.allowedBy", UserType.class,
            this::getAllowedBy, this::setAllowedBy));

        // Targeting Mode
        defs.add(FieldDef.enumField("ability.targetingMode", TargetingMode.class,
            this::getTargetingMode, this::setTargetingMode));

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

        // ── Telegraph Controls (Effects tab) ─────────────────────────
        defs.add(FieldDef.section("ability.section.telegraph").tab("Effects"));
        defs.add(FieldDef.enumField("ability.telegraphShape", TelegraphType.class,
            this::getTelegraphType, this::setTelegraphType).tab("Effects")
            .hover("ability.hover.telegraphShape"));
        defs.add(FieldDef.boolField("ability.showTelegraph", this::isShowTelegraph, this::setShowTelegraph)
            .tab("Effects").visibleWhen(this::hasTelegraphShape)
            .hover("ability.hover.showTelegraph"));
        defs.add(FieldDef.colorSubGui("ability.windUpColor", this::getWindUpColor, this::setWindUpColor)
            .tab("Effects").visibleWhen(this::isTelegraphVisible)
            .hover("ability.hover.windUpColor"));
        defs.add(FieldDef.colorSubGui("ability.activeColor", this::getActiveColor, this::setActiveColor)
            .tab("Effects").visibleWhen(this::isTelegraphVisible)
            .hover("ability.hover.activeColor"));
        defs.add(FieldDef.floatField("ability.telegraphHeight", this::getTelegraphHeightOffset, this::setTelegraphHeightOffset)
            .tab("Effects").visibleWhen(this::isTelegraphVisible)
            .hover("ability.hover.telegraphHeight"));

        // Telegraph active duration with sync toggle (mirrors windup sync pattern)
        defs.add(FieldDef.row(
            FieldDef.intField("ability.telegraphActiveTicks", this::getTelegraphActiveTicks, this::setTelegraphActiveTicks).range(0, 6000)
                .hover("ability.hover.telegraphActiveTicks"),
            FieldDef.boolField("ability.syncTelegraph", this::isSyncTelegraphWithDuration, this::setSyncTelegraphWithDuration)
                .hover("ability.hover.syncTelegraph")
        ).tab("Effects").visibleWhen(() -> isTelegraphVisible() && !isSyncTelegraphWithDuration()));
        defs.add(FieldDef.row(
            FieldDef.labelField("ability.telegraphActiveTicks", () -> getResolvedTelegraphActiveTicks() + "t"),
            FieldDef.boolField("ability.syncTelegraph", this::isSyncTelegraphWithDuration, this::setSyncTelegraphWithDuration)
                .hover("ability.hover.syncTelegraph")
        ).tab("Effects").visibleWhen(() -> isTelegraphVisible() && isSyncTelegraphWithDuration()));

        // Shape-specific dimension fields
        defs.add(FieldDef.floatField("ability.telegraphRadius", this::getTRadius, this::setTRadius)
            .tab("Effects").visibleWhen(() -> isTelegraphVisible() && needsRadius())
            .hover("ability.hover.telegraphRadius"));
        defs.add(FieldDef.floatField("ability.telegraphInnerRadius", this::getTInnerRadius, this::setTInnerRadius)
            .tab("Effects").visibleWhen(() -> isTelegraphVisible() && needsInnerRadius())
            .hover("ability.hover.telegraphInnerRadius"));
        defs.add(FieldDef.floatField("ability.telegraphLength", this::getTLength, this::setTLength)
            .tab("Effects").visibleWhen(() -> isTelegraphVisible() && needsLength())
            .hover("ability.hover.telegraphLength"));
        defs.add(FieldDef.floatField("ability.telegraphWidth", this::getTWidth, this::setTWidth)
            .tab("Effects").visibleWhen(() -> isTelegraphVisible() && needsWidth())
            .hover("ability.hover.telegraphWidth"));
        defs.add(FieldDef.floatField("ability.telegraphAngle", this::getTAngle, this::setTAngle)
            .tab("Effects").visibleWhen(() -> isTelegraphVisible() && needsAngle())
            .hover("ability.hover.telegraphAngle"));
    }

    // ==================== TELEGRAPH VISIBILITY HELPERS ====================

    private boolean hasTelegraphShape() {
        return getTelegraphType() != TelegraphType.NONE;
    }

    private boolean isTelegraphVisible() {
        return hasTelegraphShape() && isShowTelegraph();
    }

    private boolean needsRadius() {
        TelegraphType t = getTelegraphType();
        return t == TelegraphType.CIRCLE || t == TelegraphType.RING || t == TelegraphType.SQUARE;
    }

    private boolean needsInnerRadius() {
        TelegraphType t = getTelegraphType();
        return t == TelegraphType.RING || t == TelegraphType.CONE;
    }

    private boolean needsLength() {
        TelegraphType t = getTelegraphType();
        return t == TelegraphType.LINE || t == TelegraphType.CONE;
    }

    private boolean needsWidth() {
        return getTelegraphType() == TelegraphType.LINE;
    }

    private boolean needsAngle() {
        return getTelegraphType() == TelegraphType.CONE;
    }
}
