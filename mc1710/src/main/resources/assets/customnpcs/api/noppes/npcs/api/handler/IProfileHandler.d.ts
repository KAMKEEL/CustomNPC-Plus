/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Manages player profiles, which allow players to have multiple save slots
 * for their CNPC+ data.
  * @javaFqn noppes.npcs.api.handler.IProfileHandler
*/
export interface IProfileHandler {
    /**
     * Returns the profile for the given player.
     *
     * @param player the player.
     * @return the player's profile.
     */
    getProfile(player: import('../entity/IPlayer').IPlayer): import('./data/IProfile').IProfile;
    /**
     * Switches the player to a different profile slot.
     *
     * @param player the player.
     * @param slotID the slot ID to switch to.
     * @return true if the switch was successful.
     */
    changeSlot(player: import('../entity/IPlayer').IPlayer, slotID: import('./int').int): import('./boolean').boolean;
    /**
     * Checks whether a profile slot exists for the player.
     *
     * @param player the player.
     * @param slotID the slot ID.
     * @return true if the slot exists.
     */
    hasSlot(player: import('../entity/IPlayer').IPlayer, slotID: import('./int').int): import('./boolean').boolean;
    /**
     * Removes a profile slot from the player.
     *
     * @param player the player.
     * @param slotID the slot ID to remove.
     * @return true if the slot was removed.
     */
    removeSlot(player: import('../entity/IPlayer').IPlayer, slotID: import('./int').int): import('./boolean').boolean;
    /**
     * Returns the player data for a specific profile slot without switching to it.
     *
     * @param player the player.
     * @param slotID the slot ID.
     * @return the player data for the slot.
     */
    getSlotPlayerData(player: import('../entity/IPlayer').IPlayer, slotID: import('./int').int): import('./IPlayerData').IPlayerData;
    /**
     * Saves the current slot data for the player.
     *
     * @param player the player.
     */
    saveSlotData(player: import('../entity/IPlayer').IPlayer): import('./void').void;
}
