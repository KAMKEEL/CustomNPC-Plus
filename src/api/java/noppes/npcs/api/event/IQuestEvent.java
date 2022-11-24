package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;

public interface IQuestEvent extends ICustomNPCsEvent {

    IPlayer getPlayer();

    IQuest getQuest();

    interface QuestCompletedEvent extends IQuestEvent {
    }

    @Cancelable
    interface QuestStartEvent extends IQuestEvent {
    }

    @Cancelable
    interface QuestTurnedInEvent extends IQuestEvent {
        void setExpReward(int expReward);

        void setItemRewards(IItemStack[] itemRewards);

        int getExpReward();

        IItemStack[] getItemRewards();
    }
}
