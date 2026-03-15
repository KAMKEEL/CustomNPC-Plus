/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents an item entity in the world.
 * Provides methods to manage pickup delay, age, ownership, and the contained item.
 *
 * @param <T> The underlying Minecraft EntityItem type.
  * @javaFqn noppes.npcs.api.entity.IEntityItem
*/
export interface IEntityItem<T extends EntityItem /* net.minecraft.entity.item.EntityItem */> extends import('./IEntity').IEntity {
    /**
     * Returns the owner name of the item.
     * Only the owner can pick the item up (unless the age is very low).
     *
     * @return the owner's name.
     */
    getOwner(): String;
    /**
     * Sets the owner name of the item.
     *
     * @param name the owner's name.
     */
    setOwner(name: String): import('./void').void;
    /**
     * Returns the thrower (entity that threw the item).
     *
     * @return the thrower's name.
     */
    getThrower(): String;
    /**
     * Sets the thrower (entity that threw the item).
     *
     * @param name the thrower's name.
     */
    setThrower(name: String): import('./void').void;
    /**
     * @return Ticks remaining before the item can be picked up.
     * (32767 indicates an infinite delay.)
     */
    getPickupDelay(): import('./int').int;
    /**
     * Sets the delay before the item can be picked up.
     *
     * @param delay the pickup delay in ticks (32767 for infinite delay).
     */
    setPickupDelay(delay: import('./int').int): import('./void').void;
    /**
     * @return The age of the item in ticks.
     */
    getAge(): import('./long').long;
    /**
     * Sets the age of the item.
     *
     * @param age the new age (?32767 indicates infinite age).
     */
    setAge(age: import('./long').long): import('./void').void;
    /**
     * @return The lifespan threshold; when the age reaches this, the item despawns.
     */
    getLifeSpawn(): import('./int').int;
    /**
     * Sets the age threshold at which the item will despawn.
     *
     * @param age the lifespan in ticks.
     */
    setLifeSpawn(age: import('./int').int): import('./void').void;
    /**
     * @return The item contained in this entity.
     */
    getItem(): import('../item/IItemStack').IItemStack;
    /**
     * Sets the item contained in this entity.
     *
     * @param item the item to set.
     */
    setItem(item: import('../item/IItemStack').IItemStack): import('./void').void;
}
