package noppes.npcs.enchants;

import noppes.npcs.items.ItemGun;
import noppes.npcs.items.ItemStaff;

public class EnchantInfinite extends EnchantInterface {

	public EnchantInfinite() {
		super(3, ItemStaff.class, ItemGun.class);
		setName("infinite");
	}

	@Override
	public int getMinEnchantability(int par1) {
		return 20;
	}

	@Override
	public int getMaxEnchantability(int par1) {
		return 50;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
