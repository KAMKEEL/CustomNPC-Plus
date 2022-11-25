package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import noppes.npcs.ICompatibilty;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.entity.IPlayer;

public class Availability implements ICompatibilty, IAvailability {
	public int version = VersionCompatibility.ModRev;

	public EnumAvailabilityDialog dialogAvailable = EnumAvailabilityDialog.Always;
	public EnumAvailabilityDialog dialog2Available = EnumAvailabilityDialog.Always;
	public EnumAvailabilityDialog dialog3Available = EnumAvailabilityDialog.Always;
	public EnumAvailabilityDialog dialog4Available = EnumAvailabilityDialog.Always;
	public int dialogId = -1;
	public int dialog2Id = -1;
	public int dialog3Id = -1;
	public int dialog4Id = -1;
	
	public EnumAvailabilityQuest questAvailable = EnumAvailabilityQuest.Always;
	public EnumAvailabilityQuest quest2Available = EnumAvailabilityQuest.Always;
	public EnumAvailabilityQuest quest3Available = EnumAvailabilityQuest.Always;
	public EnumAvailabilityQuest quest4Available = EnumAvailabilityQuest.Always;
	public int questId = -1;
	public int quest2Id = -1;
	public int quest3Id = -1;
	public int quest4Id = -1;

	public EnumDayTime daytime = EnumDayTime.Always;
	
	public int factionId = -1;
	public int faction2Id = -1;
	
	public EnumAvailabilityFactionType factionAvailable = EnumAvailabilityFactionType.Always;
	public EnumAvailabilityFactionType faction2Available = EnumAvailabilityFactionType.Always;

	public EnumAvailabilityFaction factionStance = EnumAvailabilityFaction.Friendly;
	public EnumAvailabilityFaction faction2Stance = EnumAvailabilityFaction.Friendly;

	public int minPlayerLevel = 0;
	
	
    public void readFromNBT(NBTTagCompound compound)
    {
		version = compound.getInteger("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);

    	dialogAvailable = EnumAvailabilityDialog.values()[compound.getInteger("AvailabilityDialog")];
    	dialog2Available = EnumAvailabilityDialog.values()[compound.getInteger("AvailabilityDialog2")];
    	dialog3Available = EnumAvailabilityDialog.values()[compound.getInteger("AvailabilityDialog3")];
    	dialog4Available = EnumAvailabilityDialog.values()[compound.getInteger("AvailabilityDialog4")];
    	
    	dialogId = compound.getInteger("AvailabilityDialogId");
    	dialog2Id = compound.getInteger("AvailabilityDialog2Id");
    	dialog3Id = compound.getInteger("AvailabilityDialog3Id");
    	dialog4Id = compound.getInteger("AvailabilityDialog4Id");
    	
    	questAvailable = EnumAvailabilityQuest.values()[compound.getInteger("AvailabilityQuest")];
    	quest2Available = EnumAvailabilityQuest.values()[compound.getInteger("AvailabilityQuest2")];
    	quest3Available = EnumAvailabilityQuest.values()[compound.getInteger("AvailabilityQuest3")];
    	quest4Available = EnumAvailabilityQuest.values()[compound.getInteger("AvailabilityQuest4")];
    	
    	questId = compound.getInteger("AvailabilityQuestId");
    	quest2Id = compound.getInteger("AvailabilityQuest2Id");
    	quest3Id = compound.getInteger("AvailabilityQuest3Id");
    	quest4Id = compound.getInteger("AvailabilityQuest4Id");
    	
    	setFactionAvailability(compound.getInteger("AvailabilityFaction"));
    	setFactionAvailabilityStance(compound.getInteger("AvailabilityFactionStance"));

    	setFaction2Availability(compound.getInteger("AvailabilityFaction2"));
    	setFaction2AvailabilityStance(compound.getInteger("AvailabilityFaction2Stance"));

    	factionId = compound.getInteger("AvailabilityFactionId");
    	faction2Id = compound.getInteger("AvailabilityFaction2Id");
    	
    	daytime = EnumDayTime.values()[compound.getInteger("AvailabilityDayTime")];

    	minPlayerLevel = compound.getInteger("AvailabilityMinPlayerLevel");
    }
    
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		compound.setInteger("ModRev", version);
		
		compound.setInteger("AvailabilityDialog", dialogAvailable.ordinal());
		compound.setInteger("AvailabilityDialog2", dialog2Available.ordinal());
		compound.setInteger("AvailabilityDialog3", dialog3Available.ordinal());
		compound.setInteger("AvailabilityDialog4", dialog4Available.ordinal());
		
		compound.setInteger("AvailabilityDialogId", dialogId);
		compound.setInteger("AvailabilityDialog2Id", dialog2Id);
		compound.setInteger("AvailabilityDialog3Id", dialog3Id);
		compound.setInteger("AvailabilityDialog4Id", dialog4Id);
		
		compound.setInteger("AvailabilityQuest", questAvailable.ordinal());
		compound.setInteger("AvailabilityQuest2", quest2Available.ordinal());
		compound.setInteger("AvailabilityQuest3", quest3Available.ordinal());
		compound.setInteger("AvailabilityQuest4", quest4Available.ordinal());
		
		compound.setInteger("AvailabilityQuestId", questId);
		compound.setInteger("AvailabilityQuest2Id", quest2Id);
		compound.setInteger("AvailabilityQuest3Id", quest3Id);
		compound.setInteger("AvailabilityQuest4Id", quest4Id);
		
		compound.setInteger("AvailabilityFaction", factionAvailable.ordinal());
		compound.setInteger("AvailabilityFactionStance", factionStance.ordinal());
		
		compound.setInteger("AvailabilityFactionId", factionId);

		compound.setInteger("AvailabilityFaction2", faction2Available.ordinal());
		compound.setInteger("AvailabilityFaction2Stance", faction2Stance.ordinal());
		
		compound.setInteger("AvailabilityFaction2Id", faction2Id);

		compound.setInteger("AvailabilityDayTime", daytime.ordinal());
		compound.setInteger("AvailabilityMinPlayerLevel", minPlayerLevel);
		return compound;
    }
	public void setFactionAvailability(int value) {
    	factionAvailable =  EnumAvailabilityFactionType.values()[value];
	}
	public void setFaction2Availability(int value) {
    	faction2Available =  EnumAvailabilityFactionType.values()[value];
	}
	public void setFactionAvailabilityStance(int integer) {
		factionStance = EnumAvailabilityFaction.values()[integer];
	}
	public void setFaction2AvailabilityStance(int integer) {
		faction2Stance = EnumAvailabilityFaction.values()[integer];
	}
	public boolean isAvailable(EntityPlayer player){
		if(daytime == EnumDayTime.Day){
			long time = player.worldObj.getWorldTime() % 24000;
			if(time > 12000)
				return false;
		}
		if(daytime == EnumDayTime.Night){
			long time = player.worldObj.getWorldTime() % 24000;
			if(time < 12000)
				return false;
		}
		
		if(!dialogAvailable(dialogId, dialogAvailable, player))
			return false;
		if(!dialogAvailable(dialog2Id, dialog2Available, player))
			return false;
		if(!dialogAvailable(dialog3Id, dialog3Available, player))
			return false;
		if(!dialogAvailable(dialog4Id, dialog4Available, player))
			return false;

		if(!questAvailable(questId, questAvailable, player))
			return false;
		if(!questAvailable(quest2Id, quest2Available, player))
			return false;
		if(!questAvailable(quest3Id, quest3Available, player))
			return false;
		if(!questAvailable(quest4Id, quest4Available, player))
			return false;

		if(!factionAvailable(factionId, factionStance, factionAvailable, player))
			return false;
		if(!factionAvailable(faction2Id, faction2Stance, faction2Available, player))
			return false;
		
		if(player.experienceLevel < minPlayerLevel)
			return false;
		
		return true;
	}
	
	private boolean factionAvailable(int id, EnumAvailabilityFaction stance, EnumAvailabilityFactionType available, EntityPlayer player) {
		if(available == EnumAvailabilityFactionType.Always)
			return true;
		
		Faction faction = FactionController.getInstance().get(id);
		if(faction == null)
			return true;
		
		PlayerFactionData data = PlayerDataController.instance.getPlayerData(player).factionData;
		int points = data.getFactionPoints(id);
		
		EnumAvailabilityFaction current = EnumAvailabilityFaction.Neutral;
		if(faction.neutralPoints >= points)
			current = EnumAvailabilityFaction.Hostile;
		if(faction.friendlyPoints < points)
			current = EnumAvailabilityFaction.Friendly;

		if(available == EnumAvailabilityFactionType.Is && stance == current){
			return true;
		}
		if(available == EnumAvailabilityFactionType.IsNot && stance != current){
			return true;
		}
		
		return false;
	}
	
	public boolean dialogAvailable(int id, EnumAvailabilityDialog en, EntityPlayer player){
		if(en == EnumAvailabilityDialog.Always)
			return true;
		boolean hasRead = PlayerDataController.instance.getPlayerData(player).dialogData.dialogsRead.contains(id);
		if(hasRead && en == EnumAvailabilityDialog.After)
			return true;
		else if(!hasRead && en == EnumAvailabilityDialog.Before)
			return true;
		return false;
	}
	
	public boolean questAvailable(int id, EnumAvailabilityQuest en, EntityPlayer player){
		if(en == EnumAvailabilityQuest.Always)
			return true;
		else if(en == EnumAvailabilityQuest.After && PlayerQuestController.isQuestFinished(player, id))
			return true;
		else if(en == EnumAvailabilityQuest.Before && !PlayerQuestController.isQuestFinished(player, id))
			return true;
		else if(en == EnumAvailabilityQuest.Active && PlayerQuestController.isQuestActive(player, id))
			return true;
		else if(en == EnumAvailabilityQuest.NotActive && !PlayerQuestController.isQuestActive(player, id))
			return true;
		return false;
	}
	@Override
	public int getVersion() {
		return version;
	}
	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isAvailable(IPlayer player) {
		return this.isAvailable((EntityPlayer)player.getMCEntity());
	}

	public int getDaytime() {
		return this.daytime.ordinal();
	}

	public void setDaytime(int type) {
		this.daytime = EnumDayTime.values()[MathHelper.clamp_int(type, 0, 2)];
	}

	public int getMinPlayerLevel() {
		return this.minPlayerLevel;
	}

	public void setMinPlayerLevel(int level) {
		this.minPlayerLevel = level;
	}

	public int getDialog(int i) {
		if (i < 0 && i > 3) {
			throw new CustomNPCsException(i + " isnt between 0 and 3", new Object[0]);
		} else if (i == 0) {
			return this.dialogId;
		} else if (i == 1) {
			return this.dialog2Id;
		} else {
			return i == 2 ? this.dialog3Id : this.dialog4Id;
		}
	}

	public void setDialog(int i, int id, int type) {
		if (i < 0 && i > 3) {
			throw new CustomNPCsException(i + " isnt between 0 and 3", new Object[0]);
		} else {
			EnumAvailabilityDialog e = EnumAvailabilityDialog.values()[MathHelper.clamp_int(type, 0, 2)];
			if (i == 0) {
				this.dialogId = id;
				this.dialogAvailable = e;
			} else if (i == 1) {
				this.dialog2Id = id;
				this.dialog2Available = e;
			} else if (i == 2) {
				this.dialog3Id = id;
				this.dialog3Available = e;
			} else if (i == 3) {
				this.dialog4Id = id;
				this.dialog4Available = e;
			}

		}
	}

	public void removeDialog(int i) {
		if (i < 0 && i > 3) {
			throw new CustomNPCsException(i + " isnt between 0 and 3", new Object[0]);
		} else {
			if (i == 0) {
				this.dialogId = -1;
				this.dialogAvailable = EnumAvailabilityDialog.Always;
			} else if (i == 1) {
				this.dialog2Id = -1;
				this.dialog2Available = EnumAvailabilityDialog.Always;
			} else if (i == 2) {
				this.dialog3Id = -1;
				this.dialog3Available = EnumAvailabilityDialog.Always;
			} else if (i == 3) {
				this.dialog4Id = -1;
				this.dialog4Available = EnumAvailabilityDialog.Always;
			}

		}
	}

	public int getQuest(int i) {
		if (i < 0 && i > 3) {
			throw new CustomNPCsException(i + " isnt between 0 and 3", new Object[0]);
		} else if (i == 0) {
			return this.questId;
		} else if (i == 1) {
			return this.quest2Id;
		} else {
			return i == 2 ? this.quest3Id : this.quest4Id;
		}
	}

	public void setQuest(int i, int id, int type) {
		if (i < 0 && i > 3) {
			throw new CustomNPCsException(i + " isnt between 0 and 3", new Object[0]);
		} else {
			EnumAvailabilityQuest e = EnumAvailabilityQuest.values()[MathHelper.clamp_int(type, 0, 5)];
			if (i == 0) {
				this.questId = id;
				this.questAvailable = e;
			} else if (i == 1) {
				this.quest2Id = id;
				this.quest2Available = e;
			} else if (i == 2) {
				this.quest3Id = id;
				this.quest3Available = e;
			} else if (i == 3) {
				this.quest4Id = id;
				this.quest4Available = e;
			}

		}
	}

	public void removeQuest(int i) {
		if (i < 0 && i > 3) {
			throw new CustomNPCsException(i + " isnt between 0 and 3", new Object[0]);
		} else {
			if (i == 0) {
				this.questId = -1;
				this.questAvailable = EnumAvailabilityQuest.Always;
			} else if (i == 1) {
				this.quest2Id = -1;
				this.quest2Available = EnumAvailabilityQuest.Always;
			} else if (i == 2) {
				this.quest3Id = -1;
				this.quest3Available = EnumAvailabilityQuest.Always;
			} else if (i == 3) {
				this.quest4Id = -1;
				this.quest4Available = EnumAvailabilityQuest.Always;
			}

		}
	}

	public void setFaction(int i, int id, int type, int stance) {
		if (i < 0 && i > 1) {
			throw new CustomNPCsException(i + " isnt between 0 and 1", new Object[0]);
		} else {
			EnumAvailabilityFactionType e = EnumAvailabilityFactionType.values()[MathHelper.clamp_int(type, 0, 2)];
			EnumAvailabilityFaction ee = EnumAvailabilityFaction.values()[MathHelper.clamp_int(stance, 0, 2)];
			if (i == 0) {
				this.factionId = id;
				this.factionAvailable = e;
				this.factionStance = ee;
			} else if (i == 1) {
				this.faction2Id = id;
				this.faction2Available = e;
				this.faction2Stance = ee;
			}

		}
	}

	public void removeFaction(int i) {
		if (i < 0 && i > 1) {
			throw new CustomNPCsException(i + " isnt between 0 and 1", new Object[0]);
		} else {
			if (i == 0) {
				this.factionId = -1;
				this.factionAvailable = EnumAvailabilityFactionType.Always;
				this.factionStance = EnumAvailabilityFaction.Friendly;
			} else if (i == 1) {
				this.faction2Id = -1;
				this.faction2Available = EnumAvailabilityFactionType.Always;
				this.faction2Stance = EnumAvailabilityFaction.Friendly;
			}

		}
	}
}
