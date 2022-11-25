package noppes.npcs.quests;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.handler.data.IQuestKill;
import noppes.npcs.api.handler.data.IQuestObjective;

public class QuestKill extends QuestInterface implements IQuestKill {
	public HashMap<String,Integer> targets = new HashMap<String,Integer>();
	public int targetType = 0;
	public String customTargetType = "noppes.npcs.entity.EntityCustomNpc";

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("QuestKills", NBTTags.nbtStringIntegerMap(targets));
		//compound.setTag("QuestDialogs", NBTTags.nbtStringIntegerMap(targets));
		compound.setInteger("TargetType",targetType);
		compound.setString("CustomTargetType",customTargetType);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if(!compound.hasKey("QuestKills")) {
			targets.clear();
			HashMap<String,Integer> oldTargets = NBTTags.getStringIntegerMap(compound.getTagList("QuestDialogs", 10));
			targets.putAll(oldTargets);
		} else {
			targets = NBTTags.getStringIntegerMap(compound.getTagList("QuestKills", 10));
		}

		//targets = NBTTags.getStringIntegerMap(compound.getTagList("QuestDialogs", 10));
		targetType = compound.getInteger("TargetType");
		customTargetType = compound.getString("CustomTargetType");
	}

	@Override
	public boolean isCompleted(PlayerData playerData) {
		PlayerQuestData playerdata = playerData.questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return false;

		HashMap<String,Integer> killed = getKilled(data);
		int completed = 0;
		for(String entityName : targets.keySet()){
			int amount = 0;
			if(killed.containsKey(entityName))
				amount = killed.get(entityName);
			if(amount >= targets.get(entityName)){
				completed++;
			}
		}
		if(completed >= targets.keySet().size())
			return true;

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
		super.handleComplete(player);
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

	public IQuestObjective[] getObjectives(EntityPlayer player) {
		List<IQuestObjective> list = new ArrayList();
		Iterator var3 = this.targets.entrySet().iterator();

		while(var3.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry)var3.next();
			list.add(new noppes.npcs.quests.QuestKill.QuestKillObjective(this, player, (String)entry.getKey(), (Integer)entry.getValue()));
		}

		return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
	}

	public void setTargetType(int type) {
		if(type < 0)
			type = 0;
		if(type > 2)
			type = 2;

		this.targetType = type;
	}
	public int getTargetType() {
		return this.targetType;
	}

	class QuestKillObjective implements IQuestObjective {
		private final QuestKill parent;
		private final EntityPlayer player;
		private final String entity;
		private final int amount;

		public QuestKillObjective(QuestKill parent, EntityPlayer player, String entity, int amount) {
			this.parent = parent;
			this.player = player;
			this.entity = entity;
			this.amount = amount;
		}

		public int getProgress() {
			PlayerData data = PlayerDataController.instance.getPlayerData(player);
			PlayerQuestData playerdata = data.questData;
			QuestData questdata = (QuestData)playerdata.activeQuests.get(this.parent.questId);
			HashMap<String, Integer> killed = this.parent.getKilled(questdata);
			return !killed.containsKey(this.entity) ? 0 : (Integer)killed.get(this.entity);
		}

		public void setProgress(int progress) {
			if (progress >= 0 && progress <= this.amount) {
				PlayerData data = PlayerDataController.instance.getPlayerData(player);
				PlayerQuestData playerdata = data.questData;
				QuestData questdata = (QuestData)playerdata.activeQuests.get(this.parent.questId);
				HashMap<String, Integer> killed = this.parent.getKilled(questdata);
				if (!killed.containsKey(this.entity) || (Integer)killed.get(this.entity) != progress) {
					killed.put(this.entity, progress);
					this.parent.setKilled(questdata, killed);
					data.questData.checkQuestCompletion(data, EnumQuestType.values()[2]);
					data.questData.checkQuestCompletion(data, EnumQuestType.values()[4]);
					data.savePlayerDataOnFile();
				}
			} else {
				throw new CustomNPCsException("Progress has to be between 0 and " + this.amount, new Object[0]);
			}
		}

		public int getMaxProgress() {
			return this.amount;
		}

		public boolean isCompleted() {
			return this.getProgress() >= this.amount;
		}

		public String getText() {
			return this.entity + ": " + this.getProgress() + "/" + this.getMaxProgress();
		}
	}
}
