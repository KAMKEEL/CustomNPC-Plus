package noppes.npcs.api.handler;

import noppes.npcs.api.jobs.IJobItemGiver;

/**
 * Tracks item giver interaction data for a player, including
 * cooldown timestamps and interaction history.
 */
public interface IPlayerItemGiverData {

    /**
     * Returns the last interaction time for the given item giver job.
     *
     * @param jobItemGiver the item giver job.
     * @return the last interaction time in game ticks.
     */
    long getTime(IJobItemGiver jobItemGiver);

    /**
     * Sets the last interaction time for the given item giver job.
     *
     * @param jobItemGiver the item giver job.
     * @param day          the time to set in game ticks.
     */
    void setTime(IJobItemGiver jobItemGiver, long day);

    /**
     * Checks whether the player has interacted with the item giver before.
     *
     * @param jobItemGiver the item giver job.
     * @return true if previously interacted; false otherwise.
     */
    boolean hasInteractedBefore(IJobItemGiver jobItemGiver);
}
