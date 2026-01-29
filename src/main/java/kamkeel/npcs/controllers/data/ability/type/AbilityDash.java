package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityDash;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityDash;

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
        DEFENSIVE
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
    private float dashSpeed = 1.5f;

    // Runtime state
    private transient Vec3 dashDirection;
    private transient double startX, startY, startZ;
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
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
        IAbilityConfigCallback callback) {
        return new SubGuiAbilityDash(this, callback);
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
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        startX = npc.posX;
        startY = npc.posY;
        startZ = npc.posZ;

        // Choose random direction based on mode
        DashDirection[] directions = dashMode == DashMode.AGGRESSIVE
            ? AGGRESSIVE_DIRECTIONS
            : DEFENSIVE_DIRECTIONS;
        chosenDirection = directions[RANDOM.nextInt(directions.length)];

        // Calculate dash direction relative to target
        float baseYaw;
        if (target != null) {
            double dx = target.posX - npc.posX;
            double dz = target.posZ - npc.posZ;
            baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            baseYaw = npc.rotationYaw;
        }

        // Apply direction offset
        float dashYaw = baseYaw + chosenDirection.getAngleOffset();
        float yawRad = (float) Math.toRadians(dashYaw);

        dashDirection = Vec3.createVectorHelper(
            -Math.sin(yawRad),
            0,
            Math.cos(yawRad)
        );

    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (dashDirection == null) return;

        // Calculate distance traveled
        double distanceTraveled = Math.sqrt(
            Math.pow(npc.posX - startX, 2) +
                Math.pow(npc.posZ - startZ, 2)
        );

        // Check if reached max distance
        if (distanceTraveled >= dashDistance) {
            npc.motionX = 0;
            npc.motionZ = 0;
            npc.velocityChanged = true;
            signalCompletion();
            return;
        }

        if (isDashBlocked(npc)) {
            npc.motionX = 0;
            npc.motionZ = 0;
            npc.velocityChanged = true;
            signalCompletion();
            return;
        }

        // Move NPC
        npc.motionX = dashDirection.xCoord * dashSpeed;
        npc.motionY = 0;
        npc.motionZ = dashDirection.zCoord * dashSpeed;
        npc.velocityChanged = true;

        // Trail particles
        world.spawnParticle("smoke", npc.posX, npc.posY + 0.5, npc.posZ, 0, 0, 0);
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        npc.motionX = 0;
        npc.motionZ = 0;
        npc.velocityChanged = true;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, net.minecraft.util.DamageSource source, float damage) {
        npc.motionX = 0;
        npc.motionZ = 0;
        npc.velocityChanged = true;
    }

    @Override
    public void cleanup() {
        dashDirection = null;
        chosenDirection = null;
    }

    private boolean isDashBlocked(EntityNPCInterface npc) {
        if (dashDirection == null) return true;
        double nextX = dashDirection.xCoord * dashSpeed;
        double nextZ = dashDirection.zCoord * dashSpeed;
        return !npc.worldObj.getCollidingBoundingBoxes(npc, npc.boundingBox.copy().offset(nextX, 0, nextZ)).isEmpty();
    }

    // ==================== PREVIEW MODE ====================

    private transient double previewDirX, previewDirZ;
    private transient double previewStartX, previewStartZ;
    private transient boolean previewDashing = false;

    @Override
    @SideOnly(Side.CLIENT)
    public void onPreviewExecute(EntityNPCInterface npc) {
        previewStartX = npc.posX;
        previewStartZ = npc.posZ;
        previewDashing = false;

        // Pick a random direction based on mode
        DashDirection[] directions = dashMode == DashMode.AGGRESSIVE
            ? AGGRESSIVE_DIRECTIONS
            : DEFENSIVE_DIRECTIONS;
        DashDirection dir = directions[RANDOM.nextInt(directions.length)];

        float baseYaw;
        if (previewTarget != null) {
            double dx = previewTarget.posX - npc.posX;
            double dz = previewTarget.posZ - npc.posZ;
            baseYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            baseYaw = npc.rotationYaw;
        }

        float dashYaw = baseYaw + dir.getAngleOffset();
        float yawRad = (float) Math.toRadians(dashYaw);
        previewDirX = -Math.sin(yawRad);
        previewDirZ = Math.cos(yawRad);
        previewDashing = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onPreviewActiveTick(EntityNPCInterface npc, int tick) {
        if (!previewDashing) return;

        double distTraveled = Math.sqrt(
            Math.pow(npc.posX - previewStartX, 2) +
            Math.pow(npc.posZ - previewStartZ, 2)
        );

        if (distTraveled >= dashDistance) {
            previewDashing = false;
            return;
        }

        npc.prevPosX = npc.posX;
        npc.prevPosY = npc.posY;
        npc.prevPosZ = npc.posZ;

        npc.posX += previewDirX * dashSpeed;
        npc.posZ += previewDirZ * dashSpeed;
    }

    @Override
    public int getPreviewActiveDuration() {
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
        this.dashSpeed = nbt.hasKey("dashSpeed") ? nbt.getFloat("dashSpeed") : 1.5f;
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
}
