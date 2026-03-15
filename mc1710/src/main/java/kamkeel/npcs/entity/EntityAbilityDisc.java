package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Disc projectile - flat spinning disc with optional boomerang behavior.
 * Has a wider, thinner hitbox compared to Orb.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityDisc extends EntityEnergyProjectile {

    // Disc-specific movement propertie

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40; // Ticks before returning
    private boolean returning = false;
    private int ticksSinceMiss = 0;

    // Disc shape properties (target values)
    private float discRadius = 1.0f; // Width of disc
    private float discThickness = 0.2f; // Height of disc

    // Lerp-smoothed render values
    private float renderDiscRadius = 1.0f;
    private float renderDiscThickness = 0.2f;
    private float prevRenderDiscRadius = 1.0f;
    private float prevRenderDiscThickness = 0.2f;

    // Boomerang owner-gone tracking
    private int returnOwnerNullTicks = 0;
    private boolean vertical = false; // false = horizontal (flat), true = vertical (thin edge forward)

    // Cached travel direction for smooth rendering when motion is near zero
    private float lastTravelYaw = 0.0f;

    // Charging state (disc-specific target sizes)
    private float targetDiscRadius = 1.0f; // Full radius to grow to during charging
    private float targetDiscThickness = 0.2f; // Full thickness to grow to during charging

    public EntityAbilityDisc(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters using data classes.
     */
    public EntityAbilityDisc(World world, EntityLivingBase owner, EntityLivingBase target,
                             double x, double y, double z,
                             float discRadius, float discThickness,
                             EnergyDisplayData display, EnergyCombatData combat,
                             EnergyHomingData homing, EnergyLightningData lightning,
                             EnergyLifespanData lifespan,
                             boolean boomerang, int boomerangDelay) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z, 1.0f, display, combat, lightning, lifespan);

        this.homingData = homing != null ? homing.copy() : new EnergyHomingData();

        this.boomerang = boomerang;
        this.boomerangDelay = boomerangDelay;

        this.discRadius = discRadius;
        this.discThickness = discThickness;

        // Calculate initial velocity toward target
        calculateInitialVelocity(owner, target, x, y, z);
    }

    /**
     * Setup this disc in charging mode (for windup phase).
     * The disc will grow from 0 to discRadius over chargeDuration ticks.
     * Position follows the owner based on anchor point.
     */
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration, boolean vertical) {
        this.targetDiscRadius = this.discRadius;
        this.targetDiscThickness = this.discThickness;
        super.setupCharging(anchor, chargeDuration);
        this.vertical = vertical;
        this.discRadius = 0.01f;
        this.discThickness = 0.01f;
    }

    /**
     * Setup this disc in preview mode for GUI display.
     * Follows anchor point and animations like in the real game.
     * Can be fired when transitioning to active phase.
     */
    public void setupPreview(EntityLivingBase owner, float discRadius, float discThickness, EnergyDisplayData display, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration, boolean vertical) {
        setupPreviewState(owner, display, lightning, anchor, chargeDuration);
        this.vertical = vertical;

        // Store target size and start at 0 for grow effect
        this.targetDiscRadius = discRadius;
        this.targetDiscThickness = discThickness;
        this.discRadius = 0.01f;
        this.discThickness = 0.01f;
        setVisualSize(0.01f);
        setChargeOriginFromAnchor(owner, anchorData);
        clearMotion();
    }

    /**
     * Start preview firing (simulates firing toward a point in front of NPC).
     */
    public void startPreviewFiring() {
        startPreviewFiringDefault();
    }

    /**
     * Start the disc moving (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startMoving(EntityLivingBase target) {
        startMovingTowardTargetDefault(target);
    }

    @Override
    protected void updateRotation() {
        // Disc spins ONLY on Y axis (flat spin like a saw blade)
        this.rotationValY += getRotationSpeed();
        // No wobble - keep X and Z at 0 for stable flight
        if (this.rotationValY > 360.0f) this.rotationValY -= 360.0f;
    }

    @Override
    protected void updateProjectile() {
        // Save previous render values for sub-tick interpolation
        prevRenderDiscRadius = renderDiscRadius;
        prevRenderDiscThickness = renderDiscThickness;

        // Handle charging state (windup phase)
        if (isCharging()) {
            // During charging, snap render values to target (size grows smoothly already)
            renderDiscRadius = discRadius;
            renderDiscThickness = discThickness;
            updateCharging();
            return;
        }

        // Lerp render values toward target (smooth out sync packet jumps)
        renderDiscRadius += (discRadius - renderDiscRadius) * 0.15f;
        renderDiscThickness += (discThickness - renderDiscThickness) * 0.15f;

        // In preview mode, run movement on client (no server)
        if (previewMode) {
            updatePreviewMovement();
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
        handleSolidCollisionTermination();
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
                if (distTraveled >= getMaxDistance() * 0.8 || ticksSinceMiss >= boomerangDelay) {
                    returning = true;
                }
            }

            // If returning, only die when close to owner
            if (returning) {
                Entity owner = getOwnerEntity();
                if (owner != null) {
                    returnOwnerNullTicks = 0;
                    double distToOwner = Math.sqrt(
                        (posX - owner.posX) * (posX - owner.posX) +
                            (posY - owner.posY) * (posY - owner.posY) +
                            (posZ - owner.posZ) * (posZ - owner.posZ)
                    );
                    return distToOwner < 1.5;
                }
                // Owner gone while returning - grace period then die
                returnOwnerNullTicks++;
                return returnOwnerNullTicks > 100;
            }

            // Not returning yet - don't die from distance (we'll trigger return above)
            return false;
        }

        // Non-boomerang: standard max distance check
        return distTraveled >= getMaxDistance();
    }

    /**
     * Update movement in preview mode (client-side only, no damage).
     */
    private void updatePreviewMovement() {
        // Simple movement - just apply motion
        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;
    }

    private void updateReturnToOwner() {
        Entity owner = getOwnerEntity();

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
            double returnStrength = Math.min(1.0, getHomingStrength() * 2.5);
            double desiredVX = (dx / dist) * getSpeed();
            double desiredVY = (dy / dist) * getSpeed();
            double desiredVZ = (dz / dist) * getSpeed();

            motionX += (desiredVX - motionX) * returnStrength;
            motionY += (desiredVY - motionY) * returnStrength;
            motionZ += (desiredVZ - motionZ) * returnStrength;

            double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * getSpeed();
                motionY = (motionY / vLen) * getSpeed();
                motionZ = (motionZ / vLen) * getSpeed();
            }
        }
    }

    private void checkBlockCollision() {
        handleBlockImpact(rayTraceBlocks(posX, posY, posZ, posX + motionX, posY + motionY, posZ + motionZ), true);
    }

    private void checkEntityCollision() {
        // Swept disc hitbox to avoid miss-through at high speed.
        double nextX = posX + motionX;
        double nextY = posY + motionY;
        double nextZ = posZ + motionZ;

        double halfRadius = Math.max(0.05, discRadius * 0.5);
        double halfThickness = Math.max(0.03, discThickness * 0.5);
        double halfX;
        double halfY;
        double halfZ;

        if (vertical) {
            // Vertical disc: thin axis follows horizontal travel direction, tall on Y.
            double horizLen = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (horizLen > 1.0e-5) {
                double nx = motionX / horizLen;
                double nz = motionZ / horizLen;
                halfX = Math.abs(nx) * halfThickness + Math.abs(nz) * halfRadius;
                halfZ = Math.abs(nz) * halfThickness + Math.abs(nx) * halfRadius;
            } else {
                halfX = halfRadius;
                halfZ = halfRadius;
            }
            halfY = halfRadius;
        } else {
            // Horizontal disc: wide on XZ, thin on Y.
            halfX = halfRadius;
            halfY = halfThickness;
            halfZ = halfRadius;
        }

        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            Math.min(posX, nextX) - halfX, Math.min(posY, nextY) - halfY, Math.min(posZ, nextZ) - halfZ,
            Math.max(posX, nextX) + halfX, Math.max(posY, nextY) + halfY, Math.max(posZ, nextZ) + halfZ
        );

        processEntitiesInHitBox(hitBox, nextX, nextY, nextZ);
    }

    /**
     * Update during charging state - follow owner based on anchor point.
     * Overrides base to grow disc-specific radius/thickness instead of just size.
     */
    @Override
    protected void updateCharging() {
        chargeTick++;

        // Grow size based on charge progress
        float progress = getChargeProgress();
        this.discRadius = targetDiscRadius * progress;
        this.discThickness = targetDiscThickness * progress;
        this.size = 1.0f; // Size acts as a scaling factor; discRadius handles actual dimensions

        Entity owner = getOwnerEntity();
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData);
            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }

        // Update rotation during charging (disc still spins)
        updateRotation();
    }

    // ==================== DEBUG ====================

    @Override
    protected String debugLogExtra() {
        return String.format("motion=(%.3f,%.3f,%.3f) radius=%.2f thickness=%.2f " +
                "boomerang=%b returning=%b vertical=%b ticksSinceMiss=%d",
            motionX, motionY, motionZ,
            discRadius, discThickness,
            boomerang, returning, vertical, ticksSinceMiss);
    }

    // ==================== GETTERS ====================

    public float getDiscRadius() {
        return discRadius;
    }

    public float getInterpolatedDiscRadius(float partialTicks) {
        return prevRenderDiscRadius + (renderDiscRadius - prevRenderDiscRadius) * partialTicks;
    }

    public void setDiscRadius(float radius) {
        this.discRadius = radius;
    }

    public float getDiscThickness() {
        return discThickness;
    }

    public float getInterpolatedDiscThickness(float partialTicks) {
        return prevRenderDiscThickness + (renderDiscThickness - prevRenderDiscThickness) * partialTicks;
    }

    public void setDiscThickness(float thickness) {
        this.discThickness = thickness;
    }

    public boolean isBoomerang() {
        return boomerang;
    }

    public void setBoomerang(boolean boomerang) {
        this.boomerang = boomerang;
    }

    public int getBoomerangDelay() {
        return boomerangDelay;
    }

    public void setBoomerangDelay(int delay) {
        this.boomerangDelay = delay;
    }

    public boolean isReturning() {
        return returning;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * Get the yaw angle (degrees) from motion vector for vertical disc orientation.
     * During charging, uses owner's facing direction.
     */
    public float getTravelYaw() {
        if (isCharging()) {
            Entity owner = previewMode ? previewOwner : getOwnerEntity();
            if (owner != null) {
                lastTravelYaw = owner.rotationYaw;
                return lastTravelYaw;
            }
        }
        double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (speed > 0.001) {
            lastTravelYaw = (float) (Math.atan2(-motionX, motionZ) * 180.0 / Math.PI);
        }
        return lastTravelYaw;
    }

    // ==================== REFLECTION ====================

    @Override
    protected boolean reflectFromBarrier(EntityEnergyBarrier barrier, float reflectStrengthPct) {
        boolean reflected = super.reflectFromBarrier(barrier, reflectStrengthPct);
        if (reflected) {
            // Disable boomerang on reflected discs — the original owner is no longer
            // relevant and the return-to-owner logic causes erratic flight paths.
            this.boomerang = false;
            this.returning = false;
        }
        return reflected;
    }

    @Override
    protected void writeProjectileReflectionData(NBTTagCompound nbt) {
        nbt.setBoolean("Boomerang", boomerang);
        nbt.setBoolean("Returning", returning);
    }

    @Override
    protected void applyProjectileReflectionData(NBTTagCompound nbt) {
        boomerang = nbt.getBoolean("Boomerang");
        returning = nbt.getBoolean("Returning");
    }

    // ==================== PROPERTY SYNC ====================

    @Override
    protected void writeProjectileClientSyncData(NBTTagCompound nbt) {
        nbt.setFloat("DiscRadius", discRadius);
        nbt.setFloat("DiscThickness", discThickness);
        nbt.setBoolean("Vertical", vertical);
    }

    @Override
    protected void applyProjectileClientSyncData(NBTTagCompound nbt) {
        discRadius = nbt.getFloat("DiscRadius");
        discThickness = nbt.getFloat("DiscThickness");
        vertical = nbt.getBoolean("Vertical");
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.boomerang = nbt.hasKey("Boomerang") && nbt.getBoolean("Boomerang");
        this.boomerangDelay = nbt.hasKey("BoomerangDelay") ? nbt.getInteger("BoomerangDelay") : 40;
        this.discRadius = sanitize(nbt.hasKey("DiscRadius") ? nbt.getFloat("DiscRadius") : 1.0f, 1.0f, MAX_ENTITY_SIZE);
        this.discThickness = sanitize(nbt.hasKey("DiscThickness") ? nbt.getFloat("DiscThickness") : 0.2f, 0.2f, MAX_ENTITY_SIZE);
        this.renderDiscRadius = this.discRadius;
        this.renderDiscThickness = this.discThickness;
        this.prevRenderDiscRadius = this.discRadius;
        this.prevRenderDiscThickness = this.discThickness;
        this.vertical = nbt.hasKey("Vertical") ? nbt.getBoolean("Vertical") : false;
        this.returning = nbt.hasKey("Returning") && nbt.getBoolean("Returning");
        // Charging state (common fields handled by base)
        readChargingNBT(nbt);
        this.targetDiscRadius = nbt.hasKey("TargetDiscRadius") ? nbt.getFloat("TargetDiscRadius") : this.discRadius;
        this.targetDiscThickness = nbt.hasKey("TargetDiscThickness") ? nbt.getFloat("TargetDiscThickness") : this.discThickness;

        // Normalize size to 1.0 (scaling factor) — discRadius handles actual dimensions.
        // Old saves may have size = discRadius baked in from the quadratic scaling bug.
        this.size = 1.0f;
        this.renderCurrentSize = 1.0f;
        this.prevRenderSize = 1.0f;
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setBoolean("Boomerang", boomerang);
        nbt.setInteger("BoomerangDelay", boomerangDelay);
        nbt.setFloat("DiscRadius", discRadius);
        nbt.setFloat("DiscThickness", discThickness);
        nbt.setBoolean("Vertical", vertical);
        nbt.setBoolean("Returning", returning);
        // Charging state (common fields handled by base)
        writeChargingNBT(nbt);
        nbt.setFloat("TargetDiscRadius", targetDiscRadius);
        nbt.setFloat("TargetDiscThickness", targetDiscThickness);
    }
}
