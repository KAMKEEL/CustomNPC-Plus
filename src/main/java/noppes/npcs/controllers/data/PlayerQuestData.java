package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.data.IQuest;

public class PlayerQuestData implements IPlayerQuestData {
	private final PlayerData parent;
	public IQuest trackedQuest = null;

	public HashMap<Integer,QuestData> activeQuests = new HashMap<Integer,QuestData>();
	public HashMap<Integer,Long> finishedQuests = new HashMap<Integer,Long>();

	public PlayerQuestData(PlayerData parent) {
		this.parent = parent;
	}

	public void loadNBTData(NBTTagCompound mainCompound) {
		if(mainCompound == null)
			return;
		NBTTagCompound compound = mainCompound.getCompoundTag("QuestData");
		
        NBTTagList list = compound.getTagList("CompletedQuests", 10);
        if(list != null){
        	HashMap<Integer,Long> finishedQuests = new HashMap<Integer,Long>();
            for(int i = 0; i < list.tagCount(); i++)
            {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                finishedQuests.put(nbttagcompound.getInteger("Quest"),nbttagcompound.getLong("Date"));
            }
            this.finishedQuests = finishedQuests;
        }
		
        NBTTagList list2 = compound.getTagList("ActiveQuests", 10);
        if(list2 != null){
        	HashMap<Integer,QuestData> activeQuests = new HashMap<Integer,QuestData>();
            for(int i = 0; i < list2.tagCount(); i++)
            {
                NBTTagCompound nbttagcompound = list2.getCompoundTagAt(i);
                int id = nbttagcompound.getInteger("Quest");
                Quest quest = QuestController.instance.quests.get(id);
                if(quest == null)
                	continue;
                QuestData data = new QuestData(quest);
                data.readEntityFromNBT(nbttagcompound);
                activeQuests.put(id,data);
            }
            this.activeQuests = activeQuests;
        }

		this.trackedQuest = QuestController.instance.get(mainCompound.getInteger("TrackedQuestID"));
	}

	public void saveNBTData(NBTTagCompound maincompound) {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(int quest : finishedQuests.keySet()){
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Quest", quest);
			nbttagcompound.setLong("Date", finishedQuests.get(quest));
			list.appendTag(nbttagcompound);
		}
		
		compound.setTag("CompletedQuests", list);
		
		NBTTagList list2 = new NBTTagList();
		for(int quest : activeQuests.keySet()){
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

	public QuestData getQuestCompletion(EntityPlayer player,EntityNPCInterface npc) {
		PlayerData playerData = PlayerDataController.instance.getPlayerData(player);
		for(QuestData data : activeQuests.values()){
			Quest quest = data.quest;
			if(quest != null && quest.completion == EnumQuestCompletion.Npc && quest.completerNpc.equals(npc.getCommandSenderName()) && quest.questInterface.isCompleted(playerData)){
				return data;
			}
		}
		return null;
	}

	public boolean checkQuestCompletion(PlayerData playerData,EnumQuestType type) {
		boolean bo = false;
		EntityPlayer player = playerData.player;

		ArrayList<QuestData> activeQuestValues = new ArrayList<>(this.activeQuests.values());
		for(QuestData data : activeQuestValues){
			if(data.quest.type != type && type != null)
				continue;

			NoppesUtilPlayer.sendTrackedQuestData((EntityPlayerMP) player,data.quest);

			QuestInterface inter =  data.quest.questInterface;
			if(inter.isCompleted(playerData)){
				if(!data.isCompleted){
					if(!data.quest.complete(player,data) || data.quest.completeText.isEmpty()){
						Server.sendData((EntityPlayerMP)player, EnumPacketClient.MESSAGE, "quest.completed", data.quest.title);
						Server.sendData((EntityPlayerMP)player, EnumPacketClient.CHAT, "quest.completed",": ",data.quest.title);
					}
					data.isCompleted = true;
					bo = true;
					EventHooks.onQuestFinished(player,data.quest);
				}
			}
			else
				data.isCompleted = false;
		}
		QuestItem.pickedUp = null;
		return bo;
		
	}

	public IQuest getTrackedQuest() {
		return trackedQuest;
	}

	public void startQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null)
			return;
		if(activeQuests.containsKey(id))
			return;
		QuestData questdata = new QuestData(quest);
		activeQuests.put(id, questdata);
		Server.sendData((EntityPlayerMP)parent.player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
		Server.sendData((EntityPlayerMP)parent.player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
	}

	public void finishQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null)
			return;
		finishedQuests.put(id, System.currentTimeMillis());
	}

	public void stopQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null)
			return;
		activeQuests.remove(id);
	}

	public void removeQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null)
			return;
		activeQuests.remove(id);
		finishedQuests.remove(id);
	}

	public boolean hasFinishedQuest(int id){
		return finishedQuests.containsKey(id);
	}

	public boolean hasActiveQuest(int id){
		return activeQuests.containsKey(id);
	}

	public IQuest[] getActiveQuests() {
		List<IQuest> quests = new ArrayList<>();

		for (int id : activeQuests.keySet()) {
			IQuest quest = (IQuest) QuestController.instance.quests.get(id);
			if (quest != null) {
				quests.add(quest);
			}
		}

		return (IQuest[])quests.toArray(new IQuest[0]);
	}

	public IQuest[] getFinishedQuests() {
		List<IQuest> quests = new ArrayList<>();

		for (int id : finishedQuests.keySet()) {
			IQuest quest = (IQuest) QuestController.instance.quests.get(id);
			if (quest != null) {
				quests.add(quest);
			}
		}

		return (IQuest[])quests.toArray(new IQuest[0]);
	}
}
