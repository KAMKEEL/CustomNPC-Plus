package noppes.npcs.blocks;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.blocks.tiles.TileShelf;

public class BlockShelf extends BlockRotated{
	
	public BlockShelf() {
        super(Blocks.planks);
	}

    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
    	super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage() , 2);
    }

    @Override   
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int x, int y, int z){
    	setBlockBoundsBasedOnState(p_149668_1_, x, y, z);
        return AxisAlignedBB.getBoundingBox(x + minX, y + 0.9f, z + minZ, x + maxX, y + 1, z + maxZ);
    }
    
    @Override   
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
        par3List.add(new ItemStack(par1, 1, 3));
        par3List.add(new ItemStack(par1, 1, 4));
        par3List.add(new ItemStack(par1, 1, 5));
    }
    @Override 
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z){
    	TileEntity tileentity = world.getTileEntity(x, y, z);
    	if(!(tileentity instanceof TileColorable)){
    		super.setBlockBoundsBasedOnState(world, x, y, z);
    		return;
    	}
    	TileColorable tile = (TileColorable) tileentity;
    	float xStart = 0;
    	float zStart = 0;
    	float xEnd = 1;
    	float zEnd = 1;
    	if(tile.rotation == 0)
    		zStart = 0.3f;
    	else if(tile.rotation == 2)
    		zEnd = 0.7f;
    	else if(tile.rotation == 3)
    		xStart = 0.3f;
    	else if(tile.rotation == 1)
    		xEnd = 0.7f;
        setBlockBounds(xStart, 0.44f, zStart, xEnd, 1, zEnd);
    }

    @Override   
    public int damageDropped(int par1){
        return par1;
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileShelf();
	}

}
