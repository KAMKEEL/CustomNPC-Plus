package noppes.npcs.items;

import java.awt.Color;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumNpcToolMaterial;

public class ItemElementalStaff extends ItemStaff{
	public ItemElementalStaff(int par1, EnumNpcToolMaterial material) {
		super(par1,material);
		setHasSubtypes(true);
	}

    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
    	float[] color = EntitySheep.fleeceColorTable[par1ItemStack.getItemDamage()];
        return new Color(color[0],color[1],color[2]).getRGB();
    }
    
    @Override
    public boolean requiresMultipleRenderPasses(){
        return true;
    }
    
	@Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        for (int var4 = 0; var4 < 16; ++var4){
            par3List.add(new ItemStack(par1, 1, var4));
        }
    }

	@Override
	public ItemStack getProjectile(ItemStack stack){
		return new ItemStack(CustomItems.orb,1,stack.getItemDamage());
	}
	
	@Override
	public void spawnParticle(ItemStack stack, EntityPlayer player){
		CustomNpcs.proxy.spawnParticle(player,"Spell",stack.getItemDamage(),4);
	}
}
