package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.enums.HitType;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.energycharge.EnergyChargeTracker;
import kamkeel.npcs.entity.EntityEnergyAbility;
import kamkeel.npcs.entity.EntityEnergyZone;
import kamkeel.npcs.network.packets.data.energycharge.EnergyChargeRemovePacket;
import kamkeel.npcs.network.packets.data.energycharge.EnergyChargeSpawnPacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for all energy zone abilities (Pillar, etc.).
 * Extends AbilityEnergy for shared visual/lightning data.
 *
 * Handles:
 * - Multi-zone support with staggered spawning via fireDelay
 * - Charge visual lifecycle via EnergyChargeSpawnPacket + EnergyChargeTracker
 * - Windup -> execute -> active -> complete lifecycle
 * - Telegraph infrastructure (subclass defines shape and radius)
 * - NBT persistence for shared fields
 *
 * Subclasses must implement:
 * - createEntity()           — instantiate the zone entity
 * - setupEntityCharging()    — configure entity for charge visual
 * - setupEntityPreview()     — configure entity for GUI preview
 * - createEntityArray()      — typed array creation (generics limitation)
 * - getZoneTelegraphRadius() — telegraph size
 * - addTypeDefinitions()     — type-specific GUI fields
 * - writeTypeSpecificNBT()
 * - readTypeSpecificNBT()
 *
 * @param <E> The zone entity type this ability spawns
 */
public abstract class AbilityEnergyZone<E extends EntityEnergyZone> extends AbilityEnergy {

    protected static final int MAX_ZONES = 8;

    // ==================== ZONE COUNT & STAGGER ====================

    protected int zoneCount = 1;
    protected int fireDelay = 0;

    // ==================== SHARED DATA ====================

    protected final EnergyCombatData combatData;
    protected final EnergyLifespanData lifespanData;

    // ==================== RUNTIME STATE ====================

    protected transient E[] entities;
    protected transient boolean[] zoneSpawned;
    protected transient int spawnedCount;
    protected transient String[] chargeVisualIds;
    protected transient EntityLivingBase chargeVisualCaster;

    protected transient List<double[]> preCalculatedPositions = new ArrayList<>();

    // ==================== CONSTRUCTOR ====================

    protected AbilityEnergyZone(EnergyDisplayData displayData,
                                EnergyCombatData combatData,
                                EnergyLifespanData lifespanData) {
        super(displayData);
        this.combatData = combatData;
        this.lifespanData = lifespanData;
        this.burstOverlap = true;
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Instantiate a single zone entity at the given position.
     * Called when a zone actually spawns.
     */
    protected abstract E createEntity(EntityLivingBase caster, EntityLivingBase target,
                                      double x, double y, double z,
                                      EnergyDisplayData resolved, int index);

    /**
     * Configure entity for charge visual during windup (non-preview).
     */
    protected abstract void setupEntityCharging(E entity, int index);

    /**
     * Configure entity for GUI preview mode.
     */
    protected abstract void setupEntityPreview(E entity, EntityLivingBase caster,
                                               EnergyDisplayData resolved, int index);

    /**
     * Create typed array. Required because Java generics cannot create generic arrays.
     */
    protected abstract E[] createEntityArray(int size);

    /**
     * Telegraph radius for this zone type.
     */
    protected abstract float getZoneTelegraphRadius();

    /**
     * Add type-specific GUI field definitions.
     */
    @SideOnly(Side.CLIENT)
    protected abstract void addTypeDefinitions(List<FieldDef> defs);

    /**
     * Write type-specific fields to NBT.
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
     * Resolve the spawn position for zone index.
     * Default uses preCalculatedPositions when available, falls back to target position.
     * Subclasses override for type-specific positioning (e.g. Pillar MOVING mode).
     */
    protected double[] getSpawnPosition(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (index < preCalculatedPositions.size()) {
            return preCalculatedPositions.get(index);
        }
        if (target != null) {
            return new double[]{target.posX, target.posY, target.posZ};
        }
        return new double[]{caster.posX, caster.posY, caster.posZ};
    }

    /**
     * Resolve the display data for a specific zone index.
     * Default returns the shared displayData. Override for per-zone color overrides.
     */
    protected EnergyDisplayData resolveDisplay(int index) {
        return displayData;
    }

    // ==================== ZONE COUNT ====================

    public int getZoneCount() {
        return zoneCount;
    }

    public void setZoneCount(int count) {
        this.zoneCount = Math.max(1, Math.min(count, MAX_ZONES));
    }

    public int getFireDelay() {
        return fireDelay;
    }

    public void setFireDelay(int delay) {
        this.fireDelay = Math.max(0, delay);
    }

    // ==================== RUNTIME STATE INIT ====================

    protected void initRuntimeState(EntityLivingBase caster) {
        entities = createEntityArray(zoneCount);
        zoneSpawned = new boolean[zoneCount];
        spawnedCount = 0;
        chargeVisualIds = new String[zoneCount];
        chargeVisualCaster = caster;
    }

    // ==================== ENTITY CREATION ====================

    protected E createZoneEntity(EntityLivingBase caster, EntityLivingBase target, int index) {
        double[] pos = getSpawnPosition(caster, target, index);
        EnergyDisplayData resolved = resolveDisplay(index);
        E entity = createEntity(caster, target, pos[0], pos[1], pos[2], resolved, index);
        entity.setSourceAbility(this);
        entity.setIgnoreIFrames(this.isIgnoreIFrames());
        entity.setEffects(this.effects);
        noppes.npcs.controllers.data.MagicData resolvedMagic = resolveMagicData(caster);
        if (resolvedMagic != null) {
            entity.setMagicData(resolvedMagic.copy());
        }
        return entity;
    }

    protected void spawnZoneEntity(E entity, int index) {
        if (entity == null) return;
        spawnAbilityEntity(entity);
        entities[index] = entity;
        if (!zoneSpawned[index]) {
            zoneSpawned[index] = true;
            spawnedCount++;
        }
    }

    protected void spawnZoneAt(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (entities != null && entities[index] != null && !entities[index].isDead) return;
        E entity = createZoneEntity(caster, target, index);
        spawnZoneEntity(entity, index);
    }

    // ==================== CHARGE VISUAL ====================

    protected String getChargeVisualId(EntityLivingBase caster, int index) {
        return "energy_charge:" + caster.getEntityId() + ":" + executionStartTime + ":" + burstIndex + ":" + index;
    }

    protected void spawnChargeVisual(EntityLivingBase caster, EntityLivingBase target, int index, int chargeDuration) {
        if (isPreview() || caster == null || caster.worldObj == null || caster.worldObj.isRemote) return;
        if (chargeVisualIds == null || index < 0 || index >= chargeVisualIds.length) return;

        E previewEntity = createZoneEntity(caster, target, index);
        previewEntity.setPreviewMode(true);
        previewEntity.setPreviewOwner(caster);
        setupEntityCharging(previewEntity, index);
        previewEntity.setChargeDuration(Math.max(1, chargeDuration));

        String id = getChargeVisualId(caster, index);
        chargeVisualIds[index] = id;
        chargeVisualCaster = caster;
        EnergyChargeSpawnPacket.sendToTracking(id, previewEntity, caster);

        EnergyChargeTracker.Instance.add(new EnergyChargeTracker.ChargeEntry(
            id,
            previewEntity.getClass().getName(),
            previewEntity.exportSpawnNBT(),
            caster.getEntityId(),
            (int) caster.worldObj.getTotalWorldTime()
        ));
    }

    protected void removeChargeVisual(EntityLivingBase caster, int index) {
        if (isPreview() || caster == null || caster.worldObj == null || caster.worldObj.isRemote) return;
        if (chargeVisualIds == null || index < 0 || index >= chargeVisualIds.length) return;

        String id = chargeVisualIds[index];
        if (id != null && !id.isEmpty()) {
            EnergyChargeRemovePacket.sendToTracking(id, caster);
            EnergyChargeTracker.Instance.remove(id, caster.getEntityId());
            chargeVisualIds[index] = null;
        }
    }

    protected void removeAllChargeVisuals(EntityLivingBase caster) {
        if (chargeVisualIds == null || caster == null) return;
        for (int i = 0; i < chargeVisualIds.length; i++) {
            removeChargeVisual(caster, i);
        }
        if (!isPreview() && caster.worldObj != null && !caster.worldObj.isRemote) {
            EnergyChargeTracker.Instance.removeAllForCaster(caster.getEntityId());
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
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public boolean isReadyForBurstCompletion(int activeTick) {
        return fireDelay <= 0 || activeTick >= fireDelay * (zoneCount - 1);
    }

    @Override
    public void detach() {
        entities = null;
        zoneSpawned = null;
        spawnedCount = 0;
        preCalculatedPositions.clear();
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (caster.worldObj.isRemote && !isPreview()) return;

        if (tick == 1) {
            initRuntimeState(caster);

            if (isPreview()) {
                for (int i = 0; i < zoneCount; i++) {
                    E previewEntity = createZoneEntity(caster, target, i);
                    setupEntityPreview(previewEntity, caster, resolveDisplay(i), i);
                    spawnAbilityEntity(previewEntity);
                    entities[i] = previewEntity;
                    zoneSpawned[i] = true;
                    spawnedCount++;
                }
                return;
            }

            for (int i = 0; i < zoneCount; i++) {
                spawnChargeVisual(caster, target, i, windUpTicks);
            }
        }
    }

    @Override
    public void resetForBurst() {
        removeAllChargeVisuals(chargeVisualCaster);
        if (!burstOverlap) {
            cleanup();
        }
        entities = null;
        zoneSpawned = null;
        spawnedCount = 0;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (isPreview()) {
            if (entities == null) {
                initRuntimeState(caster);
                for (int i = 0; i < zoneCount; i++) {
                    E previewEntity = createZoneEntity(caster, target, i);
                    setupEntityPreview(previewEntity, caster, resolveDisplay(i), i);
                    spawnAbilityEntity(previewEntity);
                    entities[i] = previewEntity;
                    zoneSpawned[i] = true;
                    spawnedCount++;
                }
            }
            return;
        }

        if (entities == null || zoneSpawned == null || entities.length != zoneCount) {
            initRuntimeState(caster);
        }

        removeAllChargeVisuals(caster);

        spawnZoneAt(caster, target, 0);

        if (fireDelay <= 0) {
            for (int i = 1; i < zoneCount; i++) {
                spawnZoneAt(caster, target, i);
            }
        } else {
            for (int i = 1; i < zoneCount; i++) {
                spawnChargeVisual(caster, target, i, fireDelay * i);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (entities == null || zoneSpawned == null) {
            signalCompletion();
            return;
        }

        if (fireDelay > 0) {
            for (int i = 1; i < zoneCount; i++) {
                if (tick == fireDelay * i) {
                    removeChargeVisual(caster, i);
                    spawnZoneAt(caster, target, i);
                }
            }
        }

        if (isFreeOnCast()) {
            int lastSpawnTick = fireDelay > 0 ? fireDelay * (zoneCount - 1) : 0;
            if (tick >= lastSpawnTick) {
                signalCompletion();
                return;
            }
        }

        boolean allDead = spawnedCount >= zoneCount;
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

        if (!allDead) {
            int lastSpawnTick = fireDelay > 0 ? fireDelay * (zoneCount - 1) : 0;
            int maxLifetime = lifespanData.getMaxLifetime() > 0 ? lifespanData.getMaxLifetime() : 200;
            if (tick > lastSpawnTick + maxLifetime + 20) {
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
        zoneSpawned = null;
        spawnedCount = 0;
        chargeVisualIds = null;
        chargeVisualCaster = null;
        preCalculatedPositions.clear();
    }

    // ==================== PREVIEW ====================

    @Override
    public int getMaxPreviewDuration() {
        return lifespanData.getMaxLifetime() > 0 ? Math.min(lifespanData.getMaxLifetime(), 100) : 100;
    }

    // ==================== NBT ====================

    @Override
    public final void writeTypeNBT(NBTTagCompound nbt) {
        writeTypeSpecificNBT(nbt);

        nbt.setInteger("zoneCount", zoneCount);
        nbt.setInteger("fireDelay", fireDelay);

        writeEnergyNBT(nbt);
        combatData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
    }

    @Override
    public final void readTypeNBT(NBTTagCompound nbt) {
        readTypeSpecificNBT(nbt);

        this.zoneCount = Math.max(1, Math.min(nbt.getInteger("zoneCount"), MAX_ZONES));
        this.fireDelay = Math.max(0, nbt.getInteger("fireDelay"));

        readEnergyNBT(nbt);
        combatData.readNBT(nbt);
        lifespanData.readNBT(nbt);
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.enumField("ability.hitType", HitType.class,
                () -> HitType.fromOrdinal(getHitType()),
                v -> setHitType(v.ordinal()))
            .hover("ability.hover.hitType"));
        defs.add(FieldDef.intField("ability.multiHitDelay", this::getMultiHitDelayTicks, this::setMultiHitDelayTicks)
            .range(1, 200)
            .visibleWhen(() -> getHitType() == HitType.MULTI.ordinal())
            .hover("ability.hover.multiHitDelay"));
        defs.add(FieldDef.intField("ability.maxHits", this::getMaxHits, this::setMaxHits)
            .range(1, EnergyCombatData.MAX_HITS)
            .visibleWhen(() -> getHitType() != HitType.SINGLE.ordinal())
            .hover("ability.hover.maxHits"));

        defs.add(FieldDef.intField("ability.zoneCount", this::getZoneCount, this::setZoneCount)
            .range(1, MAX_ZONES));
        defs.add(FieldDef.intField("ability.fireDelay", this::getFireDelay, this::setFireDelay)
            .range(0, 200)
            .visibleWhen(() -> zoneCount > 1));

        addTypeDefinitions(defs);

        addEnergyColorDefinitions(defs);
        addEnergyEffectDefinitions(defs);
    }

    // ==================== COMBAT GETTERS & SETTERS ====================

    public float getDamage() { return combatData.getDamage(); }
    public void setDamage(float damage) { combatData.setDamage(damage); }

    @Override
    public float getDisplayDamage() { return combatData.getDamage(); }

    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { combatData.setKnockback(knockback); }

    public int getHitType() { return combatData.hitType.ordinal(); }
    public void setHitType(int hitType) { combatData.hitType = HitType.fromOrdinal(hitType); }

    public int getMultiHitDelayTicks() { return combatData.multiHitDelayTicks; }
    public void setMultiHitDelayTicks(int delay) { combatData.multiHitDelayTicks = Math.max(1, delay); }

    public int getMaxHits() { return combatData.getMaxHits(); }
    public void setMaxHits(int maxHits) { combatData.setMaxHits(maxHits); }

    public int getMaxLifetime() { return lifespanData.getMaxLifetime(); }
    public void setMaxLifetime(int ticks) { lifespanData.setMaxLifetime(ticks); }
}
