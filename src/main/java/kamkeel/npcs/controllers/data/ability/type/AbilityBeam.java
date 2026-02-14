package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.RotationMode;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityEnergyBeam;

import java.util.Arrays;
import java.util.List;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;

/**
 * Energy Beam ability: A homing head with a trailing path.
 * Supports 1-8 projectiles with shared visuals and per-projectile anchor points.
 */
public class AbilityBeam extends Ability implements IAbilityEnergyBeam {

    private static final int MAX_PROJECTILES = 8;

    // Beam shape properties (shared)
    private float beamWidth = 0.4f;
    private float headSize = 0.6f;

    // Projectile count and fire stagger
    private int projectileCount = 1;
    private int fireDelay = 0;

    // Shared visual data (all projectiles use these)
    private EnergyDisplayData displayData = new EnergyDisplayData(0xFFFFFF, 0x00AAFF, true, 0.4f, 0.5f, 6.0f);
    private EnergyLightningData lightningData = new EnergyLightningData();
    private EnergyTrajectoryData trajectoryData = new EnergyTrajectoryData();

    // Shared combat/movement data
    private final EnergyCombatData combatData = new EnergyCombatData(10.0f, 1.5f, 0.2f, false, 4.0f, 0.5f);
    private final EnergyHomingData homingData = new EnergyHomingData(0.4f, true, 0.1f, 15.0f);
    private final EnergyLifespanData lifespanData = new EnergyLifespanData(25.0f, 200);

    // Per-projectile data (anchor + optional color override)
    private ProjectileData[] projectiles;

    // Transient entity state
    private transient EntityAbilityBeam[] beamEntities;

    public AbilityBeam() {
        this.typeId = "ability.cnpc.beam";
        this.name = "Beam";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 40;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.rotationMode = RotationMode.TRACK;
        this.rotationPhase = LockMovementType.WINDUP_AND_ACTIVE;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Beam_Windup";
        this.activeAnimationName = "Ability_Beam_Active";
        initProjectiles(1);
    }

    private void initProjectiles(int count) {
        this.projectileCount = Math.max(1, Math.min(count, MAX_PROJECTILES));
        this.projectiles = new ProjectileData[projectileCount];
        for (int i = 0; i < projectileCount; i++) {
            projectiles[i] = new ProjectileData(getDefaultAnchor(i));
        }
    }

    private AnchorPoint getDefaultAnchor(int index) {
        switch (index) {
            case 0: return AnchorPoint.RIGHT_HAND;
            case 1: return AnchorPoint.LEFT_HAND;
            default: return AnchorPoint.FRONT;
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

    /** @deprecated Use {@link #getFireDelay()} */
    public int getDualFireDelay() { return fireDelay; }
    /** @deprecated Use {@link #setFireDelay(int)} */
    public void setDualFireDelay(int delay) { setFireDelay(delay); }

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.single", a -> {
                a.setName("Beam");
            }),
            new AbilityVariant("ability.variant.dual", a -> {
                AbilityBeam beam = (AbilityBeam) a;
                a.setName("Dual Beam");
                beam.setProjectileCount(2);
                beam.setFireDelay(0);
                // Set color override for second beam
                beam.projectiles[1].colorOverride = true;
                beam.projectiles[1].innerColor = 0xFFFFFF;
                beam.projectiles[1].outerColor = 0xFF0000;
                a.setWindUpTicks(80);
                a.setWindUpAnimationName("Ability_BeamDual_Windup");
                a.setActiveAnimationName("Ability_BeamDual_Active");
            })
        );
    }

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

    /**
     * Create beam entities.
     * NPC: target is the aggro target — beams will home toward it.
     * Player: target is null — beams fire in caster's look direction.
     */
    private EntityAbilityBeam[] createBeamEntities(EntityLivingBase caster, EntityLivingBase target, World world) {
        EntityAbilityBeam[] entities = new EntityAbilityBeam[projectileCount];
        float offsetDist = 1.0f;
        for (int i = 0; i < projectileCount; i++) {
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, projectiles[i].anchor, offsetDist);
            EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
            entities[i] = new EntityAbilityBeam(
                world, caster, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
                beamWidth, headSize,
                resolved, combatData, homingData, lightningData, lifespanData, trajectoryData,
                lockMovement.locksActive());
            entities[i].setEffects(this.effects);
            entities[i].setSourceAbility(this);
        }
        if (projectileCount == 2 && entities[0] != null && entities[1] != null) {
            entities[0].setSiblingEntityId(entities[1].getEntityId());
            entities[1].setSiblingEntityId(entities[0].getEntityId());
        }
        return entities;
    }

    private void fireBeamEntity(EntityAbilityBeam beam, EntityLivingBase target) {
        if (beam == null || beam.isDead) return;
        if (isPreview()) {
            beam.startPreviewFiring();
        } else {
            beam.startFiring(target);
        }
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote && !isPreview()) return;

        if (tick == 1) {
            beamEntities = createBeamEntities(caster, target, world);
            float offsetDist = 1.0f;
            for (int i = 0; i < projectileCount; i++) {
                EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
                if (isPreview()) {
                    beamEntities[i].setupPreview(caster, beamWidth, headSize, resolved, lightningData, projectiles[i].anchor, windUpTicks, offsetDist);
                } else {
                    beamEntities[i].setupCharging(projectiles[i].anchor, windUpTicks, offsetDist);
                }
                spawnAbilityEntity(world, beamEntities[i]);
            }
        }
    }

    @Override
    public void onBurstRefire(EntityLivingBase caster, EntityLivingBase target, World world) {
        beamEntities = createBeamEntities(caster, target, world);
        for (EntityAbilityBeam beam : beamEntities) {
            spawnAbilityEntity(world, beam);
        }

        // Fire first projectile immediately, let onActiveTick handle the rest with fireDelay
        fireBeamEntity(beamEntities[0], target);
        if (fireDelay <= 0) {
            for (int i = 1; i < projectileCount; i++) {
                fireBeamEntity(beamEntities[i], target);
            }
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote && !isPreview()) {
            signalCompletion();
            return;
        }

        if (beamEntities == null) return;

        fireBeamEntity(beamEntities[0], target);

        if (fireDelay <= 0) {
            for (int i = 1; i < projectileCount; i++) {
                fireBeamEntity(beamEntities[i], target);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (beamEntities == null) {
            signalCompletion();
            return;
        }

        if (fireDelay > 0) {
            for (int i = 1; i < projectileCount; i++) {
                if (tick == fireDelay * i) {
                    fireBeamEntity(beamEntities[i], target);
                }
            }
        }

        boolean allDead = true;
        for (EntityAbilityBeam beam : beamEntities) {
            if (beam != null && !beam.isDead) {
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
    public void onInterrupt(EntityLivingBase caster, net.minecraft.util.DamageSource source, float damage) {
        cleanup();
    }

    @Override
    public void cleanup() {
        if (beamEntities != null) {
            for (int i = 0; i < beamEntities.length; i++) {
                if (beamEntities[i] != null && !beamEntities[i].isDead) {
                    beamEntities[i].setDead();
                }
                beamEntities[i] = null;
            }
        }
        beamEntities = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        // NPC: telegraph on aggro target position
        // Player: no telegraph (beam fires in look direction, no fixed position to mark)
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster) || target == null) {
            return null;
        }

        Telegraph telegraph = Telegraph.circle(headSize * 2.0f);
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
        return headSize * 2.0f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("beamWidth", beamWidth);
        nbt.setFloat("headSize", headSize);
        nbt.setInteger("projectileCount", projectileCount);
        nbt.setInteger("fireDelay", fireDelay);

        // Shared visual data
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);

        // Shared combat/movement data
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
    public void readTypeNBT(NBTTagCompound nbt) {
        this.beamWidth = nbt.getFloat("beamWidth");
        this.headSize = nbt.getFloat("headSize");

        int count = nbt.getInteger("projectileCount");
        initProjectiles(count);

        this.fireDelay = nbt.getInteger("fireDelay");

        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);

        // Shared visual data
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);

        // Per-projectile data
        for (int i = 0; i < projectileCount; i++) {
            if (nbt.hasKey("Projectile_" + i)) {
                projectiles[i].readNBT(nbt.getCompoundTag("Projectile_" + i));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // API GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public float getBeamWidth() { return beamWidth; }
    public void setBeamWidth(float beamWidth) { this.beamWidth = beamWidth; }

    public float getHeadSize() { return headSize; }
    public void setHeadSize(float headSize) { this.headSize = headSize; }

    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { this.homingData.speed = speed; }

    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float maxDistance) { this.lifespanData.maxDistance = maxDistance; }

    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.lifespanData.maxLifetime = maxLifetime; }

    public float getDamage() { return combatData.damage; }
    public void setDamage(float damage) { this.combatData.damage = damage; }

    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { this.combatData.knockback = knockback; }

    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.combatData.knockbackUp = knockbackUp; }

    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean homing) { this.homingData.homing = homing; }

    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float homingStrength) { this.homingData.homingStrength = homingStrength; }

    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float homingRange) { this.homingData.homingRange = homingRange; }

    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean explosive) { this.combatData.explosive = explosive; }

    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.combatData.explosionRadius = explosionRadius; }

    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { this.combatData.explosionDamageFalloff = explosionDamageFalloff; }

    // Shared display data (primary colors)
    public int getInnerColor() { return displayData.innerColor; }
    public void setInnerColor(int innerColor) { displayData.innerColor = innerColor; }

    public int getOuterColor() { return displayData.outerColor; }
    public void setOuterColor(int outerColor) { displayData.outerColor = outerColor; }

    public boolean isOuterColorEnabled() { return displayData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { displayData.outerColorEnabled = outerColorEnabled; }

    public float getOuterColorWidth() { return displayData.outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { displayData.outerColorWidth = outerColorWidth; }

    public float getOuterColorAlpha() { return displayData.outerColorAlpha; }
    public void setOuterColorAlpha(float outerColorAlpha) { displayData.outerColorAlpha = outerColorAlpha; }

    public float getRotationSpeed() { return displayData.rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { displayData.rotationSpeed = rotationSpeed; }

    // Shared lightning data
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { lightningData.lightningEffect = lightningEffect; }

    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float lightningDensity) { lightningData.lightningDensity = lightningDensity; }

    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float lightningRadius) { lightningData.lightningRadius = lightningRadius; }

    public int getLightningFadeTime() { return lightningData.lightningFadeTime; }
    public void setLightningFadeTime(int lightningFadeTime) { lightningData.lightningFadeTime = lightningFadeTime; }

    // Anchor - default to projectile 0
    public AnchorPoint getAnchorPointEnum() { return projectiles[0].anchor.anchorPoint; }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { projectiles[0].anchor.anchorPoint = anchorPoint; }

    public float getAnchorOffsetX() { return projectiles[0].anchor.anchorOffsetX; }
    public void setAnchorOffsetX(float x) { projectiles[0].anchor.anchorOffsetX = x; }

    public float getAnchorOffsetY() { return projectiles[0].anchor.anchorOffsetY; }
    public void setAnchorOffsetY(float y) { projectiles[0].anchor.anchorOffsetY = y; }

    public float getAnchorOffsetZ() { return projectiles[0].anchor.anchorOffsetZ; }
    public void setAnchorOffsetZ(float z) { projectiles[0].anchor.anchorOffsetZ = z; }

    @Override
    public int getAnchorPoint() { return projectiles[0].anchor.anchorPoint.ordinal(); }

    @Override
    public void setAnchorPoint(int point) { projectiles[0].anchor.anchorPoint = AnchorPoint.fromOrdinal(point); }

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

    private int clampIndex(int index) {
        return Math.max(0, Math.min(index, projectileCount - 1));
    }

    @Override
    public int getMaxPreviewDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GUI FIELD DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.row(
            FieldDef.intField("ability.projectileCount", this::getProjectileCount, this::setProjectileCount).range(1, MAX_PROJECTILES),
            FieldDef.intField("ability.fireDelay", this::getFireDelay, this::setFireDelay)
                .range(0, 200).visibleWhen(() -> projectileCount > 1)
        ));

        defs.add(FieldDef.row(
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
            FieldDef.floatField("stats.speed", this::getSpeed, this::setSpeed)
        ));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback));
        defs.add(FieldDef.section("ability.section.beam"));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.beamWidth", this::getBeamWidth, this::setBeamWidth),
            FieldDef.floatField("ability.headSize", this::getHeadSize, this::setHeadSize)
        ));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
        ));
        defs.add(FieldDef.section("ability.section.homing"));
        defs.add(FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming),
            FieldDef.floatField("gui.range", this::getHomingRange, this::setHomingRange).visibleWhen(this::isHoming)
        ));
        defs.add(FieldDef.section("ability.section.explosive"));
        defs.add(FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"));
        defs.add(FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive));
        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));

        // Visual tab - shared primary colors
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

        // Visual tab - shared effects
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

        // Visual tab - per-projectile sections
        for (int i = 0; i < MAX_PROJECTILES; i++) {
            final int idx = i;
            String sectionKey = "ability.section.projectile" + (i + 1);

            defs.add(FieldDef.section(sectionKey).tab("ability.tab.visual")
                .visibleWhen(() -> idx < projectileCount));

            defs.add(FieldDef.enumField("ability.anchorPoint", AnchorPoint.class,
                () -> projectiles[idx].anchor.anchorPoint, v -> projectiles[idx].anchor.anchorPoint = v)
                .tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount));
            defs.add(FieldDef.row(
                FieldDef.floatField("ability.anchor.offsetX", () -> projectiles[idx].anchor.anchorOffsetX,
                    v -> projectiles[idx].anchor.anchorOffsetX = v).min(Float.NEGATIVE_INFINITY),
                FieldDef.floatField("ability.anchor.offsetY", () -> projectiles[idx].anchor.anchorOffsetY,
                    v -> projectiles[idx].anchor.anchorOffsetY = v).min(Float.NEGATIVE_INFINITY)
            ).tab("ability.tab.visual").visibleWhen(() -> idx < projectileCount));
            defs.add(FieldDef.floatField("ability.anchor.offsetZ", () -> projectiles[idx].anchor.anchorOffsetZ,
                    v -> projectiles[idx].anchor.anchorOffsetZ = v)
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
