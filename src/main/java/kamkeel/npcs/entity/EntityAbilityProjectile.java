package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;

import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.entity.IEnergyProjectile;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class for all ability projectiles (Orb, Disc, Beam, Laser).
 * Provides common functionality for visuals, combat, effects, and interpolation.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public abstract class EntityAbilityProjectile extends Entity implements IEnergyProjectile, IEntityAdditionalSpawnData {

    // ==================== VISUAL PROPERTIES ====================
    protected float size = 1.0f;

    // ==================== COMBAT PROPERTIES ====================
    protected EnergyCombatData combatData = new EnergyCombatData();

    // ==================== EFFECT PROPERTIES ====================
    protected List<AbilityEffect> effects = new ArrayList<>();

    // ==================== LIGHTNING EFFECT PROPERTIES ====================
    protected EnergyLightningData lightningData = new EnergyLightningData();

    // Client-side lightning state (not saved to NBT)
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public transient Object lightningState; // Actually AttachedLightningRenderer.LightningState

    // ==================== DISPLAY PROPERTIES ====================
    protected EnergyDisplayData displayData = new EnergyDisplayData();

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
    protected int ownerEntityId = -1;
    protected int targetEntityId = -1;

    // ==================== STATE ====================
    protected boolean hasHit = false;
    protected boolean previewMode = false; // Client-side preview mode (no damage/effects)
    protected EntityLivingBase previewOwner = null; // Direct reference for GUI preview (no world lookup)

    /** The ability that spawned this projectile. Transient, not saved to NBT. */
    protected transient Ability sourceAbility = null;

    // ==================== CHARGING STATE ====================
    protected boolean charging = false;
    protected int chargeDuration = 40;
    protected int chargeTick = 0;
    protected float targetSize = 1.0f;
    protected static final int DW_CHARGING = 20;

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

    public EntityAbilityProjectile(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.noClip = false;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
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
    protected void entityInit() {
        this.dataWatcher.addObject(DW_CHARGING, (byte) 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
        d1 *= 128.0D;
        return distance < d1 * d1;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1; // Translucent pass
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

            // Set death time on first tick if not already set (handles chunk load/unload)
            if (deathWorldTime < 0 && worldObj != null) {
                deathWorldTime = worldObj.getTotalWorldTime() + getMaxLifetime();
            }

            // Check lifespan using world time (survives chunk unload/reload)
            if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
                this.setDead();
                return;
            }

            // Check max distance (subclass can override if needed)
            if (checkMaxDistance()) {
                this.setDead();
                return;
            }

            if (hasHit) {
                this.setDead();
                return;
            }
        }

        // Subclass-specific update
        updateProjectile();
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

    // ==================== PREVIEW MODE ====================

    /**
     * Set preview mode for GUI display.
     * In preview mode, no damage or world effects are applied.
     */
    public void setPreviewMode(boolean preview) {
        this.previewMode = preview;
    }

    /**
     * Check if entity is in preview mode.
     */
    public boolean isPreviewMode() {
        return previewMode;
    }

    // ==================== DAMAGE & EFFECTS ====================

    protected void applyDamage(EntityLivingBase target) {
        if (previewMode) return; // Skip damage in preview mode
        applyDamage(target, this.getDamage(), this.getKnockback());
    }

    protected void applyDamage(EntityLivingBase target, float dmg, float kb) {
        if (previewMode) return; // Skip damage in preview mode
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
        for (AbilityEffect effect : effects) {
            effect.apply(target);
        }
    }

    /**
     * Set the effects list from the ability's configured effects.
     */
    public void setEffects(List<AbilityEffect> effects) {
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    /**
     * Set the source ability that spawned this projectile.
     * Used by external damage handlers to access ability-specific data.
     */
    public void setSourceAbility(Ability ability) {
        this.sourceAbility = ability;
    }

    /**
     * Get the source ability that spawned this projectile.
     */
    public Ability getSourceAbility() {
        return sourceAbility;
    }

    protected void doExplosion() {
        if (previewMode) return; // Skip explosion in preview mode
        worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 1.0f, 1.0f);

        Entity owner = getOwnerEntity();

        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            posX - getExplosionRadius(), posY - getExplosionRadius(), posZ - getExplosionRadius(),
            posX + getExplosionRadius(), posY + getExplosionRadius(), posZ + getExplosionRadius()
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

            if (dist <= getExplosionRadius()) {
                float falloff = 1.0f - (float) (dist / getExplosionRadius()) * getExplosionDamageFalloff();
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

    // ==================== CHARGING METHODS ====================

    /**
     * Check if entity is in charging state (synced via data watcher).
     * In preview mode, uses local field since data watcher isn't synced.
     */
    public boolean isCharging() {
        if (previewMode) return this.charging;
        return this.dataWatcher.getWatchableObjectByte(DW_CHARGING) == 1;
    }

    /**
     * Set charging state (server only, synced to clients via data watcher).
     */
    protected void setCharging(boolean value) {
        this.charging = value;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_CHARGING, (byte) (value ? 1 : 0));
        }
    }

    public float getChargeProgress() {
        if (chargeDuration <= 0) return 1.0f;
        return Math.min(1.0f, (float) chargeTick / chargeDuration);
    }

    public float getInterpolatedChargeProgress(float partialTicks) {
        if (chargeDuration <= 0) return 1.0f;
        float prevProgress = Math.max(0, (float) (chargeTick - 1) / chargeDuration);
        float currProgress = Math.min(1.0f, (float) chargeTick / chargeDuration);
        return prevProgress + (currProgress - prevProgress) * partialTicks;
    }

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
     *
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

    public Entity getOwnerEntity() {
        // In preview mode, use direct reference (no world lookup)
        if (previewMode && previewOwner != null) {
            return previewOwner;
        }
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    @Override
    public int getOwnerEntityId() {
        return ownerEntityId;
    }

    @Override
    public IEntity getOwner() {
        if (previewMode) return null;
        if (getOwnerEntity() == null) return null;
        return NpcAPI.Instance().getIEntity(getOwnerEntity());
    }

    /**
     * Set the preview owner for GUI preview mode.
     * This allows anchor point calculations without world entity lookup.
     */
    public void setPreviewOwner(EntityLivingBase owner) {
        this.previewOwner = owner;
    }

    @Override
    public int getTargetEntityId() {
        return targetEntityId;
    }

    public Entity getTargetEntity() {
        if (targetEntityId == -1) return null;
        return worldObj.getEntityByID(targetEntityId);
    }

    @Override
    public IEntity getTarget() {
        if (getTargetEntity() == null) return null;
        return NpcAPI.Instance().getIEntity(getTargetEntity());
    }

    /**
     * Check if an entity should be ignored for collision.
     * Ignores: the owner, same-faction NPCs, and other projectiles from the same caster.
     */
    protected boolean shouldIgnoreEntity(Entity entity) {
        Entity owner = getOwnerEntity();
        if (entity == owner) return true;

        if (owner instanceof EntityNPCInterface && entity instanceof EntityNPCInterface) {
            EntityNPCInterface ownerNpc = (EntityNPCInterface) owner;
            EntityNPCInterface targetNpc = (EntityNPCInterface) entity;
            if (ownerNpc.faction.id == targetNpc.faction.id) return true;
        }

        // Ignore other projectiles from the same caster (e.g. multi-projectile abilities)
        if (entity instanceof EntityAbilityProjectile) {
            EntityAbilityProjectile other = (EntityAbilityProjectile) entity;
            if (other.ownerEntityId == this.ownerEntityId) return true;
        }

        return false;
    }

    // ==================== VISUAL GETTERS ====================

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public int getInnerColor() {
        return displayData.getInnerColor();
    }

    @Override
    public int getOuterColor() {
        return displayData.getOuterColor();
    }

    @Override
    public boolean isOuterColorEnabled() {
        return displayData.isOuterColorEnabled();
    }

    @Override
    public float getOuterColorWidth() {
        return displayData.getOuterColorWidth();
    }

    @Override
    public float getOuterColorAlpha() {
        return displayData.getOuterColorAlpha();
    }

    @Override
    public float getRotationSpeed() {
        return displayData.getRotationSpeed();
    }

    // ==================== ROTATION GETTERS ====================

    @Override
    public float getInterpolatedRotationX(float partialTicks) {
        return this.prevRotationValX + (this.rotationValX - this.prevRotationValX) * partialTicks;
    }

    @Override
    public float getInterpolatedRotationY(float partialTicks) {
        return this.prevRotationValY + (this.rotationValY - this.prevRotationValY) * partialTicks;
    }

    @Override
    public float getInterpolatedRotationZ(float partialTicks) {
        return this.prevRotationValZ + (this.rotationValZ - this.prevRotationValZ) * partialTicks;
    }

    @Override
    public float getInterpolatedSize(float partialTicks) {
        return this.prevRenderSize + (this.renderCurrentSize - this.prevRenderSize) * partialTicks;
    }

    // ==================== LIGHTNING GETTERS ====================

    @Override
    public boolean hasLightningEffect() {
        return lightningData.isLightningEffect();
    }

    @Override
    public float getLightningDensity() {
        return lightningData.getLightningDensity();
    }

    @Override
    public float getLightningRadius() {
        return lightningData.getLightningRadius();
    }

    @Override
    public int getLightningFadeTime() {
        return lightningData.getLightningFadeTime();
    }

    // ==================== LIFESPAN GETTERS ====================

    @Override
    public float getMaxDistance() { return lifespanData.getMaxDistance(); }

    @Override
    public int getMaxLifetime() { return lifespanData.getMaxLifetime(); }

    // ==================== COMBAT GETTERS ====================

    @Override
    public float getDamage() { return combatData.getDamage(); }

    @Override
    public float getKnockback() { return combatData.knockback; }

    @Override
    public float getKnockbackUp() { return combatData.knockbackUp; }

    @Override
    public boolean isExplosive() { return combatData.isExplosive(); }

    @Override
    public float getExplosionRadius() { return combatData.explosionRadius; }

    @Override
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }

    // ==================== HOMING GETTERS ====================

    @Override
    public boolean isHoming() { return homingData.isHoming(); }

    @Override
    public float getHomingStrength() { return homingData.getHomingStrength(); }

    @Override
    public float getHomingRange() { return homingData.getHomingRange(); }

    // ==================== TRAJECTORY GETTERS ====================



    // ==================== ANCHOR GETTERS ====================

    @Override
    public float getSpeed() { return homingData.getSpeed(); }

    public AnchorPoint getAnchorPoint() { return anchorData.getAnchorPoint(); }

    @Override
    public int getAnchor() { return anchorData.getAnchor(); }

    @Override
    public float getAnchorOffsetX() { return anchorData.getAnchorOffsetX(); }

    @Override
    public float getAnchorOffsetY() { return anchorData.getAnchorOffsetY(); }

    @Override
    public float getAnchorOffsetZ() { return anchorData.getAnchorOffsetZ(); }

    // ==================== MOVEMENT GETTERS ====================

    @Override public double getStartX() { return startX; }

    @Override public double getStartY() { return startY; }

    @Override public double getStartZ() { return startZ; }

    @Override public double getMotionX() { return motionX; }

    @Override public double getMotionY() { return motionY; }

    @Override public double getMotionZ() { return motionZ; }

    // ==================== BRIGHTNESS ====================

    @Override
    public float getBrightness(float partialTicks) {
        return 1.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        readBaseNBT(nbt);
        readProjectileNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        writeBaseNBT(nbt);
        writeProjectileNBT(nbt);
    }

    /**
     * Read base projectile properties from NBT.
     */
    protected void readBaseNBT(NBTTagCompound nbt) {
        this.size = nbt.getFloat("Size");

        // Read effects list
        this.effects.clear();
        if (nbt.hasKey("Effects")) {
            NBTTagList effectsList = nbt.getTagList("Effects", 10); // 10 = compound tag
            for (int i = 0; i < effectsList.tagCount(); i++) {
                effects.add(AbilityEffect.fromNBT(effectsList.getCompoundTagAt(i)));
            }
        }

        this.deathWorldTime = nbt.hasKey("DeathWorldTime") ? nbt.getLong("DeathWorldTime") : -1;

        this.startX = nbt.getDouble("StartX");
        this.startY = nbt.getDouble("StartY");
        this.startZ = nbt.getDouble("StartZ");

        this.ownerEntityId = nbt.getInteger("OwnerId");
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
        displayData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        lightningData.readNBT(nbt);
        trajectoryData.readNBT(nbt);
    }

    /**
     * Write base projectile properties to NBT.
     */
    protected void writeBaseNBT(NBTTagCompound nbt) {
        nbt.setFloat("Size", size);

        // Write effects list
        if (!effects.isEmpty()) {
            NBTTagList effectsList = new NBTTagList();
            for (AbilityEffect effect : effects) {
                effectsList.appendTag(effect.writeNBT());
            }
            nbt.setTag("Effects", effectsList);
        }

        nbt.setLong("DeathWorldTime", deathWorldTime);

        nbt.setDouble("StartX", startX);
        nbt.setDouble("StartY", startY);
        nbt.setDouble("StartZ", startZ);

        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setInteger("TargetId", targetEntityId);

        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);

        anchorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        displayData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
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

    // ==================== SPAWN DATA ====================

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            this.writeEntityToNBT(compound);
            cpw.mods.fml.common.network.ByteBufUtils.writeTag(buffer, compound);
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error writing ability projectile spawn data", e);
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = cpw.mods.fml.common.network.ByteBufUtils.readTag(buffer);
            if (compound != null) {
                this.readEntityFromNBT(compound);
            }
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error reading ability projectile spawn data", e);
        }
    }

    // ==================== COLLISION SETTINGS ====================

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public float getShadowSize() {
        return 0.0f;
    }
}
