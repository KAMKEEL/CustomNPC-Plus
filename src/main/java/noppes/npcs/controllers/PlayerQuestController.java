package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.quests.QuestDialog;

import java.util.Vector;

public class PlayerQuestController {

	public static boolean hasActiveQuests(EntityPlayer player){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return !data.activeQuests.isEmpty();
	}
	
	public static boolean isQuestActive(EntityPlayer player, int quest){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return data.activeQuests.containsKey(quest);
	}
	
	public static boolean isQuestFinished(EntityPlayer player, int questid){
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		return data.finishedQuests.containsKey(questid);
	}

	public static void addActiveQuest(Quest quest, EntityPlayer player) {
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		if(canQuestBeAccepted(quest, player)){
			data.activeQuests.put(quest.id,new QuestData(quest));	
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.MESSAGE, "quest.newquest", quest.title);
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.title);
		}
	}
	
	public static void setQuestFinished(Quest quest, EntityPlayer player){
		PlayerData playerdata = PlayerDataController.instance.getPlayerData(player);
		PlayerQuestData data = playerdata.questData;
		data.activeQuests.remove(quest.id);
		if(quest.repeat == EnumQuestRepeat.RLDAILY || quest.repeat == EnumQuestRepeat.RLWEEKLY)
			data.finishedQuests.put(quest.id, System.currentTimeMillis());
		else
			data.finishedQuests.put(quest.id,player.worldObj.getTotalWorldTime());
		if(quest.repeat != EnumQuestRepeat.NONE && quest.type == EnumQuestType.Dialog){
			QuestDialog questdialog = (QuestDialog) quest.questInterface;
			for(int dialog : questdialog.dialogs.values()){
				playerdata.dialogData.dialogsRead.remove(dialog);
			}
		}
	}
	public static boolean canQuestBeAccepted(Quest quest, EntityPlayer player){
		if(quest == null)
			return false;

		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		if(data.activeQuests.containsKey(quest.id))
			return false;
		
		if(!data.finishedQuests.containsKey(quest.id) || quest.repeat == EnumQuestRepeat.REPEATABLE)
			return true;
		if(quest.repeat == EnumQuestRepeat.NONE)
			return false;
		
		long questTime = data.finishedQuests.get(quest.id);
		
		if(quest.repeat == EnumQuestRepeat.MCDAILY){
			return player.worldObj.getTotalWorldTime() - questTime >= 24000;
		}
		else if(quest.repeat == EnumQuestRepeat.MCWEEKLY){
			return player.worldObj.getTotalWorldTime() - questTime >= 168000;
		}
		else if(quest.repeat == EnumQuestRepeat.RLDAILY){
			return System.currentTimeMillis() - questTime >= 86400000;
		}
		else if(quest.repeat == EnumQuestRepeat.RLWEEKLY){
			return System.currentTimeMillis() - questTime >= 604800000;
		}
		return false;
	}
	public static Vector<Quest> getActiveQuests(EntityPlayer player)
	{
		Vector<Quest> quests = new Vector<Quest>();
		PlayerQuestData data = PlayerDataController.instance.getPlayerData(player).questData;
		for(QuestData questdata: data.activeQuests.values()){
			if(questdata.quest == null)
				continue;
			quests.add(questdata.quest);
		}
		return quests;
	}
}
