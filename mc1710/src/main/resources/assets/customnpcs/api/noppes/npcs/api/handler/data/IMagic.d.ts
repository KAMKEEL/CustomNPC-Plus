/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a magic element that can have interactions with other magics.
 * Each magic has unique properties such as an ID, name, color, and display name,
 * and can hold interaction values with other magics.
  * @javaFqn noppes.npcs.api.handler.data.IMagic
*/
export interface IMagic {
    /**
     * Gets the unique identifier of this magic.
     *
     * @return the magic ID
     */
    getId(): import('./int').int;
    /**
     * Gets the internal name of the magic.
     *
     * @return the name of the magic
     */
    getName(): String;
    /**
     * Sets the internal name of the magic.
     *
     * @param name the new name of the magic
     */
    setName(name: String): import('./void').void;
    /**
     * Sets the color for this magic.
     *
     * @param c the color represented as an integer (typically in hexadecimal)
     */
    setColor(c: import('./int').int): import('./void').void;
    /**
     * Gets the color of this magic.
     *
     * @return the color as an integer
     */
    getColor(): import('./int').int;
    /**
     * Gets the display name of the magic.
     *
     * @return the display name of the magic
     */
    getDisplayName(): String;
    /**
     * Sets the display name of the magic.
     *
     * @param displayName the new display name
     */
    setDisplayName(displayName: String): import('./void').void;
    /**
     * Saves the current state of the magic.
     * Typically persists changes to a controller or storage system.
     */
    save(): import('./void').void;
    /**
     * Checks if this magic has an interaction value set for another magic.
     *
     * @param magicID the unique identifier of the other magic
     * @return true if an interaction is present, false otherwise
     */
    hasInteraction(magicID: import('./int').int): import('./boolean').boolean;
    /**
     * Sets an interaction value for a specific other magic.
     *
     * @param magicID the unique identifier of the other magic
     * @param value   the interaction value (usually a percentage or multiplier)
     */
    setInteraction(magicID: import('./int').int, value: import('./float').float): import('./void').void;
    /**
     * Retrieves the interaction value for a specific other magic.
     *
     * @param magicID the unique identifier of the other magic
     * @param value   the default value to return if no interaction is found
     * @return the interaction value if present; otherwise, the default value
     */
    getInteraction(magicID: import('./int').int, value: import('./float').float): import('./float').float;
    name: String;
    displayName: String;
    color: import('./int').int;
    id: import('./int').int;
    item: import('./ItemStack').ItemStack;
    type: import('./EnumTextureType').EnumTextureType;
    iconTexture: String;
    interactions: Java.java.util.Map<Integer, Float>;
}
