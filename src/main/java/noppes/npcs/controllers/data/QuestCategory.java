package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

public class QuestCategory implements IQuestCategory {
	public HashMap<Integer,Quest> quests;
	public int id = -1;
	public String title = "";
	
	public QuestCategory(){
		quests = new HashMap<Integer, Quest>();
	}

	public void readNBT(NBTTagCompound nbttagcompound) {
        id = nbttagcompound.getInteger("Slot");
        title = nbttagcompound.getString("Title");
        NBTTagList dialogsList = nbttagcompound.getTagList("Dialogs", 10);
        if(dialogsList != null){
            for(int ii = 0; ii < dialogsList.tagCount(); ii++)
            {
                NBTTagCompound nbttagcompound2 = dialogsList.getCompoundTagAt(ii);
                Quest quest = new Quest();
                quest.readNBT(nbttagcompound2);
                quest.category = this;
            	quests.put(quest.id, quest);
            }
        }
	}

	public NBTTagCompound writeNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("Slot", id);
		nbttagcompound.setString("Title", title);
        NBTTagList dialogs = new NBTTagList();
        for(int dialogId : quests.keySet()){
        	Quest quest = quests.get(dialogId);
        	dialogs.appendTag(quest.writeToNBT(new NBTTagCompound()));
        }
        
        nbttagcompound.setTag("Dialogs", dialogs);
        
        return nbttagcompound;
	}

    public List<IQuest> quests() {
        return new ArrayList(this.quests.values());
    }

    public String getName() {
        return this.title;
    }

    public IQuest create() {
        Quest quest = new Quest();
        quest.category = this;
        return quest;
    }
}
