package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotValid extends Slot{
	private boolean canPutIn = true;

	public SlotValid(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}
	
	public SlotValid(IInventory par1iInventory, int par2, int par3, int par4, boolean bo) {
		super(par1iInventory, par2, par3, par4);
		canPutIn = bo;
	}
	
    public boolean isItemValid(ItemStack par1ItemStack){
        return canPutIn && inventory.isItemValidForSlot(0, par1ItemStack);
    }
}
