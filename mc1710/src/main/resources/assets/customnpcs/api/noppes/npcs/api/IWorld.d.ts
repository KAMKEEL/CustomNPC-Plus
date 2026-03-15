/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.IWorld
 */
export interface IWorld {
    /**
     * @return The worlds time
     */
    getTime(): import('./long').long;
    /**
     * @return The total world time
     */
    getTotalTime(): import('./long').long;
    areAllPlayersAsleep(): import('./boolean').boolean;
    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @return The block at the given position. Returns null if there isn't a block
     */
    getBlock(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./IBlock').IBlock;
    /**
     * @param pos the block position
     * @return The block at the given position. Returns null if there isn't a block
     */
    getBlock(pos: import('./IPos').IPos): import('./IBlock').IBlock;
    /**
     * @param x X coordinate
     * @param z Z coordinate
     * @return The top-most block in the world as an IBlock object.
     */
    getTopBlock(x: import('./int').int, z: import('./int').int): import('./IBlock').IBlock;
    getTopBlock(pos: import('./IPos').IPos): import('./IBlock').IBlock;
    isBlockFreezable(pos: import('./IPos').IPos): import('./boolean').boolean;
    isBlockFreezable(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    isBlockFreezableNaturally(pos: import('./IPos').IPos): import('./boolean').boolean;
    isBlockFreezableNaturally(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    canBlockFreeze(pos: import('./IPos').IPos, adjacentToWater: import('./boolean').boolean): import('./boolean').boolean;
    canBlockFreeze(x: import('./int').int, y: import('./int').int, z: import('./int').int, adjacentToWater: import('./boolean').boolean): import('./boolean').boolean;
    canBlockFreezeBody(pos: import('./IPos').IPos, adjacentToWater: import('./boolean').boolean): import('./boolean').boolean;
    canBlockFreezeBody(x: import('./int').int, y: import('./int').int, z: import('./int').int, adjacentToWater: import('./boolean').boolean): import('./boolean').boolean;
    canSnowAt(pos: import('./IPos').IPos, checkLight: import('./boolean').boolean): import('./boolean').boolean;
    canSnowAt(x: import('./int').int, y: import('./int').int, z: import('./int').int, checkLight: import('./boolean').boolean): import('./boolean').boolean;
    canSnowAtBody(pos: import('./IPos').IPos, checkLight: import('./boolean').boolean): import('./boolean').boolean;
    canSnowAtBody(x: import('./int').int, y: import('./int').int, z: import('./int').int, checkLight: import('./boolean').boolean): import('./boolean').boolean;
    /**
     * @param x X coordinate
     * @param z Z coordinate
     * @return The Y-value of the world at this x &amp; z value based on the height map of the world.
     */
    getHeightValue(x: import('./int').int, z: import('./int').int): import('./int').int;
    getHeightValue(pos: import('./IPos').IPos): import('./int').int;
    /**
     * @param x X coordinate
     * @param z Z coordinate
     * @return The minimum Y-value of the world at this x &amp; z value based on the height map of the world.
     */
    getChunkHeightMapMinimum(x: import('./int').int, z: import('./int').int): import('./int').int;
    getChunkHeightMapMinimum(pos: import('./IPos').IPos): import('./int').int;
    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return The metadata of the block at this position.
     */
    getBlockMetadata(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./int').int;
    getBlockMetadata(pos: import('./IPos').IPos): import('./int').int;
    setBlockMetadataWithNotify(x: import('./int').int, y: import('./int').int, z: import('./int').int, metadata: import('./int').int, flag: import('./int').int): import('./boolean').boolean;
    setBlockMetadataWithNotify(pos: import('./IPos').IPos, metadata: import('./int').int, flag: import('./int').int): import('./boolean').boolean;
    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return can the block at this position see the sky or are there no blocks above this one
     */
    canSeeSky(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    /**
     * @param pos the block position
     * @return can the block at this position see the sky or are there no blocks above this one
     */
    canSeeSky(pos: import('./IPos').IPos): import('./boolean').boolean;
    getFullBlockLightValue(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./int').int;
    getFullBlockLightValue(pos: import('./IPos').IPos): import('./int').int;
    getBlockLightValue(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./int').int;
    getBlockLightValue(pos: import('./IPos').IPos): import('./int').int;
    playSoundAtEntity(entity: import('./entity/IEntity').IEntity, sound: String, volume: import('./float').float, pitch: import('./float').float): import('./void').void;
    playSoundToNearExcept(player: import('./entity/IPlayer').IPlayer, sound: String, volume: import('./float').float, pitch: import('./float').float): import('./void').void;
    playSound(id: import('./int').int, sound: import('./handler/data/ISound').ISound): import('./void').void;
    stopSound(id: import('./int').int): import('./void').void;
    pauseSounds(): import('./void').void;
    continueSounds(): import('./void').void;
    stopSounds(): import('./void').void;
    getEntityByID(id: import('./int').int): import('./entity/IEntity').IEntity;
    spawnEntityInWorld(entity: import('./entity/IEntity').IEntity): import('./boolean').boolean;
    getClosestPlayerToEntity(entity: import('./entity/IEntity').IEntity, range: import('./double').double): import('./entity/IPlayer').IPlayer;
    getClosestPlayer(x: import('./double').double, y: import('./double').double, z: import('./double').double, range: import('./double').double): import('./entity/IPlayer').IPlayer;
    getClosestPlayer(pos: import('./IPos').IPos, range: import('./double').double): import('./entity/IPlayer').IPlayer;
    getClosestVulnerablePlayerToEntity(entity: import('./entity/IEntity').IEntity, range: import('./double').double): import('./entity/IPlayer').IPlayer;
    getClosestVulnerablePlayer(x: import('./double').double, y: import('./double').double, z: import('./double').double, range: import('./double').double): import('./entity/IPlayer').IPlayer;
    getClosestVulnerablePlayer(pos: import('./IPos').IPos, range: import('./double').double): import('./entity/IPlayer').IPlayer;
    /**
     *
     * @param entity The entity whose type will be used as a parameter
     * @return The amount of entities of the given type in the world.
     */
    countEntities(entity: import('./entity/IEntity').IEntity): import('./int').int;
    getLoadedEntities(): import('./entity/IEntity').IEntity[];
    getEntitiesNear(position: import('./IPos').IPos, range: import('./double').double): import('./entity/IEntity').IEntity[];
    getEntitiesNear(x: import('./double').double, y: import('./double').double, z: import('./double').double, range: import('./double').double): import('./entity/IEntity').IEntity[];
    /**
     * Sets the block's tile entity at the given position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param tileEntity the tile entity to place
     */
    setTileEntity(x: import('./int').int, y: import('./int').int, z: import('./int').int, tileEntity: import('./ITileEntity').ITileEntity): import('./void').void;
    setTileEntity(pos: import('./IPos').IPos, tileEntity: import('./ITileEntity').ITileEntity): import('./void').void;
    /**
     * Removes the block's tile entity at the given position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    removeTileEntity(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./void').void;
    removeTileEntity(pos: import('./IPos').IPos): import('./void').void;
    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return True if the block at this position is of cubic shape. (Not a stair, slab, etc.)
     */
    isBlockFullCube(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    isBlockFullCube(pos: import('./IPos').IPos): import('./boolean').boolean;
    getSeed(): import('./long').long;
    setSpawnLocation(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./void').void;
    setSpawnLocation(pos: import('./IPos').IPos): import('./void').void;
    canLightningStrikeAt(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    canLightningStrikeAt(pos: import('./IPos').IPos): import('./boolean').boolean;
    isBlockHighHumidity(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    isBlockHighHumidity(pos: import('./IPos').IPos): import('./boolean').boolean;
    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     * @return Text from signs
     * @since 1.7.10d
     */
    getSignText(x: import('./int').int, y: import('./int').int, z: import('./int').int): String;
    getSignText(pos: import('./IPos').IPos): String;
    /**
     * @param x    World position x
     * @param y    World position y
     * @param z    World position z
     * @param item The block to be set
     * @return true if the block was successfully placed
     */
    setBlock(x: import('./int').int, y: import('./int').int, z: import('./int').int, item: import('./item/IItemStack').IItemStack): import('./boolean').boolean;
    setBlock(pos: import('./IPos').IPos, item: import('./item/IItemStack').IItemStack): import('./boolean').boolean;
    /**
     * @param x     World position x
     * @param y     World position y
     * @param z     World position z
     * @param block The block to be set
     * @return true if the block was successfully placed
     */
    setBlock(x: import('./int').int, y: import('./int').int, z: import('./int').int, block: import('./IBlock').IBlock): import('./boolean').boolean;
    setBlock(pos: import('./IPos').IPos, block: import('./IBlock').IBlock): import('./boolean').boolean;
    /**
     * @param x World position x
     * @param y World position y
     * @param z World position z
     */
    removeBlock(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./void').void;
    removeBlock(pos: import('./IPos').IPos): import('./void').void;
    isPlaceCancelled(posX: import('./int').int, posY: import('./int').int, posZ: import('./int').int): import('./boolean').boolean;
    isPlaceCancelled(pos: import('./IPos').IPos): import('./boolean').boolean;
    isBreakCancelled(posX: import('./int').int, posY: import('./int').int, posZ: import('./int').int): import('./boolean').boolean;
    isBreakCancelled(pos: import('./IPos').IPos): import('./boolean').boolean;
    rayCastPos(startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./IPos').IPos;
    rayCastPos(startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int): import('./IPos').IPos;
    rayCastPos(startPos: import('./IPos').IPos, lookVector: import('./IPos').IPos, maxDistance: import('./int').int, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./IPos').IPos;
    rayCastPos(startPos: import('./IPos').IPos, lookVector: import('./IPos').IPos, maxDistance: import('./int').int): import('./IPos').IPos;
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
    rayCastBlock(startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./IBlock').IBlock;
    rayCastBlock(startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int): import('./IBlock').IBlock;
    rayCastBlock(startPos: import('./IPos').IPos, lookVector: import('./IPos').IPos, maxDistance: import('./int').int, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./IBlock').IBlock;
    rayCastBlock(startPos: import('./IPos').IPos, lookVector: import('./IPos').IPos, maxDistance: import('./int').int): import('./IBlock').IBlock;
    /**
     * @param startPos the start position as [x, y, z]
     * @param maxHeight maximum search height
     * @return the position of the closest block of air to startPos
     */
    getNearestAir(startPos: import('./IPos').IPos, maxHeight: import('./int').int): import('./IPos').IPos;
    rayCastEntities(startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./entity/IEntity').IEntity[];
    rayCastEntities(ignoreEntities: import('./entity/IEntity').IEntity[], startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./entity/IEntity').IEntity[];
    rayCastEntities(startPos: import('./IPos').IPos, lookVector: import('./IPos').IPos, maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double, stopOnBlock: import('./boolean').boolean, stopOnLiquid: import('./boolean').boolean, stopOnCollision: import('./boolean').boolean): import('./entity/IEntity').IEntity[];
    rayCastEntities(startPos: import('./double').double[], lookVector: import('./double').double[], maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double): import('./entity/IEntity').IEntity[];
    rayCastEntities(startPos: import('./IPos').IPos, lookVector: import('./IPos').IPos, maxDistance: import('./int').int, offset: import('./double').double, range: import('./double').double): import('./entity/IEntity').IEntity[];
    /**
     * @param name The name of the player to be returned
     * @return The Player with name. Null is returned when the player isnt found
     */
    getPlayer(name: String): import('./entity/IPlayer').IPlayer;
    getPlayerByUUID(uuid: String): import('./entity/IPlayer').IPlayer;
    /**
     * @param time The world time to be set
     */
    setTime(time: import('./long').long): import('./void').void;
    /**
     * @return Whether or not its daytime
     */
    isDay(): import('./boolean').boolean;
    /**
     * @return Whether or not its currently raining
     */
    isRaining(): import('./boolean').boolean;
    /**
     * @param bo Set if it's raining
     */
    setRaining(bo: import('./boolean').boolean): import('./void').void;
    /**
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    thunderStrike(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    thunderStrike(pos: import('./IPos').IPos): import('./void').void;
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
    spawnParticle(particle: String, x: import('./double').double, y: import('./double').double, z: import('./double').double, dx: import('./double').double, dy: import('./double').double, dz: import('./double').double, speed: import('./double').double, count: import('./int').int): import('./void').void;
    spawnParticle(particle: String, pos: import('./IPos').IPos, dx: import('./double').double, dy: import('./double').double, dz: import('./double').double, speed: import('./double').double, count: import('./int').int): import('./void').void;
    /**
     * @param id     The items name
     * @param damage The damage value
     * @param size   The number of items in the item
     * @return Returns the item
     */
    createItem(id: String, damage: import('./int').int, size: import('./int').int): import('./item/IItemStack').IItemStack;
    /**
     * @param directory The particle's texture directory. Use only forward slashes when writing a directory. Example: "customnpcs:textures/particle/tail.png"
     * @return Returns IEntityParticle object
     */
    createEntityParticle(directory: String): import('./IParticle').IParticle;
    getTempData(key: String): Object;
    setTempData(key: String, value: Object): import('./void').void;
    hasTempData(key: String): import('./boolean').boolean;
    removeTempData(key: String): import('./void').void;
    clearTempData(): import('./void').void;
    getTempDataKeys(): String[];
    getStoredData(key: String): Object;
    setStoredData(key: String, value: Object): import('./void').void;
    hasStoredData(key: String): import('./boolean').boolean;
    removeStoredData(key: String): import('./void').void;
    clearStoredData(): import('./void').void;
    getStoredDataKeys(): String[];
    /**
     * @param x     Position x
     * @param y     Position y
     * @param z     Position z
     * @param range Range of the explosion
     * @param fire  Whether or not the explosion does fire damage
     * @param grief Whether or not the explosion does damage to blocks
     */
    explode(x: import('./double').double, y: import('./double').double, z: import('./double').double, range: import('./float').float, fire: import('./boolean').boolean, grief: import('./boolean').boolean): import('./void').void;
    explode(pos: import('./IPos').IPos, range: import('./float').float, fire: import('./boolean').boolean, grief: import('./boolean').boolean): import('./void').void;
    getAllServerPlayers(): import('./entity/IPlayer').IPlayer[];
    getPlayerNames(): String[];
    /**
     * @param x Position x
     * @param z Position z
     * @return Returns the name of the biome
     * @since 1.7.10c
     */
    getBiomeName(x: import('./int').int, z: import('./int').int): String;
    getBiomeName(pos: import('./IPos').IPos): String;
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
    spawnClone(x: import('./int').int, y: import('./int').int, z: import('./int').int, tab: import('./int').int, name: String, ignoreProtection: import('./boolean').boolean): import('./entity/IEntity').IEntity;
    spawnClone(pos: import('./IPos').IPos, tab: import('./int').int, name: String, ignoreProtection: import('./boolean').boolean): import('./entity/IEntity').IEntity;
    spawnClone(x: import('./int').int, y: import('./int').int, z: import('./int').int, tab: import('./int').int, name: String): import('./entity/IEntity').IEntity;
    spawnClone(pos: import('./IPos').IPos, tab: import('./int').int, name: String): import('./entity/IEntity').IEntity;
    getScoreboard(): import('./scoreboard/IScoreboard').IScoreboard;
    /**
     * @return Returns minecraft world object
     * @since 1.7.10c
     * Expert use only
     */
    getMCWorld(): import('../../../net/minecraft/world/WorldServer').WorldServer;
    /**
     *
     * @return The ID of this world's dimension. 0 for overworld, 1 for End, -1 for Nether, etc.
     */
    getDimensionID(): import('./int').int;
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
    createEnergyOrb(owner: import('./entity/IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, size: import('./float').float): import('./entity/IEnergyOrb').IEnergyOrb;
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
    createEnergyBeam(owner: import('./entity/IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, beamWidth: import('./float').float, headSize: import('./float').float): import('./entity/IEnergyBeam').IEnergyBeam;
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
    createEnergyDisc(owner: import('./entity/IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, radius: import('./float').float, thickness: import('./float').float): import('./entity/IEnergyDisc').IEnergyDisc;
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
    createEnergyLaser(owner: import('./entity/IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, laserWidth: import('./float').float): import('./entity/IEnergyLaser').IEnergyLaser;
    broadcast(message: String): import('./void').void;
    world: import('../../../net/minecraft/world/WorldServer').WorldServer;
}
