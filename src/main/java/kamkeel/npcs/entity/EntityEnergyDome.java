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

    protected void setSize(float width, float height){
        super.setSize(width, height);

        this.ySize = 0.0F;
        this.yOffset = this.height * 0.5F;
    }


    // ==================== POSITION / BOUNDING BOX ====================

    /**
     * Override setPosition to maintain dome-sized bounding box.
     * MC's default setPosition() resets BB based on width/height fields.
     * Network sync calls setPosition(), which would shrink the BB.
     * This ensures melee targeting always works against the full dome sphere.
     */
    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
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
                // Sync prevPos with owner's prevPos for smooth interpolation
                this.prevPosX = owner.prevPosX;
                this.prevPosY = owner.prevPosY;
                this.prevPosZ = owner.prevPosZ;
            }
        }

        // Handle charging animation (both sides for smooth rendering)
        if (isCharging()) {
            chargeTick++;
            float progress = getChargeProgress();
            this.domeRadius = targetDomeRadius * progress;
            // Refresh BB for current charge radius
            this.setPosition(posX, posY, posZ);
            return; // Don't tick duration/death during charging
        }

        ticksAlive++;

        if (updateBarrierTick()) return;

        // Knockback velocity push every tick
        if (!worldObj.isRemote && barrierData.knockbackEnabled) {
            knockbackEntities();
        }
    }

    // ==================== INCOMING CHECK ====================

    /**
     * Swept ray-sphere intersection test.
     * Tests if the line segment from prevPos to currPos crosses the dome sphere boundary
     * from outside. Handles fast projectiles that skip through in a single tick.
     *
     * @return true if the segment enters the sphere from outside
     */
    private boolean isIncomingRay(
        double currX, double currY, double currZ,
        double prevX, double prevY, double prevZ,
        int projOwnerEntityId)
    {
        if (isCharging()) return false;
        if (projOwnerEntityId == this.ownerEntityId) return false;

        // Ray: P(t) = prev + t * (curr - prev), t in [0, 1]
        double rayDirX = currX - prevX;
        double rayDirY = currY - prevY;
        double rayDirZ = currZ - prevZ;

        // Vector from dome center to ray origin (prevPos)
        double ocX = prevX - this.posX;
        double ocY = prevY - this.posY;
        double ocZ = prevZ - this.posZ;

        double r = domeRadius;

        // Quadratic: a*t^2 + b*t + c = 0
        double a = rayDirX * rayDirX + rayDirY * rayDirY + rayDirZ * rayDirZ;
        double b = 2.0 * (ocX * rayDirX + ocY * rayDirY + ocZ * rayDirZ);
        double c = (ocX * ocX + ocY * ocY + ocZ * ocZ) - r * r;

        // c > 0: prevPos is outside sphere; c <= 0: inside (don't block)
        if (c <= 0) return false;

        if (a < 1e-10) return false; // No movement

        double discriminant = b * b - 4.0 * a * c;
        if (discriminant < 0) return false; // Ray misses sphere entirely

        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2.0 * a); // Entry point (first intersection)

        // Entry must be within this tick's movement segment [0, 1]
        return t1 >= 0.0 && t1 <= 1.0;
    }

    @Override
    public boolean isIncomingProjectile(EntityEnergyProjectile projectile) {
        // Faction check: don't block same-faction NPC projectiles
        Entity owner = getOwnerEntity();
        Entity projOwner = projectile.getOwnerEntity();
        if (owner instanceof noppes.npcs.entity.EntityNPCInterface && projOwner instanceof noppes.npcs.entity.EntityNPCInterface) {
            if (((noppes.npcs.entity.EntityNPCInterface) owner).faction.id == ((noppes.npcs.entity.EntityNPCInterface) projOwner).faction.id) {
                return false;
            }
        }

        double prevX = projectile.posX - projectile.motionX;
        double prevY = projectile.posY - projectile.motionY;
        double prevZ = projectile.posZ - projectile.motionZ;

        return isIncomingRay(
            projectile.posX, projectile.posY, projectile.posZ,
            prevX, prevY, prevZ,
            projectile.getOwnerEntityId());
    }

    @Override
    public boolean isIncomingGenericProjectile(
        double posX, double posY, double posZ,
        double motionX, double motionY, double motionZ,
        double prevPosX, double prevPosY, double prevPosZ,
        int ownerEntityId)
    {
        return isIncomingRay(posX, posY, posZ, prevPosX, prevPosY, prevPosZ, ownerEntityId);
    }

    @Override
    public float getMaxExtent() {
        return domeRadius;
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

    // ==================== KNOCKBACK (DBO-style motion prediction) ====================

    /**
     * DBO Ki Shield style knockback — unified algorithm with geometric jail:
     * <p>
     * When jailEnabled = false (default):
     * - OUTSIDE entities: predict future position, if it would cross into dome push outward.
     *   If close and approaching, apply soft outward push. Full motion replacement.
     * - INSIDE entities: NOT affected (free movement). Dome only blocks entry.
     * <p>
     * When jailEnabled = true:
     * - OUTSIDE entities: pushed outward (same as above, prevents entry).
     * - INSIDE entities: pushed inward when near edge (geometric containment).
     *   Any entity inside stays inside, any entity outside stays outside.
     * <p>
     * For moving domes (followCaster), dome velocity is accounted for by using
     * relative velocity (entity motion minus dome motion) for predictions.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void knockbackEntities() {
        float margin = 2.0f;
        float r = domeRadius;
        float strength = barrierData.knockbackStrength;
        boolean jail = barrierData.jailEnabled;
        float jailThresh = barrierData.jailThreshold;

        // Dome velocity for moving domes (followCaster)
        double domeVelX = posX - prevPosX;
        double domeVelY = posY - prevPosY;
        double domeVelZ = posZ - prevPosZ;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - r - margin, posY - r - margin, posZ - r - margin,
            posX + r + margin, posY + r + margin, posZ + r + margin
        );

        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        for (EntityLivingBase ent : entities) {
            if (ent.getEntityId() == ownerEntityId) continue;
            if (!isKnockbackTarget(ent)) continue;
            if (isAllyOfOwner(ent)) continue;

            // Current position relative to dome center
            double dx = ent.posX - posX;
            double dy = (ent.posY + ent.height * 0.5) - posY;
            double dz = ent.posZ - posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.01) continue;

            // Normalized direction from dome center toward entity (outward)
            double nx = dx / dist;
            double ny = dy / dist;
            double nz = dz / dist;

            // Entity velocity relative to dome (accounts for moving domes)
            double relVelX = ent.motionX - domeVelX;
            double relVelY = ent.motionY - domeVelY;
            double relVelZ = ent.motionZ - domeVelZ;

            boolean isInside = dist < r;

            if (isInside && jail) {
                // --- INSIDE + JAIL ENABLED: push toward center when near dome edge ---
                if (dist > r * jailThresh) {
                    double overshoot = dist - r * jailThresh;
                    double force = (overshoot + 0.1) * strength * 0.2;
                    // Set motion to dome velocity + inward push (keeps entity moving with dome)
                    ent.motionX = domeVelX - nx * force;
                    ent.motionY = domeVelY - ny * force;
                    ent.motionZ = domeVelZ - nz * force;
                    ent.velocityChanged = true;
                } else {
                    // Predict if entity would escape next tick (relative to dome)
                    double futDx = dx + relVelX;
                    double futDy = dy + relVelY;
                    double futDz = dz + relVelZ;
                    double futDist = Math.sqrt(futDx * futDx + futDy * futDy + futDz * futDz);
                    if (futDist >= r) {
                        // Would escape: cancel outward motion, match dome velocity
                        double radialVel = relVelX * nx + relVelY * ny + relVelZ * nz;
                        if (radialVel > 0) {
                            ent.motionX -= radialVel * nx;
                            ent.motionY -= radialVel * ny;
                            ent.motionZ -= radialVel * nz;
                            ent.velocityChanged = true;
                        }
                    }
                }
            } else if (!isInside) {
                // --- OUTSIDE: push outward (prevent entry) ---

                // Predicted future distance from center (relative to dome movement)
                double futDx = dx + relVelX;
                double futDy = dy + relVelY;
                double futDz = dz + relVelZ;
                double futDist = Math.sqrt(futDx * futDx + futDy * futDy + futDz * futDz);

                // Radial velocity relative to dome (positive = moving away)
                double radialVel = relVelX * nx + relVelY * ny + relVelZ * nz;

                if (futDist < r) {
                    // Would cross INTO dome: hard push outward proportional to overshoot
                    double overshoot = r - futDist;
                    double force = (overshoot * 2.0 + 0.05) * strength * 0.1;
                    ent.motionX = domeVelX + nx * force;
                    ent.motionY = domeVelY + ny * force * 0.5;
                    ent.motionZ = domeVelZ + nz * force;
                    ent.velocityChanged = true;
                } else if (radialVel < 0 && dist < r + margin) {
                    // Close and approaching: soft push outward proportional to proximity
                    double proximity = 1.0 - ((dist - r) / margin);
                    double force = proximity * strength * 0.1;
                    ent.motionX = domeVelX + nx * force;
                    ent.motionY = domeVelY + ny * force * 0.5;
                    ent.motionZ = domeVelZ + nz * force;
                    ent.velocityChanged = true;
                }
            }
            // else: INSIDE + no jail → do nothing (free movement)
        }
    }

    // ==================== CONTAINMENT CHECK ====================

    @Override
    public boolean isEntityInside(Entity entity) {
        if (entity == null) return false;
        double dx = entity.posX - posX;
        double dy = (entity.posY + entity.height * 0.5) - posY;
        double dz = entity.posZ - posZ;
        return dx * dx + dy * dy + dz * dz < domeRadius * domeRadius;
    }

    // ==================== MELEE (spherical check) ====================

    /**
     * Reject melee hits that land on the cubic bounding box but are outside the actual sphere.
     * Uses distance from the sphere surface (not center) for accurate spherical rejection.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getEntity() != null) {
            Entity attacker = source.getEntity();
            double dx = attacker.posX - this.posX;
            double dy = (attacker.posY + attacker.height * 0.5) - this.posY;
            double dz = attacker.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double surfaceDist = Math.abs(dist - domeRadius);
            // Allow hit only if attacker is within melee reach of the sphere surface
            if (surfaceDist > 5.0) return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    // ==================== BOUNDING BOX ====================

    /**
     * Small expansion on top of the dome-sized bounding box for comfortable
     * melee targeting. The actual BB is already dome-sized (set in onUpdate),
     * so this only adds a small reach margin for crosshair ray-testing.
     */
    @Override
    public float getCollisionBorderSize() {
        return 1.0f;
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
