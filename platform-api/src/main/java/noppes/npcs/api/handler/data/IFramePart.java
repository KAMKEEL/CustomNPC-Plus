package noppes.npcs.api.handler.data;

public interface IFramePart {

    /**
     * @return The name of the animation part (e.g. HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG, FULL_MODEL)
     */
    String getName();

    /**
     * @return The ordinal ID of the animation part
     */
    int getPartId();

    /**
     * Sets the animation part by name.
     * @param name The part name (e.g. HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG, FULL_MODEL)
     * @return This frame part for chaining
     */
    IFramePart setPart(String name);

    /**
     * Sets the animation part by ordinal ID.
     * @param partId The part ordinal ID
     * @return This frame part for chaining
     */
    IFramePart setPart(int partId);

    /**
     * @return The rotation angles as a float array [x, y, z] in degrees
     */
    float[] getRotations();

    /**
     * Sets the rotation angles.
     * @param rotation A float array [x, y, z] in degrees
     * @return This frame part for chaining
     */
    IFramePart setRotations(float[] rotation);

    /**
     * @return The pivot offsets as a float array [x, y, z]
     */
    float[] getPivots();

    /**
     * Sets the pivot offsets.
     * @param pivot A float array [x, y, z]
     * @return This frame part for chaining
     */
    IFramePart setPivots(float[] pivot);

    /**
     * @return Whether this part has custom speed and smooth settings that override the parent frame
     */
    boolean isCustomized();

    /**
     * Sets whether this part uses custom speed and smooth settings.
     * @param customized True to use part-level settings instead of the parent frame's
     * @return This frame part for chaining
     */
    IFramePart setCustomized(boolean customized);

    /**
     * @return The interpolation speed of this part
     */
    float getSpeed();

    /**
     * Sets the interpolation speed of this part.
     * @param speed The speed value
     * @return This frame part for chaining
     */
    IFramePart setSpeed(float speed);

    /**
     * @return The smooth interpolation type: 0 = Interpolated, 1 = Linear, 2 = None
     */
    byte isSmooth();

    /**
     * Sets the smooth interpolation type.
     * @param smooth The smooth type: 0 = Interpolated, 1 = Linear, 2 = None
     * @return This frame part for chaining
     */
    IFramePart setSmooth(byte smooth);
}
