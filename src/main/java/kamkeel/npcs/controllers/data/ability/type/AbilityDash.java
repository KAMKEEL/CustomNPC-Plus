package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
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
        DEFENSIVE;

        @Override
        public String toString() {
            switch (this) {
                case AGGRESSIVE:
                    return "ability.dash.aggressive";
                case DEFENSIVE:
                    return "ability.dash.defensive";
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
        BACK(180);

        private final float angleOffset;

        DashDirection(float angleOffset) {
            this.angleOffset = angleOffset;
        }

        public float getAngleOffset() {
            return angleOffset;
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
    private float dashDistance = 4.0f;
    private float dashSpeed = 0.5f;

    // Type-specific runtime state
    private transient DashDirection chosenDirection;

    public AbilityDash() {
        this.typeId = "ability.cnpc.dash";
        this.name = "Dash";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 0.0f;
        this.lockMovement = LockMovementType.NO;
        this.cooldownTicks = 0;
        this.windUpTicks = 5;
        // No telegraph for dash - it's a quick evasive move
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpSound = "mob.bat.takeoff";
        this.activeSound = "mob.endermen.portal";
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
        initMovement(caster, dashDistance, dashSpeed);

        // Choose random direction based on mode
        DashDirection[] directions = dashMode == DashMode.AGGRESSIVE
            ? AGGRESSIVE_DIRECTIONS
            : DEFENSIVE_DIRECTIONS;
        chosenDirection = directions[RANDOM.nextInt(directions.length)];

        // Calculate direction: base yaw (toward target or look dir) + direction offset
        float baseYaw = getBaseYaw(caster, target);
        float dashYaw = baseYaw + chosenDirection.getAngleOffset();
        setDirectionFromYaw(dashYaw);

        // Small upward impulse for a skip/hop arc (gravity handles the descent)
        caster.motionY = 0.2;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (checkTimeout(tick)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        if (movementDirection == null) {
            signalCompletion();
            return;
        }

        if (checkStall(caster, tick)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }
        updatePrevPosition(caster);

        if (getDistanceTraveledSq(caster) >= (double) dashDistance * dashDistance) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        if (checkBlocked(caster, dashSpeed)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        // Move caster (motionY left to gravity for skip arc)
        applyVelocity(caster, dashSpeed);

        if (!isPreview()) {
            // Trail particles
            caster.worldObj.spawnParticle("smoke", caster.posX, caster.posY + 0.5, caster.posZ, 0, 0, 0);
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        stopMomentum(caster);
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        stopMomentum(caster);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        chosenDirection = null;
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
        nbt.setFloat("dashDistance", dashDistance);
        nbt.setFloat("dashSpeed", dashSpeed);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        try {
            this.dashMode = DashMode.valueOf(nbt.getString("dashMode"));
        } catch (Exception e) {
            this.dashMode = DashMode.DEFENSIVE;
        }
        this.dashDistance = nbt.getFloat("dashDistance");
        this.dashSpeed = nbt.getFloat("dashSpeed");
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

    public DashDirection getChosenDirection() {
        return chosenDirection;
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
            )
        ));
    }
}
