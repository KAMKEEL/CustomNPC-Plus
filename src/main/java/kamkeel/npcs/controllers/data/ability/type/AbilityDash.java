package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.ability.type.IAbilityDash;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Dash ability: Quick evasive sidestep with NO telegraph.
 * Extends AbilityMovement for shared direction locking, stall detection, and velocity application.
 */
public class AbilityDash extends AbilityMovement implements IAbilityDash {

    /**
     * Dash behavior mode.
     */
    public enum DashMode {
        AGGRESSIVE,
        DEFENSIVE,
        DIRECTIONAL;

        @Override
        public String toString() {
            switch (this) {
                case AGGRESSIVE:
                    return "ability.dash.aggressive";
                case DEFENSIVE:
                    return "ability.dash.defensive";
                case DIRECTIONAL:
                    return "ability.dash.directional";
                default:
                    return name();
            }
        }
    }

    /**
     * Specific dash direction.
     */
    public enum DashDirection {
        FORWARD(0),
        DIAGONAL_FORWARD_LEFT(45),
        DIAGONAL_FORWARD_RIGHT(-45),
        LEFT(90),
        RIGHT(-90),
        DIAGONAL_BACK_LEFT(135),
        DIAGONAL_BACK_RIGHT(-135),
        BACK(180),
        CUSTOM(0);

        private final float angleOffset;

        DashDirection(float angleOffset) {
            this.angleOffset = angleOffset;
        }

        public float getAngleOffset() {
            return angleOffset;
        }

        @Override
        public String toString() {
            switch (this) {
                case FORWARD:
                    return "ability.dash.forward";
                case DIAGONAL_FORWARD_LEFT:
                    return "ability.dash.diagonalForwardLeft";
                case DIAGONAL_FORWARD_RIGHT:
                    return "ability.dash.diagonalForwardRight";
                case LEFT:
                    return "ability.dash.left";
                case RIGHT:
                    return "ability.dash.right";
                case DIAGONAL_BACK_LEFT:
                    return "ability.dash.diagonalBackLeft";
                case DIAGONAL_BACK_RIGHT:
                    return "ability.dash.diagonalBackRight";
                case BACK:
                    return "ability.dash.back";
                case CUSTOM:
                    return "ability.dash.custom";
                default:
                    return name();
            }
        }

        public static DashDirection fromOrdinal(int ordinal) {
            DashDirection[] values = values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
            return DashDirection.FORWARD;
        }
    }

    private static final Random RANDOM = new Random();

    private static final DashDirection[] AGGRESSIVE_DIRECTIONS = {
        DashDirection.FORWARD,
        DashDirection.DIAGONAL_FORWARD_LEFT,
        DashDirection.DIAGONAL_FORWARD_RIGHT
    };

    private static final DashDirection[] DEFENSIVE_DIRECTIONS = {
        DashDirection.LEFT,
        DashDirection.RIGHT,
        DashDirection.BACK,
        DashDirection.DIAGONAL_BACK_LEFT,
        DashDirection.DIAGONAL_BACK_RIGHT
    };

    // Type-specific parameters
    private DashMode dashMode = DashMode.DEFENSIVE;
    private DashDirection dashDirection = DashDirection.FORWARD;
    private float dashDistance = 4.0f;
    private float dashSpeed = 0.5f;
    private float dashAngle = 0.0f;

    // Type-specific runtime state
    private transient DashDirection chosenDirection;
    private transient double preDashMotionX;
    private transient double preDashMotionZ;

    public AbilityDash() {
        this.typeId = "ability.cnpc.dash";
        this.name = "Dash";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 0.0f;
        this.lockMovement = LockMode.NO;
        this.cooldownTicks = 0;
        this.windUpTicks = 5;
        // No telegraph for dash - it's a quick evasive move
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpSound = "mob.bat.takeoff";
    }

    @Override
    public boolean hasDamage() {
        return false;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        preDashMotionX = caster.motionX;
        preDashMotionZ = caster.motionZ;
        initMovement(caster, dashDistance, dashSpeed);

        if (dashMode == DashMode.DIRECTIONAL) {
            // Dash straight in the caster's look direction
            chosenDirection = dashDirection;
            float baseYaw = getBaseYaw(caster, target);
            float angleOffset = chosenDirection == DashDirection.CUSTOM ? dashAngle : chosenDirection.getAngleOffset();
            float dashYaw = baseYaw + angleOffset;
            setDirectionFromYaw(dashYaw);
        } else {
            // Choose random direction based on mode
            DashDirection[] directions = dashMode == DashMode.AGGRESSIVE
                ? AGGRESSIVE_DIRECTIONS
                : DEFENSIVE_DIRECTIONS;
            chosenDirection = directions[RANDOM.nextInt(directions.length)];

            // Calculate direction: base yaw (toward target or look dir) + direction offset
            float baseYaw = getBaseYaw(caster, target);
            float dashYaw = baseYaw + chosenDirection.getAngleOffset();
            setDirectionFromYaw(dashYaw);
        }

        // Small upward impulse for a skip/hop arc (gravity handles the descent)
        caster.motionY = 0.2;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (checkTimeout(tick)) {
            finishDash(caster);
            signalCompletion();
            return;
        }

        if (movementDirection == null) {
            finishDash(caster);
            signalCompletion();
            return;
        }

        if (checkStall(caster, tick)) {
            finishDash(caster);
            signalCompletion();
            return;
        }
        updatePrevPosition(caster);

        if (getDistanceTraveledSq(caster) >= (double) dashDistance * dashDistance) {
            finishDash(caster);
            signalCompletion();
            return;
        }

        if (checkBlocked(caster, dashSpeed)) {
            finishDash(caster);
            signalCompletion();
            return;
        }

        // Move caster (motionY left to gravity for skip arc).
        // Directional mode layers the dash over incoming movement so momentum
        // entering/exiting the dash stays fluid.
        if (dashMode == DashMode.DIRECTIONAL) {
            applyHorizontalMomentum(caster,
                preDashMotionX + movementDirection.xCoord * dashSpeed,
                preDashMotionZ + movementDirection.zCoord * dashSpeed);
        } else {
            applyVelocity(caster, dashSpeed);
        }

        if (!isPreview()) {
            // Trail particles
            caster.worldObj.spawnParticle("smoke", caster.posX, caster.posY + 0.5, caster.posZ, 0, 0, 0);
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        finishDash(caster);
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        finishDash(caster);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        chosenDirection = null;
        preDashMotionX = 0;
        preDashMotionZ = 0;
    }

    @Override
    public void resetForBurst() {
        super.resetForBurst();
        chosenDirection = null;
        preDashMotionX = 0;
        preDashMotionZ = 0;
    }

    @Override
    public int getMaxPreviewDuration() {
        return (int) Math.ceil(dashDistance / dashSpeed) + 5;
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for dash
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setString("dashMode", dashMode.name());
        nbt.setString("dashDirection", dashDirection.name());
        nbt.setFloat("dashDistance", dashDistance);
        nbt.setFloat("dashSpeed", dashSpeed);
        nbt.setFloat("dashAngle", dashAngle);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        try {
            this.dashMode = DashMode.valueOf(nbt.getString("dashMode"));
        } catch (Exception e) {
            this.dashMode = DashMode.DEFENSIVE;
        }

        try {
            this.dashDirection = DashDirection.valueOf(nbt.getString("dashDirection"));
        } catch (Exception e) {
            this.dashDirection = DashDirection.FORWARD;
        }

        this.dashDistance = nbt.hasKey("dashDistance") ? nbt.getFloat("dashDistance") : 4.0f;
        this.dashSpeed = nbt.hasKey("dashSpeed") ? Math.max(0.01f, nbt.getFloat("dashSpeed")) : 0.5f;
        this.dashAngle = nbt.hasKey("dashAngle") ? nbt.getFloat("dashAngle") : 0.0f;
    }

    // Getters & Setters
    public DashMode getDashModeEnum() {
        return dashMode;
    }

    public void setDashModeEnum(DashMode dashMode) {
        this.dashMode = dashMode;
    }

    @Override
    public int getDashMode() {
        return dashMode.ordinal();
    }

    @Override
    public void setDashMode(int mode) {
        DashMode[] values = DashMode.values();
        this.dashMode = mode >= 0 && mode < values.length ? values[mode] : DashMode.AGGRESSIVE;
    }

    public float getDashDistance() {
        return dashDistance;
    }

    public void setDashDistance(float dashDistance) {
        this.dashDistance = dashDistance;
    }

    public float getDashSpeed() {
        return dashSpeed;
    }

    public void setDashSpeed(float dashSpeed) {
        this.dashSpeed = dashSpeed;
    }

    public float getDashAngle() {
        return dashAngle;
    }

    public void setDashAngle(float dashAngle) {
        this.dashAngle = dashAngle;
    }

    public DashDirection getDashDirectionEnum() {
        return dashDirection;
    }

    public void setDashDirectionEnum(DashDirection dashDirection) {
        this.dashDirection = dashDirection;
    }

    @Override
    public int getDashDirection() {
        return dashDirection.ordinal();
    }

    @Override
    public void setDashDirection(int mode) {
        DashDirection[] values = DashDirection.values();
        this.dashDirection = mode >= 0 && mode < values.length ? values[mode] : DashDirection.FORWARD;
    }


    public DashDirection getChosenDirection() {
        return chosenDirection;
    }

    private void finishDash(EntityLivingBase caster) {
        if (dashMode == DashMode.DIRECTIONAL) {
            applyHorizontalMomentum(caster, preDashMotionX, preDashMotionZ);
        } else {
            stopMomentum(caster);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.enumField("ability.dashMode", DashMode.class, this::getDashModeEnum, this::setDashModeEnum)
                .hover("ability.hover.dashMode"),
            FieldDef.row(
                FieldDef.floatField("ability.dashDistance", this::getDashDistance, this::setDashDistance),
                FieldDef.floatField("ability.dashSpeed", this::getDashSpeed, this::setDashSpeed)
            ),
            FieldDef.row(
                FieldDef.enumField("ability.dashDirection", DashDirection.class, this::getDashDirectionEnum, this::setDashDirectionEnum),
                FieldDef.floatField("ability.dashAngle", this::getDashAngle, this::setDashAngle)
                    .min(Float.NEGATIVE_INFINITY).visibleWhen(() -> getDashDirectionEnum() == DashDirection.CUSTOM)
            ).visibleWhen(() -> this.getDashModeEnum() == DashMode.DIRECTIONAL)
        ));
    }
}
