package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.event.IQuestEvent;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.constants.EnumScriptType;

public class QuestEvent extends CustomNPCsEvent implements IQuestEvent {
    public final IQuest quest;
    public final IPlayer player;

    public QuestEvent(IPlayer player, IQuest quest) {
        this.quest = quest;
        this.player = player;
    }

    public IPlayer getPlayer() {
        return player;
    }

    public IQuest getQuest() {
        return quest;
    }

    public String getHookName() {
        return EnumScriptType.QUEST_EVENT.function;
    }

    public static class QuestCompletedEvent extends QuestEvent implements IQuestEvent.QuestCompletedEvent {
        public QuestCompletedEvent(IPlayer player, IQuest quest) {
            super(player, quest);
        }

        public String getHookName() {
            return EnumScriptType.QUEST_COMPLETED.function;
        }
    }

    @Cancelable
    public static class QuestStartEvent extends QuestEvent implements IQuestEvent.QuestStartEvent {
        public QuestStartEvent(IPlayer player, IQuest quest) {
            super(player, quest);
        }

        public String getHookName() {
            return EnumScriptType.QUEST_START.function;
        }
    }

    @Cancelable
    public static class QuestTurnedInEvent extends QuestEvent implements IQuestEvent.QuestTurnedInEvent {
        public int expReward;
        public IItemStack[] itemRewards = new IItemStack[0];

        public QuestTurnedInEvent(IPlayer player, IQuest quest) {
            super(player, quest);
            this.expReward = 0;
        }

        public String getHookName() {
            return EnumScriptType.QUEST_TURNIN.function;
        }

        public void setExpReward(int expReward) {
            this.expReward = expReward;
        }

        public void setItemRewards(IItemStack[] itemRewards) {
            this.itemRewards = itemRewards;
        }

        public int getExpReward() {
            return expReward;
        }

        public IItemStack[] getItemRewards() {
            return itemRewards;
        }
    }

}
