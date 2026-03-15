package noppes.npcs.api.entity;

/**
 * Represents a living entity (mob, NPC, etc.) that extends the base living functionality.
 * Provides additional methods for navigation, sound, custom name, leashing, and more.
 */
public interface IEntityLiving extends IEntityLivingBase {

    /**
     * Checks if the entity is currently navigating (pathfinding).
     *
     * @return true if navigating; false otherwise.
     */
    boolean isNavigating();

    /**
     * Clears the current navigation path.
     */
    void clearNavigation();

    /**
     * Commands the entity to navigate toward the specified destination.
     *
     * @param x     Destination x coordinate.
     * @param y     Destination y coordinate.
     * @param z     Destination z coordinate.
     * @param speed Movement speed (0.7 is default).
     */
    void navigateTo(double x, double y, double z, double speed);

    /**
     * Returns the underlying Minecraft entity.
     *
     * @return the Minecraft entity.
     */
    Object getMCEntity();

    /**
     * Plays the entity's living sound.
     */
    void playLivingSound();

    /**
     * Spawns explosion particles for this entity.
     */
    void spawnExplosionParticle();

    /**
     * Sets the forward movement speed for the entity.
     *
     * @param speed the forward speed.
     */
    void setMoveForward(float speed);

    /**
     * Rotates the entity to face the given entity.
     *
     * @param entity the target entity.
     * @param pitch  the pitch angle.
     * @param yaw    the yaw angle.
     */
    void faceEntity(IEntity entity, float pitch, float yaw);

    /**
     * @return Whether the entity can pick up loot.
     */
    boolean canPickUpLoot();

    /**
     * Sets whether the entity can pick up loot.
     *
     * @param pickUp true to allow picking up loot.
     */
    void setCanPickUpLoot(boolean pickUp);

    /**
     * @return Whether the entity is persistent (won't despawn).
     */
    boolean isPersistent();

    /**
     * Enables persistence so that the entity does not despawn.
     */
    void enablePersistence();

    /**
     * Sets a custom name tag for the entity.
     *
     * @param text the custom name.
     */
    void setCustomNameTag(String text);

    /**
     * Returns the entity's custom name tag.
     *
     * @return the custom name.
     */
    String getCustomNameTag();

    /**
     * @return Whether the entity has a custom name tag.
     */
    boolean hasCustomNameTag();

    /**
     * Sets whether the entity's name tag is always rendered.
     *
     * @param alwaysRender true to always render the name tag.
     */
    void setAlwaysRenderNameTag(boolean alwaysRender);

    /**
     * @return Whether the name tag is always rendered.
     */
    boolean getAlwaysRenderNameTag();

    /**
     * Clears the entity's leash.
     *
     * @param sendPacket whether to send a packet update.
     * @param dropLeash  whether to drop the leash item.
     */
    void clearLeashed(boolean sendPacket, boolean dropLeash);

    /**
     * @return Whether the entity allows leashing.
     */
    boolean allowLeashing();

    /**
     * @return Whether the entity is currently leashed.
     */
    boolean getLeashed();

    /**
     * @return The entity to which this entity is leashed.
     */
    IEntity getLeashedTo();

    /**
     * Leashes this entity to the specified entity.
     *
     * @param entity     the entity to leash to.
     * @param sendPacket whether to send a packet update.
     */
    void setLeashedTo(IEntity entity, boolean sendPacket);

    /**
     * @return Whether the entity can be steered.
     */
    boolean canBeSteered();
}
