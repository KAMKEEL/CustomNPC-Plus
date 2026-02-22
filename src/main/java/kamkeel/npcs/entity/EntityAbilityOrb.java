package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.EnergyTrajectoryData;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;

import java.util.Locale;

/**
 * Orb projectile - spherical homing energy ball.
 * Extends EntityEnergyAbility for shared functionality.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityOrb extends EntityEnergyProjectile {
    private static final int HOMING_STARTUP_TICKS = 4;
    private static final int HOMING_RAMP_TICKS = 8;
    private static final boolean DEBUG_ORB_FIRE_TRACE = false;
    private static final int DEBUG_TRACE_TICKS = 14;

    public EntityAbilityOrb(World world) {
        super(world);
    }

    /**
     * Full constructor using data classes for grouped parameters.
     */
    public EntityAbilityOrb(World world, EntityLivingBase owner, EntityLivingBase target,
                            double x, double y, double z, float orbSize,
                            EnergyDisplayData display, EnergyCombatData combat,
                            EnergyHomingData homing, EnergyLightningData lightning,
                            EnergyLifespanData lifespan, EnergyTrajectoryData trajectory) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z, orbSize, display, combat, lightning, lifespan, trajectory);

        this.homingData = homing;

        // Calculate initial velocity toward target
        calculateInitialVelocity(owner, target, x, y, z);
    }

    /**
     * Setup this orb in preview mode for GUI display.
     * Follows anchor point and animations like in the real game.
     * Can be fired when transitioning to active phase.
     */
    public void setupPreview(EntityLivingBase owner, float orbSize, EnergyDisplayData display, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration) {
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);

        // Set visual properties
        this.displayData = display;
        this.lightningData = lightning;

        // Set charging state
        this.setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.anchorData = anchor;

        // Store target size and start at 0 for grow effect
        this.targetSize = orbSize;
        this.size = 0.01f;
        this.renderCurrentSize = 0.01f;
        this.prevRenderSize = 0.01f;

        // Initial position follows anchor helper during charging.
        Vec3 pos = AnchorPointHelper.calculateAnchorPosition(owner, anchorData);
        this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        this.prevPosX = pos.xCoord;
        this.prevPosY = pos.yCoord;
        this.prevPosZ = pos.zCoord;
        this.startX = pos.xCoord;
        this.startY = pos.yCoord;
        this.startZ = pos.zCoord;

        // Clear motion
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    /**
     * Start preview firing (simulates firing toward a point in front of NPC).
     */
    public void startPreviewFiring() {
        beginLookVectorLaunch(true);
        setMotionAlongLookVectorOrFallback(getSpeed(), getSpeed(), 0, 0);
    }

    /**
     * Start the orb moving (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startMoving(EntityLivingBase target) {
        // Launch strictly along owner's look vector using shared launch origin.
        Entity owner = getOwnerEntity();
        if (owner instanceof EntityLivingBase) {
            Vec3 look = getOwnerLookVector();
            if (look != null) {
                debugTrace("startMoving:before", look, -1.0f);
                if (beginLookVectorLaunch((EntityLivingBase) owner, look, false)) {
                    debugTrace("setLaunchPosition", look, -1.0f);
                }
                motionX = look.xCoord * getSpeed();
                motionY = look.yCoord * getSpeed();
                motionZ = look.zCoord * getSpeed();
                debugTrace("startMoving:after", look, -1.0f);
                return;
            }
        }

        // Fallback if owner is unavailable.
        setCharging(false);
        syncStartPositionToCurrent();
        setMotionAlongLookVectorOrFallback(getSpeed(), getSpeed(), 0, 0);
        debugTrace("startMoving:fallback", null, -1.0f);
    }

    @Override
    protected void updateProjectile() {
        // Handle charging state (windup phase)
        if (isCharging()) {
            updateCharging();
            return;
        }

        // In preview mode, run movement on client (no server)
        if (previewMode) {
            updatePreviewMovement();
            return;
        }

        if (worldObj.isRemote) {
            if (shouldDebugTick()) debugTrace("client:beforeInterpolation", null, -1.0f);
            handleClientInterpolation();
            if (shouldDebugTick()) debugTrace("client:afterInterpolation", null, -1.0f);
        } else {
            // Server-side logic
            if (shouldDebugTick()) debugTrace("server:beforeMovement", null, -1.0f);
            updateMovement();
            checkBlockCollision();
            checkEntityCollision();
            this.moveEntity(motionX, motionY, motionZ);
            if (shouldDebugTick()) debugTrace("server:afterMove", null, -1.0f);
        }

        // Check wall collision
        handleSolidCollisionTermination();
    }

    /**
     * Update movement in preview mode (client-side only, no damage).
     */
    private void updatePreviewMovement() {
        // Simple movement - just apply motion
        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;
    }

    private void updateMovement() {
        if (!isTrajectoryConcluded()) {
            updateTrajectory();
        } else {
            if (ticksExisted > HOMING_STARTUP_TICKS) {
                int homingTicks = ticksExisted - HOMING_STARTUP_TICKS;
                if (homingTicks >= HOMING_RAMP_TICKS) {
                    if (shouldDebugTick()) debugTrace("updateMovement:homingFull", null, 1.0f);
                    updateHoming();
                } else {
                    float ramp = homingTicks / (float) HOMING_RAMP_TICKS;
                    if (shouldDebugTick()) debugTrace("updateMovement:homingRamp", null, ramp);
                    updateHomingWithRamp(ramp);
                }
            }
        }
    }

    private void updateHomingWithRamp(float ramp) {
        if (ramp <= 0.0f) return;
        float originalStrength = homingData.homingStrength;
        homingData.homingStrength = originalStrength * ramp;
        updateHoming();
        homingData.homingStrength = originalStrength;
    }

    private void updateTrajectory() {
        if (isTrajectoryConcluded()) return;
        // i really fucking hate math

    }

    private boolean isTrajectoryConcluded() {
        if (trajectoryData.isEmpty()) return true;

        for (int i = 0; i < trajectoryData.size(); i++) {
            if (!trajectoryData.getPath(i).isConcluded()) {
                return false;
            }
        }

        return true;
    }

    private void checkBlockCollision() {
        handleBlockImpact(rayTraceBlocks(posX, posY, posZ, posX + motionX, posY + motionY, posZ + motionZ), true);
    }

    private void checkEntityCollision() {
        // Swept collision to avoid fast orbs skipping targets between ticks.
        double nextX = posX + motionX;
        double nextY = posY + motionY;
        double nextZ = posZ + motionZ;
        // Keep collision radius proportional to rendered orb size.
        double hitSize = Math.max(0.05, size * 0.5);
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            Math.min(posX, nextX) - hitSize, Math.min(posY, nextY) - hitSize, Math.min(posZ, nextZ) - hitSize,
            Math.max(posX, nextX) + hitSize, Math.max(posY, nextY) + hitSize, Math.max(posZ, nextZ) + hitSize
        );
        processEntitiesInHitBox(hitBox, nextX, nextY, nextZ);
    }

    private boolean shouldDebugTick() {
        return DEBUG_ORB_FIRE_TRACE && !isCharging() && !previewMode && ticksExisted <= DEBUG_TRACE_TICKS;
    }

    private void debugTrace(String stage, Vec3 look, float ramp) {
        if (!DEBUG_ORB_FIRE_TRACE || worldObj == null) return;

        String side = worldObj.isRemote ? "CLIENT" : "SERVER";
        Entity owner = getOwnerEntity();
        String ownerInfo = owner == null
            ? "null"
            : owner.getEntityId() + "@(" + fmt(owner.posX) + "," + fmt(owner.posY) + "," + fmt(owner.posZ) + ")";
        String lookInfo = look == null
            ? "null"
            : "(" + fmt(look.xCoord) + "," + fmt(look.yCoord) + "," + fmt(look.zCoord) + ")";

        LogWriter.info(
            "[OrbFireTrace][" + side + "][" + stage + "] "
                + "id=" + getEntityId()
                + " tick=" + ticksExisted
                + " charging=" + isCharging()
                + " start=(" + fmt(startX) + "," + fmt(startY) + "," + fmt(startZ) + ")"
                + " pos=(" + fmt(posX) + "," + fmt(posY) + "," + fmt(posZ) + ")"
                + " prev=(" + fmt(prevPosX) + "," + fmt(prevPosY) + "," + fmt(prevPosZ) + ")"
                + " motion=(" + fmt(motionX) + "," + fmt(motionY) + "," + fmt(motionZ) + ")"
                + " size=" + fmt(size)
                + " speed=" + fmt(getSpeed())
                + " ramp=" + (ramp < 0 ? "n/a" : fmt(ramp))
                + " owner=" + ownerInfo
                + " look=" + lookInfo
        );
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.4f", v);
    }

    // ==================== GETTERS ====================

    // Legacy getter for renderer compatibility
    public float getOrbSize() {
        return size;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        readChargingNBT(nbt);
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        writeChargingNBT(nbt);
    }
}
