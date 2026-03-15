package noppes.npcs.api.block;

/**
 * Represents a text plane that can be positioned, rotated, and scaled on a block.
 */
public interface ITextPlane {

    /**
     * Gets the text displayed on this plane.
     * @return the display text
     */
    public String getText();

    /**
     * Sets the text displayed on this plane.
     * @param text the display text to set
     */
    public void setText(String text);

    /**
     * Gets the X-axis rotation in degrees.
     * @return the X rotation
     */
    public int getRotationX();

    /**
     * Gets the Y-axis rotation in degrees.
     * @return the Y rotation
     */
    public int getRotationY();

    /**
     * Gets the Z-axis rotation in degrees.
     * @return the Z rotation
     */
    public int getRotationZ();

    /**
     * Sets the X-axis rotation in degrees.
     * @param x the X rotation to set
     */
    public void setRotationX(int x);

    /**
     * Sets the Y-axis rotation in degrees.
     * @param y the Y rotation to set
     */
    public void setRotationY(int y);

    /**
     * Sets the Z-axis rotation in degrees.
     * @param z the Z rotation to set. Default: 0.5
     */
    public void setRotationZ(int z);

    /**
     * Gets the X-axis positional offset.
     * @return the X offset
     */
    public float getOffsetX();

    /**
     * Gets the Y-axis positional offset.
     * @return the Y offset
     */
    public float getOffsetY();

    /**
     * Gets the Z-axis positional offset.
     * @return the Z offset
     */
    public float getOffsetZ();

    /**
     * Sets the X-axis positional offset.
     * @param x the X offset to set
     */
    public void setOffsetX(float x);

    /**
     * Sets the Y-axis positional offset.
     * @param y the Y offset to set
     */
    public void setOffsetY(float y);

    /**
     * Sets the Z-axis positional offset.
     * @param z the Z offset to set
     */
    public void setOffsetZ(float z);

    /**
     * Gets the scale of the text plane.
     * @return the scale factor
     */
    public float getScale();

    /**
     * Sets the scale of the text plane.
     * @param scale the scale factor to set. Default: 1
     */
    public void setScale(float scale);
}
