/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a generic entity in the world.
 * Provides methods to access or modify the entity's position, motion, temporary and persistent data,
 * and to perform various actions (such as spawning particles, dropping items, or changing dimensions).
 *
 * @param <T> The underlying Minecraft entity type.
  * @javaFqn noppes.npcs.api.entity.IEntity
*/
export interface IEntity<T extends Entity /* net.minecraft.entity.Entity */> {
    /**
     * Spawns the given particle effect associated with this entity.
     *
     * @param entityParticle The particle effect to spawn.
     */
    spawnParticle(entityParticle: import('../IParticle').IParticle): import('./void').void;
    /**
     * Returns the unique entity ID.
     *
     * @return the entity ID.
     */
    getEntityId(): import('./int').int;
    /**
     * Returns the unique UUID string of the entity.
     *
     * @return the unique ID.
     */
    getUniqueID(): String;
    /**
     * Returns the vertical offset for rendering.
     *
     * @return the Y offset.
     */
    getYOffset(): import('./double').double;
    /**
     * @return The entity's width.
     */
    getWidth(): import('./double').double;
    /**
     * @return The entity's height.
     */
    getHeight(): import('./double').double;
    /**
     * @return The entity's x position.
     */
    getX(): import('./double').double;
    /**
     * Sets the entity's x position.
     *
     * @param x the new x position.
     */
    setX(x: import('./double').double): import('./void').void;
    /**
     * @return The entity's y position.
     */
    getY(): import('./double').double;
    /**
     * Sets the entity's y position.
     *
     * @param y the new y position.
     */
    setY(y: import('./double').double): import('./void').void;
    /**
     * @return The entity's z position.
     */
    getZ(): import('./double').double;
    /**
     * Sets the entity's z position.
     *
     * @param z the new z position.
     */
    setZ(z: import('./double').double): import('./void').void;
    /**
     * @return The entity's x motion.
     */
    getMotionX(): import('./double').double;
    /**
     * Sets the entity's x motion.
     *
     * @param x the new x motion.
     */
    setMotionX(x: import('./double').double): import('./void').void;
    /**
     * @return The entity's y motion.
     */
    getMotionY(): import('./double').double;
    /**
     * Sets the entity's y motion.
     *
     * @param y the new y motion.
     */
    setMotionY(y: import('./double').double): import('./void').void;
    /**
     * @return The entity's z motion.
     */
    getMotionZ(): import('./double').double;
    /**
     * Sets the entity's z motion.
     *
     * @param z the new z motion.
     */
    setMotionZ(z: import('./double').double): import('./void').void;
    /**
     * Convenience method to set the entity's motion in all three axes.
     *
     * @param x the x motion.
     * @param y the y motion.
     * @param z the z motion.
     */
    setMotion(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     * Sets the entity's motion based on the given position.
     *
     * @param pos the position containing motion components.
     */
    setMotion(pos: import('../IPos').IPos): import('./void').void;
    /**
     * Returns the entity's current motion as an IPos object.
     *
     * @return the motion vector.
     */
    getMotion(): import('../IPos').IPos;
    /**
     * @return Whether the entity is airborne.
     */
    isAirborne(): import('./boolean').boolean;
    /**
     * @return The block (integer) x position.
     */
    getBlockX(): import('./int').int;
    /**
     * @return The block (integer) y position.
     */
    getBlockY(): import('./int').int;
    /**
     * @return The block (integer) z position.
     */
    getBlockZ(): import('./int').int;
    /**
     * Sets the entity's position to the specified coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     */
    setPosition(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     * Sets the entity's position using an IPos object.
     *
     * @param pos the position object.
     */
    setPosition(pos: import('../IPos').IPos): import('./void').void;
    /**
     * Returns the entity's current position as an IPos object.
     *
     * @return the position.
     */
    getPosition(): import('../IPos').IPos;
    /**
     * @return The dimension ID the entity is in.
     */
    getDimension(): import('./int').int;
    /**
     * Moves the entity to the specified dimension.
     *
     * @param dimensionId the dimension ID.
     */
    setDimension(dimensionId: import('./int').int): import('./void').void;
    /**
     * @return Array of entities that are colliding with this entity
     */
    getCollidingEntities(): import('./IEntity').IEntity[];
    /**
     * Returns an array of entities within the specified range.
     *
     * @param range the search range.
     * @return an array of surrounding entities.
     */
    getSurroundingEntities(range: import('./int').int): import('./IEntity').IEntity[];
    /**
     * Returns an array of entities of a specified type within the given range.
     *
     * @param range the search range.
     * @param type  the EntityType to find.
     * @return an array of surrounding entities.
     */
    getSurroundingEntities(range: import('./int').int, type: import('./int').int): import('./IEntity').IEntity[];
    /**
     * @return Whether the entity is alive.
     */
    isAlive(): import('./boolean').boolean;
    /**
     * Returns temporary data stored under the given key.
     *
     * @param key the key.
     * @return the temporary data.
     */
    getTempData(key: String): Object;
    /**
     * Stores temporary data that is cleared when the entity is unloaded.
     *
     * @param key   the key.
     * @param value the value.
     */
    setTempData(key: String, value: Object): import('./void').void;
    /**
     * Checks if temporary data for the given key exists.
     *
     * @param key the key.
     * @return true if it exists; false otherwise.
     */
    hasTempData(key: String): import('./boolean').boolean;
    /**
     * Removes the temporary data for the given key.
     *
     * @param key the key.
     */
    removeTempData(key: String): import('./void').void;
    /**
     * Clears all temporary data.
     */
    clearTempData(): import('./void').void;
    /**
     * Returns an array of keys for temporary data.
     *
     * @return the keys.
     */
    getTempDataKeys(): String[];
    /**
     * Returns stored (persistent) data for the given key.
     *
     * @param key the key.
     * @return the stored data.
     */
    getStoredData(key: String): Object;
    /**
     * Stores persistent data under the given key.
     * Only Numbers and Strings are supported.
     *
     * @param key   the key.
     * @param value the value.
     */
    setStoredData(key: String, value: Object): import('./void').void;
    /**
     * Checks if stored data for the given key exists.
     *
     * @param key the key.
     * @return true if it exists; false otherwise.
     */
    hasStoredData(key: String): import('./boolean').boolean;
    /**
     * Removes the stored data for the given key.
     *
     * @param key the key.
     */
    removeStoredData(key: String): import('./void').void;
    /**
     * Clears all stored data.
     */
    clearStoredData(): import('./void').void;
    /**
     * Returns an array of keys for stored data.
     *
     * @return the keys.
     */
    getStoredDataKeys(): String[];
    /**
     * @return The age of the entity in ticks.
     */
    getAge(): import('./long').long;
    /**
     * Permanently despawns the entity.
     */
    despawn(): import('./void').void;
    /**
     * @return Whether the entity is standing in water.
     */
    inWater(): import('./boolean').boolean;
    /**
     * @return Whether the entity is standing in lava.
     */
    inLava(): import('./boolean').boolean;
    /**
     * @return Whether the entity is standing in fire.
     */
    inFire(): import('./boolean').boolean;
    /**
     * @return Whether the entity is on fire.
     */
    isBurning(): import('./boolean').boolean;
    /**
     * Sets the entity on fire for the given number of ticks (20 ticks = 1 second).
     *
     * @param ticks the burn duration.
     */
    setBurning(ticks: import('./int').int): import('./void').void;
    /**
     * Extinguishes any fire on the entity.
     */
    extinguish(): import('./void').void;
    /**
     * @return The entity type name as registered in Minecraft.
     */
    getTypeName(): String;
    /**
     * Causes the entity to drop the given item.
     *
     * @param item the item to drop.
     */
    dropItem(item: import('../item/IItemStack').IItemStack): import('./void').void;
    /**
     * @return The entity riding this entity.
     */
    getRider(): import('./IEntity').IEntity;
    /**
     * Sets the given entity as the rider of this entity.
     *
     * @param entity the rider.
     */
    setRider(entity: import('./IEntity').IEntity): import('./void').void;
    /**
     * @return The entity this entity is riding.
     */
    getMount(): import('./IEntity').IEntity;
    /**
     * Sets the entity to be mounted by this entity.
     *
     * @param entity the mount.
     */
    setMount(entity: import('./IEntity').IEntity): import('./void').void;
    /**
     * Returns the EntityType as defined in the scripting constants.
     *
     * @return the EntityType.
     */
    getType(): import('./int').int;
    /**
     * Checks whether the entity is of the specified EntityType.
     *
     * @param type the type to check.
     * @return true if the entity is of that type; false otherwise.
     */
    typeOf(type: import('./int').int): import('./boolean').boolean;
    /**
     * Sets the entity's rotation (yaw) in degrees (0-360).
     *
     * @param rotation the rotation angle.
     */
    setRotation(rotation: import('./float').float): import('./void').void;
    /**
     * Sets both the rotation (yaw) and pitch of the entity.
     *
     * @param rotationYaw   the yaw angle.
     * @param rotationPitch the pitch angle.
     */
    setRotation(rotationYaw: import('./float').float, rotationPitch: import('./float').float): import('./void').void;
    /**
     * @return The current rotation (yaw) of the entity.
     */
    getRotation(): import('./float').float;
    /**
     * Sets the entity's pitch.
     *
     * @param pitch the pitch angle.
     */
    setPitch(pitch: import('./float').float): import('./void').void;
    /**
     * Returns the entity's pitch.
     *
     * @return the pitch angle.
     */
    getPitch(): import('./float').float;
    /**
     * Applies a knockback effect to the entity.
     *
     * @param power     the strength of the knockback.
     * @param direction the direction in degrees (usually based on getRotation()).
     */
    knockback(power: import('./int').int, direction: import('./float').float): import('./void').void;
    /**
     * Applies knockback using individual power components.
     *
     * @param xpower    the x-axis power.
     * @param ypower    the y-axis power.
     * @param zpower    the z-axis power.
     * @param direction the direction in degrees.
     */
    knockback(xpower: import('./double').double, ypower: import('./double').double, zpower: import('./double').double, direction: import('./float').float): import('./void').void;
    /**
     * Applies knockback using a position vector.
     *
     * @param pos       the position vector.
     * @param direction the direction in degrees.
     */
    knockback(pos: import('../IPos').IPos, direction: import('./float').float): import('./void').void;
    /**
     * Sets the entity's immunity time (hurt resistance).
     *
     * @param ticks the number of ticks.
     */
    setImmune(ticks: import('./int').int): import('./void').void;
    /**
     * Sets the entity's invisibility.
     *
     * @param invisible true to make invisible.
     */
    setInvisible(invisible: import('./boolean').boolean): import('./void').void;
    /**
     * Sets whether the entity is sneaking.
     *
     * @param sneaking true to enable sneaking.
     */
    setSneaking(sneaking: import('./boolean').boolean): import('./void').void;
    /**
     * Sets whether the entity is sprinting.
     *
     * @param sprinting true to enable sprinting.
     */
    setSprinting(sprinting: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether the entity has collided with something.
     */
    hasCollided(): import('./boolean').boolean;
    /**
     * @return Whether the entity has collided vertically.
     */
    hasCollidedVertically(): import('./boolean').boolean;
    /**
     * @return Whether the entity has collided horizontally.
     */
    hasCollidedHorizontally(): import('./boolean').boolean;
    /**
     * @return Whether the entity is capturing drops.
     */
    capturesDrops(): import('./boolean').boolean;
    /**
     * Sets whether the entity captures drops.
     *
     * @param capture true to capture drops.
     */
    setCapturesDrops(capture: import('./boolean').boolean): import('./void').void;
    /**
     * Sets the captured drops for this entity.
     *
     * @param capturedDrops an array of captured drops.
     */
    setCapturedDrops(capturedDrops: import('./IEntity').IEntity[]): import('./void').void;
    /**
     * Returns the captured drops.
     *
     * @return an array of captured drops.
     */
    getCapturedDrops(): import('./IEntity').IEntity[];
    /**
     * @return Whether the entity is sneaking.
     * @since 1.7.10c
     */
    isSneaking(): import('./boolean').boolean;
    /**
     * @return Whether the entity is sprinting.
     * @since 1.7.10c
     */
    isSprinting(): import('./boolean').boolean;
    /**
     * Expert users only.
     *
     * @return The underlying Minecraft entity.
     */
    getMCEntity(): T;
    /**
     * Returns the entity's NBT data.
     *
     * @return the NBT data.
     */
    getNbt(): import('../INbt').INbt;
    /**
     * Returns all NBT data for the entity.
     *
     * @return the complete NBT data.
     */
    getAllNbt(): import('../INbt').INbt;
    /**
     * Applies the given NBT data to the entity.
     *
     * @param nbt the NBT data.
     */
    setNbt(nbt: import('../INbt').INbt): import('./void').void;
    /**
     * Returns optional NBT data if available.
     *
     * @return the optional NBT data, or null if none.
     */
    getNbtOptional(): import('../INbt').INbt;
    /**
     * Stores the entity as a clone with the given tab and name.
     *
     * @param tab  the tab index.
     * @param name the name for the clone.
     */
    storeAsClone(tab: import('./int').int, name: String): import('./void').void;
    /**
     * @return The world this entity exists in.
     */
    getWorld(): import('../IWorld').IWorld;
    /**
     * Updates the entity's state.
     */
    updateEntity(): import('./void').void;
}
