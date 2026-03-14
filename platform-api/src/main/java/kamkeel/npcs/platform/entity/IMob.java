package kamkeel.npcs.platform.entity;

/**
 * Platform-independent entity abstraction for core logic.
 * Thin interface — only exposes what core actually needs.
 *
 * Each MC version provides an implementation wrapping the native Entity class.
 * Use {@link #getHandle()} to retrieve the underlying MC object in platform code.
 */
public interface IMob {

    /**
     * @return the entity's numeric ID in the world
     */
    int getEntityId();

    /**
     * @return the entity's UUID as a string
     */
    String getUniqueID();

    // --- Position ---

    double getX();

    double getY();

    double getZ();

    float getYaw();

    float getPitch();

    void setPosition(double x, double y, double z);

    // --- State ---

    /**
     * @return true if the entity exists and is not dead
     */
    boolean isAlive();

    /**
     * @return the dimension/world the entity is in
     */
    IGameWorld getWorld();

    // --- Escape hatch ---

    /**
     * Returns the underlying MC entity object.
     * Platform code can cast this to the version-specific Entity class.
     * Core code should NEVER call this.
     *
     * @return the raw MC entity (e.g., net.minecraft.entity.Entity on 1.7.10)
     */
    Object getHandle();
}
