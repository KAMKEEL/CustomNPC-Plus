/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a category that groups transport locations together.
  * @javaFqn noppes.npcs.api.handler.data.ITransportCategory
*/
export interface ITransportCategory {
    /** @return the unique category ID. */
    getId(): import('./int').int;
    /** @param title the category title. */
    setTitle(title: String): import('./void').void;
    /** @return the category title. */
    getTitle(): String;
    /**
     * Adds a new transport location with the given name to this category.
     *
     * @param name the location name.
     */
    addLocation(name: String): import('./void').void;
    /**
     * Returns the transport location with the given name.
     *
     * @param name the location name.
     * @return the transport location, or null if not found.
     */
    getLocation(name: String): import('./ITransportLocation').ITransportLocation;
    /**
     * Removes the transport location with the given name.
     *
     * @param name the location name.
     */
    removeLocation(name: String): import('./void').void;
}
