/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handler for creating and managing telegraphs.
 * Access via API.getTelegraphs()
 * <p>
 * Example usage:
 * <pre>
 * // Create a telegraph
 * var telegraph = API.getTelegraphs().createCircle(5.0);
 * telegraph.setDuration(60); // 3 seconds
 * telegraph.setColor(0x80FF0000); // Semi-transparent red
 *
 * // Spawn it
 * var instance = telegraph.spawn(world, x, y, z);
 *
 * // Or save as a preset for reuse
 * API.getTelegraphs().save("myCircle", telegraph);
 *
 * // Load preset later
 * var loaded = API.getTelegraphs().get("myCircle");
 * loaded.spawn(world, x, y, z);
 * </pre>
  * @javaFqn noppes.npcs.api.handler.ITelegraphHandler
*/
export interface ITelegraphHandler {
    /**
     * Create a circle telegraph.
     *
     * @param radius The radius of the circle
     * @return A new telegraph configuration
     */
    createCircle(radius: import('./float').float): import('../ITelegraph').ITelegraph;
    /**
     * Create a ring telegraph.
     *
     * @param outerRadius The outer radius
     * @param innerRadius The inner radius
     * @return A new telegraph configuration
     */
    createRing(outerRadius: import('./float').float, innerRadius: import('./float').float): import('../ITelegraph').ITelegraph;
    /**
     * Create a line telegraph.
     *
     * @param length The length of the line
     * @param width  The width of the line
     * @return A new telegraph configuration
     */
    createLine(length: import('./float').float, width: import('./float').float): import('../ITelegraph').ITelegraph;
    /**
     * Create a cone telegraph.
     *
     * @param length The length of the cone
     * @param angle  The angle of the cone in degrees
     * @return A new telegraph configuration
     */
    createCone(length: import('./float').float, angle: import('./float').float): import('../ITelegraph').ITelegraph;
    /**
     * Create a square telegraph.
     * The square is axis-aligned by default and can be rotated with yaw when spawned.
     *
     * @param radius The half-size of the square (distance from center to edge)
     * @return A new telegraph configuration
     */
    createSquare(radius: import('./float').float): import('../ITelegraph').ITelegraph;
    /**
     * Create a point telegraph.
     *
     * @return A new telegraph configuration
     */
    createPoint(): import('../ITelegraph').ITelegraph;
    /**
     * Create a telegraph by type name.
     *
     * @param type Type name: "circle", "ring", "line", "cone", "square", "point" (case insensitive)
     * @return A new telegraph configuration, or null if type is invalid
     */
    create(type: String): import('../ITelegraph').ITelegraph;
    /**
     * Spawn a telegraph at a position.
     *
     * @param telegraph The telegraph configuration
     * @param world     The world
     * @param x         X position
     * @param y         Y position
     * @param z         Z position
     * @return The spawned instance
     */
    spawn(telegraph: import('../ITelegraph').ITelegraph, world: import('../IWorld').IWorld, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('../ITelegraphInstance').ITelegraphInstance;
    /**
     * Spawn a telegraph at a position with rotation.
     *
     * @param telegraph The telegraph configuration
     * @param world     The world
     * @param x         X position
     * @param y         Y position
     * @param z         Z position
     * @param yaw       Rotation yaw
     * @return The spawned instance
     */
    spawn(telegraph: import('../ITelegraph').ITelegraph, world: import('../IWorld').IWorld, x: import('./double').double, y: import('./double').double, z: import('./double').double, yaw: import('./float').float): import('../ITelegraphInstance').ITelegraphInstance;
    /**
     * Spawn a telegraph following an entity.
     *
     * @param telegraph The telegraph configuration
     * @param entity    The entity to follow
     * @return The spawned instance
     */
    spawn(telegraph: import('../ITelegraph').ITelegraph, entity: import('../entity/IEntity').IEntity): import('../ITelegraphInstance').ITelegraphInstance;
    /**
     * Spawn a telegraph following an entity with rotation.
     *
     * @param telegraph The telegraph configuration
     * @param entity    The entity to follow
     * @param yaw       Rotation yaw
     * @return The spawned instance
     */
    spawn(telegraph: import('../ITelegraph').ITelegraph, entity: import('../entity/IEntity').IEntity, yaw: import('./float').float): import('../ITelegraphInstance').ITelegraphInstance;
    /**
     * Get a saved telegraph preset by name.
     *
     * @param name The preset name
     * @return The telegraph configuration, or null if not found
     */
    get(name: String): import('../ITelegraph').ITelegraph;
    /**
     * Save a telegraph as a preset.
     *
     * @param name      The name for the preset
     * @param telegraph The telegraph configuration to save
     */
    save(name: String, telegraph: import('../ITelegraph').ITelegraph): import('./void').void;
    /**
     * Delete a saved preset.
     *
     * @param name The preset name
     */
    delete(name: String): import('./void').void;
    /**
     * Check if a preset exists.
     *
     * @param name The preset name
     * @return True if the preset exists
     */
    has(name: String): import('./boolean').boolean;
    /**
     * Get all saved preset names.
     *
     * @return Array of preset names
     */
    getSavedNames(): String[];
    /**
     * Remove an active telegraph by instance ID.
     *
     * @param instanceId The instance ID to remove
     */
    remove(instanceId: String): import('./void').void;
    /**
     * Remove an active telegraph instance.
     *
     * @param instance The instance to remove
     */
    remove(instance: import('../ITelegraphInstance').ITelegraphInstance): import('./void').void;
}
