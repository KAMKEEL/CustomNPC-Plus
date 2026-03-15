package noppes.npcs.api.entity.data;

/**
 * Represents the rotation settings for a specific part of a model.
 * Allows configuring the rotation angles along the X, Y, and Z axes
 * as well as enabling/disabling the rotation.
 */
public interface IModelRotatePart {

    /**
     * Sets the rotation angles for this model part.
     *
     * @param x the rotation angle around the X-axis.
     * @param y the rotation angle around the Y-axis.
     * @param z the rotation angle around the Z-axis.
     */
    void setRotation(float x, float y, float z);

    /**
     * Returns the rotation angle around the X-axis.
     *
     * @return the X-axis rotation.
     */
    float getRotateX();

    /**
     * Returns the rotation angle around the Y-axis.
     *
     * @return the Y-axis rotation.
     */
    float getRotateY();

    /**
     * Returns the rotation angle around the Z-axis.
     *
     * @return the Z-axis rotation.
     */
    float getRotateZ();

    /**
     * Enables or disables rotation for this part.
     *
     * @param enabled true to disable rotation; false to enable.
     */
    void disabled(boolean enabled);

    /**
     * Checks whether rotation is disabled for this part.
     *
     * @return true if rotation is disabled; false otherwise.
     */
    boolean disabled();
}
