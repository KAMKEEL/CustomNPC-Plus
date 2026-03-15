package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

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
 */
public interface ITelegraph {

    // ==========================================================================
    // TYPE
    // ==========================================================================

    /**
     * Get the telegraph type.
     *
     * @return Type name: "CIRCLE", "RING", "LINE", "CONE", "SQUARE", or "POINT"
     */
    String getType();

    /**
     * Set the telegraph type.
     *
     * @param type Type name: "circle", "ring", "line", "cone", "square", or "point" (case insensitive)
     */
    void setType(String type);

    // ==========================================================================
    // SHAPE PARAMETERS
    // ==========================================================================

    /**
     * Get the radius (for CIRCLE, SQUARE and RING types).
     * @return the circle/ring radius in blocks
     */
    float getRadius();

    /**
     * Set the radius (for CIRCLE, SQUARE and RING types).
     * @param radius circle/ring radius in blocks
     */
    void setRadius(float radius);

    /**
     * Get the inner radius (for RING type).
     * @return the inner radius for RING type in blocks
     */
    float getInnerRadius();

    /**
     * Set the inner radius (for RING type).
     * @param innerRadius inner radius for RING type in blocks
     */
    void setInnerRadius(float innerRadius);

    /**
     * Get the length (for LINE and CONE types).
     * @return the length for LINE/CONE types in blocks
     */
    float getLength();

    /**
     * Set the length (for LINE and CONE types).
     * @param length length for LINE/CONE types in blocks
     */
    void setLength(float length);

    /**
     * Get the width (for LINE type).
     * @return the width for LINE type in blocks
     */
    float getWidth();

    /**
     * Set the width (for LINE type).
     * @param width width for LINE type in blocks
     */
    void setWidth(float width);

    /**
     * Get the angle in degrees (for CONE type).
     * @return the arc angle for CONE type in degrees
     */
    float getAngle();

    /**
     * Set the angle in degrees (for CONE type).
     * @param angle arc angle for CONE type in degrees
     */
    void setAngle(float angle);

    // ==========================================================================
    // TIMING
    // ==========================================================================

    /**
     * Get the duration in ticks.
     * @return duration in ticks (20 ticks = 1 second)
     */
    int getDuration();

    /**
     * Set the duration in ticks (20 ticks = 1 second).
     * @param ticks duration in ticks (20 ticks = 1 second)
     */
    void setDuration(int ticks);

    // ==========================================================================
    // COLORS
    // ==========================================================================

    /**
     * Get the primary color in ARGB format.
     * @return the primary color in ARGB format
     */
    int getColor();

    /**
     * Set the primary color in ARGB format.
     * Example: 0x80FF0000 for semi-transparent red
     * @param argb primary color in ARGB format (e.g. 0x80FF0000)
     */
    void setColor(int argb);

    /**
     * Get the warning phase color in ARGB format.
     * @return the warning phase color in ARGB format
     */
    int getWarningColor();

    /**
     * Set the warning phase color in ARGB format.
     * This color is used during the final warning phase.
     * @param argb warning phase color in ARGB format
     */
    void setWarningColor(int argb);

    /**
     * Get the tick at which warning phase begins.
     * @return the tick at which warning phase begins
     */
    int getWarningStartTick();

    /**
     * Set the tick at which warning phase begins.
     * When remaining ticks falls below this value, the telegraph uses the warning color.
     * @param tick tick at which warning phase begins
     */
    void setWarningStartTick(int tick);

    // ==========================================================================
    // ANIMATION
    // ==========================================================================

    /**
     * Check if pulsing animation is enabled.
     * @return true if pulsing animation is enabled
     */
    boolean isAnimated();

    /**
     * Enable or disable pulsing animation.
     * @param animated whether pulsing animation is enabled
     */
    void setAnimated(boolean animated);

    // ==========================================================================
    // POSITIONING
    // ==========================================================================

    /**
     * Get the height offset from ground level.
     * @return the height offset from ground level in blocks
     */
    float getHeightOffset();

    /**
     * Set the height offset from ground level.
     * Default is 0.1 to prevent z-fighting with ground blocks.
     * @param offset height offset from ground level in blocks
     */
    void setHeightOffset(float offset);

    // ==========================================================================
    // SPAWN METHODS
    // ==========================================================================

    /**
     * Spawn this telegraph at a position.
     *
     * @param world The world to spawn in
     * @param x     X position
     * @param y     Y position
     * @param z     Z position
     * @return The spawned telegraph instance
     */
    ITelegraphInstance spawn(IWorld world, double x, double y, double z);

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
    ITelegraphInstance spawn(IWorld world, double x, double y, double z, float yaw);

    /**
     * Spawn this telegraph following an entity.
     * The telegraph will update its position to follow the entity.
     *
     * @param entity The entity to follow
     * @return The spawned telegraph instance
     */
    ITelegraphInstance spawn(IEntity entity);

    /**
     * Spawn this telegraph following an entity with rotation.
     *
     * @param entity The entity to follow
     * @param yaw    Rotation yaw
     * @return The spawned telegraph instance
     */
    ITelegraphInstance spawn(IEntity entity, float yaw);
}
