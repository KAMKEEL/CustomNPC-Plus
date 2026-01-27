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
    private AnchorPoint anchorPoint = AnchorPoint.FRONT;
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
     * Full constructor with all parameters.
     */
    public EntityAbilityOrb(World world, EntityNPCInterface owner, EntityLivingBase target,
                            double x, double y, double z,
                            float orbSize, int innerColor, int outerColor,
                            boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                            float damage, float knockback, float knockbackUp,
                            float speed, boolean homing, float homingStrength, float homingRange,
                            boolean explosive, float explosionRadius, float explosionDamageFalloff,
                            int stunDuration, int slowDuration, int slowLevel,
                            float maxDistance, int maxLifetime) {
        this(world, owner, target, x, y, z,
            orbSize, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp,
            speed, homing, homingStrength, homingRange,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime,
            false, 0.15f, 0.5f, 6); // Default: no lightning, low density, small radius, 6 tick fade
    }

    /**
     * Full constructor with lightning effect support.
     */
    public EntityAbilityOrb(World world, EntityNPCInterface owner, EntityLivingBase target,
                            double x, double y, double z,
                            float orbSize, int innerColor, int outerColor,
                            boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                            float damage, float knockback, float knockbackUp,
                            float speed, boolean homing, float homingStrength, float homingRange,
                            boolean explosive, float explosionRadius, float explosionDamageFalloff,
                            int stunDuration, int slowDuration, int slowLevel,
                            float maxDistance, int maxLifetime,
                            boolean lightningEffect, float lightningDensity, float lightningRadius, int lightningFadeTime) {
        super(world);

        // Initialize base properties with lightning
        initProjectile(owner, target, x, y, z,
            orbSize, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime,
            lightningEffect, lightningDensity, lightningRadius, lightningFadeTime);

        // Orb-specific properties
        this.speed = speed;
        this.homing = homing;
        this.homingStrength = homingStrength;
        this.homingRange = homingRange;

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
            // Fire in NPC's facing direction
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.motionX = -Math.sin(yaw) * Math.cos(pitch) * speed;
            this.motionY = -Math.sin(pitch) * speed;
            this.motionZ = Math.cos(yaw) * Math.cos(pitch) * speed;
        }
    }

    /**
     * Create an orb in charging mode (for windup phase).
     * The orb will grow from 0 to orbSize over chargeDuration ticks.
     * Position follows the owner based on anchor point.
     */
    public static EntityAbilityOrb createCharging(World world, EntityNPCInterface owner, EntityLivingBase target,
                                                   float orbSize, int innerColor, int outerColor,
                                                   boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                                                   float damage, float knockback, float knockbackUp,
                                                   float speed, boolean homing, float homingStrength, float homingRange,
                                                   boolean explosive, float explosionRadius, float explosionDamageFalloff,
                                                   int stunDuration, int slowDuration, int slowLevel,
                                                   float maxDistance, int maxLifetime,
                                                   boolean lightningEffect, float lightningDensity, float lightningRadius, int lightningFadeTime,
                                                   AnchorPoint anchorPoint, int chargeDuration) {
        // Calculate initial position based on anchor point
        Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(owner, anchorPoint);
        double spawnX = spawnPos.xCoord;
        double spawnY = spawnPos.yCoord;
        double spawnZ = spawnPos.zCoord;

        EntityAbilityOrb orb = new EntityAbilityOrb(
            world, owner, target,
            spawnX, spawnY, spawnZ,
            orbSize, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp, speed, homing, homingStrength, homingRange,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel, maxDistance, maxLifetime,
            lightningEffect, lightningDensity, lightningRadius, lightningFadeTime);

        // Set charging state (uses data watcher for client sync)
        orb.setCharging(true);
        orb.chargeDuration = chargeDuration;
        orb.chargeTick = 0;
        orb.anchorPoint = anchorPoint;

        // Store target size and start at 0 for grow effect
        orb.targetSize = orb.size;
        orb.size = 0.01f; // Start very small
        orb.renderCurrentSize = 0.01f;
        orb.prevRenderSize = 0.01f;

        // Clear motion while charging
        orb.motionX = 0;
        orb.motionY = 0;
        orb.motionZ = 0;

        return orb;
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

        // Calculate position based on anchor point
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorPoint);
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
        this.anchorPoint = nbt.hasKey("AnchorPoint") ? AnchorPoint.fromId(nbt.getInteger("AnchorPoint")) : AnchorPoint.FRONT;
        this.targetSize = nbt.hasKey("TargetSize") ? nbt.getFloat("TargetSize") : this.size;
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
        nbt.setInteger("AnchorPoint", anchorPoint.getId());
        nbt.setFloat("TargetSize", targetSize);
    }
}
