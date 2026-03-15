/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Tracks a player's quest progress, including active, finished, and tracked quests.
  * @javaFqn noppes.npcs.api.handler.IPlayerQuestData
*/
export interface IPlayerQuestData {
    /** @return the quest currently being tracked by the player, or null if none. */
    getTrackedQuest(): import('./data/IQuest').IQuest;
    /**
     * Starts the quest with the given ID for the player.
     *
     * @param id the quest ID.
     */
    startQuest(id: import('./int').int): import('./void').void;
    /**
     * Marks the quest with the given ID as finished.
     *
     * @param id the quest ID.
     */
    finishQuest(id: import('./int').int): import('./void').void;
    /**
     * Removes the quest from the player's active quest list.
     *
     * @param id the quest ID.
     */
    stopQuest(id: import('./int').int): import('./void').void;
    /**
     * Removes the quest from both active and finished lists.
     *
     * @param id the quest ID.
     */
    removeQuest(id: import('./int').int): import('./void').void;
    /**
     * @param id the quest ID.
     * @return true if the player has finished the quest.
     */
    hasFinishedQuest(id: import('./int').int): import('./boolean').boolean;
    /**
     * @param id the quest ID.
     * @return true if the player has the quest active.
     */
    hasActiveQuest(id: import('./int').int): import('./boolean').boolean;
    /** @return all active quests for the player. */
    getActiveQuests(): import('./data/IQuest').IQuest[];
    /** @return all finished quests for the player. */
    getFinishedQuests(): import('./data/IQuest').IQuest[];
    /**
     * Returns the timestamp when the quest was last completed.
     *
     * @param id the quest ID.
     * @return the completion time, or 0 if never completed.
     */
    getLastCompletedTime(id: import('./int').int): import('./long').long;
    /**
     * Sets the last completed time for the quest.
     *
     * @param id   the quest ID.
     * @param time the completion timestamp.
     */
    setLastCompletedTime(id: import('./int').int, time: import('./long').long): import('./void').void;
    activeQuests: Java.java.util.HashMap<Integer, import('./QuestData').QuestData>;
    finishedQuests: Java.java.util.HashMap<Integer, Long>;
}
