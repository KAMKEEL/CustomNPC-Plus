/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Tracks item giver interaction data for a player, including
 * cooldown timestamps and interaction history.
  * @javaFqn noppes.npcs.api.handler.IPlayerItemGiverData
*/
export interface IPlayerItemGiverData {
    /**
     * Returns the last interaction time for the given item giver job.
     *
     * @param jobItemGiver the item giver job.
     * @return the last interaction time in game ticks.
     */
    getTime(jobItemGiver: import('../jobs/IJobItemGiver').IJobItemGiver): import('./long').long;
    /**
     * Sets the last interaction time for the given item giver job.
     *
     * @param jobItemGiver the item giver job.
     * @param day          the time to set in game ticks.
     */
    setTime(jobItemGiver: import('../jobs/IJobItemGiver').IJobItemGiver, day: import('./long').long): import('./void').void;
    /**
     * Checks whether the player has interacted with the item giver before.
     *
     * @param jobItemGiver the item giver job.
     * @return true if previously interacted; false otherwise.
     */
    hasInteractedBefore(jobItemGiver: import('../jobs/IJobItemGiver').IJobItemGiver): import('./boolean').boolean;
}
