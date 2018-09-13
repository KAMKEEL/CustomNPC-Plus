package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.roles.RoleFollower;

class SlotNpcMercenaryCurrency extends Slot
{

	RoleFollower role; /* synthetic field */

    public SlotNpcMercenaryCurrency(RoleFollower role, IInventory inv, int i, int j, int k)
    {
        super(inv, i, j, k);
        this.role = role;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
    	Item item = itemstack.getItem();
    	for(ItemStack is : role.inventory.items.values()){
    		if(item == is.getItem()){
    			if(itemstack.getHasSubtypes() && itemstack.getItemDamage() != is.getItemDamage())
    				continue;
    			return true;
    		}
    	}
        return false;
        
    }
}
