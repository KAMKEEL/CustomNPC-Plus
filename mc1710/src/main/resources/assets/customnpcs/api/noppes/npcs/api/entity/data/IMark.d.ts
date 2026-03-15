/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Represents a mark that can be applied to an entity.
 * A mark holds a type, a color, and availability conditions.
  * @javaFqn noppes.npcs.api.entity.data.IMark
*/
export interface IMark {
    /**
     * Returns the availability conditions associated with this mark.
     *
     * @return the availability.
     */
    getAvailability(): import('../../handler/data/IAvailability').IAvailability;
    /**
     * Returns the color value of this mark.
     *
     * @return the color as an integer.
     */
    getColor(): import('./int').int;
    /**
     * Sets the color value of this mark.
     *
     * @param color the new color.
     */
    setColor(color: import('./int').int): import('./void').void;
    /**
     * Returns the type of this mark.
     *
     * @return the mark type as an integer.
     */
    getType(): import('./int').int;
    /**
     * Sets the type of this mark.
     *
     * @param type the new type.
     */
    setType(type: import('./int').int): import('./void').void;
    /**
     * Calling this will send the changes you've made to the clients.
     */
    update(): import('./void').void;
}
