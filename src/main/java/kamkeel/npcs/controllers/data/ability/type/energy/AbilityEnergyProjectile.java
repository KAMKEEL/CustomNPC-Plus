package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.enums.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.enums.HitType;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyTrajectoryData;
import kamkeel.npcs.controllers.data.ability.data.ProjectileData;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.network.packets.data.energycharge.EnergyChargeRemovePacket;
import kamkeel.npcs.network.packets.data.energycharge.EnergyChargeSpawnPacket;
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
 * <p>
 * Subclasses provide entity creation/firing/charging logic and type-specific fields.
 *
 * @param <E> The entity type this ability spawns
 */
public abstract class AbilityEnergyProjectile<E extends EntityEnergyProjectile> extends AbilityEnergy implements IAbilityEnergyProjectile {

    protected static final int MAX_PROJECTILES = 8;

    // Multi-projectile support
    protected int projectileCount = 1;
    protected int fireDelay = 0;

    // Trajectory data
    protected EnergyTrajectoryData trajectoryData;

    // Shared combat/movement data
    protected final EnergyCombatData combatData;
    protected final EnergyHomingData homingData;
    protected final EnergyLifespanData lifespanData;

    // Per-projectile data (anchor + optional color override)
    protected ProjectileData[] projectiles;

    // Transient entity state
    protected transient E[] entities;
    protected transient boolean[] projectileSpawned;
    protected transient int spawnedCount;
    protected transient String[] chargeVisualIds;
    protected transient EntityLivingBase chargeVisualCaster;

    // ==================== CONSTRUCTOR ====================

    protected AbilityEnergyProjectile(
        EnergyDisplayData displayData,
        EnergyCombatData combatData,
        EnergyHomingData homingData,
        EnergyLifespanData lifespanData
    ) {
        super(displayData);
        this.combatData = combatData;
        this.homingData = homingData;
        this.lifespanData = lifespanData;
        this.trajectoryData = new EnergyTrajectoryData();
        this.burstOverlap = true;
        initProjectiles(1);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Create a single entity for the given projectile index.
     * Called when a projectile actually fires.
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
            case 0:
                return AnchorPoint.RIGHT_HAND;
            case 1:
                return AnchorPoint.LEFT_HAND;
            default:
                return AnchorPoint.FRONT;
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

    public int getProjectileCount() {
        return projectileCount;
    }

    public int getFireDelay() {
        return fireDelay;
    }

    public void setFireDelay(int delay) {
        this.fireDelay = Math.max(0, delay);
    }

    /**
     * @deprecated Use {@link #getFireDelay()}
     */
    public int getDualFireDelay() {
        return fireDelay;
    }

    /**
     * @deprecated Use {@link #setFireDelay(int)}
     */
    public void setDualFireDelay(int delay) {
        setFireDelay(delay);
    }

    protected int clampIndex(int index) {
        return Math.max(0, Math.min(index, projectileCount - 1));
    }

    // ==================== ENTITY CREATION ====================

    protected void initRuntimeState(EntityLivingBase caster) {
        entities = createEntityArray(projectileCount);
        projectileSpawned = new boolean[projectileCount];
        spawnedCount = 0;
        chargeVisualIds = new String[projectileCount];
        chargeVisualCaster = caster;
    }

    protected E createProjectileEntity(EntityLivingBase caster, EntityLivingBase target, int index) {
        Vec3 spawnPos = getSpawnPosition(caster, index);
        EnergyDisplayData resolved = projectiles[index].resolveDisplay(displayData);
        E entity = createEntity(caster, target, spawnPos, resolved, index);
        entity.setEffects(this.effects);
        entity.setSourceAbility(this);
        return entity;
    }

    protected void spawnProjectileEntity(E entity, int index) {
        if (entity == null) return;
        spawnAbilityEntity(entity);
        entities[index] = entity;
        if (!projectileSpawned[index]) {
            projectileSpawned[index] = true;
            spawnedCount++;
        }
    }

    protected void spawnAndFireProjectile(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (entities != null && entities[index] != null && !entities[index].isDead) return;
        E entity = createProjectileEntity(caster, target, index);

        // Initialize launch position/motion BEFORE spawn so the
        // initial spawn packet already has centered look-vector data on all clients.
        fireEntitySafe(entity, caster, target);
        spawnProjectileEntity(entity, index);
    }

    /**
     * Hook for launch target selection.
     * Players launch along their look vector (crosshair-aligned), so target is ignored.
     * NPCs use their resolved target so launch math can track the target immediately.
     */
    protected EntityLivingBase getLaunchTarget(EntityLivingBase caster, EntityLivingBase target) {
        return isPlayerCaster(caster) ? null : target;
    }

    /**
     * Fire an entity if it's valid.
     * Clears charging state first so the entity can move and check collisions.
     */
    protected void fireEntitySafe(E entity, EntityLivingBase caster, EntityLivingBase target) {
        if (entity != null && !entity.isDead) {
            if (entity.isCharging()) {
                entity.setCharging(false);
            }
            fireEntity(entity, getLaunchTarget(caster, target));
        }
    }

    protected String getChargeVisualId(EntityLivingBase caster, int index) {
        return "energy_charge:" + caster.getEntityId() + ":" + executionStartTime + ":" + burstIndex + ":" + index;
    }

    protected void spawnChargeVisual(EntityLivingBase caster, EntityLivingBase target, int index, int chargeDuration) {
        if (isPreview() || caster == null || caster.worldObj == null || caster.worldObj.isRemote) return;
        if (chargeVisualIds == null || index < 0 || index >= chargeVisualIds.length) return;

        E previewEntity = createProjectileEntity(caster, target, index);
        previewEntity.setPreviewMode(true);
        previewEntity.setPreviewOwner(caster);
        setupEntityCharging(previewEntity, projectiles[index], index);
        previewEntity.setChargeDuration(Math.max(1, chargeDuration));

        String id = getChargeVisualId(caster, index);
        chargeVisualIds[index] = id;
        chargeVisualCaster = caster;
        EnergyChargeSpawnPacket.sendToTracking(id, previewEntity, caster);
    }

    protected void removeChargeVisual(EntityLivingBase caster, int index) {
        if (isPreview() || caster == null || caster.worldObj == null || caster.worldObj.isRemote) return;
        if (chargeVisualIds == null || index < 0 || index >= chargeVisualIds.length) return;

        String id = chargeVisualIds[index];
        if (id != null && !id.isEmpty()) {
            EnergyChargeRemovePacket.sendToTracking(id, caster);
            chargeVisualIds[index] = null;
        }
    }

    protected void removeAllChargeVisuals(EntityLivingBase caster) {
        if (chargeVisualIds == null || caster == null) return;
        for (int i = 0; i < chargeVisualIds.length; i++) {
            removeChargeVisual(caster, i);
        }
    }

    // ==================== LIFECYCLE ====================

    @Override
    public boolean allowOverlap() {
        return true;
    }

    @Override
    public boolean allowFreeOnCast() {
        return true;
    }

    @Override
    public void detach() {
        entities = null;
        projectileSpawned = null;
        spawnedCount = 0;
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
            initRuntimeState(caster);

            if (isPreview()) {
                for (int i = 0; i < projectileCount; i++) {
                    E previewEntity = createProjectileEntity(caster, target, i);
                    EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
                    setupEntityPreview(previewEntity, caster, resolved, projectiles[i], i);
                    spawnAbilityEntity(previewEntity);
                    entities[i] = previewEntity;
                    projectileSpawned[i] = true;
                    spawnedCount++;
                }
                return;
            }

            for (int i = 0; i < projectileCount; i++) {
                spawnChargeVisual(caster, target, i, windUpTicks);
            }
        }
    }

    @Override
    public void resetForBurst() {
        removeAllChargeVisuals(chargeVisualCaster);

        // Non-overlap: kill old entities before next burst creates new ones
        if (!burstOverlap) {
            cleanup();
        }
        // Clear reference so the next iteration creates fresh entities.
        // For overlap mode, old entities stay alive (tracked in burstEntities).
        entities = null;
        projectileSpawned = null;
        spawnedCount = 0;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        // GUI preview keeps the original non-world preview entity flow.
        if (isPreview()) {
            if (entities == null) {
                initRuntimeState(caster);
                for (int i = 0; i < projectileCount; i++) {
                    E previewEntity = createProjectileEntity(caster, target, i);
                    EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
                    setupEntityPreview(previewEntity, caster, resolved, projectiles[i], i);
                    spawnAbilityEntity(previewEntity);
                    entities[i] = previewEntity;
                    projectileSpawned[i] = true;
                    spawnedCount++;
                }
            }

            fireEntitySafe(entities[0], caster, target);
            if (fireDelay <= 0) {
                for (int i = 1; i < projectileCount; i++) {
                    fireEntitySafe(entities[i], caster, target);
                }
            }
            return;
        }

        if (entities == null || projectileSpawned == null || entities.length != projectileCount) {
            initRuntimeState(caster);
        }

        removeAllChargeVisuals(caster);

        // Fire first projectile immediately.
        spawnAndFireProjectile(caster, target, 0);

        // Fire remaining projectiles immediately if there is no stagger delay.
        if (fireDelay <= 0) {
            for (int i = 1; i < projectileCount; i++) {
                spawnAndFireProjectile(caster, target, i);
            }
        } else {
            // During active stagger, keep client-only charging visuals for unfired projectiles.
            for (int i = 1; i < projectileCount; i++) {
                spawnChargeVisual(caster, target, i, fireDelay * i);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (entities == null || projectileSpawned == null) {
            signalCompletion();
            return;
        }

        // Fire staggered projectiles: spawn real world entity only when it is time to fire.
        if (fireDelay > 0) {
            for (int i = 1; i < projectileCount; i++) {
                if (tick == fireDelay * i) {
                    removeChargeVisual(caster, i);
                    spawnAndFireProjectile(caster, target, i);
                }
            }
        }

        // Free on Cast: complete once all projectiles have been fired
        if (isFreeOnCast()) {
            int lastFireTick = fireDelay > 0 ? fireDelay * (projectileCount - 1) : 0;
            if (tick >= lastFireTick) {
                signalCompletion();
                return;
            }
        }

        // Before all projectiles have fired, the ability is still active by definition.
        boolean allDead = spawnedCount >= projectileCount;
        if (allDead) {
            for (E entity : entities) {
                if (entity == null || entity.isDead) continue;

                if (!isPreview() && tick > 5 && entity.worldObj != null
                    && entity.worldObj.getEntityByID(entity.getEntityId()) != entity) {
                    entity.setDead();
                    continue;
                }

                allDead = false;
                break;
            }
        }

        // Failsafe timeout: force completion if active phase exceeded max possible flight time
        if (!allDead) {
            int lastFireTick = fireDelay > 0 ? fireDelay * (projectileCount - 1) : 0;
            int maxFlightTime = lifespanData.maxLifetime > 0 ? lifespanData.maxLifetime : 200;
            if (tick > lastFireTick + maxFlightTime + 20) {
                cleanup();
                allDead = true;
            }
        }

        if (allDead) {
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        removeAllChargeVisuals(caster);
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        removeAllChargeVisuals(caster);
        cleanup();
    }

    @Override
    public void cleanup() {
        removeAllChargeVisuals(chargeVisualCaster);

        if (entities != null) {
            for (int i = 0; i < entities.length; i++) {
                if (entities[i] != null && !entities[i].isDead) {
                    entities[i].setDead();
                }
                entities[i] = null;
            }
        }
        entities = null;
        projectileSpawned = null;
        spawnedCount = 0;
        chargeVisualIds = null;
        chargeVisualCaster = null;
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
        writeEnergyNBT(nbt);
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
        readEnergyNBT(nbt);
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
    @Override
    public float getDamage() {
        return combatData.damage;
    }

    @Override
    public void setDamage(float damage) {
        combatData.damage = damage;
    }

    @Override
    public float getKnockback() {
        return combatData.knockback;
    }

    @Override
    public void setKnockback(float knockback) {
        combatData.knockback = knockback;
    }

    @Override
    public float getKnockbackUp() {
        return combatData.knockbackUp;
    }

    @Override
    public void setKnockbackUp(float knockbackUp) {
        combatData.knockbackUp = knockbackUp;
    }

    @Override
    public boolean isExplosive() {
        return combatData.explosive;
    }

    @Override
    public void setExplosive(boolean explosive) {
        combatData.explosive = explosive;
    }

    @Override
    public float getExplosionRadius() {
        return combatData.explosionRadius;
    }

    @Override
    public void setExplosionRadius(float explosionRadius) {
        combatData.explosionRadius = explosionRadius;
    }

    @Override
    public float getExplosionDamageFalloff() {
        return combatData.explosionDamageFalloff;
    }

    @Override
    public void setExplosionDamageFalloff(float falloff) {
        combatData.explosionDamageFalloff = falloff;
    }

    public int getHitType() {
        return combatData.hitType.ordinal();
    }

    public void setHitType(int hitType) {
        this.combatData.hitType = HitType.fromOrdinal(hitType);
    }

    public int getMultiHitDelayTicks() {
        return combatData.multiHitDelayTicks;
    }

    public void setMultiHitDelayTicks(int delay) {
        this.combatData.multiHitDelayTicks = Math.max(1, delay);
    }

    // Lifespan data
    @Override
    public float getMaxDistance() {
        return lifespanData.maxDistance;
    }

    @Override
    public void setMaxDistance(float maxDistance) {
        lifespanData.maxDistance = maxDistance;
    }

    @Override
    public int getMaxLifetime() {
        return lifespanData.maxLifetime;
    }

    @Override
    public void setMaxLifetime(int maxLifetime) {
        lifespanData.maxLifetime = maxLifetime;
    }

    // Homing data
    public boolean isHoming() {
        return homingData.homing;
    }

    public void setHoming(boolean homing) {
        homingData.homing = homing;
    }

    public float getHomingStrength() {
        return homingData.homingStrength;
    }

    public void setHomingStrength(float strength) {
        homingData.homingStrength = strength;
    }

    public float getHomingRange() {
        return homingData.homingRange;
    }

    public void setHomingRange(float range) {
        homingData.homingRange = range;
    }

    // Display and lightning data inherited from AbilityEnergy

    // Anchor - default to projectile 0
    public AnchorPoint getAnchorPointEnum() {
        return projectiles[0].anchor.anchorPoint;
    }

    public void setAnchorPointEnum(AnchorPoint point) {
        projectiles[0].anchor.anchorPoint = point;
    }

    public float getAnchorOffsetX() {
        return projectiles[0].anchor.anchorOffsetX;
    }

    public void setAnchorOffsetX(float x) {
        projectiles[0].anchor.anchorOffsetX = x;
    }

    public float getAnchorOffsetY() {
        return projectiles[0].anchor.anchorOffsetY;
    }

    public void setAnchorOffsetY(float y) {
        projectiles[0].anchor.anchorOffsetY = y;
    }

    public float getAnchorOffsetZ() {
        return projectiles[0].anchor.anchorOffsetZ;
    }

    public void setAnchorOffsetZ(float z) {
        projectiles[0].anchor.anchorOffsetZ = z;
    }

    @Override
    public int getAnchorPoint() {
        return projectiles[0].anchor.anchorPoint.ordinal();
    }

    @Override
    public void setAnchorPoint(int point) {
        projectiles[0].anchor.anchorPoint = AnchorPoint.fromOrdinal(point);
    }

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

    public int getAnchorPoint(int index) {
        return projectiles[clampIndex(index)].anchor.anchorPoint.ordinal();
    }

    public void setAnchorPoint(int index, int point) {
        projectiles[clampIndex(index)].anchor.anchorPoint = AnchorPoint.fromOrdinal(point);
    }

    public void setAnchorOffsetX(int index, float offset) {
        projectiles[clampIndex(index)].anchor.anchorOffsetX = offset;
    }

    public void setAnchorOffsetY(int index, float offset) {
        projectiles[clampIndex(index)].anchor.anchorOffsetY = offset;
    }

    public void setAnchorOffsetZ(int index, float offset) {
        projectiles[clampIndex(index)].anchor.anchorOffsetZ = offset;
    }

    // ==================== GUI FIELD DEFINITIONS ====================

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.enumField("ability.hitType", HitType.class,
                () -> HitType.fromOrdinal(getHitType()),
                (v) -> setHitType(v.ordinal()))
            .hover("ability.hover.hitType"));
        defs.add(FieldDef.intField("ability.multiHitDelay", this::getMultiHitDelayTicks, this::setMultiHitDelayTicks)
            .range(1, 200)
            .visibleWhen(() -> getHitType() == HitType.MULTI.ordinal())
            .hover("ability.hover.multiHitDelay"));

        // Type-specific fields first
        addTypeDefinitions(defs);

        // Shared visual tab - colors + effects (from AbilityEnergy)
        addEnergyColorDefinitions(defs);

        // Effects section with projectile-specific rotationSpeed before lightning
        defs.add(FieldDef.section("ability.section.effects").tab("ability.tab.visual"));
        defs.add(FieldDef.floatField("ability.rotationSpeed", this::getRotationSpeed, this::setRotationSpeed)
            .tab("ability.tab.visual"));
        addEnergyLightningDefinitions(defs);

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
                FieldDef.floatField("ability.anchor.offsetX", () -> projectiles[idx].anchor.anchorOffsetX, v -> projectiles[idx].anchor.anchorOffsetX = v)
                    .min(Float.NEGATIVE_INFINITY),
                FieldDef.floatField("ability.anchor.offsetY", () -> projectiles[idx].anchor.anchorOffsetY, v -> projectiles[idx].anchor.anchorOffsetY = v)
                    .min(Float.NEGATIVE_INFINITY)
            ).tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount));
            defs.add(FieldDef.floatField("ability.anchor.offsetZ", () -> projectiles[idx].anchor.anchorOffsetZ, v -> projectiles[idx].anchor.anchorOffsetZ = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount).min(Float.NEGATIVE_INFINITY));

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
