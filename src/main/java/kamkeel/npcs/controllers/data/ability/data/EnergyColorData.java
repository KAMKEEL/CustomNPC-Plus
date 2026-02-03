package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Groups visual color properties shared by all energy abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyColorData {
    public int innerColor = 0xFFFFFF;
    public int outerColor = 0x8888FF;
    public boolean outerColorEnabled = true;
    public float outerColorWidth = 0.4f;
    public float outerColorAlpha = 0.5f;
    public float rotationSpeed = 4.0f;

    public EnergyColorData() {}

    public EnergyColorData(int innerColor, int outerColor, boolean outerColorEnabled,
                           float outerColorWidth, float outerColorAlpha, float rotationSpeed) {
        this.innerColor = innerColor;
        this.outerColor = outerColor;
        this.outerColorEnabled = outerColorEnabled;
        this.outerColorWidth = outerColorWidth;
        this.outerColorAlpha = outerColorAlpha;
        this.rotationSpeed = rotationSpeed;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("innerColor", innerColor);
        nbt.setInteger("outerColor", outerColor);
        nbt.setBoolean("outerColorEnabled", outerColorEnabled);
        nbt.setFloat("outerColorWidth", outerColorWidth);
        nbt.setFloat("outerColorAlpha", outerColorAlpha);
        nbt.setFloat("rotationSpeed", rotationSpeed);
    }

    public void readNBT(NBTTagCompound nbt) {
        innerColor = nbt.hasKey("innerColor") ? nbt.getInteger("innerColor") : 0xFFFFFF;
        outerColor = nbt.hasKey("outerColor") ? nbt.getInteger("outerColor") : 0x8888FF;
        outerColorEnabled = !nbt.hasKey("outerColorEnabled") || nbt.getBoolean("outerColorEnabled");
        outerColorWidth = nbt.hasKey("outerColorWidth") ? nbt.getFloat("outerColorWidth") : 0.4f;
        outerColorAlpha = nbt.hasKey("outerColorAlpha") ? nbt.getFloat("outerColorAlpha") : 0.5f;
        rotationSpeed = nbt.hasKey("rotationSpeed") ? nbt.getFloat("rotationSpeed") : 4.0f;
    }

    public EnergyColorData copy() {
        return new EnergyColorData(innerColor, outerColor, outerColorEnabled, outerColorWidth, outerColorAlpha, rotationSpeed);
    }
}
