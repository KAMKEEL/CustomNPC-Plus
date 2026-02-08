package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityDisc;

import java.util.Arrays;
import java.util.List;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;

/**
 * Disc ability: Spawns a flat spinning disc projectile.
 * Supports 1-8 projectiles with shared visuals and per-projectile anchor points.
 * Has optional boomerang behavior to return to owner.
 */
public class AbilityDisc extends Ability implements IAbilityDisc {

    private static final int MAX_PROJECTILES = 8;

    // Disc geometry (shared)
    private float discRadius = 1.0f;
    private float discThickness = 0.2f;
    private boolean vertical = false; // false = horizontal (flat), true = vertical (thin edge forward)

    // Boomerang properties (shared)
    private boolean boomerang = false;
    private int boomerangDelay = 40;

    // Projectile count and fire stagger
    private int projectileCount = 1;
    private int fireDelay = 0;

    // Shared visual data (all projectiles use these)
    private EnergyDisplayData displayData = new EnergyDisplayData(0xFFFFFF, 0xFF8800, true, 0.4f, 0.5f, 5.0f);
    private EnergyLightningData lightningData = new EnergyLightningData();
    private EnergyTrajectoryData trajectoryData = new EnergyTrajectoryData();

    // Shared combat/movement data
    private final EnergyCombatData combatData = new EnergyCombatData(8.0f, 1.2f, 0.15f, false, 3.0f, 0.5f);
    private final EnergyHomingData homingData = new EnergyHomingData(0.6f, true, 0.12f, 18.0f);
    private final EnergyLifespanData lifespanData = new EnergyLifespanData(35.0f, 200);

    // Per-projectile data (anchor + optional color override)
    private ProjectileData[] projectiles;

    // Transient entity state
    private transient EntityAbilityDisc[] discEntities;

    public AbilityDisc() {
        this.typeId = "ability.cnpc.disc";
        this.name = "Disc";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 30.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 60;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        this.windUpAnimationName = "Ability_Disc_Windup";
        this.activeAnimationName = "Ability_Disc_Active";
        initProjectiles(1);
    }

    private void initProjectiles(int count) {
        this.projectileCount = Math.max(1, Math.min(count, MAX_PROJECTILES));
        this.projectiles = new ProjectileData[projectileCount];
        for (int i = 0; i < projectileCount; i++) {
            projectiles[i] = new ProjectileData(getDefaultAnchor(i));
        }
    }

    /**
     * Disc anchor order is REVERSED from Orb/Beam: [0]=LEFT_HAND, [1]=RIGHT_HAND
     */
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

    // Legacy compat
    public int getDualFireDelay() { return fireDelay; }
    public void setDualFireDelay(int delay) { setFireDelay(delay); }

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.single", a -> {
                a.setName("Disc");
            }),
            new AbilityVariant("ability.variant.dual", a -> {
                AbilityDisc disc = (AbilityDisc) a;
                a.setName("Dual Disc");
                disc.setProjectileCount(2);
                disc.setFireDelay(10);
                // Set color override for second disc
                disc.projectiles[1].colorOverride = true;
                disc.projectiles[1].innerColor = 0xFFFFFF;
                disc.projectiles[1].outerColor = 0x8800FF;
                a.setWindUpAnimationName("Ability_DiscDual_Windup");
                a.setActiveAnimationName("Ability_DiscDual_Active");
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
     * Create disc entities.
     * NPC: target is the aggro target — discs will home toward it.
     * Player: target is null — discs fire in caster's look direction.
     */
    private EntityAbilityDisc[] createDiscEntities(EntityLivingBase caster, EntityLivingBase target, World world) {
        EntityAbilityDisc[] entities = new EntityAbilityDisc[projectileCount];
        for (int i = 0; i < projectileCount; i++) {
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, projectiles[i].anchor);
            EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
            entities[i] = new EntityAbilityDisc(
                world, caster, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
                discRadius, discThickness,
                resolved, combatData, homingData, lightningData, lifespanData, trajectoryData,
                boomerang, boomerangDelay);
            entities[i].setEffects(this.effects);
            entities[i].setSourceAbility(this);
        }
        if (projectileCount == 2 && entities[0] != null && entities[1] != null) {
            entities[0].setSiblingEntityId(entities[1].getEntityId());
            entities[1].setSiblingEntityId(entities[0].getEntityId());
        }
        return entities;
    }

    private void fireDiscEntity(EntityAbilityDisc disc, EntityLivingBase target) {
        if (disc == null || disc.isDead) return;
        if (isPreview()) {
            disc.startPreviewFiring();
        } else {
            disc.startMoving(target);
        }
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote && !isPreview()) return;

        if (tick == 1) {
            discEntities = createDiscEntities(caster, target, world);
            for (int i = 0; i < projectileCount; i++) {
                EnergyDisplayData resolved = projectiles[i].resolveDisplay(displayData);
                if (isPreview()) {
                    discEntities[i].setupPreview(caster, discRadius, discThickness, resolved, lightningData, projectiles[i].anchor, windUpTicks, vertical);
                } else {
                    discEntities[i].setupCharging(projectiles[i].anchor, windUpTicks, vertical);
                }
                spawnAbilityEntity(world, discEntities[i]);
            }
        }
    }

    @Override
    public void onBurstRefire(EntityLivingBase caster, EntityLivingBase target, World world) {
        discEntities = createDiscEntities(caster, target, world);
        for (EntityAbilityDisc disc : discEntities) {
            spawnAbilityEntity(world, disc);
        }
        for (EntityAbilityDisc disc : discEntities) {
            fireDiscEntity(disc, target);
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote && !isPreview()) {
            signalCompletion();
            return;
        }

        if (discEntities == null) return;

        // Fire first projectile immediately
        fireDiscEntity(discEntities[0], target);

        // Fire remaining projectiles if no delay
        if (fireDelay <= 0) {
            for (int i = 1; i < projectileCount; i++) {
                fireDiscEntity(discEntities[i], target);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (discEntities == null) {
            signalCompletion();
            return;
        }

        // Fire staggered projectiles
        if (fireDelay > 0) {
            for (int i = 1; i < projectileCount; i++) {
                if (tick == fireDelay * i) {
                    fireDiscEntity(discEntities[i], target);
                }
            }
        }

        // Check if all entities are dead
        boolean allDead = true;
        for (EntityAbilityDisc disc : discEntities) {
            if (disc != null && !disc.isDead) {
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
        if (discEntities != null) {
            for (int i = 0; i < discEntities.length; i++) {
                if (discEntities[i] != null && !discEntities[i].isDead) {
                    discEntities[i].setDead();
                }
                discEntities[i] = null;
            }
        }
        discEntities = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        // NPC: telegraph on aggro target position
        // Player: no telegraph (discs fire in look direction, no fixed position to mark)
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster) || target == null) {
            return null;
        }

        // Create circle telegraph at target position
        Telegraph telegraph = Telegraph.circle(discRadius * 1.5f);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at target and follow target during windup
        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, target.posY, target.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(target.getEntityId());

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return discRadius * 1.5f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("discRadius", discRadius);
        nbt.setFloat("discThickness", discThickness);
        nbt.setBoolean("vertical", vertical);
        nbt.setBoolean("boomerang", boomerang);
        nbt.setInteger("boomerangDelay", boomerangDelay);
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
        this.discRadius = nbt.hasKey("discRadius") ? nbt.getFloat("discRadius") : 1.0f;
        this.discThickness = nbt.hasKey("discThickness") ? nbt.getFloat("discThickness") : 0.2f;
        this.vertical = nbt.hasKey("vertical") && nbt.getBoolean("vertical");
        this.boomerang = nbt.hasKey("boomerang") && nbt.getBoolean("boomerang");
        this.boomerangDelay = nbt.hasKey("boomerangDelay") ? nbt.getInteger("boomerangDelay") : 40;

        int count = nbt.hasKey("projectileCount") ? nbt.getInteger("projectileCount") : 1;
        initProjectiles(count);

        // Read fireDelay with fallback to old dualFireDelay key
        if (nbt.hasKey("fireDelay")) {
            this.fireDelay = nbt.getInteger("fireDelay");
        } else if (nbt.hasKey("dualFireDelay")) {
            this.fireDelay = nbt.getInteger("dualFireDelay");
        } else {
            this.fireDelay = 0;
        }

        // Shared combat/movement data
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);

        // Determine format and read accordingly
        if (nbt.hasKey("Projectile_0")) {
            NBTTagCompound proj0 = nbt.getCompoundTag("Projectile_0");
            if (proj0.hasKey("colorOverride")) {
                // NEW format: shared visuals at root + ProjectileData per projectile
                displayData.readNBT(nbt);
                lightningData.readNBT(nbt);
                for (int i = 0; i < projectileCount; i++) {
                    if (nbt.hasKey("Projectile_" + i)) {
                        projectiles[i].readNBT(nbt.getCompoundTag("Projectile_" + i));
                    }
                }
            } else {
                // OLD format: full per-projectile arrays (displayData, lightningData, etc. in each Projectile_i)
                // Read primary visuals from Projectile_0
                displayData.readNBT(proj0);
                lightningData.readNBT(proj0);
                projectiles[0].anchor.readNBT(proj0);

                // Read remaining projectiles, set color override if colors differ
                for (int i = 1; i < projectileCount; i++) {
                    if (nbt.hasKey("Projectile_" + i)) {
                        NBTTagCompound projNbt = nbt.getCompoundTag("Projectile_" + i);
                        projectiles[i].anchor.readNBT(projNbt);

                        // Check if this projectile had different colors
                        EnergyDisplayData tempDisplay = new EnergyDisplayData();
                        tempDisplay.readNBT(projNbt);
                        if (tempDisplay.innerColor != displayData.innerColor || tempDisplay.outerColor != displayData.outerColor) {
                            projectiles[i].colorOverride = true;
                            projectiles[i].innerColor = tempDisplay.innerColor;
                            projectiles[i].outerColor = tempDisplay.outerColor;
                        }
                    }
                }
            }
        } else {
            // LEGACY flat format: all data at root level
            displayData.readNBT(nbt);
            lightningData.readNBT(nbt);
            projectiles[0].anchor.readNBT(nbt);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // API GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { homingData.speed = speed; }

    public float getDiscRadius() { return discRadius; }
    public void setDiscRadius(float discRadius) { this.discRadius = discRadius; }

    public float getDiscThickness() { return discThickness; }
    public void setDiscThickness(float discThickness) { this.discThickness = discThickness; }

    public boolean isVertical() { return vertical; }
    public void setVertical(boolean vertical) { this.vertical = vertical; }

    public boolean isBoomerang() { return boomerang; }
    public void setBoomerang(boolean boomerang) { this.boomerang = boomerang; }

    public int getBoomerangDelay() { return boomerangDelay; }
    public void setBoomerangDelay(int boomerangDelay) { this.boomerangDelay = boomerangDelay; }

    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float d) { lifespanData.maxDistance = d; }

    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int t) { lifespanData.maxLifetime = t; }

    public float getDamage() { return combatData.damage; }
    public void setDamage(float d) { combatData.damage = d; }

    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float k) { combatData.knockback = k; }

    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float k) { combatData.knockbackUp = k; }

    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean h) { homingData.homing = h; }

    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float s) { homingData.homingStrength = s; }

    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float r) { homingData.homingRange = r; }

    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean e) { combatData.explosive = e; }

    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float r) { combatData.explosionRadius = r; }

    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float f) { combatData.explosionDamageFalloff = f; }

    // Shared display data (primary colors)
    public int getInnerColor() { return displayData.innerColor; }
    public void setInnerColor(int c) { displayData.innerColor = c; }

    public int getOuterColor() { return displayData.outerColor; }
    public void setOuterColor(int c) { displayData.outerColor = c; }

    public float getOuterColorWidth() { return displayData.outerColorWidth; }
    public void setOuterColorWidth(float w) { displayData.outerColorWidth = w; }

    public float getOuterColorAlpha() { return displayData.outerColorAlpha; }
    public void setOuterColorAlpha(float a) { displayData.outerColorAlpha = a; }

    public boolean isOuterColorEnabled() { return displayData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean e) { displayData.outerColorEnabled = e; }

    public float getRotationSpeed() { return displayData.rotationSpeed; }
    public void setRotationSpeed(float s) { displayData.rotationSpeed = s; }

    // Shared lightning data
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean e) { lightningData.lightningEffect = e; }

    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float d) { lightningData.lightningDensity = d; }

    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float r) { lightningData.lightningRadius = r; }

    public int getLightningFadeTime() { return lightningData.lightningFadeTime; }
    public void setLightningFadeTime(int t) { lightningData.lightningFadeTime = t; }

    // Anchor - default to projectile 0
    public AnchorPoint getAnchorPointEnum() { return projectiles[0].anchor.anchorPoint; }
    public void setAnchorPointEnum(AnchorPoint p) { projectiles[0].anchor.anchorPoint = p; }

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
        // Type tab - projectile count and fire delay
        defs.add(FieldDef.row(
            FieldDef.intField("ability.projectileCount", this::getProjectileCount, this::setProjectileCount).range(1, MAX_PROJECTILES),
            FieldDef.intField("ability.fireDelay", this::getFireDelay, this::setFireDelay)
                .range(0, 200).visibleWhen(() -> projectileCount > 1)
        ));

        // Type tab - disc properties
        defs.add(FieldDef.row(
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
            FieldDef.floatField("stats.speed", this::getSpeed, this::setSpeed)
        ));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback));
        defs.add(FieldDef.section("ability.section.disc"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.radius", this::getDiscRadius, this::setDiscRadius),
            FieldDef.floatField("gui.thickness", this::getDiscThickness, this::setDiscThickness)
        ));
        defs.add(FieldDef.boolField("ability.vertical", this::isVertical, this::setVertical)
            .hover("ability.hover.vertical"));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
        ));
        defs.add(FieldDef.section("ability.section.homing"));
        defs.add(FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"));
        defs.add(FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming));
        defs.add(FieldDef.section("ability.section.boomerang"));
        defs.add(FieldDef.boolField("gui.enabled", this::isBoomerang, this::setBoomerang).hover("ability.hover.boomerang"));
        defs.add(FieldDef.intField("gui.delay", this::getBoomerangDelay, this::setBoomerangDelay).visibleWhen(this::isBoomerang));
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

            // Anchor point
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
