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

/**
 * Disc ability: Spawns a flat spinning disc projectile.
 * Has optional boomerang behavior to return to owner.
 */
public class AbilityDisc extends Ability implements IAbilityDisc {

    // Disc geometry
    private float discRadius = 1.0f;
    private float discThickness = 0.2f;
    private boolean vertical = false; // false = horizontal (flat), true = vertical (thin edge forward)

    // Boomerang properties
    private boolean boomerang = false;
    private int boomerangDelay = 40;

    // Energy data classes
    private final EnergyDisplayData colorData = new EnergyDisplayData(0xFFFFFF, 0xFF8800, true, 0.4f, 0.5f, 5.0f);
    private final EnergyCombatData combatData = new EnergyCombatData(8.0f, 1.2f, 0.15f, false, 3.0f, 0.5f);
    private final EnergyHomingData homingData = new EnergyHomingData(0.6f, true, 0.12f, 18.0f);
    private final EnergyLightningData lightningData = new EnergyLightningData();
    private final EnergyLifespanData lifespanData = new EnergyLifespanData(35.0f, 200);
    private final EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.RIGHT_HAND);
    private final EnergyTrajectoryData trajectoryData = new EnergyTrajectoryData();

    // Transient state for disc entity (used during windup charging)
    private transient EntityAbilityDisc discEntity = null;

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
        // Default built-in animations
        this.windUpAnimationName = "Ability_Disc_Windup";
        this.activeAnimationName = "Ability_Disc_Active";
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
        if (world.isRemote && !isPreview()) {
            signalCompletion();
            return;
        }

        // Start moving the disc that was spawned during windup
        if (discEntity != null && !discEntity.isDead) {
            if (isPreview()) {
                discEntity.startPreviewFiring();
            } else {
                discEntity.startMoving(target);
            }
        }
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote && !isPreview()) return;

        // Spawn disc in charging mode on first tick of windup
        if (tick == 1) {
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, anchorData);
            discEntity = new EntityAbilityDisc(
                world, caster, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
                discRadius, discThickness,
                colorData, combatData, homingData, lightningData, lifespanData, trajectoryData,
                boomerang, boomerangDelay);

            if (isPreview()) {
                discEntity.setupPreview(caster, discRadius, discThickness, colorData, lightningData, anchorData, windUpTicks, vertical);
            } else {
                discEntity.setupCharging(anchorData, windUpTicks, vertical);
            }

            discEntity.setEffects(this.effects);
            discEntity.setSourceAbility(this);
            spawnAbilityEntity(world, discEntity);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (discEntity == null || discEntity.isDead) {
            discEntity = null;
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
        // Despawn disc entity if still alive
        if (discEntity != null && !discEntity.isDead) {
            discEntity.setDead();
        }
        discEntity = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || target == null) {
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
        anchorData.writeNBT(nbt);
        colorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.discRadius = nbt.hasKey("discRadius") ? nbt.getFloat("discRadius") : 1.0f;
        this.discThickness = nbt.hasKey("discThickness") ? nbt.getFloat("discThickness") : 0.2f;
        this.vertical = nbt.hasKey("vertical") && nbt.getBoolean("vertical");
        this.boomerang = nbt.hasKey("boomerang") && nbt.getBoolean("boomerang");
        this.boomerangDelay = nbt.hasKey("boomerangDelay") ? nbt.getInteger("boomerangDelay") : 40;
        anchorData.readNBT(nbt);
        colorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);
    }

    // Getters & Setters
    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { homingData.speed = speed; }
    public float getDiscRadius() { return discRadius; }
    public void setDiscRadius(float discRadius) { this.discRadius = discRadius; }
    public float getDiscThickness() { return discThickness; }
    public void setDiscThickness(float discThickness) { this.discThickness = discThickness; }
    public boolean isVertical() { return vertical; }
    public void setVertical(boolean vertical) { this.vertical = vertical; }
    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float maxDistance) { lifespanData.maxDistance = maxDistance; }
    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { lifespanData.maxLifetime = maxLifetime; }
    public float getDamage() { return combatData.damage; }
    public void setDamage(float damage) { combatData.damage = damage; }
    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { combatData.knockback = knockback; }
    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { combatData.knockbackUp = knockbackUp; }
    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean homing) { homingData.homing = homing; }
    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float homingStrength) { homingData.homingStrength = homingStrength; }
    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float homingRange) { homingData.homingRange = homingRange; }
    public boolean isBoomerang() { return boomerang; }
    public void setBoomerang(boolean boomerang) { this.boomerang = boomerang; }
    public int getBoomerangDelay() { return boomerangDelay; }
    public void setBoomerangDelay(int boomerangDelay) { this.boomerangDelay = boomerangDelay; }
    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean explosive) { combatData.explosive = explosive; }
    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { combatData.explosionRadius = explosionRadius; }
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { combatData.explosionDamageFalloff = explosionDamageFalloff; }
    public int getInnerColor() { return colorData.innerColor; }
    public void setInnerColor(int innerColor) { colorData.innerColor = innerColor; }
    public int getOuterColor() { return colorData.outerColor; }
    public void setOuterColor(int outerColor) { colorData.outerColor = outerColor; }
    public float getOuterColorWidth() { return colorData.outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { colorData.outerColorWidth = outerColorWidth; }
    public float getOuterColorAlpha() { return colorData.outerColorAlpha; }
    public void setOuterColorAlpha(float outerColorAlpha) { colorData.outerColorAlpha = outerColorAlpha; }
    public boolean isOuterColorEnabled() { return colorData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { colorData.outerColorEnabled = outerColorEnabled; }
    public float getRotationSpeed() { return colorData.rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { colorData.rotationSpeed = rotationSpeed; }
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { lightningData.lightningEffect = lightningEffect; }
    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float lightningDensity) { lightningData.lightningDensity = lightningDensity; }
    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float lightningRadius) { lightningData.lightningRadius = lightningRadius; }
    public AnchorPoint getAnchorPointEnum() { return anchorData.anchorPoint; }
    public float getAnchorOffsetX() { return anchorData.anchorOffsetX; }
    public float getAnchorOffsetY() { return anchorData.anchorOffsetY; }
    public float getAnchorOffsetZ() { return anchorData.anchorOffsetZ; }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { this.anchorData.anchorPoint = anchorPoint; }
    public void setAnchorOffsetX(float x) { this.anchorData.anchorOffsetX = x; }
    public void setAnchorOffsetY(float y) { this.anchorData.anchorOffsetY = y; }
    public void setAnchorOffsetZ(float z) { this.anchorData.anchorOffsetZ = z; }

    @Override
    public int getAnchorPoint() { return anchorData.anchorPoint.ordinal(); }

    @Override
    public void setAnchorPoint(int point) { this.anchorData.anchorPoint = AnchorPoint.fromOrdinal(point); }

    @Override
    public int getMaxPreviewDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            // Type tab
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("stats.speed", this::getSpeed, this::setSpeed)
            ),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
            FieldDef.section("ability.section.disc"),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getDiscRadius, this::setDiscRadius),
                FieldDef.floatField("gui.thickness", this::getDiscThickness, this::setDiscThickness)
            ),
            FieldDef.boolField("ability.vertical", this::isVertical, this::setVertical)
                .hover("ability.hover.vertical"),
            FieldDef.row(
                FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
                FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime)
            ),
            FieldDef.section("ability.section.homing"),
            FieldDef.boolField("gui.enabled", this::isHoming, this::setHoming)
                .hover("ability.hover.homing"),
            FieldDef.floatField("gui.strength", this::getHomingStrength, this::setHomingStrength)
                .visibleWhen(this::isHoming),
            FieldDef.section("ability.section.boomerang"),
            FieldDef.boolField("gui.enabled", this::isBoomerang, this::setBoomerang)
                .hover("ability.hover.boomerang"),
            FieldDef.intField("gui.delay", this::getBoomerangDelay, this::setBoomerangDelay)
                .visibleWhen(this::isBoomerang),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive)
                .hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius)
                .visibleWhen(this::isExplosive),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),
            // Visual tab
            FieldDef.enumField("ability.anchorPoint", AnchorPoint.class, this::getAnchorPointEnum, this::setAnchorPointEnum)
                .tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("ability.anchor.offsetX", this::getAnchorOffsetX, this::setAnchorOffsetX),
                FieldDef.floatField("ability.anchor.offsetY", this::getAnchorOffsetY, this::setAnchorOffsetY)
            ).tab("ability.tab.visual"),
            FieldDef.floatField("ability.anchor.offsetZ", this::getAnchorOffsetZ, this::setAnchorOffsetZ)
                .tab("ability.tab.visual"),
            FieldDef.section("ability.section.colors").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor)
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled)
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
                .tab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled),
            FieldDef.row(
                FieldDef.floatField("ability.outerWidth", this::getOuterColorWidth, this::setOuterColorWidth)
                    .visibleWhen(this::isOuterColorEnabled),
                FieldDef.floatField("ability.outerAlpha", this::getOuterColorAlpha, this::setOuterColorAlpha)
                    .range(0, 1).visibleWhen(this::isOuterColorEnabled)
            ).tab("ability.tab.visual"),
            FieldDef.section("ability.section.effects").tab("ability.tab.visual"),
            FieldDef.floatField("ability.rotationSpeed", this::getRotationSpeed, this::setRotationSpeed)
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning", this::hasLightningEffect, this::setLightningEffect)
                .tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
                    .range(0.01f, 100f).visibleWhen(this::hasLightningEffect),
                FieldDef.floatField("gui.radius", this::getLightningRadius, this::setLightningRadius)
                    .range(0.1f, 100f).visibleWhen(this::hasLightningEffect)
            ).tab("ability.tab.visual")
        ));
    }
}
