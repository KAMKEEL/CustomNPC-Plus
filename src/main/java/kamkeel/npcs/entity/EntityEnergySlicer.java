package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyTrajectoryData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Energy Slicer entity - a thin, wide blade projectile that flies in a straight line.
 * JJK Cleave/Dismantle inspired.
 * Extends EntityEnergyAbility to leverage the existing projectile infrastructure.
 */
public class EntityEnergySlicer extends EntityEnergyProjectile {

    private float sliceWidth = 3.0f;
    private float sliceThickness = 0.15f;

    // Cached travel direction for smooth rendering when motion is near zero
    private float lastTravelYaw = 0.0f;
    private float lastTravelPitch = 0.0f;

    public EntityEnergySlicer(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters.
     */
    public EntityEnergySlicer(World world, EntityLivingBase owner, EntityLivingBase target,
                              double x, double y, double z,
                              float sliceWidth, float sliceThickness,
                              EnergyDisplayData display, EnergyCombatData combat,
                              EnergyHomingData homing, EnergyLightningData lightning,
                              EnergyLifespanData lifespan, EnergyTrajectoryData trajectory) {
        super(world);

        initProjectile(owner, target, x, y, z, sliceWidth, display, combat, lightning, lifespan, trajectory);
        this.homingData = homing;
        this.sliceWidth = sliceWidth;
        this.sliceThickness = sliceThickness;

        calculateInitialVelocity(owner, target, x, y, z);
    }

    // Targets stored during charging for grow animation
    private float targetSliceWidth = 3.0f;
    private float targetSliceThickness = 0.15f;

    /**
     * Setup charging with full target dimensions.
     */
    public void setupSlicerCharging(EnergyAnchorData anchor, int chargeDuration, float width, float thickness) {
        this.targetSliceWidth = width;
        this.targetSliceThickness = thickness;
        this.sliceWidth = 0.01f;
        this.sliceThickness = 0.01f;
        super.setupCharging(anchor, chargeDuration);
    }

    /**
     * Setup preview mode.
     */
    public void setupPreview(EntityLivingBase owner, float width, float thickness,
                             EnergyDisplayData display, EnergyLightningData lightning,
                             EnergyAnchorData anchor, int chargeDuration) {
        setupPreviewState(owner, display, lightning, anchor, chargeDuration);

        this.targetSliceWidth = width;
        this.targetSliceThickness = thickness;
        this.sliceWidth = 0.01f;
        this.sliceThickness = 0.01f;
        setVisualSize(0.01f);
        setChargeOriginFromAnchor(owner, anchorData);
        clearMotion();
    }

    /**
     * Start preview firing.
     */
    public void startPreviewFiring() {
        startPreviewFiringDefault();
    }

    /**
     * Start the slicer moving (exit charging mode).
     */
    public void startMoving(EntityLivingBase target) {
        startMovingTowardTargetDefault(target);
    }

    @Override
    protected void updateRotation() {
        // Slicer doesn't spin - it stays oriented perpendicular to travel direction
        // No rotation update needed
    }

    @Override
    protected void updateProjectile() {
        if (isCharging()) {
            updateCharging();
            return;
        }

        if (previewMode) {
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            return;
        }

        if (worldObj.isRemote) {
            handleClientInterpolation();
        } else {
            // No homing by default for slicer (straight line flight)
            if (homingData.isHoming()) {
                updateHoming();
            }

            checkBlockCollision();
            checkEntityCollision();
            this.moveEntity(motionX, motionY, motionZ);
        }

        // Wall collision
        handleSolidCollisionTermination();
    }

    @Override
    protected void updateCharging() {
        chargeTick++;
        float progress = getChargeProgress();
        this.sliceWidth = targetSliceWidth * progress;
        this.sliceThickness = targetSliceThickness * progress;
        this.size = this.sliceWidth;

        Entity owner = getOwnerEntity();
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = kamkeel.npcs.util.AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData);
            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }
    }

    private void checkBlockCollision() {
        handleBlockImpact(rayTraceBlocks(posX, posY, posZ, posX + motionX, posY + motionY, posZ + motionZ), false);
    }

    private void checkEntityCollision() {
        float halfW = sliceWidth * 0.5f;
        float halfH = sliceThickness * 0.5f;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            posX - halfW, posY - halfH, posZ - halfW,
            posX + halfW, posY + halfH, posZ + halfW
        );

        processEntitiesInHitBox(hitBox, posX, posY, posZ);
    }

    /**
     * Get the yaw angle from motion vector for orientation.
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

    /**
     * Get the pitch angle from motion vector.
     */
    public float getTravelPitch() {
        double horizSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (horizSpeed > 0.001 || Math.abs(motionY) > 0.001) {
            lastTravelPitch = (float) (-Math.atan2(motionY, horizSpeed) * 180.0 / Math.PI);
        }
        return lastTravelPitch;
    }

    // ==================== GETTERS ====================

    public float getSliceWidth() {
        return sliceWidth;
    }

    public void setSliceWidth(float width) {
        this.sliceWidth = width;
    }

    public float getSliceThickness() {
        return sliceThickness;
    }

    public void setSliceThickness(float thickness) {
        this.sliceThickness = thickness;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        readChargingNBT(nbt);
        this.sliceWidth = sanitize(nbt.hasKey("SliceWidth") ? nbt.getFloat("SliceWidth") : 3.0f, 3.0f, MAX_ENTITY_SIZE);
        this.sliceThickness = sanitize(nbt.hasKey("SliceThickness") ? nbt.getFloat("SliceThickness") : 0.15f, 0.15f, MAX_ENTITY_SIZE);
        this.targetSliceWidth = sanitize(nbt.hasKey("TargetSliceWidth") ? nbt.getFloat("TargetSliceWidth") : sliceWidth, sliceWidth, MAX_ENTITY_SIZE);
        this.targetSliceThickness = sanitize(nbt.hasKey("TargetSliceThickness") ? nbt.getFloat("TargetSliceThickness") : sliceThickness, sliceThickness, MAX_ENTITY_SIZE);
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        writeChargingNBT(nbt);
        nbt.setFloat("SliceWidth", sliceWidth);
        nbt.setFloat("SliceThickness", sliceThickness);
        nbt.setFloat("TargetSliceWidth", targetSliceWidth);
        nbt.setFloat("TargetSliceThickness", targetSliceThickness);
    }
}
