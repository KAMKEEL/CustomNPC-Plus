package noppes.npcs.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemMounter extends Item{
	
    public ItemMounter(){
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }

    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
		return 0x8B4513;
    }
    
    @Override
    public boolean requiresMultipleRenderPasses(){
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister){
        this.itemIcon = Items.saddle.getIconFromDamage(0);
    }

    @Override
    public Item setUnlocalizedName(String name){
		GameRegistry.registerItem(this, name);
    	return super.setUnlocalizedName(name);
    }
}
