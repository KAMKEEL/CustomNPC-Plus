package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
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
import java.util.Map.Entry;

public class QuestManual extends QuestInterface {
    public TreeMap<String, Integer> manuals = new TreeMap<String, Integer>();

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        manuals = new TreeMap<>(NBTTags.getStringIntegerMap(compound.getTagList("QuestManual", 10)));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("QuestManual", NBTTags.nbtStringIntegerMap(manuals));
    }

    @Override
    public boolean isCompleted(PlayerData playerData) {
        if (playerData == null)
            return false;

        QuestData data = playerData.questData.activeQuests.get(questId);
        if (data == null)
            return false;
        HashMap<String, Integer> manual = getManual(data);
        if (manuals.size() != manual.size())
            return false;

        for (String entity : manuals.keySet()) {
            if (!manual.containsKey(entity) || manual.get(entity) < manuals.get(entity))
                return false;
        }

        return true;
    }

    @Override
    public void handleComplete(EntityPlayer player) {
    }

    @Override
    public Vector<String> getQuestLogStatus(EntityPlayer player) {
        Vector<String> vec = new Vector<String>();
        PlayerData playerdata = PlayerDataController.Instance.getPlayerData(player);
        if (playerdata == null)
            return vec;

        QuestData data = playerdata.questData.activeQuests.get(questId);
        if (data == null)
            return vec;

        HashMap<String, Integer> manual = getManual(data);
        for (String entity : manuals.keySet()) {
            vec.add(entity + ": " + manual.getOrDefault(entity, 0) + "/" + manuals.get(entity));
        }
        return vec;
    }

    public HashMap<String, Integer> getManual(QuestData data) {
        return NBTTags.getStringIntegerMap(data.extraData.getTagList("Manual", 10));
    }

    public void setManual(QuestData data, HashMap<String, Integer> manual) {
        data.extraData.setTag("Manual", NBTTags.nbtStringIntegerMap(manual));
    }

    public HashMap<String, Integer> getPlayerManual(QuestData data, String playerName) {
        return NBTTags.getStringIntegerMap(data.extraData.getTagList(playerName + "Manual", 10));
    }

    public void setPlayerManual(QuestData data, HashMap<String, Integer> playerManual, String playerName) {
        data.extraData.setTag(playerName + "Manual", NBTTags.nbtStringIntegerMap(playerManual));
    }

    @Override
    public IQuestObjective[] getObjectives(EntityPlayer player) {
        List<IQuestObjective> list = new ArrayList<IQuestObjective>();
        for (Entry<String, Integer> entry : manuals.entrySet()) {
            list.add(new QuestManualObjective(player, entry.getKey(), entry.getValue()));
        }
        return list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList<IQuestObjective>();
        for (Entry<String, Integer> entry : manuals.entrySet()) {
            list.add(new QuestManualObjective(party, entry.getKey(), entry.getValue()));
        }
        return list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        QuestData data = party.getQuestData();
        if (data == null)
            return vec;

        if (data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
            for (String entityName : manuals.keySet()) {
                String firstLine = entityName + ": " + manuals.get(entityName);
                List<String> playerResults = new ArrayList<>();
                for (String player : party.getPlayerNames()) {
                    HashMap<String, Integer> playerManual = getPlayerManual(data, player);
                    int amount = 0;
                    if (playerManual.containsKey(entityName))
                        amount = playerManual.get(entityName);

                    if (amount < manuals.get(entityName)) {
                        String state = player + ": " + amount;
                        playerResults.add(state);
                    }
                }

                if (!playerResults.isEmpty()) {
                    vec.add(firstLine);
                    vec.add("[" + String.join(", ", playerResults) + "]");
                } else {
                    vec.add(firstLine + " (Done)");
                }
            }
        } else {
            HashMap<String, Integer> manual = getManual(data);
            for (String entity : manuals.keySet()) {
                int amount = 0;
                if (manual.containsKey(entity))
                    amount = manual.get(entity);
                String state = amount + "/" + manuals.get(entity);
                vec.add(entity + ": " + state);
            }
        }
        return vec;
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if (party == null)
            return false;

        QuestData data = party.getQuestData();
        if (data == null)
            return false;

        if (data.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
            for (String entityName : manuals.keySet()) {
                for (String player : party.getPlayerNames()) {
                    HashMap<String, Integer> playerManual = getPlayerManual(data, player);
                    int amount = playerManual.getOrDefault(entityName, 0);
                    if (amount < manuals.get(entityName)) {
                        return false;
                    }
                }
            }
        } else {
            int completed = 0;
            HashMap<String, Integer> playerManual = getManual(data);
            for (String entityName : manuals.keySet()) {
                int amount = 0;
                if (playerManual.containsKey(entityName))
                    amount = playerManual.get(entityName);
                if (amount >= manuals.get(entityName)) {
                    completed++;
                }
            }
            if (completed >= manuals.keySet().size())
                return true;

            if (playerManual.size() != manuals.size())
                return false;
            for (String entity : playerManual.keySet()) {
                if (!manuals.containsKey(entity) || manuals.get(entity) > playerManual.get(entity))
                    return false;
            }
        }
        return true;
    }

    class QuestManualObjective implements IQuestObjective {
        private final EntityPlayer player;
        private final String entity;
        private final Party party;
        private final int amount;

        public QuestManualObjective(EntityPlayer player, String entity, int amount) {
            this.player = player;
            this.entity = entity;
            this.amount = amount;
            this.party = null;
        }

        public QuestManualObjective(Party party, String entity, int amount) {
            this.party = party;
            this.entity = entity;
            this.amount = amount;
            this.player = null;
        }

        @Override
        public int getProgress() {
            if (player != null) {
                PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                PlayerQuestData playerdata = data.questData;
                QuestData questdata = playerdata.activeQuests.get(questId);
                if (questdata != null) {
                    HashMap<String, Integer> playerManual = getManual(questdata);
                    return !playerManual.containsKey(this.entity) ? 0 : playerManual.get(this.entity);
                }
            } else if (party != null) {
                QuestData questdata = party.getQuestData();
                if (questdata != null) {
                    if (questdata.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
                        int howManyDone = 0;
                        for (String player : party.getPlayerNames()) {
                            HashMap<String, Integer> playerManual = getPlayerManual(questdata, player);
                            int currentProgress = !playerManual.containsKey(this.entity) ? 0 : playerManual.get(this.entity);
                            if (currentProgress >= this.amount)
                                howManyDone += 1;
                        }
                        if (howManyDone == party.getPlayerNames().size())
                            return getMaxProgress();

                        return 0;
                    } else {
                        HashMap<String, Integer> playerManual = getManual(questdata);
                        return !playerManual.containsKey(this.entity) ? 0 : playerManual.get(this.entity);
                    }
                }
            }
            return 0;
        }

        @Override
        public void setProgress(int progress) {
            if (progress >= 0 && progress <= this.amount) {
                if (player != null) {
                    PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                    PlayerQuestData playerdata = data.questData;
                    QuestData questdata = playerdata.activeQuests.get(questId);
                    if (questdata != null) {
                        HashMap<String, Integer> playerManual = getManual(questdata);
                        if (!playerManual.containsKey(this.entity) || playerManual.get(this.entity) != progress) {
                            playerManual.put(this.entity, progress);
                            setManual(questdata, playerManual);
                            data.questData.checkQuestCompletion(data, EnumQuestType.Manual);
                            data.updateClient = true;
                            data.save();
                        }
                    }
                } else if (party != null) {
                    QuestData questdata = party.getQuestData();
                    if (questdata != null) {
                        if (questdata.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
                            for (String player : party.getPlayerNames()) {
                                HashMap<String, Integer> playerManual = getPlayerManual(questdata, player);
                                if (!playerManual.containsKey(this.entity) || playerManual.get(this.entity) != progress) {
                                    playerManual.put(this.entity, progress);
                                    setPlayerManual(questdata, playerManual, player);
                                }
                            }
                            PartyController.Instance().checkQuestCompletion(party, EnumQuestType.Manual);
                        } else {
                            HashMap<String, Integer> playerManual = getManual(questdata);
                            if (!playerManual.containsKey(this.entity) || playerManual.get(this.entity) != progress) {
                                playerManual.put(this.entity, progress);
                                setManual(questdata, playerManual);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.Manual);
                            }
                        }
                    }
                }
            } else {
                throw new CustomNPCsException("Progress has to be between 0 and " + this.amount);
            }
        }

        @Override
        public void setPlayerProgress(String playerName, int progress) {
            if (progress >= 0 && progress <= this.amount) {
                EntityPlayer foundplayer = NoppesUtilServer.getPlayerByName(playerName);
                if (foundplayer != null && party == null) {
                    PlayerData data = PlayerDataController.Instance.getPlayerData(foundplayer);
                    PlayerQuestData playerdata = data.questData;
                    QuestData questdata = playerdata.activeQuests.get(questId);
                    if (questdata != null) {
                        HashMap<String, Integer> playerManual = getManual(questdata);
                        if (!playerManual.containsKey(this.entity) || playerManual.get(this.entity) != progress) {
                            playerManual.put(this.entity, progress);
                            setManual(questdata, playerManual);
                            data.questData.checkQuestCompletion(data, EnumQuestType.Manual);
                            data.updateClient = true;
                            data.save();
                        }
                    }
                } else if (foundplayer != null) {
                    QuestData questdata = party.getQuestData();
                    if (questdata != null) {
                        if (questdata.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
                            HashMap<String, Integer> playerManual = getPlayerManual(questdata, foundplayer.getCommandSenderName());
                            if (!playerManual.containsKey(this.entity) || playerManual.get(this.entity) != progress) {
                                playerManual.put(this.entity, progress);
                                setPlayerManual(questdata, playerManual, foundplayer.getCommandSenderName());
                            }
                            PartyController.Instance().checkQuestCompletion(party, EnumQuestType.Manual);
                        } else {
                            HashMap<String, Integer> playerManual = getManual(questdata);
                            if (!playerManual.containsKey(this.entity) || playerManual.get(this.entity) != progress) {
                                playerManual.put(this.entity, progress);
                                setManual(questdata, playerManual);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.Manual);
                            }
                        }
                    }
                }
            } else {
                throw new CustomNPCsException("Progress has to be between 0 and " + this.amount);
            }
        }

        @Override
        public int getMaxProgress() {
            return amount;
        }

        @Override
        public boolean isCompleted() {
            return getProgress() >= amount;
        }

        @Override
        public String getText() {
            if (party != null) {
                if (party.getObjectiveRequirement() == EnumPartyObjectives.All) {
                    return this.entity + ": " + this.getMaxProgress() + (isCompleted() ? " (Done)" : "");
                }
            }
            return this.entity + ": " + this.getProgress() + "/" + this.getMaxProgress();
        }

        @Override
        public String getAdditionalText() {
            if (party != null) {
                List<String> incompletePlayers = new ArrayList<>();
                QuestData questdata = party.getQuestData();
                if (questdata != null) {
                    if (questdata.quest.partyOptions.objectiveRequirement == EnumPartyObjectives.All) {
                        for (String player : party.getPlayerNames()) {
                            HashMap<String, Integer> playerManual = getPlayerManual(questdata, player);
                            int currentProgress = !playerManual.containsKey(this.entity) ? 0 : playerManual.get(this.entity);
                            if (currentProgress < this.amount) {
                                String state = player + ": " + currentProgress;
                                incompletePlayers.add(state);
                            }
                        }
                        if (!incompletePlayers.isEmpty())
                            return "[" + String.join(", ", incompletePlayers) + "]";
                    }
                }
            }
            return null;
        }
    }

}
