package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.api.ability.type.IAbilityDash;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Dash ability: Quick evasive sidestep with NO telegraph.
 * Defensive repositioning move to evade attacks.
 */
public class AbilityDash extends Ability implements IAbilityDash {

    /**
     * Dash behavior mode.
     */
    public enum DashMode {
        AGGRESSIVE,
        DEFENSIVE;

        @Override
        public String toString() {
            switch (this) {
                case AGGRESSIVE: return "ability.dash.aggressive";
                case DEFENSIVE: return "ability.dash.defensive";
                default: return name();
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

    // Runtime state
    private transient Vec3 dashDirection;
    private transient double startX, startY, startZ;
    private transient double prevTickX, prevTickZ;
    private transient DashDirection chosenDirection;
    private transient int maxActiveTicks;

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
    public boolean hasAbilityMovement() {
        return true; // This ability moves the NPC
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        startX = caster.posX;
        startY = caster.posY;
        startZ = caster.posZ;
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;

        // Safety timeout: expected ticks + generous buffer to prevent infinite dash
        maxActiveTicks = dashSpeed > 0 ? (int)(dashDistance / dashSpeed) + 10 : 10;

        // Choose random direction based on mode
        DashDirection[] directions = dashMode == DashMode.AGGRESSIVE
            ? AGGRESSIVE_DIRECTIONS
            : DEFENSIVE_DIRECTIONS;
        chosenDirection = directions[RANDOM.nextInt(directions.length)];

        // NPC: dash direction is relative to aggro target facing
        // Player: dash direction is relative to look direction
        float baseYaw;
        if (!isPlayerCaster(caster) && target != null) {
            double dx = target.posX - caster.posX;
            double dz = target.posZ - caster.posZ;
            baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            baseYaw = caster.rotationYaw;
        }

        // Apply direction offset
        float dashYaw = baseYaw + chosenDirection.getAngleOffset();
        float yawRad = (float) Math.toRadians(dashYaw);

        dashDirection = Vec3.createVectorHelper(
            -Math.sin(yawRad),
            0,
            Math.cos(yawRad)
        );

        // Small upward impulse for a skip/hop arc (gravity handles the descent)
        caster.motionY = 0.2;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Safety timeout or missing state: force-complete to prevent stuck NPC
        if (!isPreview() && (dashDirection == null || tick > maxActiveTicks)) {
            stopDash(caster);
            signalCompletion();
            return;
        }

        if (dashDirection == null) {
            signalCompletion();
            return;
        }

        // Stall detection: if entity hasn't moved since last tick, it's stuck against a wall
        if (!isPreview() && tick > 1) {
            double dx = caster.posX - prevTickX;
            double dz = caster.posZ - prevTickZ;
            if (dx * dx + dz * dz < 0.0001) {
                stopDash(caster);
                signalCompletion();
                return;
            }
        }
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;

        // Check if reached max distance
        double travelDx = caster.posX - startX;
        double travelDz = caster.posZ - startZ;
        if (travelDx * travelDx + travelDz * travelDz >= (double) dashDistance * dashDistance) {
            stopDash(caster);
            signalCompletion();
            return;
        }

        // Block detection (skip in preview - no real world collision)
        if (!isPreview() && isDashBlocked(caster)) {
            stopDash(caster);
            signalCompletion();
            return;
        }

        // Move caster (motionY left to gravity for skip arc)
        caster.motionX = dashDirection.xCoord * dashSpeed;
        caster.motionZ = dashDirection.zCoord * dashSpeed;
        if (!isPreview()) {
            caster.velocityChanged = true;

            // Trail particles
            world.spawnParticle("smoke", caster.posX, caster.posY + 0.5, caster.posZ, 0, 0, 0);
        }
    }

    private void stopDash(EntityLivingBase caster) {
        caster.motionX = 0;
        caster.motionZ = 0;
        if (!isPreview()) {
            caster.velocityChanged = true;
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        stopDash(caster);
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        stopDash(caster);
    }

    @Override
    public void cleanup() {
        dashDirection = null;
        chosenDirection = null;
        maxActiveTicks = 0;
    }

    private boolean isDashBlocked(EntityLivingBase caster) {
        if (dashDirection == null) return true;
        double nextX = dashDirection.xCoord * dashSpeed;
        double nextZ = dashDirection.zCoord * dashSpeed;
        AxisAlignedBB nextBox = caster.boundingBox.copy().offset(nextX, 0, nextZ);
        double stepThreshold = nextBox.minY + Math.max(caster.stepHeight, 0.5);

        int x1 = (int) Math.floor(nextBox.minX);
        int x2 = (int) Math.floor(nextBox.maxX + 1.0);
        int y1 = (int) Math.floor(nextBox.minY) - 1;
        int y2 = (int) Math.floor(nextBox.maxY + 1.0);
        int z1 = (int) Math.floor(nextBox.minZ);
        int z2 = (int) Math.floor(nextBox.maxZ + 1.0);

        java.util.ArrayList<AxisAlignedBB> collisionBoxes = new java.util.ArrayList<>();
        for (int bx = x1; bx < x2; bx++) {
            for (int bz = z1; bz < z2; bz++) {
                for (int by = y1; by < y2; by++) {
                    net.minecraft.block.Block block = caster.worldObj.getBlock(bx, by, bz);
                    if (!block.getMaterial().blocksMovement()) continue;
                    collisionBoxes.clear();
                    block.addCollisionBoxesToList(caster.worldObj, bx, by, bz, nextBox, collisionBoxes, caster);
                    for (AxisAlignedBB box : collisionBoxes) {
                        if (box.maxY > stepThreshold) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
        this.dashDistance = nbt.hasKey("dashDistance") ? nbt.getFloat("dashDistance") : 4.0f;
        this.dashSpeed = nbt.hasKey("dashSpeed") ? nbt.getFloat("dashSpeed") : 0.5f;
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
