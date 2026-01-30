package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * Orb projectile - spherical homing energy ball.
 * Extends EntityAbilityProjectile for shared functionality.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityOrb extends EntityAbilityProjectile {

    // Orb-specific movement properties
    private float speed = 0.5f;
    private boolean homing = true;
    private float homingStrength = 0.35f;  // Increased from 0.15 for better tracking
    private float homingRange = 20.0f;

    // Charging state (during windup)
    private boolean charging = false;
    private int chargeDuration = 40;
    private int chargeTick = 0;
    private EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.FRONT);
    private float targetSize = 1.0f; // Full size to grow to during charging

    // Data watcher index for charging state (synced to clients)
    private static final int DW_CHARGING = 20;

    public EntityAbilityOrb(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        // Register data watcher for charging state
        this.dataWatcher.addObject(DW_CHARGING, (byte) 0);
    }

    /**
     * Check if orb is in charging state (synced via data watcher).
     * In preview mode, uses local field since data watcher isn't synced.
     */
    public boolean isCharging() {
        if (previewMode) {
            return this.charging;
        }
        return this.dataWatcher.getWatchableObjectByte(DW_CHARGING) == 1;
    }

    /**
     * Set charging state (server only, synced to clients via data watcher).
     */
    private void setCharging(boolean value) {
        this.charging = value;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_CHARGING, (byte) (value ? 1 : 0));
        }
    }

    /**
     * Full constructor using data classes for grouped parameters.
     */
    public EntityAbilityOrb(World world, EntityLivingBase owner, EntityLivingBase target,
                            double x, double y, double z, float orbSize,
                            EnergyColorData color, EnergyCombatData combat,
                            EnergyHomingData homing, EnergyLightningData lightning,
                            EnergyLifespanData lifespan) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z, orbSize, color, combat, lightning, lifespan);

        // Orb-specific properties
        this.speed = homing.speed;
        this.homing = homing.homing;
        this.homingStrength = homing.homingStrength;
        this.homingRange = homing.homingRange;

        // Calculate initial velocity toward target
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.getEyeHeight()) - y;
            double dz = target.posZ - z;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                this.motionX = (dx / len) * homing.speed;
                this.motionY = (dy / len) * homing.speed;
                this.motionZ = (dz / len) * homing.speed;
            }
        } else {
            // Fire in NPC's facing direction
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.motionX = -Math.sin(yaw) * Math.cos(pitch) * homing.speed;
            this.motionY = -Math.sin(pitch) * homing.speed;
            this.motionZ = Math.cos(yaw) * Math.cos(pitch) * homing.speed;
        }
    }

    /**
     * Setup this orb in charging mode (for windup phase).
     * The orb will grow from 0 to orbSize over chargeDuration ticks.
     * Position follows the owner based on anchor point.
     */
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration) {
        setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.anchorData = anchor;
        this.targetSize = this.size;
        this.size = 0.01f;
        this.renderCurrentSize = 0.01f;
        this.prevRenderSize = 0.01f;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    /**
     * Setup this orb in preview mode for GUI display.
     * Follows anchor point and animations like in the real game.
     * Can be fired when transitioning to active phase.
     */
    public void setupPreview(EntityLivingBase owner, float orbSize, EnergyColorData color, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration) {
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);

        // Set visual properties
        this.innerColor = color.innerColor;
        this.outerColor = color.outerColor;
        this.outerColorEnabled = color.outerColorEnabled;
        this.outerColorWidth = color.outerColorWidth;
        this.rotationSpeed = color.rotationSpeed;
        this.lightningEffect = lightning.lightningEffect;
        this.lightningDensity = lightning.lightningDensity;
        this.lightningRadius = lightning.lightningRadius;
        this.lightningFadeTime = lightning.lightningFadeTime;

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
        if (!isCharging()) return;

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
        Entity owner = getOwner();
        if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(0); // Fire horizontally
            motionX = -Math.sin(yaw) * Math.cos(pitch) * speed;
            motionY = -Math.sin(pitch) * speed;
            motionZ = Math.cos(yaw) * Math.cos(pitch) * speed;
        } else {
            // Default: fire forward (positive X in model space)
            motionX = speed;
            motionY = 0;
            motionZ = 0;
        }
    }

    /**
     * Start the orb moving (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startMoving(EntityLivingBase target) {
        if (!isCharging()) return;

        setCharging(false);

        // Update start position to current position
        startX = posX;
        startY = posY;
        startZ = posZ;

        // Calculate velocity toward target
        Entity owner = getOwner();

        if (target != null) {
            double dx = target.posX - posX;
            double dy = (target.posY + target.getEyeHeight()) - posY;
            double dz = target.posZ - posZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                motionX = (dx / len) * speed;
                motionY = (dy / len) * speed;
                motionZ = (dz / len) * speed;
            }
        } else if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            motionX = -Math.sin(yaw) * Math.cos(pitch) * speed;
            motionY = -Math.sin(pitch) * speed;
            motionZ = Math.cos(yaw) * Math.cos(pitch) * speed;
        }
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
            updateHoming();
            checkBlockCollision();
            checkEntityCollision();
            this.moveEntity(motionX, motionY, motionZ);
        }

        // Check wall collision
        if (this.isCollidedHorizontally || this.isCollidedVertically) {
            if (!worldObj.isRemote) {
                hasHit = true;
                if (explosive) {
                    doExplosion();
                }
            }
            this.setDead();
        }
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

    private void updateHoming() {
        if (!homing) return;

        Entity target = getTarget();
        if (target == null || !target.isEntityAlive()) return;

        double dx = target.posX - posX;
        double dy = (target.posY + target.getEyeHeight()) - posY;
        double dz = target.posZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist <= homingRange && dist > 0) {
            // Calculate effective homing strength - increases when closer to commit to target
            // This prevents orbiting behavior at close range
            float effectiveStrength = homingStrength;
            if (dist < homingRange * 0.3) {
                // Within 30% of homing range, dramatically increase strength to commit
                effectiveStrength = Math.min(1.0f, homingStrength * 2.5f);
            } else if (dist < homingRange * 0.6) {
                // Within 60% of homing range, moderately increase strength
                effectiveStrength = Math.min(0.8f, homingStrength * 1.5f);
            }

            double desiredVX = (dx / dist) * speed;
            double desiredVY = (dy / dist) * speed;
            double desiredVZ = (dz / dist) * speed;

            motionX += (desiredVX - motionX) * effectiveStrength;
            motionY += (desiredVY - motionY) * effectiveStrength;
            motionZ += (desiredVZ - motionZ) * effectiveStrength;

            // Normalize to maintain speed
            double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * speed;
                motionY = (motionY / vLen) * speed;
                motionZ = (motionZ / vLen) * speed;
            }
        }
    }

    private void checkBlockCollision() {
        Vec3 currentPos = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 nextPos = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        // Use full raytrace that doesn't stop at liquids and checks all blocks
        MovingObjectPosition blockHit = worldObj.func_147447_a(currentPos, nextPos, false, true, false);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            hasHit = true;
            if (explosive) {
                posX = blockHit.hitVec.xCoord;
                posY = blockHit.hitVec.yCoord;
                posZ = blockHit.hitVec.zCoord;
                doExplosion();
            }
            this.setDead();
        }
    }

    private void checkEntityCollision() {
        double hitSize = size * 0.5;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            posX - hitSize, posY - hitSize, posZ - hitSize,
            posX + hitSize, posY + hitSize, posZ + hitSize
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;

            hasHit = true;

            if (explosive) {
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
     */
    private void updateCharging() {
        chargeTick++;

        Entity owner = getOwner();
        if (owner == null || owner.isDead) {
            setDead();
            return;
        }

        // Grow size based on charge progress
        float progress = getChargeProgress();
        this.size = targetSize * progress;

        // Calculate position based on anchor point, offset downward by half size to center
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData);
            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }
    }

    /**
     * Get the charge progress (0-1).
     */
    public float getChargeProgress() {
        if (chargeDuration <= 0) return 1.0f;
        return Math.min(1.0f, (float) chargeTick / chargeDuration);
    }

    /**
     * Get interpolated charge progress for smooth rendering.
     */
    public float getInterpolatedChargeProgress(float partialTicks) {
        if (chargeDuration <= 0) return 1.0f;
        float prevProgress = Math.max(0, (float) (chargeTick - 1) / chargeDuration);
        float currProgress = Math.min(1.0f, (float) chargeTick / chargeDuration);
        return prevProgress + (currProgress - prevProgress) * partialTicks;
    }

    // ==================== GETTERS ====================

    public float getSpeed() {
        return speed;
    }

    public boolean isHoming() {
        return homing;
    }

    public float getHomingStrength() {
        return homingStrength;
    }

    public float getHomingRange() {
        return homingRange;
    }

    // Legacy getter for renderer compatibility
    public float getOrbSize() {
        return size;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.speed = nbt.hasKey("OrbSpeed") ? nbt.getFloat("OrbSpeed") : 0.5f;
        this.homing = !nbt.hasKey("Homing") || nbt.getBoolean("Homing");
        this.homingStrength = nbt.hasKey("HomingStrength") ? nbt.getFloat("HomingStrength") : 0.15f;
        this.homingRange = nbt.hasKey("HomingRange") ? nbt.getFloat("HomingRange") : 20.0f;
        // Charging state
        boolean isCharging = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.charging = isCharging;
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (isCharging ? 1 : 0));
        this.chargeDuration = nbt.hasKey("ChargeDuration") ? nbt.getInteger("ChargeDuration") : 40;
        this.chargeTick = nbt.hasKey("ChargeTick") ? nbt.getInteger("ChargeTick") : 0;
        this.targetSize = nbt.hasKey("TargetSize") ? nbt.getFloat("TargetSize") : this.size;
        // Anchor data
        this.anchorData.readNBT(nbt);
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("OrbSpeed", speed);
        nbt.setBoolean("Homing", homing);
        nbt.setFloat("HomingStrength", homingStrength);
        nbt.setFloat("HomingRange", homingRange);
        // Charging state
        nbt.setBoolean("Charging", isCharging());
        nbt.setInteger("ChargeDuration", chargeDuration);
        nbt.setInteger("ChargeTick", chargeTick);
        anchorData.writeNBT(nbt);
        nbt.setFloat("TargetSize", targetSize);
    }
}
