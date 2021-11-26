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

    IPos getPos();

    String getName();

    void remove();

    boolean isAir();

    IBlock setBlock(String var1);

    IBlock setBlock(IBlock var1);

    boolean isContainer();

    IContainer getContainer();

    IWorld getWorld();

    TileEntity getMCTileEntity();

    Block getMCBlock();

    String getDisplayName();
}
