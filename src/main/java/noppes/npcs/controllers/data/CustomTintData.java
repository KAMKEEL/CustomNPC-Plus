package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class CustomTintData {
    private boolean enableCustomTint = false;
    private boolean enableHurtTint = true;
    private boolean enableNpcTint = false;
    private int colorHurtTint = 0xff0000;
    private int colorNpcTint = 0x000000;
    private int colorNpcTintAlpha = 40;

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("EnableCustomTint", enableCustomTint);
        if(enableCustomTint) {
            nbttagcompound.setBoolean("EnableHurtTint", enableHurtTint);
            nbttagcompound.setBoolean("EnableNpcTint", enableNpcTint);
            nbttagcompound.setInteger("ColorHurtTint", colorHurtTint);
            nbttagcompound.setInteger("ColorNpcTint", colorNpcTint);
            nbttagcompound.setInteger("ColorNpcTintAlpha", colorNpcTintAlpha);
        }
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        enableCustomTint = nbttagcompound.getBoolean("EnableCustomTint");
        if(enableCustomTint) {
            enableHurtTint = nbttagcompound.getBoolean("EnableHurtTint");
            enableNpcTint = nbttagcompound.getBoolean("EnableNpcTint");
            colorHurtTint = nbttagcompound.getInteger("ColorHurtTint");
            colorNpcTint = nbttagcompound.getInteger("ColorNpcTint");
            colorNpcTintAlpha = nbttagcompound.getInteger("ColorNpcTintAlpha");
        }
    }

    public boolean isEnableHurtTint() {
        return enableHurtTint;
    }

    public void setEnableHurtTint(boolean enableHurtTint) {
        this.enableHurtTint = enableHurtTint;
    }

    public int getColorHurtTint() {
        return colorHurtTint;
    }

    public void setColorHurtTint(int colorHurtTint) {
        this.colorHurtTint = colorHurtTint;
    }

    public int getColorNpcTint() {
        return colorNpcTint;
    }

    public void setColorNpcTint(int colorNpcTint) {
        this.colorNpcTint = colorNpcTint;
    }

    public boolean isEnableCustomTint() {
        return enableCustomTint;
    }

    public void setEnableCustomTint(boolean enableCustomTint) {
        this.enableCustomTint = enableCustomTint;
    }

    public boolean isEnableNpcTint() {
        return enableNpcTint;
    }

    public void setEnableNpcTint(boolean enableNpcTint) {
        this.enableNpcTint = enableNpcTint;
    }

    public int getColorNpcTintAlpha() {
        return colorNpcTintAlpha;
    }

    public void setColorNpcTintAlpha(int colorNpcTintAlpha) {
        this.colorNpcTintAlpha = colorNpcTintAlpha;
    }
}
