/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a text field component in a custom GUI.
 * Provides methods to get or set its size and text content.
  * @javaFqn noppes.npcs.api.gui.ITextField
*/
export interface ITextField extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Returns the width of the text field.
     *
     * @return the width in pixels.
     */
    getWidth(): import('./int').int;
    /**
     * Returns the height of the text field.
     *
     * @return the height in pixels.
     */
    getHeight(): import('./int').int;
    /**
     * Sets the size of the text field.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this text field instance.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./ITextField').ITextField;
    /**
     * Returns the current text content of the text field.
     *
     * @return the text.
     */
    getText(): String;
    /**
     * Sets the text content of the text field.
     *
     * @param text the new text.
     * @return this text field instance.
     */
    setText(text: String): import('./ITextField').ITextField;
}
