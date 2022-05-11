//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.entity.Entity;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.item.ScriptItemStack;

public interface IEntity<T extends Entity> {
    public double getYOffset();

    /**
     * @return The entities width
     */
    public double getWidth();

    /**
     * @return The entities height
     */
    public double getHeight();

    /**
     * @return The entities x position
     */
    public double getX();

    /**
     * @param x The entities x position
     */
    public void setX(double x);

    /**
     * @return The entities y position
     */
    public double getY();

    /**
     * @param y The entities y position
     */
    public void setY(double y);

    /**
     * @return The entities x position
     */
    public double getZ();

    /**
     * @param z The entities x position
     */
    public void setZ(double z);

    /**
     * @return The block x position
     */
    public int getBlockX();

    /**
     * @return The block y position
     */
    public int getBlockY();

    /**
     * @return The block z position
     */
    public int getBlockZ();

    /**
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setPosition(double x, double y, double z);


    /**
     * @param range The search range for entities around this entity
     * @return Array of entities within range
     */
    public ScriptEntity[] getSurroundingEntities(int range);

    /**
     * @param range The search range for entities around this entity
     * @param type The EntityType you want to find
     * @return Array of entities within range
     */
    public ScriptEntity[] getSurroundingEntities(int range, int type);

    /**
     * @return Whether the entity is alive or not
     */
    public boolean isAlive();

    /**
     * @param key Get temp data for this key
     * @return Returns the stored temp data
     */
    public Object getTempData(String key);

    /**
     * Tempdata gets cleared when the entity gets unloaded or the world restarts
     * @param key The key for the data stored
     * @param value The data stored
     */
    public void setTempData(String key, Object value);

    /**
     * @param key The key thats going to be tested against the temp data
     * @return Whether or not temp data containes the key
     */
    public boolean hasTempData(String key);

    /**
     * @param key The key for the temp data to be removed
     */
    public void removeTempData(String key);

    /**
     * Remove all tempdata
     */
    public void clearTempData();

    /**
     * @param key The key of the data to be returned
     * @return Returns the stored data
     */
    public Object getStoredData(String key);

    /**
     * Stored data persists through world restart. Unlike tempdata only Strings and Numbers can be saved
     * @param key The key for the data stored
     * @param value The data stored. This data can be either a Number or a String. Other data is not stored
     */
    public void setStoredData(String key, Object value);

    /**
     * @param key The key of the data to be checked
     * @return Returns whether or not the stored data contains the key
     */
    public boolean hasStoredData(String key);

    /**
     * @param key The key of the data to be removed
     */
    public void removeStoredData(String key);

    /**
     * Remove all stored data
     */
    public void clearStoredData();

    /**
     * @return The age of this entity in ticks
     */
    public long getAge();

    /**
     * Despawns this entity. Removes it permanently
     */
    public void despawn();

    /**
     * @return Return whether or not this entity is standing in water
     */
    public boolean inWater();

    /**
     * @return Return whether or not this entity is standing in lava
     */
    public boolean inLava();

    /**
     * @return Return whether or not this entity is standing in fire
     */
    public boolean inFire();

    /**
     * @return Return whether or not this entity is on fire
     */
    public boolean isBurning();

    /**
     * @param ticks Amount of world ticks this entity will burn. 20 ticks equals 1 second
     */
    public void setBurning(int ticks);

    /**
     * Removes fire from this entity
     */
    public void extinguish();

    /**
     * @return Name as which it's registered in minecraft
     */
    public String getTypeName();

    /**
     * @param item Item to be dropped
     */
    public void dropItem(ScriptItemStack item);

    /**
     * @return Return the rider
     */
    public ScriptEntity getRider();

    /**
     * @param entity The entity to ride this entity
     */
    public void setRider(ScriptEntity entity);

    /**
     * @return Return the entity, this entity is riding
     */
    public ScriptEntity getMount();

    /**
     * @param entity The entity this entity will mount
     */
    public void setMount(ScriptEntity entity);

    /**
     * @see noppes.npcs.scripted.constants.EntityType
     * @return Returns the EntityType of this entity
     */
    public int getType();

    /**
     * @since 1.7.10c
     * @param type @EntityType to check
     * @return Returns whether the entity is type of the given @EntityType
     */
    public boolean typeOf(int type);

    /**
     * @param rotation The rotation to be set (0-360)
     */
    public void setRotation(float rotation);

    /**
     * @return Current rotation of the npc
     */
    public float getRotation();

    /**
     * @param power How strong the knockback is
     * @param direction The direction in which he flies back (0-360). Usually based on getRotation()
     */
    public void knockback(int power, float direction);

    public void knockback(int xpower, int ypower, int zpower, float direction);

    public void setImmune(int ticks);

    public boolean hasCollided();

    /**
     * @since 1.7.10c
     * @return Returns whether or not this entity is sneaking
     */
    public boolean isSneaking();

    /**
     * @since 1.7.10c
     * @return Returns whether or not this entity is sprinting
     */
    public boolean isSprinting();

    /**
     * @since 1.7.10c
     * Expert users only
     * @return Returns minecrafts entity
     */
    public T getMCEntity();

    void storeAsClone(int tab, String name);
}
