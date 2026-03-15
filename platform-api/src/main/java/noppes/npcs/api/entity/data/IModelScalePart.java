package noppes.npcs.api.entity.data;

/**
 * Represents the scale configuration for a specific part of a model.
 * Allows setting and retrieving scale factors along the X, Y, and Z axes.
 */
public interface IModelScalePart {

    /**
     * Sets the scale factors for this model part.
     *
     * @param x the scale factor along the X-axis.
     * @param y the scale factor along the Y-axis.
     * @param z the scale factor along the Z-axis.
     */
    void setScale(float x, float y, float z);

    /**
     * Returns the scale factor along the X-axis.
     *
     * @return the X-axis scale.
     */
    float getScaleX();

    /**
     * Returns the scale factor along the Y-axis.
     *
     * @return the Y-axis scale.
     */
    float getScaleY();

    /**
     * Returns the scale factor along the Z-axis.
     *
     * @return the Z-axis scale.
     */
    float getScaleZ();
}
