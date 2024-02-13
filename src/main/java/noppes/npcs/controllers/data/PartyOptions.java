package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.api.handler.data.IPartyOptions;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyRequirements;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;

public class PartyOptions implements IPartyOptions {

	public boolean allowParty = false;
	public EnumPartyRequirements partyRequirements = EnumPartyRequirements.Leader;
	public EnumPartyExchange rewardControl = EnumPartyExchange.Leader;
	public EnumPartyExchange completeFor = EnumPartyExchange.Leader;
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

			partyRequirements = EnumPartyRequirements.Leader;
			rewardControl = EnumPartyExchange.Leader;
			completeFor = EnumPartyExchange.Leader;
			maxPartySize = ConfigMain.DefaultMaxPartySize;
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
		}

		return compound;
    }

	public boolean isAllowParty() {
		return allowParty;
	}

	public void setAllowParty(boolean allowParty) {
		this.allowParty = allowParty;
	}

	public int getPartyRequirements() {
		return partyRequirements.ordinal();
	}

	public void setPartyRequirements(int partyReq) {
		if (partyReq < 0 || partyReq >= EnumPartyRequirements.values().length) {
			return;
		}
		this.partyRequirements = EnumPartyRequirements.values()[partyReq];
	}

	public int getRewardControl() {
		return rewardControl.ordinal();
	}

	public void setRewardControl(int rewardCon) {
		if (rewardCon < 0 || rewardCon >= EnumPartyExchange.values().length) {
			return;
		}
		this.rewardControl = EnumPartyExchange.values()[rewardCon];
	}

	public int getCompleteFor() {
		return completeFor.ordinal();
	}

	public void setCompleteFor(int compFor) {
		if (compFor < 0 || compFor >= EnumPartyExchange.values().length) {
			return;
		}
		this.completeFor = EnumPartyExchange.values()[compFor];
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
