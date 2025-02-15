package noppes.npcs.blocks.tiles;

import net.minecraft.util.AxisAlignedBB;

public class TileShortLamp extends TileColorable {

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord + 0.25f, yCoord, zCoord + 0.25f, xCoord + 0.75f, yCoord + 1, zCoord + 0.75f);
    }

}
