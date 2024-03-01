package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class CustomTintData {
    private boolean tintEnabled = false;
    private boolean hurtTintEnabled = true;
    private boolean generalTintEnabled = false;
    private int hurtTint = 0xff0000;
    private int generalTint = 0x000000;
    private int generalAlpha = 40;

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("TintEnabled", tintEnabled);
        if(tintEnabled) {
            nbttagcompound.setBoolean("HurtTintEnabled", hurtTintEnabled);
            nbttagcompound.setBoolean("GeneralTintEnabled", generalTintEnabled);
            nbttagcompound.setInteger("HurtTint", hurtTint);
            nbttagcompound.setInteger("GeneralTint", generalTint);
            nbttagcompound.setInteger("GeneralAlpha", generalAlpha);
        }
        return nbttagcompound;
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        tintEnabled = nbttagcompound.getBoolean("TintEnabled");
        if(tintEnabled) {
            hurtTintEnabled = nbttagcompound.getBoolean("HurtTintEnabled");
            generalTintEnabled = nbttagcompound.getBoolean("GeneralTintEnabled");
            hurtTint = nbttagcompound.getInteger("HurtTint");
            generalTint = nbttagcompound.getInteger("GeneralTint");
            generalAlpha = nbttagcompound.getInteger("GeneralAlpha");
        }
    }

    public boolean isHurtTintEnabled() {
        return hurtTintEnabled;
    }

    public void setHurtTintEnabled(boolean hurtTintEnabled) {
        this.hurtTintEnabled = hurtTintEnabled;
    }

    public int getHurtTint() {
        return hurtTint;
    }

    public void setHurtTint(int colorHurtTint) {
        this.hurtTint = colorHurtTint;
    }

    public int getGeneralTint() {
        return generalTint;
    }

    public void setGeneralTint(int generalTint) {
        this.generalTint = generalTint;
    }

    public boolean isTintEnabled() {
        return tintEnabled;
    }

    public void setTintEnabled(boolean tintEnabled) {
        this.tintEnabled = tintEnabled;
    }

    public boolean isGeneralTintEnabled() {
        return generalTintEnabled;
    }

    public void setGeneralTintEnabled(boolean generalTintEnabled) {
        this.generalTintEnabled = generalTintEnabled;
    }

    public int getGeneralAlpha() {
        return generalAlpha;
    }

    public void setGeneralAlpha(int generalAlpha) {
        this.generalAlpha = generalAlpha;
    }
}
