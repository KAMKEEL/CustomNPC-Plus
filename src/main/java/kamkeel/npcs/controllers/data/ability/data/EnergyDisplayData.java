package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyDisplayData;

/**
 * Groups visual color properties shared by all energy abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyDisplayData implements IEnergyDisplayData {
    public int innerColor = 0xFFFFFF;
    public int outerColor = 0x8888FF;
    public boolean outerColorEnabled = true;
    public float outerColorWidth = 0.4f;
    public float outerColorAlpha = 0.5f;
    public float rotationSpeed = 4.0f;

    public EnergyDisplayData() {}

    public EnergyDisplayData(int innerColor, int outerColor) {
        this.innerColor = innerColor;
        this.outerColor = outerColor;
    }

    public EnergyDisplayData(int innerColor, int outerColor, boolean outerColorEnabled,
                             float outerColorWidth, float outerColorAlpha, float rotationSpeed) {
        this.innerColor = innerColor;
        this.outerColor = outerColor;
        this.outerColorEnabled = outerColorEnabled;
        this.outerColorWidth = outerColorWidth;
        this.outerColorAlpha = outerColorAlpha;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public int getInnerColor() {
        return innerColor;
    }

    @Override
    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
    }

    @Override
    public int getOuterColor() {
        return outerColor;
    }

    @Override
    public void setOuterColor(int outerColor) {
        this.outerColor = outerColor;
    }

    @Override
    public boolean isOuterColorEnabled() {
        return outerColorEnabled;
    }

    @Override
    public void setOuterColorEnabled(boolean outerColorEnabled) {
        this.outerColorEnabled = outerColorEnabled;
    }

    @Override
    public float getOuterColorWidth() {
        return outerColorWidth;
    }

    @Override
    public void setOuterColorWidth(float outerColorWidth) {
        this.outerColorWidth = outerColorWidth;
    }

    @Override
    public float getOuterColorAlpha() {
        return outerColorAlpha;
    }

    @Override
    public void setOuterColorAlpha(float outerColorAlpha) {
        this.outerColorAlpha = outerColorAlpha;
    }

    @Override
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    @Override
    public void setRotationSpeed(float rotationSpeed) {
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
        innerColor = nbt.getInteger("innerColor");
        outerColor = nbt.getInteger("outerColor");
        outerColorEnabled = nbt.getBoolean("outerColorEnabled");
        outerColorWidth = nbt.getFloat("outerColorWidth");
        outerColorAlpha = nbt.getFloat("outerColorAlpha");
        rotationSpeed = nbt.getFloat("rotationSpeed");
    }

    public EnergyDisplayData copy() {
        return new EnergyDisplayData(innerColor, outerColor, outerColorEnabled, outerColorWidth, outerColorAlpha, rotationSpeed);
    }
}
