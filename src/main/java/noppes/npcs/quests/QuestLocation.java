package noppes.npcs.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.handler.data.IQuestLocation;
import noppes.npcs.api.handler.data.IQuestObjective;

public class QuestLocation extends QuestInterface implements IQuestLocation {
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
	public boolean isCompleted(PlayerData playerData) {
		PlayerQuestData playerdata = playerData.questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return false;
		return getFound(data, 0);
	}

	@Override
	public void handleComplete(EntityPlayer player) {
		super.handleComplete(player);
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

	public IQuestObjective[] getObjectives(EntityPlayer player) {
		List<IQuestObjective> list = new ArrayList();
		if (!this.location.isEmpty()) {
			list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, player, this.location, "LocationFound"));
		}

		if (!this.location2.isEmpty()) {
			list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, player, this.location2, "Location2Found"));
		}

		if (!this.location3.isEmpty()) {
			list.add(new noppes.npcs.quests.QuestLocation.QuestLocationObjective(this, player, this.location3, "Location3Found"));
		}

		return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
	}

	public void setLocation1(String loc1){
		this.location = loc1;
	}
	public String getLocation1(){
		return location;
	}

	public void setLocation2(String loc2){
		this.location2 = loc2;
	}
	public String getLocation2(){
		return location2;
	}

	public void setLocation3(String loc3){
		this.location3 = loc3;
	}
	public String getLocation3(){
		return location3;
	}

	class QuestLocationObjective implements IQuestObjective {
		private final QuestLocation parent;
		private final EntityPlayer player;
		private final String location;
		private final String nbtName;

		public QuestLocationObjective(QuestLocation this$0, EntityPlayer player, String location, String nbtName) {
			this.parent = this$0;
			this.player = player;
			this.location = location;
			this.nbtName = nbtName;
		}

		public int getProgress() {
			return this.isCompleted() ? 1 : 0;
		}

		public void setProgress(int progress) {
			if (progress >= 0 && progress <= 1) {
				PlayerData data = PlayerDataController.instance.getPlayerData(player);
				QuestData questData = (QuestData)data.questData.activeQuests.get(this.parent.questId);
				boolean completed = questData.extraData.getBoolean	(this.nbtName);
				if ((!completed || progress != 1) && (completed || progress != 0)) {
					questData.extraData.setBoolean(this.nbtName, progress == 1);
					data.questData.checkQuestCompletion(data, EnumQuestType.values()[3]);
					data.savePlayerDataOnFile();
				}
			} else {
				throw new CustomNPCsException("Progress has to be 0 or 1", new Object[0]);
			}
		}

		public int getMaxProgress() {
			return 1;
		}

		public boolean isCompleted() {
			PlayerData data = PlayerDataController.instance.getPlayerData(player);
			QuestData questData = (QuestData)data.questData.activeQuests.get(this.parent.questId);
			return questData.extraData.getBoolean(this.nbtName);
		}

		public String getText() {
			return this.location + ": " + (this.isCompleted() ? "Found" : "Not Found");
		}
	}
}
