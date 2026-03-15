/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a collection of a player's custom attributes.
 * <p>
 * Expose methods to retrieve all attribute instances as an array, look up individual attributes by key,
 * and check for the presence of a specific attribute.
 * </p>
  * @javaFqn noppes.npcs.api.handler.data.IPlayerAttributes
*/
export interface IPlayerAttributes {
    /**
     * Recalulates Item Attributes on a Player
     *
     * @param player the player whose attributes to recalculate
     */
    recalculate(player: import('../../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * Returns an array of all custom attribute instances associated with the player.
     *
     * @return an array of {@link ICustomAttribute} objects
     */
    getAttributes(): import('./ICustomAttribute').ICustomAttribute[];
    /**
     * Returns the value of the attribute identified by the given key.
     *
     * @param key the attribute key (e.g., "health", "movement_speed")
     * @return the float value of the attribute, or 0 if the attribute is not found
     */
    getAttributeValue(key: String): import('./float').float;
    /**
     * Checks whether an attribute with the given key exists.
     *
     * @param key the attribute key to check
     * @return true if the attribute exists, false otherwise
     */
    hasAttribute(key: String): import('./boolean').boolean;
    /**
     * Returns the custom attribute instance identified by the given key.
     *
     * @param key the attribute key (e.g., "health", "movement_speed")
     * @return the corresponding {@link ICustomAttribute} instance, or null if not found
     */
    getAttribute(key: String): import('./ICustomAttribute').ICustomAttribute;
    playerAttributes: import('./PlayerAttributeMap').PlayerAttributeMap;
    extraHealth: import('./float').float;
    extraHealthBoost: import('./float').float;
    movementSpeed: import('./float').float;
    knockbackRes: import('./float').float;
    gearOutput: import('./float').float;
    readonly magicDamage: Java.java.util.Map<Integer, Float>;
    readonly magicBoost: Java.java.util.Map<Integer, Float>;
    readonly magicDefense: Java.java.util.Map<Integer, Float>;
    readonly magicResistance: Java.java.util.Map<Integer, Float>;
}
