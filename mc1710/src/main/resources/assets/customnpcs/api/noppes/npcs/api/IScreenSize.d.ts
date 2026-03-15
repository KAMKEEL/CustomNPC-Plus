/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * Represents the scaled screen dimensions of the client display.
  * @javaFqn noppes.npcs.api.IScreenSize
*/
export interface IScreenSize {
    /**
     * Gets the scaled screen width in pixels.
     * @return the screen width
     */
    getWidth(): import('./int').int;
    /**
     * Gets the scaled screen height in pixels.
     * @return the screen height
     */
    getHeight(): import('./int').int;
    /**
     * Gets a pixel value representing the given percentage of the screen width.
     * @param percent the percentage of the width (0.0 to 100.0)
     * @return the corresponding pixel value
     */
    getWidthPercent(percent: import('./double').double): import('./int').int;
    /**
     * Gets a pixel value representing the given percentage of the screen height.
     * @param percent the percentage of the height (0.0 to 100.0)
     * @return the corresponding pixel value
     */
    getHeightPercent(percent: import('./double').double): import('./int').int;
}
