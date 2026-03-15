/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles transport categories and their locations.
  * @javaFqn noppes.npcs.api.handler.ITransportHandler
*/
export interface ITransportHandler {
    /**
     * Returns all transport categories.
     *
     * @return an array of transport categories.
     */
    categories(): import('./data/ITransportCategory').ITransportCategory[];
    /**
     * Creates a new transport category with the given title.
     *
     * @param title the category title.
     */
    createCategory(title: String): import('./void').void;
    /**
     * Returns the transport category with the given title.
     *
     * @param title the category title.
     * @return the category, or null if not found.
     */
    getCategory(title: String): import('./data/ITransportCategory').ITransportCategory;
    /**
     * Removes the transport category with the given title.
     *
     * @param title the category title.
     */
    removeCategory(title: String): import('./void').void;
}
