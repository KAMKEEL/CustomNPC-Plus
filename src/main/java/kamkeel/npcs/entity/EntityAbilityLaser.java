package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.type.energy.AbilityEnergyProjectile;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.util.AnchorPointHelper;
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
 * Laser entity - sweeping beam that follows the caster's look vector.
 * Expands from origin at expansionSpeed until reaching maxLength,
 * then stays active until maxLifetime expires. Always tracks the owner's
 * look direction so the beam can be swept mid-shot.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityLaser extends EntityEnergyProjectile {

    // Laser-specific properties
    private float laserWidth = 0.2f;
    private float expansionSpeed = 2.0f; // Blocks per tick
    private float maxLength = 32.0f; // Maximum beam reach in blocks

    // State
    private float desiredLength = 0.0f; // Intended beam reach (grows to maxLength)
    private float currentLength = 0.0f; // Actual length after block truncation (visual + collision)
    private boolean fullyExtended = false;

    // Direction (normalized)
    private double dirX, dirY, dirZ;

    // End point for rendering
    private double endX, endY, endZ;

    // Block hit deduplication — prevents event/explosion spam every tick
    private int lastBlockHitX = Integer.MIN_VALUE;
    private int lastBlockHitY = Integer.MIN_VALUE;
    private int lastBlockHitZ = Integer.MIN_VALUE;
    private int lastExplosionTick = -100;
    private static final int EXPLOSION_COOLDOWN = 10; // ticks between explosive impacts

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
                              float expansionSpeed, float maxLength) {
        super(world);

        initProjectile(owner, target, x, y, z, laserWidth, display, combat, lightning, lifespan);

        // Laser-specific properties
        this.laserWidth = laserWidth;
        this.expansionSpeed = expansionSpeed;
        this.maxLength = maxLength;

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
        // Beam rotation is handled in the renderer via perpendicular vector rotation
    }

    @Override
    protected boolean checkMaxDistance() {
        // Laser handles distance via maxLength, not max distance
        return false;
    }

    @Override
    protected void updateProjectile() {
        if (isCharging()) {
            updateCharging();
            return;
        }

        // Origin/direction tracking runs on BOTH sides so the client can render the beam.
        // Once reflected, never resume owner-tracking — the laser flies in a fixed direction.
        if (!reflected) {
            updateLaserOriginAndDirection();
        }

        // Expand the intended reach — runs on both sides for visual sync
        if (!fullyExtended) {
            desiredLength += expansionSpeed;
            if (desiredLength >= maxLength) {
                desiredLength = maxLength;
                fullyExtended = true;
            }
        }

        // Start from full desired length, then truncate at block hits
        currentLength = desiredLength;

        // Block raytrace — truncate beam at first solid block (both sides)
        checkBlockCollision();

        // Update end point
        updateEndPoint();

        // Server-only: barrier collision, entity collision, death
        if (!worldObj.isRemote) {
            if (checkLaserBarrierCollision()) {
                return;
            }

            checkEntityCollisionAlongLine();
            // Death handled by base class deathWorldTime (from lifespanData.maxLifetime)
        }
    }

    /**
     * Update end point from current origin + direction * length.
     */
    private void updateEndPoint() {
        endX = startX + dirX * currentLength;
        endY = startY + dirY * currentLength;
        endZ = startZ + dirZ * currentLength;
    }

    /**
     * Update laser origin and direction from the owner's current look rotation.
     * Called each tick so the laser follows the caster's aim (sweeping beam).
     */
    private void updateLaserOriginAndDirection() {
        Entity owner = getOwnerEntity();
        if (owner == null || !(owner instanceof EntityLivingBase)) {
            if (worldObj != null && worldObj.isRemote) {
                handleClientInterpolation();
            }
            return;
        }
        EntityLivingBase livingOwner = (EntityLivingBase) owner;

        // Get owner look vector for direction
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

        // Set direction directly from look vector (no lockVerticalDirection split)
        dirX = look.xCoord;
        dirY = look.yCoord;
        dirZ = look.zCoord;

        // Keep direction normalized
        double dirLen = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLen > 0.0001) {
            dirX /= dirLen;
            dirY /= dirLen;
            dirZ /= dirLen;
        } else {
            dirX = 1.0;
            dirY = 0.0;
            dirZ = 0.0;
        }

        // Position: use anchor if launchFromAnchor, otherwise use look vector position.
        // When using an anchor point, recalculate direction from anchor toward the target
        // so the beam aims at the target rather than shooting straight from the offset position.
        if (anchorData.launchFromAnchor) {
            Vec3 anchorPos = AnchorPointHelper.calculateAnchorPosition(livingOwner, anchorData);
            setPosition(anchorPos.xCoord, anchorPos.yCoord, anchorPos.zCoord);

            Entity targetEntity = getTargetEntity();
            if (targetEntity instanceof EntityLivingBase && targetEntity.isEntityAlive()) {
                EntityLivingBase target = (EntityLivingBase) targetEntity;
                double dx = target.posX - anchorPos.xCoord;
                double dy = (target.posY + target.getEyeHeight() - 0.4) - anchorPos.yCoord;
                double dz = target.posZ - anchorPos.zCoord;
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len > 0.0001) {
                    dirX = dx / len;
                    dirY = dy / len;
                    dirZ = dz / len;
                }
            }
        } else {
            Vec3 direction = Vec3.createVectorHelper(dirX, dirY, dirZ);
            setLookVectorLaunchPosition(livingOwner, direction, false);
            // Lower beam origin slightly so it doesn't obscure the player's crosshair
            posY -= 0.15;
        }
        syncStartPositionToCurrent();
        syncPositionStateToCurrent(false);
    }

    public void startMoving(EntityLivingBase target) {
        beginLookVectorLaunch(false);

        desiredLength = 0.0f;
        currentLength = 0.0f;
        fullyExtended = false;
        lastBlockHitX = Integer.MIN_VALUE;
        lastBlockHitY = Integer.MIN_VALUE;
        lastBlockHitZ = Integer.MIN_VALUE;
        lastExplosionTick = -100;

        // Set initial direction: aim toward target first (like Orb/Beam),
        // fall back to owner look vector if no target is available.
        if (!setDirectionTowardTarget(target, startX, startY, startZ)) {
            Vec3 look = getOwnerLookVector();
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
        if (currentLength <= 0) return;

        double traceEndX = startX + dirX * currentLength;
        double traceEndY = startY + dirY * currentLength;
        double traceEndZ = startZ + dirZ * currentLength;

        MovingObjectPosition blockHit = rayTraceBlocks(startX, startY, startZ, traceEndX, traceEndY, traceEndZ);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            // Truncate beam at block hit
            float hitDist = (float) Math.sqrt(
                (blockHit.hitVec.xCoord - startX) * (blockHit.hitVec.xCoord - startX) +
                    (blockHit.hitVec.yCoord - startY) * (blockHit.hitVec.yCoord - startY) +
                    (blockHit.hitVec.zCoord - startZ) * (blockHit.hitVec.zCoord - startZ)
            );
            if (hitDist < currentLength) {
                currentLength = hitDist;
                endX = blockHit.hitVec.xCoord;
                endY = blockHit.hitVec.yCoord;
                endZ = blockHit.hitVec.zCoord;
            }

            // Server-only: fire event and explosion only when block changes or cooldown expires
            if (!worldObj.isRemote) {
                int bx = blockHit.blockX;
                int by = blockHit.blockY;
                int bz = blockHit.blockZ;
                boolean newBlock = (bx != lastBlockHitX || by != lastBlockHitY || bz != lastBlockHitZ);

                // Fire block impact event only when the beam hits a different block
                if (newBlock) {
                    lastBlockHitX = bx;
                    lastBlockHitY = by;
                    lastBlockHitZ = bz;
                    EventHooks.onEnergyProjectileBlockImpact(this, bx, by, bz);
                }

                // Explosive: fire on cooldown so sweeping doesn't explode every tick
                if (isExplosive() && (ticksExisted - lastExplosionTick) >= EXPLOSION_COOLDOWN) {
                    lastExplosionTick = ticksExisted;
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
        } else if (!worldObj.isRemote) {
            // Beam is not hitting any block — reset tracking so next block contact triggers fresh
            lastBlockHitX = Integer.MIN_VALUE;
            lastBlockHitY = Integer.MIN_VALUE;
            lastBlockHitZ = Integer.MIN_VALUE;
        }
    }

    private void checkEntityCollisionAlongLine() {
        if (currentLength <= 0) return;

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
                    // SINGLE hit or maxHits reached — truncate beam at impact and die
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
                // MULTI/PIERCE continue through remaining entities
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
     * Disable the base class barrier check — it uses position/velocity which doesn't work
     * for lasers (posX/Y/Z stays at origin, motionX/Y/Z is zero). We handle barriers
     * exclusively in updateProjectile() via checkLaserBarrierCollision().
     */
    @Override
    protected boolean checkBarrierCollision() {
        return false;
    }

    /**
     * Laser-specific barrier collision using line-segment intersection.
     * Called from updateProjectile() every tick.
     */
    @SuppressWarnings("unchecked")
    private boolean checkLaserBarrierCollision() {
        if (currentLength <= 0) return false;

        List<EntityEnergyBarrier> barriers = EntityEnergyBarrier.getActiveBarriers(worldObj);
        for (EntityEnergyBarrier barrier : barriers) {
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

            if (barrier instanceof EntityEnergyDome) {
                EntityEnergyDome dome = (EntityEnergyDome) barrier;
                float intersectDist = getLineSphereIntersection(dome);
                if (intersectDist >= 0) {
                    float damage = getModifiedDamage();
                    EntityEnergyBarrier.ProjectileHitOutcome hitOutcome = dome.onProjectileHitResolved(this, damage);
                    if (hitOutcome == null || hitOutcome.result == EntityEnergyBarrier.ProjectileHitResult.PASS) {
                        continue;
                    }

                    if (hitOutcome.result != EntityEnergyBarrier.ProjectileHitResult.BROKEN) {
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
                    EntityEnergyBarrier.ProjectileHitOutcome hitOutcome = barrier.onProjectileHitResolved(this, damage);
                    if (hitOutcome == null || hitOutcome.result == EntityEnergyBarrier.ProjectileHitResult.PASS) {
                        continue;
                    }

                    if (hitOutcome.result != EntityEnergyBarrier.ProjectileHitResult.BROKEN) {
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
    protected boolean reflectFromBarrier(EntityEnergyBarrier barrier, float reflectStrengthPct) {
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

        desiredLength = 0.0f;
        currentLength = 0.0f;
        fullyExtended = false;
        endX = startX;
        endY = startY;
        endZ = startZ;
        lastBlockHitX = Integer.MIN_VALUE;
        lastBlockHitY = Integer.MIN_VALUE;
        lastBlockHitZ = Integer.MIN_VALUE;
        lastExplosionTick = -100;

        // Reset lifetime so reflected laser gets a fresh maxLifetime from the base class
        deathWorldTime = -1;

        barrierImpactPauseTicks = 0;
        barrierImpactDestroyOnResume = false;

        // Detach from source ability so the caster is freed from active-phase locks.
        if (sourceAbility instanceof AbilityEnergyProjectile) {
            ((AbilityEnergyProjectile<?>) sourceAbility).detachEntity(this);
        }
        sourceAbility = null;

        // Save original owner before transfer for potential target retargeting
        int originalOwnerId = ownerEntityId;

        Entity barrierOwner = barrier.getOwnerEntity();
        if (barrierOwner != null) {
            setOwnerEntityId(barrierOwner.getEntityId());
            trackProjectile(this);
        }

        // Target Owner: set the reflected projectile's target to the original caster
        if (barrier.getBarrierData().isTargetOwner() && originalOwnerId != -1) {
            setTargetEntityId(originalOwnerId);
        } else {
            setTargetEntityId(-1);
        }

        setInnerColor(barrier.getInnerColor());
        setOuterColor(barrier.getOuterColor());

        float clampedStrength = Math.max(0.0f, Math.min(100.0f, reflectStrengthPct));
        float reducedDamage = getDamage() * (1.0f - clampedStrength / 100.0f);
        setCombatDamage(Math.max(0.0f, reducedDamage));

        hitOnceEntities.clear();
        lastHitTickByEntity.clear();
        reflected = true;
        return true;
    }

    /**
     * Line-sphere intersection between the laser line segment and a dome.
     * Returns the distance along the laser direction to the first entry point,
     * or -1 if no intersection from outside.
     */
    private float getLineSphereIntersection(EntityEnergyDome dome) {
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

    // ==================== DEBUG ====================

    @Override
    protected String debugLogExtra() {
        return String.format("dir=(%.3f,%.3f,%.3f) desired=%.2f current=%.2f max=%.2f " +
                "fullyExtended=%b end=(%.2f,%.2f,%.2f) width=%.2f expSpd=%.2f origin=(%.2f,%.2f,%.2f)",
            dirX, dirY, dirZ, desiredLength, currentLength, maxLength,
            fullyExtended, endX, endY, endZ, laserWidth, expansionSpeed,
            startX, startY, startZ);
    }

    // ==================== GETTERS / SETTERS ====================

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

    public float getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(float maxLength) {
        this.maxLength = maxLength;
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

    @Override
    protected float getLaunchClearanceRadius() {
        return Math.max(0.1f, laserWidth * 0.5f);
    }

    @Override
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration) {
        // Charge orb grows to half the beam width
        this.size = laserWidth * 0.5f;
        this.desiredLength = 0.0f;
        this.currentLength = 0.0f;
        this.fullyExtended = false;
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
        nbt.setFloat("DesiredLength", desiredLength);
        nbt.setFloat("CurrentLength", currentLength);
        nbt.setDouble("EndX", endX);
        nbt.setDouble("EndY", endY);
        nbt.setDouble("EndZ", endZ);
        nbt.setBoolean("FullyExtended", fullyExtended);
        nbt.setFloat("MaxLength", maxLength);
    }

    @Override
    protected void applyProjectileReflectionData(NBTTagCompound nbt) {
        dirX = nbt.getDouble("DirX");
        dirY = nbt.getDouble("DirY");
        dirZ = nbt.getDouble("DirZ");
        startX = nbt.getDouble("StartX");
        startY = nbt.getDouble("StartY");
        startZ = nbt.getDouble("StartZ");
        desiredLength = nbt.getFloat("DesiredLength");
        currentLength = nbt.getFloat("CurrentLength");
        endX = nbt.getDouble("EndX");
        endY = nbt.getDouble("EndY");
        endZ = nbt.getDouble("EndZ");
        fullyExtended = nbt.getBoolean("FullyExtended");
        if (nbt.hasKey("MaxLength")) maxLength = nbt.getFloat("MaxLength");
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.laserWidth = sanitize(nbt.hasKey("LaserWidth") ? nbt.getFloat("LaserWidth") : 0.2f, 0.2f, MAX_ENTITY_SIZE);
        this.expansionSpeed = sanitize(nbt.hasKey("ExpansionSpeed") ? nbt.getFloat("ExpansionSpeed") : 2.0f, 0.1f, MAX_ENTITY_SIZE);
        this.maxLength = sanitize(nbt.hasKey("MaxLength") ? nbt.getFloat("MaxLength") : 32.0f, 1.0f, MAX_ENTITY_SIZE);
        this.dirX = nbt.getDouble("DirX");
        this.dirY = nbt.getDouble("DirY");
        this.dirZ = nbt.getDouble("DirZ");
        this.desiredLength = nbt.getFloat("DesiredLength");
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
        nbt.setFloat("MaxLength", maxLength);
        nbt.setDouble("DirX", dirX);
        nbt.setDouble("DirY", dirY);
        nbt.setDouble("DirZ", dirZ);
        nbt.setFloat("DesiredLength", desiredLength);
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
                             float expansionSpeed, float maxLength) {
        setupPreviewState(owner, display, lightning, anchor, chargeDuration);

        // Laser-specific visual/behavior state.
        this.laserWidth = laserWidth;
        this.expansionSpeed = expansionSpeed;
        this.maxLength = Math.min(maxLength, 5.0f); // Limit for GUI preview
        this.desiredLength = 0.0f;
        this.currentLength = 0.0f;
        this.fullyExtended = false;

        // Charge as an orb from tiny size to half laser width while anchored.
        this.targetSize = laserWidth * 0.5f;
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
