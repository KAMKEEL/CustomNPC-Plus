/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Represents the scale configuration for a specific part of a model.
 * Allows setting and retrieving scale factors along the X, Y, and Z axes.
  * @javaFqn noppes.npcs.api.entity.data.IModelScalePart
*/
export interface IModelScalePart {
    /**
     * Sets the scale factors for this model part.
     *
     * @param x the scale factor along the X-axis.
     * @param y the scale factor along the Y-axis.
     * @param z the scale factor along the Z-axis.
     */
    setScale(x: import('./float').float, y: import('./float').float, z: import('./float').float): import('./void').void;
    /**
     * Returns the scale factor along the X-axis.
     *
     * @return the X-axis scale.
     */
    getScaleX(): import('./float').float;
    /**
     * Returns the scale factor along the Y-axis.
     *
     * @return the Y-axis scale.
     */
    getScaleY(): import('./float').float;
    /**
     * Returns the scale factor along the Z-axis.
     *
     * @return the Z-axis scale.
     */
    getScaleZ(): import('./float').float;
    scaleX: import('./float').float;
    scaleY: import('./float').float;
    scaleZ: import('./float').float;
}
