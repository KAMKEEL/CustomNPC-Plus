package noppes.npcs.enchants;

import noppes.npcs.items.ItemGun;
import noppes.npcs.items.ItemStaff;

public class EnchantDamage extends EnchantInterface {

	public EnchantDamage() {
		super(10, ItemStaff.class, ItemGun.class);
		setName("damage");
	}

	@Override
	public int getMinEnchantability(int par1) {
		return 1 + (par1 - 1) * 10;
	}

	@Override
	public int getMaxEnchantability(int par1) {
		return this.getMinEnchantability(par1) + 15;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}
}
