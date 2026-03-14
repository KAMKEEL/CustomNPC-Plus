package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
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
     * @param x X coordinate
     * @param z Z coordinate
     * @return The Y-value of the world at this x &amp; z value based on the height map of the world.
     */
    int getHeightValue(int x, int z);

    int getHeightValue(IPos pos);

    /**
     * @param x X coordinate
     * @param z Z coordinate
     * @return The minimum Y-value of the world at this x &amp; z value based on the height map of the world.
     */
    int getChunkHeightMapMinimum(int x, int z);

    int getChunkHeightMapMinimum(IPos pos);

    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return The metadata of the block at this position.
     */
    int getBlockMetadata(int x, int y, int z);

    int getBlockMetadata(IPos pos);

    boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag);

    boolean setBlockMetadataWithNotify(IPos pos, int metadata, int flag);

    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return can the block at this position see the sky or are there no blocks above this one
     */
    boolean canSeeSky(int x, int y, int z);

    /**
     * @param pos the block position
     * @return can the block at this position see the sky or are there no blocks above this one
     */
    boolean canSeeSky(IPos pos);

    int getFullBlockLightValue(int x, int y, int z);

    int getFullBlockLightValue(IPos pos);

    int getBlockLightValue(int x, int y, int z);

    int getBlockLightValue(IPos pos);

    void playSoundAtEntity(IEntity entity, String sound, float volume, float pitch);

    void playSoundToNearExcept(IPlayer player, String sound, float volume, float pitch);

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
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
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

    boolean isPlaceCancelled(int posX, int posY, int posZ);

    boolean isPlaceCancelled(IPos pos);

    boolean isBreakCancelled(int posX, int posY, int posZ);

    boolean isBreakCancelled(IPos pos);

    IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance);

    IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance);

    /**
     * @param startPos the start position as [x, y, z]
     * @param maxHeight maximum search height
     * @return the position of the closest block of air to startPos
     */
    IPos getNearestAir(IPos startPos, int maxHeight);

    IEntity[] rayCastEntities(double[] startPos, double[] lookVector, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IEntity[] rayCastEntities(IEntity[] ignoreEntities, double[] startPos, double[] lookVector, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

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
     * @param id     The items name
     * @param damage The damage value
     * @param size   The number of items in the item
     * @return Returns the item
     */
    IItemStack createItem(String id, int damage, int size);

    @Deprecated
    Object getTempData(String key);

    @Deprecated
    void setTempData(String key, Object value);

    @Deprecated
    boolean hasTempData(String key);

    @Deprecated
    void removeTempData(String key);

    @Deprecated
    void clearTempData();

    @Deprecated
    String[] getTempDataKeys();

    @Deprecated
    Object getStoredData(String key);

    @Deprecated
    void setStoredData(String key, Object value);

    @Deprecated
    boolean hasStoredData(String key);

    @Deprecated
    void removeStoredData(String key);

    @Deprecated
    void clearStoredData();

    String[] getStoredDataKeys();

    /**
     * @param x     Position x
     * @param y     Position y
     * @param z     Position z
     * @param range Range of the explosion
     * @param fire  Whether or not the explosion does fire damage
     * @param grief Whether or not the explosion does damage to blocks
     */
    void explode(double x, double y, double z, float range, boolean fire, boolean grief);

    void explode(IPos pos, float range, boolean fire, boolean grief);

    IPlayer[] getAllServerPlayers();

    String[] getPlayerNames();

    /**
     * @param x Position x
     * @param z Position z
     * @return Returns the name of the biome
     * @since 1.7.10c
     */
    String getBiomeName(int x, int z);

    String getBiomeName(IPos pos);

    /**
     * Lets you spawn a server side cloned entity
     *
     * @param x                The x position the clone will be spawned at
     * @param y                The y position the clone will be spawned at
     * @param z                The z position the clone will be spawned at
     * @param tab              The tab in which the clone is
     * @param name             Name of the cloned entity
     * @param ignoreProtection Whether the spawning of this clone skips protection checks.
     * @return Returns the entity which was spawned
     */
    IEntity spawnClone(int x, int y, int z, int tab, String name, boolean ignoreProtection);

    IEntity spawnClone(IPos pos, int tab, String name, boolean ignoreProtection);

    IEntity spawnClone(int x, int y, int z, int tab, String name);

    IEntity spawnClone(IPos pos, int tab, String name);

    /**
     * @return Returns minecraft world object
     * @since 1.7.10c
     * Expert use only
     */
    Object getMCWorld();

    /**
     *
     * @return The ID of this world's dimension. 0 for overworld, 1 for End, -1 for Nether, etc.
     */
    int getDimensionID();

    void broadcast(String message);
}
