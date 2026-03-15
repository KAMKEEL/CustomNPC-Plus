/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

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
  * @javaFqn noppes.npcs.api.ITelegraphInstance
*/
export interface ITelegraphInstance {
    /**
     * Get the unique instance ID.
     * @return the unique instance identifier
     */
    getInstanceId(): String;
    /**
     * Get the current X position.
     * @return the current X coordinate
     */
    getX(): import('./double').double;
    /**
     * Get the current Y position.
     * @return the current Y coordinate
     */
    getY(): import('./double').double;
    /**
     * Get the current Z position.
     * @return the current Z coordinate
     */
    getZ(): import('./double').double;
    /**
     * Get the rotation yaw.
     * @return the rotation yaw in degrees
     */
    getYaw(): import('./float').float;
    /**
     * Set the position.
     * Note: Only works for static telegraphs (not following an entity).
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    setPosition(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     * Make this telegraph follow an entity.
     * The telegraph position will update each tick to match the entity.
     * @param entity the entity to follow
     */
    followEntity(entity: import('./entity/IEntity').IEntity): import('./void').void;
    /**
     * Stop following any entity and fix the current position.
     */
    stopFollowing(): import('./void').void;
    /**
     * Check if this telegraph is following an entity.
     * @return true if currently following an entity
     */
    isFollowing(): import('./boolean').boolean;
    /**
     * Get the remaining ticks before the telegraph expires.
     * @return remaining ticks before expiration
     */
    getRemainingTicks(): import('./int').int;
    /**
     * Get the total duration ticks.
     * @return the total duration in ticks
     */
    getTotalTicks(): import('./int').int;
    /**
     * Get the progress from 0.0 (just started) to 1.0 (finished).
     * @return progress from 0.0 (just started) to 1.0 (finished)
     */
    getProgress(): import('./float').float;
    /**
     * Check if the telegraph is in the warning phase.
     * @return true if in the warning phase
     */
    isWarning(): import('./boolean').boolean;
    /**
     * Remove this telegraph immediately.
     * Sends a removal packet to all clients tracking it.
     */
    remove(): import('./void').void;
    /**
     * Lock the telegraph at its current position.
     * Equivalent to calling stopFollowing().
     */
    lockPosition(): import('./void').void;
}
