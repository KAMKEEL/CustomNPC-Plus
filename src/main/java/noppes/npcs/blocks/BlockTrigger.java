package noppes.npcs.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.blocks.tiles.TileColorable;

public abstract class BlockTrigger extends BlockRotated{

	protected BlockTrigger(Block block) {
		super(block);
	}

	@Override
    public boolean canProvidePower(){
        return true;
    }

	@Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int p_149748_5_){
        return this.isProvidingWeakPower(world, x, y, z, p_149748_5_);
    }

	@Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int p_149709_5_){
		TileColorable tile = (TileColorable)world.getTileEntity(x, y, z);
		if(tile != null)
			return tile.powerProvided();
        return 0;
    }

    public void updateSurrounding(World par1World, int par2, int par3, int par4){
        par1World.notifyBlocksOfNeighborChange(par2, par3, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3 - 1, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3 + 1, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2 - 1, par3, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2 + 1, par3, par4, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3, par4 - 1, this);
        par1World.notifyBlocksOfNeighborChange(par2, par3, par4 + 1, this);
    }
}
