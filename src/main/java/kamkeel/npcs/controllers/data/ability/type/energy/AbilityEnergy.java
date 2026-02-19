package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * Abstract base for all energy-based abilities (Projectile, Barrier, Sweeper, etc.).
 * Provides shared visual data (colors, lightning) and corresponding API/GUI/NBT.
 */
public abstract class AbilityEnergy extends Ability {

    protected EnergyDisplayData displayData;
    protected EnergyLightningData lightningData;

    protected AbilityEnergy(EnergyDisplayData displayData) {
        this.displayData = displayData;
        this.lightningData = new EnergyLightningData();
    }

    // ==================== DISPLAY DATA ====================

    public int getInnerColor() {
        return displayData.innerColor;
    }

    public void setInnerColor(int color) {
        displayData.innerColor = color;
    }

    public float getInnerAlpha() {
        return displayData.innerAlpha;
    }

    public void setInnerAlpha(float alpha) {
        displayData.innerAlpha = alpha;
    }

    public int getOuterColor() {
        return displayData.outerColor;
    }

    public void setOuterColor(int color) {
        displayData.outerColor = color;
    }

    public boolean isOuterColorEnabled() {
        return displayData.outerColorEnabled;
    }

    public void setOuterColorEnabled(boolean enabled) {
        displayData.outerColorEnabled = enabled;
    }

    public float getOuterColorWidth() {
        return displayData.outerColorWidth;
    }

    public void setOuterColorWidth(float width) {
        displayData.outerColorWidth = width;
    }

    public float getOuterColorAlpha() {
        return displayData.outerColorAlpha;
    }

    public void setOuterColorAlpha(float alpha) {
        displayData.outerColorAlpha = alpha;
    }

    public float getRotationSpeed() {
        return displayData.rotationSpeed;
    }

    public void setRotationSpeed(float speed) {
        displayData.rotationSpeed = speed;
    }

    // ==================== LIGHTNING DATA ====================

    public boolean hasLightningEffect() {
        return lightningData.lightningEffect;
    }

    public void setLightningEffect(boolean enabled) {
        lightningData.lightningEffect = enabled;
    }

    public float getLightningDensity() {
        return lightningData.lightningDensity;
    }

    public void setLightningDensity(float density) {
        lightningData.lightningDensity = density;
    }

    public float getLightningRadius() {
        return lightningData.lightningRadius;
    }

    public void setLightningRadius(float radius) {
        lightningData.lightningRadius = radius;
    }

    public int getLightningFadeTime() {
        return lightningData.lightningFadeTime;
    }

    public void setLightningFadeTime(int fadeTime) {
        lightningData.lightningFadeTime = fadeTime;
    }

    // ==================== NBT HELPERS ====================

    protected void writeEnergyNBT(NBTTagCompound nbt) {
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
    }

    protected void readEnergyNBT(NBTTagCompound nbt) {
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
    }

    // ==================== GUI HELPERS ====================

    /**
     * Add complete visual tab definitions (colors section + effects/lightning section).
     * Subclasses can call this directly or use the individual methods for more control.
     */
    @SideOnly(Side.CLIENT)
    protected void addEnergyVisualDefinitions(List<FieldDef> defs) {
        addEnergyColorDefinitions(defs);
        addEnergyEffectDefinitions(defs);
    }

    /**
     * Add the colors section to the visual tab.
     */
    @SideOnly(Side.CLIENT)
    protected void addEnergyColorDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.section("ability.section.colors").tab("ability.tab.visual"));
        defs.add(FieldDef.row(
            FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor),
            FieldDef.floatField("ability.innerAlpha", this::getInnerAlpha, this::setInnerAlpha).range(0, 1)
        ).tab("ability.tab.visual"));
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
    }

    /**
     * Add the effects section (lightning) to the visual tab.
     * Override to insert additional fields before lightning (e.g., rotationSpeed).
     */
    @SideOnly(Side.CLIENT)
    protected void addEnergyEffectDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.section("ability.section.effects").tab("ability.tab.visual"));
        addEnergyLightningDefinitions(defs);
    }

    /**
     * Add just the lightning fields (no section header) to the visual tab.
     */
    @SideOnly(Side.CLIENT)
    protected void addEnergyLightningDefinitions(List<FieldDef> defs) {
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
