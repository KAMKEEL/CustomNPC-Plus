package noppes.npcs.quests;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.controllers.QuestData;

public class QuestKill extends QuestInterface{
	public HashMap<String,Integer> targets = new HashMap<String,Integer>();

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		targets = NBTTags.getStringIntegerMap(compound.getTagList("QuestDialogs", 10));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("QuestDialogs", NBTTags.nbtStringIntegerMap(targets));
	}

	@Override
	public boolean isCompleted(EntityPlayer player) {
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return false;
		HashMap<String,Integer> killed = getKilled(data);
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
	}

	@Override
	public Vector<String> getQuestLogStatus(EntityPlayer player) {
		Vector<String> vec = new Vector<String>();
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
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

}
