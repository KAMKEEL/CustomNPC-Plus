package noppes.npcs.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemNpcBlock extends ItemBlock{
	public String[] names;
	public ItemNpcBlock(Block block) {
		super(block);
	}
	
    public String getUnlocalizedName(ItemStack par1ItemStack){
    	if(names != null && par1ItemStack.getItemDamage() < names.length)
    		return names[par1ItemStack.getItemDamage()];
        return this.field_150939_a.getUnlocalizedName();
    }
}
