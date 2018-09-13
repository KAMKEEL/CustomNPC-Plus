package noppes.npcs.blocks;

import java.util.List;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class BlockCrystal extends BlockBreakable{
    
	public BlockCrystal() {
		super("customnpcs:npcCrystal",Material.glass, false);
		setLightLevel(0.8f);
	}

    @Override
    public boolean isOpaqueCube(){
        return false;
    }
    
    @Override
    public boolean renderAsNormalBlock(){
        return false;
    }    

    @Override
    public int getRenderBlockPass(){
        return 1;
    }

    @Override
    public int damageDropped(int meta){
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list){
        for (int i = 0; i < 16; ++i){
        	list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z){
        return getRenderColor(world.getBlockMetadata(x, y, z));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta){
        return MapColor.getMapColorForBlockColored(meta).colorValue;
    }

    public MapColor getMapColor(int p_149728_1_){
        return MapColor.getMapColorForBlockColored(p_149728_1_);
    }
    @Override   
    public String getUnlocalizedName(){
    	return "item.npcCrystal";
    }
}
