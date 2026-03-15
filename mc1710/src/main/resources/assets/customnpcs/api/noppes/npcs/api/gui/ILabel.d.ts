/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a text label component in a custom GUI.
 * Provides methods to get or set text, size, scale, and shadow properties.
  * @javaFqn noppes.npcs.api.gui.ILabel
*/
export interface ILabel extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Returns the text of the label.
     *
     * @return the label text.
     */
    getText(): String;
    /**
     * Sets the label text.
     *
     * @param text the new text.
     * @return this label instance.
     */
    setText(text: String): import('./ILabel').ILabel;
    /**
     * Returns the label's width.
     *
     * @return the width in pixels.
     */
    getWidth(): import('./int').int;
    /**
     * Returns the label's height.
     *
     * @return the height in pixels.
     */
    getHeight(): import('./int').int;
    /**
     * Sets the size of the label.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this label instance.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./ILabel').ILabel;
    /**
     * Returns the scale factor of the label.
     *
     * @return the scale.
     */
    getScale(): import('./float').float;
    /**
     * Sets the scale factor of the label.
     *
     * @param scale the new scale.
     * @return this label instance.
     */
    setScale(scale: import('./float').float): import('./ILabel').ILabel;
    /**
     * Returns whether the label text is rendered with a shadow.
     *
     * @return true if shadow is enabled; false otherwise.
     */
    getShadow(): import('./boolean').boolean;
    /**
     * Sets whether the label text should be rendered with a shadow.
     *
     * @param shadow true to enable shadow.
     */
    setShadow(shadow: import('./boolean').boolean): import('./void').void;
}
