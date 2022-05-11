package noppes.npcs.scripted.interfaces;

import net.minecraft.tileentity.TileEntity;
import noppes.npcs.scripted.ScriptBlock;
import noppes.npcs.scripted.ScriptWorld;

public interface ITileEntity {
    int getBlockMetadata();

    ScriptWorld getWorld();

    TileEntity getMCTileEntity();

    void readFromNBT(INbt nbt);

    double getDistanceFrom(double x, double y, double z);

    ScriptBlock getBlockType();

    boolean isInvalid();

    void invalidate();

    void validate();

    void updateContainingBlockInfo();

    INbt getNBT();
}
