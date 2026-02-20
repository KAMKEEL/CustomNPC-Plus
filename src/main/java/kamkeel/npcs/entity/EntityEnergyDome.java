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
import net.minecraft.util.DamageSource;
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
        this.setSize(1.0f, 1.0f);
    }

    public EntityEnergyDome(World world, EntityLivingBase owner, double x, double y, double z,
                            float domeRadius, EnergyDisplayData display, EnergyLightningData lightning,
                            EnergyBarrierData barrier) {
        this(world);
        this.ownerEntityId = owner.getEntityId();
        this.domeRadius = domeRadius;
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.currentHealth = barrier.maxHealth;
        this.setPosition(x, y, z);
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
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Check if projectile is outside dome radius
        if (dist > domeRadius) return false;

        // Approximate the projectile's position before last tick's movement.
        // checkBarrierCollision runs BEFORE updateProjectile, so posX is post-last-movement.
        // posX - motionX gives the pre-movement position (works for moveEntity and setPosition).
        double prevDx = (projectile.posX - projectile.motionX) - this.posX;
        double prevDy = (projectile.posY - projectile.motionY) - this.posY;
        double prevDz = (projectile.posZ - projectile.motionZ) - this.posZ;
        double prevDist = Math.sqrt(prevDx * prevDx + prevDy * prevDy + prevDz * prevDz);

        // Only block projectiles that crossed the dome boundary from outside.
        // Projectiles spawned inside (prevDist < radius) pass through freely.
        if (prevDist < domeRadius) return false;

        // Projectile entered from outside — check it's still moving inward
        double dot = dx * projectile.motionX + dy * projectile.motionY + dz * projectile.motionZ;
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

    // ==================== MELEE (spherical check) ====================

    /**
     * Reject melee hits that land on the cubic bounding box but are outside the actual sphere.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getEntity() != null) {
            Entity attacker = source.getEntity();
            double dx = attacker.posX - this.posX;
            double dy = (attacker.posY + attacker.height * 0.5) - this.posY;
            double dz = attacker.posZ - this.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            // Allow hit only if attacker is within sphere + melee reach margin
            double maxDist = domeRadius + 5.0;
            if (distSq > maxDist * maxDist) return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    // ==================== BOUNDING BOX ====================

    /**
     * Expands the ray-trace targeting area so players can melee-hit the dome
     * from anywhere near its surface, not just the tiny 1x1 center.
     * This is used by EntityRenderer.getMouseOver() to expand the entity BB
     * for crosshair targeting without modifying the actual bounding box.
     */
    @Override
    public float getCollisionBorderSize() {
        return domeRadius;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = domeRadius * 8.0D;
        d *= 64.0D;
        return distance < d * d;
    }

    // ==================== DISTANCE (for render sorting) ====================

    /**
     * Returns squared distance from the given point to the nearest point on the dome sphere surface.
     * Uses absolute distance to surface: |centerDist - radius|.
     * <p>
     * Inside: nearest surface is (radius - centerDist) away.
     * Outside: nearest surface is (centerDist - radius) away.
     * This ensures entities inside the dome sort correctly relative to the dome shell.
     */
    @Override
    public double getDistanceSq(double x, double y, double z) {
        double dx = this.posX - x;
        double dy = this.posY - y;
        double dz = this.posZ - z;
        double centerDist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double surfaceDist = Math.abs(centerDist - domeRadius);
        return surfaceDist * surfaceDist;
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
