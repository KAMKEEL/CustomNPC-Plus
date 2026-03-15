/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Defines the blueprint for an attribute.
 * <p>
 * An attribute definition contains identifying information such as a unique key,
 * a human-readable display name, a translation key for localization, and a color code.
 * </p>
  * @javaFqn noppes.npcs.api.handler.data.IAttributeDefinition
*/
export interface IAttributeDefinition {
    /**
     * Returns the unique key that identifies this attribute.
     *
     * @return a String representing the attribute key
     */
    getKey(): String;
    /**
     * Returns the human-readable display name for this attribute.
     *
     * @return a String representing the display name
     */
    getDisplayName(): String;
    /**
     * Returns the translation key used for localizing this attribute.
     *
     * @return a String representing the translation key
     */
    getTranslationKey(): String;
    /**
     * Returns the color code associated with this attribute.
     *
     * @return a char representing the color code
     */
    getColorCode(): import('./char').char;
}
