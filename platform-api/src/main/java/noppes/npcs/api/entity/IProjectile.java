package noppes.npcs.api.entity;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents a projectile entity.
 * Provides methods to access and modify the projectile's item, gravity, accuracy, heading,
 * and to enable scripting events.
 */
public interface IProjectile {

    /**
     * Returns the item associated with the projectile (e.g. arrow, fireball).
     *
     * @return the item as an IItemStack.
     */
    IItemStack getItem();

    /**
     * Sets the item for the projectile.
     *
     * @param item the item to set.
     */
    void setItem(IItemStack item);

    /**
     * @return true if the projectile is affected by gravity; false if it flies straight.
     */
    boolean getHasGravity();

    /**
     * Sets whether the projectile is affected by gravity.
     *
     * @param bo true to enable gravity; false otherwise.
     */
    void setHasGravity(boolean bo);

    /**
     * Returns the accuracy value for the projectile.
     *
     * @return the accuracy.
     */
    int getAccuracy();

    /**
     * Sets the accuracy value for the projectile.
     *
     * @param accuracy the accuracy value.
     */
    void setAccuracy(int accuracy);

    /**
     * Sets the heading (target destination) for the projectile based on the target entity.
     * The projectile's position should already be set.
     *
     * @param entity the target entity.
     */
    void setHeading(IEntity entity);

    /**
     * Sets the heading for the projectile to the specified coordinates.
     * The projectile's position should already be set.
     *
     * @param x the target x coordinate.
     * @param y the target y coordinate.
     * @param z the target z coordinate.
     */
    void setHeading(double x, double y, double z);

    /**
     * Sets the heading using yaw and pitch values.
     *
     * @param yaw   the yaw angle.
     * @param pitch the pitch angle.
     */
    void setHeading(float yaw, float pitch);

    /**
     * Returns the thrower (the entity that launched the projectile).
     *
     * @return the thrower as an IEntity.
     */
    IEntity getThrower();

    /**
     * Enables projectile events for the current scripting container.
     * Must be called during a script.
     **/
    void enableEvents();
}
