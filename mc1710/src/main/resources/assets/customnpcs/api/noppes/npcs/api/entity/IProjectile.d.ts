/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a projectile entity.
 * Provides methods to access and modify the projectile's item, gravity, accuracy, heading,
 * and to enable scripting events.
  * @javaFqn noppes.npcs.api.entity.IProjectile
*/
export interface IProjectile {
    /**
     * Returns the item associated with the projectile (e.g. arrow, fireball).
     *
     * @return the item as an IItemStack.
     */
    getItem(): import('../item/IItemStack').IItemStack;
    /**
     * Sets the item for the projectile.
     *
     * @param item the item to set.
     */
    setItem(item: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * @return true if the projectile is affected by gravity; false if it flies straight.
     */
    getHasGravity(): import('./boolean').boolean;
    /**
     * Sets whether the projectile is affected by gravity.
     *
     * @param bo true to enable gravity; false otherwise.
     */
    setHasGravity(bo: import('./boolean').boolean): import('./void').void;
    /**
     * Returns the accuracy value for the projectile.
     *
     * @return the accuracy.
     */
    getAccuracy(): import('./int').int;
    /**
     * Sets the accuracy value for the projectile.
     *
     * @param accuracy the accuracy value.
     */
    setAccuracy(accuracy: import('./int').int): import('./void').void;
    /**
     * Sets the heading (target destination) for the projectile based on the target entity.
     * The projectile's position should already be set.
     *
     * @param entity the target entity.
     */
    setHeading(entity: import('./IEntity').IEntity): import('./void').void;
    /**
     * Sets the heading for the projectile to the specified coordinates.
     * The projectile's position should already be set.
     *
     * @param x the target x coordinate.
     * @param y the target y coordinate.
     * @param z the target z coordinate.
     */
    setHeading(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     * Sets the heading using yaw and pitch values.
     *
     * @param yaw   the yaw angle.
     * @param pitch the pitch angle.
     */
    setHeading(yaw: import('./float').float, pitch: import('./float').float): import('./void').void;
    /**
     * Returns the thrower (the entity that launched the projectile).
     *
     * @return the thrower as an IEntity.
     */
    getThrower(): import('./IEntity').IEntity;
    /**
     * Enables projectile events for the current scripting container.
     * Must be called during a script.
     **/
    enableEvents(): import('./void').void;
}
