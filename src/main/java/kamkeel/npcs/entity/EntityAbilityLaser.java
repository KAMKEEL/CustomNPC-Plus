package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

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

    // Lock vertical direction after firing (only update yaw, keep pitch fixed)
    private boolean lockVerticalDirection = false;

    // Track hit entities to avoid double-damage
    private Set<Integer> hitEntities = new HashSet<>();

    // End point for rendering
    private double endX, endY, endZ;

    // Charging state (during windup)
    private boolean charging = false;
    private int chargeDuration = 40;
    private int chargeTick = 0;
    private float targetSize = 1.0f; // Full size to grow to during charging

    // Data watcher index for charging state (synced to clients)
    private static final int DW_CHARGING = 20;

    public EntityAbilityLaser(World world) {
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
     * Full constructor with all parameters using data classes.
     */
    public EntityAbilityLaser(World world, EntityLivingBase owner, EntityLivingBase target,
                              double x, double y, double z,
                              float laserWidth,
                              EnergyDisplayData display, EnergyCombatData combat,
                              EnergyLightningData lightning, EnergyLifespanData lifespan,
                              EnergyTrajectoryData trajectory, float expansionSpeed, int lingerTicks) {
        super(world);

        // Initialize base properties (laser doesn't rotate)
        initProjectile(owner, target, x, y, z, laserWidth, display, combat, lightning, lifespan, trajectory);
        this.displayData.rotationSpeed = 0.0f;

        // Laser-specific properties
        this.laserWidth = laserWidth;
        this.expansionSpeed = expansionSpeed;
        this.lingerTicks = lingerTicks;

        // Calculate direction toward target or forward
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.getEyeHeight() - 0.4) - y;
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
        if (isCharging()) {
            updateCharging();
            return;
        }

        // Track owner's anchor position and rotation each tick
        // so the laser follows the NPC's facing direction
        updateLaserOriginAndDirection();

        if (!fullyExtended) {
            // Expand the laser
            currentLength += expansionSpeed;

            if (currentLength >= getMaxDistance()) {
                currentLength = getMaxDistance();
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

            // Update end point during linger (origin/direction may still change)
            endX = startX + dirX * currentLength;
            endY = startY + dirY * currentLength;
            endZ = startZ + dirZ * currentLength;

            if (ticksSinceFullExtension >= lingerTicks) {
                this.setDead();
            }
        }
    }

    /**
     * Update laser origin and direction from the owner's current anchor position and rotation.
     * Called each tick so the laser follows the NPC when tracking a target.
     */
    private void updateLaserOriginAndDirection() {
        Entity owner = getOwnerEntity();
        if (owner == null || owner.isDead) return;
        if (!(owner instanceof EntityLivingBase)) return;

        EntityLivingBase livingOwner = (EntityLivingBase) owner;

        // Update origin from anchor point
        if (anchorData != null) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition(livingOwner, anchorData);
            startX = pos.xCoord;
            startY = pos.yCoord;
            startZ = pos.zCoord;
        } else {
            startX = owner.posX;
            startY = owner.posY + owner.height * 0.7;
            startZ = owner.posZ;
        }

        // Update direction from owner's current rotation
        float yaw = (float) Math.toRadians(owner.rotationYaw);
        if (lockVerticalDirection) {
            // Only update horizontal direction (yaw), keep vertical (pitch) fixed
            double horizontalLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
            if (horizontalLen < 0.001) horizontalLen = 1.0;
            dirX = -Math.sin(yaw) * horizontalLen;
            dirZ = Math.cos(yaw) * horizontalLen;
        } else {
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            dirX = -Math.sin(yaw) * Math.cos(pitch);
            dirY = -Math.sin(pitch);
            dirZ = Math.cos(yaw) * Math.cos(pitch);
        }

        // Keep entity positioned at origin
        prevPosX = startX;
        prevPosY = startY;
        prevPosZ = startZ;
        setPosition(startX, startY, startZ);
    }

    public void startMoving(EntityLivingBase target) {
        if (!isCharging()) return;

        setCharging(false);

        // Initialize from current anchor position
        // updateLaserOriginAndDirection() will keep these updated each tick
        startX = posX;
        startY = posY;
        startZ = posZ;

        // Set initial direction from owner's rotation
        Entity owner = getOwnerEntity();
        if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.dirX = -Math.sin(yaw) * Math.cos(pitch);
            this.dirY = -Math.sin(pitch);
            this.dirZ = Math.cos(yaw) * Math.cos(pitch);
        } else if (target != null) {
            double dx = target.posX - startX;
            double dy = (target.posY + target.getEyeHeight() - 0.4) - startY;
            double dz = target.posZ - startZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                this.dirX = dx / len;
                this.dirY = dy / len;
                this.dirZ = dz / len;
            }
        }

        // Initialize end point at start (will expand from here)
        this.endX = startX;
        this.endY = startY;
        this.endZ = startZ;
    }

    private void checkBlockCollision() {
        Vec3 start = Vec3.createVectorHelper(startX, startY, startZ);
        Vec3 end = Vec3.createVectorHelper(endX, endY, endZ);
        // Use full raytrace that doesn't stop at liquids and checks all blocks
        MovingObjectPosition blockHit = worldObj.func_147447_a(start, end, false, true, false);

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

            if (isExplosive()) {
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

    /**
     * Update during charging state - follow owner based on anchor point.
     */
    private void updateCharging() {
        chargeTick++;

        Entity owner = getOwnerEntity();
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

    public float getInterpolatedChargeProgress(float partialTicks) {
        if (chargeDuration <= 0) return 1.0f;
        float prevProgress = Math.max(0, (float) (chargeTick - 1) / chargeDuration);
        float currProgress = Math.min(1.0f, (float) chargeTick / chargeDuration);
        return prevProgress + (currProgress - prevProgress) * partialTicks;
    }

    private void checkEntityCollisionAlongLine() {
        // Expand search AABB by entity sizes to ensure we find all potential targets
        double expand = Math.max(laserWidth, 1.0);
        double minX = Math.min(startX, endX) - expand;
        double minY = Math.min(startY, endY) - expand;
        double minZ = Math.min(startZ, endZ) - expand;
        double maxX = Math.max(startX, endX) + expand;
        double maxY = Math.max(startY, endY) + expand;
        double maxZ = Math.max(startZ, endZ) + expand;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;
            if (hitEntities.contains(entity.getEntityId())) continue;

            // Check if entity's bounding box intersects with the laser line
            if (isEntityOnLine(entity)) {
                hitEntities.add(entity.getEntityId());
                applyDamage(entity);

                // Piercing - don't stop, continue to next entity
            }
        }
    }

    /**
     * Check if an entity's bounding box intersects with the laser line.
     * Uses separate XZ (horizontal) and Y (vertical) checks against
     * the entity's full bounding box rather than just its center point.
     */
    private boolean isEntityOnLine(EntityLivingBase entity) {
        double ex = entity.posX;
        double ez = entity.posZ;

        // Project entity XZ position onto the laser line direction
        double vx = ex - startX;
        double vz = ez - startZ;
        // Project using horizontal direction components only
        double dirLenXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double dot;
        if (dirLenXZ > 0.001) {
            dot = (vx * dirX + vz * dirZ) / (dirLenXZ * dirLenXZ) * dirLenXZ;
            // Remap to full 3D parameter
            dot = (vx * dirX + vz * dirZ + (entity.posY + entity.height * 0.5 - startY) * dirY);
        } else {
            // Laser fires nearly straight up/down - use full 3D projection
            dot = vx * dirX + (entity.posY + entity.height * 0.5 - startY) * dirY + vz * dirZ;
        }
        dot = Math.max(0, Math.min(dot, currentLength));

        // Closest point on laser line at this parameter
        double closestX = startX + dirX * dot;
        double closestY = startY + dirY * dot;
        double closestZ = startZ + dirZ * dot;

        // XZ distance check: entity center vs laser line point
        // Use minimum collision width so visually thin lasers still hit reliably
        double xzDistSq = (ex - closestX) * (ex - closestX) + (ez - closestZ) * (ez - closestZ);
        double effectiveWidth = Math.max(laserWidth, 0.5);
        double hitRadiusXZ = effectiveWidth * 0.5 + entity.width * 0.5;
        if (xzDistSq > hitRadiusXZ * hitRadiusXZ) return false;

        // Y overlap check: laser point vs entity's full height range
        double entityMinY = entity.boundingBox.minY;
        double entityMaxY = entity.boundingBox.maxY;
        double laserHalfWidth = effectiveWidth * 0.5;

        // Check if laser Y is within entity's height range (expanded by laser half-width)
        if (closestY < entityMinY - laserHalfWidth) return false;
        if (closestY > entityMaxY + laserHalfWidth) return false;

        return true;
    }

    // ==================== GETTERS FOR RENDERER ====================

    public void setLockVerticalDirection(boolean lock) {
        this.lockVerticalDirection = lock;
    }

    public boolean isLockVerticalDirection() {
        return lockVerticalDirection;
    }

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

        // Charging state
        boolean isCharging = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.charging = isCharging;
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (isCharging ? 1 : 0));
        this.chargeDuration = nbt.hasKey("ChargeDuration") ? nbt.getInteger("ChargeDuration") : 40;
        this.chargeTick = nbt.hasKey("ChargeTick") ? nbt.getInteger("ChargeTick") : 0;
        this.targetSize = nbt.hasKey("TargetSize") ? nbt.getFloat("TargetSize") : this.size;
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

        // Charging state
        nbt.setBoolean("Charging", isCharging());
        nbt.setInteger("ChargeDuration", chargeDuration);
        nbt.setInteger("ChargeTick", chargeTick);
        nbt.setFloat("TargetSize", targetSize);
    }

    /**
     * Setup this laser in preview mode for GUI display.
     * Laser doesn't have charging state - spawns at active phase and fires immediately.
     */
    public void setupPreview(EntityLivingBase owner, float laserWidth, EnergyDisplayData display,
                             EnergyLightningData lightning, float expansionSpeed, float maxDistance) {
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);

        // Set visual properties
        this.laserWidth = laserWidth;
        this.size = laserWidth;
        this.displayData = display;
        this.expansionSpeed = expansionSpeed;
        this.lifespanData.maxDistance = Math.min(maxDistance, 5.0f); // Limit for GUI preview
        this.lightningData = lightning;

        // Position at chest height
        double x = owner.posX;
        double y = owner.posY + owner.height * 0.7;
        double z = owner.posZ;
        this.setPosition(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        // Fire in owner's facing direction
        float yaw = (float) Math.toRadians(owner.rotationYaw);
        this.dirX = -Math.sin(yaw);
        this.dirY = 0;
        this.dirZ = Math.cos(yaw);

        // Initialize end point (same as start, will expand)
        this.endX = x;
        this.endY = y;
        this.endZ = z;
    }
}
