package noppes.npcs.controllers.data;

import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.AchievementPacket;
import kamkeel.npcs.network.packets.data.ChatAlertPacket;
import kamkeel.npcs.network.packets.data.OverlayQuestTrackingPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.data.IParty;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerQuestData implements IPlayerQuestData {
    private final PlayerData parent;
    private IQuest trackedQuest = null;

    public HashMap<Integer, QuestData> activeQuests = new HashMap<Integer, QuestData>();
    public HashMap<Integer, Long> finishedQuests = new HashMap<Integer, Long>();

    public PlayerQuestData(PlayerData parent) {
        this.parent = parent;
    }

    public void loadNBTData(NBTTagCompound mainCompound) {
        if (mainCompound == null)
            return;
        NBTTagCompound compound = mainCompound.getCompoundTag("QuestData");

        NBTTagList list = compound.getTagList("CompletedQuests", 10);
        if (list != null) {
            HashMap<Integer, Long> finishedQuests = new HashMap<Integer, Long>();
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                finishedQuests.put(nbttagcompound.getInteger("Quest"), nbttagcompound.getLong("Date"));
            }
            this.finishedQuests = finishedQuests;
        }

        NBTTagList list2 = compound.getTagList("ActiveQuests", 10);
        if (list2 != null) {
            HashMap<Integer, QuestData> activeQuests = new HashMap<Integer, QuestData>();
            for (int i = 0; i < list2.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list2.getCompoundTagAt(i);
                int id = nbttagcompound.getInteger("Quest");
                Quest quest = QuestController.Instance.quests.get(id);
                if (quest == null)
                    continue;
                QuestData data = new QuestData(quest);
                data.readEntityFromNBT(nbttagcompound);
                activeQuests.put(id, data);
            }
            this.activeQuests = activeQuests;
        }

        this.trackedQuest = QuestController.Instance.get(mainCompound.getInteger("TrackedQuestID"));
    }

    public void saveNBTData(NBTTagCompound maincompound) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (int quest : finishedQuests.keySet()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("Quest", quest);
            nbttagcompound.setLong("Date", finishedQuests.get(quest));
            list.appendTag(nbttagcompound);
        }

        compound.setTag("CompletedQuests", list);

        NBTTagList list2 = new NBTTagList();
        for (int quest : activeQuests.keySet()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("Quest", quest);
            activeQuests.get(quest).writeEntityToNBT(nbttagcompound);
            list2.appendTag(nbttagcompound);
        }

        compound.setTag("ActiveQuests", list2);

        maincompound.setTag("QuestData", compound);

        if (this.trackedQuest != null) {
            maincompound.setInteger("TrackedQuestID", this.trackedQuest.getId());
        } else {
            maincompound.setInteger("TrackedQuestID", -1);
        }
    }

    public QuestData getQuestCompletion(EntityPlayer player, EntityNPCInterface npc) {
        PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
        for (QuestData data : activeQuests.values()) {
            Quest quest = data.quest;
            if (quest != null && quest.completion == EnumQuestCompletion.Npc && quest.completerNpc.equals(npc.getCommandSenderName()) && quest.questInterface.isCompleted(playerData)) {
                return data;
            }
        }
        return null;
    }

    public Party getPartyQuestCompletion(EntityPlayer player, EntityNPCInterface npc) {
        PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
        if (playerData != null) {
            Party party = playerData.getPlayerParty();
            if (party != null) {
                QuestData data = party.getQuestData();
                if (data != null) {
                    Quest quest = data.quest;
                    if (quest != null && quest.completion == EnumQuestCompletion.Npc && quest.completerNpc.equals(npc.getCommandSenderName()) && quest.questInterface.isPartyCompleted(party)) {
                        return party;
                    }
                }
            }
        }
        return null;
    }

    public boolean checkQuestCompletion(PlayerData playerData, EnumQuestType type) {
        boolean bo = false;
        EntityPlayer player = playerData.player;

        int partyQuestID = -1;
        if (playerData.partyUUID != null) {
            Party party = PartyController.Instance().getParty(playerData.partyUUID);
            if (party != null) {
                IQuest quest = party.getQuest();
                if (quest != null) {
                    partyQuestID = quest.getId();
                }
            }
        }

        ArrayList<QuestData> activeQuestValues = new ArrayList<>(this.activeQuests.values());
        for (QuestData data : activeQuestValues) {
            if (data.quest.type != type && type != null)
                continue;

            // Check if Active Quest = Party Quest
            if (partyQuestID != -1 && data.quest.id == partyQuestID && data.quest.partyOptions.isAllowParty()) {
                continue;
            }

            if (data.quest.partyOptions.allowParty && data.quest.partyOptions.onlyParty)
                continue;

            QuestInterface inter = data.quest.questInterface;
            if (inter.isCompleted(playerData)) {
                if ((!data.isCompleted && data.quest.completion == EnumQuestCompletion.Npc) || data.quest.instantComplete(player, data)) {
                    data.isCompleted = true;
                    if (data.quest.completion == EnumQuestCompletion.Npc) {
                        EventHooks.onQuestFinished(player, data.quest);
                    }
                    bo = true;
                }
            } else {
                data.isCompleted = false;
            }

            if (this.trackedQuest != null && data.quest.getId() == this.trackedQuest.getId()) {
                NoppesUtilPlayer.sendTrackedQuestData((EntityPlayerMP) player);
            }
        }
        QuestItem.pickedUp = null;
        return bo;
    }

    public void trackQuest(IQuest quest) {
        if (this.trackedQuest == null || quest.getId() != this.trackedQuest.getId()) {
            this.trackedQuest = quest;
            NoppesUtilPlayer.sendTrackedQuestData((EntityPlayerMP) this.parent.player);
        }
    }

    public void trackParty(IParty party) {
        if (party == null)
            return;

        IQuest quest = party.getQuest();
        if (this.trackedQuest == null || quest.getId() != this.trackedQuest.getId()) {
            this.trackedQuest = quest;
            NoppesUtilPlayer.sendPartyTrackedQuestData((EntityPlayerMP) this.parent.player, (Party) party);
        }
    }

    public void untrackQuest() {
        if (this.trackedQuest != null) {
            this.trackedQuest = null;
            PacketHandler.Instance.sendToPlayer(new OverlayQuestTrackingPacket(new NBTTagCompound()), (EntityPlayerMP) this.parent.player);
        }
    }

    public IQuest getTrackedQuest() {
        return this.trackedQuest;
    }

    public void startQuest(int id) {
        Quest quest = QuestController.Instance.quests.get(id);
        if (quest == null)
            return;
        if (activeQuests.containsKey(id))
            return;
        QuestData questdata = new QuestData(quest);
        activeQuests.put(id, questdata);

        AchievementPacket.sendAchievement((EntityPlayerMP) parent.player, false, "quest.newquest", quest.title);
        ChatAlertPacket.sendChatAlert((EntityPlayerMP) parent.player, "quest.newquest", ": ", quest.title);
        parent.updateClient = true;
    }

    public void finishQuest(int id) {
        Quest quest = QuestController.Instance.quests.get(id);
        if (quest == null)
            return;
        PlayerQuestController.setQuestFinished(quest, parent.player);
        parent.updateClient = true;
    }

    public void stopQuest(int id) {
        Quest quest = QuestController.Instance.quests.get(id);
        if (quest == null)
            return;
        activeQuests.remove(id);
        parent.updateClient = true;
    }

    public void removeQuest(int id) {
        Quest quest = QuestController.Instance.quests.get(id);
        if (quest == null)
            return;
        activeQuests.remove(id);
        finishedQuests.remove(id);
        parent.updateClient = true;
    }

    public boolean hasFinishedQuest(int id) {
        return finishedQuests.containsKey(id);
    }

    public boolean hasActiveQuest(int id) {
        return activeQuests.containsKey(id);
    }

    public IQuest[] getActiveQuests() {
        List<IQuest> quests = new ArrayList<>();

        for (int id : activeQuests.keySet()) {
            IQuest quest = (IQuest) QuestController.Instance.quests.get(id);
            if (quest != null) {
                quests.add(quest);
            }
        }

        return (IQuest[]) quests.toArray(new IQuest[0]);
    }

    public IQuest[] getFinishedQuests() {
        List<IQuest> quests = new ArrayList<>();

        for (int id : finishedQuests.keySet()) {
            IQuest quest = (IQuest) QuestController.Instance.quests.get(id);
            if (quest != null) {
                quests.add(quest);
            }
        }

        return (IQuest[]) quests.toArray(new IQuest[0]);
    }

    public long getLastCompletedTime(int id) {
        if (this.hasFinishedQuest(id)) {
            return this.finishedQuests.get(id);
        }
        return 0;
    }

    public void setLastCompletedTime(int id, long time) {
        Quest quest = QuestController.Instance.quests.get(id);
        if (quest == null)
            return;
        this.finishedQuests.put(id, time);
    }
}
