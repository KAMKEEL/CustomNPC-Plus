/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IQuestEvent extends IPlayerEvent {

    // Methods
    getQuest(): import('../handler/data/IQuest').IQuest;

    // Nested interfaces
    interface QuestCompletedEvent extends IQuestEvent {
    }
    interface QuestStartEvent extends IQuestEvent {
    }
    interface QuestTurnedInEvent extends IQuestEvent {
        setExpReward(expReward: number): void;
        setItemRewards(itemRewards: import('../item/IItemStack').IItemStack[]): void;
        getExpReward(): number;
        getItemRewards(): import('../item/IItemStack').IItemStack[];
    }

}
