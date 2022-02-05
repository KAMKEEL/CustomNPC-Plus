package noppes.npcs.quests;

import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.controllers.QuestData;

public class QuestLocation extends QuestInterface{
	public String location = "";
	public String location2 = "";
	public String location3 = "";

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		location = compound.getString("QuestLocation");
		location2 = compound.getString("QuestLocation2");
		location3 = compound.getString("QuestLocation3");
		
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setString("QuestLocation", location);
		compound.setString("QuestLocation2", location2);
		compound.setString("QuestLocation3", location3);
	}

	@Override
	public boolean isCompleted(EntityPlayer player) {
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return false;
		return getFound(data, 0);
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
		String found = StatCollector.translateToLocal("quest.found");
		String notfound = StatCollector.translateToLocal("quest.notfound");

		if(!location.isEmpty())
			vec.add(location + ": " + (getFound(data,1)?found:notfound));
		if(!location2.isEmpty())
			vec.add(location2 + ": " + (getFound(data,2)?found:notfound));
		if(!location3.isEmpty())
			vec.add(location3 + ": " + (getFound(data,3)?found:notfound));
		
		return vec;
	}

	public boolean getFound(QuestData data, int i) {
		if(i == 1)
			return data.extraData.getBoolean("LocationFound");
		if(i == 2)
			return data.extraData.getBoolean("Location2Found");
		if(i == 3)
			return data.extraData.getBoolean("Location3Found");

		if(!location.isEmpty() && !data.extraData.getBoolean("LocationFound"))
			return false;
		if(!location2.isEmpty() && !data.extraData.getBoolean("Location2Found"))
			return false;
		if(!location3.isEmpty() && !data.extraData.getBoolean("Location3Found"))
			return false;
		return true;
	}
	public boolean setFound(QuestData data, String location) {
		if(location.equalsIgnoreCase(this.location) && !data.extraData.getBoolean("LocationFound")){
			data.extraData.setBoolean("LocationFound", true);
			return true;
		}
		if(location.equalsIgnoreCase(location2) && !data.extraData.getBoolean("LocationFound2")){
			data.extraData.setBoolean("Location2Found", true);
			return true;
		}
		if(location.equalsIgnoreCase(location3) && !data.extraData.getBoolean("LocationFound3")){
			data.extraData.setBoolean("Location3Found", true);
			return true;
		}
		
		return false;
	}

}
