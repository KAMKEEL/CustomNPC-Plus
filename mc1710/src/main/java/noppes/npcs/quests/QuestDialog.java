package noppes.npcs.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IQuestDialog;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class QuestDialog extends QuestInterface implements IQuestDialog {

    public HashMap<Integer, Integer> dialogs = new HashMap<Integer, Integer>();

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        dialogs = NBTTags.getIntegerIntegerMap(compound.getTagList("QuestDialogs", 10));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("QuestDialogs", NBTTags.nbtIntegerIntegerMap(dialogs));
    }

    @Override
    public boolean isCompleted(PlayerData playerData) {
        for (int dialogId : dialogs.values())
            if (!playerData.dialogData.dialogsRead.contains(dialogId))
                return false;
        return true;
    }

    @Override
    public void handleComplete(EntityPlayer player) {
        super.handleComplete(player);
    }

    @Override
    public Vector<String> getQuestLogStatus(EntityPlayer player) {
        Vector<String> vec = new Vector<String>();
        for (int dialogId : dialogs.values()) {
            Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
            if (dialog == null)
                continue;
            String title = dialog.title;
            if (PlayerData.get(player).dialogData.dialogsRead.contains(dialogId))
                title += " (read)";
            else
                title += " (unread)";
            vec.add(title);
        }

        return vec;
    }

    @Override
    public IQuestObjective[] getObjectives(EntityPlayer player) {
        List<IQuestObjective> list = new ArrayList();

        for (int i = 0; i < 3; ++i) {
            if (this.dialogs.containsKey(i)) {
                Dialog dialog = (Dialog) DialogController.Instance.dialogs.get(this.dialogs.get(i));
                if (dialog != null) {
                    list.add(new noppes.npcs.quests.QuestDialog.QuestDialogObjective(this, player, dialog));
                }
            }
        }

        return (IQuestObjective[]) list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public IQuestObjective[] getPartyObjectives(Party party) {
        List<IQuestObjective> list = new ArrayList();

        for (int i = 0; i < 3; ++i) {
            if (this.dialogs.containsKey(i)) {
                Dialog dialog = (Dialog) DialogController.Instance.dialogs.get(this.dialogs.get(i));
                if (dialog != null) {
                    list.add(new noppes.npcs.quests.QuestDialog.QuestDialogObjective(this, party, dialog));
                }
            }
        }

        return (IQuestObjective[]) list.toArray(new IQuestObjective[list.size()]);
    }

    @Override
    public Vector<String> getPartyQuestLogStatus(Party party) {
        Vector<String> vec = new Vector<String>();
        if (party == null)
            return vec;

        if (party.getQuestData() == null)
            return vec;

        if (party.getQuestData().quest == null)
            return vec;

        EnumPartyObjectives objectives = party.getQuestData().quest.partyOptions.objectiveRequirement;
        if (objectives == EnumPartyObjectives.All) {
            for (int dialogId : dialogs.values()) {
                Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
                if (dialog == null)
                    continue;

                ArrayList<String> unread = new ArrayList<>();
                for (UUID uuid : party.getPlayerUUIDs()) {
                    EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                    PlayerData playerData;
                    if (player != null) {
                        playerData = PlayerData.get(player);
                    } else {
                        playerData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                    }
                    if (playerData != null) {
                        if (!playerData.dialogData.dialogsRead.contains(dialogId))
                            unread.add(playerData.playername);
                    }
                }

                String title = dialog.title;
                if (!unread.isEmpty()) {
                    title += " (unread)";
                } else {
                    title += " (read)";
                }
                vec.add(title);

                if (!unread.isEmpty()) {
                    StringBuilder unreaders = new StringBuilder();
                    for (String name : unread) {
                        if (unreaders.length() > 0) {
                            unreaders.append(", ");
                        }
                        unreaders.append(name);
                    }
                    vec.add(unreaders.toString());
                }
            }
            return vec;
        } else if (objectives == EnumPartyObjectives.Leader) {
            EntityPlayer leader = NoppesUtilServer.getPlayer(party.getLeaderUUID());
            PlayerData playerData;
            if (leader != null) {
                playerData = PlayerData.get(leader);
            } else {
                playerData = PlayerDataController.Instance.getPlayerDataCache(party.getLeaderUUID().toString());
            }
            if (playerData == null)
                return vec;
            for (int dialogId : dialogs.values()) {
                Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
                if (dialog == null)
                    continue;
                String title = dialog.title;
                if (playerData.dialogData.dialogsRead.contains(dialogId))
                    title += " (read)";
                else
                    title += " (unread)";
                vec.add(title);
            }
            return vec;
        } else {
            Set<Integer> readValues = new HashSet<>();
            for (int dialogId : dialogs.values()) {
                Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
                if (dialog == null)
                    continue;
                for (UUID uuid : party.getPlayerUUIDs()) {
                    EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                    PlayerData sharedData;
                    if (player != null) {
                        sharedData = PlayerData.get(player);
                    } else {
                        sharedData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                    }
                    if (sharedData != null) {
                        if (sharedData.dialogData.dialogsRead.contains(dialogId))
                            readValues.add(dialogId);
                    }
                }
            }
            for (int dialogId : dialogs.values()) {
                Dialog dialog = DialogController.Instance.dialogs.get(dialogId);
                if (dialog == null)
                    continue;
                String title = dialog.title;
                if (readValues.contains(dialogId))
                    title += " (read)";
                else
                    title += " (unread)";
                vec.add(title);
            }
            return vec;
        }
    }

    @Override
    public boolean isPartyCompleted(Party party) {
        if (party == null)
            return false;

        if (party.getQuestData() == null)
            return false;

        if (party.getQuestData().quest == null)
            return false;

        EnumPartyObjectives objectives = party.getQuestData().quest.partyOptions.objectiveRequirement;
        if (objectives == EnumPartyObjectives.All) {
            List<String> incomplete = new ArrayList<>();
            for (UUID uuid : party.getPlayerUUIDs()) {
                EntityPlayer individual = NoppesUtilServer.getPlayer(uuid);
                PlayerData individualData;
                if (individual != null) {
                    individualData = PlayerData.get(individual);
                } else {
                    individualData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                }
                if (individualData != null) {
                    for (int dialogId : dialogs.values()) {
                        if (!individualData.dialogData.dialogsRead.contains(dialogId)) {
                            incomplete.add(individualData.playername);
                            break;
                        }
                    }
                }
            }
            return incomplete.isEmpty();
        } else if (objectives == EnumPartyObjectives.Leader) {
            EntityPlayer leaderPlayer = NoppesUtilServer.getPlayer(party.getLeaderUUID());
            PlayerData leaderData;
            if (leaderPlayer != null) {
                leaderData = PlayerData.get(leaderPlayer);
            } else {
                leaderData = PlayerDataController.Instance.getPlayerDataCache(party.getLeaderUUID().toString());
            }
            if (leaderData != null) {
                for (int dialogId : dialogs.values()) {
                    if (!leaderData.dialogData.dialogsRead.contains(dialogId)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        } else {
            HashMap<Integer, Boolean> readValues = new HashMap<>();
            for (UUID uuid : party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                PlayerData playerData;
                if (player != null) {
                    playerData = PlayerData.get(player);
                } else {
                    playerData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                }
                if (playerData != null) {
                    for (int dialogId : dialogs.values()) {
                        if (playerData.dialogData.dialogsRead.contains(dialogId)) {
                            readValues.put(dialogId, true);
                        }
                    }
                }
            }
            for (boolean result : readValues.values()) {
                if (!result)
                    return false;
            }
            return true;
        }
    }

    class QuestDialogObjective implements IQuestObjective {
        private final QuestDialog parent;
        private final EntityPlayer player;
        private final Party party;
        private final Dialog dialog;

        public QuestDialogObjective(QuestDialog this$0, EntityPlayer player, Dialog dialog) {
            this.parent = this$0;
            this.player = player;
            this.dialog = dialog;
            this.party = null;
        }

        public QuestDialogObjective(QuestDialog this$0, Party party, Dialog dialog) {
            this.parent = this$0;
            this.party = party;
            this.dialog = dialog;
            this.player = null;
        }

        public int getProgress() {
            return this.isCompleted() ? 1 : 0;
        }

        public void setProgress(int progress) {
            if (progress >= 0 && progress <= 1) {
                if (player != null) {
                    PlayerData data = PlayerData.get(player);
                    boolean completed = data.dialogData.dialogsRead.contains(this.dialog.id);
                    if (progress == 0 && completed) {
                        data.dialogData.dialogsRead.remove(this.dialog.id);
                        data.questData.checkQuestCompletion(data, EnumQuestType.values()[1]);
                        data.updateClient = true;
                        data.save();
                    }

                    if (progress == 1 && !completed) {
                        data.dialogData.dialogsRead.add(this.dialog.id);
                        data.questData.checkQuestCompletion(data, EnumQuestType.values()[1]);
                        data.updateClient = true;
                        data.save();
                    }
                } else if (party != null) {
                    if (party.getObjectiveRequirement() != null) {
                        EnumPartyObjectives objectives = party.getObjectiveRequirement();
                        if (objectives == EnumPartyObjectives.Leader) {
                            EntityPlayer leaderPlayer = NoppesUtilServer.getPlayer(party.getLeaderUUID());
                            PlayerData leaderData;
                            if (leaderPlayer != null) {
                                leaderData = PlayerData.get(leaderPlayer);
                            } else {
                                leaderData = PlayerDataController.Instance.getPlayerDataCache(party.getLeaderUUID().toString());
                            }
                            if (leaderData == null)
                                return;

                            boolean completed = leaderData.dialogData.dialogsRead.contains(this.dialog.id);
                            if (progress == 0 && completed) {
                                leaderData.dialogData.dialogsRead.remove(this.dialog.id);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[1]);
                                leaderData.updateClient = true;
                                leaderData.save();
                            }
                            if (progress == 1 && !completed) {
                                leaderData.dialogData.dialogsRead.add(this.dialog.id);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[1]);
                                leaderData.updateClient = true;
                                leaderData.save();
                            }
                        } else {
                            for (UUID uuid : party.getPlayerUUIDs()) {
                                EntityPlayer individual = NoppesUtilServer.getPlayer(uuid);
                                PlayerData individualData;
                                if (individual != null) {
                                    individualData = PlayerData.get(individual);
                                } else {
                                    individualData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                                }
                                if (individualData != null) {
                                    boolean completed = individualData.dialogData.dialogsRead.contains(this.dialog.id);
                                    if (progress == 0 && completed) {
                                        individualData.dialogData.dialogsRead.remove(this.dialog.id);
                                        individualData.save();
                                        individualData.updateClient = true;
                                    }
                                    if (progress == 1 && !completed) {
                                        individualData.dialogData.dialogsRead.add(this.dialog.id);
                                        individualData.save();
                                        individualData.updateClient = true;
                                    }
                                }
                            }
                            PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[1]);
                        }
                    }
                }
            } else {
                throw new CustomNPCsException("Progress has to be 0 or 1", new Object[0]);
            }
        }

        @Override
        public void setPlayerProgress(String playerName, int progress) {
            if (progress >= 0 && progress <= 1) {
                EntityPlayer foundplayer = NoppesUtilServer.getPlayerByName(playerName);
                if (foundplayer != null && party == null) {
                    PlayerData data = PlayerData.get(foundplayer);
                    boolean completed = data.dialogData.dialogsRead.contains(this.dialog.id);
                    if (progress == 0 && completed) {
                        data.dialogData.dialogsRead.remove(this.dialog.id);
                        data.questData.checkQuestCompletion(data, EnumQuestType.values()[1]);
                        data.updateClient = true;
                        data.save();
                    }

                    if (progress == 1 && !completed) {
                        data.dialogData.dialogsRead.add(this.dialog.id);
                        data.questData.checkQuestCompletion(data, EnumQuestType.values()[1]);
                        data.updateClient = true;
                        data.save();
                    }
                } else if (foundplayer != null) {
                    if (party.getObjectiveRequirement() != null) {
                        EnumPartyObjectives objectives = party.getObjectiveRequirement();
                        if (objectives == EnumPartyObjectives.Leader) {
                            EntityPlayer leaderPlayer = NoppesUtilServer.getPlayer(party.getLeaderUUID());
                            PlayerData leaderData;
                            if (leaderPlayer != null) {
                                leaderData = PlayerData.get(leaderPlayer);
                            } else {
                                leaderData = PlayerDataController.Instance.getPlayerDataCache(party.getLeaderUUID().toString());
                            }
                            if (leaderData == null)
                                return;

                            boolean completed = leaderData.dialogData.dialogsRead.contains(this.dialog.id);
                            if (progress == 0 && completed) {
                                leaderData.dialogData.dialogsRead.remove(this.dialog.id);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[1]);
                                leaderData.updateClient = true;
                                leaderData.save();
                            }
                            if (progress == 1 && !completed) {
                                leaderData.dialogData.dialogsRead.add(this.dialog.id);
                                PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[1]);
                                leaderData.updateClient = true;
                                leaderData.save();
                            }
                        } else {
                            PlayerData individualData = PlayerData.get(foundplayer);
                            if (individualData != null) {
                                boolean completed = individualData.dialogData.dialogsRead.contains(this.dialog.id);
                                if (progress == 0 && completed) {
                                    individualData.dialogData.dialogsRead.remove(this.dialog.id);
                                    individualData.save();
                                    individualData.updateClient = true;
                                }
                                if (progress == 1 && !completed) {
                                    individualData.dialogData.dialogsRead.add(this.dialog.id);
                                    individualData.save();
                                    individualData.updateClient = true;
                                }
                            }
                            PartyController.Instance().checkQuestCompletion(party, EnumQuestType.values()[1]);
                        }
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
            if (player != null) {
                PlayerData data = PlayerData.get(player);
                return data.dialogData.dialogsRead.contains(this.dialog.id);
            } else if (party != null) {
                if (party.getObjectiveRequirement() != null) {
                    EnumPartyObjectives objectives = party.getObjectiveRequirement();
                    if (objectives == EnumPartyObjectives.Leader) {
                        EntityPlayer leaderPlayer = NoppesUtilServer.getPlayer(party.getLeaderUUID());
                        PlayerData leaderData;
                        if (leaderPlayer != null) {
                            leaderData = PlayerData.get(leaderPlayer);
                        } else {
                            leaderData = PlayerDataController.Instance.getPlayerDataCache(party.getLeaderUUID().toString());
                        }
                        if (leaderData == null)
                            return false;
                        return leaderData.dialogData.dialogsRead.contains(this.dialog.id);
                    } else {
                        boolean requiresOneRead = objectives == EnumPartyObjectives.Shared;
                        for (UUID uuid : party.getPlayerUUIDs()) {
                            EntityPlayer individual = NoppesUtilServer.getPlayer(uuid);
                            PlayerData individualData;
                            if (individual != null) {
                                individualData = PlayerData.get(individual);
                            } else {
                                individualData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                            }
                            if (individualData != null) {
                                boolean read = individualData.dialogData.dialogsRead.contains(this.dialog.id);
                                if (requiresOneRead && read) {
                                    return true;
                                } else if (!requiresOneRead && !read) {
                                    return false;
                                }
                            }
                        }
                        return !requiresOneRead;
                    }
                }
            }
            return true;
        }

        public String getText() {
            return this.dialog.title + (this.isCompleted() ? " (read)" : " (unread)");
        }

        @Override
        public String getAdditionalText() {
            if (party != null && party.getObjectiveRequirement() == EnumPartyObjectives.All) {
                List<String> incomplete = new ArrayList<>();
                for (UUID uuid : party.getPlayerUUIDs()) {
                    EntityPlayer individual = NoppesUtilServer.getPlayer(uuid);
                    PlayerData individualData;
                    if (individual != null) {
                        individualData = PlayerData.get(individual);
                    } else {
                        individualData = PlayerDataController.Instance.getPlayerDataCache(uuid.toString());
                    }
                    if (individualData != null) {
                        boolean read = individualData.dialogData.dialogsRead.contains(this.dialog.id);
                        if (!read) {
                            incomplete.add(individualData.playername);
                        }
                    }
                }
                if (!incomplete.isEmpty())
                    return "[" + String.join(", ", incomplete) + "]";

            }
            return null;
        }
    }

}
