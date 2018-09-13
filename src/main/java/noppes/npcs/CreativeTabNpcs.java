package noppes.npcs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class CreativeTabNpcs extends CreativeTabs{
	public Item item = Items.bowl;
	public int meta = 0;

	public CreativeTabNpcs(String label) {
		super(label);
	}

	@Override
	public Item getTabIconItem() {
		return item;
	}
	
	@Override
    public int func_151243_f(){
        return meta;
    }
}
