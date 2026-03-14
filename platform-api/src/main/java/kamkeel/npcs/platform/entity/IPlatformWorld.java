package kamkeel.npcs.platform.entity;

/**
 * Platform-independent world abstraction.
 */
public interface IPlatformWorld {

    /**
     * @return the dimension ID (e.g., 0 for overworld, -1 for nether)
     */
    int getDimensionId();

    /**
     * @return true if this is a client-side world
     */
    boolean isClient();

    /**
     * Look up an entity by its numeric ID.
     *
     * @param id the entity ID
     * @return the entity, or null if not found
     */
    IPlatformEntity getEntityById(int id);

    /**
     * Returns the underlying MC World object.
     * Core code should NEVER call this.
     *
     * @return the raw MC World
     */
    Object getHandle();
}
