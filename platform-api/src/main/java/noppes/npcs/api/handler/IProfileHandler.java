package noppes.npcs.api.handler;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IProfile;

/**
 * Manages player profiles, which allow players to have multiple save slots
 * for their CNPC+ data.
 */
public interface IProfileHandler {

    /**
     * Returns the profile for the given player.
     *
     * @param player the player.
     * @return the player's profile.
     */
    IProfile getProfile(IPlayer player);

    /**
     * Switches the player to a different profile slot.
     *
     * @param player the player.
     * @param slotID the slot ID to switch to.
     * @return true if the switch was successful.
     */
    boolean changeSlot(IPlayer player, int slotID);

    /**
     * Checks whether a profile slot exists for the player.
     *
     * @param player the player.
     * @param slotID the slot ID.
     * @return true if the slot exists.
     */
    boolean hasSlot(IPlayer player, int slotID);

    /**
     * Removes a profile slot from the player.
     *
     * @param player the player.
     * @param slotID the slot ID to remove.
     * @return true if the slot was removed.
     */
    boolean removeSlot(IPlayer player, int slotID);

    /**
     * Returns the player data for a specific profile slot without switching to it.
     *
     * @param player the player.
     * @param slotID the slot ID.
     * @return the player data for the slot.
     */
    IPlayerData getSlotPlayerData(IPlayer player, int slotID);

    /**
     * Saves the current slot data for the player.
     *
     * @param player the player.
     */
    void saveSlotData(IPlayer player);
}
