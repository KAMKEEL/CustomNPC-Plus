package noppes.npcs.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

class SlotNPCArmor extends Slot
{

    final int armorType; /* synthetic field */

    SlotNPCArmor(IInventory iinventory, int i, int j, int k, int l)
    {
        super(iinventory, i, j, k);
        armorType = l;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    @Override
    public IIcon getBackgroundIconIndex()
    {
        return ItemArmor.func_94602_b(this.armorType);
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
        if(itemstack.getItem() instanceof ItemArmor)
        {
            return ((ItemArmor)itemstack.getItem()).armorType == armorType;
        }
        if(itemstack.getItem() instanceof ItemBlock)
        {
            return armorType == 0;
        } else
        {
            return false;
        }
    }
}
