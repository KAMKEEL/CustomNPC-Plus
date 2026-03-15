/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a transport destination that players can teleport to.
  * @javaFqn noppes.npcs.api.handler.data.ITransportLocation
*/
export interface ITransportLocation {
    /** @return the unique transport location ID. */
    getId(): import('./int').int;
    /** @param name the location display name. */
    setName(name: String): import('./void').void;
    /** @return the location display name. */
    getName(): String;
    /** @param dimension the dimension ID where this transport is located. */
    setDimension(dimension: import('./int').int): import('./void').void;
    /** @return the dimension ID. */
    getDimension(): import('./int').int;
    /**
     * Sets the transport type.
     *
     * @param type the type ordinal.
     */
    setType(type: import('./int').int): import('./void').void;
    /** @return the transport type ordinal. */
    getType(): import('./int').int;
    /**
     * Sets the transport destination coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     */
    setPosition(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./void').void;
    /**
     * Sets the transport destination position.
     *
     * @param pos the position.
     */
    setPosition(pos: import('../../IPos').IPos): import('./void').void;
    /** @return the x coordinate. */
    getX(): import('./double').double;
    /** @return the y coordinate. */
    getY(): import('./double').double;
    /** @return the z coordinate. */
    getZ(): import('./double').double;
    /** Saves this transport location to disk. */
    save(): import('./void').void;
}
