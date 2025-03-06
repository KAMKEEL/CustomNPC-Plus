package noppes.npcs.blocks.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;

public class TileBlockAnvil extends TileEntity {

    public boolean firstTick = true;

    public TileBlockAnvil(){}

    public boolean canUpdate(){
        return true;
    }

    @Override
    public void updateEntity() {
        if (hasWorldObj() && firstTick && !getWorldObj().isRemote) {
            firstTick = false;
            getWorldObj().updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
            getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }
}
