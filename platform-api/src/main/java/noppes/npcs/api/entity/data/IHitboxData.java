package noppes.npcs.api.entity.data;

/**
 * Provides configuration data for an NPC's custom hitbox dimensions.
 * <p>
 * When enabled, allows independent scaling of the NPC's collision box
 * width and height beyond the default model-based sizing.
 */
public interface IHitboxData {

    /**
     * Returns whether the custom hitbox is enabled.
     *
     * @return true if the custom hitbox is enabled, false otherwise.
     */
    boolean isHitboxEnabled();

    /**
     * Sets whether the custom hitbox is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    void setHitboxEnabled(boolean enabled);

    /**
     * Returns the width scale factor for the hitbox.
     *
     * @return the width scale factor (1.0 = default).
     */
    float getWidthScale();

    /**
     * Sets the width scale factor for the hitbox.
     * Clamped to the server-configured maximum.
     *
     * @param widthScale the width scale factor.
     */
    void setWidthScale(float widthScale);

    /**
     * Returns the height scale factor for the hitbox.
     *
     * @return the height scale factor (1.0 = default).
     */
    float getHeightScale();

    /**
     * Sets the height scale factor for the hitbox.
     * Clamped to the server-configured maximum.
     *
     * @param heightScale the height scale factor.
     */
    void setHeightScale(float heightScale);
}
