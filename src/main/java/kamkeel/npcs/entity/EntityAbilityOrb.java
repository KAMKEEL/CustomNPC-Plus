package kamkeel.npcs.entity;

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

    public EntityAbilityOrb(World world) {
        super(world);
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

    @Override
    protected void updateProjectile() {
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
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("OrbSpeed", speed);
        nbt.setBoolean("Homing", homing);
        nbt.setFloat("HomingStrength", homingStrength);
        nbt.setFloat("HomingRange", homingRange);
    }
}
