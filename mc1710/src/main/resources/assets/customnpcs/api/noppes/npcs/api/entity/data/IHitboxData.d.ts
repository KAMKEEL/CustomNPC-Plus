/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Provides configuration data for an NPC's custom hitbox dimensions.
 * <p>
 * When enabled, allows independent scaling of the NPC's collision box
 * width and height beyond the default model-based sizing.
  * @javaFqn noppes.npcs.api.entity.data.IHitboxData
*/
export interface IHitboxData {
    /**
     * Returns whether the custom hitbox is enabled.
     *
     * @return true if the custom hitbox is enabled, false otherwise.
     */
    isHitboxEnabled(): import('./boolean').boolean;
    /**
     * Sets whether the custom hitbox is enabled.
     *
     * @param enabled true to enable, false to disable.
     */
    setHitboxEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Returns the width scale factor for the hitbox.
     *
     * @return the width scale factor (1.0 = default).
     */
    getWidthScale(): import('./float').float;
    /**
     * Sets the width scale factor for the hitbox.
     * Clamped to the server-configured maximum.
     *
     * @param widthScale the width scale factor.
     */
    setWidthScale(widthScale: import('./float').float): import('./void').void;
    /**
     * Returns the height scale factor for the hitbox.
     *
     * @return the height scale factor (1.0 = default).
     */
    getHeightScale(): import('./float').float;
    /**
     * Sets the height scale factor for the hitbox.
     * Clamped to the server-configured maximum.
     *
     * @param heightScale the height scale factor.
     */
    setHeightScale(heightScale: import('./float').float): import('./void').void;
}
