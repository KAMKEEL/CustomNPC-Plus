/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.ITileEntity
 */
export interface ITileEntity {
    /**
     *
     * @return An integer representing the metadata of this block. Blocks with different states will
     * return different values. For example, each stage of growth for a wheat crop will return a different value, each
     * orientation of a stair block will return a different value, etc.
     */
    getBlockMetadata(): import('./int').int;
    /**
     *
     * @return The world this tile entity is in.
     */
    getWorld(): import('./IWorld').IWorld;
    setWorld(world: import('./IWorld').IWorld): import('./void').void;
    /**
     *
     * @return An obfuscated MC tile entity object.
     */
    getMCTileEntity(): import('../../../net/minecraft/tileentity/TileEntity').TileEntity;
    markDirty(): import('./void').void;
    /**
     * Reads an INbt compound tag, creates a tile entity based on the data, and replaces this entity with the new one.
     * @param nbt the NBT data to read from
     */
    readFromNBT(nbt: import('./INbt').INbt): import('./void').void;
    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return The distance of this tile entity from the point given by the x, y, and z parameters.
     */
    getDistanceFrom(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./double').double;
    getDistanceFrom(pos: import('./IPos').IPos): import('./double').double;
    /**
     * @return A new IBlock object based on this tile entity's type.
     */
    getBlockType(): import('./IBlock').IBlock;
    /**
     *
     * @return True if the tile entity is invalid, false otherwise.
     */
    isInvalid(): import('./boolean').boolean;
    /**
     * Invalidates the tile entity.
     */
    invalidate(): import('./void').void;
    /**
     * Validates the tile entity for use.
     */
    validate(): import('./void').void;
    /**
     * Updates the block's tile entity to the values set in this object.
     */
    updateContainingBlockInfo(): import('./void').void;
    /**
     * Writes the tile entity to NBT and returns the compound tag.
     * @return the tile entity's NBT data
     */
    getNBT(): import('./INbt').INbt;
}
