package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IPartyOptions;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumPartyRequirements;

public class PartyOptions implements IPartyOptions {
	public boolean allowParty = false;

    public EnumPartyRequirements partyRequirements = EnumPartyRequirements.Leader;

    public EnumPartyExchange rewardControl = EnumPartyExchange.Leader;
	public EnumPartyExchange completeFor = EnumPartyExchange.Leader;

    public EnumPartyObjectives objectiveRequirement = EnumPartyObjectives.All;
	public int maxPartySize = ConfigMain.DefaultMaxPartySize;

    public void readFromNBT(NBTTagCompound compound)
    {
		// Party Management
		allowParty = compound.getBoolean("AllowParty");
		if(allowParty){
			partyRequirements = EnumPartyRequirements.values()[compound.getInteger("PartyRequirements")];
			rewardControl = EnumPartyExchange.values()[compound.getInteger("RewardControl")];
			completeFor = EnumPartyExchange.values()[compound.getInteger("CompleteFor")];
			maxPartySize = compound.getInteger("MaxPartySize");
            objectiveRequirement = EnumPartyObjectives.values()[compound.getInteger("ObjectiveRequirement")];
		}
		else {
			if(compound.hasKey("PartyRequirements")){
				compound.removeTag("PartyRequirements");
			}
			if(compound.hasKey("RewardControl")){
				compound.removeTag("RewardControl");
			}
			if(compound.hasKey("CompleteFor")){
				compound.removeTag("CompleteFor");
			}
			if(compound.hasKey("MaxPartySize")){
				compound.removeTag("MaxPartySize");
			}
            if(compound.hasKey("ObjectiveRequirement")){
                compound.removeTag("ObjectiveRequirement");
            }

            partyRequirements = EnumPartyRequirements.Leader;
			rewardControl = EnumPartyExchange.Leader;
			completeFor = EnumPartyExchange.Leader;
			maxPartySize = ConfigMain.DefaultMaxPartySize;
            objectiveRequirement = EnumPartyObjectives.All;
		}
    }
	public NBTTagCompound writeToNBT()
    {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("AllowParty", allowParty);
		if(allowParty){
			compound.setInteger("PartyRequirements", partyRequirements.ordinal());
			compound.setInteger("RewardControl", rewardControl.ordinal());
			compound.setInteger("CompleteFor", completeFor.ordinal());
			compound.setInteger("MaxPartySize", maxPartySize);
            compound.setInteger("ObjectiveRequirement", objectiveRequirement.ordinal());
		}
		else {
			if(compound.hasKey("PartyRequirements")){
				compound.removeTag("PartyRequirements");
			}
			if(compound.hasKey("RewardControl")){
				compound.removeTag("RewardControl");
			}
			if(compound.hasKey("CompleteFor")){
				compound.removeTag("CompleteFor");
			}
			if(compound.hasKey("MaxPartySize")){
				compound.removeTag("MaxPartySize");
			}
            if(compound.hasKey("ObjectiveRequirement")){
                compound.removeTag("ObjectiveRequirement");
            }
		}

		return compound;
    }

    @Override
	public boolean isAllowParty() {
		return allowParty;
	}

    @Override
	public void setAllowParty(boolean allowParty) {
		this.allowParty = allowParty;
	}

    @Override
	public int getPartyRequirements() {
		return partyRequirements.ordinal();
	}

    @Override
	public void setPartyRequirements(int partyReq) {
		if (partyReq < 0 || partyReq >= EnumPartyRequirements.values().length) {
			return;
		}
		this.partyRequirements = EnumPartyRequirements.values()[partyReq];
	}

    @Override
	public int getRewardControl() {
		return rewardControl.ordinal();
	}

    @Override
	public void setRewardControl(int rewardCon) {
		if (rewardCon < 0 || rewardCon >= EnumPartyExchange.values().length) {
			return;
		}
		this.rewardControl = EnumPartyExchange.values()[rewardCon];
	}

    @Override
	public int getCompleteFor() {
		return completeFor.ordinal();
	}

    @Override
	public void setCompleteFor(int compFor) {
		if (compFor < 0 || compFor >= EnumPartyExchange.values().length) {
			return;
		}
		this.completeFor = EnumPartyExchange.values()[compFor];
	}

    public int getObjectiveRequirement() {
        return objectiveRequirement.ordinal();
    }

    public void setObjectiveRequirement(int requirement) {
        if (requirement < 0 || requirement >= EnumPartyObjectives.values().length) {
            return;
        }
        this.objectiveRequirement = EnumPartyObjectives.values()[requirement];
    }

	@Override
	public int getMaxPartySize() {
		return maxPartySize;
	}

	@Override
	public void setMaxPartySize(int newSize) {
		maxPartySize = newSize;
	}
}
