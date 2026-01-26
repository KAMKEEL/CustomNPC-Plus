package kamkeel.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Laser projectile - fast expanding thin line that pierces through multiple targets.
 * No homing, travels in a straight line from origin to max distance.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityLaser extends EntityAbilityProjectile {

    // Laser-specific properties
    private float laserWidth = 0.2f;
    private float expansionSpeed = 2.0f; // Blocks per tick
    private int lingerTicks = 10; // How long laser stays visible after reaching max

    // State
    private float currentLength = 0.0f;
    private boolean fullyExtended = false;
    private int ticksSinceFullExtension = 0;

    // Direction (normalized)
    private double dirX, dirY, dirZ;

    // Track hit entities to avoid double-damage
    private Set<Integer> hitEntities = new HashSet<>();

    // End point for rendering
    private double endX, endY, endZ;

    public EntityAbilityLaser(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters.
     */
    public EntityAbilityLaser(World world, EntityNPCInterface owner, EntityLivingBase target,
                               double x, double y, double z,
                               float laserWidth, int innerColor, int outerColor,
                               float damage, float knockback, float knockbackUp,
                               float expansionSpeed, int lingerTicks,
                               boolean explosive, float explosionRadius, float explosionDamageFalloff,
                               int stunDuration, int slowDuration, int slowLevel,
                               float maxDistance, int maxLifetime) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z,
            laserWidth, innerColor, outerColor, 0.0f, // No rotation for laser
            damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime);

        // Laser-specific properties
        this.laserWidth = laserWidth;
        this.expansionSpeed = expansionSpeed;
        this.lingerTicks = lingerTicks;

        // Calculate direction toward target or forward
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.height * 0.5) - y;
            double dz = target.posZ - z;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                this.dirX = dx / len;
                this.dirY = dy / len;
                this.dirZ = dz / len;
            }
        } else {
            // Fire in NPC's facing direction
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.dirX = -Math.sin(yaw) * Math.cos(pitch);
            this.dirY = -Math.sin(pitch);
            this.dirZ = Math.cos(yaw) * Math.cos(pitch);
        }

        // Initialize end point
        this.endX = x;
        this.endY = y;
        this.endZ = z;
    }

    @Override
    protected void updateRotation() {
        // Laser doesn't rotate
    }

    @Override
    protected boolean checkMaxDistance() {
        // Laser handles distance differently - it expands to max then lingers
        return false;
    }

    @Override
    protected void updateProjectile() {
        if (!fullyExtended) {
            // Expand the laser
            currentLength += expansionSpeed;

            if (currentLength >= maxDistance) {
                currentLength = maxDistance;
                fullyExtended = true;
            }

            // Update end point
            endX = startX + dirX * currentLength;
            endY = startY + dirY * currentLength;
            endZ = startZ + dirZ * currentLength;

            // Check for block collision along the new segment
            if (!worldObj.isRemote) {
                checkBlockCollision();
                checkEntityCollisionAlongLine();
            }
        } else {
            // Laser is fully extended, count down linger time
            ticksSinceFullExtension++;
            if (ticksSinceFullExtension >= lingerTicks) {
                this.setDead();
            }
        }
    }

    private void checkBlockCollision() {
        Vec3 start = Vec3.createVectorHelper(startX, startY, startZ);
        Vec3 end = Vec3.createVectorHelper(endX, endY, endZ);
        MovingObjectPosition blockHit = worldObj.rayTraceBlocks(start, end);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            // Laser stops at block
            currentLength = (float) Math.sqrt(
                (blockHit.hitVec.xCoord - startX) * (blockHit.hitVec.xCoord - startX) +
                (blockHit.hitVec.yCoord - startY) * (blockHit.hitVec.yCoord - startY) +
                (blockHit.hitVec.zCoord - startZ) * (blockHit.hitVec.zCoord - startZ)
            );
            endX = blockHit.hitVec.xCoord;
            endY = blockHit.hitVec.yCoord;
            endZ = blockHit.hitVec.zCoord;
            fullyExtended = true;

            if (explosive) {
                // Explode at hit point
                double oldPosX = posX;
                double oldPosY = posY;
                double oldPosZ = posZ;
                posX = endX;
                posY = endY;
                posZ = endZ;
                doExplosion();
                posX = oldPosX;
                posY = oldPosY;
                posZ = oldPosZ;
            }
        }
    }

    private void checkEntityCollisionAlongLine() {
        // Check collision along the entire laser line
        // Use a slightly expanded AABB for the full line
        double minX = Math.min(startX, endX) - laserWidth;
        double minY = Math.min(startY, endY) - laserWidth;
        double minZ = Math.min(startZ, endZ) - laserWidth;
        double maxX = Math.max(startX, endX) + laserWidth;
        double maxY = Math.max(startY, endY) + laserWidth;
        double maxZ = Math.max(startZ, endZ) + laserWidth;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;
            if (hitEntities.contains(entity.getEntityId())) continue;

            // Check if entity is close to the laser line
            if (isEntityOnLine(entity)) {
                hitEntities.add(entity.getEntityId());
                applyDamage(entity);

                // Piercing - don't stop, continue to next entity
            }
        }
    }

    /**
     * Check if an entity intersects with the laser line.
     */
    private boolean isEntityOnLine(EntityLivingBase entity) {
        // Get entity center
        double ex = entity.posX;
        double ey = entity.posY + entity.height * 0.5;
        double ez = entity.posZ;

        // Vector from start to entity
        double vx = ex - startX;
        double vy = ey - startY;
        double vz = ez - startZ;

        // Project onto line direction
        double dot = vx * dirX + vy * dirY + vz * dirZ;

        // Clamp to line segment
        dot = Math.max(0, Math.min(dot, currentLength));

        // Closest point on line
        double closestX = startX + dirX * dot;
        double closestY = startY + dirY * dot;
        double closestZ = startZ + dirZ * dot;

        // Distance from entity to closest point
        double distSq = (ex - closestX) * (ex - closestX) +
                        (ey - closestY) * (ey - closestY) +
                        (ez - closestZ) * (ez - closestZ);

        // Hit if within laser width + entity radius
        double hitRadius = laserWidth * 0.5 + entity.width * 0.5;
        return distSq <= hitRadius * hitRadius;
    }

    // ==================== GETTERS FOR RENDERER ====================

    public float getLaserWidth() {
        return laserWidth;
    }

    public float getCurrentLength() {
        return currentLength;
    }

    public boolean isFullyExtended() {
        return fullyExtended;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getEndZ() {
        return endZ;
    }

    public double getDirX() {
        return dirX;
    }

    public double getDirY() {
        return dirY;
    }

    public double getDirZ() {
        return dirZ;
    }

    /**
     * Get the alpha for fade-out effect during linger.
     */
    public float getLingerAlpha() {
        if (!fullyExtended) return 1.0f;
        return 1.0f - ((float) ticksSinceFullExtension / lingerTicks);
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.laserWidth = nbt.hasKey("LaserWidth") ? nbt.getFloat("LaserWidth") : 0.2f;
        this.expansionSpeed = nbt.hasKey("ExpansionSpeed") ? nbt.getFloat("ExpansionSpeed") : 2.0f;
        this.lingerTicks = nbt.hasKey("LingerTicks") ? nbt.getInteger("LingerTicks") : 10;
        this.dirX = nbt.getDouble("DirX");
        this.dirY = nbt.getDouble("DirY");
        this.dirZ = nbt.getDouble("DirZ");
        this.currentLength = nbt.getFloat("CurrentLength");
        this.fullyExtended = nbt.getBoolean("FullyExtended");
        this.endX = nbt.getDouble("EndX");
        this.endY = nbt.getDouble("EndY");
        this.endZ = nbt.getDouble("EndZ");
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("LaserWidth", laserWidth);
        nbt.setFloat("ExpansionSpeed", expansionSpeed);
        nbt.setInteger("LingerTicks", lingerTicks);
        nbt.setDouble("DirX", dirX);
        nbt.setDouble("DirY", dirY);
        nbt.setDouble("DirZ", dirZ);
        nbt.setFloat("CurrentLength", currentLength);
        nbt.setBoolean("FullyExtended", fullyExtended);
        nbt.setDouble("EndX", endX);
        nbt.setDouble("EndY", endY);
        nbt.setDouble("EndZ", endZ);
    }
}
