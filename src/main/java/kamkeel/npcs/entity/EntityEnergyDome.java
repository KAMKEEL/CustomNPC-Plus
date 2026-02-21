package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;

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
        this.noClip = true;
        this.stepHeight = 0.0F;
        this.setSize(1.0f, 1.0f);
    }

    public EntityEnergyDome(World world, EntityLivingBase owner, double x, double y, double z,
                            float domeRadius, EnergyDisplayData display, EnergyLightningData lightning,
                            EnergyBarrierData barrier) {
        this(world);
        this.noClip = true;
        this.stepHeight = 0.0F;
        this.ownerEntityId = owner.getEntityId();
        this.setDomeRadius(domeRadius);
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.currentHealth = barrier.maxHealth;
        this.setPosition(x, y, z);
    }

    // ==================== POSITION / BOUNDING BOX ====================

    @Override
    protected void setSize(float width, float height) {
        super.setSize(width, height);
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
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
            this.setDomeRadius(targetDomeRadius * progress);
            // Refresh BB for current charge radius
            this.setPosition(posX, posY, posZ);
            return; // Don't tick duration/death during charging
        }

        ticksAlive++;

        if (updateBarrierTick()) return;

        // Process entity physics every tick
        // Server: solid + knockback for all entities
        // Client: solid prediction for local player only (smooth movement blocking)
        if (barrierData.solid || barrierData.knockbackEnabled) {
            processEntityPhysics();
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
        this.setDomeRadius(0.01f);
        this.chargeDuration = duration;
        this.chargeTick = 0;
        setCharging(true);
    }

    @Override
    public void finishCharging() {
        this.setDomeRadius(this.targetDomeRadius);
        setCharging(false);
    }

    // ==================== ENTITY PHYSICS (Solid + Knockback) ====================

    /**
     * Processes entity physics for the dome barrier. Two independent systems:
     * <p>
     * SOLID: Hard wall — entities cannot cross the dome boundary in either direction.
     *   Uses reactive crossing detection: compares current position to previous tick
     *   to detect boundary crossings, then teleports back and applies velocity correction.
     *   Also preemptively cancels inward velocity when near the surface.
     * <p>
     * KNOCKBACK: Repulsion force — entities near the dome surface get pushed away.
     *   This is a push effect only; entities can still pass through with enough effort.
     *   Does NOT prevent passthrough — use solid for that.
     * <p>
     * For moving domes (followCaster), dome velocity is accounted for by using
     * the dome's previous position for previous-tick distance calculations.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void processEntityPhysics() {
        float r = domeRadius;
        boolean solid = barrierData.solid;
        boolean knockback = barrierData.knockbackEnabled;
        float strength = barrierData.knockbackStrength;

        float solidMargin = 1.5f;   // Zone around surface for preemptive velocity correction
        float knockbackMargin = 2.0f; // Zone around surface for knockback push

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - r - knockbackMargin, posY - r - knockbackMargin, posZ - r - knockbackMargin,
            posX + r + knockbackMargin, posY + r + knockbackMargin, posZ + r + knockbackMargin
        );

        // On client, identify local player for client-side solid prediction
        EntityPlayer localPlayer = worldObj.isRemote ? CustomNpcs.proxy.getPlayer() : null;

        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        for (EntityLivingBase ent : entities) {
            if (ent.getEntityId() == ownerEntityId) continue;
            if (isAllyOfOwner(ent)) continue;

            // Client-side: only process local player for solid prediction
            if (worldObj.isRemote && (localPlayer == null || ent != localPlayer)) continue;

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

            boolean isInside = dist < r;

            // --- SOLID: hard wall preventing boundary crossing ---
            if (solid) {
                // Previous tick: entity position relative to dome's previous position
                double prevDx = ent.prevPosX - prevPosX;
                double prevDy = (ent.prevPosY + ent.height * 0.5) - prevPosY;
                double prevDz = ent.prevPosZ - prevPosZ;
                double prevDist = Math.sqrt(prevDx * prevDx + prevDy * prevDy + prevDz * prevDz);
                boolean wasInside = prevDist < r;

                // Radial velocity (positive = moving outward from dome center)
                double radialVel = ent.motionX * nx + ent.motionY * ny + ent.motionZ * nz;

                if (wasInside != isInside) {
                    // Entity crossed the boundary this tick — push back to original side
                    double pushDist = wasInside ? (r - 0.3) : (r + 0.3);
                    double surfaceX = posX + nx * pushDist;
                    double surfaceY = posY + ny * pushDist - ent.height * 0.5;
                    double surfaceZ = posZ + nz * pushDist;
                    teleportEntity(ent, surfaceX, surfaceY, surfaceZ);

                    // Cancel radial velocity and add corrective push
                    ent.motionX -= radialVel * nx;
                    ent.motionY -= radialVel * ny;
                    ent.motionZ -= radialVel * nz;
                    double pushForce = wasInside ? -0.15 : 0.15;
                    ent.motionX += nx * pushForce;
                    ent.motionZ += nz * pushForce;
                    ent.velocityChanged = true;
                } else {
                    // Preemptive: near surface and moving toward it — cancel radial velocity
                    double surfaceDist = Math.abs(dist - r);
                    if (surfaceDist < solidMargin) {
                        boolean movingTowardSurface = (!isInside && radialVel < -0.01)
                            || (isInside && radialVel > 0.01);
                        if (movingTowardSurface) {
                            ent.motionX -= radialVel * nx;
                            ent.motionY -= radialVel * ny;
                            ent.motionZ -= radialVel * nz;
                            double pushForce = isInside ? -0.05 : 0.05;
                            ent.motionX += nx * pushForce;
                            ent.motionZ += nz * pushForce;
                            ent.velocityChanged = true;
                        }
                    }
                }
            }

            // --- KNOCKBACK: repulsion push (server only, synced via velocityChanged) ---
            if (!worldObj.isRemote && knockback) {
                double surfaceDist = Math.abs(dist - r);
                if (surfaceDist < knockbackMargin) {
                    double proximity = 1.0 - (surfaceDist / knockbackMargin);
                    double force = proximity * strength * 0.06;

                    if (isInside) {
                        // Push toward center (away from surface)
                        ent.motionX += -nx * force;
                        ent.motionY += -ny * force * 0.5;
                        ent.motionZ += -nz * force;
                    } else {
                        // Push outward (away from surface)
                        ent.motionX += nx * force;
                        ent.motionY += ny * force * 0.5;
                        ent.motionZ += nz * force;
                    }
                    ent.velocityChanged = true;
                }
            }
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
     * On the client side, the dome's large bounding box intercepts all raycasts from inside,
     * preventing the player from targeting mobs within the dome. To fix this, return false
     * when the local player is inside the dome so the raycast passes through to other entities.
     * Players outside the dome can still target it for melee normally.
     */
    @Override
    public boolean canBeCollidedWith() {
        if (!barrierData.meleeEnabled) return false;
        if (worldObj.isRemote) {
            EntityPlayer player = CustomNpcs.proxy.getPlayer();
            if (player != null && isEntityInside(player)) {
                return false;
            }
        }
        return true;
    }

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
     * No expansion needed — the bounding box is already dome-sized.
     * Returning 0 ensures players outside the dome sphere are also outside the
     * ray-testing BB, so MC's raycast returns the near-face hit (within reach)
     * rather than the far exit point (beyond reach).
     */
    @Override
    public float getCollisionBorderSize() {
        return 0.0f;
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
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    public boolean isFollowCaster() {
        return followCaster;
    }

    public void setFollowCaster(boolean follow) {
        this.followCaster = follow;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int increments) {
        // Don't do the vanilla "push up out of blocks" logic.
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        updateBoundingBox();
    }

    protected void updateBoundingBox() {
        float r = this.domeRadius;
        this.boundingBox.setBounds(
            this.posX - r, this.posY - r, this.posZ - r,
            this.posX + r, this.posY + r, this.posZ + r
        );
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        readBarrierBaseNBT(nbt);
        this.setDomeRadius(sanitize(nbt.getFloat("DomeRadius"), 5.0f, MAX_ENTITY_RADIUS));
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
