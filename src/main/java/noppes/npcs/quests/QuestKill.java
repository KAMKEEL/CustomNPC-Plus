package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.data.IQuestKill;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.*;

public class QuestKill extends QuestInterface implements IQuestKill {
	public HashMap<String,Integer> targets = new HashMap<String,Integer>();
	public int targetType = 0;
	public String customTargetType = "noppes.npcs.entity.EntityCustomNpc";

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("QuestKills", NBTTags.nbtStringIntegerMap(targets));
		compound.setInteger("TargetType",targetType);
		compound.setString("CustomTargetType",customTargetType);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if(!compound.hasKey("QuestKills")) {
			targets.clear();
			HashMap<String,Integer> oldTargets = NBTTags.getStringIntegerMap(compound.getTagList("QuestDialogs", 10));
			targets.putAll(oldTargets);
		} else {
			targets = NBTTags.getStringIntegerMap(compound.getTagList("QuestKills", 10));
		}
        targetType = compound.getInteger("TargetType");
		customTargetType = compound.getString("CustomTargetType");
	}

	@Override
	public boolean isCompleted(PlayerData playerData) {
		PlayerQuestData playerdata = playerData.questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return false;

		HashMap<String,Integer> killed = getKilled(data);
		int completed = 0;
		for(String entityName : targets.keySet()){
			int amount = 0;
			if(killed.containsKey(entityName))
				amount = killed.get(entityName);
			if(amount >= targets.get(entityName)){
				completed++;
			}
		}
		if(completed >= targets.keySet().size())
			return true;

		if(killed.size() != targets.size())
			return false;
		for(String entity : killed.keySet()){
			if(!targets.containsKey(entity) || targets.get(entity) > killed.get(entity))
				return false;
		}

		return true;
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
		HashMap<String,Integer> killed = getKilled(data);
		for(String entityName : targets.keySet()){
			//Class cls = (Class) EntityList.stringToClassMapping.get(entityName);
			int amount = 0;
			if(killed.containsKey(entityName))
				amount = killed.get(entityName);
			String state = amount + "/" + targets.get(entityName);

			vec.add(entityName + ": " + state);
		}

		return vec;
	}

	public HashMap<String, Integer> getKilled(QuestData data) {
		return NBTTags.getStringIntegerMap(data.extraData.getTagList("Killed", 10));
	}
	public void setKilled(QuestData data, HashMap<String, Integer> killed) {
		data.extraData.setTag("Killed", NBTTags.nbtStringIntegerMap(killed));
	}

	public IQuestObjective[] getObjectives(EntityPlayer player) {
		List<IQuestObjective> list = new ArrayList();
		Iterator var3 = this.targets.entrySet().iterator();

		while(var3.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry)var3.next();
			list.add(new noppes.npcs.quests.QuestKill.QuestKillObjective(this, player, (String)entry.getKey(), (Integer)entry.getValue()));
		}

		return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
	}

    public HashMap<String, Integer> getPlayerKilled(QuestData data, String playerName) {
        return NBTTags.getStringIntegerMap(data.extraData.getTagList(playerName + "Killed", 10));
    }
    public void setPlayerKilled(QuestData data, HashMap<String, Integer> killed, String playerName) {
        data.extraData.setTag(playerName + "Killed", NBTTags.nbtStringIntegerMap(killed));
    }

    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList();
        Iterator var3 = this.targets.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry)var3.next();
            list.add(new noppes.npcs.quests.QuestKill.QuestKillObjective(this, party, (String)entry.getKey(), (Integer)entry.getValue()));
        }

        return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        QuestData data = party.getQuestData();
        if(data == null)
            return vec;

        if(data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All){
            for(String entityName : targets.keySet()){
                String firstLine = entityName + ": " + targets.get(entityName);
                List<String> playerResults = new ArrayList<>();
                for(String player : party.getPlayerNames()){
                    HashMap<String,Integer> killed = getPlayerKilled(data, player);
                    int amount = 0;
                    if(killed.containsKey(entityName))
                        amount = killed.get(entityName);

                    if(amount < targets.get(entityName)){
                        String state = player + ": " + amount;
                        playerResults.add(state);
                    }
                }

                if(!playerResults.isEmpty()){
                    vec.add(firstLine);
                    vec.add("[" + String.join(", ", playerResults) + "]");
                }
                else {
                    vec.add(firstLine + " (Done)");
                }
            }
        }
        else {
            HashMap<String,Integer> killed = getKilled(data);
            for(String entityName : targets.keySet()){
                int amount = 0;
                if(killed.containsKey(entityName))
                    amount = killed.get(entityName);
                String state = amount + "/" + targets.get(entityName);

                vec.add(entityName + ": " + state);
            }
        }
        return vec;
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if(party == null)
            return false;

        QuestData data = party.getQuestData();
        if(data == null)
            return false;

        if(data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All){
            for(String entityName : targets.keySet()){
                for(String player : party.getPlayerNames()){
                    HashMap<String, Integer> killed = getPlayerKilled(data, player);
                    int amount = killed.getOrDefault(entityName, 0);
                    if(amount < targets.get(entityName)){
                        return false;
                    }
                }
            }
        }
        else {
            int completed = 0;
            HashMap<String,Integer> killed = getKilled(data);
            for(String entityName : targets.keySet()){
                int amount = 0;
                if(killed.containsKey(entityName))
                    amount = killed.get(entityName);
                if(amount >= targets.get(entityName)){
                    completed++;
                }
            }
            if(completed >= targets.keySet().size())
                return true;

            if(killed.size() != targets.size())
                return false;
            for(String entity : killed.keySet()){
                if(!targets.containsKey(entity) || targets.get(entity) > killed.get(entity))
                    return false;
            }
        }

        return true;
    }

    public void setTargetType(int type) {
		if(type < 0)
			type = 0;
		if(type > 2)
			type = 2;

		this.targetType = type;
	}
	public int getTargetType() {
		return this.targetType;
	}

	class QuestKillObjective implements IQuestObjective {
		private final QuestKill parent;
		private final EntityPlayer player;
        private final Party party;
		private final String entity;
		private final int amount;

		public QuestKillObjective(QuestKill parent, EntityPlayer player, String entity, int amount) {
			this.parent = parent;
			this.player = player;
			this.entity = entity;
			this.amount = amount;
            this.party = null;
		}

        public QuestKillObjective(QuestKill parent, Party party, String entity, int amount) {
            this.parent = parent;
            this.party = party;
            this.entity = entity;
            this.amount = amount;
            this.player = null;
        }

		public int getProgress() {
            if(player != null){
                PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                PlayerQuestData playerdata = data.questData;
                QuestData questdata = (QuestData)playerdata.activeQuests.get(this.parent.questId);
                if(questdata != null){
                    HashMap<String, Integer> killed = this.parent.getKilled(questdata);
                    return !killed.containsKey(this.entity) ? 0 : (Integer)killed.get(this.entity);
                }
            } else if(party != null) {
                QuestData questdata = party.getQuestData();
                if (questdata != null) {
                    if (questdata.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
                        int progress = 0;
                        for (String player : party.getPlayerNames()) {
                            HashMap<String, Integer> killed = this.parent.getPlayerKilled(questdata, player);
                            int currentProgress = !killed.containsKey(this.entity) ? 0 : (Integer) killed.get(this.entity);
                            if(currentProgress >= this.amount)
                                progress += 1;
                        }
                        return progress;
                    } else {
                        HashMap<String, Integer> killed = this.parent.getKilled(questdata);
                        return !killed.containsKey(this.entity) ? 0 : (Integer) killed.get(this.entity);
                    }
                }
            }
			return 0;
		}

		public void setProgress(int progress) {
			if (progress >= 0 && progress <= this.amount) {
                if(player != null){
                    PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                    PlayerQuestData playerdata = data.questData;
                    QuestData questdata = (QuestData)playerdata.activeQuests.get(this.parent.questId);
                    if(questdata != null){
                        HashMap<String, Integer> killed = this.parent.getKilled(questdata);
                        if (!killed.containsKey(this.entity) || (Integer)killed.get(this.entity) != progress) {
                            killed.put(this.entity, progress);
                            this.parent.setKilled(questdata, killed);
                            data.questData.checkQuestCompletion(data, EnumQuestType.values()[2]);
                            data.questData.checkQuestCompletion(data, EnumQuestType.values()[4]);
                            data.save();
                        }
                    }
                } else if(party != null){
                    QuestData questdata = party.getQuestData();
                    if(questdata != null){
                        if(questdata.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All){
                            for(String player : party.getPlayerNames()){
                                HashMap<String, Integer> killed = this.parent.getPlayerKilled(questdata, player);
                                if (!killed.containsKey(this.entity) || (Integer)killed.get(this.entity) != progress) {
                                    killed.put(this.entity, progress);
                                    this.parent.setPlayerKilled(questdata, killed, player);
                                }
                            }
                            PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[2]);
                            PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[4]);
                        }
                        else {
                            HashMap<String, Integer> killed = this.parent.getKilled(questdata);
                            if (!killed.containsKey(this.entity) || (Integer)killed.get(this.entity) != progress) {
                                killed.put(this.entity, progress);
                                this.parent.setKilled(questdata, killed);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[2]);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[4]);
                            }
                        }
                    }
                }
			} else {
				throw new CustomNPCsException("Progress has to be between 0 and " + this.amount, new Object[0]);
			}
		}

		public int getMaxProgress() {
			return this.amount;
		}

		public boolean isCompleted() {
			return this.getProgress() >= this.amount;
		}

		public String getText() {
			return this.entity + ": " + this.getProgress() + "/" + this.getMaxProgress();
		}

        @Override
        public String getAdditionalText() {
            return null;
        }
    }
}
