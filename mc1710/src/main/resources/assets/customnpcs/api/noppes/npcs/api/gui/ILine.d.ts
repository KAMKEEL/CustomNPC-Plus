/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

/**
 * Represents a line component in a custom GUI.
 * Provides methods to get or set endpoints and thickness.
  * @javaFqn noppes.npcs.api.gui.ILine
*/
export interface ILine extends import('./ICustomGuiComponent').ICustomGuiComponent {
    /**
     * Returns the starting x coordinate of the line.
     *
     * @return the x1 value.
     */
    getX1(): import('./int').int;
    /**
     * Returns the starting y coordinate of the line.
     *
     * @return the y1 value.
     */
    getY1(): import('./int').int;
    /**
     * Returns the ending x coordinate of the line.
     *
     * @return the x2 value.
     */
    getX2(): import('./int').int;
    /**
     * Returns the ending y coordinate of the line.
     *
     * @return the y2 value.
     */
    getY2(): import('./int').int;
    /**
     * Returns the thickness of the line.
     *
     * @return the thickness in pixels.
     */
    getThickness(): import('./int').int;
    /**
     * Sets the starting x coordinate of the line.
     *
     * @param x1 the new x1 value.
     */
    setX1(x1: import('./int').int): import('./void').void;
    /**
     * Sets the starting y coordinate of the line.
     *
     * @param y1 the new y1 value.
     */
    setY1(y1: import('./int').int): import('./void').void;
    /**
     * Sets the ending x coordinate of the line.
     *
     * @param x2 the new x2 value.
     */
    setX2(x2: import('./int').int): import('./void').void;
    /**
     * Sets the ending y coordinate of the line.
     *
     * @param y2 the new y2 value.
     */
    setY2(y2: import('./int').int): import('./void').void;
    /**
     * Sets the thickness of the line.
     *
     * @param thickness the new thickness.
     */
    setThickness(thickness: import('./int').int): import('./void').void;
}
