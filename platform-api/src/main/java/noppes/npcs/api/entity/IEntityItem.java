package noppes.npcs.api.entity;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents an item entity in the world.
 * Provides methods to manage pickup delay, age, ownership, and the contained item.
 *
 */
public interface IEntityItem extends IEntity {

    /**
     * Returns the owner name of the item.
     * Only the owner can pick the item up (unless the age is very low).
     *
     * @return the owner's name.
     */
    String getOwner();

    /**
     * Sets the owner name of the item.
     *
     * @param name the owner's name.
     */
    void setOwner(String name);

    /**
     * Returns the thrower (entity that threw the item).
     *
     * @return the thrower's name.
     */
    String getThrower();

    /**
     * Sets the thrower (entity that threw the item).
     *
     * @param name the thrower's name.
     */
    void setThrower(String name);

    /**
     * @return Ticks remaining before the item can be picked up.
     * (32767 indicates an infinite delay.)
     */
    int getPickupDelay();

    /**
     * Sets the delay before the item can be picked up.
     *
     * @param delay the pickup delay in ticks (32767 for infinite delay).
     */
    void setPickupDelay(int delay);

    /**
     * @return The age of the item in ticks.
     */
    long getAge();

    /**
     * Sets the age of the item.
     *
     * @param age the new age (−32767 indicates infinite age).
     */
    void setAge(long age);

    /**
     * @return The lifespan threshold; when the age reaches this, the item despawns.
     */
    int getLifeSpawn();

    /**
     * Sets the age threshold at which the item will despawn.
     *
     * @param age the lifespan in ticks.
     */
    void setLifeSpawn(int age);

    /**
     * @return The item contained in this entity.
     */
    IItemStack getItem();

    /**
     * Sets the item contained in this entity.
     *
     * @param item the item to set.
     */
    void setItem(IItemStack item);
}
