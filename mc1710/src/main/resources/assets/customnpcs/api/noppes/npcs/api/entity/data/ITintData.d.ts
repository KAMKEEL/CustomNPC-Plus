/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Provides configuration data for an NPC's color tinting effects.
 * <p>
 * Controls both the hurt tint (flash color when damaged) and a persistent
 * general tint overlay applied to the NPC's model.
  * @javaFqn noppes.npcs.api.entity.data.ITintData
*/
export interface ITintData {
    /**
     * Returns whether the tint system is enabled.
     *
     * @return true if tinting is enabled, false otherwise.
     */
    isTintEnabled(): import('./boolean').boolean;
    /**
     * Sets whether the tint system is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    setTintEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Returns whether the hurt tint effect is enabled.
     *
     * @return true if hurt tint is enabled, false otherwise.
     */
    isHurtTintEnabled(): import('./boolean').boolean;
    /**
     * Sets whether the hurt tint effect is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    setHurtTintEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Returns whether the general (persistent) tint is enabled.
     *
     * @return true if general tint is enabled, false otherwise.
     */
    isGeneralTintEnabled(): import('./boolean').boolean;
    /**
     * Sets whether the general (persistent) tint is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    setGeneralTintEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Returns the hurt tint color as an RGB integer (e.g. 0xff0000 for red).
     *
     * @return the hurt tint color.
     */
    getHurtTint(): import('./int').int;
    /**
     * Sets the hurt tint color as an RGB integer.
     *
     * @param color the hurt tint color (e.g. 0xff0000 for red).
     */
    setHurtTint(color: import('./int').int): import('./void').void;
    /**
     * Returns the general tint color as an RGB integer.
     *
     * @return the general tint color.
     */
    getGeneralTint(): import('./int').int;
    /**
     * Sets the general tint color as an RGB integer.
     *
     * @param color the general tint color.
     */
    setGeneralTint(color: import('./int').int): import('./void').void;
    /**
     * Returns the alpha (opacity) of the general tint overlay (0-255).
     *
     * @return the general tint alpha value.
     */
    getGeneralAlpha(): import('./int').int;
    /**
     * Sets the alpha (opacity) of the general tint overlay (0-255).
     *
     * @param alpha the general tint alpha value.
     */
    setGeneralAlpha(alpha: import('./int').int): import('./void').void;
}
