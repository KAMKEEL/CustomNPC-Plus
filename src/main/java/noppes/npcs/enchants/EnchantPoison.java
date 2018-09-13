package noppes.npcs.enchants;

import noppes.npcs.items.ItemGun;
import noppes.npcs.items.ItemStaff;

public class EnchantPoison extends EnchantInterface {

	public EnchantPoison() {
		super(6, ItemStaff.class, ItemGun.class);
		setName("poison");
	}

	@Override
	public int getMinEnchantability(int par1) {
        return 12 + (par1 - 1) * 20;
	}

	@Override
	public int getMaxEnchantability(int par1) {
        return this.getMinEnchantability(par1) + 25;
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}
}
