/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IFrame
 */
export interface IFrame {
    /**
     * @return An array of all frame parts in this frame
     */
    getParts(): import('./IFramePart').IFramePart[];
    /**
     * Adds a frame part to this frame.
     * @param partConfig The frame part to add
     * @return This frame for chaining
     */
    addPart(partConfig: import('./IFramePart').IFramePart): import('./IFrame').IFrame;
    /**
     * Removes a frame part by its name.
     * @param partName The animation part name (e.g. HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG, FULL_MODEL)
     * @return This frame for chaining
     */
    removePart(partName: String): import('./IFrame').IFrame;
    /**
     * Removes a frame part by its ordinal ID.
     * @param partId The animation part ordinal ID
     * @return This frame for chaining
     */
    removePart(partId: import('./int').int): import('./IFrame').IFrame;
    /**
     * Removes all frame parts from this frame.
     * @return This frame for chaining
     */
    clearParts(): import('./IFrame').IFrame;
    /**
     * @return The duration of this frame in ticks
     */
    getDuration(): import('./int').int;
    /**
     * Sets the duration of this frame.
     * @param duration The duration in ticks
     * @return This frame for chaining
     */
    setDuration(duration: import('./int').int): import('./IFrame').IFrame;
    /**
     * @return Whether this frame has custom speed and smooth settings that override the parent animation
     */
    isCustomized(): import('./boolean').boolean;
    /**
     * Sets whether this frame uses custom speed and smooth settings.
     * @param customized True to use frame-level settings instead of the parent animation's
     * @return This frame for chaining
     */
    setCustomized(customized: import('./boolean').boolean): import('./IFrame').IFrame;
    /**
     * @return The interpolation speed of this frame
     */
    getSpeed(): import('./float').float;
    /**
     * Sets the interpolation speed of this frame.
     * @param speed The speed value
     * @return This frame for chaining
     */
    setSpeed(speed: import('./float').float): import('./IFrame').IFrame;
    /**
     * @return The smooth interpolation type: 0 = Interpolated, 1 = Linear, 2 = None
     */
    smoothType(): import('./byte').byte;
    /**
     * Sets the smooth interpolation type.
     * @param smooth The smooth type: 0 = Interpolated, 1 = Linear, 2 = None
     * @return This frame for chaining
     */
    setSmooth(smooth: import('./byte').byte): import('./IFrame').IFrame;
}
