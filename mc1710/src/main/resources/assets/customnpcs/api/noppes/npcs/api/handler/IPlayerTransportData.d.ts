/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Tracks which transport locations a player has unlocked.
  * @javaFqn noppes.npcs.api.handler.IPlayerTransportData
*/
export interface IPlayerTransportData {
    /**
     * @param id the transport location ID.
     * @return true if the player has unlocked this transport.
     */
    hasTransport(id: import('./int').int): import('./boolean').boolean;
    /**
     * Unlocks a transport location by ID.
     *
     * @param id the transport location ID.
     */
    addTransport(id: import('./int').int): import('./void').void;
    /**
     * Unlocks the given transport location.
     *
     * @param location the transport location.
     */
    addTransport(location: import('./data/ITransportLocation').ITransportLocation): import('./void').void;
    /**
     * Returns the transport location with the given ID.
     *
     * @param id the transport location ID.
     * @return the transport location, or null if not found.
     */
    getTransport(id: import('./int').int): import('./data/ITransportLocation').ITransportLocation;
    /**
     * Returns all unlocked transport locations for the player.
     *
     * @return an array of transport locations.
     */
    getTransports(): import('./data/ITransportLocation').ITransportLocation[];
    /**
     * Removes an unlocked transport location by ID.
     *
     * @param id the transport location ID.
     */
    removeTransport(id: import('./int').int): import('./void').void;
}
