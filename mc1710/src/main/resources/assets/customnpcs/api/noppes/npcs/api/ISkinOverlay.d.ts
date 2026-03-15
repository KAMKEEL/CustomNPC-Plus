/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * A skin overlay object for both NPCs and players. Several skin overlays at a time can be layered on top of each other.
 * In an NPC's case, the old overlay texture is now the skin overlay with ID 0.
 * <p>
 * Modifying these attributes, an overlay can be made to look like a powered creeper effect, just a new skin, or both!
  * @javaFqn noppes.npcs.api.ISkinOverlay
*/
export interface ISkinOverlay {
    /**
     * Sets the directory texture of the overlay. Does not support URLs.
     * @param texture the texture path
     */
    setTexture(texture: String): import('./void').void;
    getTexture(): String;
    /**
     * Sets whether this overlay glows in the dark or not.
     * @param glow whether to glow
     */
    setGlow(glow: import('./boolean').boolean): import('./void').void;
    getGlow(): import('./boolean').boolean;
    /**
     * Sets whether this overlay blends on top of bottom textures at any alpha value.
     * @param blend whether to blend
     */
    setBlend(blend: import('./boolean').boolean): import('./void').void;
    getBlend(): import('./boolean').boolean;
    /**
     * Sets the transparency of the overlay, from 0 to 1.
     * @param alpha the alpha value
     */
    setAlpha(alpha: import('./float').float): import('./void').void;
    getAlpha(): import('./float').float;
    /**
     * Sets the size of the entire overlay when rendered on the entity. By default, overlays render slightly on top of
     * the entity's model.
     * @param size the overlay size
     */
    setSize(size: import('./float').float): import('./void').void;
    getSize(): import('./float').float;
    setColor(color: import('./int').int): import('./void').void;
    getColor(): import('./int').int;
    /**
     * Sets the texture scale of the overlay. Higher values will "zoom in" to the texture more.
     * @param scaleX horizontal scale
     * @param scaleY vertical scale
     */
    setTextureScale(scaleX: import('./float').float, scaleY: import('./float').float): import('./void').void;
    getTextureScaleX(): import('./float').float;
    getTextureScaleY(): import('./float').float;
    /**
     * Sets the "texture speed" by which the texture shifts on each render tick. Used to create a powered creeper effect!
     * This does not move the entire overlay, it just moves the position at which the texture begins rendering.
     * @param speedX horizontal speed
     * @param speedY vertical speed
     */
    setSpeed(speedX: import('./float').float, speedY: import('./float').float): import('./void').void;
    getSpeedX(): import('./float').float;
    getSpeedY(): import('./float').float;
    /**
     * Sets the amount in each direction by which the overlay will be offset from the entity.
     * @param offsetX horizontal render offset
     * @param offsetY vertical render offset
     * @param offsetZ depth render offset
     */
    setOffset(offsetX: import('./float').float, offsetY: import('./float').float, offsetZ: import('./float').float): import('./void').void;
    getOffsetX(): import('./float').float;
    getOffsetY(): import('./float').float;
    getOffsetZ(): import('./float').float;
}
