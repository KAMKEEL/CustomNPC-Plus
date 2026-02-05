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
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityOrb;

import java.util.Arrays;
import java.util.List;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Orb ability: Spawns a homing projectile sphere that tracks target.
 * The EntityAbilityOrb handles all movement, collision, and damage logic.
 * This ability just configures and spawns the orb entity.
 */
public class AbilityOrb extends Ability implements IAbilityOrb {

    // Ability-specific properties
    private float orbSize = 1.0f;

    // Data classes for energy properties
    private final EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.RIGHT_HAND);
    private final EnergyCombatData combatData = new EnergyCombatData();
    private final EnergyDisplayData displayData = new EnergyDisplayData();
    private final EnergyHomingData homingData = new EnergyHomingData();
    private final EnergyLightningData lightningData = new EnergyLightningData();
    private final EnergyLifespanData lifespanData = new EnergyLifespanData();
    private final EnergyTrajectoryData trajectoryData = new EnergyTrajectoryData();

    // Transient state for orb entity (used during windup charging)
    private transient EntityAbilityOrb orbEntity = null;

    public AbilityOrb() {
        this.typeId = "ability.cnpc.orb";
        this.name = "Orb";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 25.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        // Default built-in animation
        this.windUpAnimationName = "Ability_Orb_Windup";
        this.activeAnimationName = "Ability_Orb_Active";
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
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        // Start moving the orb that was spawned during windup
        if (orbEntity != null && !orbEntity.isDead) {
            orbEntity.startMoving(target);
        }

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn orb in charging mode on first tick of windup
        if (tick == 1) {
            // Create orb in charging mode - follows caster based on anchor point during windup
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, anchorData);
            orbEntity = new EntityAbilityOrb(
                world, caster, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord, orbSize,
                displayData, combatData, homingData, lightningData, lifespanData, trajectoryData);
            orbEntity.setupCharging(anchorData, windUpTicks);

            orbEntity.setEffects(this.effects);
            orbEntity.setSourceAbility(this);
            world.spawnEntityInWorld(orbEntity);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (orbEntity == null || orbEntity.isDead) {
            orbEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        // Nothing to clean up - entity manages itself
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, net.minecraft.util.DamageSource source, float damage) {
        cleanup();
    }

    @Override
    public void cleanup() {
        // Despawn orb entity if still alive
        if (orbEntity != null && !orbEntity.isDead) {
            orbEntity.setDead();
        }
        orbEntity = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || target == null) {
            return null;
        }

        // Create small circle telegraph at target position
        Telegraph telegraph = Telegraph.circle(orbSize * 1.5f);
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
        return orbSize * 1.5f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("orbSize", orbSize);
        anchorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        displayData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
        trajectoryData.writeNBT(nbt);
        // Backward compat: old key was "orbSpeed"
        nbt.setFloat("orbSpeed", homingData.speed);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.orbSize = nbt.hasKey("orbSize") ? nbt.getFloat("orbSize") : 1.0f;
        anchorData.readNBT(nbt);
        combatData.readNBT(nbt);
        displayData.readNBT(nbt);
        homingData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        trajectoryData.readNBT(nbt);
        // Backward compat: old key was "orbSpeed"
        if (nbt.hasKey("orbSpeed")) {
            homingData.speed = nbt.getFloat("orbSpeed");
        }
    }

    // Getters & Setters
    public float getOrbSpeed() {
        return homingData.speed;
    }

    public void setOrbSpeed(float orbSpeed) {
        homingData.speed = orbSpeed;
    }

    public float getOrbSize() {
        return orbSize;
    }

    public void setOrbSize(float orbSize) {
        this.orbSize = orbSize;
    }

    public float getMaxDistance() {
        return lifespanData.maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        lifespanData.maxDistance = maxDistance;
    }

    public int getMaxLifetime() {
        return lifespanData.maxLifetime;
    }

    public void setMaxLifetime(int maxLifetime) {
        lifespanData.maxLifetime = maxLifetime;
    }

    public float getDamage() {
        return combatData.damage;
    }

    public void setDamage(float damage) {
        combatData.damage = damage;
    }

    public float getKnockback() {
        return combatData.knockback;
    }

    public void setKnockback(float knockback) {
        combatData.knockback = knockback;
    }

    public float getKnockbackUp() {
        return combatData.knockbackUp;
    }

    public void setKnockbackUp(float knockbackUp) {
        combatData.knockbackUp = knockbackUp;
    }

    public boolean isHoming() {
        return homingData.homing;
    }

    public void setHoming(boolean homing) {
        homingData.homing = homing;
    }

    public float getHomingStrength() {
        return homingData.homingStrength;
    }

    public void setHomingStrength(float homingStrength) {
        homingData.homingStrength = homingStrength;
    }

    public float getHomingRange() {
        return homingData.homingRange;
    }

    public void setHomingRange(float homingRange) {
        homingData.homingRange = homingRange;
    }

    public boolean isExplosive() {
        return combatData.explosive;
    }

    public void setExplosive(boolean explosive) {
        combatData.explosive = explosive;
    }

    public float getExplosionRadius() {
        return combatData.explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        combatData.explosionRadius = explosionRadius;
    }

    public float getExplosionDamageFalloff() {
        return combatData.explosionDamageFalloff;
    }

    public void setExplosionDamageFalloff(float explosionDamageFalloff) {
        combatData.explosionDamageFalloff = explosionDamageFalloff;
    }

    public int getInnerColor() {
        return displayData.innerColor;
    }

    public void setInnerColor(int innerColor) {
        displayData.innerColor = innerColor;
    }

    public int getOuterColor() {
        return displayData.outerColor;
    }

    public void setOuterColor(int outerColor) {
        displayData.outerColor = outerColor;
    }

    public float getOuterColorWidth() {
        return displayData.outerColorWidth;
    }

    public void setOuterColorWidth(float outerColorWidth) {
        displayData.outerColorWidth = outerColorWidth;
    }

    public float getOuterColorAlpha() {
        return displayData.outerColorAlpha;
    }

    public void setOuterColorAlpha(float outerColorAlpha) {
        displayData.outerColorAlpha = outerColorAlpha;
    }

    public boolean isOuterColorEnabled() {
        return displayData.outerColorEnabled;
    }

    public void setOuterColorEnabled(boolean outerColorEnabled) {
        displayData.outerColorEnabled = outerColorEnabled;
    }

    public float getRotationSpeed() {
        return displayData.rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        displayData.rotationSpeed = rotationSpeed;
    }

    public boolean hasLightningEffect() {
        return lightningData.lightningEffect;
    }

    public void setLightningEffect(boolean lightningEffect) {
        lightningData.lightningEffect = lightningEffect;
    }

    public float getLightningDensity() {
        return lightningData.lightningDensity;
    }

    public void setLightningDensity(float lightningDensity) {
        lightningData.lightningDensity = lightningDensity;
    }

    public float getLightningRadius() {
        return lightningData.lightningRadius;
    }

    public void setLightningRadius(float lightningRadius) {
        lightningData.lightningRadius = lightningRadius;
    }

    public int getLightningFadeTime() {
        return lightningData.lightningFadeTime;
    }

    public void setLightningFadeTime(int lightningFadeTime) {
        lightningData.lightningFadeTime = lightningFadeTime;
    }

    public AnchorPoint getAnchorPointEnum() { return anchorData.anchorPoint; }

    public float getAnchorOffsetX() { return anchorData.anchorOffsetX; }

    public float getAnchorOffsetY() { return anchorData.anchorOffsetY; }

    public float getAnchorOffsetZ() { return anchorData.anchorOffsetZ; }

    public void setAnchorPointEnum(AnchorPoint anchorPoint) { this.anchorData.anchorPoint = anchorPoint; }

    public void setAnchorOffsetX(float x) { this.anchorData.anchorOffsetX = x; }

    public void setAnchorOffsetY(float y) { this.anchorData.anchorOffsetY = y; }

    public void setAnchorOffsetZ(float z) { this.anchorData.anchorOffsetZ = z; }

    public int getAnchorPoint() {
        return anchorData.anchorPoint.ordinal();
    }

    @Override
    public void setAnchorPoint(int point) {
        this.anchorData.anchorPoint = AnchorPoint.fromOrdinal(point);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityOrb orb = new EntityAbilityOrb(npc.worldObj);
        orb.setupPreview(npc, orbSize, displayData, lightningData, anchorData, windUpTicks);
        return orb;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            // Type tab
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("stats.speed", this::getOrbSpeed, this::setOrbSpeed)
            ),
            FieldDef.row(
                FieldDef.floatField("stats.size", this::getOrbSize, this::setOrbSize),
                FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback)
            ),
            FieldDef.row(
                FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
                FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
            ),
            FieldDef.section("ability.section.homing"),
            FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming).hover("ability.hover.homing"),
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength).visibleWhen(this::isHoming),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),


            // Visual tab
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class, this::getAnchorPointEnum, this::setAnchorPointEnum)
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.colors").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor).tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled).tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
                .tab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled),
            FieldDef.row(
                FieldDef.floatField("ability.outerWidth", this::getOuterColorWidth, this::setOuterColorWidth)
                    .visibleWhen(this::isOuterColorEnabled),
                FieldDef.floatField("ability.outerAlpha", this::getOuterColorAlpha, this::setOuterColorAlpha)
                    .range(0, 1).visibleWhen(this::isOuterColorEnabled)
            ).tab("ability.tab.visual"),
            FieldDef.section("ability.section.effects").tab("ability.tab.visual"),
            FieldDef.floatField("ability.rotationSpeed", this::getRotationSpeed, this::setRotationSpeed).tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning", this::hasLightningEffect, this::setLightningEffect).tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
                    .visibleWhen(this::hasLightningEffect).range(0.01f, 100f),
                FieldDef.floatField("gui.radius", this::getLightningRadius, this::setLightningRadius)
                    .range(0.1f, 100f).visibleWhen(this::hasLightningEffect)
            ).tab("ability.tab.visual")
        ));
    }
}
