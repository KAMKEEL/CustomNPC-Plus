package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.data.IQuestLocation;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class QuestLocation extends QuestInterface implements IQuestLocation {
	public String location = "";
	public String location2 = "";
	public String location3 = "";

    @Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		location = compound.getString("QuestLocation");
		location2 = compound.getString("QuestLocation2");
		location3 = compound.getString("QuestLocation3");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setString("QuestLocation", location);
		compound.setString("QuestLocation2", location2);
		compound.setString("QuestLocation3", location3);
	}

	@Override
	public boolean isCompleted(PlayerData playerData) {
		PlayerQuestData playerdata = playerData.questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return false;
		return getFound(data, 0);
	}

	@Override
	public void handleComplete(EntityPlayer player) {
		super.handleComplete(player);
	}

	@Override
	public Vector<String> getQuestLogStatus(EntityPlayer player) {
		Vector<String> vec = new Vector<String>();
		PlayerQuestData playerdata = PlayerDataController.Instance.getPlayerData(player).questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return vec;
		String found = StatCollector.translateToLocal("quest.found");
		String notfound = StatCollector.translateToLocal("quest.notfound");

		if(!location.isEmpty())
			vec.add(location + ": " + (getFound(data,1)?found:notfound));
		if(!location2.isEmpty())
			vec.add(location2 + ": " + (getFound(data,2)?found:notfound));
		if(!location3.isEmpty())
			vec.add(location3 + ": " + (getFound(data,3)?found:notfound));

		return vec;
	}

    @Override
    public IQuestObjective[] getObjectives(EntityPlayer player) {
        List<IQuestObjective> list = new ArrayList();
        if (!this.location.isEmpty()) {
            list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, player, this.location, "LocationFound"));
        }

        if (!this.location2.isEmpty()) {
            list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, player, this.location2, "Location2Found"));
        }

        if (!this.location3.isEmpty()) {
            list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, player, this.location3, "Location3Found"));
        }

        return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
    }

	public boolean getFound(QuestData data, int i) {
        if(i == 1)
            return data.extraData.getBoolean("LocationFound");
        if(i == 2)
            return data.extraData.getBoolean("Location2Found");
        if(i == 3)
            return data.extraData.getBoolean("Location3Found");

        if(!location.isEmpty() && !data.extraData.getBoolean("LocationFound"))
            return false;
        if(!location2.isEmpty() && !data.extraData.getBoolean("Location2Found"))
            return false;
        if(!location3.isEmpty() && !data.extraData.getBoolean("Location3Found"))
            return false;
        return true;
    }

	public boolean setFound(QuestData data, String location) {
		if(location.equalsIgnoreCase(this.location) && !data.extraData.getBoolean("LocationFound")){
			data.extraData.setBoolean("LocationFound", true);
			return true;
		}
		if(location.equalsIgnoreCase(location2) && !data.extraData.getBoolean("LocationFound2")){
			data.extraData.setBoolean("Location2Found", true);
			return true;
		}
		if(location.equalsIgnoreCase(location3) && !data.extraData.getBoolean("LocationFound3")){
			data.extraData.setBoolean("Location3Found", true);
			return true;
		}

		return false;
	}

    public boolean setFoundParty(Party party, EntityPlayer player, String location, boolean isLeader) {
        QuestData data = party.getQuestData();
        if(data == null)
            return false;

        if(data.quest == null)
            return false;

        int memberCount = party.getPlayerNames().size();
        EnumPartyObjectives objectives = data.quest.partyOptions.objectiveRequirement;
        if(objectives == EnumPartyObjectives.All){
            if(location.equalsIgnoreCase(this.location) && !data.extraData.getBoolean("LocationFound")){
                return setPartyPlayerFound(data, player, "LocationFound", memberCount);
            }
            if(location.equalsIgnoreCase(location2) && !data.extraData.getBoolean("LocationFound2")){
                return setPartyPlayerFound(data, player, "Location2Found", memberCount);
            }
            if(location.equalsIgnoreCase(location3) && !data.extraData.getBoolean("LocationFound3")){
                return setPartyPlayerFound(data, player, "Location3Found", memberCount);
            }
        }
        else if(objectives == EnumPartyObjectives.Leader && isLeader || objectives == EnumPartyObjectives.Shared){
            if(location.equalsIgnoreCase(this.location) && !data.extraData.getBoolean("LocationFound")){
                data.extraData.setBoolean("LocationFound", true);
                return true;
            }
            if(location.equalsIgnoreCase(location2) && !data.extraData.getBoolean("LocationFound2")){
                data.extraData.setBoolean("Location2Found", true);
                return true;
            }
            if(location.equalsIgnoreCase(location3) && !data.extraData.getBoolean("LocationFound3")){
                data.extraData.setBoolean("Location3Found", true);
                return true;
            }
        }
        return false;
    }

    public List<String> getPartyFound(Party party, String locationFoundKey) {
        List<String> foundPlayers = new ArrayList<>();
        if(party == null)
            return foundPlayers;

        QuestData data = party.getQuestData();
        if(data == null)
            return foundPlayers;

        String locationKey = "Players" + locationFoundKey;
        if(data.extraData.hasKey(locationKey)){
            foundPlayers = NBTTags.getStringList(data.extraData.getTagList(locationKey, 10));
        }

        return foundPlayers;
    }


    public boolean setPartyPlayerFound(QuestData data, EntityPlayer player, String locationFoundKey, int partySize) {
        boolean newFind = false;
        List<String> foundPlayers;
        String locationKey = "Players" + locationFoundKey;
        if(data.extraData.hasKey(locationKey)){
            foundPlayers = NBTTags.getStringList(data.extraData.getTagList(locationKey, 10));
        }
        else {
            foundPlayers = new ArrayList<>();
        }

        if(!foundPlayers.contains(player.getCommandSenderName())){
            foundPlayers.add(player.getCommandSenderName());
            newFind = true;
        }

        data.extraData.setTag(locationKey, NBTTags.nbtStringList(foundPlayers));
        if(foundPlayers.size() == partySize){
            data.extraData.setBoolean(locationFoundKey, true);
        }

        return newFind;
    }

    @Override
    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList();
        if (!this.location.isEmpty()) {
            list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, party, this.location, "LocationFound", getPartyFound(party, "LocationFound")));
        }

        if (!this.location2.isEmpty()) {
            list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, party, this.location2, "Location2Found", getPartyFound(party, "Location2Found")));
        }

        if (!this.location3.isEmpty()) {
            list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, party, this.location3, "Location3Found", getPartyFound(party, "Location3Found")));
        }

        return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        QuestData data = party.getQuestData();
        if(data == null)
            return vec;
        String found = StatCollector.translateToLocal("quest.found");
        String notfound = StatCollector.translateToLocal("quest.notfound");

        if(data.quest == null)
            return vec;

        boolean requireAll = data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All;
        if(!location.isEmpty()){
            vec.add(location + ": " + (getFound(data,1)?found:notfound));
            if(requireAll){
                List<String> playersFound = getPartyFound(party, "LocationFound");
                String playersFoundString = "Completed: " + String.join(", ", playersFound);
                vec.add(playersFoundString);
            }
        }
        if(!location2.isEmpty()){
            vec.add(location2 + ": " + (getFound(data,2)?found:notfound));
            if(requireAll){
                List<String> playersFound = getPartyFound(party, "Location2Found");
                String playersFoundString = "Completed: " + String.join(", ", playersFound);
                vec.add(playersFoundString);
            }
        }
        if(!location3.isEmpty()){
            vec.add(location3 + ": " + (getFound(data,3)?found:notfound));
            if(requireAll){
                List<String> playersFound = getPartyFound(party, "Location3Found");
                String playersFoundString = "Completed: " + String.join(", ", playersFound);
                vec.add(playersFoundString);
            }
        }
        return vec;
    }

    public boolean isMultiQuest(Party party){
        QuestData data = party.getQuestData();
        if(data == null)
            return false;

        if(data.quest == null)
            return false;

        return data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All;
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if(party == null)
            return false;

        QuestData data = party.getQuestData();
        if(data == null)
            return false;
        return getFound(data, 0);
    }

    public void setLocation1(String loc1){
		this.location = loc1;
	}
	public String getLocation1(){
		return location;
	}

	public void setLocation2(String loc2){
		this.location2 = loc2;
	}
	public String getLocation2(){
		return location2;
	}

	public void setLocation3(String loc3){
		this.location3 = loc3;
	}
	public String getLocation3(){
		return location3;
	}

	class QuestLocationObjective implements IQuestObjective {
		private final QuestLocation parent;
		private final EntityPlayer player;
        private final Party party;
		private final String location;
		private final String nbtName;
        private final List<String> completedPlayers;

		public QuestLocationObjective(QuestLocation this$0, EntityPlayer player, String location, String nbtName) {
			this.parent = this$0;
			this.player = player;
			this.location = location;
			this.nbtName = nbtName;
            this.party = null;
            this.completedPlayers = new ArrayList<>();
		}

        public QuestLocationObjective(QuestLocation this$0, Party party, String location, String nbtName, List<String> completedPlayers) {
            this.parent = this$0;
            this.player = null;
            this.location = location;
            this.nbtName = nbtName;
            this.party = party;
            this.completedPlayers =  new ArrayList<>(completedPlayers);
        }

		public int getProgress() {
			return this.isCompleted() ? 1 : 0;
		}

		public void setProgress(int progress) {
			if (progress >= 0 && progress <= 1) {
                if(player != null){
                    PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                    QuestData questData = (QuestData)data.questData.activeQuests.get(this.parent.questId);
                    boolean completed = questData.extraData.getBoolean	(this.nbtName);
                    if ((!completed || progress != 1) && (completed || progress != 0)) {
                        questData.extraData.setBoolean(this.nbtName, progress == 1);
                        data.questData.checkQuestCompletion(data, EnumQuestType.values()[3]);
                        data.save();
                        data.updateClient = true;
                    }
                } else if (party != null){
                    QuestData questData = party.getQuestData();
                    boolean completed = questData.extraData.getBoolean	(this.nbtName);
                    if ((!completed || progress != 1) && (completed || progress != 0)) {
                        boolean setTo = progress == 1;
                        questData.extraData.setBoolean(this.nbtName, setTo);
                        if(questData.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All){
                            completedPlayers.clear();
                            if(setTo){
                                completedPlayers.addAll(party.getPlayerNames());
                            }
                            String locationKey = "Players" + nbtName;
                            questData.extraData.setTag(locationKey, NBTTags.nbtStringList(completedPlayers));
                        }
                        PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[3]);
                    }
                }
			} else {
				throw new CustomNPCsException("Progress has to be 0 or 1", new Object[0]);
			}
		}

		public int getMaxProgress() {
			return 1;
		}

		public boolean isCompleted() {
            QuestData questData = null;
            if(player != null){
                PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                PlayerQuestData playerQuestData = data.questData;
                if(playerQuestData != null){
                    questData = playerQuestData.activeQuests.get(this.parent.questId);
                }
            } else if(party != null){
                questData = party.getQuestData();
            }

            if(questData != null){
                return questData.extraData.getBoolean(this.nbtName);
            }
			return false;
		}

		public String getText() {
			return this.location + ": " + (this.isCompleted() ? "Found" : "Not Found");
		}

        public String getAdditionalText() {
            if(party != null && parent.isMultiQuest(party) && completedPlayers.size() != party.getPlayerNames().size())
                return  "Completed: " + String.join(", ", completedPlayers);
            return null;
        }
	}
}
