package noppes.npcs.items;

import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;

import org.lwjgl.opengl.GL11;

public class ItemWand extends ItemNpcInterface{

	public ItemWand(int par1) {
		super(par1);
		setCreativeTab(CustomItems.tabMisc);
	}
	
	@Override
	public boolean hasEffect(ItemStack par1ItemStack, int pass){
		return true;
	}

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.54f, 0.54f, 0.54f);
        GL11.glTranslatef(0.1F, 0.5f, 0.1f);
	}
}
