/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

/**
 * Represents the transporter role for an NPC, allowing players to teleport
 * between unlocked transport locations.
  * @javaFqn noppes.npcs.api.roles.IRoleTransporter
*/
export interface IRoleTransporter extends import('./IRole').IRole {
    /** @return the display name of this transporter NPC. */
    getName(): String;
    /** @return the transport location ID assigned to this NPC. */
    getTransportId(): import('./int').int;
    /**
     * Unlocks the given transport location for the player.
     *
     * @param player   the player.
     * @param location the transport location to unlock.
     */
    unlock(player: import('../entity/IPlayer').IPlayer, location: import('../handler/data/ITransportLocation').ITransportLocation): import('./void').void;
    /** @return the transport location assigned to this NPC, or null if none. */
    getTransport(): import('../handler/data/ITransportLocation').ITransportLocation;
    /** @return true if this NPC has a transport location assigned. */
    hasTransport(): import('./boolean').boolean;
    /**
     * Assigns a transport location to this NPC.
     *
     * @param location the transport location.
     */
    setTransport(location: import('../handler/data/ITransportLocation').ITransportLocation): import('./void').void;
}
