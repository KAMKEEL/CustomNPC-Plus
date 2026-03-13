package noppes.npcs.api.handler.data;

/**
 * Represents a category that groups transport locations together.
 */
public interface ITransportCategory {

    /** @return the unique category ID. */
    int getId();

    /** @param title the category title. */
    void setTitle(String title);

    /** @return the category title. */
    String getTitle();

    /**
     * Adds a new transport location with the given name to this category.
     *
     * @param name the location name.
     */
    void addLocation(String name);

    /**
     * Returns the transport location with the given name.
     *
     * @param name the location name.
     * @return the transport location, or null if not found.
     */
    ITransportLocation getLocation(String name);

    /**
     * Removes the transport location with the given name.
     *
     * @param name the location name.
     */
    void removeLocation(String name);
}
