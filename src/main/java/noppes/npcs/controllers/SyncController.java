package noppes.npcs.controllers;

import io.netty.buffer.ByteBuf;
import kamkeel.addon.DBCAddon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.SyncType;
import noppes.npcs.controllers.data.*;

import java.util.HashMap;

public class SyncController {

	public static void syncPlayer(EntityPlayerMP player){
		NBTTagList list = new NBTTagList();
		NBTTagCompound compound = new NBTTagCompound();

        ////////////////////////////////////////////////////////////////////////////////
        // Faction Sync
        for(Faction faction : FactionController.getInstance().factions.values()){
            NBTTagCompound factioNBT = new NBTTagCompound();
            faction.writeNBT(factioNBT);
            list.appendTag(factioNBT);
            if(list.tagCount() > 10){
                compound = new NBTTagCompound();
                compound.setTag("Data", list);
                Server.sendData(player, EnumPacketClient.SYNC_ADD, SyncType.FACTION, compound);
                list = new NBTTagList();
            }
        }
        compound = new NBTTagCompound();
        compound.setTag("Data", list);
        Server.sendData(player, EnumPacketClient.SYNC_END, SyncType.FACTION, compound);
        ////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////
        // Quest Sync
        for(QuestCategory category : QuestController.Instance.categories.values()){
            NBTTagCompound questCompound;
            NBTTagList questList = new NBTTagList();
            for(int questID : category.quests.keySet()){
                Quest quest = category.quests.get(questID);
                questList.appendTag(quest.writeToNBT(new NBTTagCompound()));
                if(questList.tagCount() > 40){
                    questCompound = new NBTTagCompound();
                    questCompound.setTag("Data", questList);
                    questCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
                    Server.sendData(player, EnumPacketClient.SYNC_ADD, SyncType.QUEST_CATEGORY, questCompound);
                    questList = new NBTTagList();
                }
            }
            questCompound = new NBTTagCompound();
            questCompound.setTag("Data", questList);
            questCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
            Server.sendData(player, EnumPacketClient.SYNC_ADD, SyncType.QUEST_CATEGORY, questCompound);
        }
        Server.sendData(player, EnumPacketClient.SYNC_END, SyncType.QUEST_CATEGORY, new NBTTagCompound());
        ////////////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////////////
        // Dialog Sync
        for(DialogCategory category : DialogController.Instance.categories.values()){
            NBTTagCompound dialogCompound;
            NBTTagList dialogList = new NBTTagList();
            for(int dialogID : category.dialogs.keySet()){
                Dialog dialog = category.dialogs.get(dialogID);
                dialogList.appendTag(dialog.writeToNBT(new NBTTagCompound()));
                if(dialogList.tagCount() > 40){
                    dialogCompound = new NBTTagCompound();
                    dialogCompound.setTag("Data", dialogList);
                    dialogCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
                    Server.sendData(player, EnumPacketClient.SYNC_ADD, SyncType.DIALOG_CATEGORY, dialogCompound);
                    dialogList = new NBTTagList();
                }
            }
            dialogCompound = new NBTTagCompound();
            dialogCompound.setTag("Data", dialogList);
            dialogCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
            Server.sendData(player, EnumPacketClient.SYNC_ADD, SyncType.DIALOG_CATEGORY, dialogCompound);
        }
        Server.sendData(player, EnumPacketClient.SYNC_END, SyncType.DIALOG_CATEGORY, new NBTTagCompound());
        ////////////////////////////////////////////////////////////////////////////////

        DBCAddon.instance.syncPlayer(player);

		PlayerData data = PlayerData.get(player);
        if(data != null){
            NoppesUtilServer.sendPlayerDataCompound(player, data.getSyncNBTFull(), false);
        }
	}

	public static void syncAllDialogs() {
        for(DialogCategory category : DialogController.Instance.categories.values()){
            NBTTagCompound dialogCompound;
            NBTTagList dialogList = new NBTTagList();
            for(int dialogID : category.dialogs.keySet()){
                Dialog dialog = category.dialogs.get(dialogID);
                dialogList.appendTag(dialog.writeToNBT(new NBTTagCompound()));
                if(dialogList.tagCount() > 40){
                    dialogCompound = new NBTTagCompound();
                    dialogCompound.setTag("Data", dialogList);
                    dialogCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
                    Server.sendToAll(EnumPacketClient.SYNC_ADD, SyncType.DIALOG_CATEGORY, dialogCompound);
                    dialogList = new NBTTagList();
                }
            }
            dialogCompound = new NBTTagCompound();
            dialogCompound.setTag("Data", dialogList);
            dialogCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
            Server.sendToAll(EnumPacketClient.SYNC_ADD, SyncType.DIALOG_CATEGORY, dialogCompound);
        }
        Server.sendToAll(EnumPacketClient.SYNC_END, SyncType.DIALOG_CATEGORY, new NBTTagCompound());
	}

	public static void syncAllQuests() {
        for(QuestCategory category : QuestController.Instance.categories.values()){
            NBTTagCompound questCompound;
            NBTTagList questList = new NBTTagList();
            for(int questID : category.quests.keySet()){
                Quest quest = category.quests.get(questID);
                questList.appendTag(quest.writeToNBT(new NBTTagCompound()));
                if(questList.tagCount() > 40){
                    questCompound = new NBTTagCompound();
                    questCompound.setTag("Data", questList);
                    questCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
                    Server.sendToAll(EnumPacketClient.SYNC_ADD, SyncType.QUEST_CATEGORY, questCompound);
                    questList = new NBTTagList();
                }
            }
            questCompound = new NBTTagCompound();
            questCompound.setTag("Data", questList);
            questCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
            Server.sendToAll(EnumPacketClient.SYNC_ADD, SyncType.QUEST_CATEGORY, questCompound);
        }
        Server.sendToAll(EnumPacketClient.SYNC_END, SyncType.QUEST_CATEGORY, new NBTTagCompound());
	}

    public static void updateQuestCat(QuestCategory questCategory) {
        NBTTagCompound questCompound;
        NBTTagList questList = new NBTTagList();
        for(int questID : questCategory.quests.keySet()){
            Quest quest = questCategory.quests.get(questID);
            questList.appendTag(quest.writeToNBT(new NBTTagCompound()));
            if(questList.tagCount() > 40){
                questCompound = new NBTTagCompound();
                questCompound.setTag("Data", questList);
                questCompound.setTag("CatNBT", questCategory.writeSmallNBT(new NBTTagCompound()));
                Server.sendToAll(EnumPacketClient.SYNC_UPDATE, SyncType.QUEST_CATEGORY, questCompound);
                questList = new NBTTagList();
            }
        }
        questCompound = new NBTTagCompound();
        questCompound.setTag("Data", questList);
        questCompound.setTag("CatNBT", questCategory.writeSmallNBT(new NBTTagCompound()));
        Server.sendToAll(EnumPacketClient.SYNC_UPDATE, SyncType.QUEST_CATEGORY, questCompound);
    }

    public static void updateDialogCat(DialogCategory dialogCategory) {
        NBTTagCompound dialogCompound;
        NBTTagList dialogList = new NBTTagList();
        for(int questID : dialogCategory.dialogs.keySet()){
            Dialog dialog = dialogCategory.dialogs.get(questID);
            dialogList.appendTag(dialog.writeToNBT(new NBTTagCompound()));
            if(dialogList.tagCount() > 40){
                dialogCompound = new NBTTagCompound();
                dialogCompound.setTag("Data", dialogList);
                dialogCompound.setTag("CatNBT", dialogCategory.writeSmallNBT(new NBTTagCompound()));
                Server.sendToAll(EnumPacketClient.SYNC_UPDATE, SyncType.DIALOG_CATEGORY, dialogCompound);
                dialogList = new NBTTagList();
            }
        }
        dialogCompound = new NBTTagCompound();
        dialogCompound.setTag("Data", dialogList);
        dialogCompound.setTag("CatNBT", dialogCategory.writeSmallNBT(new NBTTagCompound()));
        Server.sendToAll(EnumPacketClient.SYNC_UPDATE, SyncType.DIALOG_CATEGORY, dialogCompound);
    }

	public static void clientSync(int synctype, NBTTagCompound compound, boolean syncEnd) {
		if(synctype == SyncType.FACTION){
			NBTTagList list = compound.getTagList("Data", 10);
            for(int i = 0; i < list.tagCount(); i++)
            {
	            Faction faction = new Faction();
	            faction.readNBT(list.getCompoundTagAt(i));
            	FactionController.getInstance().factionsSync.put(faction.id, faction);
            }
	        if(syncEnd){
	        	FactionController.getInstance().factions = FactionController.getInstance().factionsSync;
	        	FactionController.getInstance().factionsSync = new HashMap<Integer, Faction>();
	        }
		}
		else if(synctype == SyncType.QUEST_CATEGORY){
            if(!compound.hasNoTags()){
                QuestCategory category = new QuestCategory();
                category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                NBTTagList list = compound.getTagList("Data", 10);
                if(QuestController.Instance.categoriesSync.containsKey(category.id)){
                    category = QuestController.Instance.categoriesSync.get(category.id);
                    category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                }
                for(int i = 0; i < list.tagCount(); i++)
                {
                    Quest quest = new Quest();
                    quest.readNBT(list.getCompoundTagAt(i));
                    quest.category = category;
                    category.quests.put(quest.id, quest);
                }
                QuestController.Instance.categoriesSync.put(category.id, category);
            }
	        if(syncEnd){
	        	HashMap<Integer,Quest> quests = new HashMap<Integer, Quest>();
	        	for(QuestCategory category : QuestController.Instance.categoriesSync.values()){
	        		for(Quest quest : category.quests.values()){
	        			quests.put(quest.id, quest);
	        		}
	        	}
	        	QuestController.Instance.categories = QuestController.Instance.categoriesSync;
	        	QuestController.Instance.quests = quests;
	        	QuestController.Instance.categoriesSync = new HashMap<Integer, QuestCategory>();
	        }
		}
		else if(synctype == SyncType.DIALOG_CATEGORY){
            if(!compound.hasNoTags()){
                DialogCategory category = new DialogCategory();
                category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                NBTTagList list = compound.getTagList("Data", 10);
                if(DialogController.Instance.categoriesSync.containsKey(category.id)){
                    category = DialogController.Instance.categoriesSync.get(category.id);
                    category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                }
                for(int i = 0; i < list.tagCount(); i++)
                {
                    Dialog dialog = new Dialog();
                    dialog.readNBT(list.getCompoundTagAt(i));
                    dialog.category = category;
                    category.dialogs.put(dialog.id, dialog);
                }
                DialogController.Instance.categoriesSync.put(category.id, category);
            }
	        if(syncEnd){
	        	HashMap<Integer,Dialog> dialogs = new HashMap<Integer, Dialog>();
	        	for(DialogCategory category : DialogController.Instance.categoriesSync.values()){
	        		for(Dialog dialog : category.dialogs.values()){
	        			dialogs.put(dialog.id, dialog);
	        		}
	        	}
	        	DialogController.Instance.categories = DialogController.Instance.categoriesSync;
	        	DialogController.Instance.dialogs = dialogs;
	        	DialogController.Instance.categoriesSync = new HashMap<Integer, DialogCategory>();
	        }
		}
	}

	public static void clientSyncUpdate(int synctype, NBTTagCompound compound, ByteBuf buffer) {
		if(synctype == SyncType.FACTION){
			Faction faction = new Faction();
			faction.readNBT(compound);
			FactionController.getInstance().factions.put(faction.id, faction);
		}
		else if(synctype == SyncType.DIALOG){
			DialogCategory category = DialogController.Instance.categories.get(buffer.readInt());
			Dialog dialog = new Dialog();
            dialog.category = category;
			dialog.readNBT(compound);
			DialogController.Instance.dialogs.put(dialog.id, dialog);
			category.dialogs.put(dialog.id, dialog);
		}
		else if(synctype == SyncType.DIALOG_CATEGORY){
            DialogCategory category = new DialogCategory();
            category.readSmallNBT(compound.getCompoundTag("CatNBT"));
            NBTTagList list = compound.getTagList("Data", 10);
            if(DialogController.Instance.categoriesSync.containsKey(category.id)){
                category = DialogController.Instance.categoriesSync.get(category.id);
                category.readSmallNBT(compound.getCompoundTag("CatNBT"));
            }
            for(int i = 0; i < list.tagCount(); i++)
            {
                Dialog dialog = new Dialog();
                dialog.readNBT(list.getCompoundTagAt(i));
                dialog.category = category;
                category.dialogs.put(dialog.id, dialog);
            }
            DialogController.Instance.categories.put(category.id, category);
		}
		else if(synctype == SyncType.QUEST){
			QuestCategory category = QuestController.Instance.categories.get(buffer.readInt());
			Quest quest = new Quest();
            quest.category = category;
			quest.readNBT(compound);
			QuestController.Instance.quests.put(quest.id, quest);
			category.quests.put(quest.id, quest);
		}
		else if(synctype == SyncType.QUEST_CATEGORY){
			QuestCategory category = new QuestCategory();
            category.readSmallNBT(compound.getCompoundTag("CatNBT"));
            NBTTagList list = compound.getTagList("Data", 10);
            if(QuestController.Instance.categoriesSync.containsKey(category.id)){
                category = QuestController.Instance.categoriesSync.get(category.id);
                category.readSmallNBT(compound.getCompoundTag("CatNBT"));
            }
            for(int i = 0; i < list.tagCount(); i++)
            {
                Quest quest = new Quest();
                quest.readNBT(list.getCompoundTagAt(i));
                quest.category = category;
                category.quests.put(quest.id, quest);
            }
			QuestController.Instance.categories.put(category.id, category);
		}
	}

	public static void clientSyncRemove(int synctype, int id) {
		if(synctype == SyncType.FACTION){
			FactionController.getInstance().factions.remove(id);
		}
		else if(synctype == SyncType.DIALOG){
			Dialog dialog = DialogController.Instance.dialogs.remove(id);
			if(dialog != null) {
				dialog.category.dialogs.remove(id);
			}
		}
		else if(synctype == SyncType.DIALOG_CATEGORY){
			DialogCategory category = DialogController.Instance.categories.remove(id);
			if(category != null) {
				DialogController.Instance.dialogs.keySet().removeAll(category.dialogs.keySet());
			}
		}
		else if(synctype == SyncType.QUEST){
			Quest quest = QuestController.Instance.quests.remove(id);
			if(quest != null) {
				quest.category.quests.remove(id);
			}
		}
		else if(synctype == SyncType.QUEST_CATEGORY){
			QuestCategory category = QuestController.Instance.categories.remove(id);
			if(category != null) {
				QuestController.Instance.quests.keySet().removeAll(category.quests.keySet());
			}
		}
	}

}
