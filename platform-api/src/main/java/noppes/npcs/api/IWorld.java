package noppes.npcs.api;

import noppes.npcs.api.entity.*;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.scoreboard.IScoreboard;

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
     * @param pos the block position
     * @return The block at the given position. Returns null if there isn't a block
     */
    public IBlock getBlock(IPos pos);

    /**
     * @param x X coordinate
     * @param z Z coordinate
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
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param tileEntity the tile entity to place
     */
    void setTileEntity(int x, int y, int z, ITileEntity tileEntity);

    void setTileEntity(IPos pos, ITileEntity tileEntity);

    /**
     * Removes the block's tile entity at the given position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    void removeTileEntity(int x, int y, int z);

    void removeTileEntity(IPos pos);

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

    /**
     * @param x    World position x
     * @param y    World position y
     * @param z    World position z
     * @param item The block to be set
     * @return true if the block was successfully placed
     */
    boolean setBlock(int x, int y, int z, IItemStack item);

    boolean setBlock(IPos pos, IItemStack item);

    /**
     * @param x     World position x
     * @param y     World position y
     * @param z     World position z
     * @param block The block to be set
     * @return true if the block was successfully placed
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

    boolean isPlaceCancelled(int posX, int posY, int posZ);

    boolean isPlaceCancelled(IPos pos);

    boolean isBreakCancelled(int posX, int posY, int posZ);

    boolean isBreakCancelled(IPos pos);

    IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance);

    IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance);

    /**
     * starting at the start position, draw a line in the lookVector direction until a block is detected
     *
     * @param startPos the ray origin as [x, y, z]
     * @param lookVector  should be a normalized direction vector
     * @param maxDistance maximum ray distance in blocks
     * @param stopOnBlock whether to stop on solid blocks
     * @param stopOnLiquid whether to stop on liquid blocks
     * @param stopOnCollision whether to stop on collision boundaries
     * @return the first detected block but null if maxDistance is reached
     */
    IBlock rayCastBlock(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IBlock rayCastBlock(double[] startPos, double[] lookVector, int maxDistance);

    IBlock rayCastBlock(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IBlock rayCastBlock(IPos startPos, IPos lookVector, int maxDistance);

    /**
     * @param startPos the start position as [x, y, z]
     * @param maxHeight maximum search height
     * @return the position of the closest block of air to startPos
     */
    public IPos getNearestAir(IPos startPos, int maxHeight);

    IEntity[] rayCastEntities(double[] startPos, double[] lookVector, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    public IEntity[] rayCastEntities(IEntity[] ignoreEntities, double[] startPos, double[] lookVector, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

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
     *
     * @param particle Particle name. Particle name list: http://minecraft.wiki/w/Particles
     * @param x        The x position
     * @param y        The y position
     * @param z        The z position
     * @param dx       Usually used for the x motion
     * @param dy       Usually used for the y motion
     * @param dz       Usually used for the z motion
     * @param speed    Speed of the particles, usually between 0 and 1
     * @param count    Particle count
     */
    void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count);

    void spawnParticle(String particle, IPos pos, double dx, double dy, double dz, double speed, int count);

    /**
     * @param id     The items name
     * @param damage The damage value
     * @param size   The number of items in the item
     * @return Returns the item
     */
    IItemStack createItem(String id, int damage, int size);

    /**
     * @param directory The particle's texture directory. Use only forward slashes when writing a directory. Example: "customnpcs:textures/particle/tail.png"
     * @return Returns IEntityParticle object
     */
    @Deprecated
    IParticle createEntityParticle(String directory);

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

    public String[] getPlayerNames();

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

    IScoreboard getScoreboard();

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

    /**
     * Creates an energy orb projectile. Not spawned until fire() is called.
     *
     * @param owner The entity that owns this projectile
     * @param x     Spawn X position
     * @param y     Spawn Y position
     * @param z     Spawn Z position
     * @param size  Orb size
     * @return the energy orb entity
     */
    IEnergyOrb createEnergyOrb(IEntity owner, double x, double y, double z, float size);

    /**
     * Creates an energy beam projectile. Not spawned until fire() is called.
     *
     * @param owner     The entity that owns this projectile
     * @param x         Spawn X position
     * @param y         Spawn Y position
     * @param z         Spawn Z position
     * @param beamWidth Width of the beam
     * @param headSize  Size of the beam head
     * @return the energy beam entity
     */
    IEnergyBeam createEnergyBeam(IEntity owner, double x, double y, double z, float beamWidth, float headSize);

    /**
     * Creates an energy disc projectile. Not spawned until fire() is called.
     *
     * @param owner     The entity that owns this projectile
     * @param x         Spawn X position
     * @param y         Spawn Y position
     * @param z         Spawn Z position
     * @param radius    Disc radius
     * @param thickness Disc thickness
     * @return the energy disc entity
     */
    IEnergyDisc createEnergyDisc(IEntity owner, double x, double y, double z, float radius, float thickness);

    /**
     * Creates an energy laser projectile. Not spawned until fire() is called.
     *
     * @param owner      The entity that owns this projectile
     * @param x          Spawn X position
     * @param y          Spawn Y position
     * @param z          Spawn Z position
     * @param laserWidth Width of the laser
     * @return the energy laser entity
     */
    IEnergyLaser createEnergyLaser(IEntity owner, double x, double y, double z, float laserWidth);

    void broadcast(String message);
}
