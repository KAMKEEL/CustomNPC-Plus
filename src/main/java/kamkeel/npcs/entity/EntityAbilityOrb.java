package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * Orb projectile - spherical homing energy ball.
 * Extends EntityEnergyAbility for shared functionality.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityOrb extends EntityEnergyProjectile {
    private static final int HOMING_STARTUP_TICKS = 4;
    private static final int HOMING_RAMP_TICKS = 8;

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
                            EnergyLifespanData lifespan) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z, orbSize, display, combat, lightning, lifespan);

        this.homingData = homing != null ? homing.copy() : new EnergyHomingData();

        // Calculate initial velocity toward target
        calculateInitialVelocity(owner, target, x, y, z);
    }

    /**
     * Setup this orb in preview mode for GUI display.
     * Follows anchor point and animations like in the real game.
     * Can be fired when transitioning to active phase.
     */
    public void setupPreview(EntityLivingBase owner, float orbSize, EnergyDisplayData display, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration) {
        setupPreviewState(owner, display, lightning, anchor, chargeDuration);

        // Store target size and start at 0 for grow effect
        this.targetSize = orbSize;
        setVisualSize(0.01f);
        setChargeOriginFromAnchor(owner, anchorData);
        clearMotion();
    }

    /**
     * Start preview firing (simulates firing toward a point in front of NPC).
     */
    public void startPreviewFiring() {
        startPreviewFiringDefault();
    }

    /**
     * Start the orb moving (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startMoving(EntityLivingBase target) {
        startMovingTowardTargetDefault(target);
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
            handleClientInterpolation();
        } else {
            // Server-side logic
            updateMovement();
            checkBlockCollision();
            checkEntityCollision();
            this.moveEntity(motionX, motionY, motionZ);
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
        if (ticksExisted > HOMING_STARTUP_TICKS) {
            int homingTicks = ticksExisted - HOMING_STARTUP_TICKS;
            if (homingTicks >= HOMING_RAMP_TICKS) {
                updateHoming();
            } else {
                float ramp = homingTicks / (float) HOMING_RAMP_TICKS;
                updateHomingWithRamp(ramp);
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

    // ==================== DEBUG ====================

    @Override
    protected String debugLogExtra() {
        return String.format("motion=(%.3f,%.3f,%.3f) homing=%b start=(%.2f,%.2f,%.2f)",
            motionX, motionY, motionZ,
            homingData != null && homingData.isHoming(),
            startX, startY, startZ);
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
