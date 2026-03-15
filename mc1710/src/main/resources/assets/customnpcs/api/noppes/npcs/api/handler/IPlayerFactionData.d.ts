/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Tracks a player's faction standing points.
  * @javaFqn noppes.npcs.api.handler.IPlayerFactionData
*/
export interface IPlayerFactionData {
    /**
     * Returns the player's points for the given faction.
     *
     * @param id the faction ID.
     * @return the faction points.
     */
    getPoints(id: import('./int').int): import('./int').int;
    /**
     * Adds points to the player's standing with the given faction.
     * Use negative values to decrease.
     *
     * @param id     the faction ID.
     * @param points the points to add.
     */
    addPoints(id: import('./int').int, points: import('./int').int): import('./void').void;
    /**
     * Sets the player's points for the given faction.
     *
     * @param id     the faction ID.
     * @param points the new point value.
     */
    setPoints(id: import('./int').int, points: import('./int').int): import('./void').void;
    factionData: Java.java.util.HashMap<Integer, Integer>;
}
