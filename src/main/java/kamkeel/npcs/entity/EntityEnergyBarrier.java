package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyBarrierData;
import kamkeel.npcs.network.packets.data.energy.BarrierClientSyncPacket;
import kamkeel.npcs.util.AttributeAttackUtil;
import kamkeel.npcs.util.CNPCDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.wrapper.nbt.NBTWrapper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for energy barrier entities (Dome, Panel).
 * Provides shared health, damage, hit flash, knockback target,
 * and barrier lifecycle management.
 * Extends EntityEnergyAbility for shared visual/owner/charging state.
 */
public abstract class EntityEnergyBarrier extends EntityEnergyAbility {

    /**
     * Outcome of a projectile vs barrier interaction.
     */
    public enum ProjectileHitResult {
        PASS,
        BLOCKED,
        BROKEN;

        public boolean isAbsorbed() {
            return this == BLOCKED || this == BROKEN;
        }
    }

    /**
     * Full result of a projectile vs barrier hit after multiplier/event processing.
     */
    public static class ProjectileHitOutcome {
        public final ProjectileHitResult result;
        public final float barrierHealthBefore;
        public final float barrierHealthAfter;
        public final float appliedBarrierDamage;
        public final float remainingProjectileDamage;
        public final boolean reflectEnabled;
        public final float reflectStrengthPct;
        public final boolean useHealth;

        public ProjectileHitOutcome(ProjectileHitResult result,
                                    float barrierHealthBefore,
                                    float barrierHealthAfter,
                                    float appliedBarrierDamage,
                                    float remainingProjectileDamage,
                                    boolean reflectEnabled,
                                    float reflectStrengthPct,
                                    boolean useHealth) {
            this.result = result;
            this.barrierHealthBefore = barrierHealthBefore;
            this.barrierHealthAfter = barrierHealthAfter;
            this.appliedBarrierDamage = appliedBarrierDamage;
            this.remainingProjectileDamage = remainingProjectileDamage;
            this.reflectEnabled = reflectEnabled;
            this.reflectStrengthPct = Math.max(0.0f, Math.min(100.0f, reflectStrengthPct));
            this.useHealth = useHealth;
        }

        public boolean shouldReflect() {
            return reflectEnabled && result == ProjectileHitResult.BLOCKED;
        }
    }

    // ==================== ACTIVE BARRIER REGISTRY ====================
    private static final List<WeakReference<EntityEnergyBarrier>> activeBarriers = new ArrayList<>();
    private boolean tracked = false;

    public static void trackBarrier(EntityEnergyBarrier barrier) {
        activeBarriers.add(new WeakReference<>(barrier));
    }

    /**
     * Get all living barriers in the given world. Auto-cleans dead refs.
     */
    public static List<EntityEnergyBarrier> getActiveBarriers(World world) {
        List<EntityEnergyBarrier> result = new ArrayList<>();
        Iterator<WeakReference<EntityEnergyBarrier>> it = activeBarriers.iterator();
        while (it.hasNext()) {
            EntityEnergyBarrier b = it.next().get();
            if (b == null || b.isDead) {
                it.remove();
            } else if (b.worldObj == world) {
                result.add(b);
            }
        }
        return result;
    }

    public static void clearAllBarriers() {
        activeBarriers.clear();
    }

    // ==================== BARRIER PROPERTIES ====================
    protected EnergyBarrierData barrierData = new EnergyBarrierData();
    protected float currentHealth;
    protected int ticksAlive = 0;

    // ==================== DATA WATCHER INDICES ====================
    protected static final int DW_HEALTH_PERCENT = 21;
    protected static final int DW_HIT_FLASH = 22;

    public EntityEnergyBarrier(World world) {
        super(world);
        this.noClip = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit(); // DW_CHARGING = 20
        this.dataWatcher.addObject(DW_HEALTH_PERCENT, 1.0f);
        this.dataWatcher.addObject(DW_HIT_FLASH, (byte) 0);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Check if a projectile is incoming toward this barrier.
     * Dome uses spherical check, Panel uses planar check.
     */
    public abstract boolean isIncomingProjectile(EntityEnergyProjectile projectile);

    /**
     * Setup charging state. Called during windup phase.
     */
    public abstract void setupCharging(int duration);

    /**
     * Finish charging — snap to full size and become active.
     */
    public abstract void finishCharging();

    /**
     * Process entity physics for this barrier.
     * Handles solid wall (entity repositioning) and knockback (repulsion force).
     */
    protected abstract void processEntityPhysics();

    /**
     * Get the maximum extent of this barrier for distance pre-filtering.
     * Dome returns radius, Panel returns max(width, height) / 2.
     */
    public abstract float getMaxExtent();

    /**
     * Check if a generic (non-CNPC+) projectile is incoming toward this barrier.
     * Uses the same geometric logic as isIncomingProjectile but with raw position data.
     *
     * @param posX, posY, posZ           Current (or predicted) position of the projectile
     * @param motionX, motionY, motionZ  Current velocity of the projectile
     * @param prevPosX, prevPosY, prevPosZ Previous position of the projectile
     * @param ownerEntityId              Entity ID of the projectile's shooter
     */
    public abstract boolean isIncomingGenericProjectile(
        double posX, double posY, double posZ,
        double motionX, double motionY, double motionZ,
        double prevPosX, double prevPosY, double prevPosZ,
        int ownerEntityId);

    /**
     * Compute the outward surface normal at a given hit point on this barrier.
     * Used by external systems (e.g., mixin-based projectile reflection) that cannot
     * access barrier-subtype internals.
     *
     * @param hitX, hitY, hitZ    Position near the barrier surface
     * @param velX, velY, velZ    Incoming velocity (used as fallback direction)
     * @return double[3] normalized surface normal pointing outward from the barrier
     */
    public abstract double[] getSurfaceNormal(double hitX, double hitY, double hitZ,
                                               double velX, double velY, double velZ);

    /**
     * Compute a position just outside the barrier surface, along the outward normal
     * from the given position. Used to snap paused/reflected projectiles outside
     * the collision zone.
     *
     * @param px, py, pz              Current projectile position
     * @param velX, velY, velZ        Current velocity (used as fallback for direction)
     * @param bias                    Distance outside the surface to snap to
     * @return double[3] position outside the barrier surface
     */
    public abstract double[] getOutsideSurfacePoint(double px, double py, double pz,
                                                     double velX, double velY, double velZ,
                                                     float bias);

    // ==================== GENERIC PROJECTILE HIT ====================

    /**
     * Apply damage from a generic (non-CNPC+) projectile to this barrier.
     * Uses the typeId for damage multiplier lookup (e.g. "dbc.ki_attack").
     *
     * @param projectileEntity The entity that hit the barrier (for context)
     * @param damage           Base damage to apply
     * @param typeId           Type identifier for damage multiplier lookup
     * @return true if the barrier absorbed the hit (projectile should be destroyed)
     */
    public boolean onGenericProjectileHit(Entity projectileEntity, float damage, String typeId) {
        float multiplier = barrierData.getMultiplier(typeId);
        float finalDamage = damage * multiplier;

        if (!worldObj.isRemote) {
            float eventDamage = EventHooks.onEnergyBarrierHit(this, ownerEntityId, null, finalDamage);
            if (eventDamage < 0) return false;
            finalDamage = eventDamage;
        }

        triggerHitFlash();

        if (!barrierData.useHealth) {
            return true;
        }

        currentHealth -= finalDamage;
        syncHealthPercent();

        if (currentHealth <= 0) {
            onBarrierDestroyed();
            this.setDead();
        }

        return true;
    }

    /**
     * Apply damage from a generic (non-CNPC+) projectile and return full hit context.
     * Same logic as onProjectileHitResolved() but for raw Entity + typeId.
     *
     * @param projectileEntity The entity that hit the barrier (for event context)
     * @param damage           Base damage to apply
     * @param typeId           Type identifier for damage multiplier lookup (e.g., "dbc.ki_attack")
     * @return Full ProjectileHitOutcome with result, reflect data, remaining damage
     */
    public ProjectileHitOutcome onGenericProjectileHitResolved(Entity projectileEntity, float damage, String typeId) {
        float multiplier = barrierData.getMultiplier(typeId);
        float finalDamage = damage * multiplier;

        // Apply magic interactions if projectile is a CNPC+ energy entity
        if (projectileEntity instanceof EntityEnergyAbility) {
            float magicMultiplier = AttributeAttackUtil.calculateMagicInteractionMultiplier(
                ((EntityEnergyAbility) projectileEntity).getMagicData(), this.magicData);
            finalDamage *= magicMultiplier;
        }

        if (!worldObj.isRemote) {
            float eventDamage = EventHooks.onEnergyBarrierHit(this, ownerEntityId, null, finalDamage);
            if (eventDamage < 0) {
                return new ProjectileHitOutcome(
                    ProjectileHitResult.PASS,
                    currentHealth, currentHealth,
                    0.0f, 0.0f,
                    barrierData.reflect, barrierData.reflectStrengthPct,
                    barrierData.useHealth
                );
            }
            finalDamage = eventDamage;
        }

        triggerHitFlash();
        float healthBefore = currentHealth;

        if (!barrierData.useHealth) {
            return new ProjectileHitOutcome(
                ProjectileHitResult.BLOCKED,
                healthBefore, healthBefore,
                0.0f, 0.0f,
                barrierData.reflect, barrierData.reflectStrengthPct,
                false
            );
        }

        currentHealth -= finalDamage;
        syncHealthPercent();

        if (currentHealth <= 0) {
            float remaining = Math.max(0.0f, finalDamage - healthBefore);
            onBarrierDestroyed();
            this.setDead();
            return new ProjectileHitOutcome(
                ProjectileHitResult.BROKEN,
                healthBefore, 0.0f,
                finalDamage, remaining,
                barrierData.reflect, barrierData.reflectStrengthPct,
                true
            );
        }

        return new ProjectileHitOutcome(
            ProjectileHitResult.BLOCKED,
            healthBefore, currentHealth,
            finalDamage, 0.0f,
            barrierData.reflect, barrierData.reflectStrengthPct,
            true
        );
    }

    // ==================== PROJECTILE HIT ====================

    /**
     * Apply damage to this barrier from a projectile.
     * Returns true if the barrier absorbed the hit (projectile should be destroyed).
     */
    public boolean onProjectileHit(EntityEnergyProjectile projectile, float baseDamage) {
        return onProjectileHitDetailed(projectile, baseDamage).isAbsorbed();
    }

    /**
     * Apply damage to this barrier from a projectile and return detailed outcome.
     */
    public ProjectileHitResult onProjectileHitDetailed(EntityEnergyProjectile projectile, float baseDamage) {
        return onProjectileHitResolved(projectile, baseDamage).result;
    }

    /**
     * Apply damage to this barrier from a projectile and return full hit context.
     */
    public ProjectileHitOutcome onProjectileHitResolved(EntityEnergyProjectile projectile, float baseDamage) {
        // Get damage multiplier for this projectile type
        String typeId = "";
        if (projectile.getSourceAbility() != null) {
            typeId = projectile.getSourceAbility().getTypeId();
        }
        float multiplier = barrierData.getMultiplier(typeId);
        float damage = baseDamage * multiplier;

        // Apply magic interactions (e.g., Water projectile vs Fire barrier)
        float magicMultiplier = AttributeAttackUtil.calculateMagicInteractionMultiplier(
            projectile.getMagicData(), this.magicData);
        damage *= magicMultiplier;

        // Fire hit event (may cancel or modify damage)
        if (!worldObj.isRemote) {
            float eventDamage = EventHooks.onEnergyBarrierHit(this, ownerEntityId, projectile, damage);
            if (eventDamage < 0) {
                return new ProjectileHitOutcome(
                    ProjectileHitResult.PASS,
                    currentHealth,
                    currentHealth,
                    0.0f,
                    0.0f,
                    barrierData.reflect,
                    barrierData.reflectStrengthPct,
                    barrierData.useHealth
                );
            }
            damage = eventDamage;
        }

        triggerHitFlash();

        float healthBefore = currentHealth;

        if (!barrierData.useHealth) {
            return new ProjectileHitOutcome(
                ProjectileHitResult.BLOCKED,
                healthBefore,
                healthBefore,
                0.0f,
                0.0f,
                barrierData.reflect,
                barrierData.reflectStrengthPct,
                false
            );
        }

        currentHealth -= damage;
        syncHealthPercent();

        if (currentHealth <= 0) {
            float remaining = Math.max(0.0f, damage - healthBefore);
            onBarrierDestroyed();
            this.setDead();
            return new ProjectileHitOutcome(
                ProjectileHitResult.BROKEN,
                healthBefore,
                0.0f,
                damage,
                remaining,
                barrierData.reflect,
                barrierData.reflectStrengthPct,
                true
            );
        }

        return new ProjectileHitOutcome(
            ProjectileHitResult.BLOCKED,
            healthBefore,
            currentHealth,
            damage,
            0.0f,
            barrierData.reflect,
            barrierData.reflectStrengthPct,
            true
        );
    }

    // ==================== MELEE DAMAGE ====================

    @Override
    public boolean canBeCollidedWith() {
        return barrierData.meleeEnabled;
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }

    @Override
    protected boolean func_145771_j(double x, double y, double z) {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (worldObj.isRemote || !barrierData.meleeEnabled) return false;
        if (isCharging()) return false;

        float damage = amount * barrierData.meleeDamageMultiplier;

        // Apply magic interactions from attacker's magic vs barrier's magic
        Entity attacker = source.getEntity();
        if (attacker != null) {
            MagicData attackerMagic = null;
            if (attacker instanceof EntityPlayer) {
                PlayerData data = PlayerData.get((EntityPlayer) attacker);
                if (data != null) attackerMagic = data.magicData;
            } else if (attacker instanceof EntityNPCInterface) {
                EntityNPCInterface npc = (EntityNPCInterface) attacker;
                if (npc.stats != null) attackerMagic = npc.stats.magicData;
            }
            if (attackerMagic != null) {
                float magicMultiplier = AttributeAttackUtil.calculateMagicInteractionMultiplier(
                    attackerMagic, this.magicData);
                damage *= magicMultiplier;
            }
        }

        // Fire hit event with null projectile (melee hit)
        float eventDamage = EventHooks.onEnergyBarrierHit(this, ownerEntityId, null, damage);
        if (eventDamage < 0) return false;
        damage = eventDamage;

        triggerHitFlash();

        if (!barrierData.useHealth) return true;

        currentHealth -= damage;
        syncHealthPercent();

        if (currentHealth <= 0) {
            onBarrierDestroyed();
            this.setDead();
        }
        return true;
    }

    // ==================== SHARED UPDATE LOGIC ====================

    /**
     * Shared barrier tick logic for onUpdate(). Handles spawned event, owner death,
     * duration check, hit flash decay, and tick event.
     * Call this after incrementing ticksAlive in subclass onUpdate().
     *
     * @return true if barrier died this tick (caller should return early)
     */
    protected boolean updateBarrierTick() {
        if (!worldObj.isRemote) {
            // Track barrier and fire spawned event on first tick
            if (ticksAlive == 1) {
                if (!tracked) {
                    trackBarrier(this);
                    tracked = true;
                }
                EventHooks.onEnergyBarrierSpawned(this, ownerEntityId);
            }

            // Check owner death
            if (ownerEntityId >= 0 && ticksAlive > 5) {
                Entity owner = worldObj.getEntityByID(ownerEntityId);
                if (owner != null) {
                    if (owner.isDead) {
                        onBarrierDestroyed();
                        this.setDead();
                        return true;
                    }
                    if (owner instanceof EntityNPCInterface && ((EntityNPCInterface) owner).isKilled()) {
                        onBarrierDestroyed();
                        this.setDead();
                        return true;
                    }
                }
            }

            // Duration check
            if (barrierData.useDuration && ticksAlive >= barrierData.durationTicks) {
                onBarrierDestroyed();
                this.setDead();
                return true;
            }

            // Absolute hard lifetime cap (safety net for barriers with no duration)
            if (ticksAlive > BARRIER_HARD_LIFETIME_CAP) {
                onBarrierDestroyed();
                this.setDead();
                return true;
            }

            // Reset hit flash
            if (getHitFlash() > 0) {
                setHitFlash((byte) (getHitFlash() - 1));
            }

            // Fire tick event
            EventHooks.onEnergyBarrierTick(this, ownerEntityId);
        }
        return false;
    }

    // ==================== HELPERS ====================

    protected void onBarrierDestroyed() {
        EventHooks.onEnergyBarrierDestroyed(this, ownerEntityId);
    }

    protected void syncHealthPercent() {
        float percent = barrierData.useHealth && barrierData.maxHealth > 0
            ? Math.max(0, currentHealth / barrierData.maxHealth)
            : 1.0f;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_HEALTH_PERCENT, percent);
        }
    }

    protected void triggerHitFlash() {
        if (!worldObj.isRemote) {
            setHitFlash((byte) 4);
        }
    }

    protected void setHitFlash(byte value) {
        this.dataWatcher.updateObject(DW_HIT_FLASH, value);
    }

    public byte getHitFlash() {
        return this.dataWatcher.getWatchableObjectByte(DW_HIT_FLASH);
    }

    public float getHealthPercent() {
        return this.dataWatcher.getWatchableObjectFloat(DW_HEALTH_PERCENT);
    }

    /**
     * Client prediction only runs for the local player.
     */
    protected EntityPlayer getClientPredictionPlayer() {
        return worldObj != null && worldObj.isRemote ? CustomNpcs.proxy.getPlayer() : null;
    }

    /**
     * Shared target filter used by Dome/Panel physics loops.
     */
    protected boolean shouldSkipBarrierPhysicsTarget(EntityLivingBase entity, EntityPlayer clientPredictionPlayer) {
        if (entity == null || !entity.isEntityAlive()) return true;
        if (entity.getEntityId() == ownerEntityId) return true;
        if (worldObj.isRemote && (clientPredictionPlayer == null || entity != clientPredictionPlayer)) return true;
        return isAllyOfOwner(entity);
    }

    /**
     * Check if an entity is an ally of this barrier's owner.
     * Server uses full relationship logic; client uses a lightweight safe approximation.
     */
    protected boolean isAllyOfOwner(EntityLivingBase entity) {
        Entity owner = getOwnerEntity();
        if (owner == null || !(owner instanceof EntityLivingBase)) return false;
        if (worldObj != null && worldObj.isRemote) {
            return isClientPredictedAlly((EntityLivingBase) owner, entity);
        }
        return AbilityTargetHelper.isAlly((EntityLivingBase) owner, entity);
    }

    /**
     * Client-safe ally approximation used only for movement prediction.
     * Avoids PlayerData/Party/Faction point lookups that are server-owned.
     */
    protected boolean isClientPredictedAlly(EntityLivingBase owner, EntityLivingBase target) {
        if (owner == null || target == null) return false;
        if (owner == target) return true;

        if (target instanceof EntityNPCInterface) {
            EntityNPCInterface targetNpc = (EntityNPCInterface) target;
            if (targetNpc.faction.isPassive) return true;
            if (owner instanceof EntityNPCInterface) {
                EntityNPCInterface ownerNpc = (EntityNPCInterface) owner;
                return ownerNpc.faction.id == targetNpc.faction.id;
            }
        }

        return false;
    }

    // ==================== CONTAINMENT & ABSORBING ====================

    /**
     * Check if an entity is geometrically inside this barrier's protected zone.
     * Override in subclasses (dome checks sphere containment).
     */
    public boolean isEntityInside(Entity entity) {
        return false;
    }

    /**
     * Find a barrier that would absorb damage for the given entity (its caster).
     * Returns the barrier if: absorbing is enabled, entity is the barrier's owner,
     * and entity is within the barrier's absorb radius of the barrier's current position.
     *
     * Absorb radius behavior:
     *  -1 = no limit (absorb regardless of distance)
     *   0 = uses the barrier's extent as the radius (dome radius, panel half-size, etc.)
     *  >0 = owner must be within this many blocks of the barrier entity
     */
    public static EntityEnergyBarrier getAbsorbingBarrier(Entity entity) {
        if (entity == null || entity.worldObj == null) return null;
        List<EntityEnergyBarrier> barriers = getActiveBarriers(entity.worldObj);
        for (EntityEnergyBarrier barrier : barriers) {
            if (barrier.isDead) continue;
            if (!barrier.barrierData.absorbing) continue;
            if (barrier.ownerEntityId != entity.getEntityId()) continue;

            float absorbRadius = barrier.barrierData.absorbRadius;

            // -1 = unlimited range
            if (absorbRadius < 0) {
                return barrier;
            }

            // 0 = use barrier's own extent as the effective radius
            // Domes: dome radius. Panels: max(width, height) * 0.5 + 1.0, doubled.
            float effectiveRadius;
            if (absorbRadius > 0) {
                effectiveRadius = absorbRadius;
            } else if (barrier instanceof EntityEnergyDome) {
                effectiveRadius = barrier.getMaxExtent();
            } else {
                effectiveRadius = barrier.getMaxExtent() * 3.0f;
            }

            // Distance check from barrier's current position to the owner
            double dx = entity.posX - barrier.posX;
            double dy = (entity.posY + entity.height * 0.5) - barrier.posY;
            double dz = entity.posZ - barrier.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= (double) effectiveRadius * effectiveRadius) {
                return barrier;
            }
        }
        return null;
    }

    /**
     * Absorb damage from an external source. Reduces barrier health directly.
     * Used by damage protection to redirect attacks on inside entities to the barrier.
     *
     * @param amount The damage amount to absorb
     * @return true if damage was absorbed (barrier still alive or just destroyed)
     */
    public boolean absorbDamage(float amount) {
        if (worldObj.isRemote) return false;

        triggerHitFlash();

        if (!barrierData.useHealth) {
            return true; // Duration-only: absorb but no health loss
        }

        currentHealth -= amount;
        syncHealthPercent();

        if (currentHealth <= 0) {
            onBarrierDestroyed();
            this.setDead();
        }
        return true;
    }

    /**
     * Teleport an entity to a position. Uses network handler for players
     * to ensure reliable client synchronization.
     * Skips teleport if destination intersects solid blocks.
     */
    protected void teleportEntity(EntityLivingBase ent, double x, double y, double z) {
        if (!isPositionClear(ent, x, y, z)) return;

        if (ent instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) ent;
            player.playerNetServerHandler.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        } else {
            ent.setPosition(x, y, z);
        }
    }

    /**
     * Returns true if placing the entity at (x, y, z) would not intersect solid blocks.
     */
    protected boolean isPositionClear(EntityLivingBase ent, double x, double y, double z) {
        double halfW = ent.width * 0.5;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            x - halfW, y, z - halfW,
            x + halfW, y + ent.height, z + halfW
        );
        return worldObj.getCollidingBoundingBoxes(ent, box).isEmpty();
    }

    /**
     * Returns true if the entity can be pushed in the given direction without hitting solid blocks.
     * Probes a small distance (0.3 blocks) ahead in the push direction.
     */
    protected boolean canPushInDirection(EntityLivingBase ent, double dirX, double dirY, double dirZ) {
        double probe = 0.3;
        double halfW = ent.width * 0.5;
        double px = ent.posX + dirX * probe;
        double py = ent.posY + dirY * probe;
        double pz = ent.posZ + dirZ * probe;
        // Never probe below the entity's feet — a downward probe always detects the
        // block the entity is already standing/hovering above (false positive).
        // MC's own collision system prevents entities from falling through floors,
        // so the downward check is redundant. Upward probes (ceiling detection) still work.
        if (py < ent.posY) {
            py = ent.posY;
        }
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            px - halfW, py, pz - halfW,
            px + halfW, py + ent.height, pz + halfW
        );
        return worldObj.getCollidingBoundingBoxes(ent, box).isEmpty();
    }

    // ==================== GETTERS ====================

    public EnergyBarrierData getBarrierData() {
        return barrierData;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float health) {
        this.currentHealth = health;
        syncHealthPercent();
    }

    /**
     * Override the barrier's max health and set current health to match.
     * Used by extenders (e.g., DBC scaling) to dynamically set barrier HP at spawn time.
     */
    public void setBarrierMaxHealth(float maxHealth) {
        this.barrierData.setMaxHealth(maxHealth);
        this.currentHealth = maxHealth;
        syncHealthPercent();
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    // ==================== CLIENT SYNC ====================

    /**
     * Write all client-relevant barrier state for sync.
     * Subclasses should override writeBarrierClientSyncData for type-specific fields.
     */
    public NBTTagCompound writeClientSyncData() {
        NBTTagCompound nbt = new NBTTagCompound();
        // Position
        nbt.setDouble("PosX", posX);
        nbt.setDouble("PosY", posY);
        nbt.setDouble("PosZ", posZ);
        // Display
        nbt.setInteger("InnerColor", getInnerColor());
        nbt.setFloat("InnerAlpha", getInnerAlpha());
        nbt.setInteger("OuterColor", getOuterColor());
        nbt.setBoolean("OuterColorEnabled", isOuterColorEnabled());
        nbt.setFloat("OuterColorWidth", getOuterColorWidth());
        nbt.setFloat("OuterColorAlpha", getOuterColorAlpha());
        nbt.setFloat("RotationSpeed", getRotationSpeed());
        // Lightning
        nbt.setBoolean("LightningEffect", hasLightningEffect());
        nbt.setFloat("LightningDensity", getLightningDensity());
        nbt.setFloat("LightningRadius", getLightningRadius());
        nbt.setInteger("LightningFadeTime", getLightningFadeTime());
        writeBarrierClientSyncData(nbt);
        return nbt;
    }

    /**
     * Apply client sync data from server.
     * Subclasses should override applyBarrierClientSyncData for type-specific fields.
     */
    public void applyClientSyncData(NBTTagCompound nbt) {
        // Position
        setPosition(nbt.getDouble("PosX"), nbt.getDouble("PosY"), nbt.getDouble("PosZ"));
        // Display
        displayData.setInnerColor(nbt.getInteger("InnerColor"));
        displayData.setInnerAlpha(nbt.getFloat("InnerAlpha"));
        displayData.setOuterColor(nbt.getInteger("OuterColor"));
        displayData.setOuterColorEnabled(nbt.getBoolean("OuterColorEnabled"));
        displayData.setOuterColorWidth(nbt.getFloat("OuterColorWidth"));
        displayData.setOuterColorAlpha(nbt.getFloat("OuterColorAlpha"));
        displayData.setRotationSpeed(nbt.getFloat("RotationSpeed"));
        // Lightning
        setLightningEffect(nbt.getBoolean("LightningEffect"));
        setLightningDensity(nbt.getFloat("LightningDensity"));
        setLightningRadius(nbt.getFloat("LightningRadius"));
        setLightningFadeTime(nbt.getInteger("LightningFadeTime"));
        applyBarrierClientSyncData(nbt);
    }

    /**
     * Subclass hook: write type-specific client sync data (dome radius, panel dimensions, etc.).
     */
    protected void writeBarrierClientSyncData(NBTTagCompound nbt) {
    }

    /**
     * Subclass hook: apply type-specific client sync data on client.
     */
    protected void applyBarrierClientSyncData(NBTTagCompound nbt) {
    }

    /**
     * Send full client sync to all tracking clients.
     */
    public void sendClientSync() {
        if (worldObj == null || worldObj.isRemote) return;
        BarrierClientSyncPacket.sendToTracking(this, writeClientSyncData());
    }

    // ==================== NBT HELPERS ====================

    /**
     * Write shared barrier base fields to NBT.
     * Call from subclass writeEntityToNBT() before writing type-specific fields.
     */
    protected void writeBarrierBaseNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
        nbt.setInteger("TicksAlive", ticksAlive);
        nbt.setFloat("CurrentHealth", currentHealth);
        nbt.setBoolean("Charging", charging);
        nbt.setInteger("ChargeTick", chargeTick);
        nbt.setInteger("ChargeDuration", chargeDuration);
        barrierData.writeNBT(new NBTWrapper(nbt));
    }

    /**
     * Read shared barrier base fields from NBT.
     * Call from subclass readEntityFromNBT() before reading type-specific fields.
     */
    protected void readBarrierBaseNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
        this.ticksAlive = nbt.getInteger("TicksAlive");
        this.currentHealth = nbt.getFloat("CurrentHealth");
        if (Float.isNaN(currentHealth) || Float.isInfinite(currentHealth) || currentHealth < 0) currentHealth = barrierData.maxHealth;
        this.charging = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.chargeTick = nbt.getInteger("ChargeTick");
        this.chargeDuration = nbt.getInteger("ChargeDuration");
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (charging ? 1 : 0));
        barrierData.readNBT(new NBTWrapper(nbt));
    }

    // ==================== DEBUG LOGGING ====================

    /**
     * Debug log called every tick for barriers. Subclasses override debugLogBarrierExtra().
     */
    protected void debugLogBarrierTick() {
        boolean isClient = worldObj.isRemote;
        if (isClient ? !CNPCDebug.isClientEnabled("energy") : !CNPCDebug.isServerEnabled("energy"))
            return;

        String className = getClass().getSimpleName();
        String base = String.format("[%s id=%d tick=%d] pos=(%.2f,%.2f,%.2f) charging=%b health=%.1f",
            className, getEntityId(), ticksAlive,
            posX, posY, posZ, isCharging(), currentHealth);

        String extra = debugLogBarrierExtra();
        String full = extra.isEmpty() ? base : base + " " + extra;
        CNPCDebug.log("energy", isClient, full);
    }

    /**
     * Subclass hook for barrier-specific debug data.
     */
    protected String debugLogBarrierExtra() {
        return "";
    }
}
