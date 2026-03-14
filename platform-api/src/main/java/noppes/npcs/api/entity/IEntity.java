package noppes.npcs.api.entity;

import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.item.IItemStack;

/**
 * Represents a generic entity in the world.
 * Provides methods to access or modify the entity's position, motion, temporary and persistent data,
 * and to perform various actions (such as dropping items or changing dimensions).
 */
public interface IEntity {

    /**
     * Returns the unique entity ID.
     *
     * @return the entity ID.
     */
    int getEntityId();

    /**
     * Returns the unique UUID string of the entity.
     *
     * @return the unique ID.
     */
    String getUniqueID();

    /**
     * Returns the vertical offset for rendering.
     *
     * @return the Y offset.
     */
    double getYOffset();

    /**
     * @return The entity's width.
     */
    double getWidth();

    /**
     * @return The entity's height.
     */
    double getHeight();

    /**
     * @return The entity's x position.
     */
    double getX();

    /**
     * Sets the entity's x position.
     *
     * @param x the new x position.
     */
    void setX(double x);

    /**
     * @return The entity's y position.
     */
    double getY();

    /**
     * Sets the entity's y position.
     *
     * @param y the new y position.
     */
    void setY(double y);

    /**
     * @return The entity's z position.
     */
    double getZ();

    /**
     * Sets the entity's z position.
     *
     * @param z the new z position.
     */
    void setZ(double z);

    /**
     * @return The entity's x motion.
     */
    double getMotionX();

    /**
     * Sets the entity's x motion.
     *
     * @param x the new x motion.
     */
    void setMotionX(double x);

    /**
     * @return The entity's y motion.
     */
    double getMotionY();

    /**
     * Sets the entity's y motion.
     *
     * @param y the new y motion.
     */
    void setMotionY(double y);

    /**
     * @return The entity's z motion.
     */
    double getMotionZ();

    /**
     * Sets the entity's z motion.
     *
     * @param z the new z motion.
     */
    void setMotionZ(double z);

    /**
     * Convenience method to set the entity's motion in all three axes.
     *
     * @param x the x motion.
     * @param y the y motion.
     * @param z the z motion.
     */
    void setMotion(double x, double y, double z);

    /**
     * Sets the entity's motion based on the given position.
     *
     * @param pos the position containing motion components.
     */
    void setMotion(IPos pos);

    /**
     * Returns the entity's current motion as an IPos object.
     *
     * @return the motion vector.
     */
    IPos getMotion();

    /**
     * @return Whether the entity is airborne.
     */
    boolean isAirborne();

    /**
     * @return The block (integer) x position.
     */
    int getBlockX();

    /**
     * @return The block (integer) y position.
     */
    int getBlockY();

    /**
     * @return The block (integer) z position.
     */
    int getBlockZ();

    /**
     * Sets the entity's position to the specified coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     */
    void setPosition(double x, double y, double z);

    /**
     * Sets the entity's position using an IPos object.
     *
     * @param pos the position object.
     */
    void setPosition(IPos pos);

    /**
     * Returns the entity's current position as an IPos object.
     *
     * @return the position.
     */
    IPos getPosition();

    /**
     * @return The dimension ID the entity is in.
     */
    int getDimension();

    /**
     * Moves the entity to the specified dimension.
     *
     * @param dimensionId the dimension ID.
     */
    void setDimension(int dimensionId);

    /**
     * @return Array of entities that are colliding with this entity
     */
    IEntity[] getCollidingEntities();

    /**
     * Returns an array of entities within the specified range.
     *
     * @param range the search range.
     * @return an array of surrounding entities.
     */
    IEntity[] getSurroundingEntities(int range);

    /**
     * Returns an array of entities of a specified type within the given range.
     *
     * @param range the search range.
     * @param type  the EntityType to find.
     * @return an array of surrounding entities.
     */
    IEntity[] getSurroundingEntities(int range, int type);

    /**
     * @return Whether the entity is alive.
     */
    boolean isAlive();

    /**
     * Returns temporary data stored under the given key.
     *
     * @param key the key.
     * @return the temporary data.
     */
    Object getTempData(String key);

    /**
     * Stores temporary data that is cleared when the entity is unloaded.
     *
     * @param key   the key.
     * @param value the value.
     */
    void setTempData(String key, Object value);

    /**
     * Checks if temporary data for the given key exists.
     *
     * @param key the key.
     * @return true if it exists; false otherwise.
     */
    boolean hasTempData(String key);

    /**
     * Removes the temporary data for the given key.
     *
     * @param key the key.
     */
    void removeTempData(String key);

    /**
     * Clears all temporary data.
     */
    void clearTempData();

    /**
     * Returns an array of keys for temporary data.
     *
     * @return the keys.
     */
    String[] getTempDataKeys();

    /**
     * Returns stored (persistent) data for the given key.
     *
     * @param key the key.
     * @return the stored data.
     */
    Object getStoredData(String key);

    /**
     * Stores persistent data under the given key.
     * Only Numbers and Strings are supported.
     *
     * @param key   the key.
     * @param value the value.
     */
    void setStoredData(String key, Object value);

    /**
     * Checks if stored data for the given key exists.
     *
     * @param key the key.
     * @return true if it exists; false otherwise.
     */
    boolean hasStoredData(String key);

    /**
     * Removes the stored data for the given key.
     *
     * @param key the key.
     */
    void removeStoredData(String key);

    /**
     * Clears all stored data.
     */
    void clearStoredData();

    /**
     * Returns an array of keys for stored data.
     *
     * @return the keys.
     */
    String[] getStoredDataKeys();

    /**
     * @return The age of the entity in ticks.
     */
    long getAge();

    /**
     * Permanently despawns the entity.
     */
    void despawn();

    /**
     * @return Whether the entity is standing in water.
     */
    boolean inWater();

    /**
     * @return Whether the entity is standing in lava.
     */
    boolean inLava();

    /**
     * @return Whether the entity is standing in fire.
     */
    boolean inFire();

    /**
     * @return Whether the entity is on fire.
     */
    boolean isBurning();

    /**
     * Sets the entity on fire for the given number of ticks (20 ticks = 1 second).
     *
     * @param ticks the burn duration.
     */
    void setBurning(int ticks);

    /**
     * Extinguishes any fire on the entity.
     */
    void extinguish();

    /**
     * @return The entity type name as registered in Minecraft.
     */
    String getTypeName();

    /**
     * Causes the entity to drop the given item.
     *
     * @param item the item to drop.
     */
    void dropItem(IItemStack item);

    /**
     * @return The entity riding this entity.
     */
    IEntity getRider();

    /**
     * Sets the given entity as the rider of this entity.
     *
     * @param entity the rider.
     */
    void setRider(IEntity entity);

    /**
     * @return The entity this entity is riding.
     */
    IEntity getMount();

    /**
     * Sets the entity to be mounted by this entity.
     *
     * @param entity the mount.
     */
    void setMount(IEntity entity);

    /**
     * Returns the EntityType as defined in the scripting constants.
     *
     * @return the EntityType.
     */
    int getType();

    /**
     * Checks whether the entity is of the specified EntityType.
     *
     * @param type the type to check.
     * @return true if the entity is of that type; false otherwise.
     */
    boolean typeOf(int type);

    /**
     * Sets the entity's rotation (yaw) in degrees (0-360).
     *
     * @param rotation the rotation angle.
     */
    void setRotation(float rotation);

    /**
     * Sets both the rotation (yaw) and pitch of the entity.
     *
     * @param rotationYaw   the yaw angle.
     * @param rotationPitch the pitch angle.
     */
    void setRotation(float rotationYaw, float rotationPitch);

    /**
     * @return The current rotation (yaw) of the entity.
     */
    float getRotation();

    /**
     * Sets the entity's pitch.
     *
     * @param pitch the pitch angle.
     */
    void setPitch(float pitch);

    /**
     * Returns the entity's pitch.
     *
     * @return the pitch angle.
     */
    float getPitch();

    /**
     * Applies a knockback effect to the entity.
     *
     * @param power     the strength of the knockback.
     * @param direction the direction in degrees (usually based on getRotation()).
     */
    void knockback(int power, float direction);

    /**
     * Applies knockback using individual power components.
     *
     * @param xpower    the x-axis power.
     * @param ypower    the y-axis power.
     * @param zpower    the z-axis power.
     * @param direction the direction in degrees.
     */
    void knockback(double xpower, double ypower, double zpower, float direction);

    /**
     * Applies knockback using a position vector.
     *
     * @param pos       the position vector.
     * @param direction the direction in degrees.
     */
    void knockback(IPos pos, float direction);

    /**
     * Sets the entity's immunity time (hurt resistance).
     *
     * @param ticks the number of ticks.
     */
    void setImmune(int ticks);

    /**
     * Sets the entity's invisibility.
     *
     * @param invisible true to make invisible.
     */
    void setInvisible(boolean invisible);

    /**
     * Sets whether the entity is sneaking.
     *
     * @param sneaking true to enable sneaking.
     */
    void setSneaking(boolean sneaking);

    /**
     * Sets whether the entity is sprinting.
     *
     * @param sprinting true to enable sprinting.
     */
    void setSprinting(boolean sprinting);

    /**
     * @return Whether the entity has collided with something.
     */
    boolean hasCollided();

    /**
     * @return Whether the entity has collided vertically.
     */
    boolean hasCollidedVertically();

    /**
     * @return Whether the entity has collided horizontally.
     */
    boolean hasCollidedHorizontally();

    /**
     * @return Whether the entity is capturing drops.
     */
    boolean capturesDrops();

    /**
     * Sets whether the entity captures drops.
     *
     * @param capture true to capture drops.
     */
    void setCapturesDrops(boolean capture);

    /**
     * Sets the captured drops for this entity.
     *
     * @param capturedDrops an array of captured drops.
     */
    void setCapturedDrops(IEntity[] capturedDrops);

    /**
     * Returns the captured drops.
     *
     * @return an array of captured drops.
     */
    IEntity[] getCapturedDrops();

    /**
     * @return Whether the entity is sneaking.
     * @since 1.7.10c
     */
    boolean isSneaking();

    /**
     * @return Whether the entity is sprinting.
     * @since 1.7.10c
     */
    boolean isSprinting();

    /**
     * Expert users only.
     *
     * @return The underlying Minecraft entity.
     */
    Object getMCEntity();

    /**
     * Returns the entity's NBT data.
     *
     * @return the NBT data.
     */
    INbt getNbt();

    /**
     * Returns all NBT data for the entity.
     *
     * @return the complete NBT data.
     */
    INbt getAllNbt();

    /**
     * Applies the given NBT data to the entity.
     *
     * @param nbt the NBT data.
     */
    void setNbt(INbt nbt);

    /**
     * Returns optional NBT data if available.
     *
     * @return the optional NBT data, or null if none.
     */
    INbt getNbtOptional();

    /**
     * Stores the entity as a clone with the given tab and name.
     *
     * @param tab  the tab index.
     * @param name the name for the clone.
     */
    void storeAsClone(int tab, String name);

    /**
     * @return The world this entity exists in.
     */
    IWorld getWorld();

    /**
     * Updates the entity's state.
     */
    void updateEntity();
}
