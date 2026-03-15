/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Represents the rotation settings for a specific part of a model.
 * Allows configuring the rotation angles along the X, Y, and Z axes
 * as well as enabling/disabling the rotation.
  * @javaFqn noppes.npcs.api.entity.data.IModelRotatePart
*/
export interface IModelRotatePart {
    /**
     * Sets the rotation angles for this model part.
     *
     * @param x the rotation angle around the X-axis.
     * @param y the rotation angle around the Y-axis.
     * @param z the rotation angle around the Z-axis.
     */
    setRotation(x: import('./float').float, y: import('./float').float, z: import('./float').float): import('./void').void;
    /**
     * Returns the rotation angle around the X-axis.
     *
     * @return the X-axis rotation.
     */
    getRotateX(): import('./float').float;
    /**
     * Returns the rotation angle around the Y-axis.
     *
     * @return the Y-axis rotation.
     */
    getRotateY(): import('./float').float;
    /**
     * Returns the rotation angle around the Z-axis.
     *
     * @return the Z-axis rotation.
     */
    getRotateZ(): import('./float').float;
    /**
     * Enables or disables rotation for this part.
     *
     * @param enabled true to disable rotation; false to enable.
     */
    disabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Checks whether rotation is disabled for this part.
     *
     * @return true if rotation is disabled; false otherwise.
     */
    disabled(): import('./boolean').boolean;
    rotationX: import('./float').float;
    rotationY: import('./float').float;
    rotationZ: import('./float').float;
    disabled: import('./boolean').boolean;
}
