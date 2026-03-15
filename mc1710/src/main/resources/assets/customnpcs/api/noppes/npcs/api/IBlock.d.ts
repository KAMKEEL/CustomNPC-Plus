/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.IBlock
 */
export interface IBlock {
    getX(): import('./int').int;
    getY(): import('./int').int;
    getZ(): import('./int').int;
    /**
     *
     * @return An IPos object with the block's XYZ position.
     */
    getPosition(): import('./IPos').IPos;
    /**
     * Moves the block to a new position in the given world. The old position is replaced with air.
     *
     * @param pos   The new position of the block
     * @param world The destination world of the block
     * @return Whether the block was successfully placed
     */
    setPosition(pos: import('./IPos').IPos, world: import('./IWorld').IWorld): import('./boolean').boolean;
    setPosition(pos: import('./IPos').IPos): import('./boolean').boolean;
    setPosition(x: import('./int').int, y: import('./int').int, z: import('./int').int, world: import('./IWorld').IWorld): import('./boolean').boolean;
    setPosition(x: import('./int').int, y: import('./int').int, z: import('./int').int): import('./boolean').boolean;
    /**
     *
     * @return The block name as it appears in the block registry. Return example: "minecraft:stone"
     */
    getName(): String;
    /**
     * Deletes the block, setting it to air.
     */
    remove(): import('./void').void;
    isAir(): import('./boolean').boolean;
    /**
     *
     * @param blockName The name of the block to be set in place of this block.
     * @return The new block set in place of this block as an IBlock object.
     */
    setBlock(blockName: String): import('./IBlock').IBlock;
    /**
     *
     * @param block Input IBlock object to replace this block.
     * @return The new block set in place of the previous block.
     */
    setBlock(block: import('./IBlock').IBlock): import('./IBlock').IBlock;
    /**
     *
     * @return True if the block can contain items like a chest does.
     */
    isContainer(): import('./boolean').boolean;
    /**
     *
     * @return The container object of this block. If this block is not a container, an exception will be thrown.
     */
    getContainer(): import('./IContainer').IContainer;
    /**
     *
     * @return An IWorld object of the world this block is in.
     */
    getWorld(): import('./IWorld').IWorld;
    /**
     *
     * @return True if this block has a tile entity. Blocks with custom data like signs, player skulls, chests, etc. will have tile entities.
     */
    hasTileEntity(): import('./boolean').boolean;
    /**
     *
     * @return An ITileEntity object which can modify this block's tile entity.
     */
    getTileEntity(): import('./ITileEntity').ITileEntity;
    /**
     *
     * @param tileEntity Replaces this block's tile entity based on the data given by this parameter.
     */
    setTileEntity(tileEntity: import('./ITileEntity').ITileEntity): import('./void').void;
    /**
     *
     * @return An obfuscated MC object for the block's tile entity.
     */
    getMCTileEntity(): import('../../../net/minecraft/tileentity/TileEntity').TileEntity;
    /**
     *
     * @return An obfuscated MC block object.
     */
    getMCBlock(): import('../../../net/minecraft/block/Block').Block;
    getDisplayName(): String;
    /**
     *
     * @return An INbt object which can modify the block's tile entity's NBT data.
     */
    getTileEntityNBT(): import('./INbt').INbt;
    /**
     *
     * @param maxVolume The volume threshold to determine whether this block's bounding box collides, if it has one.
     * @return Whether this block can be collided with
     */
    canCollide(maxVolume: import('./double').double): import('./boolean').boolean;
    canCollide(): import('./boolean').boolean;
    setBounds(minX: import('./float').float, minY: import('./float').float, minZ: import('./float').float, maxX: import('./float').float, maxY: import('./float').float, maxZ: import('./float').float): import('./void').void;
    getBlockBoundsMinX(): import('./double').double;
    getBlockBoundsMinY(): import('./double').double;
    getBlockBoundsMinZ(): import('./double').double;
    getBlockBoundsMaxX(): import('./double').double;
    getBlockBoundsMaxY(): import('./double').double;
    getBlockBoundsMaxZ(): import('./double').double;
}
