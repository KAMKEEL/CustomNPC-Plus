/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * A telegraph is a visual warning indicator that displays on the ground.
 * Telegraphs are commonly used to warn players of incoming attacks or mark areas of effect.
 * <p>
 * To create a telegraph:
 * <pre>
 * var telegraph = API.getTelegraphs().createCircle(5.0);
 * telegraph.setDuration(60); // 3 seconds
 * telegraph.setColor(0x80FF0000); // Semi-transparent red
 * var instance = telegraph.spawn(world, x, y, z);
 * </pre>
 * <p>
 * Telegraph types:
 * <ul>
 *     <li>CIRCLE - A filled circle with a radius</li>
 *     <li>RING - A ring with inner and outer radius</li>
 *     <li>LINE - A rectangular line in a direction</li>
 *     <li>CONE - A cone/arc shape</li>
 *     <li>SQUARE - A square shape</li>
 *     <li>POINT - A small point marker</li>
 * </ul>
  * @javaFqn noppes.npcs.api.ITelegraph
*/
export interface ITelegraph {
    /**
     * Get the telegraph type.
     *
     * @return Type name: "CIRCLE", "RING", "LINE", "CONE", "SQUARE", or "POINT"
     */
    getType(): String;
    /**
     * Set the telegraph type.
     *
     * @param type Type name: "circle", "ring", "line", "cone", "square", or "point" (case insensitive)
     */
    setType(type: String): import('./void').void;
    /**
     * Get the radius (for CIRCLE, SQUARE and RING types).
     * @return the circle/ring radius in blocks
     */
    getRadius(): import('./float').float;
    /**
     * Set the radius (for CIRCLE, SQUARE and RING types).
     * @param radius circle/ring radius in blocks
     */
    setRadius(radius: import('./float').float): import('./void').void;
    /**
     * Get the inner radius (for RING type).
     * @return the inner radius for RING type in blocks
     */
    getInnerRadius(): import('./float').float;
    /**
     * Set the inner radius (for RING type).
     * @param innerRadius inner radius for RING type in blocks
     */
    setInnerRadius(innerRadius: import('./float').float): import('./void').void;
    /**
     * Get the length (for LINE and CONE types).
     * @return the length for LINE/CONE types in blocks
     */
    getLength(): import('./float').float;
    /**
     * Set the length (for LINE and CONE types).
     * @param length length for LINE/CONE types in blocks
     */
    setLength(length: import('./float').float): import('./void').void;
    /**
     * Get the width (for LINE type).
     * @return the width for LINE type in blocks
     */
    getWidth(): import('./float').float;
    /**
     * Set the width (for LINE type).
     * @param width width for LINE type in blocks
     */
    setWidth(width: import('./float').float): import('./void').void;
    /**
     * Get the angle in degrees (for CONE type).
     * @return the arc angle for CONE type in degrees
     */
    getAngle(): import('./float').float;
    /**
     * Set the angle in degrees (for CONE type).
     * @param angle arc angle for CONE type in degrees
     */
    setAngle(angle: import('./float').float): import('./void').void;
    /**
     * Get the duration in ticks.
     * @return duration in ticks (20 ticks = 1 second)
     */
    getDuration(): import('./int').int;
    /**
     * Set the duration in ticks (20 ticks = 1 second).
     * @param ticks duration in ticks (20 ticks = 1 second)
     */
    setDuration(ticks: import('./int').int): import('./void').void;
    /**
     * Get the primary color in ARGB format.
     * @return the primary color in ARGB format
     */
    getColor(): import('./int').int;
    /**
     * Set the primary color in ARGB format.
     * Example: 0x80FF0000 for semi-transparent red
     * @param argb primary color in ARGB format (e.g. 0x80FF0000)
     */
    setColor(argb: import('./int').int): import('./void').void;
    /**
     * Get the warning phase color in ARGB format.
     * @return the warning phase color in ARGB format
     */
    getWarningColor(): import('./int').int;
    /**
     * Set the warning phase color in ARGB format.
     * This color is used during the final warning phase.
     * @param argb warning phase color in ARGB format
     */
    setWarningColor(argb: import('./int').int): import('./void').void;
    /**
     * Get the tick at which warning phase begins.
     * @return the tick at which warning phase begins
     */
    getWarningStartTick(): import('./int').int;
    /**
     * Set the tick at which warning phase begins.
     * When remaining ticks falls below this value, the telegraph uses the warning color.
     * @param tick tick at which warning phase begins
     */
    setWarningStartTick(tick: import('./int').int): import('./void').void;
    /**
     * Check if pulsing animation is enabled.
     * @return true if pulsing animation is enabled
     */
    isAnimated(): import('./boolean').boolean;
    /**
     * Enable or disable pulsing animation.
     * @param animated whether pulsing animation is enabled
     */
    setAnimated(animated: import('./boolean').boolean): import('./void').void;
    /**
     * Get the height offset from ground level.
     * @return the height offset from ground level in blocks
     */
    getHeightOffset(): import('./float').float;
    /**
     * Set the height offset from ground level.
     * Default is 0.1 to prevent z-fighting with ground blocks.
     * @param offset height offset from ground level in blocks
     */
    setHeightOffset(offset: import('./float').float): import('./void').void;
    /**
     * Spawn this telegraph at a position.
     *
     * @param world The world to spawn in
     * @param x     X position
     * @param y     Y position
     * @param z     Z position
     * @return The spawned telegraph instance
     */
    spawn(world: import('./IWorld').IWorld, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./ITelegraphInstance').ITelegraphInstance;
    /**
     * Spawn this telegraph at a position with rotation.
     *
     * @param world The world to spawn in
     * @param x     X position
     * @param y     Y position
     * @param z     Z position
     * @param yaw   Rotation yaw (for directional telegraphs like LINE/CONE)
     * @return The spawned telegraph instance
     */
    spawn(world: import('./IWorld').IWorld, x: import('./double').double, y: import('./double').double, z: import('./double').double, yaw: import('./float').float): import('./ITelegraphInstance').ITelegraphInstance;
    /**
     * Spawn this telegraph following an entity.
     * The telegraph will update its position to follow the entity.
     *
     * @param entity The entity to follow
     * @return The spawned telegraph instance
     */
    spawn(entity: import('./entity/IEntity').IEntity): import('./ITelegraphInstance').ITelegraphInstance;
    /**
     * Spawn this telegraph following an entity with rotation.
     *
     * @param entity The entity to follow
     * @param yaw    Rotation yaw
     * @return The spawned telegraph instance
     */
    spawn(entity: import('./entity/IEntity').IEntity, yaw: import('./float').float): import('./ITelegraphInstance').ITelegraphInstance;
}
