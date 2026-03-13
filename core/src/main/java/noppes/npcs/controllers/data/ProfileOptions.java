package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.IProfileOptions;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.core.NBT;
import noppes.npcs.constants.EnumProfileSync;

public class ProfileOptions implements IProfileOptions {
    public boolean enableOptions = false;

    public EnumProfileSync cooldownControl = EnumProfileSync.Individual;
    public EnumProfileSync completeControl = EnumProfileSync.Individual;

    public void readFromNBT(INBTCompound compound) {
        // Party Management
        enableOptions = compound.getBoolean("EnableProfiles");
        if (enableOptions) {
            cooldownControl = EnumProfileSync.values()[compound.getInteger("CooldownControl")];
            completeControl = EnumProfileSync.values()[compound.getInteger("CompleteControl")];
        } else {
            if (compound.hasKey("CooldownControl")) {
                compound.removeTag("CooldownControl");
            }
            if (compound.hasKey("CompleteControl")) {
                compound.removeTag("CompleteControl");
            }

            cooldownControl = EnumProfileSync.Individual;
            completeControl = EnumProfileSync.Individual;
        }
    }

    public INBTCompound writeToNBT() {
        INBTCompound compound = NBT.compound();
        compound.setBoolean("EnableProfiles", enableOptions);
        if (enableOptions) {
            compound.setInteger("CooldownControl", cooldownControl.ordinal());
            compound.setInteger("CompleteControl", completeControl.ordinal());
        } else {
            if (compound.hasKey("CooldownControl")) {
                compound.removeTag("CooldownControl");
            }
            if (compound.hasKey("CompleteControl")) {
                compound.removeTag("CompleteControl");
            }
        }

        return compound;
    }

    public boolean hasProfileOptions() {
        return enableOptions;
    }

    public void setProfileOptions(boolean enable) {
        this.enableOptions = enable;
    }

    public void setCooldownControl(int profileType) {
        if (profileType < 0 || profileType >= EnumProfileSync.values().length) {
            return;
        }
        this.cooldownControl = EnumProfileSync.values()[profileType];
    }

    public int getCooldownControl() {
        return this.cooldownControl.ordinal();
    }

    public void setCompleteControl(int profileType) {
        if (profileType < 0 || profileType >= EnumProfileSync.values().length) {
            return;
        }
        this.completeControl = EnumProfileSync.values()[profileType];
    }

    public int getCompleteControl() {
        return this.completeControl.ordinal();
    }
}
