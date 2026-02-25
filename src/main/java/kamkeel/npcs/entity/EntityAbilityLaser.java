package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.EventHooks;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Laser projectile - fast expanding thin line.
 * No homing, travels in a straight line from origin to max distance.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityLaser extends EntityEnergyProjectile {

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
    private int reflectedLockTicks = 0;

    // End point for rendering
    private double endX, endY, endZ;

    public EntityAbilityLaser(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters using data classes.
     */
    public EntityAbilityLaser(World world, EntityLivingBase owner, EntityLivingBase target,
                              double x, double y, double z,
                              float laserWidth,
                              EnergyDisplayData display, EnergyCombatData combat,
                              EnergyLightningData lightning, EnergyLifespanData lifespan,
                              float expansionSpeed, int lingerTicks) {
        super(world);

        // Initialize base properties (laser doesn't rotate)
        initProjectile(owner, target, x, y, z, laserWidth, display, combat, lightning, lifespan);
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
            // Fall back to owner look direction so launch matches visual aim.
            Vec3 look = owner == null ? null : owner.getLookVec();
            if (look != null) {
                this.dirX = look.xCoord;
                this.dirY = look.yCoord;
                this.dirZ = look.zCoord;
            } else if (owner != null) {
                float yaw = (float) Math.toRadians(owner.rotationYaw);
                float pitch = (float) Math.toRadians(owner.rotationPitch);
                this.dirX = -Math.sin(yaw) * Math.cos(pitch);
                this.dirY = -Math.sin(pitch);
                this.dirZ = Math.cos(yaw) * Math.cos(pitch);
            } else {
                this.dirX = 1.0;
                this.dirY = 0.0;
                this.dirZ = 0.0;
            }
        }

        // Initialize end point
        this.endX = x;
        this.endY = y;
        this.endZ = z;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double range = Math.max(128.0D, currentLength * 2.0D + 64.0D);
        return distance < range * range;
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

        // After reflection, hold trajectory briefly so the rebound is visible.
        // Once reflected, never resume owner-tracking — the laser flies in a fixed direction.
        if (reflectedLockTicks > 0) {
            reflectedLockTicks--;
        } else if (!reflected) {
            // Track owner's anchor position and rotation each tick
            // so the laser follows the NPC's facing direction
            updateLaserOriginAndDirection();
        }

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
                // Re-evaluate barriers against the current beam segment before entity hits.
                // This prevents first-tick pass-through where the pre-update barrier check
                // still sees a zero-length beam.
                if (checkBarrierCollisionForCurrentSegment()) {
                    return;
                }
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
                if (!worldObj.isRemote) {
                    EventHooks.onEnergyProjectileExpired(this);
                }
                this.setDead();
            }
        }
    }

    /**
     * Update laser origin and direction from the owner's current look rotation.
     * Called each tick so the laser origin stays centered on look vector.
     */
    private void updateLaserOriginAndDirection() {
        Entity owner = getOwnerEntity();
        if (owner == null || !(owner instanceof EntityLivingBase)) {
            // On client, fall back to entity tracker interpolation when owner not loaded
            if (worldObj != null && worldObj.isRemote) {
                handleClientInterpolation();
            }
            return;
        }
        EntityLivingBase livingOwner = (EntityLivingBase) owner;

        // Use owner look vector (head/eye aim) for direction updates so hit logic matches visuals.
        Vec3 look = livingOwner.getLookVec();
        if (look == null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            look = Vec3.createVectorHelper(
                -Math.sin(yaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch)
            );
        }

        if (lockVerticalDirection) {
            // Keep current vertical component, but align horizontal direction to current look.
            double lookHorizontalLen = Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord);
            if (lookHorizontalLen > 0.0001) {
                double currentHorizontalLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
                if (currentHorizontalLen < 0.0001) {
                    currentHorizontalLen = Math.sqrt(Math.max(0.0001, 1.0 - dirY * dirY));
                }
                dirX = (look.xCoord / lookHorizontalLen) * currentHorizontalLen;
                dirZ = (look.zCoord / lookHorizontalLen) * currentHorizontalLen;
            }
        } else {
            dirX = look.xCoord;
            dirY = look.yCoord;
            dirZ = look.zCoord;
        }

        // Keep direction normalized for consistent expansion and collision math.
        Vec3 direction = Vec3.createVectorHelper(dirX, dirY, dirZ);
        double dirLen = Math.sqrt(direction.xCoord * direction.xCoord + direction.yCoord * direction.yCoord + direction.zCoord * direction.zCoord);
        if (dirLen > 0.0001) {
            direction = Vec3.createVectorHelper(
                direction.xCoord / dirLen,
                direction.yCoord / dirLen,
                direction.zCoord / dirLen
            );
        } else {
            direction = look != null ? look : Vec3.createVectorHelper(1.0, 0.0, 0.0);
        }

        dirX = direction.xCoord;
        dirY = direction.yCoord;
        dirZ = direction.zCoord;

        // Keep origin centered on look vector while active and clear owner bbox.
        setLookVectorLaunchPosition(livingOwner, direction, false);
        syncStartPositionToCurrent();
        syncPositionStateToCurrent(false);
    }

    public void startMoving(EntityLivingBase target) {
        beginLookVectorLaunch(false);

        currentLength = 0.0f;
        fullyExtended = false;
        ticksSinceFullExtension = 0;

        // Set initial direction: target-based when available, otherwise owner look vector.
        Vec3 look = getOwnerLookVector();
        if (!setDirectionTowardTarget(target, startX, startY, startZ)) {
            if (look != null) {
                dirX = look.xCoord;
                dirY = look.yCoord;
                dirZ = look.zCoord;
            } else {
                dirX = 1.0;
                dirY = 0.0;
                dirZ = 0.0;
            }
        }

        // Initialize end point at start (will expand from here)
        this.endX = startX;
        this.endY = startY;
        this.endZ = startZ;
    }

    private boolean setDirectionTowardTarget(EntityLivingBase target, double sourceX, double sourceY, double sourceZ) {
        if (target == null) return false;
        double dx = target.posX - sourceX;
        double dy = (target.posY + target.getEyeHeight() - 0.4) - sourceY;
        double dz = target.posZ - sourceZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len <= 0.0001) return false;

        dirX = dx / len;
        dirY = dy / len;
        dirZ = dz / len;
        return true;
    }

    private void checkBlockCollision() {
        MovingObjectPosition blockHit = rayTraceBlocks(startX, startY, startZ, endX, endY, endZ);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!worldObj.isRemote) {
                EventHooks.onEnergyProjectileBlockImpact(this, blockHit.blockX, blockHit.blockY, blockHit.blockZ);
            }
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
            if (!canHitEntityNow(entity)) continue;

            // Check if entity's bounding box intersects with the laser line
            if (isEntityOnLine(entity)) {
                if (processEntityHit(entity, entity.posX, entity.posY + entity.height * 0.5, entity.posZ)) {
                    // Stop at impact point and terminate
                    double dx = entity.posX - startX;
                    double dy = (entity.posY + entity.height * 0.5) - startY;
                    double dz = entity.posZ - startZ;
                    float impactDist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    currentLength = Math.min(impactDist, currentLength);
                    endX = startX + dirX * currentLength;
                    endY = startY + dirY * currentLength;
                    endZ = startZ + dirZ * currentLength;
                    return;
                }

                // PIERCE/MULTI continue through remaining entities.
            }
        }
    }

    /**
     * Check if an entity's bounding box intersects with the laser line.
     * Uses a swept segment vs expanded AABB intercept test for reliable hits.
     */
    private boolean isEntityOnLine(EntityLivingBase entity) {
        AxisAlignedBB bb = entity.boundingBox;
        if (bb == null) return false;

        // Minimum effective width keeps thin lasers from visually clipping targets without hitting.
        double effectiveWidth = Math.max(laserWidth, 0.65);
        double expand = effectiveWidth * 0.5;
        AxisAlignedBB expanded = bb.expand(expand, expand, expand);

        Vec3 segmentStart = Vec3.createVectorHelper(startX, startY, startZ);
        Vec3 segmentEnd = Vec3.createVectorHelper(endX, endY, endZ);
        MovingObjectPosition intercept = expanded.calculateIntercept(segmentStart, segmentEnd);
        if (intercept != null) return true;

        // Handle edge case where the segment starts or ends already inside the target volume.
        return expanded.isVecInside(segmentStart) || expanded.isVecInside(segmentEnd);
    }

    // ==================== BARRIER COLLISION ====================

    /**
     * Override barrier collision for lasers. The base class uses projectile position/velocity
     * which doesn't work for lasers (posX/Y/Z stays at origin, motionX/Y/Z is zero).
     * Instead, performs line-sphere intersection against domes.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean checkBarrierCollision() {
        return checkBarrierCollisionInternal(false);
    }

    private boolean checkBarrierCollisionForCurrentSegment() {
        return checkBarrierCollisionInternal(true);
    }

    @SuppressWarnings("unchecked")
    private boolean checkBarrierCollisionInternal(boolean allowWhenFullyExtended) {
        if (currentLength <= 0) return false;
        if (fullyExtended && !allowWhenFullyExtended) return false;

        List<EntityAbilityBarrier> barriers = EntityAbilityBarrier.getActiveBarriers(worldObj);
        for (EntityAbilityBarrier barrier : barriers) {
            if (barrier.isDead || barrier.isCharging()) continue;
            if (barrier.getOwnerEntityId() == this.ownerEntityId) continue;

            // Same-faction NPC check
            Entity bOwner = barrier.getOwnerEntity();
            Entity lOwner = this.getOwnerEntity();
            if (bOwner instanceof EntityNPCInterface && lOwner instanceof EntityNPCInterface) {
                if (((EntityNPCInterface) bOwner).faction.id == ((EntityNPCInterface) lOwner).faction.id) {
                    continue;
                }
            }

            if (barrier instanceof EntityAbilityDome) {
                EntityAbilityDome dome = (EntityAbilityDome) barrier;
                float intersectDist = getLineSphereIntersection(dome);
                if (intersectDist >= 0) {
                    float damage = getModifiedDamage();
                    EntityAbilityBarrier.ProjectileHitOutcome hitOutcome = dome.onProjectileHitResolved(this, damage);
                    if (hitOutcome == null || hitOutcome.result == EntityAbilityBarrier.ProjectileHitResult.PASS) {
                        continue;
                    }

                    if (hitOutcome.result != EntityAbilityBarrier.ProjectileHitResult.BROKEN) {
                        currentLength = Math.max(0.0f, intersectDist);
                        endX = startX + dirX * currentLength;
                        endY = startY + dirY * currentLength;
                        endZ = startZ + dirZ * currentLength;
                    }

                    if (handleBarrierHitOutcome(dome, hitOutcome, damage)) {
                        return true;
                    }
                    return false;
                }
            } else {
                // For line-based lasers, use a swept segment against generic barrier checks.
                // Panel/Dome logic expects previous+current positions and motion; map those
                // to [start -> end] each tick.
                double segMotionX = endX - startX;
                double segMotionY = endY - startY;
                double segMotionZ = endZ - startZ;
                boolean incoming = barrier.isIncomingGenericProjectile(
                    endX, endY, endZ,
                    segMotionX, segMotionY, segMotionZ,
                    startX, startY, startZ,
                    this.ownerEntityId
                );

                if (incoming) {
                    float damage = getModifiedDamage();
                    EntityAbilityBarrier.ProjectileHitOutcome hitOutcome = barrier.onProjectileHitResolved(this, damage);
                    if (hitOutcome == null || hitOutcome.result == EntityAbilityBarrier.ProjectileHitResult.PASS) {
                        continue;
                    }

                    if (hitOutcome.result != EntityAbilityBarrier.ProjectileHitResult.BROKEN) {
                        // Keep current segment length at the exact collision tick position.
                        endX = startX + dirX * currentLength;
                        endY = startY + dirY * currentLength;
                        endZ = startZ + dirZ * currentLength;
                    }

                    if (handleBarrierHitOutcome(barrier, hitOutcome, damage)) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean reflectFromBarrier(EntityAbilityBarrier barrier, float reflectStrengthPct) {
        if (barrier == null) {
            return false;
        }

        double vx = dirX;
        double vy = dirY;
        double vz = dirZ;
        double vLenSq = vx * vx + vy * vy + vz * vz;
        if (vLenSq < 1.0e-8) {
            return false;
        }

        double[] normal = getBarrierImpactNormal(barrier, vx, vy, vz);
        if (normal == null) {
            return false;
        }

        double dot = vx * normal[0] + vy * normal[1] + vz * normal[2];
        if (dot > 0.0) {
            normal[0] = -normal[0];
            normal[1] = -normal[1];
            normal[2] = -normal[2];
            dot = vx * normal[0] + vy * normal[1] + vz * normal[2];
        }

        double rx = vx - 2.0 * dot * normal[0];
        double ry = vy - 2.0 * dot * normal[1];
        double rz = vz - 2.0 * dot * normal[2];
        if (ry < -0.06) {
            ry = Math.max(0.08, Math.abs(ry) * 0.35);
        }
        double rLen = Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rLen < 1.0e-8) {
            rx = -vx;
            ry = -vy;
            rz = -vz;
            rLen = Math.sqrt(rx * rx + ry * ry + rz * rz);
            if (rLen < 1.0e-8) {
                return false;
            }
        }

        dirX = rx / rLen;
        dirY = ry / rLen;
        dirZ = rz / rLen;

        // Start a fresh segment at the impact edge to make the rebound visible.
        startX = endX;
        startY = endY;
        startZ = endZ;
        setPosition(startX, startY, startZ);
        syncPositionStateToCurrent(true);

        currentLength = 0.0f;
        fullyExtended = false;
        ticksSinceFullExtension = 0;
        endX = startX;
        endY = startY;
        endZ = startZ;

        reflectedLockTicks = Math.max(reflectedLockTicks, 10);
        barrierImpactPauseTicks = 0;
        barrierImpactDestroyOnResume = false;

        Entity barrierOwner = barrier.getOwnerEntity();
        if (barrierOwner != null) {
            setOwnerEntityId(barrierOwner.getEntityId());
            trackProjectile(this);
        }
        setTargetEntityId(-1);

        setInnerColor(barrier.getInnerColor());
        setOuterColor(barrier.getOuterColor());

        float clampedStrength = Math.max(0.0f, Math.min(100.0f, reflectStrengthPct));
        float reducedDamage = getDamage() * (1.0f - clampedStrength / 100.0f);
        setCombatDamage(Math.max(0.0f, reducedDamage));

        hitOnceEntities.clear();
        lastHitTickByEntity.clear();
        return true;
    }

    /**
     * Line-sphere intersection between the laser line segment and a dome.
     * Returns the distance along the laser direction to the first entry point,
     * or -1 if no intersection from outside.
     */
    private float getLineSphereIntersection(EntityAbilityDome dome) {
        double cx = dome.posX;
        double cy = dome.posY;
        double cz = dome.posZ;
        float radius = dome.getDomeRadius();

        // If laser origin is inside the dome, don't block (outgoing)
        double ocX = startX - cx;
        double ocY = startY - cy;
        double ocZ = startZ - cz;
        double originDistSq = ocX * ocX + ocY * ocY + ocZ * ocZ;
        if (originDistSq < (double) radius * radius) return -1;

        // Solve: |start + t*dir - C|^2 = R^2
        // a*t^2 + b*t + c = 0
        double a = dirX * dirX + dirY * dirY + dirZ * dirZ;
        double b = 2.0 * (dirX * ocX + dirY * ocY + dirZ * ocZ);
        double c = originDistSq - (double) radius * radius;

        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0) return -1;

        double sqrtD = Math.sqrt(discriminant);
        double t1 = (-b - sqrtD) / (2.0 * a); // Entry point

        if (t1 >= 0 && t1 <= currentLength) {
            return (float) t1;
        }

        return -1;
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

    public void setLaserWidth(float width) {
        this.laserWidth = width;
    }

    public float getExpansionSpeed() {
        return expansionSpeed;
    }

    public void setExpansionSpeed(float speed) {
        this.expansionSpeed = speed;
    }

    public int getLingerTicks() {
        return lingerTicks;
    }

    public void setLingerTicks(int ticks) {
        this.lingerTicks = ticks;
    }

    public void setDirection(double x, double y, double z) {
        this.dirX = x;
        this.dirY = y;
        this.dirZ = z;
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
        if (lingerTicks <= 0) return 0.0f;
        return Math.max(0.0f, 1.0f - ((float) ticksSinceFullExtension / lingerTicks));
    }

    @Override
    protected float getLaunchClearanceRadius() {
        return Math.max(0.1f, laserWidth * 0.5f);
    }

    @Override
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration) {
        // Ensure charging visuals grow from a small orb based on laser width.
        this.size = laserWidth;
        this.currentLength = 0.0f;
        this.fullyExtended = false;
        this.ticksSinceFullExtension = 0;
        super.setupCharging(anchor, chargeDuration);
        this.endX = posX;
        this.endY = posY;
        this.endZ = posZ;
    }

    // ==================== REFLECTION SYNC ====================

    @Override
    protected void writeProjectileReflectionData(NBTTagCompound nbt) {
        nbt.setDouble("DirX", dirX);
        nbt.setDouble("DirY", dirY);
        nbt.setDouble("DirZ", dirZ);
        nbt.setDouble("StartX", startX);
        nbt.setDouble("StartY", startY);
        nbt.setDouble("StartZ", startZ);
        nbt.setFloat("CurrentLength", currentLength);
        nbt.setDouble("EndX", endX);
        nbt.setDouble("EndY", endY);
        nbt.setDouble("EndZ", endZ);
        nbt.setInteger("ReflectedLockTicks", reflectedLockTicks);
        nbt.setBoolean("FullyExtended", fullyExtended);
    }

    @Override
    protected void applyProjectileReflectionData(NBTTagCompound nbt) {
        dirX = nbt.getDouble("DirX");
        dirY = nbt.getDouble("DirY");
        dirZ = nbt.getDouble("DirZ");
        startX = nbt.getDouble("StartX");
        startY = nbt.getDouble("StartY");
        startZ = nbt.getDouble("StartZ");
        currentLength = nbt.getFloat("CurrentLength");
        endX = nbt.getDouble("EndX");
        endY = nbt.getDouble("EndY");
        endZ = nbt.getDouble("EndZ");
        reflectedLockTicks = nbt.getInteger("ReflectedLockTicks");
        fullyExtended = nbt.getBoolean("FullyExtended");
        ticksSinceFullExtension = 0;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.laserWidth = sanitize(nbt.hasKey("LaserWidth") ? nbt.getFloat("LaserWidth") : 0.2f, 0.2f, MAX_ENTITY_SIZE);
        this.expansionSpeed = nbt.hasKey("ExpansionSpeed") ? nbt.getFloat("ExpansionSpeed") : 2.0f;
        if (Float.isNaN(expansionSpeed) || Float.isInfinite(expansionSpeed) || expansionSpeed <= 0) expansionSpeed = 2.0f;
        this.lingerTicks = nbt.hasKey("LingerTicks") ? nbt.getInteger("LingerTicks") : 10;
        if (lingerTicks <= 0) lingerTicks = 1;
        this.lockVerticalDirection = nbt.getBoolean("LockVerticalDir");
        this.ticksSinceFullExtension = nbt.getInteger("TicksSinceExtended");
        this.dirX = nbt.getDouble("DirX");
        this.dirY = nbt.getDouble("DirY");
        this.dirZ = nbt.getDouble("DirZ");
        this.currentLength = nbt.getFloat("CurrentLength");
        this.fullyExtended = nbt.getBoolean("FullyExtended");
        this.endX = nbt.getDouble("EndX");
        this.endY = nbt.getDouble("EndY");
        this.endZ = nbt.getDouble("EndZ");

        readChargingNBT(nbt);
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("LaserWidth", laserWidth);
        nbt.setFloat("ExpansionSpeed", expansionSpeed);
        nbt.setInteger("LingerTicks", lingerTicks);
        nbt.setBoolean("LockVerticalDir", lockVerticalDirection);
        nbt.setInteger("TicksSinceExtended", ticksSinceFullExtension);
        nbt.setDouble("DirX", dirX);
        nbt.setDouble("DirY", dirY);
        nbt.setDouble("DirZ", dirZ);
        nbt.setFloat("CurrentLength", currentLength);
        nbt.setBoolean("FullyExtended", fullyExtended);
        nbt.setDouble("EndX", endX);
        nbt.setDouble("EndY", endY);
        nbt.setDouble("EndZ", endZ);

        writeChargingNBT(nbt);
    }

    /**
     * Setup this laser in preview mode for GUI display.
     * Uses charging-orb visuals during windup before firing.
     */
    public void setupPreview(EntityLivingBase owner, float laserWidth, EnergyDisplayData display,
                             EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration,
                             float expansionSpeed, float maxDistance) {
        setupPreviewState(owner, display, lightning, anchor, chargeDuration);

        // Laser-specific visual/behavior state.
        this.laserWidth = laserWidth;
        this.expansionSpeed = expansionSpeed;
        this.lifespanData.maxDistance = Math.min(maxDistance, 5.0f); // Limit for GUI preview
        this.currentLength = 0.0f;
        this.fullyExtended = false;
        this.ticksSinceFullExtension = 0;

        // Charge as an orb from tiny size to laser width while anchored.
        this.targetSize = laserWidth;
        setVisualSize(0.01f);
        setChargeOriginFromAnchor(owner, anchorData);
        clearMotion();
        this.endX = posX;
        this.endY = posY;
        this.endZ = posZ;

        // Precompute initial direction from owner look for launch.
        Vec3 look = owner == null ? null : owner.getLookVec();
        if (look == null) {
            if (owner != null) {
                float yaw = (float) Math.toRadians(owner.rotationYaw);
                float pitch = (float) Math.toRadians(owner.rotationPitch);
                look = Vec3.createVectorHelper(
                    -Math.sin(yaw) * Math.cos(pitch),
                    -Math.sin(pitch),
                    Math.cos(yaw) * Math.cos(pitch)
                );
            } else {
                look = Vec3.createVectorHelper(1.0, 0.0, 0.0);
            }
        }
        this.dirX = look.xCoord;
        this.dirY = look.yCoord;
        this.dirZ = look.zCoord;
    }
}
