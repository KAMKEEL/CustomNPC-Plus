package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Beam projectile - head with trailing path that curves with homing.
 * Stays attached to origin point, trail shows path of head.
 *
 * IMPORTANT: Trail points are stored RELATIVE to startX/Y/Z (origin).
 * This ensures rendering is stable regardless of entity position interpolation.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityBeam extends EntityAbilityProjectile {

    // Beam-specific movement properties
    private float speed = 0.5f;
    private boolean homing = true;
    private float homingStrength = 0.35f;  // Increased from 0.15 for better tracking
    private float homingRange = 20.0f;

    // Beam shape properties
    private float beamWidth = 0.3f;
    private float headSize = 0.5f;

    // Trail - list of points showing beam path (RELATIVE to origin!)
    private List<Vec3> trailPoints = new ArrayList<>();
    private static final int MAX_TRAIL_POINTS = 200;
    private static final double MIN_POINT_DISTANCE = 0.2;

    // Head position relative to origin
    private double headOffsetX, headOffsetY, headOffsetZ;
    private double prevHeadOffsetX, prevHeadOffsetY, prevHeadOffsetZ;

    // Origin stays fixed (or follows owner)
    private boolean attachedToOwner = true;

    // Whether to render tail orb (only when anchored)
    private boolean renderTailOrb = true;

    // Charging state (during windup)
    private boolean charging = false;
    private int chargeDuration = 40;
    private int chargeTick = 0;
    private float chargeOffsetDistance = 1.0f;
    private EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.FRONT);

    // Trail fading for non-anchored beams (comet effect)
    private boolean fadeTrail = false;
    private int trailFadeTime = 20; // Ticks for trail to fully fade
    private List<Integer> trailPointAges = new ArrayList<>();

    // Data watcher index for charging state (synced to clients)
    private static final int DW_CHARGING = 20;

    // Debug logging
    private static final boolean DEBUG_LOGGING = false;

    public EntityAbilityBeam(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        // Register data watcher for charging state
        this.dataWatcher.addObject(DW_CHARGING, (byte) 0);
    }

    /**
     * Check if beam is in charging state (synced via data watcher).
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
     * @param anchoredMode If true, origin follows owner and tail orb is rendered.
     *                     If false, beam is free-moving with trailing length (no tail orb).
     */
    public EntityAbilityBeam(World world, EntityLivingBase owner, EntityLivingBase target,
                              double x, double y, double z,
                              float beamWidth, float headSize,
                              EnergyColorData color, EnergyCombatData combat,
                              EnergyHomingData homing, EnergyLightningData lightning,
                              EnergyLifespanData lifespan,
                              boolean anchoredMode) {
        super(world);

        // Initialize base properties via parent
        initProjectile(owner, target, x, y, z, headSize, color, combat, lightning, lifespan);

        // Beam-specific properties from homing data
        this.speed = homing.speed;
        this.homing = homing.homing;
        this.homingStrength = homing.homingStrength;
        this.homingRange = homing.homingRange;
        this.beamWidth = beamWidth;
        this.headSize = headSize;

        // Anchored mode controls whether origin follows owner and tail orb is rendered
        this.attachedToOwner = anchoredMode;
        this.renderTailOrb = anchoredMode;

        // Initialize head offset at origin (0,0,0 relative)
        this.headOffsetX = 0;
        this.headOffsetY = 0;
        this.headOffsetZ = 0;
        this.prevHeadOffsetX = 0;
        this.prevHeadOffsetY = 0;
        this.prevHeadOffsetZ = 0;

        // Add initial trail point at origin (relative 0,0,0)
        trailPoints.add(Vec3.createVectorHelper(0, 0, 0));

        if (DEBUG_LOGGING && !world.isRemote) {
            LogWriter.info("[Beam] Created at origin " + x + ", " + y + ", " + z + " maxDist=" + lifespan.maxDistance);
        }

        // Calculate initial velocity toward target
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.getEyeHeight()) - y;
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

    public void setupCharging(EnergyAnchorData anchor, int chargeDuration, float chargeOffsetDistance) {
        setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.chargeOffsetDistance = chargeOffsetDistance;
        this.anchorData = anchor;
        this.fadeTrail = !attachedToOwner;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    public void setupPreview(EntityLivingBase owner, float beamWidth, float headSize, EnergyColorData color, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration, float chargeOffsetDistance) {
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);

        // Set visual properties
        this.beamWidth = beamWidth;
        this.headSize = headSize;
        this.size = headSize;
        this.innerColor = color.innerColor;
        this.outerColor = color.outerColor;
        this.outerColorEnabled = color.outerColorEnabled;
        this.outerColorWidth = color.outerColorWidth;
        this.outerColorAlpha = color.outerColorAlpha;
        this.rotationSpeed = color.rotationSpeed;
        this.lightningEffect = lightning.lightningEffect;
        this.lightningDensity = lightning.lightningDensity;
        this.lightningRadius = lightning.lightningRadius;

        // Set charging state
        this.setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.chargeOffsetDistance = chargeOffsetDistance;
        this.anchorData = anchor;

        // Initial position at anchor point
        Vec3 pos = AnchorPointHelper.calculateAnchorPosition(owner, anchorData, chargeOffsetDistance);
        this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        this.prevPosX = pos.xCoord;
        this.prevPosY = pos.yCoord;
        this.prevPosZ = pos.zCoord;
        this.startX = pos.xCoord;
        this.startY = pos.yCoord;
        this.startZ = pos.zCoord;

        // Initialize head offsets
        this.headOffsetX = 0;
        this.headOffsetY = 0;
        this.headOffsetZ = 0;
        this.prevHeadOffsetX = 0;
        this.prevHeadOffsetY = 0;
        this.prevHeadOffsetZ = 0;

        // Attach to owner for anchor following
        this.attachedToOwner = true;
        this.renderTailOrb = true;

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
            float pitch = 0; // Fire horizontally
            motionX = -Math.sin(yaw) * Math.cos(pitch) * speed;
            motionY = -Math.sin(pitch) * speed;
            motionZ = Math.cos(yaw) * Math.cos(pitch) * speed;
        } else {
            motionX = speed;
            motionY = 0;
            motionZ = 0;
        }
    }

    /**
     * Start the beam firing (exit charging mode).
     * Called by ability when windup ends.
     *
     * For anchored beams: origin follows owner, head starts at charged position
     * For non-anchored beams: origin fixed at charged position, head starts there
     */
    public void startFiring(EntityLivingBase target) {
        if (!isCharging()) return;

        setCharging(false);

        // Origin (tail) stays at the charged position - where the orb was
        // This is the same for both anchored and non-anchored modes
        startX = posX;
        startY = posY;
        startZ = posZ;

        // Head starts at the origin (tail position)
        headOffsetX = 0;
        headOffsetY = 0;
        headOffsetZ = 0;
        prevHeadOffsetX = 0;
        prevHeadOffsetY = 0;
        prevHeadOffsetZ = 0;

        // Origin no longer follows owner after firing starts
        // (for anchored mode, tail is fixed in space; for non-anchored, there's no tail)
        attachedToOwner = false;

        // Initialize trail with just the origin point
        trailPoints.clear();
        trailPointAges.clear();
        trailPoints.add(Vec3.createVectorHelper(0, 0, 0));
        if (fadeTrail) trailPointAges.add(0);

        // Calculate velocity toward target (head starts at origin = startX/Y/Z)
        Entity owner = getOwner();

        if (target != null) {
            double dx = target.posX - startX;
            double dy = (target.posY + target.getEyeHeight()) - startY;
            double dz = target.posZ - startZ;
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

        if (DEBUG_LOGGING && !worldObj.isRemote) {
            LogWriter.info("[Beam] startFiring: origin=(" + startX + "," + startY + "," + startZ +
                ") headOffset=(" + headOffsetX + "," + headOffsetY + "," + headOffsetZ +
                ") motion=(" + motionX + "," + motionY + "," + motionZ + ")");
        }
    }

    @Override
    protected boolean checkMaxDistance() {
        // Check distance using head offset (distance from origin)
        double distFromOrigin = Math.sqrt(
            headOffsetX * headOffsetX +
            headOffsetY * headOffsetY +
            headOffsetZ * headOffsetZ
        );
        boolean exceeded = distFromOrigin >= maxDistance;
        if (exceeded && !worldObj.isRemote && DEBUG_LOGGING) {
            LogWriter.info("[Beam] DEAD: Max distance exceeded. dist=" + distFromOrigin + " max=" + maxDistance);
        }
        return exceeded;
    }

    @Override
    protected void updateProjectile() {
        // Handle charging state (windup phase) - use isCharging() for synced value
        if (isCharging()) {
            updateCharging();
            return;
        }

        // Store previous head offset for interpolation
        prevHeadOffsetX = headOffsetX;
        prevHeadOffsetY = headOffsetY;
        prevHeadOffsetZ = headOffsetZ;

        // Age trail points for fading effect
        if (fadeTrail) {
            ageTrailPoints();
        }

        // In preview mode, run movement on client (no server)
        if (previewMode) {
            updatePreviewMovement();
            return;
        }

        if (worldObj.isRemote) {
            // Client: interpolate entity position
            handleClientInterpolation();

            // Client: derive head offset from entity position and origin
            // Entity position = head world position, so headOffset = pos - origin
            // Note: During charging, updateCharging() handles origin positioning
            // After firing, origin is fixed at the charged position
            headOffsetX = posX - startX;
            headOffsetY = posY - startY;
            headOffsetZ = posZ - startZ;

            // Add trail point on client
            addTrailPoint();
        } else {
            // Server-side logic
            updateHoming();

            // Move head offset
            headOffsetX += motionX;
            headOffsetY += motionY;
            headOffsetZ += motionZ;

            // Update origin if attached to owner
            if (attachedToOwner) {
                Entity owner = getOwner();
                if (owner != null) {
                    startX = owner.posX;
                    startY = owner.posY + owner.getEyeHeight() * 0.7;
                    startZ = owner.posZ;
                }
            }

            // Calculate world position of head for collision detection
            double headWorldX = startX + headOffsetX;
            double headWorldY = startY + headOffsetY;
            double headWorldZ = startZ + headOffsetZ;

            // Update entity position to head (for network sync)
            this.setPosition(headWorldX, headWorldY, headWorldZ);

            // Add trail point
            addTrailPoint();

            // Debug log periodically
            if (DEBUG_LOGGING && ticksExisted % 20 == 0) {
                LogWriter.info("[Beam] Server tick=" + ticksExisted + " headOffset=(" +
                    String.format("%.2f", headOffsetX) + "," +
                    String.format("%.2f", headOffsetY) + "," +
                    String.format("%.2f", headOffsetZ) + ") origin=(" +
                    String.format("%.2f", startX) + "," +
                    String.format("%.2f", startY) + "," +
                    String.format("%.2f", startZ) + ") trail=" + trailPoints.size());
            }

            // Skip collision checks on first few ticks
            if (ticksExisted > 2) {
                checkBlockCollision(headWorldX, headWorldY, headWorldZ);
                checkEntityCollision(headWorldX, headWorldY, headWorldZ);
            }
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

        // Calculate position based on anchor point
        Vec3 pos;
        if (owner instanceof EntityLivingBase) {
            pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData, chargeOffsetDistance);
        } else {
            // Fallback for non-living entities (shouldn't happen normally)
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            double offsetX = -Math.sin(yaw) * chargeOffsetDistance;
            double offsetZ = Math.cos(yaw) * chargeOffsetDistance;
            pos = Vec3.createVectorHelper(
                owner.posX + offsetX,
                owner.posY + owner.getEyeHeight() * 0.7,
                owner.posZ + offsetZ
            );
        }

        // Offset downward by half headSize to center
        double centeredY = pos.yCoord - headSize * 0.5;
        setPosition(pos.xCoord, centeredY, pos.zCoord);

        // Also update origin for when firing starts
        startX = pos.xCoord;
        startY = centeredY;
        startZ = pos.zCoord;
    }

    /**
     * Age trail points and remove old ones (for comet effect).
     */
    private void ageTrailPoints() {
        // Age all trail points
        for (int i = 0; i < trailPointAges.size(); i++) {
            trailPointAges.set(i, trailPointAges.get(i) + 1);
        }

        // Remove trail points that have fully faded
        while (!trailPointAges.isEmpty() && trailPointAges.get(0) >= trailFadeTime) {
            trailPointAges.remove(0);
            if (!trailPoints.isEmpty()) {
                trailPoints.remove(0);
            }
        }
    }

    private void addTrailPoint() {
        // Trail points are RELATIVE to origin
        if (trailPoints.isEmpty()) {
            trailPoints.add(Vec3.createVectorHelper(headOffsetX, headOffsetY, headOffsetZ));
            if (fadeTrail) {
                trailPointAges.add(0);
            }
            return;
        }

        Vec3 lastPoint = trailPoints.get(trailPoints.size() - 1);
        double dist = Math.sqrt(
            (headOffsetX - lastPoint.xCoord) * (headOffsetX - lastPoint.xCoord) +
            (headOffsetY - lastPoint.yCoord) * (headOffsetY - lastPoint.yCoord) +
            (headOffsetZ - lastPoint.zCoord) * (headOffsetZ - lastPoint.zCoord)
        );

        if (dist >= MIN_POINT_DISTANCE) {
            trailPoints.add(Vec3.createVectorHelper(headOffsetX, headOffsetY, headOffsetZ));
            if (fadeTrail) {
                trailPointAges.add(0);
            }

            // Limit trail length (only if not using fading - fading handles its own cleanup)
            if (!fadeTrail) {
                while (trailPoints.size() > MAX_TRAIL_POINTS) {
                    trailPoints.remove(0);
                }
            }
        }
    }

    /**
     * Update movement in preview mode (client-side only, no damage).
     */
    private void updatePreviewMovement() {
        // Move head offset
        headOffsetX += motionX;
        headOffsetY += motionY;
        headOffsetZ += motionZ;

        // Update origin if attached to owner
        if (attachedToOwner) {
            Entity owner = getOwner();
            if (owner != null) {
                startX = owner.posX;
                startY = owner.posY + owner.getEyeHeight() * 0.7;
                startZ = owner.posZ;
            }
        }

        // Calculate world position of head
        double headWorldX = startX + headOffsetX;
        double headWorldY = startY + headOffsetY;
        double headWorldZ = startZ + headOffsetZ;

        // Update entity position to head
        this.setPosition(headWorldX, headWorldY, headWorldZ);

        // Add trail point
        addTrailPoint();
    }

    private void updateHoming() {
        if (!homing) return;

        Entity target = getTarget();
        if (target == null || !target.isEntityAlive()) return;

        // Calculate world position of head
        double headWorldX = startX + headOffsetX;
        double headWorldY = startY + headOffsetY;
        double headWorldZ = startZ + headOffsetZ;

        double dx = target.posX - headWorldX;
        double dy = (target.posY + target.getEyeHeight()) - headWorldY;
        double dz = target.posZ - headWorldZ;
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

    private void checkBlockCollision(double headX, double headY, double headZ) {
        // Previous head world position
        double prevHeadWorldX = startX + prevHeadOffsetX;
        double prevHeadWorldY = startY + prevHeadOffsetY;
        double prevHeadWorldZ = startZ + prevHeadOffsetZ;

        Vec3 currentPos = Vec3.createVectorHelper(prevHeadWorldX, prevHeadWorldY, prevHeadWorldZ);
        Vec3 nextPos = Vec3.createVectorHelper(headX, headY, headZ);
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

    private void checkEntityCollision(double headX, double headY, double headZ) {
        double hitSize = headSize * 0.5;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            headX - hitSize, headY - hitSize, headZ - hitSize,
            headX + hitSize, headY + hitSize, headZ + hitSize
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;

            if (DEBUG_LOGGING) {
                LogWriter.info("[Beam] DEAD: Entity collision with " + entity.getClass().getSimpleName() + " at tick " + ticksExisted);
            }
            hasHit = true;

            if (explosive) {
                posX = headX;
                posY = headY;
                posZ = headZ;
                doExplosion();
            } else {
                applyDamage(entity);
            }

            this.setDead();
            return;
        }
    }

    // ==================== GETTERS FOR RENDERER ====================

    public float getBeamWidth() {
        return beamWidth;
    }

    public float getHeadSize() {
        return headSize;
    }

    /**
     * Get interpolated head offset for smooth rendering.
     * Returns offset RELATIVE to origin.
     */
    public double getInterpolatedHeadOffsetX(float partialTicks) {
        return prevHeadOffsetX + (headOffsetX - prevHeadOffsetX) * partialTicks;
    }

    public double getInterpolatedHeadOffsetY(float partialTicks) {
        return prevHeadOffsetY + (headOffsetY - prevHeadOffsetY) * partialTicks;
    }

    public double getInterpolatedHeadOffsetZ(float partialTicks) {
        return prevHeadOffsetZ + (headOffsetZ - prevHeadOffsetZ) * partialTicks;
    }

    /**
     * Get trail points. These are RELATIVE to origin (startX/Y/Z).
     */
    public List<Vec3> getTrailPoints() {
        return trailPoints;
    }

    public double getOriginX() {
        return startX;
    }

    public double getOriginY() {
        return startY;
    }

    public double getOriginZ() {
        return startZ;
    }

    /**
     * Whether the tail orb should be rendered (only true in anchored mode).
     */
    public boolean shouldRenderTailOrb() {
        return renderTailOrb;
    }

    /**
     * Whether the beam is attached to its owner (anchored mode).
     */
    public boolean isAttachedToOwner() {
        return attachedToOwner;
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

    /**
     * Whether trail should fade (comet effect for non-anchored beams).
     */
    public boolean hasFadingTrail() {
        return fadeTrail;
    }

    /**
     * Get trail point ages for fading calculation.
     */
    public List<Integer> getTrailPointAges() {
        return trailPointAges;
    }

    /**
     * Get trail fade time in ticks.
     */
    public int getTrailFadeTime() {
        return trailFadeTime;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.speed = nbt.hasKey("Speed") ? nbt.getFloat("Speed") : 0.5f;
        this.homing = !nbt.hasKey("Homing") || nbt.getBoolean("Homing");
        this.homingStrength = nbt.hasKey("HomingStrength") ? nbt.getFloat("HomingStrength") : 0.15f;
        this.homingRange = nbt.hasKey("HomingRange") ? nbt.getFloat("HomingRange") : 20.0f;
        this.beamWidth = nbt.hasKey("BeamWidth") ? nbt.getFloat("BeamWidth") : 0.3f;
        this.headSize = nbt.hasKey("HeadSize") ? nbt.getFloat("HeadSize") : 0.5f;
        this.headOffsetX = nbt.hasKey("HeadOffsetX") ? nbt.getDouble("HeadOffsetX") : 0;
        this.headOffsetY = nbt.hasKey("HeadOffsetY") ? nbt.getDouble("HeadOffsetY") : 0;
        this.headOffsetZ = nbt.hasKey("HeadOffsetZ") ? nbt.getDouble("HeadOffsetZ") : 0;
        this.prevHeadOffsetX = this.headOffsetX;
        this.prevHeadOffsetY = this.headOffsetY;
        this.prevHeadOffsetZ = this.headOffsetZ;
        this.attachedToOwner = !nbt.hasKey("AttachedToOwner") || nbt.getBoolean("AttachedToOwner");
        this.renderTailOrb = !nbt.hasKey("RenderTailOrb") || nbt.getBoolean("RenderTailOrb");
        // Read charging state and sync to data watcher
        boolean isCharging = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.charging = isCharging;
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (isCharging ? 1 : 0));
        this.chargeDuration = nbt.hasKey("ChargeDuration") ? nbt.getInteger("ChargeDuration") : 40;
        this.chargeTick = nbt.hasKey("ChargeTick") ? nbt.getInteger("ChargeTick") : 0;
        this.chargeOffsetDistance = nbt.hasKey("ChargeOffsetDistance") ? nbt.getFloat("ChargeOffsetDistance") : 1.0f;
        this.fadeTrail = nbt.hasKey("FadeTrail") && nbt.getBoolean("FadeTrail");
        this.trailFadeTime = nbt.hasKey("TrailFadeTime") ? nbt.getInteger("TrailFadeTime") : 20;

        this.anchorData.readNBT(nbt);

        // Read trail points (relative to origin)
        trailPoints.clear();
        if (nbt.hasKey("Trail")) {
            NBTTagList trailList = nbt.getTagList("Trail", 10);
            for (int i = 0; i < trailList.tagCount(); i++) {
                NBTTagCompound point = trailList.getCompoundTagAt(i);
                trailPoints.add(Vec3.createVectorHelper(
                    point.getDouble("X"),
                    point.getDouble("Y"),
                    point.getDouble("Z")
                ));
            }
        }
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("Speed", speed);
        nbt.setBoolean("Homing", homing);
        nbt.setFloat("HomingStrength", homingStrength);
        nbt.setFloat("HomingRange", homingRange);
        nbt.setFloat("BeamWidth", beamWidth);
        nbt.setFloat("HeadSize", headSize);
        nbt.setDouble("HeadOffsetX", headOffsetX);
        nbt.setDouble("HeadOffsetY", headOffsetY);
        nbt.setDouble("HeadOffsetZ", headOffsetZ);
        nbt.setBoolean("AttachedToOwner", attachedToOwner);
        nbt.setBoolean("RenderTailOrb", renderTailOrb);
        nbt.setBoolean("Charging", isCharging());
        nbt.setInteger("ChargeDuration", chargeDuration);
        nbt.setInteger("ChargeTick", chargeTick);
        nbt.setFloat("ChargeOffsetDistance", chargeOffsetDistance);
        nbt.setBoolean("FadeTrail", fadeTrail);
        nbt.setInteger("TrailFadeTime", trailFadeTime);

        this.anchorData.writeNBT(nbt);

        // Write trail points
        NBTTagList trailList = new NBTTagList();
        for (Vec3 point : trailPoints) {
            NBTTagCompound pointNbt = new NBTTagCompound();
            pointNbt.setDouble("X", point.xCoord);
            pointNbt.setDouble("Y", point.yCoord);
            pointNbt.setDouble("Z", point.zCoord);
            trailList.appendTag(pointNbt);
        }
        nbt.setTag("Trail", trailList);
    }
}
