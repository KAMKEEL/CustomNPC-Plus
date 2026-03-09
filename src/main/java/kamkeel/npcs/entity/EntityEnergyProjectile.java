package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.EnergyController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.type.energy.AbilityEnergyProjectile;
import kamkeel.npcs.controllers.data.ability.data.effect.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.enums.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.enums.HitType;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.network.packets.data.energy.ProjectileClientSyncPacket;
import kamkeel.npcs.network.packets.data.energy.ProjectileReflectPacket;
import kamkeel.npcs.network.packets.data.energyexplosion.EnergyExplosionSpawnPacket;
import kamkeel.npcs.util.AnchorPointHelper;
import kamkeel.npcs.util.AttributeAttackUtil;
import kamkeel.npcs.util.CNPCDebug;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.EventHooks;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.config.ConfigEnergy;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Base class for all ability projectiles (Orb, Disc, Beam, Laser, Slicer).
 * Provides common functionality for combat, effects, homing, and interpolation.
 * Extends EntityEnergyAbility for shared visual/owner/charging state.
 * <p>
 * Design inspired by LouisXIV's energy attack system.
 */
public abstract class EntityEnergyProjectile extends EntityEnergyAbility {
    // ==================== ACTIVE PROJECTILE TRACKING ====================
    private static final Map<Integer, List<WeakReference<EntityEnergyProjectile>>> activeProjectiles = new HashMap<>();

    public static void trackProjectile(EntityEnergyProjectile projectile) {
        int ownerId = projectile.getOwnerEntityId();
        if (ownerId < 0) return;
        List<WeakReference<EntityEnergyProjectile>> refs = activeProjectiles.get(ownerId);
        if (refs == null) {
            refs = new ArrayList<>();
            activeProjectiles.put(ownerId, refs);
        }
        refs.add(new WeakReference<>(projectile));
    }

    public static void clearAllProjectiles() {
        activeProjectiles.clear();
    }

    public static List<EntityEnergyProjectile> getActiveProjectiles(int ownerEntityId) {
        List<WeakReference<EntityEnergyProjectile>> refs = activeProjectiles.get(ownerEntityId);
        if (refs == null) return Collections.emptyList();
        List<EntityEnergyProjectile> result = new ArrayList<>();
        Iterator<WeakReference<EntityEnergyProjectile>> it = refs.iterator();
        while (it.hasNext()) {
            EntityEnergyProjectile p = it.next().get();
            if (p == null || p.isDead) {
                it.remove();
            } else {
                result.add(p);
            }
        }
        if (refs.isEmpty()) activeProjectiles.remove(ownerEntityId);
        return result;
    }

    // ==================== VISUAL PROPERTIES ====================
    protected float size = 1.0f;

    // ==================== COMBAT PROPERTIES ====================
    protected EnergyCombatData combatData = new EnergyCombatData();

    // ==================== EFFECT PROPERTIES ====================
    protected List<AbilityPotionEffect> effects = new ArrayList<>();

    // ==================== ANCHOR PROPERTIES ====================
    protected EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.FRONT);

    // ==================== LIFESPAN PROPERTIES ====================
    protected EnergyLifespanData lifespanData = new EnergyLifespanData();
    protected long deathWorldTime = -1;  // World time when entity should die (-1 = not set)

    // ==================== HOMING PROPERTIES ====================
    protected EnergyHomingData homingData = new EnergyHomingData();

    // ==================== TRACKING ====================
    protected double startX, startY, startZ;
    protected int targetEntityId = -1;

    // ==================== STATE ====================
    protected boolean reflected = false;  // Set after barrier reflection; prevents owner-tracking on reflected projectiles
    protected boolean hasHit = false;
    protected int hitCount = 0;
    protected final Set<Integer> hitOnceEntities = new HashSet<Integer>();
    protected final Map<Integer, Integer> lastHitTickByEntity = new HashMap<Integer, Integer>();
    protected static final int BARRIER_IMPACT_PAUSE_TICKS = 10;
    protected static final int BARRIER_BREAK_SPARK_TICKS = 10;
    protected static final int DW_BARRIER_SPARK_TICKS = 21;
    protected static final int DW_SYNC_INNER_COLOR = 22;
    protected static final int DW_SYNC_OUTER_COLOR = 23;
    protected int barrierImpactPauseTicks = 0;
    protected boolean barrierImpactDestroyOnResume = false;
    protected double pausedMotionX, pausedMotionY, pausedMotionZ;

    // ==================== CHARGING STATE (projectile-specific) ====================
    protected float targetSize = 1.0f;

    // ==================== ROTATION INTERPOLATION ====================
    public float prevRotationValX, prevRotationValY, prevRotationValZ;
    public float rotationValX, rotationValY, rotationValZ;

    // ==================== SIZE INTERPOLATION ====================
    protected float renderCurrentSize;
    protected float prevRenderSize;

    // ==================== POSITION INTERPOLATION ====================
    protected double interpTargetX, interpTargetY, interpTargetZ;
    protected double interpTargetMotionX, interpTargetMotionY, interpTargetMotionZ;
    protected int interpSteps;

    public EntityEnergyProjectile(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.yOffset = this.height / 2.0f;
        this.noClip = false;
    }

    @Override
    protected void entityInit() {
        super.entityInit(); // DW_CHARGING = 20
        this.dataWatcher.addObject(DW_BARRIER_SPARK_TICKS, 0);
        // displayData is not guaranteed to be initialized yet during Entity construction.
        this.dataWatcher.addObject(DW_SYNC_INNER_COLOR, 0xFFFFFF);
        this.dataWatcher.addObject(DW_SYNC_OUTER_COLOR, 0xFFFFFF);
    }

    /**
     * Initialize common properties using data classes. Call from subclass constructors.
     */
    protected void initProjectile(EntityLivingBase owner, EntityLivingBase target,
                                  double x, double y, double z, float size,
                                  EnergyDisplayData display, EnergyCombatData combat,
                                  EnergyLightningData lightning, EnergyLifespanData lifespan) {
        this.setPosition(x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;

        // Visual
        this.size = size;

        // Defensive copy: entities must never share data objects with the source ability.
        // Without copies, runtime mutations (e.g. barrier reflection changing colors/damage)
        // bleed back into the ability template and corrupt future projectiles.
        this.displayData = display != null ? display.copy() : new EnergyDisplayData();
        this.combatData = combat != null ? combat.copy() : new EnergyCombatData();
        this.lifespanData = lifespan != null ? lifespan.copy() : new EnergyLifespanData();
        this.lightningData = lightning != null ? lightning.copy() : new EnergyLightningData();
        syncProjectileColorWatchers();

        // Initialize render size
        this.renderCurrentSize = size;
        this.prevRenderSize = size;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
        d1 *= 128.0D;
        return distance < d1 * d1;
    }

    @Override
    public void onUpdate() {
        // Store previous values for interpolation
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationValX = this.rotationValX;
        this.prevRotationValY = this.rotationValY;
        this.prevRotationValZ = this.rotationValZ;
        this.prevRenderSize = this.renderCurrentSize;

        // Skip super.onUpdate() in preview mode to avoid world checks
        if (!previewMode) {
            super.onUpdate();

            // Track on first server tick
            if (ticksExisted == 1 && !worldObj.isRemote) {
                trackProjectile(this);
            }
        } else {
            // Preview entities skip super.onUpdate() but still need ticksExisted
            // for renderer pulsing effects (e.g., sin(ticksExisted + partialTicks)).
            this.ticksExisted++;

            // Preview-specific safety: self-destruct if charging has exceeded duration + grace
            if (isCharging() && chargeDuration > 0 && chargeTick > chargeDuration + PREVIEW_CHARGE_GRACE) {
                this.setDead();
                return;
            }

            // Preview hard lifetime cap (mirrors real entity HARD_LIFETIME_CAP)
            if (ticksExisted > HARD_LIFETIME_CAP) {
                this.setDead();
                return;
            }
        }

        if (!previewMode) {
            syncDisplayColorsFromWatchers();
        }

        // Update rotation
        updateRotation();

        // Update render size: during charging, track directly (size already grows smoothly).
        // Post-charging, lerp for smooth visual transitions.
        if (isCharging()) {
            this.renderCurrentSize = this.size;
        } else {
            this.renderCurrentSize = this.renderCurrentSize + (this.size - this.renderCurrentSize) * 0.15f;
        }

        // Server-synced visual timer used for brief barrier impact lightning.
        if (!previewMode && !worldObj.isRemote) {
            tickBarrierSparkTimer();
        }

        // Skip lifetime/distance checks in preview mode
        if (!previewMode) {
            // Only self-destruct if the owner is confirmed dead/killed.
            // A null owner (unloaded/out of chunk) is NOT treated as dead —
            // the projectile keeps acting normally and deathWorldTime handles lifetime.
            if (ownerEntityId >= 0 && ticksExisted > 5) {
                Entity owner = worldObj.getEntityByID(ownerEntityId);
                if (owner != null) {
                    if (owner.isDead) {
                        this.setDead();
                        return;
                    }
                    if (owner instanceof EntityNPCInterface && ((EntityNPCInterface) owner).isKilled()) {
                        this.setDead();
                        return;
                    }
                }
            }

            // Charging timeout - if charge phase exceeded with grace period, force kill
            if (isCharging() && chargeTick > chargeDuration + CHARGE_TIMEOUT_GRACE) {
                this.setDead();
                return;
            }

            // Absolute hard lifetime cap (safety net for any stuck state)
            if (ticksExisted > HARD_LIFETIME_CAP) {
                this.setDead();
                return;
            }

            // Set death time on first tick if not already set (handles chunk load/unload)
            // Skip during charging phase - lifetime starts when the entity actually fires
            if (deathWorldTime < 0 && worldObj != null && !isCharging()) {
                deathWorldTime = worldObj.getTotalWorldTime() + getMaxLifetime();
            }

            // Check lifespan using world time (survives chunk unload/reload)
            if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
                if (!worldObj.isRemote) {
                    EventHooks.onEnergyProjectileExpired(this);
                }
                this.setDead();
                return;
            }

            // Check max distance (subclass can override if needed)
            if (checkMaxDistance()) {
                if (!worldObj.isRemote) {
                    EventHooks.onEnergyProjectileExpired(this);
                }
                this.setDead();
                return;
            }

            if (hasHit) {
                this.setDead();
                return;
            }
        }

        if (!previewMode && !worldObj.isRemote && !isCharging()) {
            if (tickBarrierImpactPause()) {
                return;
            }
        }

        // Check barrier collisions (server-side, non-preview, non-charging)
        if (!previewMode && !worldObj.isRemote && !isCharging()) {
            if (checkBarrierCollision()) {
                return; // Barrier interaction handled (pause + destroy/continue)
            }
        }

        // Subclass-specific update
        updateProjectile();

        // Fire tick event (server-side, non-preview only)
        if (!previewMode && !worldObj.isRemote) {
            EventHooks.onEnergyProjectileTick(this);
        }

        debugLogTick();
    }

    /**
     * Update rotation values. Override for custom rotation behavior.
     */
    protected void updateRotation() {
        this.rotationValX += getRotationSpeed() * 0.7f;
        this.rotationValY += getRotationSpeed();
        this.rotationValZ += getRotationSpeed() * 0.5f;

        if (this.rotationValX > 360.0f) this.rotationValX -= 360.0f;
        if (this.rotationValY > 360.0f) this.rotationValY -= 360.0f;
        if (this.rotationValZ > 360.0f) this.rotationValZ -= 360.0f;
    }

    // ==================== DEBUG LOGGING ====================

    /**
     * Debug log called every tick. Logs position/size/charging state.
     * Subclasses override debugLogExtra() for type-specific data.
     */
    protected void debugLogTick() {
        boolean isClient = worldObj.isRemote;
        if (isClient ? !CNPCDebug.isClientEnabled("energy") : !CNPCDebug.isServerEnabled("energy"))
            return;

        String className = getClass().getSimpleName();
        boolean dwCharging = isCharging();   // DataWatcher value (what client sees)
        boolean localCharging = this.charging; // Local field (server truth)

        String base = String.format("[%s id=%d tick=%d] pos=(%.2f,%.2f,%.2f) prev=(%.2f,%.2f,%.2f) " +
                "size=%.3f renderSize=%.3f prevRenderSize=%.3f " +
                "charging(dw)=%b charging(local)=%b chargeTick=%d/%d chargeProgress=%.3f",
            className, getEntityId(), ticksExisted,
            posX, posY, posZ, prevPosX, prevPosY, prevPosZ,
            size, renderCurrentSize, prevRenderSize,
            dwCharging, localCharging, chargeTick, chargeDuration, getChargeProgress());

        // Flag mismatch between DataWatcher and local field
        if (dwCharging != localCharging) {
            base += " !!CHARGE_MISMATCH!!";
        }

        // Client interpolation state (only meaningful on client)
        if (isClient) {
            base += String.format(" interp(steps=%d target=(%.2f,%.2f,%.2f))",
                interpSteps, interpTargetX, interpTargetY, interpTargetZ);
        }

        String extra = debugLogExtra();
        String full = extra.isEmpty() ? base : base + " " + extra;
        CNPCDebug.log("energy", isClient, full);
    }

    /**
     * Subclass hook for type-specific debug data.
     * Return empty string if nothing extra to log.
     */
    protected String debugLogExtra() {
        return "";
    }

    /**
     * Check if projectile has exceeded max distance.
     *
     * @return true if should die
     */
    protected boolean checkMaxDistance() {
        double distTraveled = Math.sqrt(
            (posX - startX) * (posX - startX) +
                (posY - startY) * (posY - startY) +
                (posZ - startZ) * (posZ - startZ)
        );
        return distTraveled >= getMaxDistance();
    }

    /**
     * Subclass-specific update logic. Called every tick.
     */
    protected abstract void updateProjectile();

    // ==================== BARRIER COLLISION ====================

    /**
     * Check for collision with energy barrier entities.
     * Barriers only block incoming projectiles from enemies.
     *
     * @return true if barrier interaction was handled (caller should stop processing this tick)
     */
    protected boolean checkBarrierCollision() {
        List<EntityEnergyBarrier> barriers = EntityEnergyBarrier.getActiveBarriers(worldObj);
        for (EntityEnergyBarrier barrier : barriers) {
            if (barrier.isDead) continue;

            // Quick distance pre-filter
            double dx = barrier.posX - this.posX;
            double dy = barrier.posY - this.posY;
            double dz = barrier.posZ - this.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            double maxRange = barrier.getMaxExtent() + 5.0;
            if (distSq > maxRange * maxRange) continue;

            if (barrier.isIncomingProjectile(this)) {
                float damage = getModifiedDamage();
                EntityEnergyBarrier.ProjectileHitOutcome hitOutcome = barrier.onProjectileHitResolved(this, damage);
                if (hitOutcome == null || hitOutcome.result == EntityEnergyBarrier.ProjectileHitResult.PASS) {
                    continue;
                }
                if (handleBarrierHitOutcome(barrier, hitOutcome, damage)) {
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Handle the result of a projectile/barrier interaction.
     *
     * @param fullDamage The DBC-scaled damage that was passed to the barrier (from getModifiedDamage).
     * @return true if caller should stop processing this tick.
     */
    protected boolean handleBarrierHitOutcome(EntityEnergyBarrier barrier, EntityEnergyBarrier.ProjectileHitOutcome hitOutcome, float fullDamage) {
        if (hitOutcome == null || hitOutcome.result == EntityEnergyBarrier.ProjectileHitResult.PASS) {
            return false;
        }

        if (hitOutcome.result == EntityEnergyBarrier.ProjectileHitResult.BROKEN) {
            if (hitOutcome.remainingProjectileDamage <= 0.0f) {
                damageMultiplier = 0.0f;
                setBarrierSparkTicks(Math.max(getBarrierSparkTicks(), BARRIER_BREAK_SPARK_TICKS));
                hasHit = true;
                return true;
            }

            // Accumulate barrier pass-through ratio for extender damage scaling.
            // fullDamage is the DBC-scaled damage the barrier saw; remainingProjectileDamage
            // is what survived. Multiply into the running ratio so multiple barriers stack.
            if (fullDamage > 0.0f) {
                damageMultiplier *= (hitOutcome.remainingProjectileDamage / fullDamage);
            }

            setCombatDamage(hitOutcome.remainingProjectileDamage);
            setBarrierSparkTicks(Math.max(getBarrierSparkTicks(), BARRIER_BREAK_SPARK_TICKS));
            return false;
        }

        if (hitOutcome.shouldReflect()) {
            if (!reflectFromBarrier(barrier, hitOutcome.reflectStrengthPct)) {
                beginBarrierImpactPause(true, barrier);
            } else {
                sendReflectionSync();
            }
            return true;
        }

        beginBarrierImpactPause(true, barrier);
        return true;
    }

    protected boolean reflectFromBarrier(EntityEnergyBarrier barrier, float reflectStrengthPct) {
        if (barrier == null) {
            return false;
        }

        double vx = motionX;
        double vy = motionY;
        double vz = motionZ;
        double vLenSq = vx * vx + vy * vy + vz * vz;
        if (vLenSq < 1.0e-8) {
            return false;
        }

        double[] normal = getBarrierImpactNormal(barrier, vx, vy, vz);
        if (normal == null) {
            return false;
        }

        double dot = vx * normal[0] + vy * normal[1] + vz * normal[2];
        if (dot > 0.0) {
            normal[0] = -normal[0];
            normal[1] = -normal[1];
            normal[2] = -normal[2];
            dot = vx * normal[0] + vy * normal[1] + vz * normal[2];
        }

        double rx = vx - 2.0 * dot * normal[0];
        double ry = vy - 2.0 * dot * normal[1];
        double rz = vz - 2.0 * dot * normal[2];
        double rLenSq = rx * rx + ry * ry + rz * rz;
        if (rLenSq < 1.0e-8) {
            rx = -vx;
            ry = -vy;
            rz = -vz;
            rLenSq = rx * rx + ry * ry + rz * rz;
        }

        // Prefer rebounding out and away from ground instead of immediately diving downward.
        if (ry < -0.06) {
            double speed = Math.sqrt(Math.max(rLenSq, 1.0e-8));
            ry = Math.max(0.08, Math.abs(ry) * 0.35);
            double newLen = Math.sqrt(rx * rx + ry * ry + rz * rz);
            if (newLen > 1.0e-8) {
                double scale = speed / newLen;
                rx *= scale;
                ry *= scale;
                rz *= scale;
            }
        }

        // Push slightly outside the collision boundary so the reflected motion
        // starts cleanly from the barrier surface instead of re-colliding in place.
        snapOutsideBarrierForPause(barrier);

        motionX = rx;
        motionY = ry;
        motionZ = rz;
        pausedMotionX = rx;
        pausedMotionY = ry;
        pausedMotionZ = rz;
        barrierImpactPauseTicks = 0;
        barrierImpactDestroyOnResume = false;

        // Reset start position and lifetime so the reflected projectile gets a fresh
        // max-distance budget and lifetime. Without this, re-reflection fails because
        // the projectile expires from the original launch's distance/time limits.
        startX = posX;
        startY = posY;
        startZ = posZ;
        deathWorldTime = -1;

        // Detach from source ability BEFORE changing ownership.
        // This lets the ability see the projectile as gone and free the caster
        // (e.g., release movement/rotation locks on the player).
        if (sourceAbility instanceof AbilityEnergyProjectile) {
            ((AbilityEnergyProjectile<?>) sourceAbility).detachEntity(this);
        }
        sourceAbility = null;

        // Save original owner before transfer for potential target retargeting
        int originalOwnerId = ownerEntityId;

        Entity barrierOwner = barrier.getOwnerEntity();
        if (barrierOwner != null) {
            setOwnerEntityId(barrierOwner.getEntityId());
            // Ensure ownership-sensitive systems can query this projectile under the new owner.
            trackProjectile(this);
        }

        // Target Owner: set the reflected projectile's target to the original caster
        if (barrier.getBarrierData().isTargetOwner() && originalOwnerId != -1) {
            setTargetEntityId(originalOwnerId);
        } else {
            setTargetEntityId(-1);
        }

        setInnerColor(barrier.getInnerColor());
        setOuterColor(barrier.getOuterColor());

        float clampedStrength = Math.max(0.0f, Math.min(100.0f, reflectStrengthPct));
        float reductionFactor = 1.0f - clampedStrength / 100.0f;
        float reducedDamage = getDamage() * reductionFactor;
        setCombatDamage(Math.max(0.0f, reducedDamage));
        damageMultiplier *= reductionFactor;

        hitOnceEntities.clear();
        lastHitTickByEntity.clear();
        hitCount = 0;
        reflected = true;
        return true;
    }

    // ==================== REFLECTION SYNC ====================

    /**
     * Write reflection state for client sync.
     * Subclasses should override writeProjectileReflectionData for type-specific fields.
     */
    public NBTTagCompound writeReflectionData() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setDouble("PosX", posX);
        nbt.setDouble("PosY", posY);
        nbt.setDouble("PosZ", posZ);
        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);
        nbt.setInteger("InnerColor", getInnerColor());
        nbt.setInteger("OuterColor", getOuterColor());
        nbt.setBoolean("Reflected", reflected);
        writeProjectileReflectionData(nbt);
        return nbt;
    }

    /**
     * Apply reflection state from server sync.
     * Subclasses should override applyProjectileReflectionData for type-specific fields.
     */
    public void applyReflectionData(NBTTagCompound nbt) {
        ownerEntityId = nbt.getInteger("OwnerId");
        double px = nbt.getDouble("PosX");
        double py = nbt.getDouble("PosY");
        double pz = nbt.getDouble("PosZ");
        setPosition(px, py, pz);
        motionX = nbt.getDouble("MotionX");
        motionY = nbt.getDouble("MotionY");
        motionZ = nbt.getDouble("MotionZ");
        setInnerColor(nbt.getInteger("InnerColor"));
        setOuterColor(nbt.getInteger("OuterColor"));
        reflected = nbt.getBoolean("Reflected");
        syncPositionStateToCurrent(true);
        hitOnceEntities.clear();
        lastHitTickByEntity.clear();
        hitCount = 0;
        applyProjectileReflectionData(nbt);
    }

    /**
     * Subclass hook: write type-specific reflection data.
     */
    protected void writeProjectileReflectionData(NBTTagCompound nbt) {
    }

    /**
     * Subclass hook: apply type-specific reflection data on client.
     */
    protected void applyProjectileReflectionData(NBTTagCompound nbt) {
    }

    /**
     * Send reflection state to all tracking clients.
     */
    protected void sendReflectionSync() {
        if (worldObj == null || worldObj.isRemote) return;
        ProjectileReflectPacket.sendToTracking(this, writeReflectionData());
    }

    // ==================== CLIENT SYNC ====================

    /**
     * Write all client-relevant state: position, motion, visual, and movement properties.
     * Subclasses should override writeProjectileClientSyncData for type-specific fields.
     */
    public NBTTagCompound writeClientSyncData() {
        NBTTagCompound nbt = new NBTTagCompound();
        // Position & motion
        nbt.setDouble("PosX", posX);
        nbt.setDouble("PosY", posY);
        nbt.setDouble("PosZ", posZ);
        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);
        // Size & visual
        nbt.setFloat("Size", size);
        nbt.setFloat("RotationSpeed", displayData.getRotationSpeed());
        nbt.setFloat("InnerAlpha", displayData.getInnerAlpha());
        nbt.setBoolean("OuterColorEnabled", displayData.isOuterColorEnabled());
        nbt.setFloat("OuterColorWidth", displayData.getOuterColorWidth());
        nbt.setFloat("OuterColorAlpha", displayData.getOuterColorAlpha());
        // Lightning
        nbt.setBoolean("LightningEffect", hasLightningEffect());
        nbt.setFloat("LightningDensity", getLightningDensity());
        nbt.setFloat("LightningRadius", getLightningRadius());
        nbt.setInteger("LightningFadeTime", getLightningFadeTime());
        // Movement
        nbt.setFloat("Speed", getSpeed());
        nbt.setBoolean("Homing", isHoming());
        nbt.setFloat("HomingStrength", getHomingStrength());
        nbt.setFloat("HomingRange", getHomingRange());
        writeProjectileClientSyncData(nbt);
        return nbt;
    }

    /**
     * Apply client sync data from server.
     * Subclasses should override applyProjectileClientSyncData for type-specific fields.
     */
    public void applyClientSyncData(NBTTagCompound nbt) {
        // Position & motion
        setPosition(nbt.getDouble("PosX"), nbt.getDouble("PosY"), nbt.getDouble("PosZ"));
        motionX = nbt.getDouble("MotionX");
        motionY = nbt.getDouble("MotionY");
        motionZ = nbt.getDouble("MotionZ");
        syncPositionStateToCurrent(true);
        // Size & visual
        setProjectileSize(nbt.getFloat("Size"));
        displayData.setRotationSpeed(nbt.getFloat("RotationSpeed"));
        displayData.setInnerAlpha(nbt.getFloat("InnerAlpha"));
        displayData.setOuterColorEnabled(nbt.getBoolean("OuterColorEnabled"));
        displayData.setOuterColorWidth(nbt.getFloat("OuterColorWidth"));
        displayData.setOuterColorAlpha(nbt.getFloat("OuterColorAlpha"));
        // Lightning
        setLightningEffect(nbt.getBoolean("LightningEffect"));
        setLightningDensity(nbt.getFloat("LightningDensity"));
        setLightningRadius(nbt.getFloat("LightningRadius"));
        setLightningFadeTime(nbt.getInteger("LightningFadeTime"));
        // Movement
        setSpeed(nbt.getFloat("Speed"));
        setHomingEnabled(nbt.getBoolean("Homing"));
        setHomingStrength(nbt.getFloat("HomingStrength"));
        setHomingRange(nbt.getFloat("HomingRange"));
        applyProjectileClientSyncData(nbt);
    }

    /**
     * Subclass hook: write type-specific client sync data.
     */
    protected void writeProjectileClientSyncData(NBTTagCompound nbt) {
    }

    /**
     * Subclass hook: apply type-specific client sync data on client.
     */
    protected void applyProjectileClientSyncData(NBTTagCompound nbt) {
    }

    /**
     * Send full client sync to all tracking clients.
     */
    public void sendClientSync() {
        if (worldObj == null || worldObj.isRemote) return;
        ProjectileClientSyncPacket.sendToTracking(this, writeClientSyncData());
    }

    protected double[] getBarrierImpactNormal(EntityEnergyBarrier barrier, double velocityX, double velocityY, double velocityZ) {
        return barrier.getSurfaceNormal(posX, posY, posZ, velocityX, velocityY, velocityZ);
    }

    protected void beginBarrierImpactPause(boolean destroyOnResume, EntityEnergyBarrier barrier) {
        if (barrierImpactPauseTicks <= 0) {
            pausedMotionX = motionX;
            pausedMotionY = motionY;
            pausedMotionZ = motionZ;
        }

        barrierImpactDestroyOnResume = destroyOnResume;
        barrierImpactPauseTicks = BARRIER_IMPACT_PAUSE_TICKS;
        motionX = 0;
        motionY = 0;
        motionZ = 0;

        int sparkTicks = getBarrierSparkTicks();
        if (sparkTicks < BARRIER_IMPACT_PAUSE_TICKS) {
            setBarrierSparkTicks(BARRIER_IMPACT_PAUSE_TICKS);
        }
    }

    protected float getBarrierPauseOutsideDistance() {
        // Keep the paused projectile just outside the surface without a visible "fling".
        return Math.max(0.06f, Math.min(0.22f, size * 0.18f + 0.03f));
    }

    protected void snapOutsideBarrierForPause(EntityEnergyBarrier barrier) {
        if (barrier == null) return;

        float bias = getBarrierPauseOutsideDistance();
        double[] point = barrier.getOutsideSurfacePoint(posX, posY, posZ, motionX, motionY, motionZ, bias);
        setPosition(point[0], point[1], point[2]);
    }

    protected boolean tickBarrierImpactPause() {
        if (barrierImpactPauseTicks <= 0) {
            return false;
        }

        motionX = 0;
        motionY = 0;
        motionZ = 0;
        barrierImpactPauseTicks--;

        if (barrierImpactPauseTicks <= 0) {
            if (barrierImpactDestroyOnResume) {
                hasHit = true;
                this.setDead();
            } else {
                motionX = pausedMotionX;
                motionY = pausedMotionY;
                motionZ = pausedMotionZ;
            }
            barrierImpactDestroyOnResume = false;
        }

        return true;
    }

    protected void tickBarrierSparkTimer() {
        int sparkTicks = getBarrierSparkTicks();
        if (sparkTicks > 0) {
            setBarrierSparkTicks(sparkTicks - 1);
        }
    }

    protected void setBarrierSparkTicks(int ticks) {
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_BARRIER_SPARK_TICKS, Math.max(0, ticks));
        }
    }

    public int getBarrierSparkTicks() {
        if (this.dataWatcher == null) {
            return 0;
        }
        return Math.max(0, this.dataWatcher.getWatchableObjectInt(DW_BARRIER_SPARK_TICKS));
    }

    protected void syncProjectileColorWatchers() {
        if (this.dataWatcher == null) {
            return;
        }
        this.dataWatcher.updateObject(DW_SYNC_INNER_COLOR, this.displayData.getInnerColor());
        this.dataWatcher.updateObject(DW_SYNC_OUTER_COLOR, this.displayData.getOuterColor());
    }

    protected void syncDisplayColorsFromWatchers() {
        if (this.dataWatcher == null) {
            return;
        }
        this.displayData.setInnerColor(this.dataWatcher.getWatchableObjectInt(DW_SYNC_INNER_COLOR));
        this.displayData.setOuterColor(this.dataWatcher.getWatchableObjectInt(DW_SYNC_OUTER_COLOR));
    }

    @Override
    public void setInnerColor(int color) {
        super.setInnerColor(color);
        if (!previewMode) {
            syncProjectileColorWatchers();
        }
    }

    @Override
    public void setOuterColor(int color) {
        super.setOuterColor(color);
        if (!previewMode) {
            syncProjectileColorWatchers();
        }
    }

    /**
     * Get the projectile damage modified by extenders (e.g. DBC damage scaling).
     * Falls back to base getDamage() when no sourceAbility or no extenders modify it.
     */
    protected float getModifiedDamage() {
        float damage = getDamage();
        if (sourceAbility != null) {
            Entity owner = getOwnerEntity();
            if (owner instanceof EntityLivingBase) {
                damage = AbilityController.Instance.fireModifyProjectileDamage(
                    sourceAbility, (EntityLivingBase) owner, damage);
            }
        }
        return damage;
    }

    // ==================== POSITION INTERPOLATION ====================

    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements) {
        int steps = Math.max(1, posRotationIncrements);
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;
        double dotToCurrentMotion = dx * this.motionX + dy * this.motionY + dz * this.motionZ;

        // Spawn-time and large correction snaps: avoid visible startup rubber-banding.
        if (worldObj != null && worldObj.isRemote) {
            double distSq = dx * dx + dy * dy + dz * dz;
            // Launch-time stale tracker packets can arrive one update late and point behind
            // an already-predicted projectile. Skipping these avoids visible backward "bounce".
            if (ticksExisted <= 24 && distSq <= 4.0D && dotToCurrentMotion < -0.0025D) {
                return;
            }
            if (ticksExisted <= 2 || distSq > 16.0D) {
                this.setPosition(x, y, z);
                syncPositionState(x, y, z, true);
                return;
            }
        }

        this.interpTargetX = x;
        this.interpTargetY = y;
        this.interpTargetZ = z;
        if (worldObj != null && worldObj.isRemote && ticksExisted <= 24 && dotToCurrentMotion < 0.0D) {
            // Keep existing velocity when correction points opposite current travel
            // to prevent launch-time reverse lerp.
            this.interpTargetMotionX = this.motionX;
            this.interpTargetMotionY = this.motionY;
            this.interpTargetMotionZ = this.motionZ;
        } else {
            this.interpTargetMotionX = dx / steps;
            this.interpTargetMotionY = dy / steps;
            this.interpTargetMotionZ = dz / steps;
        }
        this.interpSteps = steps;
    }

    @Override
    public void setVelocity(double motionX, double motionY, double motionZ) {
        this.interpTargetMotionX = motionX;
        this.interpTargetMotionY = motionY;
        this.interpTargetMotionZ = motionZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    /**
     * Handle client-side position interpolation.
     * Only moves position during active interpolation (interpSteps > 0).
     * Does NOT predict/drift between server updates — the motion values are
     * derived from correction deltas, not actual entity velocity, so applying
     * them between updates causes entities to fly off in wrong directions.
     * This matches vanilla Minecraft behavior for non-player entities.
     */
    protected void handleClientInterpolation() {
        if (this.interpSteps > 0) {
            double dx = this.interpTargetX - this.posX;
            double dy = this.interpTargetY - this.posY;
            double dz = this.interpTargetZ - this.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq < 0.0001) {
                this.setPosition(this.interpTargetX, this.interpTargetY, this.interpTargetZ);
                this.interpSteps = 0;
                return;
            }

            double newX = this.posX + dx / this.interpSteps;
            double newY = this.posY + dy / this.interpSteps;
            double newZ = this.posZ + dz / this.interpSteps;

            this.motionX = this.motionX + (this.interpTargetMotionX - this.motionX) / this.interpSteps;
            this.motionY = this.motionY + (this.interpTargetMotionY - this.motionY) / this.interpSteps;
            this.motionZ = this.motionZ + (this.interpTargetMotionZ - this.motionZ) / this.interpSteps;

            this.setPosition(newX, newY, newZ);
            this.interpSteps--;
        }
    }

    // ==================== DAMAGE & EFFECTS ====================

    protected boolean applyDamage(EntityLivingBase target) {
        if (previewMode) return false; // Skip damage in preview mode
        return applyDamage(target, this.getDamage(), this.getKnockback());
    }

    protected boolean applyDamage(EntityLivingBase target, float dmg, float kb) {
        if (previewMode) return false; // Skip damage in preview mode
        if (target == null || shouldIgnoreEntity(target)) return false;

        // Fire entity impact event (may cancel or modify damage)
        if (!worldObj.isRemote) {
            float result = EventHooks.onEnergyProjectileEntityImpact(this, target, dmg);
            if (result < 0) return false; // Event was cancelled
            dmg = result;
        }

        Entity owner = getOwnerEntity();
        boolean ignoreIFrames = isIgnoreIFrames();

        // Check for ability extenders (e.g., DBC Addon damage routing)
        boolean handled = false;
        boolean defaultDamageApplied = false;
        if (sourceAbility != null && owner instanceof EntityLivingBase) {
            double dx = target.posX - posX;
            double dz = target.posZ - posZ;
            float kbUp = getKnockbackUp() > 0 ? getKnockbackUp() : 0.1f;
            handled = AbilityController.Instance.fireOnAbilityDamage(
                sourceAbility, (EntityLivingBase) owner, target,
                dmg, kb, kbUp, dx, dz, damageMultiplier);
        }
        // Fallback: route through EnergyController for script-created entities with custom damage data
        if (!handled && customDamageData != null && owner instanceof EntityLivingBase) {
            double dx = target.posX - posX;
            double dz = target.posZ - posZ;
            float kbUp = getKnockbackUp() > 0 ? getKnockbackUp() : 0.1f;
            handled = EnergyController.Instance.fireOnEnergyDamage(
                this, (EntityLivingBase) owner, target,
                dmg, kb, kbUp, dx, dz, damageMultiplier, customDamageData);
        }

        int previousHurtResistantTime = Ability.clearHurtResistanceIfNeeded(target, ignoreIFrames);
        try {
            if (!handled) {
                float finalDmg = dmg;

                // Apply magic pipeline for player and NPC casters
                if (this.magicData != null && !this.magicData.isEmpty() && owner instanceof EntityLivingBase) {
                    finalDmg = AttributeAttackUtil.calculateAbilityDamage(
                        (EntityLivingBase) owner, target, dmg, this.magicData);
                }

                // Default damage path
                if (owner instanceof EntityNPCInterface) {
                    defaultDamageApplied = target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), finalDmg);
                } else if (owner instanceof EntityPlayer) {
                    defaultDamageApplied = target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), finalDmg);
                } else if (owner instanceof EntityLivingBase) {
                    defaultDamageApplied = target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), finalDmg);
                } else {
                    defaultDamageApplied = target.attackEntityFrom(new NpcDamageSource("npc_ability", null), finalDmg);
                }
            }
        } finally {
            Ability.restoreHurtResistanceIfNeeded(target, ignoreIFrames, previousHurtResistantTime);
        }

        boolean allowSecondaryEffects = handled || dmg <= 0 || defaultDamageApplied;
        if (!allowSecondaryEffects) return false;

        if (kb > 0) {
            double dx = target.posX - posX;
            double dz = target.posZ - posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) {
                double kbX = (dx / len) * kb * 0.5;
                double kbZ = (dz / len) * kb * 0.5;
                double kbY = getKnockbackUp() > 0 ? getKnockbackUp() : 0.1;
                target.addVelocity(kbX, kbY, kbZ);
                target.velocityChanged = true;
            }
        }

        applyEffects(target);
        return true;
    }

    protected void applyEffects(EntityLivingBase target) {
        for (AbilityPotionEffect effect : effects) {
            effect.apply(target);
        }
    }

    /**
     * Set the effects list from the ability's configured effects.
     */
    public void setAnchorData(EnergyAnchorData anchor) {
        this.anchorData = anchor != null ? anchor.copy() : new EnergyAnchorData(AnchorPoint.FRONT);
    }

    public void setEffects(List<AbilityPotionEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            this.effects = new ArrayList<>();
        } else {
            // Deep copy: entity must not share the ability's effects list or its entries.
            this.effects = new ArrayList<>(effects.size());
            for (AbilityPotionEffect effect : effects) {
                this.effects.add(effect.copy());
            }
        }
    }

    protected boolean doExplosion() {
        if (previewMode) return false; // Skip explosion in preview mode
        if (worldObj.isRemote) return false; // Explosions are server-side only
        float explosionRad = getExplosionRadius();
        if (Float.isNaN(explosionRad) || explosionRad <= 0) return false;
        final double explosionRadSq = explosionRad * explosionRad;
        final float baseDamage = getDamage();
        final float baseKnockback = getKnockback();
        final float damageFalloff = getExplosionDamageFalloff();
        spawnExplosionRenderEntity(explosionRad);
        spawnExplosionVisuals(explosionRad);
        worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 1.0f, 1.0f);

        Entity owner = getOwnerEntity();

        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            posX - explosionRad, posY - explosionRad, posZ - explosionRad,
            posX + explosionRad, posY + explosionRad, posZ + explosionRad
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, explosionBox);
        boolean anyDamaged = false;

        for (EntityLivingBase target : targets) {
            if (target == owner) continue;
            if (shouldIgnoreExplosionTarget(target)) continue;

            // Measure distance to nearest point on entity's bounding box, not feet position.
            // Using feet position (target.posY) causes explosions at impact height to miss
            // entities standing below — e.g. a projectile hitting at eye height (~1.62 above feet)
            // would measure dist=1.62, failing a radius<=1.6 check despite a direct hit.
            if (target.boundingBox == null) continue;
            double closestX = Math.max(target.boundingBox.minX, Math.min(posX, target.boundingBox.maxX));
            double closestY = Math.max(target.boundingBox.minY, Math.min(posY, target.boundingBox.maxY));
            double closestZ = Math.max(target.boundingBox.minZ, Math.min(posZ, target.boundingBox.maxZ));
            double dx = closestX - posX;
            double dy = closestY - posY;
            double dz = closestZ - posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > explosionRadSq) continue;

            float falloff = 1.0f;
            if (damageFalloff != 0.0f && distSq > 0.0D) {
                float dist = (float) Math.sqrt(distSq);
                falloff = 1.0f - (dist / explosionRad) * damageFalloff;
            }
            if (applyDamage(target, baseDamage * falloff, baseKnockback * falloff)) {
                anyDamaged = true;
            }
        }

        tryDestroyTerrain(explosionRad);
        return anyDamaged;
    }

    /**
     * Explosion safety filter.
     * Reuses standard projectile-friendly checks (owner, faction, party).
     */
    protected boolean shouldIgnoreExplosionTarget(EntityLivingBase target) {
        if (target == null) return true;
        return shouldIgnoreEntity(target);
    }

    /**
     * Clamp a point to the nearest point inside the provided AABB.
     */
    protected Vec3 closestPointOnBoundingBox(AxisAlignedBB box, double x, double y, double z) {
        if (box == null) return null;
        double closestX = Math.max(box.minX, Math.min(x, box.maxX));
        double closestY = Math.max(box.minY, Math.min(y, box.maxY));
        double closestZ = Math.max(box.minZ, Math.min(z, box.maxZ));
        return Vec3.createVectorHelper(closestX, closestY, closestZ);
    }

    /**
     * Spawn a packet-driven client preview so explosions are visible as geometry,
     * not only as particles.
     */
    protected void spawnExplosionRenderEntity(float explosionRad) {
        if (worldObj == null || worldObj.isRemote) return;
        float renderRad = Math.max(0.75f, Math.min(explosionRad, EnergyCombatData.MAX_EXPLOSION_RADIUS));
        EntityEnergyExplosion fx = new EntityEnergyExplosion(worldObj, this, renderRad);
        EnergyExplosionSpawnPacket.sendToTracking(getExplosionVisualInstanceId(), fx, this);
    }

    protected String getExplosionVisualInstanceId() {
        return "energy_explosion_" + UUID.randomUUID();
    }

    /**
     * Shared voxel-style explosion VFX for all energy projectile abilities.
     */
    protected void spawnExplosionVisuals(float explosionRad) {
        if (worldObj == null) return;

        float visualRad = Math.max(0.75f, Math.min(explosionRad, EnergyCombatData.MAX_EXPLOSION_RADIUS));
        int coreBursts = Math.max(1, Math.min(4, Math.round(visualRad * 0.35f)));
        int shellCount = Math.max(1, Math.min(3, Math.round(visualRad / 3.5f) + 1));

        // Core flash.
        emitRandomBurstParticle("largeexplode", coreBursts, visualRad * 0.10, visualRad * 0.08, visualRad * 0.10, 0.01);

        // Expanding voxel shells.
        for (int i = 1; i <= shellCount; i++) {
            double frac = i / (double) shellCount;
            double shellRadius = visualRad * (0.35D + 0.65D * frac);
            spawnVoxelShell(shellRadius, i, visualRad);
        }

        // Residual smoke volume.
        int smokeCount = Math.max(6, Math.min(40, Math.round(visualRad * 4.5f)));
        emitRandomBurstParticle("smoke", smokeCount, visualRad * 0.45, visualRad * 0.30, visualRad * 0.45, 0.01);

        // Pull block fragments for stronger voxel feel.
        spawnVoxelDebris(visualRad);
    }

    /**
     * Emit random burst particles centered around this explosion.
     */
    protected void emitRandomBurstParticle(String particle, int count,
                                           double spreadX, double spreadY, double spreadZ,
                                           double speed) {
        if (worldObj == null || count <= 0 || particle == null || particle.isEmpty()) return;

        if (worldObj.isRemote) {
            for (int i = 0; i < count; i++) {
                double px = posX + (rand.nextDouble() - 0.5D) * 2.0D * spreadX;
                double py = posY + (rand.nextDouble() - 0.5D) * 2.0D * spreadY;
                double pz = posZ + (rand.nextDouble() - 0.5D) * 2.0D * spreadZ;
                double mx = rand.nextGaussian() * speed;
                double my = rand.nextGaussian() * speed;
                double mz = rand.nextGaussian() * speed;
                worldObj.spawnParticle(particle, px, py, pz, mx, my, mz);
            }
            return;
        }

        if (worldObj instanceof WorldServer) {
            ((WorldServer) worldObj).func_147487_a(particle, posX, posY, posZ, count, spreadX, spreadY, spreadZ, speed);
        }
    }

    /**
     * Emits a single directed voxel particle.
     * Uses count=0 packet mode on server so motion values are preserved.
     */
    protected void emitVoxelParticle(String particle, double x, double y, double z,
                                     double motionX, double motionY, double motionZ) {
        if (worldObj == null || particle == null || particle.isEmpty()) return;

        if (worldObj.isRemote) {
            worldObj.spawnParticle(particle, x, y, z, motionX, motionY, motionZ);
            return;
        }

        if (worldObj instanceof WorldServer) {
            ((WorldServer) worldObj).func_147487_a(particle, x, y, z, 0, motionX, motionY, motionZ, 1.0D);
        }
    }

    /**
     * Spawn one cubic shell of explosion particles to get a blocky/voxel silhouette.
     */
    protected void spawnVoxelShell(double shellRadius, int shellIndex, float visualRad) {
        int steps = Math.max(2, Math.min(4, (int) Math.ceil(shellRadius * 0.55D)));
        double cell = shellRadius / steps;
        double jitter = Math.min(0.15D, cell * 0.20D);
        double shellSpeed = 0.03D + (shellRadius / Math.max(1.0D, visualRad)) * 0.05D;

        for (int ix = -steps; ix <= steps; ix++) {
            for (int iy = -steps; iy <= steps; iy++) {
                for (int iz = -steps; iz <= steps; iz++) {
                    int edge = Math.max(Math.abs(ix), Math.max(Math.abs(iy), Math.abs(iz)));
                    if (edge != steps) continue;
                    if (((ix + iy + iz + shellIndex) & 1) != 0) continue;

                    double px = posX + ix * cell + (rand.nextDouble() - 0.5D) * jitter;
                    double py = posY + iy * cell + (rand.nextDouble() - 0.5D) * jitter;
                    double pz = posZ + iz * cell + (rand.nextDouble() - 0.5D) * jitter;

                    double nx = ix;
                    double ny = iy;
                    double nz = iz;
                    double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (len < 0.001D) {
                        ny = 1.0D;
                        len = 1.0D;
                    }
                    nx /= len;
                    ny /= len;
                    nz /= len;

                    emitVoxelParticle("explode", px, py, pz, nx * shellSpeed, ny * shellSpeed, nz * shellSpeed);

                    if (((ix + iy + iz + shellIndex) % 3) == 0) {
                        emitVoxelParticle("smoke", px, py, pz, nx * shellSpeed * 0.35D, ny * shellSpeed * 0.35D, nz * shellSpeed * 0.35D);
                    }
                }
            }
        }
    }

    /**
     * Spawn debris particles using nearby block textures.
     */
    protected void spawnVoxelDebris(float visualRad) {
        if (worldObj == null) return;

        int count = Math.max(4, Math.min(24, Math.round(visualRad * 2.5f)));
        for (int i = 0; i < count; i++) {
            double ox = (rand.nextDouble() - 0.5D) * visualRad * 1.2D;
            double oy = (rand.nextDouble() - 0.35D) * visualRad * 0.9D;
            double oz = (rand.nextDouble() - 0.5D) * visualRad * 1.2D;

            int bx = MathHelper.floor_double(posX + ox);
            int by = MathHelper.floor_double(posY + oy);
            int bz = MathHelper.floor_double(posZ + oz);

            Block block = worldObj.getBlock(bx, by, bz);
            if (block == null || block == Blocks.air) continue;

            int blockId = Block.getIdFromBlock(block);
            if (blockId <= 0) continue;
            int meta = worldObj.getBlockMetadata(bx, by, bz);

            String particle = "blockcrack_" + blockId + "_" + Math.max(0, meta);
            emitVoxelParticle(
                particle,
                posX + ox * 0.55D,
                posY + oy * 0.55D,
                posZ + oz * 0.55D,
                ox * 0.03D, oy * 0.03D, oz * 0.03D
            );
        }
    }

    /**
     * Optional terrain damage for energy explosions.
     * Controlled server-side by ConfigEnergy.EnableEnergyExplosionBlockDamage.
     */
    protected void tryDestroyTerrain(float explosionRad) {
        if (worldObj == null || worldObj.isRemote) return;
        if (!ConfigEnergy.EnableEnergyExplosionBlockDamage) return;
        if (!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) return;

        float terrainRad = Math.max(0.5f, Math.min(explosionRad, EnergyCombatData.MAX_EXPLOSION_RADIUS));
        int minX = MathHelper.floor_double(posX - terrainRad);
        int maxX = MathHelper.floor_double(posX + terrainRad);
        int minY = MathHelper.floor_double(posY - terrainRad);
        int maxY = MathHelper.floor_double(posY + terrainRad);
        int minZ = MathHelper.floor_double(posZ - terrainRad);
        int maxZ = MathHelper.floor_double(posZ + terrainRad);
        int worldMaxY = Math.max(0, worldObj.getActualHeight() - 1);
        if (maxY < 0 || minY > worldMaxY) return;
        float resistanceCutoff = Math.max(4.0f, terrainRad * 6.0f);

        Explosion context = new Explosion(worldObj, this, posX, posY, posZ, terrainRad);
        context.isFlaming = false;
        context.isSmoking = true;
        int lastChunkX = Integer.MIN_VALUE;
        int lastChunkZ = Integer.MIN_VALUE;
        boolean lastChunkLoaded = false;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double cx = x + 0.5D - posX;
                    double cy = y + 0.5D - posY;
                    double cz = z + 0.5D - posZ;

                    // Chebyshev metric yields a voxel/cubic blast shape.
                    double chebyshev = Math.max(Math.abs(cx), Math.max(Math.abs(cy), Math.abs(cz)));
                    if (chebyshev > terrainRad) continue;

                    // Soften cube corners a bit for a less artificial crater edge.
                    double normalized = chebyshev / terrainRad;
                    if (normalized > 0.72D && rand.nextDouble() < (normalized - 0.72D) * 1.4D) continue;
                    if (y < 0 || y > worldMaxY) continue;

                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                    if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
                        lastChunkX = chunkX;
                        lastChunkZ = chunkZ;
                        lastChunkLoaded = worldObj.getChunkProvider().chunkExists(chunkX, chunkZ);
                    }
                    if (!lastChunkLoaded) continue;

                    Block block = worldObj.getBlock(x, y, z);
                    if (block == null || block == Blocks.air || block == Blocks.bedrock) continue;
                    Material material = block.getMaterial();
                    if (material == null || material == Material.air) continue;

                    float resistance = block.getExplosionResistance(this, worldObj, x, y, z, posX, posY, posZ);
                    if (resistance > resistanceCutoff) continue;

                    try {
                        block.onBlockExploded(worldObj, x, y, z, context);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    // ==================== LAUNCH HELPERS ====================

    /**
     * Snaps the projectile position to the owner's look vector on launch.
     * This ensures projectiles launch aligned with the owner's facing direction
     * rather than from the last anchor point position.
     * Should be called in startMoving/startFiring BEFORE setting startX/Y/Z.
     */
    protected void snapToLookVector() {
        setLookVectorLaunchPosition(true);
    }

    /**
     * Shared launch transition: exit charging, snap to look-vector launch origin,
     * sync start position, and optionally reset interpolation state.
     *
     * @param resetInterpolationState true to reset prev/lastTick/interp state to launch origin.
     * @return true when look-vector launch origin was resolved from owner/look data.
     */
    protected boolean beginLookVectorLaunch(boolean resetInterpolationState) {
        setCharging(false);
        Entity owner = getOwnerEntity();
        boolean positioned = false;
        if (owner instanceof EntityLivingBase && shouldSnapLaunchToLookVector((EntityLivingBase) owner)) {
            positioned = setLookVectorLaunchPosition((EntityLivingBase) owner, getOwnerLookVector(), true);
        }
        syncStartPositionToCurrent();
        if (resetInterpolationState) {
            syncPositionStateToCurrent(true);
        }
        return positioned;
    }

    /**
     * Shared launch transition with explicit owner/look vector data.
     */
    protected boolean beginLookVectorLaunch(EntityLivingBase owner, Vec3 look, boolean resetInterpolationState) {
        setCharging(false);
        boolean positioned = shouldSnapLaunchToLookVector(owner) && setLookVectorLaunchPosition(owner, look, true);
        syncStartPositionToCurrent();
        if (resetInterpolationState) {
            syncPositionStateToCurrent(true);
        }
        return positioned;
    }

    /**
     * Determines whether the projectile should snap to the look-vector launch origin on fire.
     * By default, player-cast projectiles snap (launch from eye position along crosshair),
     * and NPC projectiles keep their anchor charge origin.
     * When {@code anchorData.launchFromAnchor} is true, snapping is disabled so the
     * projectile launches from its configured anchor position instead.
     */
    protected boolean shouldSnapLaunchToLookVector(EntityLivingBase owner) {
        if (anchorData.launchFromAnchor) {
            return false;
        }
        return owner instanceof EntityPlayer;
    }

    /**
     * Default preview launch: snap to look-vector origin and fire along look.
     */
    protected void startPreviewFiringDefault() {
        beginLookVectorLaunch(true);
        setMotionAlongLookVectorOrFallback(getSpeed(), getSpeed(), 0, 0);
    }

    /**
     * Default active launch: snap to look-vector origin and fire toward target when available.
     */
    protected void startMovingTowardTargetDefault(EntityLivingBase target) {
        beginLookVectorLaunch(false);
        setMotionTowardTargetOrLookVector(target, posX, posY, posZ, getSpeed(), getSpeed(), 0, 0);
    }

    /**
     * Default active launch from fixed origin fields (used by beam-like projectiles).
     */
    protected void startMovingTowardTargetFromStartDefault(EntityLivingBase target) {
        beginLookVectorLaunch(false);
        setMotionTowardTargetOrLookVector(target, startX, startY, startZ, getSpeed(), getSpeed(), 0, 0);
    }

    /**
     * Default active launch strictly along look vector.
     */
    protected void startMovingAlongLookVectorDefault() {
        beginLookVectorLaunch(false);
        setMotionAlongLookVectorOrFallback(getSpeed(), getSpeed(), 0, 0);
    }

    /**
     * Resolve the owner's look vector.
     * Falls back to yaw/pitch math if look vector is unavailable.
     */
    protected Vec3 getOwnerLookVector() {
        Entity owner = getOwnerEntity();
        if (!(owner instanceof EntityLivingBase)) return null;

        Vec3 look = owner.getLookVec();
        if (look != null) return look;

        float yaw = (float) Math.toRadians(owner.rotationYaw);
        float pitch = (float) Math.toRadians(owner.rotationPitch);
        return Vec3.createVectorHelper(
            -Math.sin(yaw) * Math.cos(pitch),
            -Math.sin(pitch),
            Math.cos(yaw) * Math.cos(pitch)
        );
    }

    /**
     * Shared launch origin: owner eye position + look-vector offset.
     *
     * @param syncPositionState When true, sync prev/lastTick/interp targets to this position.
     */
    protected boolean setLookVectorLaunchPosition(boolean syncPositionState) {
        Entity owner = getOwnerEntity();
        if (!(owner instanceof EntityLivingBase)) return false;
        Vec3 look = getOwnerLookVector();
        if (look == null) return false;
        return setLookVectorLaunchPosition((EntityLivingBase) owner, look, syncPositionState);
    }

    /**
     * Shared launch origin using provided owner/look data.
     */
    protected boolean setLookVectorLaunchPosition(EntityLivingBase owner, Vec3 look, boolean syncPositionState) {
        if (owner == null || look == null) return false;

        double originX = owner.posX;
        double originY = owner.posY + owner.getEyeHeight();
        double originZ = owner.posZ;

        float frontDist = computeLaunchFrontDistance(owner, look, originX, originY, originZ);
        double newX = originX + look.xCoord * frontDist;
        double newY = originY + look.yCoord * frontDist;
        double newZ = originZ + look.zCoord * frontDist;

        setPosition(newX, newY, newZ);
        if (syncPositionState) {
            syncPositionState(newX, newY, newZ, true);
        }
        return true;
    }

    /**
     * Radius used to clear the caster hitbox on launch.
     * Subclasses may override if their effective launch footprint differs from {@code size}.
     */
    protected float getLaunchClearanceRadius() {
        return Math.max(0.1f, size * 0.5f);
    }

    private float computeLaunchFrontDistance(EntityLivingBase owner, Vec3 look, double originX, double originY, double originZ) {
        // Legacy baseline offset retained as lower bound.
        float baseline = size * 0.4f;
        float clearance = getLaunchClearanceRadius() + 0.05f;

        AxisAlignedBB bb = owner.boundingBox;
        if (bb == null) {
            return Math.max(baseline, owner.width * 0.5f + clearance);
        }

        double dx = look.xCoord;
        double dy = look.yCoord;
        double dz = look.zCoord;

        double tx = computeRayExitT(originX, dx, bb.minX, bb.maxX);
        double ty = computeRayExitT(originY, dy, bb.minY, bb.maxY);
        double tz = computeRayExitT(originZ, dz, bb.minZ, bb.maxZ);

        double tExit = Math.min(tx, Math.min(ty, tz));
        if (Double.isInfinite(tExit) || tExit < 0) {
            return Math.max(baseline, owner.width * 0.5f + clearance);
        }

        return (float) Math.max(baseline, tExit + clearance);
    }

    private double computeRayExitT(double origin, double dir, double min, double max) {
        final double EPS = 1.0e-6;
        if (dir > EPS) {
            return (max - origin) / dir;
        } else if (dir < -EPS) {
            return (min - origin) / dir;
        }
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Set projectile motion along owner look vector.
     *
     * @return true if owner/look were available.
     */
    protected boolean setMotionAlongLookVector(float speed) {
        Vec3 look = getOwnerLookVector();
        if (look == null) return false;

        motionX = look.xCoord * speed;
        motionY = look.yCoord * speed;
        motionZ = look.zCoord * speed;
        return true;
    }

    /**
     * Set motion along owner look vector or apply explicit fallback values.
     * When {@code launchFromAnchor} is true, ray-casts from the owner's eye to find the
     * crosshair target point and aims from the projectile's anchor position toward it.
     */
    protected void setMotionAlongLookVectorOrFallback(float speed, double fallbackX, double fallbackY, double fallbackZ) {
        if (anchorData.launchFromAnchor && setMotionTowardLookTarget(speed)) {
            return;
        }
        if (!setMotionAlongLookVector(speed)) {
            motionX = fallbackX;
            motionY = fallbackY;
            motionZ = fallbackZ;
        }
    }

    /**
     * Ray-cast from the owner's eye along the look vector to find the crosshair target point,
     * then set motion from the projectile's current position toward that point.
     * Used when {@code launchFromAnchor} is true so projectiles fired from offset anchor
     * positions converge to the crosshair rather than flying parallel to the look direction.
     */
    protected boolean setMotionTowardLookTarget(float speed) {
        Entity owner = getOwnerEntity();
        if (!(owner instanceof EntityLivingBase)) return false;
        Vec3 look = getOwnerLookVector();
        if (look == null) return false;

        EntityLivingBase livingOwner = (EntityLivingBase) owner;
        double eyeX = livingOwner.posX;
        double eyeY = livingOwner.posY + livingOwner.getEyeHeight();
        double eyeZ = livingOwner.posZ;

        double maxDist = 200.0;
        Vec3 start = Vec3.createVectorHelper(eyeX, eyeY, eyeZ);
        Vec3 end = Vec3.createVectorHelper(
            eyeX + look.xCoord * maxDist,
            eyeY + look.yCoord * maxDist,
            eyeZ + look.zCoord * maxDist
        );

        double targetX, targetY, targetZ;
        MovingObjectPosition hit = worldObj.rayTraceBlocks(start, end);
        if (hit != null && hit.hitVec != null) {
            targetX = hit.hitVec.xCoord;
            targetY = hit.hitVec.yCoord;
            targetZ = hit.hitVec.zCoord;
        } else {
            targetX = eyeX + look.xCoord * maxDist;
            targetY = eyeY + look.yCoord * maxDist;
            targetZ = eyeZ + look.zCoord * maxDist;
        }

        double dx = targetX - posX;
        double dy = targetY - posY;
        double dz = targetZ - posZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len <= 0.0001) return false;

        motionX = (dx / len) * speed;
        motionY = (dy / len) * speed;
        motionZ = (dz / len) * speed;
        return true;
    }

    /**
     * Set projectile motion toward a target from a given source point.
     *
     * @return true if target was valid and motion was updated.
     */
    protected boolean setMotionTowardTarget(EntityLivingBase target, double sourceX, double sourceY, double sourceZ, float speed) {
        if (target == null) return false;

        double dx = target.posX - sourceX;
        double dy = (target.posY + target.getEyeHeight()) - sourceY;
        double dz = target.posZ - sourceZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len <= 0.0001) return false;

        motionX = (dx / len) * speed;
        motionY = (dy / len) * speed;
        motionZ = (dz / len) * speed;
        return true;
    }

    /**
     * Set motion toward target from source point; when target is unavailable,
     * fall back to owner look vector, then explicit fallback values.
     */
    protected void setMotionTowardTargetOrLookVector(EntityLivingBase target, double sourceX, double sourceY, double sourceZ, float speed,
                                                     double fallbackX, double fallbackY, double fallbackZ) {
        if (!setMotionTowardTarget(target, sourceX, sourceY, sourceZ, speed)) {
            setMotionAlongLookVectorOrFallback(speed, fallbackX, fallbackY, fallbackZ);
        }
    }

    /**
     * Sync start position (distance origin) to the current entity position.
     */
    protected void syncStartPositionToCurrent() {
        startX = posX;
        startY = posY;
        startZ = posZ;
    }

    /**
     * Sync prev/lastTick and optional interpolation state to an explicit position.
     */
    protected void syncPositionState(double x, double y, double z, boolean resetInterpolation) {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        lastTickPosX = x;
        lastTickPosY = y;
        lastTickPosZ = z;

        if (resetInterpolation) {
            interpTargetX = x;
            interpTargetY = y;
            interpTargetZ = z;
            interpTargetMotionX = motionX;
            interpTargetMotionY = motionY;
            interpTargetMotionZ = motionZ;
            interpSteps = 0;
        }
    }

    /**
     * Sync prev/lastTick and optional interpolation state to current entity position.
     */
    public void syncPositionStateToCurrent(boolean resetInterpolation) {
        syncPositionState(posX, posY, posZ, resetInterpolation);
    }

    // ==================== CHARGING METHODS (projectile-specific) ====================

    /**
     * Shared charging-state bootstrap used by both world and preview setup paths.
     */
    protected void setupChargingState(EnergyAnchorData anchor, int chargeDuration) {
        setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.anchorData = anchor;
    }

    /**
     * Shared preview bootstrap used by projectile preview setup methods.
     */
    protected void setupPreviewState(EntityLivingBase owner, EnergyDisplayData display,
                                     EnergyLightningData lightning, EnergyAnchorData anchor,
                                     int chargeDuration) {
        this.setPreviewMode(true);
        this.setPreviewOwner(owner);
        this.displayData = display != null ? display.copy() : new EnergyDisplayData();
        this.lightningData = lightning != null ? lightning.copy() : new EnergyLightningData();
        setupChargingState(anchor, chargeDuration);
    }

    /**
     * Set size and render-size state to a single value.
     */
    protected void setVisualSize(float value) {
        this.size = value;
        this.renderCurrentSize = value;
        this.prevRenderSize = value;
    }

    /**
     * Set entity/start state to a fixed position used as charging/launch origin.
     */
    protected void setChargeOrigin(Vec3 pos) {
        if (pos == null) return;
        this.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        syncPositionState(pos.xCoord, pos.yCoord, pos.zCoord, true);
        this.startX = pos.xCoord;
        this.startY = pos.yCoord;
        this.startZ = pos.zCoord;
    }

    /**
     * Resolve and apply charging/launch origin from anchor position.
     */
    protected void setChargeOriginFromAnchor(EntityLivingBase owner, EnergyAnchorData anchor) {
        if (owner == null || anchor == null) return;
        setChargeOrigin(AnchorPointHelper.calculateAnchorPosition(owner, anchor));
    }

    /**
     * Resolve and apply charging/launch origin from anchor position with forward offset.
     */
    protected void setChargeOriginFromAnchor(EntityLivingBase owner, EnergyAnchorData anchor, float offsetDistance) {
        if (owner == null || anchor == null) return;
        setChargeOrigin(AnchorPointHelper.calculateAnchorPosition(owner, anchor, offsetDistance));
    }

    /**
     * Zero current motion.
     */
    protected void clearMotion() {
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
    }

    /**
     * Setup this entity in charging mode (for windup phase).
     * Grows from 0 to current size over chargeDuration ticks.
     * Subclasses with different charging behavior should override or use their own method.
     */
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration) {
        setupChargingState(anchor, chargeDuration);
        this.targetSize = this.size;
        setVisualSize(0.01f);
        clearMotion();
    }

    /**
     * Update during charging state - grow size and follow anchor.
     * Default implementation grows {@code size} from 0 to {@code targetSize}.
     * Override in subclasses for type-specific charging (Disc, Beam).
     * <p>
     * Note: Owner death checks are handled by onUpdate() before this is called.
     */
    protected void updateCharging() {
        chargeTick++;
        float progress = getChargeProgress();
        this.size = targetSize * progress;
        Entity owner = getOwnerEntity();
        if (owner instanceof EntityLivingBase) {
            Vec3 pos = AnchorPointHelper.calculateAnchorPosition((EntityLivingBase) owner, anchorData);
            setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        }
    }

    // ==================== HOMING ====================

    /**
     * Update homing toward target. Uses position-based homing with distance-scaled strength.
     * Override in subclasses for type-specific homing (e.g. Beam uses head offset).
     */
    protected void updateHoming() {
        if (!isHoming()) return;
        Entity target = getTargetEntity();
        if (target == null || !target.isEntityAlive()) return;

        double dx = target.posX - posX;
        double dy = (target.posY + target.getEyeHeight()) - posY;
        double dz = target.posZ - posZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist <= getHomingRange() && dist > 0) {
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

    // ==================== VELOCITY HELPERS ====================

    /**
     * Calculate initial velocity toward target or forward based on owner facing.
     * Call from subclass constructors after initProjectile().
     */
    protected void calculateInitialVelocity(EntityLivingBase owner, EntityLivingBase target,
                                            double x, double y, double z) {
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.getEyeHeight()) - y;
            double dz = target.posZ - z;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                this.motionX = (dx / len) * getSpeed();
                this.motionY = (dy / len) * getSpeed();
                this.motionZ = (dz / len) * getSpeed();
            }
        } else {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.motionX = -Math.sin(yaw) * Math.cos(pitch) * getSpeed();
            this.motionY = -Math.sin(pitch) * getSpeed();
            this.motionZ = Math.cos(yaw) * Math.cos(pitch) * getSpeed();
        }
    }

    // ==================== CHARGING NBT ====================

    /**
     * Read common charging state from NBT. Call from subclass readProjectileNBT().
     */
    protected void readChargingNBT(NBTTagCompound nbt) {
        boolean isChargingVal = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.charging = isChargingVal;
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (isChargingVal ? 1 : 0));
        this.chargeDuration = nbt.hasKey("ChargeDuration") ? nbt.getInteger("ChargeDuration") : 40;
        this.chargeTick = nbt.hasKey("ChargeTick") ? nbt.getInteger("ChargeTick") : 0;
        this.targetSize = nbt.hasKey("TargetSize") ? nbt.getFloat("TargetSize") : this.size;
    }

    /**
     * Write common charging state to NBT. Call from subclass writeProjectileNBT().
     */
    protected void writeChargingNBT(NBTTagCompound nbt) {
        nbt.setBoolean("Charging", isCharging());
        nbt.setInteger("ChargeDuration", chargeDuration);
        nbt.setInteger("ChargeTick", chargeTick);
        nbt.setFloat("TargetSize", targetSize);
    }

    // ==================== ENTITY HELPERS ====================

    public IEntity getOwner() {
        if (previewMode) return null;
        if (getOwnerEntity() == null) return null;
        return NpcAPI.Instance().getIEntity(getOwnerEntity());
    }

    public int getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(int id) {
        this.targetEntityId = id;
    }

    public Entity getTargetEntity() {
        if (targetEntityId == -1) return null;
        return worldObj.getEntityByID(targetEntityId);
    }

    public IEntity getTarget() {
        if (getTargetEntity() == null) return null;
        return NpcAPI.Instance().getIEntity(getTargetEntity());
    }

    /**
     * Check if an entity should be ignored for collision.
     * Ignores: the owner, same-faction NPCs, passive NPCs, NPCs friendly to player owner,
     * party members (if friendly fire is off), and other projectiles from the same caster.
     */
    protected boolean shouldIgnoreEntity(Entity entity) {
        Entity owner = getOwnerEntity();
        if (entity == owner) return true;

        // Ignore other projectiles from the same caster (e.g. multi-projectile abilities)
        if (entity instanceof EntityEnergyProjectile) {
            EntityEnergyProjectile other = (EntityEnergyProjectile) entity;
            if (other.ownerEntityId == this.ownerEntityId) return true;
        }

        // NPC target checks
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface targetNpc = (EntityNPCInterface) entity;

            // Ignore passive NPCs
            if (targetNpc.faction.isPassive) return true;

            // NPC owner: ignore same-faction NPCs
            if (owner instanceof EntityNPCInterface) {
                EntityNPCInterface ownerNpc = (EntityNPCInterface) owner;
                if (ownerNpc.faction.id == targetNpc.faction.id) return true;
            }

            // Player owner: ignore NPCs whose faction is friendly to the player
            if (owner instanceof EntityPlayer) {
                if (targetNpc.faction.isFriendlyToPlayer((EntityPlayer) owner)) return true;
            }
        }

        // Party friendly fire check: player owner hitting another player
        if (owner instanceof EntityPlayer && entity instanceof EntityPlayer) {
            EntityPlayer ownerPlayer = (EntityPlayer) owner;
            EntityPlayer targetPlayer = (EntityPlayer) entity;
            PlayerData ownerData = PlayerData.get(ownerPlayer);
            PlayerData targetData = PlayerData.get(targetPlayer);
            if (ownerData.partyUUID != null && ownerData.partyUUID.equals(targetData.partyUUID)) {
                Party party = PartyController.Instance().getParty(ownerData.partyUUID);
                if (party != null && !party.friendlyFire()) return true;
            }
        }

        return false;
    }

    // ==================== VISUAL GETTERS & SETTERS ====================

    public float getSize() {
        return size;
    }

    public void setProjectileSize(float size) {
        this.size = size;
        this.renderCurrentSize = size;
        this.prevRenderSize = size;
    }

    /**
     * Set the start position for distance calculations.
     * Used by factory methods when creating projectiles without the full constructor.
     */
    public void setStartPosition(double x, double y, double z) {
        this.startX = x;
        this.startY = y;
        this.startZ = z;
    }

    // ==================== ROTATION GETTERS ====================

    public float getInterpolatedRotationX(float partialTicks) {
        return this.prevRotationValX + (this.rotationValX - this.prevRotationValX) * partialTicks;
    }

    public float getInterpolatedRotationY(float partialTicks) {
        return this.prevRotationValY + (this.rotationValY - this.prevRotationValY) * partialTicks;
    }

    public float getInterpolatedRotationZ(float partialTicks) {
        return this.prevRotationValZ + (this.rotationValZ - this.prevRotationValZ) * partialTicks;
    }

    public float getInterpolatedSize(float partialTicks) {
        return this.prevRenderSize + (this.renderCurrentSize - this.prevRenderSize) * partialTicks;
    }

    // ==================== LIFESPAN GETTERS & SETTERS ====================

    public float getMaxDistance() {
        return lifespanData.getMaxDistance();
    }

    public void setMaxDistance(float distance) {
        lifespanData.setMaxDistance(distance);
    }

    public int getMaxLifetime() {
        return lifespanData.getMaxLifetime();
    }

    public void setMaxLifetime(int ticks) {
        lifespanData.setMaxLifetime(ticks);
    }

    // ==================== COMBAT GETTERS & SETTERS ====================

    /**
     * Accumulated proportional damage multiplier (0.0-1.0).
     * Accumulates multiplicatively when the projectile breaks through barriers, is reflected,
     * or any other system reduces its effective damage. Used by ability extenders to scale
     * recalculated damage proportionally.
     */
    private float damageMultiplier = 1.0f;

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getDamage() {
        return combatData.getDamage();
    }

    public void setCombatDamage(float damage) {
        combatData.setDamage(damage);
    }

    public float getKnockback() {
        return combatData.knockback;
    }

    public void setCombatKnockback(float knockback) {
        combatData.setKnockback(knockback);
    }

    public float getKnockbackUp() {
        return combatData.knockbackUp;
    }

    public void setCombatKnockbackUp(float knockbackUp) {
        combatData.setKnockbackUp(knockbackUp);
    }

    public boolean isExplosive() {
        return combatData.isExplosive();
    }

    public void setExplosive(boolean explosive) {
        combatData.setExplosive(explosive);
    }

    public float getExplosionRadius() {
        return combatData.getExplosionRadius();
    }

    public void setExplosionRadius(float radius) {
        combatData.setExplosionRadius(radius);
    }

    public float getExplosionDamageFalloff() {
        return combatData.explosionDamageFalloff;
    }

    public void setExplosionDamageFalloff(float falloff) {
        combatData.setExplosionDamageFalloff(falloff);
    }

    public int getHitType() {
        return combatData.hitType.ordinal();
    }

    public void setHitType(int hitType) {
        combatData.hitType = HitType.fromOrdinal(hitType);
    }

    public int getMultiHitDelayTicks() {
        return combatData.multiHitDelayTicks;
    }

    public void setMultiHitDelayTicks(int delayTicks) {
        combatData.multiHitDelayTicks = Math.max(1, delayTicks);
    }

    public int getMaxHits() {
        return combatData.getMaxHits();
    }

    public void setMaxHits(int maxHits) {
        combatData.setMaxHits(maxHits);
    }

    protected boolean canHitEntityNow(EntityLivingBase entity) {
        if (entity == null) return false;
        if (combatData.hitType != HitType.SINGLE && hasReachedMaxHits()) return false;

        int entityId = entity.getEntityId();
        switch (combatData.hitType) {
            case PIERCE:
                return !hitOnceEntities.contains(entityId);
            case MULTI:
                Integer lastHitTick = lastHitTickByEntity.get(entityId);
                if (lastHitTick == null) return true;
                int delay = Math.max(1, combatData.multiHitDelayTicks);
                return ticksExisted - lastHitTick >= delay;
            case SINGLE:
            default:
                return !hasHit;
        }
    }

    protected void recordEntityHit(EntityLivingBase entity) {
        if (entity == null) return;
        int entityId = entity.getEntityId();
        hitOnceEntities.add(entityId);
        lastHitTickByEntity.put(entityId, ticksExisted);
        hitCount++;
    }

    protected boolean hasReachedMaxHits() {
        if (combatData.hitType == HitType.SINGLE) return false;
        return hitCount >= combatData.getMaxHits();
    }

    protected boolean shouldTerminateAfterHit() {
        if (combatData.hitType == HitType.SINGLE) return true;
        return hasReachedMaxHits();
    }

    /**
     * Shared block raytrace helper used by most projectile types.
     */
    protected MovingObjectPosition rayTraceBlocks(double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        Vec3 currentPos = Vec3.createVectorHelper(fromX, fromY, fromZ);
        Vec3 nextPos = Vec3.createVectorHelper(toX, toY, toZ);
        return worldObj.func_147447_a(currentPos, nextPos, false, true, false);
    }

    /**
     * Shared block-hit handling: fires block impact hook, optional explosion, and kills projectile.
     */
    protected boolean handleBlockImpact(MovingObjectPosition blockHit, boolean explodeAtHitPoint) {
        if (blockHit == null || blockHit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return false;

        if (!worldObj.isRemote) {
            EventHooks.onEnergyProjectileBlockImpact(this, blockHit.blockX, blockHit.blockY, blockHit.blockZ);
        }
        hasHit = true;

        if (isExplosive()) {
            if (explodeAtHitPoint && blockHit.hitVec != null) {
                posX = blockHit.hitVec.xCoord;
                posY = blockHit.hitVec.yCoord;
                posZ = blockHit.hitVec.zCoord;
            }
            doExplosion();
        }

        this.setDead();
        return true;
    }

    /**
     * Apply projectile hit behavior to a target and return true if the projectile terminated.
     */
    protected boolean processEntityHit(EntityLivingBase entity, double impactX, double impactY, double impactZ) {
        if (entity == null || shouldIgnoreEntity(entity) || !canHitEntityNow(entity)) return false;

        boolean successfulHit;
        if (isExplosive()) {
            Vec3 impactPoint = resolveEntityImpactPoint(entity, impactX, impactY, impactZ);
            double oldX = posX;
            double oldY = posY;
            double oldZ = posZ;
            posX = impactPoint.xCoord;
            posY = impactPoint.yCoord;
            posZ = impactPoint.zCoord;
            successfulHit = doExplosion();
            posX = oldX;
            posY = oldY;
            posZ = oldZ;
        } else {
            successfulHit = applyDamage(entity);
        }

        if (successfulHit) {
            recordEntityHit(entity);
        }

        if ((successfulHit && shouldTerminateAfterHit()) || (!successfulHit && combatData.hitType == HitType.SINGLE)) {
            hasHit = true;
            this.setDead();
            return true;
        }

        return false;
    }

    /**
     * Resolve a stable impact point on the target so explosive collisions use actual contact.
     */
    protected Vec3 resolveEntityImpactPoint(EntityLivingBase entity, double impactX, double impactY, double impactZ) {
        if (entity == null || entity.boundingBox == null) {
            return Vec3.createVectorHelper(impactX, impactY, impactZ);
        }

        Vec3 segmentStart = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 segmentEnd = Vec3.createVectorHelper(impactX, impactY, impactZ);
        AxisAlignedBB expandedBox = entity.boundingBox.expand(1.0e-4, 1.0e-4, 1.0e-4);
        MovingObjectPosition intercept = expandedBox.calculateIntercept(segmentStart, segmentEnd);
        if (intercept != null && intercept.hitVec != null) {
            return intercept.hitVec;
        }

        Vec3 closest = closestPointOnBoundingBox(entity.boundingBox, impactX, impactY, impactZ);
        return closest != null ? closest : Vec3.createVectorHelper(impactX, impactY, impactZ);
    }

    /**
     * Scan entities in a hitbox and process the first/each valid hit based on hit mode.
     */
    protected boolean processEntitiesInHitBox(AxisAlignedBB hitBox, double impactX, double impactY, double impactZ) {
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);
        for (EntityLivingBase entity : entities) {
            if (processEntityHit(entity, impactX, impactY, impactZ)) {
                return true;
            }
        }
        return false;
    }

    protected boolean handleSolidCollisionTermination() {
        if (!this.isCollidedHorizontally && !this.isCollidedVertically) return false;

        if (!worldObj.isRemote) {
            hasHit = true;
            if (isExplosive()) {
                doExplosion();
            }
        }
        this.setDead();
        return true;
    }

    // ==================== HOMING GETTERS & SETTERS ====================

    public boolean isHoming() {
        return homingData.isHoming();
    }

    public void setHomingEnabled(boolean homing) {
        homingData.setHoming(homing);
    }

    public float getHomingStrength() {
        return homingData.getHomingStrength();
    }

    public void setHomingStrength(float strength) {
        homingData.setHomingStrength(strength);
    }

    public float getHomingRange() {
        return homingData.getHomingRange();
    }

    public void setHomingRange(float range) {
        homingData.setHomingRange(range);
    }

    // ==================== SPEED & ANCHOR GETTERS & SETTERS ====================

    public float getSpeed() {
        return homingData.getSpeed();
    }

    public void setSpeed(float speed) {
        float oldSpeed = homingData.getSpeed();
        homingData.setSpeed(speed);
        // Rescale current motion vector to match new speed
        if (addedToChunk && !worldObj.isRemote) {
            double len = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (len > 0) {
                double scale = speed / len;
                motionX *= scale;
                motionY *= scale;
                motionZ *= scale;
            }
        }
    }

    public AnchorPoint getAnchorPoint() {
        return anchorData.getAnchorPoint();
    }

    public int getAnchor() {
        return anchorData.getAnchor();
    }

    public float getAnchorOffsetX() {
        return anchorData.getAnchorOffsetX();
    }

    public float getAnchorOffsetY() {
        return anchorData.getAnchorOffsetY();
    }

    public float getAnchorOffsetZ() {
        return anchorData.getAnchorOffsetZ();
    }

    // ==================== MOVEMENT GETTERS ====================

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }

    // ==================== STATE GETTERS ====================

    public boolean getHasHit() {
        return hasHit;
    }

    @Override
    public boolean hasLightningEffect() {
        return super.hasLightningEffect() || getBarrierSparkTicks() > 0;
    }

    // ==================== NBT ====================


    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeBaseNBT(nbt);
        writeProjectileNBT(nbt);
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readBaseNBT(nbt);
        readProjectileNBT(nbt);
    }

    /**
     * Read base projectile properties from NBT.
     */
    protected void readBaseNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);

        this.size = sanitize(nbt.getFloat("Size"), 1.0f, MAX_ENTITY_SIZE);

        // Read effects list
        this.effects.clear();
        if (nbt.hasKey("Effects")) {
            NBTTagList effectsList = nbt.getTagList("Effects", 10); // 10 = compound tag
            for (int i = 0; i < effectsList.tagCount(); i++) {
                effects.add(AbilityPotionEffect.fromNBT(effectsList.getCompoundTagAt(i)));
            }
        }

        this.deathWorldTime = nbt.hasKey("DeathWorldTime") ? nbt.getLong("DeathWorldTime") : -1;

        this.startX = nbt.getDouble("StartX");
        this.startY = nbt.getDouble("StartY");
        this.startZ = nbt.getDouble("StartZ");

        this.targetEntityId = nbt.getInteger("TargetId");
        this.reflected = nbt.getBoolean("Reflected");

        this.motionX = nbt.getDouble("MotionX");
        this.motionY = nbt.getDouble("MotionY");
        this.motionZ = nbt.getDouble("MotionZ");

        // Authoritative launch origin comes from spawn NBT (set during fire/startMoving).
        // Sync full client position state to this origin to avoid first-tick spawn/interp pops.
        this.setPosition(this.startX, this.startY, this.startZ);
        this.prevPosX = this.startX;
        this.prevPosY = this.startY;
        this.prevPosZ = this.startZ;
        this.lastTickPosX = this.startX;
        this.lastTickPosY = this.startY;
        this.lastTickPosZ = this.startZ;
        this.interpTargetX = this.startX;
        this.interpTargetY = this.startY;
        this.interpTargetZ = this.startZ;
        this.interpTargetMotionX = this.motionX;
        this.interpTargetMotionY = this.motionY;
        this.interpTargetMotionZ = this.motionZ;
        this.interpSteps = 0;

        // Always initialize render size from NBT size so entities
        // loading in from outside render distance render correctly
        this.renderCurrentSize = this.size;
        this.prevRenderSize = this.size;

        anchorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        syncProjectileColorWatchers();
    }

    /**
     * Write base projectile properties to NBT.
     */
    protected void writeBaseNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);

        nbt.setFloat("Size", size);

        // Write effects list (always write, even if empty, so readBaseNBT finds the key)
        NBTTagList effectsList = new NBTTagList();
        for (AbilityPotionEffect effect : effects) {
            effectsList.appendTag(effect.writeNBT());
        }
        nbt.setTag("Effects", effectsList);

        nbt.setLong("DeathWorldTime", deathWorldTime);

        nbt.setDouble("StartX", startX);
        nbt.setDouble("StartY", startY);
        nbt.setDouble("StartZ", startZ);

        nbt.setInteger("TargetId", targetEntityId);
        nbt.setBoolean("Reflected", reflected);

        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);

        anchorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
    }

    /**
     * Subclass-specific NBT reading.
     */
    protected abstract void readProjectileNBT(NBTTagCompound nbt);

    /**
     * Subclass-specific NBT writing.
     */
    protected abstract void writeProjectileNBT(NBTTagCompound nbt);

    // ==================== COLLISION SETTINGS ====================

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
