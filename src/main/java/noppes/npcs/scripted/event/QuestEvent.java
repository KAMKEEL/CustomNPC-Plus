package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.scripted.handler.data.IQuest;
import noppes.npcs.scripted.interfaces.IItemStack;
import noppes.npcs.scripted.interfaces.IPlayer;

public class QuestEvent extends CustomNPCsEvent {
    public final IQuest quest;
    public final IPlayer player;

    public QuestEvent(IPlayer player, IQuest quest) {
        this.quest = quest;
        this.player = player;
    }

    public static class QuestCompletedEvent extends QuestEvent {
        public QuestCompletedEvent(IPlayer player, IQuest quest) {
            super(player, quest);
        }
    }

    @Cancelable
    public static class QuestStartEvent extends QuestEvent {
        public QuestStartEvent(IPlayer player, IQuest quest) {
            super(player, quest);
        }
    }

    public static class QuestTurnedInEvent extends QuestEvent {
        public int expReward;
        public IItemStack[] itemRewards = new IItemStack[0];

        public QuestTurnedInEvent(IPlayer player, IQuest quest) {
            super(player, quest);
            this.expReward = 0;
        }
    }

}
