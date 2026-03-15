/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a clickable button component in a custom GUI.
 * Provides methods to control size, label, texture, and state.
  * @javaFqn noppes.npcs.api.gui.IButton
*/
export interface IButton extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Returns the button's width.
     *
     * @return the width in pixels.
     */
    getWidth(): import('./int').int;
    /**
     * Returns the button's height.
     *
     * @return the height in pixels.
     */
    getHeight(): import('./int').int;
    /**
     * Sets the size of the button.
     *
     * @param width  the new width in pixels.
     * @param height the new height in pixels.
     * @return this button instance.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./IButton').IButton;
    /**
     * Returns the button's label text.
     *
     * @return the label.
     */
    getLabel(): String;
    /**
     * Sets the button's label text.
     *
     * @param text the new label.
     * @return this button instance.
     */
    setLabel(text: String): import('./IButton').IButton;
    /**
     * Returns the texture resource location for this button.
     *
     * @return the texture location as a string.
     */
    getTexture(): String;
    /**
     * Checks if the button has an assigned texture.
     *
     * @return true if a texture is set; false otherwise.
     */
    hasTexture(): import('./boolean').boolean;
    /**
     * Sets the texture resource location for this button.
     *
     * @param texture the texture resource location.
     * @return this button instance.
     */
    setTexture(texture: String): import('./IButton').IButton;
    /**
     * Returns the X offset within the texture.
     *
     * @return the texture X offset.
     */
    getTextureX(): import('./int').int;
    /**
     * Returns the Y offset within the texture.
     *
     * @return the texture Y offset.
     */
    getTextureY(): import('./int').int;
    /**
     * Sets the texture offset (X and Y) for this button.
     *
     * @param textureX the new X offset.
     * @param textureY the new Y offset.
     * @return this button instance.
     */
    setTextureOffset(textureX: import('./int').int, textureY: import('./int').int): import('./IButton').IButton;
    /**
     * Sets the scale factor of the button.
     *
     * @param scale the scale factor.
     */
    setScale(scale: import('./float').float): import('./void').void;
    /**
     * Returns the scale factor of the button.
     *
     * @return the scale.
     */
    getScale(): import('./float').float;
    /**
     * Enables or disables the button.
     *
     * @param enabled true to enable; false to disable.
     */
    setEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Checks if the button is enabled.
     *
     * @return true if enabled; false otherwise.
     */
    isEnabled(): import('./boolean').boolean;
}
