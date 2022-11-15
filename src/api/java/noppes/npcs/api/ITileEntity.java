package noppes.npcs.api;

import net.minecraft.tileentity.TileEntity;

public interface ITileEntity {
    /**
     *
     * @return An integer representing the metadata of this block. Blocks with different states will
     * return different values. For example, each stage of growth for a wheat crop will return a different value, each
     * orientation of a stair block will return a different value, etc.
     */
    int getBlockMetadata();

    /**
     *
     * @return The world this tile entity is in.
     */
    IWorld getWorld();

    void setWorld(IWorld world);

    /**
     *
     * @return An obfuscated MC tile entity object.
     */
    TileEntity getMCTileEntity();

    void markDirty();

    /**
     * Reads an INbt compound tag, creates a tile entity based on the data, and replaces this entity with the new one.
     */
    void readFromNBT(INbt nbt);

    /**
     * @return The distance of this tile entity from the point given by the x, y, and z parameters.
     */
    double getDistanceFrom(double x, double y, double z);
    double getDistanceFrom(IPos pos);

    /**
     * @return A new IBlock object based on this tile entity's type.
     */
    IBlock getBlockType();

    /**
     *
     * @return True if the tile entity is invalid, false otherwise.
     */
    boolean isInvalid();

    /**
     * Invalidates the tile entity.
     */
    void invalidate();

    /**
     * Validates the tile entity for use.
     */
    void validate();

    /**
     * Updates the block's tile entity to the values set in this object.
     */
    void updateContainingBlockInfo();

    /**
     * Writes the tile entity to NBT and returns the compound tag.
     */
    INbt getNBT();
}
