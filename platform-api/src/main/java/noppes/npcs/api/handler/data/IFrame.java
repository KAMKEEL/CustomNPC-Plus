package noppes.npcs.api.handler.data;

public interface IFrame {

    /**
     * @return An array of all frame parts in this frame
     */
    IFramePart[] getParts();

    /**
     * Adds a frame part to this frame.
     * @param partConfig The frame part to add
     * @return This frame for chaining
     */
    IFrame addPart(IFramePart partConfig);

    /**
     * Removes a frame part by its name.
     * @param partName The animation part name (e.g. HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG, FULL_MODEL)
     * @return This frame for chaining
     */
    IFrame removePart(String partName);

    /**
     * Removes a frame part by its ordinal ID.
     * @param partId The animation part ordinal ID
     * @return This frame for chaining
     */
    IFrame removePart(int partId);

    /**
     * Removes all frame parts from this frame.
     * @return This frame for chaining
     */
    IFrame clearParts();

    /**
     * @return The duration of this frame in ticks
     */
    int getDuration();

    /**
     * Sets the duration of this frame.
     * @param duration The duration in ticks
     * @return This frame for chaining
     */
    IFrame setDuration(int duration);

    /**
     * @return Whether this frame has custom speed and smooth settings that override the parent animation
     */
    boolean isCustomized();

    /**
     * Sets whether this frame uses custom speed and smooth settings.
     * @param customized True to use frame-level settings instead of the parent animation's
     * @return This frame for chaining
     */
    IFrame setCustomized(boolean customized);

    /**
     * @return The interpolation speed of this frame
     */
    float getSpeed();

    /**
     * Sets the interpolation speed of this frame.
     * @param speed The speed value
     * @return This frame for chaining
     */
    IFrame setSpeed(float speed);

    /**
     * @return The smooth interpolation type: 0 = Interpolated, 1 = Linear, 2 = None
     */
    byte smoothType();

    /**
     * Sets the smooth interpolation type.
     * @param smooth The smooth type: 0 = Interpolated, 1 = Linear, 2 = None
     * @return This frame for chaining
     */
    IFrame setSmooth(byte smooth);
}
