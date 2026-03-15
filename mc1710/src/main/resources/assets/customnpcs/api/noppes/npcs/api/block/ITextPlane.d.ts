/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.block
 */

/**
 * Represents a text plane that can be positioned, rotated, and scaled on a block.
  * @javaFqn noppes.npcs.api.block.ITextPlane
*/
export interface ITextPlane {
    /**
     * Gets the text displayed on this plane.
     * @return the display text
     */
    getText(): String;
    /**
     * Sets the text displayed on this plane.
     * @param text the display text to set
     */
    setText(text: String): import('./void').void;
    /**
     * Gets the X-axis rotation in degrees.
     * @return the X rotation
     */
    getRotationX(): import('./int').int;
    /**
     * Gets the Y-axis rotation in degrees.
     * @return the Y rotation
     */
    getRotationY(): import('./int').int;
    /**
     * Gets the Z-axis rotation in degrees.
     * @return the Z rotation
     */
    getRotationZ(): import('./int').int;
    /**
     * Sets the X-axis rotation in degrees.
     * @param x the X rotation to set
     */
    setRotationX(x: import('./int').int): import('./void').void;
    /**
     * Sets the Y-axis rotation in degrees.
     * @param y the Y rotation to set
     */
    setRotationY(y: import('./int').int): import('./void').void;
    /**
     * Sets the Z-axis rotation in degrees.
     * @param z the Z rotation to set. Default: 0.5
     */
    setRotationZ(z: import('./int').int): import('./void').void;
    /**
     * Gets the X-axis positional offset.
     * @return the X offset
     */
    getOffsetX(): import('./float').float;
    /**
     * Gets the Y-axis positional offset.
     * @return the Y offset
     */
    getOffsetY(): import('./float').float;
    /**
     * Gets the Z-axis positional offset.
     * @return the Z offset
     */
    getOffsetZ(): import('./float').float;
    /**
     * Sets the X-axis positional offset.
     * @param x the X offset to set
     */
    setOffsetX(x: import('./float').float): import('./void').void;
    /**
     * Sets the Y-axis positional offset.
     * @param y the Y offset to set
     */
    setOffsetY(y: import('./float').float): import('./void').void;
    /**
     * Sets the Z-axis positional offset.
     * @param z the Z offset to set
     */
    setOffsetZ(z: import('./float').float): import('./void').void;
    /**
     * Gets the scale of the text plane.
     * @return the scale factor
     */
    getScale(): import('./float').float;
    /**
     * Sets the scale of the text plane.
     * @param scale the scale factor to set. Default: 1
     */
    setScale(scale: import('./float').float): import('./void').void;
}
