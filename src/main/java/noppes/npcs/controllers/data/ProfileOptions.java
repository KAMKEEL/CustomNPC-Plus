package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IProfileOptions;
import noppes.npcs.constants.EnumProfileSync;

public class ProfileOptions implements IProfileOptions {
	public boolean enableOptions = false;

    public EnumProfileSync cooldownControl = EnumProfileSync.Individual;
	public EnumProfileSync completeControl = EnumProfileSync.Individual;

    public void readFromNBT(NBTTagCompound compound)
    {
		// Party Management
        enableOptions = compound.getBoolean("EnableProfiles");
		if(enableOptions){
            cooldownControl = EnumProfileSync.values()[compound.getInteger("CooldownControl")];
            completeControl = EnumProfileSync.values()[compound.getInteger("CompleteControl")];
		}
		else {
            if(compound.hasKey("CooldownControl")){
                compound.removeTag("CooldownControl");
            }
			if(compound.hasKey("CompleteControl")){
				compound.removeTag("CompleteControl");
			}

            cooldownControl = EnumProfileSync.Individual;
            completeControl = EnumProfileSync.Individual;
		}
    }
	public NBTTagCompound writeToNBT()
    {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("EnableProfiles", enableOptions);
		if(enableOptions){
			compound.setInteger("CooldownControl", cooldownControl.ordinal());
			compound.setInteger("CompleteControl", completeControl.ordinal());
		}
		else {
            if(compound.hasKey("CooldownControl")){
                compound.removeTag("CooldownControl");
            }
			if(compound.hasKey("CompleteControl")){
				compound.removeTag("CompleteControl");
			}
		}

		return compound;
    }

    @Override
	public boolean hasProfileOptions() {
		return enableOptions;
	}

    @Override
	public void setProfileOptions(boolean enable) {
		this.enableOptions = enable;
	}

    @Override
	public void setCooldownControl(int profileType) {
		if (profileType < 0 || profileType >= EnumProfileSync.values().length) {
			return;
		}
		this.cooldownControl = EnumProfileSync.values()[profileType];
	}

    @Override
    public int getCooldownControl(){
        return this.cooldownControl.ordinal();
    }

    @Override
    public void setCompleteControl(int profileType) {
        if (profileType < 0 || profileType >= EnumProfileSync.values().length) {
            return;
        }
        this.completeControl = EnumProfileSync.values()[profileType];
    }

    @Override
    public int getCompleteControl(){
        return this.completeControl.ordinal();
    }
}
