package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyColorData;
import kamkeel.npcs.controllers.data.ability.gui.ColumnHint;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityLaser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.api.ability.type.IAbilityLaserShot;

import java.util.Arrays;
import java.util.List;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Laser Shot ability: Fast expanding thin line that pierces through targets.
 * Travels in a straight line from origin, damaging all entities it passes through.
 */
public class AbilityLaserShot extends Ability implements IAbilityLaserShot {

    // Laser properties
    private float laserWidth = 0.3f;
    private float expansionSpeed = 3.0f;
    private int lingerTicks = 8;

    // Data classes
    private EnergyColorData colorData = new EnergyColorData(0xFFFFFF, 0xFF0000, true, 0.4f, 0.5f, 0.0f);
    private EnergyCombatData combatData = new EnergyCombatData(6.0f, 0.5f, 0.05f, false, 2.0f, 0.5f);
    private EnergyLightningData lightningData = new EnergyLightningData();
    private EnergyLifespanData lifespanData = new EnergyLifespanData(40.0f, 100);

    // Transient state for laser entity (used for movement locking)
    private transient EntityAbilityLaser laserEntity = null;

    public AbilityLaserShot() {
        this.typeId = "ability.cnpc.laser_shot";
        this.name = "Laser Shot";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 35.0f;
        this.minRange = 3.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 15;
        this.lockMovement = LockMovementType.WINDUP;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        // Default built-in animations
        this.windUpAnimationName = "Ability_Laser_Windup";
        this.activeAnimationName = "Ability_Laser_Active";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return false;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public float getTelegraphLength() {
        return lifespanData.maxDistance;
    }

    @Override
    public float getTelegraphWidth() {
        return laserWidth * 2.0f; // Make telegraph slightly wider for visibility
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        double spawnX = npc.posX;
        double spawnY = npc.posY + npc.getEyeHeight() * 0.8;
        double spawnZ = npc.posZ;

        laserEntity = new EntityAbilityLaser(
            world, npc, target,
            spawnX, spawnY, spawnZ,
            laserWidth,
            colorData, combatData, lightningData, lifespanData,
            expansionSpeed, lingerTicks
        );

        laserEntity.setEffects(this.effects);
        world.spawnEntityInWorld(laserEntity);

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (laserEntity == null || laserEntity.isDead) {
            laserEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
    }

    @Override
    public void cleanup() {
        // Despawn laser entity if still alive
        if (laserEntity != null && !laserEntity.isDead) {
            laserEntity.setDead();
        }
        laserEntity = null;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("laserWidth", laserWidth);
        nbt.setFloat("expansionSpeed", expansionSpeed);
        nbt.setInteger("lingerTicks", lingerTicks);
        colorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.laserWidth = nbt.hasKey("laserWidth") ? nbt.getFloat("laserWidth") : 0.3f;
        this.expansionSpeed = nbt.hasKey("expansionSpeed") ? nbt.getFloat("expansionSpeed") : 3.0f;
        this.lingerTicks = nbt.hasKey("lingerTicks") ? nbt.getInteger("lingerTicks") : 8;
        colorData.readNBT(nbt);
        combatData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);
    }

    // Getters & Setters - Standalone fields
    public float getLaserWidth() { return laserWidth; }
    public void setLaserWidth(float laserWidth) { this.laserWidth = laserWidth; }
    public float getExpansionSpeed() { return expansionSpeed; }
    public void setExpansionSpeed(float expansionSpeed) { this.expansionSpeed = expansionSpeed; }
    public int getLingerTicks() { return lingerTicks; }
    public void setLingerTicks(int lingerTicks) { this.lingerTicks = lingerTicks; }

    // Getters & Setters - Lifespan data
    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float maxDistance) { this.lifespanData.maxDistance = maxDistance; }
    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.lifespanData.maxLifetime = maxLifetime; }

    // Getters & Setters - Combat data
    public float getDamage() { return combatData.damage; }
    public void setDamage(float damage) { this.combatData.damage = damage; }
    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { this.combatData.knockback = knockback; }
    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.combatData.knockbackUp = knockbackUp; }
    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean explosive) { this.combatData.explosive = explosive; }
    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.combatData.explosionRadius = explosionRadius; }
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { this.combatData.explosionDamageFalloff = explosionDamageFalloff; }
    // Getters & Setters - Color data
    public int getInnerColor() { return colorData.innerColor; }
    public void setInnerColor(int innerColor) { this.colorData.innerColor = innerColor; }
    public int getOuterColor() { return colorData.outerColor; }
    public void setOuterColor(int outerColor) { this.colorData.outerColor = outerColor; }
    public float getOuterColorWidth() { return colorData.outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { this.colorData.outerColorWidth = outerColorWidth; }
    public float getOuterColorAlpha() { return colorData.outerColorAlpha; }
    public void setOuterColorAlpha(float outerColorAlpha) { this.colorData.outerColorAlpha = outerColorAlpha; }
    public boolean isOuterColorEnabled() { return colorData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { this.colorData.outerColorEnabled = outerColorEnabled; }

    // Getters & Setters - Lightning data
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { this.lightningData.lightningEffect = lightningEffect; }
    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float lightningDensity) { this.lightningData.lightningDensity = lightningDensity; }
    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float lightningRadius) { this.lightningData.lightningRadius = lightningRadius; }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityLaser laser = new EntityAbilityLaser(npc.worldObj);
        laser.setupPreview(npc, laserWidth, colorData, lightningData, expansionSpeed, lifespanData.maxDistance);
        return laser;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 60) : 60;
    }

    @Override
    public boolean spawnPreviewDuringWindup() {
        return false; // Laser has no charging, spawns at active phase
    }

    @Override
    public List<FieldDef> getFieldDefinitions() {
        return Arrays.asList(
            // Type tab
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.knockbackUp", this::getKnockbackUp, this::setKnockbackUp).column(ColumnHint.RIGHT),
            FieldDef.section("ability.section.beam"),
            FieldDef.floatField("ability.laserWidth", this::getLaserWidth, this::setLaserWidth).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.expansionSpeed", this::getExpansionSpeed, this::setExpansionSpeed).column(ColumnHint.RIGHT),
            FieldDef.intField("ability.lingerTicks", this::getLingerTicks, this::setLingerTicks).column(ColumnHint.LEFT),
            FieldDef.intField("ability.lifetime", this::getMaxLifetime, this::setMaxLifetime).column(ColumnHint.RIGHT),
            FieldDef.floatField("ability.maxDistance", this::getMaxDistance, this::setMaxDistance),
            FieldDef.section("ability.section.explosive"),
            FieldDef.boolField("gui.enabled", this::isExplosive, this::setExplosive).hover("ability.hover.explosive"),
            FieldDef.floatField("gui.radius", this::getExplosionRadius, this::setExplosionRadius).visibleWhen(this::isExplosive),
            FieldDef.effectsListField("ability.effects", this::getEffects, this::setEffects),
            // Visual tab
            FieldDef.section("ability.section.colors").customTab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor).customTab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled).customTab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
                .customTab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled),
            FieldDef.floatField("ability.outerWidth", this::getOuterColorWidth, this::setOuterColorWidth)
                .customTab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.outerAlpha", this::getOuterColorAlpha, this::setOuterColorAlpha)
                .customTab("ability.tab.visual").range(0, 1).visibleWhen(this::isOuterColorEnabled).column(ColumnHint.RIGHT),
            FieldDef.section("ability.section.effects").customTab("ability.tab.visual"),
            FieldDef.boolField("ability.lightning", this::hasLightningEffect, this::setLightningEffect).customTab("ability.tab.visual"),
            FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
                .customTab("ability.tab.visual").range(0.01f, 5.0f).visibleWhen(this::hasLightningEffect).column(ColumnHint.LEFT),
            FieldDef.floatField("gui.radius", this::getLightningRadius, this::setLightningRadius)
                .customTab("ability.tab.visual").range(0.1f, 10.0f).visibleWhen(this::hasLightningEffect).column(ColumnHint.RIGHT)
        );
    }
}
