package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityProjectile;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import noppes.npcs.api.ability.type.IAbilityEnergyProjectile;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * Abstract base for energy projectile abilities (Orb, Disc, Beam, LaserShot).
 * Handles shared lifecycle (windup → execute → active → completion), multi-projectile
 * support with staggered firing, telegraph creation, NBT persistence, and GUI definitions.
 *
 * Subclasses provide entity creation/firing/charging logic and type-specific fields.
 *
 * @param <E> The entity type this ability spawns
 */
public abstract class AbstractEnergyProjectileAbility<E extends EntityAbilityProjectile>
    extends Ability implements IAbilityEnergyProjectile {

    protected static final int MAX_PROJECTILES = 8;

    // Multi-projectile support
    protected int projectileCount = 1;
    protected int fireDelay = 0;

    // Shared visual data
    protected EnergyDisplayData displayData;
    protected EnergyLightningData lightningData;
    protected EnergyTrajectoryData trajectoryData;

    // Shared combat/movement data
    protected final EnergyCombatData combatData;
    protected final EnergyHomingData homingData;
    protected final EnergyLifespanData lifespanData;

    // Per-projectile data (anchor + optional color override)
    protected ProjectileData[] projectiles;

    // Transient entity state
    protected transient E[] entities;

    // ==================== CONSTRUCTOR ====================

    protected AbstractEnergyProjectileAbility(
        EnergyDisplayData displayData,
        EnergyCombatData combatData,
        EnergyHomingData homingData,
        EnergyLifespanData lifespanData
    ) {
        this.displayData = displayData;
        this.combatData = combatData;
        this.homingData = homingData;
        this.lifespanData = lifespanData;
        this.lightningData = new EnergyLightningData();
        this.trajectoryData = new EnergyTrajectoryData();
        initProjectiles(1);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Create a single entity for the given projectile index.
     * Called during windup (tick 1) and burst refire (onExecute when entities are null).
     */
    protected abstract E createEntity(EntityLivingBase caster, EntityLivingBase target,
                                       Vec3 spawnPos, EnergyDisplayData resolved, int index);

    /**
     * Fire an entity (start moving toward target). Entity is guaranteed non-null and alive.
     */
    protected abstract void fireEntity(E entity, EntityLivingBase target);

    /**
     * Setup entity for charging animation during windup (non-preview).
     */
    protected abstract void setupEntityCharging(E entity, ProjectileData projData, int index);

    /**
     * Setup entity for preview mode during windup.
     */
    protected abstract void setupEntityPreview(E entity, EntityLivingBase caster,
                                                EnergyDisplayData resolved, ProjectileData projData, int index);

    /**
     * Create typed array (Java generics can't create generic arrays).
     */
    protected abstract E[] createEntityArray(int size);

    /**
     * Get the telegraph radius for this projectile type.
     */
    protected abstract float getProjectileTelegraphRadius();

    /**
     * Add type-specific GUI field definitions (Type tab).
     * Called before shared visual tab definitions.
     */
    @SideOnly(Side.CLIENT)
    protected abstract void addTypeDefinitions(List<FieldDef> defs);

    /**
     * Write type-specific fields to NBT (e.g., orbSize, discRadius).
     * Shared data classes are written by the base class.
     */
    protected abstract void writeTypeSpecificNBT(NBTTagCompound nbt);

    /**
     * Read type-specific fields from NBT.
     * Shared data classes are read by the base class.
     */
    protected abstract void readTypeSpecificNBT(NBTTagCompound nbt);

    // ==================== OVERRIDABLE HOOKS ====================

    /**
     * Get the default anchor point for a projectile index.
     * Override for types with different anchor defaults.
     */
    protected AnchorPoint getDefaultAnchor(int index) {
        switch (index) {
            case 0: return AnchorPoint.RIGHT_HAND;
            case 1: return AnchorPoint.LEFT_HAND;
            default: return AnchorPoint.FRONT;
        }
    }

    /**
     * Get the spawn position for a projectile. Override for types that need
     * an offset distance (e.g., Beam uses offsetDist=1.0).
     */
    protected Vec3 getSpawnPosition(EntityLivingBase caster, int index) {
        return AnchorPointHelper.calculateAnchorPosition(caster, projectiles[index].anchor);
    }

    // ==================== PROJECTILE MANAGEMENT ====================

    protected void initProjectiles(int count) {
        this.projectileCount = Math.max(1, Math.min(count, MAX_PROJECTILES));
        this.projectiles = new ProjectileData[projectileCount];
        for (int i = 0; i < projectileCount; i++) {
            projectiles[i] = new ProjectileData(getDefaultAnchor(i));
        }
    }

    public void setProjectileCount(int count) {
        int newCount = Math.max(1, Math.min(count, MAX_PROJECTILES));
        if (newCount == projectileCount) return;

        ProjectileData[] oldProjectiles = projectiles;
        initProjectiles(newCount);

        if (oldProjectiles != null) {
            int copyCount = Math.min(oldProjectiles.length, newCount);
            for (int i = 0; i < copyCount; i++) {
                if (oldProjectiles[i] != null) projectiles[i] = oldProjectiles[i];
            }
        }
    }

    public int getProjectileCount() { return projectileCount; }

    public int getFireDelay() { return fireDelay; }
    public void setFireDelay(int delay) { this.fireDelay = Math.max(0, delay); }

    /** @deprecated Use {@link #getFireDelay()} */
    public int getDualFireDelay() { return fireDelay; }
    /** @deprecated Use {@link #setFireDelay(int)} */
    public void setDualFireDelay(int delay) { setFireDelay(delay); }

    protected int clampIndex(int index) {
        return Math.max(0, Math.min(index, projectileCount - 1));
    }

    // ==================== ENTITY CREATION ====================

    /**
     * Create all entities for this ability.
     * Sets effects, source ability, and sibling links.
     */
    protected E[] createAllEntities(EntityLivingBase caster, EntityLivingBase target) {
        E[] newEntities = createEntityArray(projectileCount);
        for (int i = 0; i < projectileCount; i++) {
            Vec3 spawnPos = getSpawnPosition(caster, i);
            EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
            newEntities[i] = createEntity(caster, target, spawnPos, resolved, i);
            newEntities[i].setEffects(this.effects);
            newEntities[i].setSourceAbility(this);
        }
        return newEntities;
    }

    /**
     * Fire an entity if it's valid.
     */
    protected void fireEntitySafe(E entity, EntityLivingBase target) {
        if (entity != null && !entity.isDead) {
            fireEntity(entity, target);
        }
    }

    // ==================== LIFECYCLE ====================

    @Override
    public boolean allowOverlap() {
        return true;
    }

    @Override
    public boolean isReadyForBurstCompletion(int activeTick) {
        return fireDelay <= 0 || activeTick >= fireDelay * (projectileCount - 1);
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (caster.worldObj.isRemote && !isPreview()) return;

        if (tick == 1) {
            entities = createAllEntities(caster, target);
            for (int i = 0; i < projectileCount; i++) {
                EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
                if (isPreview()) {
                    setupEntityPreview(entities[i], caster, resolved, projectiles[i], i);
                } else {
                    setupEntityCharging(entities[i], projectiles[i], i);
                }
                spawnAbilityEntity(entities[i]);
            }
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        // Burst refire: entities not created during windup (it was skipped)
        if (entities == null) {
            entities = createAllEntities(caster, target);
            for (E entity : entities) {
                spawnAbilityEntity(entity);
            }
        }

        // Fire first projectile immediately
        fireEntitySafe(entities[0], target);

        // Fire remaining projectiles if no delay
        if (fireDelay <= 0) {
            for (int i = 1; i < projectileCount; i++) {
                fireEntitySafe(entities[i], target);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (entities == null) {
            signalCompletion();
            return;
        }

        // Fire staggered projectiles
        if (fireDelay > 0) {
            for (int i = 1; i < projectileCount; i++) {
                if (tick == fireDelay * i) {
                    fireEntitySafe(entities[i], target);
                }
            }
        }

        // Check if all entities are dead
        boolean allDead = true;
        for (E entity : entities) {
            if (entity != null && !entity.isDead) {
                allDead = false;
                break;
            }
        }
        if (allDead) {
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        cleanup();
    }

    @Override
    public void cleanup() {
        if (entities != null) {
            for (int i = 0; i < entities.length; i++) {
                if (entities[i] != null && !entities[i].isDead) {
                    entities[i].setDead();
                }
                entities[i] = null;
            }
        }
        entities = null;
    }

    // ==================== TELEGRAPH ====================

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster) || target == null) {
            return null;
        }

        Telegraph telegraph = Telegraph.circle(getProjectileTelegraphRadius());
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, target.posY, target.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(target.getEntityId());

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return getProjectileTelegraphRadius();
    }

    // ==================== NBT ====================

    @Override
    public final void writeTypeNBT(NBTTagCompound nbt) {
        // Type-specific fields
        writeTypeSpecificNBT(nbt);

        // Shared fields
        nbt.setInteger("projectileCount", projectileCount);
        nbt.setInteger("fireDelay", fireDelay);

        // Shared data classes
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);

        // Per-projectile data
        for (int i = 0; i < projectileCount; i++) {
            NBTTagCompound projNbt = new NBTTagCompound();
            projectiles[i].writeNBT(projNbt);
            nbt.setTag("Projectile_" + i, projNbt);
        }
    }

    @Override
    public final void readTypeNBT(NBTTagCompound nbt) {
        // Type-specific fields
        readTypeSpecificNBT(nbt);

        // Shared fields
        int count = nbt.getInteger("projectileCount");
        initProjectiles(count);
        this.fireDelay = nbt.getInteger("fireDelay");

        // Shared data classes
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);

        // Per-projectile data
        for (int i = 0; i < projectileCount; i++) {
            if (nbt.hasKey("Projectile_" + i)) {
                projectiles[i].readNBT(nbt.getCompoundTag("Projectile_" + i));
            }
        }
    }

    // ==================== PREVIEW ====================

    @Override
    public int getMaxPreviewDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }

    // ==================== API GETTERS & SETTERS ====================

    // Combat data
    @Override public float getDamage() { return combatData.damage; }
    @Override public void setDamage(float damage) { combatData.damage = damage; }

    @Override public float getKnockback() { return combatData.knockback; }
    @Override public void setKnockback(float knockback) { combatData.knockback = knockback; }

    @Override public float getKnockbackUp() { return combatData.knockbackUp; }
    @Override public void setKnockbackUp(float knockbackUp) { combatData.knockbackUp = knockbackUp; }

    @Override public boolean isExplosive() { return combatData.explosive; }
    @Override public void setExplosive(boolean explosive) { combatData.explosive = explosive; }

    @Override public float getExplosionRadius() { return combatData.explosionRadius; }
    @Override public void setExplosionRadius(float explosionRadius) { combatData.explosionRadius = explosionRadius; }

    @Override public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    @Override public void setExplosionDamageFalloff(float falloff) { combatData.explosionDamageFalloff = falloff; }

    // Lifespan data
    @Override public float getMaxDistance() { return lifespanData.maxDistance; }
    @Override public void setMaxDistance(float maxDistance) { lifespanData.maxDistance = maxDistance; }

    @Override public int getMaxLifetime() { return lifespanData.maxLifetime; }
    @Override public void setMaxLifetime(int maxLifetime) { lifespanData.maxLifetime = maxLifetime; }

    // Homing data
    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean homing) { homingData.homing = homing; }

    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float strength) { homingData.homingStrength = strength; }

    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float range) { homingData.homingRange = range; }

    // Display data - primary colors
    @Override public int getInnerColor() { return displayData.innerColor; }
    @Override public void setInnerColor(int color) { displayData.innerColor = color; }

    @Override public int getOuterColor() { return displayData.outerColor; }
    @Override public void setOuterColor(int color) { displayData.outerColor = color; }

    @Override public boolean isOuterColorEnabled() { return displayData.outerColorEnabled; }
    @Override public void setOuterColorEnabled(boolean enabled) { displayData.outerColorEnabled = enabled; }

    @Override public float getOuterColorWidth() { return displayData.outerColorWidth; }
    @Override public void setOuterColorWidth(float width) { displayData.outerColorWidth = width; }

    @Override public float getOuterColorAlpha() { return displayData.outerColorAlpha; }
    @Override public void setOuterColorAlpha(float alpha) { displayData.outerColorAlpha = alpha; }

    public float getRotationSpeed() { return displayData.rotationSpeed; }
    public void setRotationSpeed(float speed) { displayData.rotationSpeed = speed; }

    // Lightning data
    @Override public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    @Override public void setLightningEffect(boolean enabled) { lightningData.lightningEffect = enabled; }

    @Override public float getLightningDensity() { return lightningData.lightningDensity; }
    @Override public void setLightningDensity(float density) { lightningData.lightningDensity = density; }

    @Override public float getLightningRadius() { return lightningData.lightningRadius; }
    @Override public void setLightningRadius(float radius) { lightningData.lightningRadius = radius; }

    public int getLightningFadeTime() { return lightningData.lightningFadeTime; }
    public void setLightningFadeTime(int fadeTime) { lightningData.lightningFadeTime = fadeTime; }

    // Anchor - default to projectile 0
    public AnchorPoint getAnchorPointEnum() { return projectiles[0].anchor.anchorPoint; }
    public void setAnchorPointEnum(AnchorPoint point) { projectiles[0].anchor.anchorPoint = point; }

    public float getAnchorOffsetX() { return projectiles[0].anchor.anchorOffsetX; }
    public void setAnchorOffsetX(float x) { projectiles[0].anchor.anchorOffsetX = x; }

    public float getAnchorOffsetY() { return projectiles[0].anchor.anchorOffsetY; }
    public void setAnchorOffsetY(float y) { projectiles[0].anchor.anchorOffsetY = y; }

    public float getAnchorOffsetZ() { return projectiles[0].anchor.anchorOffsetZ; }
    public void setAnchorOffsetZ(float z) { projectiles[0].anchor.anchorOffsetZ = z; }

    @Override public int getAnchorPoint() { return projectiles[0].anchor.anchorPoint.ordinal(); }
    @Override public void setAnchorPoint(int point) { projectiles[0].anchor.anchorPoint = AnchorPoint.fromOrdinal(point); }

    // Indexed API methods
    public int getInnerColor(int index) {
        ProjectileData p = projectiles[clampIndex(index)];
        return p.colorOverride ? p.innerColor : displayData.innerColor;
    }
    public void setInnerColor(int index, int color) {
        ProjectileData p = projectiles[clampIndex(index)];
        p.colorOverride = true;
        p.innerColor = color;
    }

    public int getOuterColor(int index) {
        ProjectileData p = projectiles[clampIndex(index)];
        return p.colorOverride ? p.outerColor : displayData.outerColor;
    }
    public void setOuterColor(int index, int color) {
        ProjectileData p = projectiles[clampIndex(index)];
        p.colorOverride = true;
        p.outerColor = color;
    }

    public int getAnchorPoint(int index) { return projectiles[clampIndex(index)].anchor.anchorPoint.ordinal(); }
    public void setAnchorPoint(int index, int point) { projectiles[clampIndex(index)].anchor.anchorPoint = AnchorPoint.fromOrdinal(point); }
    public void setAnchorOffsetX(int index, float offset) { projectiles[clampIndex(index)].anchor.anchorOffsetX = offset; }
    public void setAnchorOffsetY(int index, float offset) { projectiles[clampIndex(index)].anchor.anchorOffsetY = offset; }
    public void setAnchorOffsetZ(int index, float offset) { projectiles[clampIndex(index)].anchor.anchorOffsetZ = offset; }

    // ==================== GUI FIELD DEFINITIONS ====================

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        // Type-specific fields first
        addTypeDefinitions(defs);

        // Shared visual tab - primary colors
        defs.add(FieldDef.section("ability.section.colors").tab("ability.tab.visual"));
        defs.add(FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor)
            .tab("ability.tab.visual"));
        defs.add(FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled)
            .tab("ability.tab.visual"));
        defs.add(FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
            .tab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.outerWidth", this::getOuterColorWidth, this::setOuterColorWidth)
                .visibleWhen(this::isOuterColorEnabled),
            FieldDef.floatField("ability.outerAlpha", this::getOuterColorAlpha, this::setOuterColorAlpha)
                .range(0, 1).visibleWhen(this::isOuterColorEnabled)
        ).tab("ability.tab.visual"));

        // Shared visual tab - effects
        defs.add(FieldDef.section("ability.section.effects").tab("ability.tab.visual"));
        defs.add(FieldDef.floatField("ability.rotationSpeed", this::getRotationSpeed, this::setRotationSpeed)
            .tab("ability.tab.visual"));
        defs.add(FieldDef.boolField("ability.lightning", this::hasLightningEffect, this::setLightningEffect)
            .tab("ability.tab.visual"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
                .visibleWhen(this::hasLightningEffect).range(0.01f, 100f),
            FieldDef.floatField("gui.radius", this::getLightningRadius, this::setLightningRadius)
                .range(0.1f, 100f).visibleWhen(this::hasLightningEffect)
        ).tab("ability.tab.visual"));

        // Shared visual tab - per-projectile sections
        for (int i = 0; i < MAX_PROJECTILES; i++) {
            final int idx = i;
            String sectionKey = "ability.section.projectile" + (i + 1);

            defs.add(FieldDef.section(sectionKey).tab("ability.tab.visual")
                .visibleWhen(() -> idx < projectileCount));

            defs.add(FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> projectiles[idx].anchor.anchorPoint, v -> projectiles[idx].anchor.anchorPoint = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount));
            defs.add(FieldDef.row(
                FieldDef.floatField("ability.anchor.offsetX", () -> projectiles[idx].anchor.anchorOffsetX, v -> projectiles[idx].anchor.anchorOffsetX = v),
                FieldDef.floatField("ability.anchor.offsetY", () -> projectiles[idx].anchor.anchorOffsetY, v -> projectiles[idx].anchor.anchorOffsetY = v)
            ).tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount));
            defs.add(FieldDef.floatField("ability.anchor.offsetZ", () -> projectiles[idx].anchor.anchorOffsetZ, v -> projectiles[idx].anchor.anchorOffsetZ = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount));

            // Color override (only when multiple projectiles)
            defs.add(FieldDef.boolField("ability.colorOverride", () -> projectiles[idx].colorOverride, v -> projectiles[idx].colorOverride = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount && projectileCount > 1));
            defs.add(FieldDef.colorSubGui("ability.innerColor", () -> projectiles[idx].innerColor, v -> projectiles[idx].innerColor = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount && projectileCount > 1 && projectiles[idx].colorOverride));
            defs.add(FieldDef.colorSubGui("ability.outerColor", () -> projectiles[idx].outerColor, v -> projectiles[idx].outerColor = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount && projectileCount > 1 && projectiles[idx].colorOverride));
        }
    }
}
