package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.EnergyController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.effect.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.enums.HitType;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.util.AttributeAttackUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for all energy zone entities.
 * Extends EntityEnergyAbility for shared visual/owner/charging/magic state.
 *
 * Provides:
 * - Lifetime management via deathWorldTime (robust to chunk unload)
 * - Combat pipeline: sourceAbility -> customDamageData -> native fallback
 * - Hit type tracking: SINGLE, MULTI, PIERCE
 * - Effects application
 * - affectsCaster flag
 * - Charging infrastructure (hook only — subclass defines growth behavior)
 * - Size interpolation: radius/height with target/render/prev layers
 *
 * Subclasses must implement:
 * - updateZone()         — tick logic (damage, movement, etc.)
 * - writeZoneNBT()       — type-specific NBT write
 * - readZoneNBT()        — type-specific NBT read
 *
 * Subclasses may override:
 * - updateCharging()     — charging growth behavior (default: increments chargeTick only)
 * - shouldIgnoreEntity() — damage/collision filter
 */
public abstract class EntityEnergyZone extends EntityEnergyAbility {

    // ==================== SAFETY CONSTANTS ====================

    protected static final float MAX_ZONE_RADIUS = 64.0f;
    protected static final float MAX_ZONE_HEIGHT = 64.0f;
    protected static final double GROUND_OFFSET = 0.05;

    // ==================== LIFESPAN ====================

    protected EnergyLifespanData lifespanData = new EnergyLifespanData();
    protected long deathWorldTime = -1;

    // ==================== COMBAT ====================

    protected EnergyCombatData combatData = new EnergyCombatData();
    protected List<AbilityPotionEffect> effects = new ArrayList<>();
    protected boolean affectsCaster = false;

    // ==================== HIT TRACKING ====================

    /** Entities hit exactly once — PIERCE: never again; SINGLE: zone dies on first hit. */
    protected final Set<Integer> hitOnceEntities = new HashSet<>();

    /** Last tick each entity was hit, used for MULTI delay. */
    protected final Map<Integer, Integer> lastHitTickByEntity = new HashMap<>();

    protected int hitCount = 0;

    // ==================== SIZE — LOGICAL ====================

    /** Current radius used for collision and damage checks. */
    protected float radius = 1.0f;

    /** Current height used for collision and damage checks. */
    protected float height = 1.0f;

    // ==================== SIZE — INTERPOLATION ====================

    /** Target radius the zone is growing or shrinking toward. */
    protected float targetRadius = 1.0f;

    /** Target height the zone is growing or shrinking toward. */
    protected float targetHeight = 1.0f;

    /** Smoothed radius sent to the renderer each frame. */
    protected float renderRadius = 1.0f;

    /** Smoothed height sent to the renderer each frame. */
    protected float renderHeight = 1.0f;

    /** Previous-tick render radius for partialTicks interpolation. */
    protected float prevRenderRadius = 1.0f;

    /** Previous-tick render height for partialTicks interpolation. */
    protected float prevRenderHeight = 1.0f;

    // ==================== CONSTRUCTOR ====================

    public EntityEnergyZone(World world) {
        super(world);
        this.setSize(0.1f, 0.1f);
        this.noClip = true;
    }

    // ==================== ENTITY LIFECYCLE ====================

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRenderRadius = this.renderRadius;
        this.prevRenderHeight = this.renderHeight;

        if (!previewMode) {
            super.onUpdate();
        } else {
            this.ticksExisted++;

            if (isCharging() && chargeDuration > 0 && chargeTick > chargeDuration + PREVIEW_CHARGE_GRACE) {
                this.setDead();
                return;
            }
            if (ticksExisted > HARD_LIFETIME_CAP) {
                this.setDead();
                return;
            }
        }

        // During charging, snap render values so they follow subclass growth without lag.
        // After charging, lerp smoothly toward current logical size.
        if (isCharging()) {
            this.renderRadius = this.radius;
            this.renderHeight = this.height;
            updateCharging();
            return;
        } else {
            this.renderRadius += (this.radius - this.renderRadius) * 0.15f;
            this.renderHeight += (this.height - this.renderHeight) * 0.15f;
        }

        if (!previewMode) {
            if (deathWorldTime < 0 && worldObj != null) {
                deathWorldTime = worldObj.getTotalWorldTime() + lifespanData.getMaxLifetime();
            }

            if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
                this.setDead();
                return;
            }

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
        }

        updateZone();
    }

    // ==================== HOOKS ====================

    /**
     * Subclass tick logic — damage, movement, custom behavior.
     * Called every tick after charging ends and lifetime is validated.
     */
    protected abstract void updateZone();

    /**
     * Charging growth hook. Called every tick while isCharging() is true.
     * Default implementation increments chargeTick only.
     * Override to define what dimensions grow during the windup phase.
     */
    protected void updateCharging() {
        chargeTick++;
    }

    // ==================== CHARGING SETUP ====================

    /**
     * Initialize charging state. Subclasses call this and then set their own target values.
     * Does not zero radius or height — subclass decides starting values.
     */
    public void setupCharging(int chargeDuration) {
        setCharging(true);
        this.chargeDuration = chargeDuration;
        this.chargeTick = 0;
    }

    // ==================== DAMAGE PIPELINE ====================

    /**
     * Apply damage to a target through the full energy pipeline:
     * sourceAbility extender -> customDamageData -> native damage source.
     *
     * @param target entity to damage
     * @param damage raw damage amount
     * @return true if damage was applied
     */
    protected boolean applyDamage(EntityLivingBase target, float damage) {
        if (previewMode) return false;
        if (target == null) return false;
        if (damage <= 0 && sourceAbility == null && customDamageData == null) return false;

        Entity owner = getOwnerEntity();
        int previousHurtResistantTime = Ability.clearHurtResistanceIfNeeded(target, ignoreIFrames);
        try {
            boolean handled = false;

            if (sourceAbility != null && owner instanceof EntityLivingBase) {
                double dx = target.posX - posX;
                double dz = target.posZ - posZ;
                handled = AbilityController.Instance.fireOnAbilityDamage(
                    sourceAbility, (EntityLivingBase) owner, target,
                    damage, 0.0f, 0.0f, dx, dz, 1.0f);
            }

            if (!handled && customDamageData != null && owner instanceof EntityLivingBase) {
                double dx = target.posX - posX;
                double dz = target.posZ - posZ;
                handled = EnergyController.Instance.fireOnEnergyDamage(
                    this, (EntityLivingBase) owner, target,
                    damage, 0.0f, 0.0f, dx, dz, 1.0f, customDamageData);
            }

            if (!handled) {
                float finalDamage = damage;

                if (magicData != null && !magicData.isEmpty() && owner instanceof EntityLivingBase) {
                    finalDamage = AttributeAttackUtil.calculateAbilityDamage(
                        (EntityLivingBase) owner, target, damage, magicData);
                }

                if (owner instanceof EntityNPCInterface) {
                    return target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), finalDamage);
                } else if (owner instanceof EntityPlayer) {
                    return target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), finalDamage);
                } else if (owner instanceof EntityLivingBase) {
                    return target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), finalDamage);
                } else {
                    return target.attackEntityFrom(new NpcDamageSource("npc_ability", null), finalDamage);
                }
            }
            return true;

        } finally {
            Ability.restoreHurtResistanceIfNeeded(target, ignoreIFrames, previousHurtResistantTime);
        }
    }

    /**
     * Apply potion effects to a target.
     */
    protected void applyEffects(EntityLivingBase target) {
        if (target == null || effects.isEmpty()) return;
        for (AbilityPotionEffect effect : effects) {
            effect.apply(target);
        }
    }

    // ==================== HIT TYPE LOGIC ====================

    /**
     * Whether this zone can hit the given entity right now, based on hit type.
     */
    protected boolean canHitEntityNow(EntityLivingBase entity) {
        if (entity == null) return false;
        int entityId = entity.getEntityId();
        switch (combatData.hitType) {
            case SINGLE:
            case PIERCE:
                return !hitOnceEntities.contains(entityId);
            case MULTI:
                Integer lastTick = lastHitTickByEntity.get(entityId);
                if (lastTick == null) return true;
                return (ticksExisted - lastTick) >= Math.max(1, combatData.multiHitDelayTicks);
            default:
                return true;
        }
    }

    /**
     * Record that an entity was hit this tick.
     */
    protected void recordEntityHit(EntityLivingBase entity) {
        if (entity == null) return;
        int entityId = entity.getEntityId();
        hitOnceEntities.add(entityId);
        lastHitTickByEntity.put(entityId, ticksExisted);
        hitCount++;
    }

    /**
     * Whether the zone should die after a successful hit (SINGLE only).
     */
    protected boolean shouldTerminateAfterHit() {
        return combatData.hitType == HitType.SINGLE;
    }

    // ==================== ENTITY FILTER ====================

    /**
     * Whether an entity should be ignored for damage and collision.
     * Subclasses may override to add type-specific filters.
     */
    protected boolean shouldIgnoreEntity(EntityLivingBase entity) {
        if (entity == null) return true;
        Entity owner = getOwnerEntity();
        if (entity == owner && !affectsCaster) return true;
        if (owner instanceof EntityLivingBase) {
            return !AbilityTargetHelper.shouldAffect((EntityLivingBase) owner, entity, TargetFilter.ENEMIES, affectsCaster);
        }
        return false;
    }

    // ==================== SIZE INTERPOLATION ====================

    public float getInterpolatedRadius(float partialTicks) {
        return prevRenderRadius + (renderRadius - prevRenderRadius) * partialTicks;
    }

    public float getInterpolatedHeight(float partialTicks) {
        return prevRenderHeight + (renderHeight - prevRenderHeight) * partialTicks;
    }

    /**
     * Snap all radius interpolation layers to a single value.
     * Use on init or after teleport to avoid pop artifacts.
     */
    protected void setVisualRadius(float value) {
        this.radius = value;
        this.renderRadius = value;
        this.prevRenderRadius = value;
    }

    /**
     * Snap all height interpolation layers to a single value.
     */
    protected void setVisualHeight(float value) {
        this.height = value;
        this.renderHeight = value;
        this.prevRenderHeight = value;
    }

    // ==================== GROUND SNAP ====================

    /**
     * Position this zone at ground level beneath x/y/z.
     */
    protected void snapToGround(double x, double y, double z) {
        double groundY = Ability.findGroundLevel(worldObj, x, y, z) + GROUND_OFFSET;
        this.setPosition(x, groundY, z);
    }

    // ==================== PREVIEW ====================

    /**
     * Set up this zone for GUI preview rendering.
     */
    public void setupPreview(EntityLivingBase owner) {
        this.previewMode = true;
        this.previewOwner = owner;
    }

    // ==================== GETTERS & SETTERS ====================

    public float getDamage() { return combatData.getDamage(); }
    public void setDamage(float damage) { combatData.setDamage(damage); }

    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { combatData.setKnockback(knockback); }

    public int getHitType() { return combatData.hitType.ordinal(); }
    public void setHitType(int hitType) { combatData.hitType = HitType.fromOrdinal(hitType); }

    public int getMultiHitDelayTicks() { return combatData.multiHitDelayTicks; }
    public void setMultiHitDelayTicks(int ticks) { combatData.multiHitDelayTicks = Math.max(1, ticks); }

    public boolean isAffectsCaster() { return affectsCaster; }
    public void setAffectsCaster(boolean affectsCaster) { this.affectsCaster = affectsCaster; }

    public int getMaxLifetime() { return lifespanData.getMaxLifetime(); }
    public void setMaxLifetime(int ticks) { lifespanData.setMaxLifetime(ticks); }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = sanitize(radius, 1.0f, MAX_ZONE_RADIUS); }

    public float getZoneHeight() { return height; }
    public void setZoneHeight(float height) { this.height = sanitize(height, 1.0f, MAX_ZONE_HEIGHT); }

    public float getTargetRadius() { return targetRadius; }
    public void setTargetRadius(float radius) { this.targetRadius = sanitize(radius, 1.0f, MAX_ZONE_RADIUS); }

    public float getTargetHeight() { return targetHeight; }
    public void setTargetHeight(float height) { this.targetHeight = sanitize(height, 1.0f, MAX_ZONE_HEIGHT); }

    public void setEffects(List<AbilityPotionEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            this.effects = new ArrayList<>();
        } else {
            this.effects = new ArrayList<>(effects.size());
            for (AbilityPotionEffect effect : effects) {
                this.effects.add(effect.copy());
            }
        }
    }

    // ==================== NBT ====================

    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
        writeZoneBaseNBT(nbt);
        writeZoneNBT(nbt);
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
        readZoneBaseNBT(nbt);
        readZoneNBT(nbt);
    }

    protected void writeZoneBaseNBT(NBTTagCompound nbt) {
        nbt.setFloat("Radius", radius);
        nbt.setFloat("Height", height);
        nbt.setFloat("TargetRadius", targetRadius);
        nbt.setFloat("TargetHeight", targetHeight);

        lifespanData.writeNBT(nbt);
        nbt.setLong("DeathWorldTime", deathWorldTime);

        combatData.writeNBT(nbt);
        nbt.setBoolean("AffectsCaster", affectsCaster);

        nbt.setBoolean("Charging", isCharging());
        nbt.setInteger("ChargeDuration", chargeDuration);
        nbt.setInteger("ChargeTick", chargeTick);

        NBTTagList effectsList = new NBTTagList();
        for (AbilityPotionEffect effect : effects) {
            effectsList.appendTag(effect.writeNBT());
        }
        nbt.setTag("Effects", effectsList);
    }

    protected void readZoneBaseNBT(NBTTagCompound nbt) {
        this.radius = sanitize(nbt.getFloat("Radius"), 1.0f, MAX_ZONE_RADIUS);
        this.height = sanitize(nbt.getFloat("Height"), 1.0f, MAX_ZONE_HEIGHT);
        this.targetRadius = sanitize(nbt.getFloat("TargetRadius"), 1.0f, MAX_ZONE_RADIUS);
        this.targetHeight = sanitize(nbt.getFloat("TargetHeight"), 1.0f, MAX_ZONE_HEIGHT);

        this.renderRadius = this.radius;
        this.prevRenderRadius = this.radius;
        this.renderHeight = this.height;
        this.prevRenderHeight = this.height;

        lifespanData.readNBT(nbt);
        this.deathWorldTime = nbt.hasKey("DeathWorldTime") ? nbt.getLong("DeathWorldTime") : -1;

        combatData.readNBT(nbt);
        this.affectsCaster = nbt.getBoolean("AffectsCaster");

        boolean isChargingVal = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.charging = isChargingVal;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_CHARGING, (byte) (isChargingVal ? 1 : 0));
        }
        this.chargeDuration = nbt.hasKey("ChargeDuration") ? nbt.getInteger("ChargeDuration") : 0;
        this.chargeTick = nbt.hasKey("ChargeTick") ? nbt.getInteger("ChargeTick") : 0;

        this.effects.clear();
        if (nbt.hasKey("Effects")) {
            NBTTagList effectsList = nbt.getTagList("Effects", 10);
            for (int i = 0; i < effectsList.tagCount(); i++) {
                AbilityPotionEffect effect = AbilityPotionEffect.fromNBT(effectsList.getCompoundTagAt(i));
                if (effect != null && effect.isValid()) {
                    this.effects.add(effect);
                }
            }
        }
    }

    /**
     * Subclass-specific NBT write. Called after writeZoneBaseNBT.
     */
    protected abstract void writeZoneNBT(NBTTagCompound nbt);

    /**
     * Subclass-specific NBT read. Called after readZoneBaseNBT.
     */
    protected abstract void readZoneNBT(NBTTagCompound nbt);
}
