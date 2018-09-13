package noppes.npcs.items;

import java.awt.Color;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemOrb extends ItemNpcInterface{

	public ItemOrb(int par1) {
		super(par1);
		setHasSubtypes(true);
	}


    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2){
    	float[] color = EntitySheep.fleeceColorTable[par1ItemStack.getItemDamage()];
        return new Color(color[0],color[1],color[2]).getRGB();
    }
    
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
	@Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int var4 = 0; var4 < 16; ++var4)
        {
            par3List.add(new ItemStack(par1, 1, var4));
        }
    }

}
