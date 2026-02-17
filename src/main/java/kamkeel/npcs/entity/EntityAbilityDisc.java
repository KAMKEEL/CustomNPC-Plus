package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import noppes.npcs.EventHooks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * Disc projectile - flat spinning disc with optional boomerang behavior.
 * Has a wider, thinner hitbox compared to Orb.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityDisc extends EntityEnergyProjectile {

    // Disc-specific movement propertie

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40; // Ticks before returning
    private boolean returning = false;
    private int ticksSinceMiss = 0;

    // Disc shape properties
    private float discRadius = 1.0f; // Width of disc
    private float discThickness = 0.2f; // Height of disc
    private boolean vertical = false; // false = horizontal (flat), true = vertical (thin edge forward)

    // Charging state (disc-specific target sizes)
    private float targetDiscRadius = 1.0f; // Full radius to grow to during charging
    private float targetDiscThickness = 0.2f; // Full thickness to grow to during charging

    public EntityAbilityDisc(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters using data classes.
     */
    public EntityAbilityDisc(World world, EntityLivingBase owner, EntityLivingBase target,
                             double x, double y, double z,
                             float discRadius, float discThickness,
                             EnergyDisplayData display, EnergyCombatData combat,
                             EnergyHomingData homing, EnergyLightningData lightning,
                             EnergyLifespanData lifespan, EnergyTrajectoryData trajectory,
                             boolean boomerang, int boomerangDelay) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z, discRadius, display, combat, lightning, lifespan, trajectory);

        this.homingData = homing;

        this.boomerang = boomerang;
        this.boomerangDelay = boomerangDelay;

        this.discRadius = discRadius;
        this.discThickness = discThickness;

        // Calculate initial velocity toward target
        calculateInitialVelocity(owner, target, x, y, z);
    }

    /**
     * Setup this disc in charging mode (for windup phase).
     * The disc will grow from 0 to discRadius over chargeDuration ticks.
     * Position follows the owner based on anchor point.
     */
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration, boolean vertical) {
        this.targetDiscRadius = this.discRadius;
        this.targetDiscThickness = this.discThickness;
        super.setupCharging(anchor, chargeDuration);
        this.vertical = vertical;
        this.discRadius = 0.01f;
        this.discThickness = 0.01f;
    }

    /**
     * Setup this disc in preview mode for GUI display.
     * Follows anchor point and animations like in the real game.
     * Can be fired when transitioning to active phase.
     */
    public void setupPreview(EntityLivingBase owner, float discRadius, float discThickness, EnergyDisplayData display, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration, boolean vertical) {
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
        this.vertical = vertical;

        // Store target size and start at 0 for grow effect
        this.targetDiscRadius = discRadius;
        this.targetDiscThickness = discThickness;
        this.discRadius = 0.01f;
        this.discThickness = 0.01f;
        this.size = 0.01f;

        // Initial position at anchor point
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
        setCharging(false);

        // Update start position to current position
        startX = posX;
        startY = posY;
        startZ = posZ;

        // Sync prev position to prevent visual jump on first frame
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        // Fire forward based on owner facing direction
        Entity owner = getOwnerEntity();
        if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = 0; // Fire horizontally
            motionX = -Math.sin(yaw) * Math.cos(pitch) * getSpeed();
            motionY = -Math.sin(pitch) * getSpeed();
            motionZ = Math.cos(yaw) * Math.cos(pitch) * getSpeed();
        } else {
            motionX = getSpeed();
            motionY = 0;
            motionZ = 0;
        }
    }

    /**
     * Start the disc moving (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startMoving(EntityLivingBase target) {
        setCharging(false);

        // For player casters, snap to look vector for crosshair-aligned launch
        snapToPlayerLookVector();

        // Update start position to current position
        startX = posX;
        startY = posY;
        startZ = posZ;

        // Calculate velocity toward target
        Entity owner = getOwnerEntity();

        if (target != null) {
            double dx = target.posX - posX;
            double dy = (target.posY + target.getEyeHeight()) - posY;
            double dz = target.posZ - posZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                motionX = (dx / len) * getSpeed();
                motionY = (dy / len) * getSpeed();
                motionZ = (dz / len) * getSpeed();
            }
        } else if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            motionX = -Math.sin(yaw) * Math.cos(pitch) * getSpeed();
            motionY = -Math.sin(pitch) * getSpeed();
            motionZ = Math.cos(yaw) * Math.cos(pitch) * getSpeed();
        }
    }

    @Override
    protected void updateRotation() {
        // Disc spins ONLY on Y axis (flat spin like a saw blade)
        this.rotationValY += getRotationSpeed();
        // No wobble - keep X and Z at 0 for stable flight
        if (this.rotationValY > 360.0f) this.rotationValY -= 360.0f;
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
            if (returning) {
                updateReturnToOwner();
            } else {
                updateHoming();
            }

            checkBlockCollision();
            checkEntityCollision();
            this.moveEntity(motionX, motionY, motionZ);

            // Track ticks for boomerang delay (checkMaxDistance handles the actual return trigger)
            if (boomerang && !returning && !hasHit) {
                ticksSinceMiss++;
            }
        }

        // Check wall collision
        if (this.isCollidedHorizontally || this.isCollidedVertically) {
            if (!worldObj.isRemote) {
                hasHit = true;
                if (isExplosive()) {
                    doExplosion();
                }
            }
            this.setDead();
        }
    }

    @Override
    protected boolean checkMaxDistance() {
        double distTraveled = Math.sqrt(
            (posX - startX) * (posX - startX) +
            (posY - startY) * (posY - startY) +
            (posZ - startZ) * (posZ - startZ)
        );

        if (boomerang) {
            // If not returning yet, check if we should start returning
            if (!returning && !hasHit) {
                if (distTraveled >= getMaxDistance() * 0.8 || ticksSinceMiss >= boomerangDelay) {
                    returning = true;
                }
            }

            // If returning, only die when close to owner
            if (returning) {
                Entity owner = getOwnerEntity();
                if (owner != null) {
                    double distToOwner = Math.sqrt(
                        (posX - owner.posX) * (posX - owner.posX) +
                        (posY - owner.posY) * (posY - owner.posY) +
                        (posZ - owner.posZ) * (posZ - owner.posZ)
                    );
                    return distToOwner < 1.5;
                }
                // Owner gone but returning - keep going toward last known position
                return false;
            }

            // Not returning yet - don't die from distance (we'll trigger return above)
            return false;
        }

        // Non-boomerang: standard max distance check
        return distTraveled >= getMaxDistance();
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

    private void updateReturnToOwner() {
        Entity owner = getOwnerEntity();

        // If owner is gone (unloaded/dead), head back toward start position
        double targetX, targetY, targetZ;
        if (owner != null) {
            targetX = owner.posX;
            targetY = owner.posY + owner.height * 0.5;
            targetZ = owner.posZ;
        } else {
            // Return to start position if owner not available
            targetX = startX;
            targetY = startY;
            targetZ = startZ;
        }

        double dx = targetX - posX;
        double dy = targetY - posY;
        double dz = targetZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > 0) {
            // Stronger homing when returning
            double returnStrength = Math.min(1.0, getHomingStrength() * 2.5);
            double desiredVX = (dx / dist) * getSpeed();
            double desiredVY = (dy / dist) * getSpeed();
            double desiredVZ = (dz / dist) * getSpeed();

            motionX += (desiredVX - motionX) * returnStrength;
            motionY += (desiredVY - motionY) * returnStrength;
            motionZ += (desiredVZ - motionZ) * returnStrength;

            double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * getSpeed();
                motionY = (motionY / vLen) * getSpeed();
                motionZ = (motionZ / vLen) * getSpeed();
            }
        }
    }

    private void checkBlockCollision() {
        Vec3 currentPos = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 nextPos = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        // Use full raytrace that doesn't stop at liquids and checks all blocks
        MovingObjectPosition blockHit = worldObj.func_147447_a(currentPos, nextPos, false, true, false);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!worldObj.isRemote) {
                EventHooks.onEnergyProjectileBlockImpact(this, blockHit.blockX, blockHit.blockY, blockHit.blockZ);
            }
            hasHit = true;
            if (isExplosive()) {
                posX = blockHit.hitVec.xCoord;
                posY = blockHit.hitVec.yCoord;
                posZ = blockHit.hitVec.zCoord;
                doExplosion();
            }
            this.setDead();
        }
    }

    private void checkEntityCollision() {
        // Disc hitbox: wider but thinner
        double halfWidth = discRadius * 0.5;
        double halfHeight = discThickness * 0.5;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            posX - halfWidth, posY - halfHeight, posZ - halfWidth,
            posX + halfWidth, posY + halfHeight, posZ + halfWidth
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;

            hasHit = true;

            if (isExplosive()) {
                doExplosion();
            } else {
                applyDamage(entity);
            }

            this.setDead();
            return;
        }
    }

    /**
     * Update during charging state - follow owner based on anchor point.
     * Overrides base to grow disc-specific radius/thickness instead of just size.
     */
    @Override
    protected void updateCharging() {
        chargeTick++;

        // Grow size based on charge progress
        float progress = getChargeProgress();
        this.discRadius = targetDiscRadius * progress;
        this.discThickness = targetDiscThickness * progress;
        this.size = this.discRadius; // Base size for interpolation

        Entity owner = getOwnerEntity();
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData);
            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }

        // Update rotation during charging (disc still spins)
        updateRotation();
    }

    // ==================== GETTERS ====================

    public float getDiscRadius() {
        return discRadius;
    }

    public void setDiscRadius(float radius) {
        this.discRadius = radius;
    }

    public float getDiscThickness() {
        return discThickness;
    }

    public void setDiscThickness(float thickness) {
        this.discThickness = thickness;
    }

    public boolean isBoomerang() {
        return boomerang;
    }

    public void setBoomerang(boolean boomerang) {
        this.boomerang = boomerang;
    }

    public int getBoomerangDelay() {
        return boomerangDelay;
    }

    public void setBoomerangDelay(int delay) {
        this.boomerangDelay = delay;
    }

    public boolean isReturning() {
        return returning;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * Get the yaw angle (degrees) from motion vector for vertical disc orientation.
     * During charging, uses owner's facing direction.
     */
    public float getTravelYaw() {
        if (isCharging()) {
            Entity owner = previewMode ? previewOwner : getOwnerEntity();
            if (owner != null) {
                return owner.rotationYaw;
            }
        }
        double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (speed > 0.001) {
            return (float) (Math.atan2(-motionX, motionZ) * 180.0 / Math.PI);
        }
        return 0.0f;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.boomerang = nbt.hasKey("Boomerang") && nbt.getBoolean("Boomerang");
        this.boomerangDelay = nbt.hasKey("BoomerangDelay") ? nbt.getInteger("BoomerangDelay") : 40;
        this.discRadius = nbt.hasKey("DiscRadius") ? nbt.getFloat("DiscRadius") : 1.0f;
        this.discThickness = nbt.hasKey("DiscThickness") ? nbt.getFloat("DiscThickness") : 0.2f;
        this.vertical = nbt.hasKey("Vertical") ? nbt.getBoolean("Vertical") : false;
        this.returning = nbt.hasKey("Returning") && nbt.getBoolean("Returning");
        // Charging state (common fields handled by base)
        readChargingNBT(nbt);
        this.targetDiscRadius = nbt.hasKey("TargetDiscRadius") ? nbt.getFloat("TargetDiscRadius") : this.discRadius;
        this.targetDiscThickness = nbt.hasKey("TargetDiscThickness") ? nbt.getFloat("TargetDiscThickness") : this.discThickness;
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setBoolean("Boomerang", boomerang);
        nbt.setInteger("BoomerangDelay", boomerangDelay);
        nbt.setFloat("DiscRadius", discRadius);
        nbt.setFloat("DiscThickness", discThickness);
        nbt.setBoolean("Vertical", vertical);
        nbt.setBoolean("Returning", returning);
        // Charging state (common fields handled by base)
        writeChargingNBT(nbt);
        nbt.setFloat("TargetDiscRadius", targetDiscRadius);
        nbt.setFloat("TargetDiscThickness", targetDiscThickness);
    }
}
