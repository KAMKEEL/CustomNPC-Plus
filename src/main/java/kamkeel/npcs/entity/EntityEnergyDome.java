package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Energy Dome entity - a spherical barrier that blocks incoming energy projectiles.
 * Centered on the caster's position at time of casting.
 * Only blocks incoming attacks (not outgoing from allies inside).
 * Extends EntityEnergyBarrier for shared barrier logic.
 */
public class EntityEnergyDome extends EntityEnergyBarrier {

    // ==================== DOME-SPECIFIC PROPERTIES ====================
    protected float domeRadius = 5.0f;
    protected float targetDomeRadius = 5.0f;
    protected boolean followCaster = false;

    public EntityEnergyDome(World world) {
        super(world);
        this.setSize(1.0f, 1.0f); // Hitbox doesn't really matter, we use radius checks
    }

    public EntityEnergyDome(World world, EntityLivingBase owner, double x, double y, double z,
                            float domeRadius, EnergyDisplayData display, EnergyLightningData lightning,
                            EnergyBarrierData barrier) {
        this(world);
        this.setPosition(x, y, z);
        this.ownerEntityId = owner.getEntityId();
        this.domeRadius = domeRadius;
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.currentHealth = barrier.maxHealth;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();

        // Follow caster: both sides for smooth interpolated rendering
        if (followCaster) {
            Entity owner = ownerEntityId >= 0 ? worldObj.getEntityByID(ownerEntityId) : null;
            if (owner != null) {
                this.setPosition(owner.posX, owner.posY, owner.posZ);
            }
        }

        // Handle charging animation (both sides for smooth rendering)
        if (isCharging()) {
            chargeTick++;
            float progress = getChargeProgress();
            this.domeRadius = targetDomeRadius * progress;
            return; // Don't tick duration/death during charging
        }

        ticksAlive++;

        if (updateBarrierTick()) return;

        // Knockback entities inside the dome
        if (!worldObj.isRemote && barrierData.knockbackEnabled) {
            knockbackEntities();
        }
    }

    // ==================== INCOMING CHECK ====================

    /**
     * Check if a projectile at the given position is entering this dome from outside.
     * Only blocks incoming projectiles (dot product check).
     */
    @Override
    public boolean isIncomingProjectile(EntityEnergyProjectile projectile) {
        // Don't block during charging phase
        if (isCharging()) return false;
        // Don't block projectiles from the dome's owner
        if (projectile.getOwnerEntityId() == this.ownerEntityId) return false;

        // Don't block projectiles from same-faction NPCs
        Entity owner = getOwnerEntity();
        Entity projOwner = projectile.getOwnerEntity();
        if (owner instanceof noppes.npcs.entity.EntityNPCInterface && projOwner instanceof noppes.npcs.entity.EntityNPCInterface) {
            if (((noppes.npcs.entity.EntityNPCInterface) owner).faction.id == ((noppes.npcs.entity.EntityNPCInterface) projOwner).faction.id) {
                return false;
            }
        }

        double dx = projectile.posX - this.posX;
        double dy = projectile.posY - this.posY;
        double dz = projectile.posZ - this.posZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        // Check if projectile is within dome radius
        if (distSq > domeRadius * domeRadius) return false;

        // Check if projectile is moving inward (dot product of velocity and position relative to center)
        double dot = dx * projectile.motionX + dy * projectile.motionY + dz * projectile.motionZ;
        // Negative dot = moving toward center = incoming
        return dot < 0;
    }

    // ==================== CHARGING ====================

    @Override
    public void setupCharging(int duration) {
        this.targetDomeRadius = this.domeRadius;
        this.domeRadius = 0.01f;
        this.chargeDuration = duration;
        this.chargeTick = 0;
        setCharging(true);
    }

    @Override
    public void finishCharging() {
        this.domeRadius = targetDomeRadius;
        setCharging(false);
    }

    // ==================== KNOCKBACK ====================

    /**
     * Push entities away from the dome surface.
     * Entities outside are pushed outward, entities inside are pushed inward (containment).
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void knockbackEntities() {
        float margin = 1.0f;
        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - domeRadius - margin, posY - domeRadius - margin, posZ - domeRadius - margin,
            posX + domeRadius + margin, posY + domeRadius + margin, posZ + domeRadius + margin
        );

        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        for (EntityLivingBase ent : entities) {
            if (ent.getEntityId() == ownerEntityId) continue;
            if (!isKnockbackTarget(ent)) continue;

            double dx = ent.posX - posX;
            double dy = (ent.posY + ent.height * 0.5) - posY;
            double dz = ent.posZ - posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist < 0.01) continue;

            // Only affect entities near the dome surface
            if (dist < domeRadius + margin && dist > domeRadius - margin) {
                double pushStrength = barrierData.knockbackStrength * 0.5;
                double pushDir = dist >= domeRadius ? 1.0 : -1.0; // Outside = outward, Inside = inward
                ent.addVelocity(
                    (dx / dist) * pushStrength * pushDir,
                    0.05 * pushDir,
                    (dz / dist) * pushStrength * pushDir
                );
                ent.velocityChanged = true;
            }
        }
    }

    // ==================== BOUNDING BOX ====================

    @Override
    public AxisAlignedBB getBoundingBox() {
        if (barrierData.meleeEnabled) {
            return AxisAlignedBB.getBoundingBox(
                posX - domeRadius, posY - domeRadius, posZ - domeRadius,
                posX + domeRadius, posY + domeRadius, posZ + domeRadius
            );
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = domeRadius * 8.0D;
        d *= 64.0D;
        return distance < d * d;
    }

    // ==================== GETTERS ====================

    public float getDomeRadius() {
        return domeRadius;
    }

    public void setDomeRadius(float radius) {
        this.domeRadius = Math.max(0.1f, radius);
    }

    public boolean isFollowCaster() {
        return followCaster;
    }

    public void setFollowCaster(boolean follow) {
        this.followCaster = follow;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        readBarrierBaseNBT(nbt);
        this.domeRadius = sanitize(nbt.getFloat("DomeRadius"), 5.0f, MAX_ENTITY_RADIUS);
        this.targetDomeRadius = sanitize(nbt.hasKey("TargetDomeRadius") ? nbt.getFloat("TargetDomeRadius") : domeRadius, 5.0f, MAX_ENTITY_RADIUS);
        this.followCaster = nbt.hasKey("FollowCaster") && nbt.getBoolean("FollowCaster");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        writeBarrierBaseNBT(nbt);
        nbt.setFloat("DomeRadius", domeRadius);
        nbt.setFloat("TargetDomeRadius", targetDomeRadius);
        nbt.setBoolean("FollowCaster", followCaster);
    }
}
