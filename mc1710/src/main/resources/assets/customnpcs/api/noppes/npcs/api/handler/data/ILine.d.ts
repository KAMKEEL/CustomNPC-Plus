/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a single NPC dialog line with text, an optional sound, and target formatting.
  * @javaFqn noppes.npcs.api.handler.data.ILine
*/
export interface ILine {
    /**
     * Returns a copy of this line with target-specific placeholders resolved.
     *
     * @param entityLivingBase the target entity used for placeholder substitution.
     * @return the formatted line.
     */
    formatTarget(entityLivingBase: import('../../entity/IEntityLivingBase').IEntityLivingBase): import('./ILine').ILine;
    /** @return the text content of this line. */
    getText(): String;
    /**
     * Sets the text content of this line.
     *
     * @param text the new text.
     */
    setText(text: String): import('./void').void;
    /** @return the sound resource to play with this line, or null if none. */
    getSound(): String;
    /**
     * Sets the sound resource to play with this line.
     *
     * @param sound the sound resource path, or null for none.
     */
    setSound(sound: String): import('./void').void;
    /**
     * Sets whether the text of this line should be hidden when spoken.
     *
     * @param hide true to hide the text; false to show.
     */
    hideText(hide: import('./boolean').boolean): import('./void').void;
    /** @return true if the text is hidden when this line is spoken. */
    hideText(): import('./boolean').boolean;
}
