package noppes.npcs.api.entity.data;

/**
 * Provides configuration data for a model including wearable settings,
 * hidden parts, rotation, and scale.
 * <p>
 * This interface allows controlling the appearance of an entity's model
 * such as headwear, bodywear, arm and leg configurations, and the ability
 * to hide parts. It also manages model rotation and scaling settings as well
 * as the association with an entity type.
 */
public interface IModelData {

    /**
     * Sets the headwear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the headwear configuration.
     */
    void headWear(byte config);

    /**
     * Gets the current headwear configuration.
     *
     * @return the headwear configuration.
     */
    byte headWear();

    /**
     * Sets the bodywear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the bodywear configuration.
     */
    void bodyWear(byte config);

    /**
     * Gets the current bodywear configuration.
     *
     * @return the bodywear configuration.
     */
    byte bodyWear();

    /**
     * Sets the right arm wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the right arm configuration.
     */
    void rightArmWear(byte config);

    /**
     * Gets the current right arm wear configuration.
     *
     * @return the right arm configuration.
     */
    byte rightArmWear();

    /**
     * Sets the left arm wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the left arm configuration.
     */
    void leftArmWear(byte config);

    /**
     * Gets the current left arm wear configuration.
     *
     * @return the left arm configuration.
     */
    byte leftArmWear();

    /**
     * Sets the right leg wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the right leg configuration.
     */
    void rightLegWear(byte config);

    /**
     * Gets the current right leg wear configuration.
     *
     * @return the right leg configuration.
     */
    byte rightLegWear();

    /**
     * Sets the left leg wear configuration.
     * Valid values: 0 (hidden), 1 (2D), 2 (3D).
     *
     * @param config the left leg configuration.
     */
    void leftLegWear(byte config);

    /**
     * Gets the current left leg wear configuration.
     *
     * @return the left leg configuration.
     */
    byte leftLegWear();

    /**
     * Hide Body Parts
     * part: [0: Head, 1: Body, 2: Arms, 3: Legs]
     * hide: [0: None, 1: Both, 2: Right, 3: Left], only values 0 and 1 used for head and body.
     *
     * @param part the part index.
     * @param hide the hide configuration.
     */
    void hidePart(int part, byte hide);

    /**
     * Returns the hide configuration for the specified part.
     *
     * @param part the part index.
     * @return the current hide configuration.
     */
    int hidden(int part);

    /**
     * Enables or disables rotation for the model.
     *
     * @param enableRotation true to enable rotation; false to disable.
     */
    void enableRotation(boolean enableRotation);

    /**
     * Checks whether rotation is enabled for the model.
     *
     * @return true if rotation is enabled; false otherwise.
     */
    boolean enableRotation();

    /**
     * Returns the model rotation configuration.
     *
     * @return the rotation settings.
     */
    IModelRotate getRotation();

    /**
     * Returns the model scaling configuration.
     *
     * @return the scale settings.
     */
    IModelScale getScale();

    /**
     * Associates this model data with an entity using its class name.
     *
     * @param string the fully qualified class name of the entity.
     */
    void setEntity(String string);

    /**
     * Gets the entity class name associated with this model data.
     *
     * @return the entity class name, or null if not set.
     */
    String getEntity();
}
