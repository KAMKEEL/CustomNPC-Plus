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

    public EntityAbilityDisc(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters.
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
        super(world);

        // Initialize base properties (use discRadius as size for base)
        initProjectile(owner, target, x, y, z,
            discRadius, innerColor, outerColor, outerColorEnabled, outerColorWidth, rotationSpeed,
            damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime);

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

    @Override
    protected void updateRotation() {
        // Disc spins ONLY on Y axis (flat spin like a saw blade)
        this.rotationValY += rotationSpeed;
        // No wobble - keep X and Z at 0 for stable flight
        if (this.rotationValY > 360.0f) this.rotationValY -= 360.0f;
    }

    @Override
    protected void updateProjectile() {
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

            // Boomerang: if we've traveled far without hitting, start returning
            if (boomerang && !returning && !hasHit) {
                double distTraveled = Math.sqrt(
                    (posX - startX) * (posX - startX) +
                    (posY - startY) * (posY - startY) +
                    (posZ - startZ) * (posZ - startZ)
                );
                // Start returning at 80% of max distance or after delay
                if (distTraveled >= maxDistance * 0.8 || ticksSinceMiss >= boomerangDelay) {
                    returning = true;
                }
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
        // If boomerang and returning, don't die from max distance
        if (boomerang && returning) {
            // Die if we get close to owner
            Entity owner = getOwner();
            if (owner != null) {
                double distToOwner = Math.sqrt(
                    (posX - owner.posX) * (posX - owner.posX) +
                    (posY - owner.posY) * (posY - owner.posY) +
                    (posZ - owner.posZ) * (posZ - owner.posZ)
                );
                return distToOwner < 1.0;
            }
        }
        return super.checkMaxDistance();
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
        if (owner == null) {
            this.setDead();
            return;
        }

        double dx = owner.posX - posX;
        double dy = (owner.posY + owner.height * 0.5) - posY;
        double dz = owner.posZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > 0) {
            // Stronger homing when returning
            double returnStrength = homingStrength * 2.0;
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
    }
}
