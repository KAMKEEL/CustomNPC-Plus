package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;

import java.util.HashSet;
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
    protected float offsetX = 0.0f;
    protected float offsetY = 0.0f;
    protected float offsetZ = 0.0f;

    // Server-side melee detection state
    private boolean inTickMelee = false;
    private final HashSet<Integer> processedMeleeSwings = new HashSet<>();

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
        this.ownerEntityId = owner != null ? owner.getEntityId() : -1;
        this.setDomeRadius(domeRadius);
        // Defensive copy: entities must never share data objects with the source ability.
        this.displayData = display != null ? display.copy() : new EnergyDisplayData();
        this.lightningData = lightning != null ? lightning.copy() : new EnergyLightningData();
        this.barrierData = barrier != null ? barrier.copy() : new EnergyBarrierData();
        this.currentHealth = this.barrierData.maxHealth;
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
                this.setPosition(owner.posX + offsetX, owner.posY + offsetY, owner.posZ + offsetZ);
                // Sync prevPos with owner's prevPos for smooth interpolation
                this.prevPosX = owner.prevPosX + offsetX;
                this.prevPosY = owner.prevPosY + offsetY;
                this.prevPosZ = owner.prevPosZ + offsetZ;
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

        // Server-side: detect and apply melee hits from nearby players.
        // Bypasses MC's processUseEntity reach check (which uses center-to-center
        // distance and fails for domes with radius > 5).
        if (!worldObj.isRemote && barrierData.meleeEnabled) {
            processMeleeHits();
        }

        // Process entity physics every tick
        // Server: solid + knockback for all entities
        // Client: solid prediction for local player only (smooth movement blocking)
        if (barrierData.solid || barrierData.knockbackEnabled) {
            processEntityPhysics();
        }

        // Client-side: adjust BB for accurate melee targeting at all approach angles.
        // Must run AFTER all position/BB updates and BEFORE getMouseOver() in the render phase.
        if (worldObj.isRemote && barrierData.meleeEnabled && !isCharging()) {
            adjustMeleeBoundingBox();
        }

        debugLogBarrierTick();
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

        // Test the UPCOMING movement (this tick) so the barrier intercepts before
        // entity collision runs in updateProjectile(). Using the previous tick's
        // segment caused small domes to miss fast projectiles that could cross the
        // dome and hit the player in a single tick.
        double nextX = projectile.posX + projectile.motionX;
        double nextY = projectile.posY + projectile.motionY;
        double nextZ = projectile.posZ + projectile.motionZ;

        return isIncomingRay(
            nextX, nextY, nextZ,
            projectile.posX, projectile.posY, projectile.posZ,
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
    public double[] getSurfaceNormal(double hitX, double hitY, double hitZ,
                                      double velX, double velY, double velZ) {
        double nx = hitX - this.posX;
        double ny = hitY - this.posY;
        double nz = hitZ - this.posZ;
        double lenSq = nx * nx + ny * ny + nz * nz;
        if (lenSq > 1.0e-8) {
            double invLen = 1.0 / Math.sqrt(lenSq);
            return new double[]{nx * invLen, ny * invLen, nz * invLen};
        }
        // Fallback: use negative velocity direction
        double vLenSq = velX * velX + velY * velY + velZ * velZ;
        if (vLenSq > 1.0e-8) {
            double invVLen = 1.0 / Math.sqrt(vLenSq);
            return new double[]{-velX * invVLen, -velY * invVLen, -velZ * invVLen};
        }
        return new double[]{0.0, 1.0, 0.0};
    }

    @Override
    public double[] getOutsideSurfacePoint(double px, double py, double pz,
                                            double velX, double velY, double velZ,
                                            float bias) {
        double nx = px - this.posX;
        double ny = py - this.posY;
        double nz = pz - this.posZ;
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (len < 1.0e-5) {
            double vLen = Math.sqrt(velX * velX + velY * velY + velZ * velZ);
            if (vLen > 1.0e-5) {
                nx = -velX / vLen;
                ny = -velY / vLen;
                nz = -velZ / vLen;
            } else {
                nx = 0.0; ny = 1.0; nz = 0.0;
            }
            len = 1.0;
        }

        double invLen = 1.0 / len;
        double target = domeRadius + bias;
        return new double[]{
            this.posX + nx * invLen * target,
            this.posY + ny * invLen * target,
            this.posZ + nz * invLen * target
        };
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

        EntityPlayer localPlayer = getClientPredictionPlayer();

        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        for (EntityLivingBase ent : entities) {
            if (shouldSkipBarrierPhysicsTarget(ent, localPlayer)) continue;

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
                    // Entity crossed the boundary this tick — push back to original side.
                    // Use a larger buffer (1.0) to prevent flying entities from crossing
                    // again in a single tick (their AI hard-sets motionY every tick).
                    double pushDist = wasInside ? (r - 1.0) : (r + 0.3);
                    double surfaceX = posX + nx * pushDist;
                    double surfaceY = posY + ny * pushDist - ent.height * 0.5;
                    double surfaceZ = posZ + nz * pushDist;

                    // Prevent grounded entities from being teleported below the floor.
                    // The spherical surface calculation can produce a Y below ground level
                    // near the dome's equator, and setPosition bypasses collision detection.
                    if (ent.onGround && surfaceY < ent.posY) {
                        surfaceY = ent.posY;
                    }

                    teleportEntity(ent, surfaceX, surfaceY, surfaceZ);

                    // Cancel radial velocity and add corrective push (all 3 axes).
                    // The Y component is critical for the dome top where nx/nz ≈ 0:
                    // without it, flying entities get no inward push at the apex.
                    ent.motionX -= radialVel * nx;
                    ent.motionY -= radialVel * ny;
                    ent.motionZ -= radialVel * nz;
                    double pushForce = wasInside ? -0.15 : 0.15;
                    if (canPushInDirection(ent, nx * pushForce, ny * pushForce, nz * pushForce)) {
                        ent.motionX += nx * pushForce;
                        ent.motionY += ny * pushForce;
                        ent.motionZ += nz * pushForce;
                    }
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
                            if (canPushInDirection(ent, nx * pushForce, ny * pushForce, nz * pushForce)) {
                                ent.motionX += nx * pushForce;
                                ent.motionY += ny * pushForce;
                                ent.motionZ += nz * pushForce;
                            }
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

                    double pushX, pushY, pushZ;
                    if (isInside) {
                        pushX = -nx * force;
                        pushY = -ny * force * 0.5;
                        pushZ = -nz * force;
                    } else {
                        pushX = nx * force;
                        pushY = ny * force * 0.5;
                        pushZ = nz * force;
                    }

                    if (canPushInDirection(ent, pushX, pushY, pushZ)) {
                        ent.motionX += pushX;
                        ent.motionY += pushY;
                        ent.motionZ += pushZ;
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
     * Controls whether MC's entity picking (crosshair raycast) can target this dome.
     * Returns false when melee is disabled or the dome is still charging.
     * <p>
     * Inside players CAN target the dome because adjustMeleeBoundingBox places a small BB
     * at the nearest surface point, allowing mobs in other directions to still be targeted.
     */
    @Override
    public boolean canBeCollidedWith() {
        if (!barrierData.meleeEnabled) return false;
        if (isCharging()) return false;
        return true;
    }

    /**
     * Client-side: Repositions the bounding box to a small cube at the nearest sphere surface
     * point toward the player. This fixes melee targeting at diagonal approach angles where the
     * player would otherwise be inside the dome-sized cubic BB but outside the actual sphere,
     * causing MC's AABB raycast to return the far exit point (beyond reach distance).
     * <p>
     * With a small BB (2x2x2) at the surface point, even if the player is inside it at close
     * range, the exit point is at most ~1.7 blocks away — well within melee reach.
     */
    private void adjustMeleeBoundingBox() {
        EntityPlayer player = CustomNpcs.proxy.getPlayer();
        if (player == null) {
            updateBoundingBox();
            return;
        }

        double dx = player.posX - posX;
        double dy = (player.posY + player.getEyeHeight()) - posY;
        double dz = player.posZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Only adjust when player is within reasonable melee range of dome surface
        if (dist < 0.01 || dist > domeRadius + 6.0) {
            updateBoundingBox();
            return;
        }

        // Nearest point on sphere surface toward player
        double nx = dx / dist;
        double ny = dy / dist;
        double nz = dz / dist;
        double surfX = posX + nx * domeRadius;
        double surfY = posY + ny * domeRadius;
        double surfZ = posZ + nz * domeRadius;

        float s = 1.0f;
        this.boundingBox.setBounds(
            surfX - s, surfY - s, surfZ - s,
            surfX + s, surfY + s, surfZ + s
        );
    }

    /**
     * Player melee is handled by processMeleeHits (server-side tick detection) to bypass
     * MC's processUseEntity reach check which uses center-to-center distance.
     * Direct player melee ("player" damage type) is rejected unless inTickMelee is set,
     * preventing double-hits when both the C02 path and tick detection fire.
     * Non-player melee (mobs) and all other damage sources pass through with sphere validation.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // Player melee: only allow from tick-based detection
        if (!inTickMelee && "player".equals(source.damageType)) {
            return false;
        }
        if (source.getEntity() != null) {
            Entity attacker = source.getEntity();
            double dx = attacker.posX - this.posX;
            double dy = (attacker.posY + attacker.height * 0.5) - this.posY;
            double dz = attacker.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double surfaceDist = Math.abs(dist - domeRadius);
            if (surfaceDist > 5.0) return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    /**
     * Server-side melee detection. Each tick, finds players near the dome surface who are
     * swinging and looking at the dome, then applies the attack via attackTargetEntityWithCurrentItem
     * (preserving enchantments, crits, fire aspect, etc.). This bypasses MC's processUseEntity
     * reach check which uses center-to-center distance and fails for large domes.
     */
    @SuppressWarnings("unchecked")
    private void processMeleeHits() {
        float r = domeRadius;
        float maxReach = 6.0f;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - r - maxReach, posY - r - maxReach, posZ - r - maxReach,
            posX + r + maxReach, posY + r + maxReach, posZ + r + maxReach
        );

        List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, searchBox);
        for (EntityPlayer player : players) {
            if (player.getEntityId() == ownerEntityId) continue;
            if (isAllyOfOwner(player)) continue;

            int playerId = player.getEntityId();

            if (!player.isSwingInProgress) {
                processedMeleeSwings.remove(playerId);
                continue;
            }

            if (processedMeleeSwings.contains(playerId)) continue;

            // Check surface distance
            double dx = player.posX - posX;
            double dy = (player.posY + 1.62) - posY;
            double dz = player.posZ - posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            double dist = Math.sqrt(distSq);
            double surfaceDist = Math.abs(dist - r);
            if (surfaceDist > maxReach) continue;

            // Ray-sphere intersection: check if look direction hits the dome
            Vec3 look = player.getLook(1.0f);
            double b = 2.0 * (dx * look.xCoord + dy * look.yCoord + dz * look.zCoord);
            double c = distSq - (double)(r * r);
            double discriminant = b * b - 4.0 * c;
            if (discriminant < 0) continue;

            double sqrtDisc = Math.sqrt(discriminant);
            double t1 = (-b - sqrtDisc) / 2.0;
            double t2 = (-b + sqrtDisc) / 2.0;

            // Outside: t1 is entry distance. Inside: t1 < 0, t2 is exit distance.
            double hitDist = t1 >= 0 ? t1 : t2;
            if (hitDist < 0 || hitDist > maxReach) continue;

            // Apply the attack with full MC damage calculation
            processedMeleeSwings.add(playerId);
            inTickMelee = true;
            player.attackTargetEntityWithCurrentItem(this);
            inTickMelee = false;
        }
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

    // ==================== DEBUG ====================

    @Override
    protected String debugLogBarrierExtra() {
        return String.format("radius=%.2f targetRadius=%.2f follow=%b",
            domeRadius, targetDomeRadius, followCaster);
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

    public void setOffsets(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
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

    // ==================== CLIENT SYNC ====================

    @Override
    protected void writeBarrierClientSyncData(NBTTagCompound nbt) {
        nbt.setFloat("DomeRadius", domeRadius);
    }

    @Override
    protected void applyBarrierClientSyncData(NBTTagCompound nbt) {
        setDomeRadius(nbt.getFloat("DomeRadius"));
    }

    // ==================== NBT ====================


    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeBarrierBaseNBT(nbt);
        nbt.setFloat("DomeRadius", domeRadius);
        nbt.setFloat("TargetDomeRadius", targetDomeRadius);
        nbt.setBoolean("FollowCaster", followCaster);
        nbt.setFloat("OffsetX", offsetX);
        nbt.setFloat("OffsetY", offsetY);
        nbt.setFloat("OffsetZ", offsetZ);
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readBarrierBaseNBT(nbt);
        this.setDomeRadius(sanitize(nbt.getFloat("DomeRadius"), 5.0f, MAX_ENTITY_RADIUS));
        this.targetDomeRadius = sanitize(nbt.hasKey("TargetDomeRadius") ? nbt.getFloat("TargetDomeRadius") : domeRadius, 5.0f, MAX_ENTITY_RADIUS);
        this.followCaster = nbt.hasKey("FollowCaster") && nbt.getBoolean("FollowCaster");
        this.offsetX = nbt.hasKey("OffsetX") ? nbt.getFloat("OffsetX") : 0.0f;
        this.offsetY = nbt.hasKey("OffsetY") ? nbt.getFloat("OffsetY") : 0.0f;
        this.offsetZ = nbt.hasKey("OffsetZ") ? nbt.getFloat("OffsetZ") : 0.0f;
    }
}
