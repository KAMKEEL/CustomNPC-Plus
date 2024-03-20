package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IPartyOptions;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumPartyRequirements;

import java.util.Vector;

public class PartyOptions implements IPartyOptions {
	public boolean allowParty = false;
    public boolean onlyParty = false;

    public EnumPartyRequirements partyRequirements = EnumPartyRequirements.Leader;

    public EnumPartyExchange rewardControl = EnumPartyExchange.Leader;
	public EnumPartyExchange completeFor = EnumPartyExchange.Leader;
    public EnumPartyExchange executeCommand = EnumPartyExchange.Leader;

    public EnumPartyObjectives objectiveRequirement = EnumPartyObjectives.Shared;

    public int minPartySize = ConfigMain.DefaultMinPartySize;
	public int maxPartySize = ConfigMain.DefaultMaxPartySize;

    public void readFromNBT(NBTTagCompound compound)
    {
		// Party Management
		allowParty = compound.getBoolean("AllowParty");
		if(allowParty){
            onlyParty = compound.getBoolean("OnlyParty");
			partyRequirements = EnumPartyRequirements.values()[compound.getInteger("PartyRequirements")];
			rewardControl = EnumPartyExchange.values()[compound.getInteger("RewardControl")];
			completeFor = EnumPartyExchange.values()[compound.getInteger("CompleteFor")];
            executeCommand = EnumPartyExchange.values()[compound.getInteger("ExecuteCommand")];
            minPartySize = compound.getInteger("MinPartySize");
			maxPartySize = compound.getInteger("MaxPartySize");
            objectiveRequirement = EnumPartyObjectives.values()[compound.getInteger("ObjectiveRequirement")];
		}
		else {
            if(compound.hasKey("OnlyParty")){
                compound.removeTag("OnlyParty");
            }
			if(compound.hasKey("PartyRequirements")){
				compound.removeTag("PartyRequirements");
			}
			if(compound.hasKey("RewardControl")){
				compound.removeTag("RewardControl");
			}
			if(compound.hasKey("CompleteFor")){
				compound.removeTag("CompleteFor");
			}
            if(compound.hasKey("ExecuteCommand")){
                compound.removeTag("ExecuteCommand");
            }
            if(compound.hasKey("MinPartySize")){
                compound.removeTag("MinPartySize");
            }
			if(compound.hasKey("MaxPartySize")){
				compound.removeTag("MaxPartySize");
			}
            if(compound.hasKey("ObjectiveRequirement")){
                compound.removeTag("ObjectiveRequirement");
            }

            onlyParty = false;
            partyRequirements = EnumPartyRequirements.Leader;
			rewardControl = EnumPartyExchange.Leader;
			completeFor = EnumPartyExchange.Leader;
            executeCommand = EnumPartyExchange.Leader;
            minPartySize = ConfigMain.DefaultMinPartySize;
			maxPartySize = ConfigMain.DefaultMaxPartySize;
            objectiveRequirement = EnumPartyObjectives.Shared;
		}
    }
	public NBTTagCompound writeToNBT()
    {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("AllowParty", allowParty);
		if(allowParty){
            compound.setBoolean("OnlyParty", onlyParty);
			compound.setInteger("PartyRequirements", partyRequirements.ordinal());
			compound.setInteger("RewardControl", rewardControl.ordinal());
			compound.setInteger("CompleteFor", completeFor.ordinal());
            compound.setInteger("ExecuteCommand", executeCommand.ordinal());
            compound.setInteger("MinPartySize", minPartySize);
			compound.setInteger("MaxPartySize", maxPartySize);
            compound.setInteger("ObjectiveRequirement", objectiveRequirement.ordinal());
		}
		else {
            if(compound.hasKey("OnlyParty")){
                compound.removeTag("OnlyParty");
            }
			if(compound.hasKey("PartyRequirements")){
				compound.removeTag("PartyRequirements");
			}
			if(compound.hasKey("RewardControl")){
				compound.removeTag("RewardControl");
			}
			if(compound.hasKey("CompleteFor")){
				compound.removeTag("CompleteFor");
			}
            if(compound.hasKey("ExecuteCommand")){
                compound.removeTag("ExecuteCommand");
            }
            if(compound.hasKey("MinPartySize")){
                compound.removeTag("MinPartySize");
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
    public boolean isOnlyParty() {
        return onlyParty;
    }

    @Override
    public void setOnlyParty(boolean onlyParty) {
        this.onlyParty = onlyParty;
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

    @Override
    public int getExecuteCommandFor() {
        return executeCommand.ordinal();
    }

    @Override
    public void setExecuteCommandFor(int commandFor) {
        if (commandFor < 0 || commandFor >= EnumPartyExchange.values().length) {
            return;
        }
        this.executeCommand = EnumPartyExchange.values()[commandFor];
    }

    @Override
    public int getObjectiveRequirement() {
        return objectiveRequirement.ordinal();
    }

    @Override
    public void setObjectiveRequirement(int requirement) {
        if (requirement < 0 || requirement >= EnumPartyObjectives.values().length) {
            return;
        }
        this.objectiveRequirement = EnumPartyObjectives.values()[requirement];
    }

    @Override
    public int getMinPartySize() {
        return minPartySize;
    }

    @Override
    public void setMinPartySize(int newSize) {
        if(newSize < 1)
            newSize = 1;

        if(newSize > maxPartySize)
            newSize = maxPartySize;

        minPartySize = newSize;
    }

    @Override
	public int getMaxPartySize() {
		return maxPartySize;
	}

	@Override
	public void setMaxPartySize(int newSize) {
        if(newSize < minPartySize)
            newSize = minPartySize;

		maxPartySize = newSize;
	}

    public Vector<String> getPartyOptionsList() {
        Vector<String> vec = new Vector<String>();
        if(onlyParty){
            vec.add("party.only" + ":" + "gui.yes");
        }
        vec.add("party.partyRequirements" + ":" + partyRequirements.name);
        vec.add("quest.objectives" + ":" + objectiveRequirement.name);
        vec.add("party.completeFor" + ":" + completeFor.name);
        vec.add("quest.reward" + ":" + rewardControl.name);
        vec.add("party.minPartySize" + ":" + minPartySize);
        vec.add("party.maxPartySize" + ":" + maxPartySize);
        return vec;
    }
}
