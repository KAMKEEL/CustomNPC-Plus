package noppes.npcs.scripted.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.scripted.*;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.entity.ScriptPlayer;

public interface IWorld {
    /**
     * @return The worlds time
     */
    public long getTime();

    /**
     * @return The total world time
     */
    public long getTotalTime();

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @return The block at the given position. Returns null if there isn't a block
     */
    public IBlock getBlock(int x, int y, int z);

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @return Text from signs
     * @since 1.7.10d
     */
    public String getSignText(int x, int y, int z);

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @param item The block to be set
     */
    public void setBlock(int x, int y, int z, ScriptItemStack item);

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     */
    public void removeBlock(int x, int y, int z);

    /**
     * @param name The name of the player to be returned
     * @return The Player with name. Null is returned when the player isnt found
     */
    public ScriptPlayer getPlayer(String name);

    /**
     * @param time The world time to be set
     */
    public void setTime(long time);

    /**
     * @return Whether or not its daytime
     */
    public boolean isDay();

    /**
     * @return Whether or not its currently raining
     */
    public boolean isRaining();

    /**
     * @param bo Set if it's raining
     */
    public void setRaining(boolean bo);

    /**
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void thunderStrike(double x, double y, double z);

    /**
     * Sends a packet from the server to the client everytime its called. Probably should not use this too much.
     * @param particle Particle name. Particle name list: http://minecraft.gamepedia.com/Particles
     * @param x The x position
     * @param y The y position
     * @param z The z position
     * @param dx Usually used for the x motion
     * @param dy Usually used for the y motion
     * @param dz Usually used for the z motion
     * @param speed Speed of the particles, usually between 0 and 1
     * @param count Particle count
     */
    public void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count);

    /**
     * @param id The items name
     * @param damage The damage value
     * @param size The number of items in the item
     * @return Returns the item
     */
    public ScriptItemStack createItem(String id, int damage, int size);

    /**
     * @param directory The particle's texture directory. Use only forward slashes when writing a directory. Example: "customnpcs:textures/particle/tail.png"
     * @return Returns ScriptEntityParticle object
     */
    public ScriptEntityParticle createEntityParticle(String directory);

    /**
     * @param key Get temp data for this key
     * @return Returns the stored temp data
     */
    public Object getTempData(String key);

    /**
     * Tempdata gets cleared when the server restarts. All worlds share the same temp data.
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
     * Removes all tempdata
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
     * @param x Position x
     * @param y Position y
     * @param z Position z
     * @param range Range of the explosion
     * @param fire Whether or not the explosion does fire damage
     * @param grief Whether or not the explosion does damage to blocks
     */
    public void explode(double x, double y, double z, float range, boolean fire, boolean grief);

    public ScriptPlayer[] getAllServerPlayers();

    /**
     * @since 1.7.10c
     * @param x Position x
     * @param z Position z
     * @return Returns the name of the biome
     */
    public String getBiomeName(int x, int z);

    /**
     * Lets you spawn a server side cloned entity
     * @param x The x position the clone will be spawned at
     * @param y The y position the clone will be spawned at
     * @param z The z position the clone will be spawned at
     * @param tab The tab in which the clone is
     * @param name Name of the cloned entity
     * @return Returns the entity which was spawned
     */
    public ScriptEntity spawnClone(int x, int y, int z, int tab, String name);

    public ScriptScoreboard getScoreboard();

    BlockPos getMCBlockPos(int x, int y, int z);

    /**
     * @since 1.7.10c
     * Expert use only
     * @return Returns minecraft world object
     */
    public WorldServer getMCWorld();

    public int getDimensionID();
}
