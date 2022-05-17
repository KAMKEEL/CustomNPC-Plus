//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public interface IBlock {
    int getX();

    int getY();

    int getZ();

    /**
     *
     * @return Returns an IPos object with the block's XYZ position.
     */
    IPos getPos();

    /**
     *
     * @return Returns the block name as it appears in the block registry. Return example: "minecraft:stone"
     */
    String getName();

    /**
     * Deletes the block, setting it to air.
     */
    void remove();

    boolean isAir();

    /**
     *
     * @param blockName The name of the block to be set in place of this block.
     * @return Returns the new block set in place of this block as an IBlock object.
     */
    IBlock setBlock(String blockName);

    /**
     *
     * @param block Input IBlock object to replace this block.
     * @return Returns the new block set in place of the previous block.
     */
    IBlock setBlock(IBlock block);

    /**
     *
     * @return Returns true if the block can contain items like a chest does.
     */
    boolean isContainer();

    /**
     *
     * @return Returns the container object of this block. If this block is not a container, an exception will be thrown.
     */
    IContainer getContainer();

    /**
     *
     * @return Returns an IWorld object of the world this block is in.
     */
    IWorld getWorld();

    /**
     *
     * @return Returns true if this block has a tile entity. Blocks with custom data like signs, player skulls, chests, etc. will have tile entities.
     */
    boolean hasTileEntity();

    /**
     *
     * @return Returns an ITileEntity object which can modify this block's tile entity.
     */
    ITileEntity getTileEntity();

    /**
     *
     * @param tileEntity Replaces this block's tile entity based on the data given by this parameter.
     */
    void setTileEntity(ITileEntity tileEntity);

    /**
     *
     * @return Returns a non-script, obfuscated MC object for the block's tile entity.
     */
    TileEntity getMCTileEntity();

    /**
     *
     * @return Returns a non-script, obfuscated MC block object.
     */
    Block getMCBlock();

    String getDisplayName();

    /**
     *
     * @return Returns an INbt object which can modify the block's tile entity's NBT data.
     */
    INbt getTileEntityNBT();
}
