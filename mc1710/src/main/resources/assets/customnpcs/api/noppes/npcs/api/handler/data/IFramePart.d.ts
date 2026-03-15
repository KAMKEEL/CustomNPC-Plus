/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IFramePart
 */
export interface IFramePart {
    /**
     * @return The name of the animation part (e.g. HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG, FULL_MODEL)
     */
    getName(): String;
    /**
     * @return The ordinal ID of the animation part
     */
    getPartId(): import('./int').int;
    /**
     * Sets the animation part by name.
     * @param name The part name (e.g. HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG, FULL_MODEL)
     * @return This frame part for chaining
     */
    setPart(name: String): import('./IFramePart').IFramePart;
    /**
     * Sets the animation part by ordinal ID.
     * @param partId The part ordinal ID
     * @return This frame part for chaining
     */
    setPart(partId: import('./int').int): import('./IFramePart').IFramePart;
    /**
     * @return The rotation angles as a float array [x, y, z] in degrees
     */
    getRotations(): import('./float').float[];
    /**
     * Sets the rotation angles.
     * @param rotation A float array [x, y, z] in degrees
     * @return This frame part for chaining
     */
    setRotations(rotation: import('./float').float[]): import('./IFramePart').IFramePart;
    /**
     * @return The pivot offsets as a float array [x, y, z]
     */
    getPivots(): import('./float').float[];
    /**
     * Sets the pivot offsets.
     * @param pivot A float array [x, y, z]
     * @return This frame part for chaining
     */
    setPivots(pivot: import('./float').float[]): import('./IFramePart').IFramePart;
    /**
     * @return Whether this part has custom speed and smooth settings that override the parent frame
     */
    isCustomized(): import('./boolean').boolean;
    /**
     * Sets whether this part uses custom speed and smooth settings.
     * @param customized True to use part-level settings instead of the parent frame's
     * @return This frame part for chaining
     */
    setCustomized(customized: import('./boolean').boolean): import('./IFramePart').IFramePart;
    /**
     * @return The interpolation speed of this part
     */
    getSpeed(): import('./float').float;
    /**
     * Sets the interpolation speed of this part.
     * @param speed The speed value
     * @return This frame part for chaining
     */
    setSpeed(speed: import('./float').float): import('./IFramePart').IFramePart;
    /**
     * @return The smooth interpolation type: 0 = Interpolated, 1 = Linear, 2 = None
     */
    isSmooth(): import('./byte').byte;
    /**
     * Sets the smooth interpolation type.
     * @param smooth The smooth type: 0 = Interpolated, 1 = Linear, 2 = None
     * @return This frame part for chaining
     */
    setSmooth(smooth: import('./byte').byte): import('./IFramePart').IFramePart;
}
