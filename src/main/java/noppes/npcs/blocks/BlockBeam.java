package noppes.npcs.blocks;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.blocks.tiles.TileBeam;
import noppes.npcs.blocks.tiles.TileColorable;

public class BlockBeam extends BlockRotated{
	
	public BlockBeam() {
        super(Blocks.planks);
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
    public int damageDropped(int par1){
        return par1;
    }
    @Override 
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z){
    	TileEntity tileentity = world.getTileEntity(x, y, z);
    	if(!(tileentity instanceof TileColorable)){
    		super.setBlockBoundsBasedOnState(world, x, y, z);
    		return;
    	}
    	TileColorable tile = (TileColorable) tileentity;
    	if(tile.rotation == 0){
	        setBlockBounds(0.33f, 0.33f, 0.25f, 0.67f, 0.67f, 1f );
		}
		else if(tile.rotation == 2){
	        setBlockBounds(0.33f, 0.33f, 0, 0.67f, 0.67f, 0.75f );
		}
		else if(tile.rotation == 3){
	        setBlockBounds(0.25f, 0.33f, 0.33f, 1f, 0.67f, 0.67f );
		}
		else if(tile.rotation == 1){
	        setBlockBounds(0, 0.33f, 0.33f, 0.75f, 0.67f, 0.67f );
		}
    }

    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage() , 2);
    }


	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileBeam();
	}

}
