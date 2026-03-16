package noppes.npcs.controllers.data;


import noppes.npcs.api.entity.data.ITintData;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

public class TintData implements ITintData {
    private boolean tintEnabled = false;
    private boolean hurtTintEnabled = true;
    private boolean generalTintEnabled = false;
    private int hurtTint = 0xff0000;
    private int generalTint = 0x000000;
    private int generalAlpha = 40;

    public INbt writeToNBT(INbt INbt) {
        INbt.setBoolean("TintEnabled", tintEnabled);
        if (tintEnabled) {
            INbt.setBoolean("HurtTintEnabled", hurtTintEnabled);
            INbt.setBoolean("GeneralTintEnabled", generalTintEnabled);
            INbt.setInteger("HurtTint", hurtTint);
            INbt.setInteger("GeneralTint", generalTint);
            INbt.setInteger("GeneralAlpha", generalAlpha);
        }
        return INbt;
    }

    public void readFromNBT(INbt INbt) {
        tintEnabled = INbt.getBoolean("TintEnabled");
        if (tintEnabled) {
            hurtTintEnabled = INbt.getBoolean("HurtTintEnabled");
            generalTintEnabled = INbt.getBoolean("GeneralTintEnabled");
            hurtTint = INbt.getInteger("HurtTint");
            generalTint = INbt.getInteger("GeneralTint");
            generalAlpha = INbt.getInteger("GeneralAlpha");
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

    public boolean processColor(boolean isHurt) {
        if (isHurt) {
            return (isTintEnabled() && (!isHurtTintEnabled() && !isGeneralTintEnabled()));
        } else {
            return !(isTintEnabled() && isGeneralTintEnabled());
        }
    }
}
