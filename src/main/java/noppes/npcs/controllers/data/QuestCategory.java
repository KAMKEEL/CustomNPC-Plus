package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestCategory implements IQuestCategory {
    public HashMap<Integer, Quest> quests;
    public int id = -1;
    public String title = "";

    public QuestCategory() {
        quests = new HashMap<Integer, Quest>();
    }

    public void readNBT(NBTTagCompound nbttagcompound) {
        id = nbttagcompound.getInteger("Slot");
        title = nbttagcompound.getString("Title");
        NBTTagList questList = nbttagcompound.getTagList("Quests", 10);
        if (questList != null) {
            for (int ii = 0; ii < questList.tagCount(); ii++) {
                NBTTagCompound nbttagcompound2 = questList.getCompoundTagAt(ii);
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
        NBTTagList quests = new NBTTagList();
        for (int questID : this.quests.keySet()) {
            Quest quest = this.quests.get(questID);
            quests.appendTag(quest.writeToNBT(new NBTTagCompound()));
        }

        nbttagcompound.setTag("Quests", quests);

        return nbttagcompound;
    }

    public NBTTagCompound writeSmallNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInteger("Slot", id);
        nbttagcompound.setString("Title", title);
        return nbttagcompound;
    }

    public void readSmallNBT(NBTTagCompound nbttagcompound) {
        id = nbttagcompound.getInteger("Slot");
        title = nbttagcompound.getString("Title");
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

    @Override
    public int getId() {
        return this.id;
    }
}
