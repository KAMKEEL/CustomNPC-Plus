package noppes.npcs.blocks.tiles;

import net.minecraft.util.AxisAlignedBB;


public class TileWallBanner extends TileBanner {

	@Override
    public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord - 1, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
	
}
