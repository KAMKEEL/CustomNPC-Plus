package noppes.npcs.api;

/**
 * A skin overlay object for both NPCs and players. Several skin overlays at a time can be layered on top of each other.
 * In an NPC's case, the old overlay texture is now the skin overlay with ID 0.
 *
 * Modifying these attributes, an overlay can be made to look like a powered creeper effect, just a new skin, or both!
 */
public interface ISkinOverlay {
    /**
     * Sets the directory texture of the overlay. Does not support URLs.
     */
    void setTexture(String texture);
    String getTexture();

    /**
     * Sets whether this overlay glows in the dark or not.
     */
    void setGlow(boolean glow);
    boolean getGlow();

    /**
     * Sets whether this overlay blends on top of bottom textures at any alpha value.
     */
    void setBlend(boolean blend);
    boolean getBlend();

    /**
     * Sets the transparency of the overlay, from 0 to 1.
     */
    void setAlpha(float alpha);
    float getAlpha();

    /**
     * Sets the size of the entire overlay when rendered on the entity. By default, overlays render slightly on top of
     * the entity's model.
     */
    void setSize(float size);
    float getSize();

    /**
     * Sets the texture scale of the overlay. Higher values will "zoom in" to the texture more.
     */
    void setTextureScale(float scaleX, float scaleY);
    float getTextureScaleX();
    float getTextureScaleY();

    /**
     * Sets the "texture speed" by which the texture shifts on each render tick. Used to create a powered creeper effect!
     * This does not move the entire overlay, it just moves the position at which the texture begins rendering.
     */
    void setSpeed(float speedX, float speedY);
    float getSpeedX();
    float getSpeedY();

    /**
     * Sets the amount in each direction by which the overlay will be offset from the entity.
     */
    void setOffset(float offsetX, float offsetY, float offsetZ);
    float getOffsetX();
    float getOffsetY();
    float getOffsetZ();
}
