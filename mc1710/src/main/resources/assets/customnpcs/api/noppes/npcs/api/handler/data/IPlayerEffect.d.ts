/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents an active custom effect instance on a player.
  * @javaFqn noppes.npcs.api.handler.data.IPlayerEffect
*/
export interface IPlayerEffect {
    /** Removes this effect from the player. */
    kill(): import('./void').void;
    /** @return the custom effect ID. */
    getId(): import('./int').int;
    /** @return the remaining duration in seconds (-100 for infinite). */
    getDuration(): import('./int').int;
    /** @param duration the remaining duration in seconds (-100 for infinite). */
    setDuration(duration: import('./int').int): import('./void').void;
    /** @return the effect level/amplifier. */
    getLevel(): import('./byte').byte;
    /** @param level the effect level/amplifier. */
    setLevel(level: import('./byte').byte): import('./void').void;
    /** @return the display name of the effect. */
    getName(): String;
    /**
     * Applies this effect's tick logic to the given player.
     *
     * @param player the player to apply the effect to.
     */
    performEffect(player: import('../../entity/IPlayer').IPlayer): import('./void').void;
    /** @return the effect source index (0: CNPC+, 1: DBC Addon). */
    getIndex(): import('./int').int;
    /** @param index the effect source index (0: CNPC+, 1: DBC Addon). */
    setIndex(index: import('./int').int): import('./void').void;
    id: import('./int').int;
    duration: import('./int').int;
    level: import('./byte').byte;
    index: import('./int').int;
}
