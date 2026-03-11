package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Beam projectile - head with trailing path that curves with homing.
 * Stays attached to origin point, trail shows path of head.
 * <p>
 * IMPORTANT: Trail points are stored RELATIVE to startX/Y/Z (origin).
 * This ensures rendering is stable regardless of entity position interpolation.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityBeam extends EntityEnergyProjectile {

    private enum BeamMode {
        ANCHORED,
        FREE_TRAIL
    }

    // Beam shape properties (target values - set by sync packets/scripts)
    private float beamWidth = 0.3f;
    private float headSize = 0.5f;

    // Lerp-smoothed render values (approach target each tick)
    private float renderBeamWidth = 0.3f;
    private float renderHeadSize = 0.5f;
    private float prevRenderBeamWidth = 0.3f;
    private float prevRenderHeadSize = 0.5f;

    // Trail - list of points showing beam path (RELATIVE to origin!)
    private List<Vec3> trailPoints = new ArrayList<>();
    private static final int MAX_TRAIL_POINTS = 200;
    private static final double MIN_POINT_DISTANCE = 0.2;
    private static final int TRAIL_COMPACT_THRESHOLD = 128;
    private int trailStartIndex = 0;

    // Head position relative to origin
    private double headOffsetX, headOffsetY, headOffsetZ;
    private double prevHeadOffsetX, prevHeadOffsetY, prevHeadOffsetZ;

    // Beam behavior mode (single source of truth).
    private BeamMode beamMode = BeamMode.ANCHORED;

    // Free Aim (players only) - steers beam toward player's look direction
    private boolean freeAim = false;
    private static final float FREE_AIM_STRENGTH = 0.15f;

    // Charging state (beam-specific)
    private float chargeOffsetDistance = 1.0f;
    private int trailFadeTime = 20; // Ticks for trail to fully fade
    private List<Integer> trailPointAges = new ArrayList<>();

    public EntityAbilityBeam(World world) {
        super(world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        // Beam can extend far from entity position (head); use a generous render distance
        // that accounts for the full beam length (origin to head)
        double headDist = Math.sqrt(headOffsetX * headOffsetX + headOffsetY * headOffsetY + headOffsetZ * headOffsetZ);
        double range = Math.max(128.0D, headDist * 2.0D + 64.0D);
        return distance < range * range;
    }

    private static BeamMode modeFromAnchored(boolean anchored) {
        return anchored ? BeamMode.ANCHORED : BeamMode.FREE_TRAIL;
    }

    private boolean isAnchoredMode() {
        return beamMode == BeamMode.ANCHORED;
    }

    private boolean isFadingMode() {
        return beamMode == BeamMode.FREE_TRAIL;
    }

    private int getActiveTrailSize() {
        return Math.max(0, trailPoints.size() - trailStartIndex);
    }

    private void resetHeadOffsets() {
        headOffsetX = 0;
        headOffsetY = 0;
        headOffsetZ = 0;
        prevHeadOffsetX = 0;
        prevHeadOffsetY = 0;
        prevHeadOffsetZ = 0;
    }

    private void resetTrailStorage() {
        trailPoints.clear();
        trailPointAges.clear();
        trailStartIndex = 0;
    }

    private void setBeamMode(BeamMode mode) {
        beamMode = mode != null ? mode : BeamMode.ANCHORED;
        if (isFadingMode()) {
            while (trailPointAges.size() < trailPoints.size()) {
                trailPointAges.add(0);
            }
            while (trailPointAges.size() > trailPoints.size()) {
                trailPointAges.remove(trailPointAges.size() - 1);
            }
        } else {
            if (trailStartIndex > 0 && trailStartIndex < trailPoints.size()) {
                trailPoints = new ArrayList<Vec3>(trailPoints.subList(trailStartIndex, trailPoints.size()));
            }
            trailStartIndex = 0;
            trailPointAges.clear();
        }
    }

    /**
     * Full constructor with all parameters using data classes.
     *
     * @param anchoredMode If true, origin follows owner and tail orb is rendered.
     *                     If false, beam is free-moving with trailing length (no tail orb).
     */
    public EntityAbilityBeam(World world, EntityLivingBase owner, EntityLivingBase target,
                             double x, double y, double z,
                             float beamWidth, float headSize,
                             EnergyDisplayData display, EnergyCombatData combat,
                             EnergyHomingData homing, EnergyLightningData lightning,
                             EnergyLifespanData lifespan,
                             boolean anchoredMode) {
        super(world);

        // Initialize base properties via parent
        initProjectile(owner, target, x, y, z, headSize, display, combat, lightning, lifespan);

        // Beam-specific properties from homing data

        this.homingData = homing != null ? homing.copy() : new EnergyHomingData();
        this.beamWidth = beamWidth;
        this.headSize = headSize;

        // Beam mode controls tail attachment/orb/fading behavior.
        setBeamMode(modeFromAnchored(anchoredMode));

        // Initialize head offset at origin (0,0,0 relative)
        resetHeadOffsets();

        // Add initial trail point at origin (relative 0,0,0)
        trailPoints.add(Vec3.createVectorHelper(0, 0, 0));

        // Calculate initial velocity toward target
        calculateInitialVelocity(owner, target, x, y, z);
    }

    public void setupCharging(EnergyAnchorData anchor, int chargeDuration, float chargeOffsetDistance) {
        setupChargingState(anchor, chargeDuration);
        this.chargeOffsetDistance = chargeOffsetDistance;
        clearMotion();
    }

    public void setupPreview(EntityLivingBase owner, float beamWidth, float headSize, EnergyDisplayData display, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration, float chargeOffsetDistance) {
        setupPreviewState(owner, display, lightning, anchor, chargeDuration);

        // Set visual properties
        this.beamWidth = beamWidth;
        this.headSize = headSize;
        setVisualSize(headSize);

        this.chargeOffsetDistance = chargeOffsetDistance;
        setChargeOriginFromAnchor(owner, anchorData, chargeOffsetDistance);

        // Initialize head offsets
        resetHeadOffsets();

        // Clear motion
        clearMotion();
    }

    /**
     * Start preview firing (simulates firing toward a point in front of NPC).
     */
    public void startPreviewFiring() {
        startPreviewFiringDefault();

        // Reset head offset to origin
        resetHeadOffsets();

        // Reset trail
        resetTrailStorage();
        trailPoints.add(Vec3.createVectorHelper(0, 0, 0));
        if (isFadingMode()) trailPointAges.add(0);
    }

    /**
     * Start the beam firing (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startFiring(EntityLivingBase target) {
        startMovingTowardTargetFromStartDefault(target);

        // Head starts at the origin (tail position)
        resetHeadOffsets();

        // Initialize trail with just the origin point
        resetTrailStorage();
        trailPoints.add(Vec3.createVectorHelper(0, 0, 0));
        if (isFadingMode()) trailPointAges.add(0);

    }

    @Override
    protected boolean checkMaxDistance() {
        // Check distance using head offset (distance from origin)
        double distFromOrigin = Math.sqrt(
            headOffsetX * headOffsetX +
                headOffsetY * headOffsetY +
                headOffsetZ * headOffsetZ
        );
        return distFromOrigin >= getMaxDistance();
    }

    @Override
    protected void updateProjectile() {
        // Save previous render values for sub-tick interpolation
        prevRenderBeamWidth = renderBeamWidth;
        prevRenderHeadSize = renderHeadSize;

        // Handle charging state (windup phase) - use isCharging() for synced value
        if (isCharging()) {
            // During charging, snap render values to target (size grows smoothly already)
            renderBeamWidth = beamWidth;
            renderHeadSize = headSize;
            updateCharging();
            return;
        }

        // Lerp render values toward target (smooth out sync packet jumps)
        renderBeamWidth += (beamWidth - renderBeamWidth) * 0.15f;
        renderHeadSize += (headSize - renderHeadSize) * 0.15f;

        prevHeadOffsetX = headOffsetX;
        prevHeadOffsetY = headOffsetY;
        prevHeadOffsetZ = headOffsetZ;

        // Age trail points for fading effect
        if (isFadingMode()) {
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
            if (isAnchoredMode()) {
                Entity owner = getOwnerEntity();
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

            // Skip collision checks on first few ticks
            if (ticksExisted > 2) {
                checkBlockCollision(headWorldX, headWorldY, headWorldZ);
                checkEntityCollision(headWorldX, headWorldY, headWorldZ);
            }
        }
    }

    /**
     * Update during charging state - follow owner based on anchor point.
     * Beam-specific: positions based on chargeOffsetDistance and updates startX/Y/Z.
     */
    @Override
    protected void updateCharging() {
        chargeTick++;

        Entity owner = getOwnerEntity();
        if (owner != null) {
            Vec3 pos;
            if (owner instanceof EntityLivingBase) {
                pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData, chargeOffsetDistance);
            } else {
                float yaw = (float) Math.toRadians(owner.rotationYaw);
                double offsetX = -Math.sin(yaw) * chargeOffsetDistance;
                double offsetZ = Math.cos(yaw) * chargeOffsetDistance;
                pos = Vec3.createVectorHelper(
                    owner.posX + offsetX,
                    owner.posY + owner.getEyeHeight() * 0.7,
                    owner.posZ + offsetZ
                );
            }

            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
            startX = pos.xCoord;
            startY = pos.yCoord;
            startZ = pos.zCoord;
        }
    }

    /**
     * Age trail points and remove old ones (for comet effect).
     */
    private void ageTrailPoints() {
        if (trailStartIndex >= trailPoints.size()) {
            resetTrailStorage();
            return;
        }

        // Age all active trail points.
        for (int i = trailStartIndex; i < trailPointAges.size(); i++) {
            trailPointAges.set(i, trailPointAges.get(i) + 1);
        }

        // Move logical start index forward for fully faded points.
        while (trailStartIndex < trailPointAges.size() && trailPointAges.get(trailStartIndex) >= trailFadeTime) {
            trailStartIndex++;
        }

        if (trailStartIndex >= trailPoints.size()) {
            resetTrailStorage();
            return;
        }

        // Compact stale prefix to avoid unbounded growth of dead entries.
        if (trailStartIndex >= TRAIL_COMPACT_THRESHOLD && trailStartIndex * 2 >= trailPoints.size()) {
            trailPoints = new ArrayList<Vec3>(trailPoints.subList(trailStartIndex, trailPoints.size()));
            trailPointAges = new ArrayList<Integer>(trailPointAges.subList(trailStartIndex, trailPointAges.size()));
            trailStartIndex = 0;
        }
    }

    private void addTrailPoint() {
        if (trailStartIndex >= trailPoints.size()) {
            resetTrailStorage();
        }

        // Trail points are RELATIVE to origin
        if (trailPoints.isEmpty()) {
            trailPoints.add(Vec3.createVectorHelper(headOffsetX, headOffsetY, headOffsetZ));
            if (isFadingMode()) {
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
            if (isFadingMode()) {
                trailPointAges.add(0);
            }

            // Limit trail length (only if not using fading - fading handles its own cleanup)
            if (!isFadingMode()) {
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
        if (isAnchoredMode()) {
            Entity owner = getOwnerEntity();
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

    /**
     * Steer beam toward player's look direction (Free Aim mode).
     * Blends the velocity DIRECTION toward the look vector each tick, producing
     * a smooth arc. The turn radius scales naturally with speed (faster = wider arc).
     * <p>
     * Previous approach projected a target point at headDist along the look line,
     * which caused oscillation at high speeds (the position target kept shifting)
     * and spiraling at large distances (cross-track error grew faster than correction).
     */
    private void updateFreeAim() {
        Entity owner = getOwnerEntity();
        if (!(owner instanceof EntityPlayer)) return;

        Vec3 look = getOwnerLookVector();
        if (look == null) return;

        double speed = getSpeed();

        // Desired velocity = look direction at current speed
        double desiredVX = look.xCoord * speed;
        double desiredVY = look.yCoord * speed;
        double desiredVZ = look.zCoord * speed;

        // Blend current velocity toward desired direction
        motionX += (desiredVX - motionX) * FREE_AIM_STRENGTH;
        motionY += (desiredVY - motionY) * FREE_AIM_STRENGTH;
        motionZ += (desiredVZ - motionZ) * FREE_AIM_STRENGTH;

        // Renormalize to constant speed
        double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        if (vLen > 0) {
            motionX = (motionX / vLen) * speed;
            motionY = (motionY / vLen) * speed;
            motionZ = (motionZ / vLen) * speed;
        }
    }

    @Override
    protected void updateHoming() {
        if (freeAim) {
            Entity owner = getOwnerEntity();
            if (owner instanceof EntityPlayer) {
                updateFreeAim();
                return;
            }
            // NPCs fall through to normal homing
        }

        if (!isHoming()) return;

        Entity target = getTargetEntity();
        if (target == null || !target.isEntityAlive()) return;

        // Calculate world position of head
        double headWorldX = startX + headOffsetX;
        double headWorldY = startY + headOffsetY;
        double headWorldZ = startZ + headOffsetZ;

        double dx = target.posX - headWorldX;
        double dy = (target.posY + target.getEyeHeight()) - headWorldY;
        double dz = target.posZ - headWorldZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist <= getHomingRange() && dist > 0) {
            // Calculate effective homing strength - increases when closer to commit to target
            float effectiveStrength = getHomingStrength();
            if (dist < getHomingRange() * 0.3) {
                effectiveStrength = Math.min(1.0f, getHomingStrength() * 2.5f);
            } else if (dist < getHomingRange() * 0.6) {
                effectiveStrength = Math.min(0.8f, getHomingStrength() * 1.5f);
            }

            double desiredVX = (dx / dist) * getSpeed();
            double desiredVY = (dy / dist) * getSpeed();
            double desiredVZ = (dz / dist) * getSpeed();

            motionX += (desiredVX - motionX) * effectiveStrength;
            motionY += (desiredVY - motionY) * effectiveStrength;
            motionZ += (desiredVZ - motionZ) * effectiveStrength;

            double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * getSpeed();
                motionY = (motionY / vLen) * getSpeed();
                motionZ = (motionZ / vLen) * getSpeed();
            }
        }
    }

    private void checkBlockCollision(double headX, double headY, double headZ) {
        // Previous head world position
        double prevHeadWorldX = startX + prevHeadOffsetX;
        double prevHeadWorldY = startY + prevHeadOffsetY;
        double prevHeadWorldZ = startZ + prevHeadOffsetZ;

        handleBlockImpact(rayTraceBlocks(prevHeadWorldX, prevHeadWorldY, prevHeadWorldZ, headX, headY, headZ), true);
    }

    private void checkEntityCollision(double headX, double headY, double headZ) {
        // Swept head hitbox prevents misses when beam head moves quickly between ticks.
        double prevHeadWorldX = startX + prevHeadOffsetX;
        double prevHeadWorldY = startY + prevHeadOffsetY;
        double prevHeadWorldZ = startZ + prevHeadOffsetZ;
        double hitSize = Math.max(0.05, headSize * 0.5);
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            Math.min(prevHeadWorldX, headX) - hitSize,
            Math.min(prevHeadWorldY, headY) - hitSize,
            Math.min(prevHeadWorldZ, headZ) - hitSize,
            Math.max(prevHeadWorldX, headX) + hitSize,
            Math.max(prevHeadWorldY, headY) + hitSize,
            Math.max(prevHeadWorldZ, headZ) + hitSize
        );

        processEntitiesInHitBox(hitBox, headX, headY, headZ);
    }

    // ==================== DEBUG ====================

    @Override
    protected String debugLogExtra() {
        double headWorldX = startX + headOffsetX;
        double headWorldY = startY + headOffsetY;
        double headWorldZ = startZ + headOffsetZ;
        return String.format("headOff=(%.2f,%.2f,%.2f) prevHead=(%.2f,%.2f,%.2f) " +
                "origin=(%.2f,%.2f,%.2f) headWorld=(%.2f,%.2f,%.2f) " +
                "trail=%d mode=%s anchored=%b beamW=%.2f headSz=%.2f " +
                "motion=(%.3f,%.3f,%.3f)",
            headOffsetX, headOffsetY, headOffsetZ,
            prevHeadOffsetX, prevHeadOffsetY, prevHeadOffsetZ,
            startX, startY, startZ,
            headWorldX, headWorldY, headWorldZ,
            getActiveTrailSize(),
            beamMode.name(), isAnchoredMode(), beamWidth, headSize,
            motionX, motionY, motionZ);
    }

    // ==================== GETTERS FOR RENDERER ====================

    public float getBeamWidth() {
        return beamWidth;
    }

    public float getInterpolatedBeamWidth(float partialTicks) {
        return prevRenderBeamWidth + (renderBeamWidth - prevRenderBeamWidth) * partialTicks;
    }

    public void setBeamWidth(float beamWidth) {
        this.beamWidth = beamWidth;
    }

    public float getHeadSize() {
        return headSize;
    }

    public float getInterpolatedHeadSize(float partialTicks) {
        return prevRenderHeadSize + (renderHeadSize - prevRenderHeadSize) * partialTicks;
    }

    public void setHeadSize(float headSize) {
        this.headSize = headSize;
    }

    public void setAttachedToOwner(boolean attached) {
        setBeamMode(modeFromAnchored(attached));
    }

    public boolean isFreeAim() {
        return freeAim;
    }

    public void setFreeAim(boolean freeAim) {
        this.freeAim = freeAim;
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
        if (trailStartIndex <= 0) {
            return trailPoints;
        }
        if (trailStartIndex >= trailPoints.size()) {
            return Collections.emptyList();
        }
        return trailPoints.subList(trailStartIndex, trailPoints.size());
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
        return isAnchoredMode();
    }

    /**
     * Whether the beam is attached to its owner (anchored mode).
     */
    public boolean isAttachedToOwner() {
        return isAnchoredMode();
    }

    /**
     * Whether trail should fade (comet effect for non-anchored beams).
     */
    public boolean hasFadingTrail() {
        return isFadingMode();
    }

    /**
     * Get trail point ages for fading calculation.
     */
    public List<Integer> getTrailPointAges() {
        if (trailStartIndex <= 0) {
            return trailPointAges;
        }
        if (trailStartIndex >= trailPointAges.size()) {
            return Collections.emptyList();
        }
        return trailPointAges.subList(trailStartIndex, trailPointAges.size());
    }

    /**
     * Get trail fade time in ticks.
     */
    public int getTrailFadeTime() {
        return trailFadeTime;
    }

    // ==================== REFLECTION ====================

    @Override
    protected boolean reflectFromBarrier(EntityEnergyBarrier barrier, float reflectStrengthPct) {
        boolean reflected = super.reflectFromBarrier(barrier, reflectStrengthPct);
        if (reflected) {
            // Switch to FREE_TRAIL: detach from caster (tail-less), trail fades out.
            setBeamMode(BeamMode.FREE_TRAIL);

            // Reset origin to the reflection point (current head position).
            startX = posX;
            startY = posY;
            startZ = posZ;
            resetHeadOffsets();
            resetTrailStorage();

            // Free aim only works for the original caster
            freeAim = false;

            // If targeting owner, keep homing so beam tracks back to caster
            if (!barrier.getBarrierData().isTargetOwner()) {
                homingData.setHoming(false);
                setTargetEntityId(-1);
            }
        }
        return reflected;
    }

    @Override
    protected void writeProjectileReflectionData(NBTTagCompound nbt) {
        nbt.setByte("BeamMode", (byte) beamMode.ordinal());
        nbt.setDouble("StartX", startX);
        nbt.setDouble("StartY", startY);
        nbt.setDouble("StartZ", startZ);
    }

    @Override
    protected void applyProjectileReflectionData(NBTTagCompound nbt) {
        int mode = nbt.getByte("BeamMode");
        setBeamMode(mode >= 0 && mode < BeamMode.values().length ? BeamMode.values()[mode] : BeamMode.FREE_TRAIL);
        startX = nbt.getDouble("StartX");
        startY = nbt.getDouble("StartY");
        startZ = nbt.getDouble("StartZ");
        resetHeadOffsets();
        resetTrailStorage();
        homingData.setHoming(false);
        freeAim = false;
        setTargetEntityId(-1);
    }

    // ==================== PROPERTY SYNC ====================

    @Override
    protected void writeProjectileClientSyncData(NBTTagCompound nbt) {
        nbt.setFloat("BeamWidth", beamWidth);
        nbt.setFloat("HeadSize", headSize);
        nbt.setBoolean("AttachedToOwner", isAttachedToOwner());
    }

    @Override
    protected void applyProjectileClientSyncData(NBTTagCompound nbt) {
        beamWidth = nbt.getFloat("BeamWidth");
        headSize = nbt.getFloat("HeadSize");
        setAttachedToOwner(nbt.getBoolean("AttachedToOwner"));
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.beamWidth = sanitize(nbt.hasKey("BeamWidth") ? nbt.getFloat("BeamWidth") : 0.3f, 0.3f, MAX_ENTITY_SIZE);
        this.headSize = sanitize(nbt.hasKey("HeadSize") ? nbt.getFloat("HeadSize") : 0.5f, 0.5f, MAX_ENTITY_SIZE);
        this.renderBeamWidth = this.beamWidth;
        this.renderHeadSize = this.headSize;
        this.prevRenderBeamWidth = this.beamWidth;
        this.prevRenderHeadSize = this.headSize;
        this.headOffsetX = nbt.hasKey("HeadOffsetX") ? nbt.getDouble("HeadOffsetX") : 0;
        this.headOffsetY = nbt.hasKey("HeadOffsetY") ? nbt.getDouble("HeadOffsetY") : 0;
        this.headOffsetZ = nbt.hasKey("HeadOffsetZ") ? nbt.getDouble("HeadOffsetZ") : 0;
        this.prevHeadOffsetX = this.headOffsetX;
        this.prevHeadOffsetY = this.headOffsetY;
        this.prevHeadOffsetZ = this.headOffsetZ;
        readChargingNBT(nbt);
        this.chargeOffsetDistance = nbt.hasKey("ChargeOffsetDistance") ? nbt.getFloat("ChargeOffsetDistance") : 1.0f;
        this.trailFadeTime = nbt.hasKey("TrailFadeTime") ? nbt.getInteger("TrailFadeTime") : 20;
        this.freeAim = nbt.getBoolean("FreeAim");

        boolean attachedLegacy = !nbt.hasKey("AttachedToOwner") || nbt.getBoolean("AttachedToOwner");
        BeamMode loadedMode = modeFromAnchored(attachedLegacy);
        if (nbt.hasKey("BeamMode")) {
            try {
                loadedMode = BeamMode.valueOf(nbt.getString("BeamMode"));
            } catch (IllegalArgumentException ignored) {
                loadedMode = modeFromAnchored(attachedLegacy);
            }
        } else if (nbt.hasKey("FadeTrail")) {
            loadedMode = nbt.getBoolean("FadeTrail") ? BeamMode.FREE_TRAIL : modeFromAnchored(attachedLegacy);
        }

        // Read trail points (relative to origin)
        trailPoints.clear();
        trailPointAges.clear();
        trailStartIndex = 0;
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

        // Read trail point ages (must match trailPoints size for fading trail)
        if (nbt.hasKey("TrailAges")) {
            int[] ages = nbt.getIntArray("TrailAges");
            for (int age : ages) {
                trailPointAges.add(age);
            }
        }
        setBeamMode(loadedMode);
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("BeamWidth", beamWidth);
        nbt.setFloat("HeadSize", headSize);
        nbt.setDouble("HeadOffsetX", headOffsetX);
        nbt.setDouble("HeadOffsetY", headOffsetY);
        nbt.setDouble("HeadOffsetZ", headOffsetZ);
        nbt.setBoolean("AttachedToOwner", isAnchoredMode());
        nbt.setString("BeamMode", beamMode.name());
        writeChargingNBT(nbt);
        nbt.setFloat("ChargeOffsetDistance", chargeOffsetDistance);
        nbt.setBoolean("FreeAim", freeAim);
        nbt.setBoolean("FadeTrail", isFadingMode());
        nbt.setInteger("TrailFadeTime", trailFadeTime);

        // Write trail points
        NBTTagList trailList = new NBTTagList();
        for (int i = trailStartIndex; i < trailPoints.size(); i++) {
            Vec3 point = trailPoints.get(i);
            NBTTagCompound pointNbt = new NBTTagCompound();
            pointNbt.setDouble("X", point.xCoord);
            pointNbt.setDouble("Y", point.yCoord);
            pointNbt.setDouble("Z", point.zCoord);
            trailList.appendTag(pointNbt);
        }
        nbt.setTag("Trail", trailList);

        // Write trail point ages for fading trail sync
        int activeSize = isFadingMode() ? Math.max(0, trailPoints.size() - trailStartIndex) : 0;
        int[] ages = new int[activeSize];
        for (int i = 0; i < activeSize; i++) {
            int idx = trailStartIndex + i;
            ages[i] = idx < trailPointAges.size() ? trailPointAges.get(idx) : 0;
        }
        nbt.setIntArray("TrailAges", ages);
    }
}
