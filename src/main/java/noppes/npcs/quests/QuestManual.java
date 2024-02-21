package noppes.npcs.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.scripted.CustomNPCsException;

public class QuestManual extends QuestInterface {

    public static final String NBT_KEY_OBJECTIVE = "QuestManual";
    public static final String DATA_KEY_OBJECTIVE_FOUND = "QuestManualFound";
    public static final Integer OBJECTIVES_COUNT = 5;
    
    public HashMap<String, Integer> objectives = new HashMap<String, Integer>();

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag(NBT_KEY_OBJECTIVE, NBTTags.nbtStringIntegerMap(objectives));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        objectives.clear();
        if (compound.hasKey(NBT_KEY_OBJECTIVE)) {
            objectives.putAll(NBTTags.getStringIntegerMap(compound.getTagList(NBT_KEY_OBJECTIVE, 5)));
        }
    }

    @Override
    public boolean isCompleted(PlayerData player) {
        QuestData data = player.questData.activeQuests.get(questId);

        if (data == null) {
            return false;
        }

        HashMap<String, Integer> found = getFound(data);

        for (String objectiveName : objectives.keySet()) {
            int amount = 0;
            if (found.containsKey(objectiveName))
                amount = found.get(objectiveName);
            if (amount < objectives.get(objectiveName))
                return false;
        }

        return true;
    }

	@Override
	public Vector<String> getQuestLogStatus(EntityPlayer player) {
		Vector<String> vec = new Vector<String>();
		PlayerQuestData playerdata = PlayerDataController.Instance.getPlayerData(player).questData;
		QuestData data = playerdata.activeQuests.get(questId);
		if(data == null)
			return vec;
		HashMap<String,Integer> found = getFound(data);
		for(String entityName : objectives.keySet()){
			//Class cls = (Class) EntityList.stringToClassMapping.get(entityName);
			int amount = 0;
			if(found.containsKey(entityName))
				amount = found.get(entityName);
			String state = amount + "/" + objectives.get(entityName);
			
			vec.add(entityName + ": " + state);
		}
		
		return vec;
	}

    @Override
    public IQuestObjective[] getObjectives(EntityPlayer player) {
		List<IQuestObjective> list = new ArrayList<IQuestObjective>();

        for (Map.Entry<String, Integer> entry : objectives.entrySet()) {
            list.add(new noppes.npcs.quests.QuestManual.QuestManualObjective(this, player, entry.getKey(), entry.getValue()));
        }

		return (IQuestObjective[])list.toArray(new IQuestObjective[list.size()]);
    }

    public HashMap<String, Integer> getFound(QuestData data) {
        return NBTTags.getStringIntegerMap(data.extraData.getTagList(DATA_KEY_OBJECTIVE_FOUND, 10));
    }

    public void setFound(QuestData data, HashMap<String, Integer> found) {
        data.extraData.setTag(DATA_KEY_OBJECTIVE_FOUND, NBTTags.nbtStringIntegerMap(found));
    }

    class QuestManualObjective implements IQuestObjective {
        private final QuestManual parent;
        private final EntityPlayer player;
        private final String objective;
        private final int amount;

        public QuestManualObjective(QuestManual parent, EntityPlayer player, String objective, int amount) {
            this.parent = parent;
            this.player = player;
            this.objective = objective;
            this.amount = amount;
        }

        @Override
        public int getProgress() {
			PlayerData data = PlayerDataController.Instance.getPlayerData(player);
			PlayerQuestData playerdata = data.questData;
			QuestData questdata = (QuestData)playerdata.activeQuests.get(parent.questId);

			if(questdata != null) {
				HashMap<String, Integer> found = parent.getFound(questdata);
                String key = QuestManual.DATA_KEY_OBJECTIVE_FOUND + objective;
				return !found.containsKey(key) ? 0 : (Integer)found.get(key);
			}

			return 0;
        }

        @Override
        public void setProgress(int progress) {
			if (progress >= 0 && progress <= this.amount) {
				PlayerData data = PlayerDataController.Instance.getPlayerData(player);
				PlayerQuestData playerdata = data.questData;
				QuestData questdata = (QuestData)playerdata.activeQuests.get(this.parent.questId);
				if(questdata != null){
					HashMap<String, Integer> found = this.parent.getFound(questdata);
                    
					if (!found.containsKey(this.objective) || (Integer)found.get(this.objective) != progress) {
						found.put(this.objective, progress);
						this.parent.setFound(questdata, found);
						data.questData.checkQuestCompletion(data, EnumQuestType.Manual);
						data.save();
					}
				}
			} else {
				throw new CustomNPCsException("Progress has to be between 0 and " + this.amount, new Object[0]);
			}
        }

        @Override
        public int getMaxProgress() {
            return this.amount;
        }

        @Override
        public boolean isCompleted() {
            return this.getProgress() >= this.amount;
        }

        @Override
        public String getText() {
            // TODO: Show the objective name instead of the number!
            return this.objective + ": " + this.getProgress() + "/" + this.getMaxProgress();
        }
    }
}
