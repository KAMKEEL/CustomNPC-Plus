package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.EnergyTrajectoryData;
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
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);
        this.displayData = display;
        this.lightningData = lightning;
        this.setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.anchorData = anchor;

        this.targetSliceWidth = width;
        this.targetSliceThickness = thickness;
        this.sliceWidth = 0.01f;
        this.sliceThickness = 0.01f;
        this.size = 0.01f;
        this.renderCurrentSize = 0.01f;
        this.prevRenderSize = 0.01f;

        Vec3 pos = kamkeel.npcs.util.AnchorPointHelper.calculateAnchorPosition(owner, anchorData);
        this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        this.prevPosX = pos.xCoord;
        this.prevPosY = pos.yCoord;
        this.prevPosZ = pos.zCoord;
        this.startX = pos.xCoord;
        this.startY = pos.yCoord;
        this.startZ = pos.zCoord;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    /**
     * Start preview firing.
     */
    public void startPreviewFiring() {
        setCharging(false);
        startX = posX;
        startY = posY;
        startZ = posZ;
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        Entity owner = getOwnerEntity();
        if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            motionX = -Math.sin(yaw) * getSpeed();
            motionY = 0;
            motionZ = Math.cos(yaw) * getSpeed();
        } else {
            motionX = getSpeed();
            motionY = 0;
            motionZ = 0;
        }
    }

    /**
     * Start the slicer moving (exit charging mode).
     */
    public void startMoving(EntityLivingBase target) {
        setCharging(false);
        snapToPlayerLookVector();
        startX = posX;
        startY = posY;
        startZ = posZ;

        Entity owner = getOwnerEntity();
        if (target != null) {
            double dx = target.posX - posX;
            double dy = (target.posY + target.getEyeHeight()) - posY;
            double dz = target.posZ - posZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                motionX = (dx / len) * getSpeed();
                motionY = (dy / len) * getSpeed();
                motionZ = (dz / len) * getSpeed();
            }
        } else if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            motionX = -Math.sin(yaw) * Math.cos(pitch) * getSpeed();
            motionY = -Math.sin(pitch) * getSpeed();
            motionZ = Math.cos(yaw) * Math.cos(pitch) * getSpeed();
        }
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
            if (owner != null) return owner.rotationYaw;
        }
        double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (speed > 0.001) {
            return (float) (Math.atan2(-motionX, motionZ) * 180.0 / Math.PI);
        }
        return 0.0f;
    }

    /**
     * Get the pitch angle from motion vector.
     */
    public float getTravelPitch() {
        double horizSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (horizSpeed > 0.001 || Math.abs(motionY) > 0.001) {
            return (float) (-Math.atan2(motionY, horizSpeed) * 180.0 / Math.PI);
        }
        return 0.0f;
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
