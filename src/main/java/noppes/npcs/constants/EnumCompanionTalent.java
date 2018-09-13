package noppes.npcs.constants;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;
import noppes.npcs.roles.RoleCompanion;

public enum EnumCompanionTalent {
	INVENTORY(CustomItems.satchel), ARMOR(Items.iron_chestplate), 
	SWORD(Items.diamond_sword), RANGED(Items.bow), 
	ACROBATS(Items.leather_boots), INTEL(CustomItems.letter);

	public ItemStack item;
	private EnumCompanionTalent(Item item){
		this.item = new ItemStack(item);
	}
}
