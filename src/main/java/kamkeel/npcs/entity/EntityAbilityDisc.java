package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Disc projectile - flat spinning disc with optional boomerang behavior.
 * Has a wider, thinner hitbox compared to Orb.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityDisc extends EntityAbilityProjectile {

    // Disc-specific movement properties
    private float speed = 0.5f;
    private boolean homing = true;
    private float homingStrength = 0.35f;  // Increased from 0.15 for better tracking
    private float homingRange = 20.0f;

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40; // Ticks before returning
    private boolean returning = false;
    private int ticksSinceMiss = 0;

    // Disc shape properties
    private float discRadius = 1.0f; // Width of disc
    private float discThickness = 0.2f; // Height of disc

    // Charging state (during windup)
    private boolean charging = false;
    private int chargeDuration = 40;
    private int chargeTick = 0;
    private AnchorPoint anchorPoint = AnchorPoint.FRONT;
    private float targetDiscRadius = 1.0f; // Full radius to grow to during charging
    private float targetDiscThickness = 0.2f; // Full thickness to grow to during charging

    // Data watcher index for charging state (synced to clients)
    private static final int DW_CHARGING = 20;

    public EntityAbilityDisc(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        // Register data watcher for charging state
        this.dataWatcher.addObject(DW_CHARGING, (byte) 0);
    }

    /**
     * Check if disc is in charging state (synced via data watcher).
     */
    public boolean isCharging() {
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
     * Full constructor with all parameters (no lightning).
     */
    public EntityAbilityDisc(World world, EntityNPCInterface owner, EntityLivingBase target,
                              double x, double y, double z,
                              float discRadius, float discThickness, int innerColor, int outerColor,
                              boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                              float damage, float knockback, float knockbackUp,
                              float speed, boolean homing, float homingStrength, float homingRange,
                              boolean boomerang, int boomerangDelay,
                              boolean explosive, float explosionRadius, float explosionDamageFalloff,
                              int stunDuration, int slowDuration, int slowLevel,
                              float maxDistance, int maxLifetime) {
        this(world, owner, target, x, y, z,
            discRadius, discThickness, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp, speed, homing, homingStrength, homingRange,
            boomerang, boomerangDelay, explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel, maxDistance, maxLifetime,
            false, 0.15f, 0.5f);
    }

    /**
     * Full constructor with all parameters including lightning.
     */
    public EntityAbilityDisc(World world, EntityNPCInterface owner, EntityLivingBase target,
                              double x, double y, double z,
                              float discRadius, float discThickness, int innerColor, int outerColor,
                              boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                              float damage, float knockback, float knockbackUp,
                              float speed, boolean homing, float homingStrength, float homingRange,
                              boolean boomerang, int boomerangDelay,
                              boolean explosive, float explosionRadius, float explosionDamageFalloff,
                              int stunDuration, int slowDuration, int slowLevel,
                              float maxDistance, int maxLifetime,
                              boolean lightningEffect, float lightningDensity, float lightningRadius) {
        super(world);

        // Initialize base properties with lightning (use discRadius as size for base)
        initProjectile(owner, target, x, y, z,
            discRadius, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime,
            lightningEffect, lightningDensity, lightningRadius, 6);

        // Disc-specific properties
        this.speed = speed;
        this.homing = homing;
        this.homingStrength = homingStrength;
        this.homingRange = homingRange;

        this.boomerang = boomerang;
        this.boomerangDelay = boomerangDelay;

        this.discRadius = discRadius;
        this.discThickness = discThickness;

        // Calculate initial velocity toward target
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.height * 0.5) - y;
            double dz = target.posZ - z;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                this.motionX = (dx / len) * speed;
                this.motionY = (dy / len) * speed;
                this.motionZ = (dz / len) * speed;
            }
        } else {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.motionX = -Math.sin(yaw) * Math.cos(pitch) * speed;
            this.motionY = -Math.sin(pitch) * speed;
            this.motionZ = Math.cos(yaw) * Math.cos(pitch) * speed;
        }
    }

    /**
     * Create a disc in charging mode (for windup phase).
     * The disc will grow from 0 to discRadius over chargeDuration ticks.
     * Position follows the owner based on anchor point.
     */
    public static EntityAbilityDisc createCharging(World world, EntityNPCInterface owner, EntityLivingBase target,
                                                    float discRadius, float discThickness, int innerColor, int outerColor,
                                                    boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                                                    float damage, float knockback, float knockbackUp,
                                                    float speed, boolean homing, float homingStrength, float homingRange,
                                                    boolean boomerang, int boomerangDelay,
                                                    boolean explosive, float explosionRadius, float explosionDamageFalloff,
                                                    int stunDuration, int slowDuration, int slowLevel,
                                                    float maxDistance, int maxLifetime,
                                                    boolean lightningEffect, float lightningDensity, float lightningRadius,
                                                    AnchorPoint anchorPoint, int chargeDuration) {
        // Calculate initial position based on anchor point
        Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(owner, anchorPoint);
        double spawnX = spawnPos.xCoord;
        double spawnY = spawnPos.yCoord;
        double spawnZ = spawnPos.zCoord;

        EntityAbilityDisc disc = new EntityAbilityDisc(
            world, owner, target,
            spawnX, spawnY, spawnZ,
            discRadius, discThickness, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp, speed, homing, homingStrength, homingRange,
            boomerang, boomerangDelay,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel, maxDistance, maxLifetime,
            lightningEffect, lightningDensity, lightningRadius);

        // Set charging state (uses data watcher for client sync)
        disc.setCharging(true);
        disc.chargeDuration = chargeDuration;
        disc.chargeTick = 0;
        disc.anchorPoint = anchorPoint;

        // Store target size and start at 0 for grow effect
        disc.targetDiscRadius = disc.discRadius;
        disc.targetDiscThickness = disc.discThickness;
        disc.discRadius = 0.01f; // Start very small
        disc.discThickness = 0.01f;
        disc.size = 0.01f; // Base size used for rendering
        disc.renderCurrentSize = 0.01f;
        disc.prevRenderSize = 0.01f;

        // Clear motion while charging
        disc.motionX = 0;
        disc.motionY = 0;
        disc.motionZ = 0;

        return disc;
    }

    /**
     * Start the disc moving (exit charging mode).
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
            double dy = (target.posY + target.height * 0.5) - posY;
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
    protected void updateRotation() {
        // Disc spins ONLY on Y axis (flat spin like a saw blade)
        this.rotationValY += rotationSpeed;
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
                if (explosive) {
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
                if (distTraveled >= maxDistance * 0.8 || ticksSinceMiss >= boomerangDelay) {
                    returning = true;
                }
            }

            // If returning, only die when close to owner
            if (returning) {
                Entity owner = getOwner();
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
        return distTraveled >= maxDistance;
    }

    private void updateHoming() {
        if (!homing) return;

        Entity target = getTarget();
        if (target == null || !target.isEntityAlive()) return;

        double dx = target.posX - posX;
        double dy = (target.posY + target.height * 0.5) - posY;
        double dz = target.posZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist <= homingRange && dist > 0) {
            // Calculate effective homing strength - increases when closer to commit to target
            float effectiveStrength = homingStrength;
            if (dist < homingRange * 0.3) {
                effectiveStrength = Math.min(1.0f, homingStrength * 2.5f);
            } else if (dist < homingRange * 0.6) {
                effectiveStrength = Math.min(0.8f, homingStrength * 1.5f);
            }

            double desiredVX = (dx / dist) * speed;
            double desiredVY = (dy / dist) * speed;
            double desiredVZ = (dz / dist) * speed;

            motionX += (desiredVX - motionX) * effectiveStrength;
            motionY += (desiredVY - motionY) * effectiveStrength;
            motionZ += (desiredVZ - motionZ) * effectiveStrength;

            double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * speed;
                motionY = (motionY / vLen) * speed;
                motionZ = (motionZ / vLen) * speed;
            }
        }
    }

    private void updateReturnToOwner() {
        Entity owner = getOwner();

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
            double returnStrength = Math.min(1.0, homingStrength * 2.5);
            double desiredVX = (dx / dist) * speed;
            double desiredVY = (dy / dist) * speed;
            double desiredVZ = (dz / dist) * speed;

            motionX += (desiredVX - motionX) * returnStrength;
            motionY += (desiredVY - motionY) * returnStrength;
            motionZ += (desiredVZ - motionZ) * returnStrength;

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
        MovingObjectPosition blockHit = worldObj.rayTraceBlocks(currentPos, nextPos);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            hasHit = true;
            if (explosive) {
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
        this.discRadius = targetDiscRadius * progress;
        this.discThickness = targetDiscThickness * progress;
        this.size = this.discRadius; // Base size for interpolation

        // Calculate position based on anchor point
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorPoint);
            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }

        // Update rotation during charging (disc still spins)
        updateRotation();
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

    public float getDiscRadius() {
        return discRadius;
    }

    public float getDiscThickness() {
        return discThickness;
    }

    public boolean isBoomerang() {
        return boomerang;
    }

    public boolean isReturning() {
        return returning;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.speed = nbt.hasKey("Speed") ? nbt.getFloat("Speed") : 0.5f;
        this.homing = !nbt.hasKey("Homing") || nbt.getBoolean("Homing");
        this.homingStrength = nbt.hasKey("HomingStrength") ? nbt.getFloat("HomingStrength") : 0.15f;
        this.homingRange = nbt.hasKey("HomingRange") ? nbt.getFloat("HomingRange") : 20.0f;
        this.boomerang = nbt.hasKey("Boomerang") && nbt.getBoolean("Boomerang");
        this.boomerangDelay = nbt.hasKey("BoomerangDelay") ? nbt.getInteger("BoomerangDelay") : 40;
        this.discRadius = nbt.hasKey("DiscRadius") ? nbt.getFloat("DiscRadius") : 1.0f;
        this.discThickness = nbt.hasKey("DiscThickness") ? nbt.getFloat("DiscThickness") : 0.2f;
        this.returning = nbt.hasKey("Returning") && nbt.getBoolean("Returning");
        // Charging state
        boolean isCharging = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.charging = isCharging;
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (isCharging ? 1 : 0));
        this.chargeDuration = nbt.hasKey("ChargeDuration") ? nbt.getInteger("ChargeDuration") : 40;
        this.chargeTick = nbt.hasKey("ChargeTick") ? nbt.getInteger("ChargeTick") : 0;
        this.anchorPoint = nbt.hasKey("AnchorPoint") ? AnchorPoint.fromId(nbt.getInteger("AnchorPoint")) : AnchorPoint.FRONT;
        this.targetDiscRadius = nbt.hasKey("TargetDiscRadius") ? nbt.getFloat("TargetDiscRadius") : this.discRadius;
        this.targetDiscThickness = nbt.hasKey("TargetDiscThickness") ? nbt.getFloat("TargetDiscThickness") : this.discThickness;
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("Speed", speed);
        nbt.setBoolean("Homing", homing);
        nbt.setFloat("HomingStrength", homingStrength);
        nbt.setFloat("HomingRange", homingRange);
        nbt.setBoolean("Boomerang", boomerang);
        nbt.setInteger("BoomerangDelay", boomerangDelay);
        nbt.setFloat("DiscRadius", discRadius);
        nbt.setFloat("DiscThickness", discThickness);
        nbt.setBoolean("Returning", returning);
        // Charging state
        nbt.setBoolean("Charging", isCharging());
        nbt.setInteger("ChargeDuration", chargeDuration);
        nbt.setInteger("ChargeTick", chargeTick);
        nbt.setInteger("AnchorPoint", anchorPoint.getId());
        nbt.setFloat("TargetDiscRadius", targetDiscRadius);
        nbt.setFloat("TargetDiscThickness", targetDiscThickness);
    }
}
