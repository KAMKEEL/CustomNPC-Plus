package noppes.npcs.blocks.tiles;

import net.minecraft.util.AxisAlignedBB;


public class TileTallLamp extends TileColorable {

	@Override
    public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }
}
