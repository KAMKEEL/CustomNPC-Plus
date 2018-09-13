package noppes.npcs.controllers;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestInterface;

public class PlayerQuestData {
	public HashMap<Integer,QuestData> activeQuests = new HashMap<Integer,QuestData>();
	public HashMap<Integer,Long> finishedQuests = new HashMap<Integer,Long>();
	
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
	}

	public QuestData getQuestCompletion(EntityPlayer player,EntityNPCInterface npc) {
		for(QuestData data : activeQuests.values()){
			Quest quest = data.quest;
			if(quest != null && quest.completion == EnumQuestCompletion.Npc && quest.completerNpc.equals(npc.getCommandSenderName()) && quest.questInterface.isCompleted(player)){
				return data;
			}
		}
		return null;
	}

	public boolean checkQuestCompletion(EntityPlayer player,EnumQuestType type) {
		boolean bo = false;
		for(QuestData data : this.activeQuests.values()){
			if(data.quest.type != type && type != null)
				continue;
			
			QuestInterface inter =  data.quest.questInterface;
			
			if(inter.isCompleted(player)){
				if(!data.isCompleted){
					if(!data.quest.complete(player,data)){
						Server.sendData((EntityPlayerMP)player, EnumPacketClient.MESSAGE, "quest.completed", data.quest.title);
						Server.sendData((EntityPlayerMP)player, EnumPacketClient.CHAT, "quest.completed",": ",data.quest.title);
					}
					data.isCompleted = true;
					bo = true;
				}
			}
			else
				data.isCompleted = false;
		}
		return bo;
		
	}
	
}
