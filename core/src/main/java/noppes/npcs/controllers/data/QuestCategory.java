package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
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

    public void readNBT(INbt INbt) {
        id = INbt.getInteger("Slot");
        title = INbt.getString("Title");
        INbtList questList = getFirstValidList(INbt, NbtConstants.TAG_COMPOUND, "Quests", "Dialogs");
        if (questList.tagCount() > 0) {
            for (int ii = 0; ii < questList.tagCount(); ii++) {
                INbt nbttagcompound2 = questList.getCompoundTagAt(ii);
                Quest quest = new Quest();
                quest.readNBT(nbttagcompound2);
                quest.category = this;
                quests.put(quest.id, quest);
            }
        }
    }

    private INbtList getFirstValidList(INbt nbt, int type, String... keys) {
        for (String key : keys) {
            if (nbt.hasKey(key, NbtConstants.TAG_LIST)) {
                INbtList list = nbt.getTagList(key, type);
                if (list.tagCount() > 0) {
                    return list;
                }
            }
        }
        return new INbtList();
    }

    public INbt writeNBT(INbt INbt) {
        INbt.setInteger("Slot", id);
        INbt.setString("Title", title);
        INbtList quests = new INbtList();
        for (int questID : this.quests.keySet()) {
            Quest quest = this.quests.get(questID);
            quests.appendTag(quest.writeToNBT(new INbt()));
        }

        INbt.setTag("Quests", quests);

        return INbt;
    }

    public INbt writeSmallNBT(INbt INbt) {
        INbt.setInteger("Slot", id);
        INbt.setString("Title", title);
        return INbt;
    }

    public void readSmallNBT(INbt INbt) {
        id = INbt.getInteger("Slot");
        title = INbt.getString("Title");
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
