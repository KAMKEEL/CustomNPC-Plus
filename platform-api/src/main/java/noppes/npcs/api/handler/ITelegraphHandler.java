package noppes.npcs.api.handler;

import noppes.npcs.api.ITelegraph;
import noppes.npcs.api.ITelegraphInstance;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

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
 */
public interface ITelegraphHandler {

    // ==========================================================================
    // FACTORY METHODS
    // ==========================================================================

    /**
     * Create a circle telegraph.
     *
     * @param radius The radius of the circle
     * @return A new telegraph configuration
     */
    ITelegraph createCircle(float radius);

    /**
     * Create a ring telegraph.
     *
     * @param outerRadius The outer radius
     * @param innerRadius The inner radius
     * @return A new telegraph configuration
     */
    ITelegraph createRing(float outerRadius, float innerRadius);

    /**
     * Create a line telegraph.
     *
     * @param length The length of the line
     * @param width  The width of the line
     * @return A new telegraph configuration
     */
    ITelegraph createLine(float length, float width);

    /**
     * Create a cone telegraph.
     *
     * @param length The length of the cone
     * @param angle  The angle of the cone in degrees
     * @return A new telegraph configuration
     */
    ITelegraph createCone(float length, float angle);

    /**
     * Create a square telegraph.
     * The square is axis-aligned by default and can be rotated with yaw when spawned.
     *
     * @param radius The half-size of the square (distance from center to edge)
     * @return A new telegraph configuration
     */
    ITelegraph createSquare(float radius);

    /**
     * Create a point telegraph.
     *
     * @return A new telegraph configuration
     */
    ITelegraph createPoint();

    /**
     * Create a telegraph by type name.
     *
     * @param type Type name: "circle", "ring", "line", "cone", "square", "point" (case insensitive)
     * @return A new telegraph configuration, or null if type is invalid
     */
    ITelegraph create(String type);

    // ==========================================================================
    // SPAWN METHODS
    // ==========================================================================

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
    ITelegraphInstance spawn(ITelegraph telegraph, IWorld world, double x, double y, double z);

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
    ITelegraphInstance spawn(ITelegraph telegraph, IWorld world, double x, double y, double z, float yaw);

    /**
     * Spawn a telegraph following an entity.
     *
     * @param telegraph The telegraph configuration
     * @param entity    The entity to follow
     * @return The spawned instance
     */
    ITelegraphInstance spawn(ITelegraph telegraph, IEntity entity);

    /**
     * Spawn a telegraph following an entity with rotation.
     *
     * @param telegraph The telegraph configuration
     * @param entity    The entity to follow
     * @param yaw       Rotation yaw
     * @return The spawned instance
     */
    ITelegraphInstance spawn(ITelegraph telegraph, IEntity entity, float yaw);

    // ==========================================================================
    // PRESET MANAGEMENT
    // ==========================================================================

    /**
     * Get a saved telegraph preset by name.
     *
     * @param name The preset name
     * @return The telegraph configuration, or null if not found
     */
    ITelegraph get(String name);

    /**
     * Save a telegraph as a preset.
     *
     * @param name      The name for the preset
     * @param telegraph The telegraph configuration to save
     */
    void save(String name, ITelegraph telegraph);

    /**
     * Delete a saved preset.
     *
     * @param name The preset name
     */
    void delete(String name);

    /**
     * Check if a preset exists.
     *
     * @param name The preset name
     * @return True if the preset exists
     */
    boolean has(String name);

    /**
     * Get all saved preset names.
     *
     * @return Array of preset names
     */
    String[] getSavedNames();

    // ==========================================================================
    // REMOVAL
    // ==========================================================================

    /**
     * Remove an active telegraph by instance ID.
     *
     * @param instanceId The instance ID to remove
     */
    void remove(String instanceId);

    /**
     * Remove an active telegraph instance.
     *
     * @param instance The instance to remove
     */
    void remove(ITelegraphInstance instance);
}
