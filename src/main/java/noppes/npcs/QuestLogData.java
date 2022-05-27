package noppes.npcs;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.Quest;

public class QuestLogData {
	public String trackedQuestKey = "";
	public HashMap<String,Vector<String>> categories = new HashMap<String,Vector<String>>();
	public String selectedQuest = "";
	public String selectedCategory = "";
	public HashMap<String,String> questText = new HashMap<String,String>();
	public HashMap<String,Vector<String>> questStatus = new HashMap<String,Vector<String>>();
	public HashMap<String,String> finish = new HashMap<String,String>();
	
	public NBTTagCompound writeNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Categories", NBTTags.nbtVectorMap(categories));
		compound.setTag("Logs", NBTTags.nbtStringStringMap(questText));
		compound.setTag("Status", NBTTags.nbtVectorMap(questStatus));
		compound.setTag("QuestFinisher", NBTTags.nbtStringStringMap(finish));
		compound.setString("TrackedQuestID", trackedQuestKey);
		return compound;
	}

	public void readNBT(NBTTagCompound compound){
		categories = NBTTags.getVectorMap(compound.getTagList("Categories", 10));
		questText = NBTTags.getStringStringMap(compound.getTagList("Logs", 10));
		questStatus = NBTTags.getVectorMap(compound.getTagList("Status", 10));
		finish = NBTTags.getStringStringMap(compound.getTagList("QuestFinisher", 10));
		trackedQuestKey = compound.getString("TrackedQuestID");
	}
	public void setData(EntityPlayer player){
		PlayerData playerData = PlayerDataController.instance.getPlayerData(player);

		for(Quest quest : PlayerQuestController.getActiveQuests(player))
        {
    		String category = quest.category.title;
    		if(!categories.containsKey(category))
    			categories.put(category, new Vector<String>());
    		Vector<String> list = categories.get(category);
    		list.add(quest.title);
    		
    		questText.put(category + ":" + quest.title, quest.logText);

    		questStatus.put(category + ":" + quest.title, quest.questInterface.getQuestLogStatus(player));
    		if(quest.completion == EnumQuestCompletion.Npc && quest.questInterface.isCompleted(playerData))
    			finish.put(category + ":" + quest.title, quest.completerNpc);

			if (playerData.questData.trackedQuest != null) {
				if (quest.id == playerData.questData.trackedQuest.getId()) {
					trackedQuestKey = category + ":" + quest.title;
				}
			}
        }
	}

	public boolean hasSelectedQuest() {
		return !selectedQuest.isEmpty();
	}

	public String getQuestText() {
		return questText.get(selectedCategory + ":" + selectedQuest);
	}

	public Vector<String> getQuestStatus() {
		return questStatus.get(selectedCategory + ":" + selectedQuest);
	}

	public String getComplete() {
		return finish.get(selectedCategory + ":" + selectedQuest);
	}
}
