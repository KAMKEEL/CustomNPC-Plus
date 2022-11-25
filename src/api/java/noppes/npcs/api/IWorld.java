package noppes.npcs.api;

import net.minecraft.world.WorldServer;
import noppes.npcs.api.scoreboard.IScoreboard;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;

public interface IWorld {
    /**
     * @return The worlds time
     */
    long getTime();

    /**
     * @return The total world time
     */
    long getTotalTime();

    boolean areAllPlayersAsleep();

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @return The block at the given position. Returns null if there isn't a block
     */
    public IBlock getBlock(int x, int y, int z);

    /**
	 * @param pos
	 * @return The block at the given position. Returns null if there isn't a block
	 */
    public IBlock getBlock(IPos pos);

    /**
     *
     * @return The top-most block in the world as an IBlock object.
     */
    IBlock getTopBlock(int x, int z);
    IBlock getTopBlock(IPos pos);

    boolean isBlockFreezable(IPos pos);
    boolean isBlockFreezable(int x, int y, int z);

    boolean isBlockFreezableNaturally(IPos pos);
    boolean isBlockFreezableNaturally(int x, int y, int z);

    boolean canBlockFreeze(IPos pos, boolean adjacentToWater);
    boolean canBlockFreeze(int x, int y, int z, boolean adjacentToWater);

    boolean canBlockFreezeBody(IPos pos, boolean adjacentToWater);
    boolean canBlockFreezeBody(int x, int y, int z, boolean adjacentToWater);

    boolean canSnowAt(IPos pos, boolean checkLight);
    boolean canSnowAt(int x, int y, int z, boolean checkLight);

    boolean canSnowAtBody(IPos pos, boolean checkLight);
    boolean canSnowAtBody(int x, int y, int z, boolean checkLight);

    /**
     * @return The Y-value of the world at this x & z value based on the height map of the world.
     */
    int getHeightValue(int x, int z);
    int getHeightValue(IPos pos);

    /**
     * @return The minimum Y-value of the world at this x & z value based on the height map of the world.
     */
    int getChunkHeightMapMinimum(int x, int z);
    int getChunkHeightMapMinimum(IPos pos);

    /**
     * @return The metadata of the block at this position.
     */
    int getBlockMetadata(int x, int y, int z);
    int getBlockMetadata(IPos pos);

    boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag);
    boolean setBlockMetadataWithNotify(IPos pos, int metadata, int flag);

    /**
     * @param x
     * @param y
     * @param z
     * @return can the block at this position see the sky or are there no blocks above this one
     */
    boolean canSeeSky(int x, int y, int z);
    /**
     * @param pos
     * @return can the block at this position see the sky or are there no blocks above this one
     */
    boolean canSeeSky(IPos pos);

    int getFullBlockLightValue(int x, int y, int z);
    int getFullBlockLightValue(IPos pos);

    int getBlockLightValue(int x, int y, int z);
    int getBlockLightValue(IPos pos);

    void playSoundAtEntity(IEntity entity, String sound, float volume, float pitch);

    void playSoundToNearExcept(IPlayer player, String sound, float volume, float pitch);

    void playSound(int id, ISound sound);

    void stopSound(int id);

    void pauseSounds();

    void continueSounds();

    void stopSounds();

    IEntity getEntityByID(int id);

    boolean spawnEntityInWorld(IEntity entity);

    IPlayer getClosestPlayerToEntity(IEntity entity, double range);

    IPlayer getClosestPlayer(double x, double y, double z, double range);
    IPlayer getClosestPlayer(IPos pos, double range);

    IPlayer getClosestVulnerablePlayerToEntity(IEntity entity, double range);

    IPlayer getClosestVulnerablePlayer(double x, double y, double z, double range);
    IPlayer getClosestVulnerablePlayer(IPos pos, double range);

    /**
     *
     * @param entity The entity whose type will be used as a parameter
     * @return The amount of entities of the given type in the world.
     */
    int countEntities(IEntity entity);

    IEntity[] getLoadedEntities();

    IEntity[] getEntitiesNear(IPos position, double range);

    IEntity[] getEntitiesNear(double x, double y, double z, double range);

    /**
     * Sets the block's tile entity at the given position.
     */
    void setTileEntity(int x, int y, int z, ITileEntity tileEntity);
    void setTileEntity(IPos pos, ITileEntity tileEntity);

    /**
     * Removes the block's tile entity at the given position.
     */
    void removeTileEntity(int x, int y, int z);
    void removeTileEntity(IPos pos);

    /**
     *
     * @return True if the block at this position is of cubic shape. (Not a stair, slab, etc.)
     */
    boolean isBlockFullCube(int x, int y, int z);
    boolean isBlockFullCube(IPos pos);

    long getSeed();

    void setSpawnLocation(int x, int y, int z);
    void setSpawnLocation(IPos pos);

    boolean canLightningStrikeAt(int x, int y, int z);
    boolean canLightningStrikeAt(IPos pos);

    boolean isBlockHighHumidity(int x, int y, int z);
    boolean isBlockHighHumidity(IPos pos);

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @return Text from signs
     * @since 1.7.10d
     */
    String getSignText(int x, int y, int z);
    String getSignText(IPos pos);

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @param item The block to be set
     */
    boolean setBlock(int x, int y, int z, IItemStack item);
    boolean setBlock(IPos pos, IItemStack item);

	/**
	 * @param x World position x
	 * @param y World position y
	 * @param z World position z
	 * @param block The block to be set
	 */
    boolean setBlock(int x, int y, int z, IBlock block);
    boolean setBlock(IPos pos, IBlock block);

    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     */
    void removeBlock(int x, int y, int z);
    void removeBlock(IPos pos);

    IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance);

    IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance);

    /**
     * starting at the start position, draw a line in the lookVector direction until a block is detected
     * @param startPos
     * @param lookVector should be a normalized direction vector
     * @param maxDistance
     * @return the first detected block but null if maxDistance is reached
     */
    IBlock rayCastBlock(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IBlock rayCastBlock(double[] startPos, double[] lookVector, int maxDistance);

    IBlock rayCastBlock(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IBlock rayCastBlock(IPos startPos, IPos lookVector, int maxDistance);

    /**
     * @param startPos
     * @param maxHeight
     * @return the position of the closest block of air to startPos
     */
    public IPos getNearestAir(IPos startPos, int maxHeight);

    IEntity[] rayCastEntities(double[] startPos, double[] lookVector, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IEntity[] rayCastEntities(IPos startPos, IPos lookVector, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IEntity[] rayCastEntities(double[] startPos, double[] lookVector, int maxDistance, double offset, double range);

    IEntity[] rayCastEntities(IPos startPos, IPos lookVector, int maxDistance, double offset, double range);

    /**
     * @param name The name of the player to be returned
     * @return The Player with name. Null is returned when the player isnt found
     */
    IPlayer getPlayer(String name);

    IPlayer getPlayerByUUID(String uuid);

    /**
     * @param time The world time to be set
     */
    void setTime(long time);

    /**
     * @return Whether or not its daytime
     */
    boolean isDay();

    /**
     * @return Whether or not its currently raining
     */
    boolean isRaining();

    /**
     * @param bo Set if it's raining
     */
    void setRaining(boolean bo);

    /**
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    void thunderStrike(double x, double y, double z);
    void thunderStrike(IPos pos);

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
    void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count);
    void spawnParticle(String particle, IPos pos, double dx, double dy, double dz, double speed, int count);

    /**
     * @param id The items name
     * @param damage The damage value
     * @param size The number of items in the item
     * @return Returns the item
     */
    IItemStack createItem(String id, int damage, int size);

    /**
     * @param directory The particle's texture directory. Use only forward slashes when writing a directory. Example: "customnpcs:textures/particle/tail.png"
     * @return Returns IEntityParticle object
     */
    @Deprecated
    IParticle createEntityParticle(String directory);

    /**
     * @param key Get temp data for this key
     * @return Returns the stored temp data
     */
    Object getTempData(String key);

    /**
     * Tempdata gets cleared when the server restarts. All worlds share the same temp data.
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
     * Removes all tempdata
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
     * @param x Position x
     * @param y Position y
     * @param z Position z
     * @param range Range of the explosion
     * @param fire Whether or not the explosion does fire damage
     * @param grief Whether or not the explosion does damage to blocks
     */
    void explode(double x, double y, double z, float range, boolean fire, boolean grief);
    void explode(IPos pos, float range, boolean fire, boolean grief);

    IPlayer[] getAllServerPlayers();

    public String[] getPlayerNames();

    /**
     * @since 1.7.10c
     * @param x Position x
     * @param z Position z
     * @return Returns the name of the biome
     */
    String getBiomeName(int x, int z);
    String getBiomeName(IPos pos);

    /**
     * Lets you spawn a server side cloned entity
     * @param x The x position the clone will be spawned at
     * @param y The y position the clone will be spawned at
     * @param z The z position the clone will be spawned at
     * @param tab The tab in which the clone is
     * @param name Name of the cloned entity
     * @param ignoreProtection Whether the spawning of this clone skips protection checks.
     * @return Returns the entity which was spawned
     */
    IEntity spawnClone(int x, int y, int z, int tab, String name, boolean ignoreProtection);
    IEntity spawnClone(IPos pos, int tab, String name, boolean ignoreProtection);

    IEntity spawnClone(int x, int y, int z, int tab, String name);
    IEntity spawnClone(IPos pos, int tab, String name);

    IScoreboard getScoreboard();

    /**
     * @since 1.7.10c
     * Expert use only
     * @return Returns minecraft world object
     */
    WorldServer getMCWorld();

    /**
     *
     * @return The ID of this world's dimension. 0 for overworld, 1 for End, -1 for Nether, etc.
     */
    int getDimensionID();
}
