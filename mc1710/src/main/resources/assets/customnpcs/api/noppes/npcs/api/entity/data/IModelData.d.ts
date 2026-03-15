/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

/**
 * Provides configuration data for a model including wearable settings,
 * hidden parts, rotation, and scale.
 * <p>
 * This interface allows controlling the appearance of an entity's model
 * such as headwear, bodywear, arm and leg configurations, and the ability
 * to hide parts. It also manages model rotation and scaling settings as well
 * as the association with an entity type.
  * @javaFqn noppes.npcs.api.entity.data.IModelData
*/
export interface IModelData {
    /**
     * Sets the headwear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the headwear configuration.
     */
    headWear(config: import('./byte').byte): import('./void').void;
    /**
     * Gets the current headwear configuration.
     *
     * @return the headwear configuration.
     */
    headWear(): import('./byte').byte;
    /**
     * Sets the bodywear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the bodywear configuration.
     */
    bodyWear(config: import('./byte').byte): import('./void').void;
    /**
     * Gets the current bodywear configuration.
     *
     * @return the bodywear configuration.
     */
    bodyWear(): import('./byte').byte;
    /**
     * Sets the right arm wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the right arm configuration.
     */
    rightArmWear(config: import('./byte').byte): import('./void').void;
    /**
     * Gets the current right arm wear configuration.
     *
     * @return the right arm configuration.
     */
    rightArmWear(): import('./byte').byte;
    /**
     * Sets the left arm wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the left arm configuration.
     */
    leftArmWear(config: import('./byte').byte): import('./void').void;
    /**
     * Gets the current left arm wear configuration.
     *
     * @return the left arm configuration.
     */
    leftArmWear(): import('./byte').byte;
    /**
     * Sets the right leg wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the right leg configuration.
     */
    rightLegWear(config: import('./byte').byte): import('./void').void;
    /**
     * Gets the current right leg wear configuration.
     *
     * @return the right leg configuration.
     */
    rightLegWear(): import('./byte').byte;
    /**
     * Sets the left leg wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the left leg configuration.
     */
    leftLegWear(config: import('./byte').byte): import('./void').void;
    /**
     * Gets the current left leg wear configuration.
     *
     * @return the left leg configuration.
     */
    leftLegWear(): import('./byte').byte;
    /**
     * Hide Body Parts
     * part: [0: Head, 1: Body, 2: Arms, 3: Legs]
     * hide: [0: None, 1: Both, 2: Right, 3: Left], only values 0 and 1 used for head and body.
     *
     * @param part the part index.
     * @param hide the hide configuration.
     */
    hidePart(part: import('./int').int, hide: import('./byte').byte): import('./void').void;
    /**
     * Returns the hide configuration for the specified part.
     *
     * @param part the part index.
     * @return the current hide configuration.
     */
    hidden(part: import('./int').int): import('./int').int;
    /**
     * Enables or disables rotation for the model.
     *
     * @param enableRotation true to enable rotation; false to disable.
     */
    enableRotation(enableRotation: import('./boolean').boolean): import('./void').void;
    /**
     * Checks whether rotation is enabled for the model.
     *
     * @return true if rotation is enabled; false otherwise.
     */
    enableRotation(): import('./boolean').boolean;
    /**
     * Returns the model rotation configuration.
     *
     * @return the rotation settings.
     */
    getRotation(): import('./IModelRotate').IModelRotate;
    /**
     * Returns the model scaling configuration.
     *
     * @return the scale settings.
     */
    getScale(): import('./IModelScale').IModelScale;
    /**
     * Associates this model data with an entity using its class name.
     *
     * @param string the fully qualified class name of the entity.
     */
    setEntity(string: String): import('./void').void;
    /**
     * Gets the entity class name associated with this model data.
     *
     * @return the entity class name, or null if not set.
     */
    getEntity(): String;
    modelScale: import('./ModelScale').ModelScale;
    enableRotation: import('./boolean').boolean;
    rotation: import('./ModelRotate').ModelRotate;
    legParts: import('./ModelPartData').ModelPartData;
    entityClass: Class<import('./EntityLivingBase').EntityLivingBase>;
    entity: import('./EntityLivingBase').EntityLivingBase;
    extra: import('./NBTTagCompound').NBTTagCompound;
    breasts: import('./byte').byte;
    headwear: import('./byte').byte;
    bodywear: import('./byte').byte;
    armwear: import('./byte').byte;
    legwear: import('./byte').byte;
    solidArmwear: import('./byte').byte;
    solidLegwear: import('./byte').byte;
    hideHead: import('./byte').byte;
    hideBody: import('./byte').byte;
    hideArms: import('./byte').byte;
    hideLegs: import('./byte').byte;
}
