/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * Provides access to a player's Pixelmon data, including party and PC storage.
  * @javaFqn noppes.npcs.api.IPixelmonPlayerData
*/
export interface IPixelmonPlayerData {
    /**
     * Gets the Pixelmon in the specified party slot.
     * @param slot the party slot index (0-5)
     * @return the Pixelmon in that slot, or null if empty
     */
    getPartySlot(slot: import('./int').int): import('./entity/IPixelmon').IPixelmon;
    /**
     * Gets the total number of Pixelmon stored in the player's PC.
     * @return the PC Pixelmon count
     */
    countPCPixelmon(): import('./int').int;
}
