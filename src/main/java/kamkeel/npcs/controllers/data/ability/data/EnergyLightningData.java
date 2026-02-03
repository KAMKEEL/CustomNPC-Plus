package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Groups lightning visual effect properties for energy abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyLightningData {
    public boolean lightningEffect = false;
    public float lightningDensity = 0.15f;
    public float lightningRadius = 0.5f;
    public int lightningFadeTime = 6;

    public EnergyLightningData() {}

    public EnergyLightningData(boolean lightningEffect, float lightningDensity,
                               float lightningRadius, int lightningFadeTime) {
        this.lightningEffect = lightningEffect;
        this.lightningDensity = lightningDensity;
        this.lightningRadius = lightningRadius;
        this.lightningFadeTime = lightningFadeTime;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setBoolean("lightningEffect", lightningEffect);
        nbt.setFloat("lightningDensity", lightningDensity);
        nbt.setFloat("lightningRadius", lightningRadius);
        nbt.setInteger("lightningFadeTime", lightningFadeTime);
    }

    public void readNBT(NBTTagCompound nbt) {
        lightningEffect = nbt.hasKey("lightningEffect") && nbt.getBoolean("lightningEffect");
        lightningDensity = nbt.hasKey("lightningDensity") ? nbt.getFloat("lightningDensity") : 0.15f;
        lightningRadius = nbt.hasKey("lightningRadius") ? nbt.getFloat("lightningRadius") : 0.5f;
        lightningFadeTime = nbt.hasKey("lightningFadeTime") ? nbt.getInteger("lightningFadeTime") : 6;
    }

    public EnergyLightningData copy() {
        return new EnergyLightningData(lightningEffect, lightningDensity, lightningRadius, lightningFadeTime);
    }
}
