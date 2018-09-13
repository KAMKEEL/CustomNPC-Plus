package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.IIcon;
import noppes.npcs.roles.RoleCompanion;

class SlotCompanionWeapon extends Slot{

    final RoleCompanion role;

    public SlotCompanionWeapon(RoleCompanion role, IInventory iinventory, int id, int x, int y){
        super(iinventory, id, x, y);
        this.role = role;
    }

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {	
		return role.canWearSword(itemstack);
	}
}
