package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.HitType;
import kamkeel.npcs.controllers.data.ability.data.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.EnergyTrajectoryData;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.EventHooks;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.entity.IEntity;
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

    // ==================== TRAJECTORY PROPERTIES ====================
    protected EnergyTrajectoryData trajectoryData = new EnergyTrajectoryData();
    protected Map<Integer, Integer> pathDelays;

    // ==================== TRACKING ====================
    protected double startX, startY, startZ;
    protected int targetEntityId = -1;

    // ==================== STATE ====================
    protected boolean hasHit = false;
    protected final Set<Integer> hitOnceEntities = new HashSet<Integer>();
    protected final Map<Integer, Integer> lastHitTickByEntity = new HashMap<Integer, Integer>();

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

    /**
     * Initialize common properties using data classes. Call from subclass constructors.
     */
    protected void initProjectile(EntityLivingBase owner, EntityLivingBase target,
                                  double x, double y, double z, float size,
                                  EnergyDisplayData display, EnergyCombatData combat,
                                  EnergyLightningData lightning, EnergyLifespanData lifespan,
                                  EnergyTrajectoryData trajectory) {
        this.setPosition(x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;

        // Visual
        this.size = size;

        this.displayData = display;
        this.combatData = combat;
        this.lifespanData = lifespan; // deathWorldTime will be set on first tick when world is available
        this.lightningData = lightning;
        this.trajectoryData = trajectory;

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
        }

        // Update rotation
        updateRotation();

        // Lerp render size toward actual size
        this.renderCurrentSize = this.renderCurrentSize + (this.size - this.renderCurrentSize) * 0.15f;

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

        // Check barrier collisions (server-side, non-preview, non-charging)
        if (!previewMode && !worldObj.isRemote && !isCharging()) {
            if (checkBarrierCollision()) {
                return; // Projectile was absorbed by a barrier
            }
        }

        // Subclass-specific update
        updateProjectile();

        // Fire tick event (server-side, non-preview only)
        if (!previewMode && !worldObj.isRemote) {
            EventHooks.onEnergyProjectileTick(this);
        }
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
     * @return true if projectile was absorbed (caller should stop processing)
     */
    protected boolean checkBarrierCollision() {
        List<EntityAbilityBarrier> barriers = EntityAbilityBarrier.getActiveBarriers(worldObj);
        for (EntityAbilityBarrier barrier : barriers) {
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
                if (barrier.onProjectileHit(this, damage)) {
                    hasHit = true;
                    this.setDead();
                    return true;
                }
            }
        }

        return false;
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
        this.interpTargetX = x;
        this.interpTargetY = y;
        this.interpTargetZ = z;
        this.interpSteps = posRotationIncrements;
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
     */
    protected void handleClientInterpolation() {
        if (this.interpSteps > 0) {
            double newX = this.posX + (this.interpTargetX - this.posX) / this.interpSteps;
            double newY = this.posY + (this.interpTargetY - this.posY) / this.interpSteps;
            double newZ = this.posZ + (this.interpTargetZ - this.posZ) / this.interpSteps;

            this.motionX = this.motionX + (this.interpTargetMotionX - this.motionX) / this.interpSteps;
            this.motionY = this.motionY + (this.interpTargetMotionY - this.motionY) / this.interpSteps;
            this.motionZ = this.motionZ + (this.interpTargetMotionZ - this.motionZ) / this.interpSteps;

            this.setPosition(newX, newY, newZ);
            this.interpSteps--;
        } else {
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.setPosition(this.posX, this.posY, this.posZ);
        }
    }

    // ==================== DAMAGE & EFFECTS ====================

    protected void applyDamage(EntityLivingBase target) {
        if (previewMode) return; // Skip damage in preview mode
        applyDamage(target, this.getDamage(), this.getKnockback());
    }

    protected void applyDamage(EntityLivingBase target, float dmg, float kb) {
        if (previewMode) return; // Skip damage in preview mode

        // Fire entity impact event (may cancel or modify damage)
        if (!worldObj.isRemote) {
            float result = EventHooks.onEnergyProjectileEntityImpact(this, target, dmg);
            if (result < 0) return; // Event was cancelled
            dmg = result;
        }

        Entity owner = getOwnerEntity();

        // Check for ability extenders (e.g., DBC Addon damage routing)
        boolean handled = false;
        if (sourceAbility != null && owner instanceof EntityLivingBase) {
            double dx = target.posX - posX;
            double dz = target.posZ - posZ;
            float kbUp = getKnockbackUp() > 0 ? getKnockbackUp() : 0.1f;
            handled = AbilityController.Instance.fireOnAbilityDamage(
                sourceAbility, (EntityLivingBase) owner, target,
                dmg, kb, kbUp, dx, dz);
        }

        if (!handled) {
            // Default damage path
            if (owner instanceof EntityNPCInterface) {
                target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), dmg);
            } else if (owner instanceof EntityPlayer) {
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), dmg);
            } else if (owner instanceof EntityLivingBase) {
                target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), dmg);
            } else {
                target.attackEntityFrom(new NpcDamageSource("npc_ability", null), dmg);
            }
        }

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
    }

    protected void applyEffects(EntityLivingBase target) {
        for (AbilityPotionEffect effect : effects) {
            effect.apply(target);
        }
    }

    /**
     * Set the effects list from the ability's configured effects.
     */
    public void setEffects(List<AbilityPotionEffect> effects) {
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    protected void doExplosion() {
        if (previewMode) return; // Skip explosion in preview mode
        float explosionRad = getExplosionRadius();
        if (Float.isNaN(explosionRad) || explosionRad <= 0) return;
        worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 1.0f, 1.0f);

        Entity owner = getOwnerEntity();

        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            posX - explosionRad, posY - explosionRad, posZ - explosionRad,
            posX + explosionRad, posY + explosionRad, posZ + explosionRad
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, explosionBox);

        for (EntityLivingBase target : targets) {
            if (target == owner) continue;

            double dist = Math.sqrt(
                Math.pow(target.posX - posX, 2) +
                    Math.pow(target.posY - posY, 2) +
                    Math.pow(target.posZ - posZ, 2)
            );

            if (dist <= explosionRad) {
                float falloff = 1.0f - (float) (dist / explosionRad) * getExplosionDamageFalloff();
                applyDamage(target, getDamage() * falloff, getKnockback() * falloff);
            }
        }
    }

    // ==================== LAUNCH HELPERS ====================

    /**
     * For player casters, snap the projectile to a position along the player's look vector.
     * This ensures projectiles launch aligned with the crosshair rather than from the hand anchor.
     * Only affects player casters; NPC projectiles launch from their anchor position as configured.
     * Should be called in startMoving/startFiring BEFORE setting startX/Y/Z.
     */
    protected void snapToPlayerLookVector() {
        Entity owner = getOwnerEntity();
        if (!(owner instanceof EntityPlayer)) return;

        Vec3 look = owner.getLookVec();
        double eyeY = owner.posY + owner.getEyeHeight();
        float frontDist = Math.max(0.5f, size * 0.5f);

        double newX = owner.posX + look.xCoord * frontDist;
        double newY = eyeY + look.yCoord * frontDist;
        double newZ = owner.posZ + look.zCoord * frontDist;

        setPosition(newX, newY, newZ);
        prevPosX = newX;
        prevPosY = newY;
        prevPosZ = newZ;
    }

    // ==================== CHARGING METHODS (projectile-specific) ====================

    /**
     * Setup this entity in charging mode (for windup phase).
     * Grows from 0 to current size over chargeDuration ticks.
     * Subclasses with different charging behavior should override or use their own method.
     */
    public void setupCharging(EnergyAnchorData anchor, int chargeDuration) {
        setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
        this.anchorData = anchor;
        this.targetSize = this.size;
        this.size = 0.01f;
        this.renderCurrentSize = 0.01f;
        this.prevRenderSize = 0.01f;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
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
        return combatData.explosionRadius;
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

    protected boolean canHitEntityNow(EntityLivingBase entity) {
        if (entity == null) return false;

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
    }

    protected boolean shouldTerminateAfterHit() {
        return combatData.hitType == HitType.SINGLE;
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

        recordEntityHit(entity);
        if (isExplosive()) {
            double oldX = posX;
            double oldY = posY;
            double oldZ = posZ;
            posX = impactX;
            posY = impactY;
            posZ = impactZ;
            doExplosion();
            posX = oldX;
            posY = oldY;
            posZ = oldZ;
        } else {
            applyDamage(entity);
        }

        if (shouldTerminateAfterHit()) {
            hasHit = true;
            this.setDead();
            return true;
        }

        return false;
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
        homingData.setSpeed(speed);
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

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

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

        this.motionX = nbt.getDouble("MotionX");
        this.motionY = nbt.getDouble("MotionY");
        this.motionZ = nbt.getDouble("MotionZ");

        // Always initialize render size from NBT size so entities
        // loading in from outside render distance render correctly
        this.renderCurrentSize = this.size;
        this.prevRenderSize = this.size;

        anchorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        trajectoryData.readNBT(nbt);
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

        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);

        anchorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
        trajectoryData.writeNBT(nbt);
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
