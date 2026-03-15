/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during quest lifecycle: starting, completing, and turning in.
  * @javaFqn noppes.npcs.api.event.IQuestEvent
*/
export interface IQuestEvent extends import('./IPlayerEvent').IPlayerEvent {
    /** @return the quest associated with this event. */
    getQuest(): import('../handler/data/IQuest').IQuest;
    readonly quest: import('../handler/data/IQuest').IQuest;
}

export namespace IQuestEvent {
    
    /**
     * Fired when a quest's objectives are all completed.
     * @hookName questCompleted
          * @javaFqn noppes.npcs.api.event.IQuestEvent.QuestCompletedEvent
*/
    export interface QuestCompletedEvent extends IQuestEvent {
    }
    /**
     * Fired when a quest is started/accepted. Cancelable.
     * @hookName questStart
          * @javaFqn noppes.npcs.api.event.IQuestEvent.QuestStartEvent
*/
    export interface QuestStartEvent extends IQuestEvent {
    }
    /**
     * Fired when a completed quest is turned in. Cancelable.
     * @hookName questTurnIn
          * @javaFqn noppes.npcs.api.event.IQuestEvent.QuestTurnedInEvent
*/
    export interface QuestTurnedInEvent extends IQuestEvent {
        /** @param expReward the experience reward to give. */
        setExpReward(expReward: import('./int').int): import('./void').void;
        /** @param itemRewards the item rewards to give. */
        setItemRewards(itemRewards: import('../item/IItemStack').IItemStack[]): import('./void').void;
        /** @return the experience reward. */
        getExpReward(): import('./int').int;
        /** @return the item rewards. */
        getItemRewards(): import('../item/IItemStack').IItemStack[];
        expReward: import('./int').int;
        itemRewards: import('../item/IItemStack').IItemStack[];
    }
}
