package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;

/**
 * Events fired during quest lifecycle: starting, completing, and turning in.
 */
public interface IQuestEvent extends IPlayerEvent {

    /** @return the quest associated with this event. */
    IQuest getQuest();

    /**
     * Fired when a quest's objectives are all completed.
     * @hookName questCompleted
     */
    interface QuestCompletedEvent extends IQuestEvent {
    }

    /**
     * Fired when a quest is started/accepted. Cancelable.
     * @hookName questStart
     */
    @Cancelable
    interface QuestStartEvent extends IQuestEvent {
    }

    /**
     * Fired when a completed quest is turned in. Cancelable.
     * @hookName questTurnIn
     */
    @Cancelable
    interface QuestTurnedInEvent extends IQuestEvent {
        /** @param expReward the experience reward to give. */
        void setExpReward(int expReward);

        /** @param itemRewards the item rewards to give. */
        void setItemRewards(IItemStack[] itemRewards);

        /** @return the experience reward. */
        int getExpReward();

        /** @return the item rewards. */
        IItemStack[] getItemRewards();
    }
}
