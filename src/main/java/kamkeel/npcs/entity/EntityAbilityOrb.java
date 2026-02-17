package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import noppes.npcs.EventHooks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * Orb projectile - spherical homing energy ball.
 * Extends EntityEnergyAbility for shared functionality.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityOrb extends EntityEnergyProjectile {

    public EntityAbilityOrb(World world) {
        super(world);
    }

    /**
     * Full constructor using data classes for grouped parameters.
     */
    public EntityAbilityOrb(World world, EntityLivingBase owner, EntityLivingBase target,
                            double x, double y, double z, float orbSize,
                            EnergyDisplayData display, EnergyCombatData combat,
                            EnergyHomingData homing, EnergyLightningData lightning,
                            EnergyLifespanData lifespan, EnergyTrajectoryData trajectory) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z, orbSize, display, combat, lightning, lifespan, trajectory);

        this.homingData = homing;

        // Calculate initial velocity toward target
        calculateInitialVelocity(owner, target, x, y, z);
    }

    /**
     * Setup this orb in preview mode for GUI display.
     * Follows anchor point and animations like in the real game.
     * Can be fired when transitioning to active phase.
     */
    public void setupPreview(EntityLivingBase owner, float orbSize, EnergyDisplayData display, EnergyLightningData lightning, EnergyAnchorData anchor, int chargeDuration) {
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);

        // Set visual properties
        this.displayData = display;
        this.lightningData = lightning;

        // Set charging state
        this.setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.anchorData = anchor;

        // Store target size and start at 0 for grow effect
        this.targetSize = orbSize;
        this.size = 0.01f;
        this.renderCurrentSize = 0.01f;
        this.prevRenderSize = 0.01f;

        // Initial position at anchor point
        Vec3 pos = AnchorPointHelper.calculateAnchorPosition(owner, anchorData);
        this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        this.prevPosX = pos.xCoord;
        this.prevPosY = pos.yCoord;
        this.prevPosZ = pos.zCoord;
        this.startX = pos.xCoord;
        this.startY = pos.yCoord;
        this.startZ = pos.zCoord;

        // Clear motion
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    /**
     * Start preview firing (simulates firing toward a point in front of NPC).
     */
    public void startPreviewFiring() {
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
        Entity owner = getOwnerEntity();
        if (owner != null) {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(0); // Fire horizontally
            motionX = -Math.sin(yaw) * Math.cos(pitch) * getSpeed();
            motionY = -Math.sin(pitch) * getSpeed();
            motionZ = Math.cos(yaw) * Math.cos(pitch) * getSpeed();
        } else {
            // Default: fire forward (positive X in model space)
            motionX = getSpeed();
            motionY = 0;
            motionZ = 0;
        }
    }

    /**
     * Start the orb moving (exit charging mode).
     * Called by ability when windup ends.
     */
    public void startMoving(EntityLivingBase target) {
        setCharging(false);

        // For player casters, snap to look vector for crosshair-aligned launch
        snapToPlayerLookVector();

        // Update start position to current position
        startX = posX;
        startY = posY;
        startZ = posZ;

        // Calculate velocity toward target
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
    protected void updateProjectile() {
        // Handle charging state (windup phase)
        if (isCharging()) {
            updateCharging();
            return;
        }

        // In preview mode, run movement on client (no server)
        if (previewMode) {
            updatePreviewMovement();
            return;
        }

        if (worldObj.isRemote) {
            handleClientInterpolation();
        } else {
            // Server-side logic
            updateMovement();
            checkBlockCollision();
            checkEntityCollision();
            this.moveEntity(motionX, motionY, motionZ);
        }

        // Check wall collision
        if (this.isCollidedHorizontally || this.isCollidedVertically) {
            if (!worldObj.isRemote) {
                hasHit = true;
                if (isExplosive()) {
                    doExplosion();
                }
            }
            this.setDead();
        }
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

    private void updateMovement() {
        if (!isTrajectoryConcluded()) {
            updateTrajectory();
        } else {
            updateHoming();
        }
    }

    private void updateTrajectory() {
        if (isTrajectoryConcluded()) return;
        // i really fucking hate math

    }

    private boolean isTrajectoryConcluded() {
        if (trajectoryData.isEmpty()) return true;

        for (int i = 0; i < trajectoryData.size(); i++) {
            if (!trajectoryData.getPath(i).isConcluded()) {
                return false;
            }
        }

        return true;
    }

    private void checkBlockCollision() {
        Vec3 currentPos = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 nextPos = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        // Use full raytrace that doesn't stop at liquids and checks all blocks
        MovingObjectPosition blockHit = worldObj.func_147447_a(currentPos, nextPos, false, true, false);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!worldObj.isRemote) {
                EventHooks.onEnergyProjectileBlockImpact(this, blockHit.blockX, blockHit.blockY, blockHit.blockZ);
            }
            hasHit = true;
            if (isExplosive()) {
                posX = blockHit.hitVec.xCoord;
                posY = blockHit.hitVec.yCoord;
                posZ = blockHit.hitVec.zCoord;
                doExplosion();
            }
            this.setDead();
        }
    }

    private void checkEntityCollision() {
        double hitSize = size * 0.5;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            posX - hitSize, posY - hitSize, posZ - hitSize,
            posX + hitSize, posY + hitSize, posZ + hitSize
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;

            hasHit = true;

            if (isExplosive()) {
                doExplosion();
            } else {
                applyDamage(entity);
            }

            this.setDead();
            return;
        }
    }

    // ==================== GETTERS ====================

    // Legacy getter for renderer compatibility
    public float getOrbSize() {
        return size;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        readChargingNBT(nbt);
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        writeChargingNBT(nbt);
    }
}
