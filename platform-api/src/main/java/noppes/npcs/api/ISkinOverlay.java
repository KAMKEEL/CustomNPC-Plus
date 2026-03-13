package noppes.npcs.api;

/**
 * A skin overlay object for both NPCs and players. Several skin overlays at a time can be layered on top of each other.
 * In an NPC's case, the old overlay texture is now the skin overlay with ID 0.
 * <p>
 * Modifying these attributes, an overlay can be made to look like a powered creeper effect, just a new skin, or both!
 */
public interface ISkinOverlay {
    /**
     * Sets the directory texture of the overlay. Does not support URLs.
     * @param texture the texture path
     */
    void setTexture(String texture);

    String getTexture();

    /**
     * Sets whether this overlay glows in the dark or not.
     * @param glow whether to glow
     */
    void setGlow(boolean glow);

    boolean getGlow();

    /**
     * Sets whether this overlay blends on top of bottom textures at any alpha value.
     * @param blend whether to blend
     */
    void setBlend(boolean blend);

    boolean getBlend();

    /**
     * Sets the transparency of the overlay, from 0 to 1.
     * @param alpha the alpha value
     */
    void setAlpha(float alpha);

    float getAlpha();

    /**
     * Sets the size of the entire overlay when rendered on the entity. By default, overlays render slightly on top of
     * the entity's model.
     * @param size the overlay size
     */
    void setSize(float size);

    float getSize();

    void setColor(int color);

    int getColor();

    /**
     * Sets the texture scale of the overlay. Higher values will "zoom in" to the texture more.
     * @param scaleX horizontal scale
     * @param scaleY vertical scale
     */
    void setTextureScale(float scaleX, float scaleY);

    float getTextureScaleX();

    float getTextureScaleY();

    /**
     * Sets the "texture speed" by which the texture shifts on each render tick. Used to create a powered creeper effect!
     * This does not move the entire overlay, it just moves the position at which the texture begins rendering.
     * @param speedX horizontal speed
     * @param speedY vertical speed
     */
    void setSpeed(float speedX, float speedY);

    float getSpeedX();

    float getSpeedY();

    /**
     * Sets the amount in each direction by which the overlay will be offset from the entity.
     * @param offsetX horizontal render offset
     * @param offsetY vertical render offset
     * @param offsetZ depth render offset
     */
    void setOffset(float offsetX, float offsetY, float offsetZ);

    float getOffsetX();

    float getOffsetY();

    float getOffsetZ();
}
