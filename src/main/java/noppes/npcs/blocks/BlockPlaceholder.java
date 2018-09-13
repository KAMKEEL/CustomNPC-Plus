package noppes.npcs.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlaceholder extends Block{
	
	private IIcon[] icons = new IIcon[16];

	public BlockPlaceholder() {
        super(Material.rock);
	}
	
    @Override   
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack){
    	super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage() , 2);
    }
    
    @Override   
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List){
    	for(int i = 0; i < 16; i++)
    		par3List.add(new ItemStack(par1, 1, i));
    }

    @Override   
    public int damageDropped(int par1){
        return par1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister){
    	for(int i = 0; i < 16; i++){
    		icons[i] = par1IconRegister.registerIcon("customnpcs:placeholder_" + i);
    	}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int meta)
    {
    	if(meta < icons.length)
    		return icons[meta];
        return icons[0];
    }

}
