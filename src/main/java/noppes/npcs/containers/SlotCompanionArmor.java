package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import noppes.npcs.roles.RoleCompanion;

class SlotCompanionArmor extends Slot{

    final int armorType;
    final RoleCompanion role;

    public SlotCompanionArmor(RoleCompanion role, IInventory iinventory, int id, int x, int y, int type){
        super(iinventory, id, x, y);
        armorType = type;
        this.role = role;
    }

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public IIcon getBackgroundIconIndex() {
		return ItemArmor.func_94602_b(this.armorType);
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {
		if (itemstack.getItem() instanceof ItemArmor && role.canWearArmor(itemstack))
			return ((ItemArmor) itemstack.getItem()).armorType == armorType;
		
		if (itemstack.getItem() instanceof ItemBlock)
			return armorType == 0;
		
		return false;
	}
}
