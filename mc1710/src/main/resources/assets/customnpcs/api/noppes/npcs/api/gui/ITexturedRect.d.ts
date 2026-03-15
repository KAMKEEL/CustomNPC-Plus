/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a textured rectangle component in a custom GUI.
 * Allows configuration of texture resource, size, scale, and texture offset.
  * @javaFqn noppes.npcs.api.gui.ITexturedRect
*/
export interface ITexturedRect extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Returns the texture resource location.
     *
     * @return the texture as a string.
     */
    getTexture(): String;
    /**
     * Sets the texture resource location.
     *
     * @param texture the texture resource location.
     * @return this textured rectangle instance.
     */
    setTexture(texture: String): import('./ITexturedRect').ITexturedRect;
    /**
     * Returns the width of the rectangle.
     *
     * @return the width in pixels.
     */
    getWidth(): import('./int').int;
    /**
     * Returns the height of the rectangle.
     *
     * @return the height in pixels.
     */
    getHeight(): import('./int').int;
    /**
     * Sets the size of the rectangle.
     *
     * @param width  the new width.
     * @param height the new height.
     * @return this textured rectangle instance.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./ITexturedRect').ITexturedRect;
    /**
     * Returns the scale factor of the rectangle.
     *
     * @return the scale.
     */
    getScale(): import('./float').float;
    /**
     * Sets the scale factor of the rectangle.
     *
     * @param scale the new scale.
     * @return this textured rectangle instance.
     */
    setScale(scale: import('./float').float): import('./ITexturedRect').ITexturedRect;
    /**
     * Returns the texture's x offset.
     *
     * @return the x offset.
     */
    getTextureX(): import('./int').int;
    /**
     * Returns the texture's y offset.
     *
     * @return the y offset.
     */
    getTextureY(): import('./int').int;
    /**
     * Sets the texture offset.
     *
     * @param textureX the new x offset.
     * @param textureY the new y offset.
     * @return this textured rectangle instance.
     */
    setTextureOffset(textureX: import('./int').int, textureY: import('./int').int): import('./ITexturedRect').ITexturedRect;
    /**
     * Returns whether this textured rect has animation enabled.
     *
     * @return true if animated.
     */
    isAnimated(): import('./boolean').boolean;
    /**
     * Returns the number of animation frames.
     *
     * @return the frame count.
     */
    getFrameCount(): import('./int').int;
    /**
     * Returns the ticks per animation frame.
     *
     * @return the frame time in ticks.
     */
    getFrameTime(): import('./int').int;
    /**
     * Enables animation on this textured rect using a vertical sprite strip.
     * The texture PNG should contain frames stacked vertically.
     *
     * @param frameCount number of frames in the strip.
     * @param frametime  ticks per frame (1 tick = 50ms).
     * @return this textured rectangle instance.
     */
    setAnimation(frameCount: import('./int').int, frametime: import('./int').int): import('./ITexturedRect').ITexturedRect;
}
