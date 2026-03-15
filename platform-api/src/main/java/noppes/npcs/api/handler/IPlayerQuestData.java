package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IQuest;

/**
 * Tracks a player's quest progress, including active, finished, and tracked quests.
 */
public interface IPlayerQuestData {

    /** @return the quest currently being tracked by the player, or null if none. */
    IQuest getTrackedQuest();

    /**
     * Starts the quest with the given ID for the player.
     *
     * @param id the quest ID.
     */
    void startQuest(int id);

    /**
     * Marks the quest with the given ID as finished.
     *
     * @param id the quest ID.
     */
    void finishQuest(int id);

    /**
     * Removes the quest from the player's active quest list.
     *
     * @param id the quest ID.
     */
    void stopQuest(int id);

    /**
     * Removes the quest from both active and finished lists.
     *
     * @param id the quest ID.
     */
    void removeQuest(int id);

    /**
     * @param id the quest ID.
     * @return true if the player has finished the quest.
     */
    boolean hasFinishedQuest(int id);

    /**
     * @param id the quest ID.
     * @return true if the player has the quest active.
     */
    boolean hasActiveQuest(int id);

    /** @return all active quests for the player. */
    IQuest[] getActiveQuests();

    /** @return all finished quests for the player. */
    IQuest[] getFinishedQuests();

    /**
     * Returns the timestamp when the quest was last completed.
     *
     * @param id the quest ID.
     * @return the completion time, or 0 if never completed.
     */
    long getLastCompletedTime(int id);

    /**
     * Sets the last completed time for the quest.
     *
     * @param id   the quest ID.
     * @param time the completion timestamp.
     */
    void setLastCompletedTime(int id, long time);
}
