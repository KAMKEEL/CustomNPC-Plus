package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;
import java.util.Map;

/**
 * Abstract base for energy barrier abilities (Dome, Wall, Shield).
 * Handles shared visual data, barrier configuration, and lifecycle management.
 */
public abstract class AbstractEnergyBarrierAbility extends Ability {

    protected EnergyDisplayData displayData;
    protected EnergyLightningData lightningData;
    protected final EnergyBarrierData barrierData;

    // Runtime entity tracking
    protected transient Entity barrierEntity;

    protected AbstractEnergyBarrierAbility(EnergyDisplayData displayData, EnergyBarrierData barrierData) {
        this.displayData = displayData;
        this.barrierData = barrierData;
        this.lightningData = new EnergyLightningData();
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Create and spawn the barrier entity during execution.
     */
    protected abstract Entity createBarrierEntity(EntityLivingBase caster, EntityLivingBase target);

    /**
     * Add type-specific GUI field definitions.
     */
    @SideOnly(Side.CLIENT)
    protected abstract void addBarrierTypeDefinitions(List<FieldDef> defs);

    /**
     * Write type-specific fields to NBT.
     */
    protected abstract void writeBarrierTypeNBT(NBTTagCompound nbt);

    /**
     * Read type-specific fields from NBT.
     */
    protected abstract void readBarrierTypeNBT(NBTTagCompound nbt);

    // ==================== LIFECYCLE ====================

    @Override
    public boolean allowBurst() {
        return false;
    }

    @Override
    public boolean hasDamage() {
        return false; // Barriers are defensive
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        barrierEntity = createBarrierEntity(caster, target);
        if (barrierEntity != null) {
            spawnAbilityEntity(barrierEntity);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (barrierEntity == null || barrierEntity.isDead) {
            signalCompletion();
            return;
        }

        // Duration tracked by entity itself, but check here too as a failsafe
        if (barrierData.useDuration && tick >= barrierData.durationTicks) {
            cleanup();
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
        if (barrierEntity != null && !barrierEntity.isDead) {
            barrierEntity.setDead();
        }
        barrierEntity = null;
    }

    // ==================== NBT ====================

    @Override
    public final void writeTypeNBT(NBTTagCompound nbt) {
        writeBarrierTypeNBT(nbt);
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        barrierData.writeNBT(nbt);
    }

    @Override
    public final void readTypeNBT(NBTTagCompound nbt) {
        readBarrierTypeNBT(nbt);
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
        barrierData.readNBT(nbt);
    }

    // ==================== API GETTERS & SETTERS ====================

    // Display data
    public int getInnerColor() { return displayData.innerColor; }
    public void setInnerColor(int color) { displayData.innerColor = color; }

    public int getOuterColor() { return displayData.outerColor; }
    public void setOuterColor(int color) { displayData.outerColor = color; }

    public boolean isOuterColorEnabled() { return displayData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean enabled) { displayData.outerColorEnabled = enabled; }

    public float getOuterColorWidth() { return displayData.outerColorWidth; }
    public void setOuterColorWidth(float width) { displayData.outerColorWidth = width; }

    public float getOuterColorAlpha() { return displayData.outerColorAlpha; }
    public void setOuterColorAlpha(float alpha) { displayData.outerColorAlpha = alpha; }

    // Lightning data
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean enabled) { lightningData.lightningEffect = enabled; }

    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float density) { lightningData.lightningDensity = density; }

    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float radius) { lightningData.lightningRadius = radius; }

    public int getLightningFadeTime() { return lightningData.lightningFadeTime; }
    public void setLightningFadeTime(int fadeTime) { lightningData.lightningFadeTime = fadeTime; }

    // Barrier data
    public float getBarrierMaxHealth() { return barrierData.maxHealth; }
    public void setBarrierMaxHealth(float maxHealth) { barrierData.setMaxHealth(maxHealth); }

    public boolean isUseHealth() { return barrierData.useHealth; }
    public void setUseHealth(boolean useHealth) { barrierData.useHealth = useHealth; }

    public int getBarrierDuration() { return barrierData.durationTicks; }
    public void setBarrierDuration(int ticks) { barrierData.setDurationTicks(ticks); }

    public boolean isUseDuration() { return barrierData.useDuration; }
    public void setUseDuration(boolean useDuration) { barrierData.useDuration = useDuration; }

    public float getDefaultMultiplier() { return barrierData.defaultMultiplier; }
    public void setDefaultMultiplier(float mult) { barrierData.defaultMultiplier = mult; }

    public void setDamageMultiplier(String typeId, float mult) { barrierData.setMultiplier(typeId, mult); }
    public float getDamageMultiplier(String typeId) { return barrierData.getMultiplier(typeId); }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        // Type-specific fields first
        addBarrierTypeDefinitions(defs);

        // Barrier section
        defs.add(FieldDef.section("ability.section.barrier"));
        defs.add(FieldDef.boolField("ability.useHealth", this::isUseHealth, this::setUseHealth));
        defs.add(FieldDef.floatField("ability.maxHealth", this::getBarrierMaxHealth, this::setBarrierMaxHealth)
            .visibleWhen(this::isUseHealth));
        defs.add(FieldDef.boolField("ability.useDuration", this::isUseDuration, this::setUseDuration));
        defs.add(FieldDef.intField("ability.duration", this::getBarrierDuration, this::setBarrierDuration)
            .range(1, 6000).visibleWhen(this::isUseDuration));
        defs.add(FieldDef.floatField("ability.defaultMultiplier", this::getDefaultMultiplier, this::setDefaultMultiplier));

        // Visual tab
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

        defs.add(FieldDef.section("ability.section.effects").tab("ability.tab.visual"));
        defs.add(FieldDef.boolField("ability.lightning", this::hasLightningEffect, this::setLightningEffect)
            .tab("ability.tab.visual"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
                .visibleWhen(this::hasLightningEffect).range(0.01f, 100f),
            FieldDef.floatField("gui.radius", this::getLightningRadius, this::setLightningRadius)
                .range(0.1f, 100f).visibleWhen(this::hasLightningEffect)
        ).tab("ability.tab.visual"));
    }
}
