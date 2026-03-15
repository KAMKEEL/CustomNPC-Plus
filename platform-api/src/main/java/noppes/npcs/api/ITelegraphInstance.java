package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

/**
 * A runtime instance of an active telegraph in the world.
 * Represents a spawned telegraph with position, timing, and control methods.
 * <p>
 * Instances are created when you call spawn() on an ITelegraph:
 * <pre>
 * var telegraph = API.getTelegraphs().createCircle(5.0);
 * var instance = telegraph.spawn(world, x, y, z);
 *
 * // Later...
 * instance.remove(); // Remove the telegraph early
 * </pre>
 */
public interface ITelegraphInstance {

    /**
     * Get the unique instance ID.
     * @return the unique instance identifier
     */
    String getInstanceId();

    // ==========================================================================
    // POSITION
    // ==========================================================================

    /**
     * Get the current X position.
     * @return the current X coordinate
     */
    double getX();

    /**
     * Get the current Y position.
     * @return the current Y coordinate
     */
    double getY();

    /**
     * Get the current Z position.
     * @return the current Z coordinate
     */
    double getZ();

    /**
     * Get the rotation yaw.
     * @return the rotation yaw in degrees
     */
    float getYaw();

    /**
     * Set the position.
     * Note: Only works for static telegraphs (not following an entity).
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    void setPosition(double x, double y, double z);

    // ==========================================================================
    // ENTITY FOLLOWING
    // ==========================================================================

    /**
     * Make this telegraph follow an entity.
     * The telegraph position will update each tick to match the entity.
     * @param entity the entity to follow
     */
    void followEntity(IEntity entity);

    /**
     * Stop following any entity and fix the current position.
     */
    void stopFollowing();

    /**
     * Check if this telegraph is following an entity.
     * @return true if currently following an entity
     */
    boolean isFollowing();

    // ==========================================================================
    // TIMING
    // ==========================================================================

    /**
     * Get the remaining ticks before the telegraph expires.
     * @return remaining ticks before expiration
     */
    int getRemainingTicks();

    /**
     * Get the total duration ticks.
     * @return the total duration in ticks
     */
    int getTotalTicks();

    /**
     * Get the progress from 0.0 (just started) to 1.0 (finished).
     * @return progress from 0.0 (just started) to 1.0 (finished)
     */
    float getProgress();

    /**
     * Check if the telegraph is in the warning phase.
     * @return true if in the warning phase
     */
    boolean isWarning();

    // ==========================================================================
    // CONTROL
    // ==========================================================================

    /**
     * Remove this telegraph immediately.
     * Sends a removal packet to all clients tracking it.
     */
    void remove();

    /**
     * Lock the telegraph at its current position.
     * Equivalent to calling stopFollowing().
     */
    void lockPosition();
}
