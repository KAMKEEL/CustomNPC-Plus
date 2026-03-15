/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles creation, deletion, and retrieval of factions.
  * @javaFqn noppes.npcs.api.handler.IFactionHandler
*/
export interface IFactionHandler {
    /**
     * Returns all registered factions.
     *
     * @return a list of factions.
     */
    list(): import('./data/IFaction').IFaction[];
    /**
     * Deletes the faction with the given ID.
     *
     * @param id the faction ID.
     * @return the deleted faction, or null if not found.
     */
    delete(id: import('./int').int): import('./data/IFaction').IFaction;
    /**
     * Creates a new faction with the given name and default points.
     *
     * @param name          the faction name.
     * @param defaultPoints the default standing points for new players.
     * @return the created faction.
     */
    create(name: String, defaultPoints: import('./int').int): import('./data/IFaction').IFaction;
    /**
     * Returns the faction with the given ID.
     *
     * @param id the faction ID.
     * @return the faction, or null if not found.
     */
    get(id: import('./int').int): import('./data/IFaction').IFaction;
}
