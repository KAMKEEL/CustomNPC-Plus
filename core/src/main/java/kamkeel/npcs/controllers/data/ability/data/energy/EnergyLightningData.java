package kamkeel.npcs.controllers.data.ability.data.energy;

import noppes.npcs.api.ability.data.IEnergyLightningData;
import noppes.npcs.api.INbt;

/**
 * Groups lightning visual effect properties for energy abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyLightningData implements IEnergyLightningData {
    public boolean lightningEffect = false;
    public float lightningDensity = 0.15f;
    public float lightningRadius = 0.5f;
    public int lightningFadeTime = 6;

    public EnergyLightningData() {
    }

    public EnergyLightningData(boolean lightningEffect, float lightningDensity,
                               float lightningRadius, int lightningFadeTime) {
        this.lightningEffect = lightningEffect;
        this.lightningDensity = lightningDensity;
        this.lightningRadius = lightningRadius;
        this.lightningFadeTime = lightningFadeTime;
    }

    @Override
    public boolean isLightningEffect() {
        return lightningEffect;
    }

    @Override
    public void setLightningEffect(boolean lightningEffect) {
        this.lightningEffect = lightningEffect;
    }

    @Override
    public float getLightningDensity() {
        return lightningDensity;
    }

    @Override
    public void setLightningDensity(float lightningDensity) {
        this.lightningDensity = lightningDensity;
    }

    @Override
    public float getLightningRadius() {
        return lightningRadius;
    }

    @Override
    public void setLightningRadius(float lightningRadius) {
        this.lightningRadius = lightningRadius;
    }

    @Override
    public int getLightningFadeTime() {
        return lightningFadeTime;
    }

    @Override
    public void setLightningFadeTime(int lightningFadeTime) {
        this.lightningFadeTime = Math.max(1, lightningFadeTime);
    }

    public void writeNBT(INbt nbt) {
        nbt.setBoolean("lightningEffect", lightningEffect);
        nbt.setFloat("lightningDensity", lightningDensity);
        nbt.setFloat("lightningRadius", lightningRadius);
        nbt.setInteger("lightningFadeTime", lightningFadeTime);
    }

    public void readNBT(INbt nbt) {
        lightningEffect = nbt.hasKey("lightningEffect") && nbt.getBoolean("lightningEffect");
        lightningDensity = nbt.hasKey("lightningDensity") ? nbt.getFloat("lightningDensity") : 0.15f;
        lightningRadius = nbt.hasKey("lightningRadius") ? nbt.getFloat("lightningRadius") : 0.5f;
        lightningFadeTime = nbt.hasKey("lightningFadeTime") ? nbt.getInteger("lightningFadeTime") : 6;
        if (lightningFadeTime <= 0) lightningFadeTime = 1;
    }

    public EnergyLightningData copy() {
        return new EnergyLightningData(lightningEffect, lightningDensity, lightningRadius, lightningFadeTime);
    }
}
