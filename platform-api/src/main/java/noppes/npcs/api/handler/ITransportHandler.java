package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.ITransportCategory;

/**
 * Handles transport categories and their locations.
 */
public interface ITransportHandler {

    /**
     * Returns all transport categories.
     *
     * @return an array of transport categories.
     */
    ITransportCategory[] categories();

    /**
     * Creates a new transport category with the given title.
     *
     * @param title the category title.
     */
    void createCategory(String title);

    /**
     * Returns the transport category with the given title.
     *
     * @param title the category title.
     * @return the category, or null if not found.
     */
    ITransportCategory getCategory(String title);

    /**
     * Removes the transport category with the given title.
     *
     * @param title the category title.
     */
    void removeCategory(String title);
}
