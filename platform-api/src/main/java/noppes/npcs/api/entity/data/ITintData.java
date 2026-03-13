package noppes.npcs.api.entity.data;

/**
 * Provides configuration data for an NPC's color tinting effects.
 * <p>
 * Controls both the hurt tint (flash color when damaged) and a persistent
 * general tint overlay applied to the NPC's model.
 */
public interface ITintData {

    /**
     * Returns whether the tint system is enabled.
     *
     * @return true if tinting is enabled, false otherwise.
     */
    boolean isTintEnabled();

    /**
     * Sets whether the tint system is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    void setTintEnabled(boolean enabled);

    /**
     * Returns whether the hurt tint effect is enabled.
     *
     * @return true if hurt tint is enabled, false otherwise.
     */
    boolean isHurtTintEnabled();

    /**
     * Sets whether the hurt tint effect is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    void setHurtTintEnabled(boolean enabled);

    /**
     * Returns whether the general (persistent) tint is enabled.
     *
     * @return true if general tint is enabled, false otherwise.
     */
    boolean isGeneralTintEnabled();

    /**
     * Sets whether the general (persistent) tint is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    void setGeneralTintEnabled(boolean enabled);

    /**
     * Returns the hurt tint color as an RGB integer (e.g. 0xff0000 for red).
     *
     * @return the hurt tint color.
     */
    int getHurtTint();

    /**
     * Sets the hurt tint color as an RGB integer.
     *
     * @param color the hurt tint color (e.g. 0xff0000 for red).
     */
    void setHurtTint(int color);

    /**
     * Returns the general tint color as an RGB integer.
     *
     * @return the general tint color.
     */
    int getGeneralTint();

    /**
     * Sets the general tint color as an RGB integer.
     *
     * @param color the general tint color.
     */
    void setGeneralTint(int color);

    /**
     * Returns the alpha (opacity) of the general tint overlay (0-255).
     *
     * @return the general tint alpha value.
     */
    int getGeneralAlpha();

    /**
     * Sets the alpha (opacity) of the general tint overlay (0-255).
     *
     * @param alpha the general tint alpha value.
     */
    void setGeneralAlpha(int alpha);
}
