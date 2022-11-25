//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.entity;

import net.minecraft.entity.Entity;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IParticle;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.item.IItemStack;

public interface IEntity<T extends Entity> {
    
    void spawnParticle(IParticle entityParticle);

    int getEntityId();

    String getUniqueID();
    
    double getYOffset();

    /**
     * @return The entities width
     */
    double getWidth();

    /**
     * @return The entities height
     */
    double getHeight();

    /**
     * @return The entities x position
     */
    double getX();

    /**
     * @param x The entities x position
     */
    void setX(double x);

    /**
     * @return The entities y position
     */
    double getY();

    /**
     * @param y The entities y position
     */
    void setY(double y);

    /**
     * @return The entities x position
     */
    double getZ();

    /**
     * @param z The entities x position
     */
    void setZ(double z);

    /**
     * @return The entities z motion
     */
    double getMotionX();

    /**
     * @param x The entities x motion
     */
    void setMotionX(double x);

    /**
     * @return The entities x motion
     */
    double getMotionY();

    /**
     * @param y The entities y motion
     */
    void setMotionY(double y);

    /**
     * @return The entities y motion
     */
    double getMotionZ();

    /**
     * @param z The entities z motion
     */
    void setMotionZ(double z);

    void setMotion(double x, double y, double z);

    void setMotion(IPos pos);

    IPos getMotion();

    boolean isAirborne();
    
    /**
     * @return The block x position
     */
    int getBlockX();

    /**
     * @return The block y position
     */
    int getBlockY();

    /**
     * @return The block z position
     */
    int getBlockZ();

    /**
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    void setPosition(double x, double y, double z);

    void setPosition(IPos pos);

    IPos getPosition();

    int getDimension();

    /**
     * @param range The search range for entities around this entity
     * @return Array of entities within range
     */
    IEntity[] getSurroundingEntities(int range);

    /**
     * @param range The search range for entities around this entity
     * @param type The EntityType you want to find
     * @return Array of entities within range
     */
    IEntity[] getSurroundingEntities(int range, int type);

    /**
     * @return Whether the entity is alive or not
     */
    boolean isAlive();

    /**
     * @param key Get temp data for this key
     * @return Returns the stored temp data
     */
    Object getTempData(String key);

    /**
     * Tempdata gets cleared when the entity gets unloaded or the world restarts
     * @param key The key for the data stored
     * @param value The data stored
     */
    void setTempData(String key, Object value);

    /**
     * @param key The key thats going to be tested against the temp data
     * @return Whether or not temp data containes the key
     */
    boolean hasTempData(String key);

    /**
     * @param key The key for the temp data to be removed
     */
    void removeTempData(String key);

    /**
     * Remove all tempdata
     */
    void clearTempData();

    /**
     * @param key The key of the data to be returned
     * @return Returns the stored data
     */
    Object getStoredData(String key);

    /**
     * Stored data persists through world restart. Unlike tempdata only Strings and Numbers can be saved
     * @param key The key for the data stored
     * @param value The data stored. This data can be either a Number or a String. Other data is not stored
     */
    void setStoredData(String key, Object value);

    /**
     * @param key The key of the data to be checked
     * @return Returns whether or not the stored data contains the key
     */
    boolean hasStoredData(String key);

    /**
     * @param key The key of the data to be removed
     */
    void removeStoredData(String key);

    /**
     * Remove all stored data
     */
    void clearStoredData();

    /**
     * @return The age of this entity in ticks
     */
    long getAge();

    /**
     * Despawns this entity. Removes it permanently
     */
    void despawn();

    /**
     * @return Return whether or not this entity is standing in water
     */
    boolean inWater();

    /**
     * @return Return whether or not this entity is standing in lava
     */
    boolean inLava();

    /**
     * @return Return whether or not this entity is standing in fire
     */
    boolean inFire();

    /**
     * @return Return whether or not this entity is on fire
     */
    boolean isBurning();

    /**
     * @param ticks Amount of world ticks this entity will burn. 20 ticks equals 1 second
     */
    void setBurning(int ticks);

    /**
     * Removes fire from this entity
     */
    void extinguish();

    /**
     * @return Name as which it's registered in minecraft
     */
    String getTypeName();

    /**
     * @param item Item to be dropped
     */
    void dropItem(IItemStack item);

    /**
     * @return Return the rider
     */
    IEntity getRider();

    /**
     * @param entity The entity to ride this entity
     */
    void setRider(IEntity entity);

    /**
     * @return Return the entity, this entity is riding
     */
    IEntity getMount();

    /**
     * @param entity The entity this entity will mount
     */
    void setMount(IEntity entity);

    /**
     * @see noppes.npcs.scripted.constants.EntityType
     * @return Returns the EntityType of this entity
     */
    int getType();

    /**
     * @since 1.7.10c
     * @param type @EntityType to check
     * @return Returns whether the entity is type of the given @EntityType
     */
    boolean typeOf(int type);

    /**
     * @param rotation The rotation to be set (0-360)
     */
    void setRotation(float rotation);

    /**
     * @return Current rotation of the npc
     */
    float getRotation();

    public void setPitch(float pitch);

    public float getPitch();

    /**
     * @param power How strong the knockback is
     * @param direction The direction in which he flies back (0-360). Usually based on getRotation()
     */
    void knockback(int power, float direction);

    void knockback(int xpower, int ypower, int zpower, float direction);
    void knockback(IPos pos, float direction);

    void setImmune(int ticks);

    public void setInvisible(boolean invisible);

    public void setSneaking(boolean sneaking);

    public void setSprinting(boolean sprinting);

    boolean hasCollided();

    /**
     * @since 1.7.10c
     * @return Returns whether or not this entity is sneaking
     */
    boolean isSneaking();

    /**
     * @since 1.7.10c
     * @return Returns whether or not this entity is sprinting
     */
    boolean isSprinting();

    /**
     * @since 1.7.10c
     * Expert users only
     * @return Returns minecrafts entity
     */
    T getMCEntity();

    public INbt getNbt();

    public INbt getAllNbt();

    public void setNbt(INbt nbt);

    INbt getNbtOptional();

    void storeAsClone(int tab, String name);

    IWorld getWorld();
}
