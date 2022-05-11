//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptTileEntity;

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

    boolean hasTileEntity();

    ScriptTileEntity getTileEntity();

    void setTileEntity(ScriptTileEntity tileEntity);

    TileEntity getMCTileEntity();

    Block getMCBlock();

    String getDisplayName();

    INbt getTileEntityNBT();
}
