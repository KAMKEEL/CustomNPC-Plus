package noppes.npcs.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.tiles.TileCrate;
import noppes.npcs.blocks.tiles.TileNpcContainer;
import noppes.npcs.constants.EnumGuiType;

public class BlockCrate extends BlockRotated{

	public BlockCrate() {
        super(Blocks.planks);
        //setBlockBounds(0f, 0, 0f, 1f, 0.75f, 1f);
	}
    @Override    
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9){
    	if(par1World.isRemote)
    		return true;

    	par1World.playSoundEffect(i, j + 0.5D, k, "random.chestopen", 0.5F, par1World.rand.nextFloat() * 0.1F + 0.9F);
    	player.openGui(CustomNpcs.instance, EnumGuiType.Crate.ordinal(), par1World, i, j, k);
    	return true;
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
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
    	super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage() , 2);
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCrate();
	}

	@Override
    public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_){
		TileNpcContainer tile = (TileNpcContainer)world.getTileEntity(x, y, z);
        if (tile == null)
        	return;
        tile.dropItems(world, x, y, z);

        world.func_147453_f(x, y, z, block);
        super.breakBlock(world, x, y, z, block, p_149749_6_);
    }
}
