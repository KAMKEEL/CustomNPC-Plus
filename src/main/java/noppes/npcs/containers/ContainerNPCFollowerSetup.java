package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

public class ContainerNPCFollowerSetup extends Container
{
	private RoleFollower role;

    public ContainerNPCFollowerSetup(EntityNPCInterface npc,EntityPlayer player)
    {
        role = (RoleFollower) npc.roleInterface;
    	
        for(int i1 = 0; i1 < 3; i1++)
        	addSlotToContainer(new Slot(role.inventory, i1, 44, 39 + i1 * 25));
        
        for(int i1 = 0; i1 < 3; i1++)
        {
            for(int l1 = 0; l1 < 9; l1++)
            {
            	addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 8 + l1 * 18, 113 + i1 * 18));
            }

        }

        for(int j1 = 0; j1 < 9; j1++)
        {
        	addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 171));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)inventorySlots.get(i);
        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if(i >= 0 && i < 3)
            {
                if(!mergeItemStack(itemstack1, 3, 38, true))
                {
                    return null;
                }
            } else
            if(i >= 3 && i < 30)
            {
                if(!mergeItemStack(itemstack1, 30, 38, false))
                {
                    return null;
                }
            } else
            if(i >= 30 && i < 38)
            {
                if(!mergeItemStack(itemstack1, 3, 29, false))
                {
                    return null;
                }
            } else
            if(!mergeItemStack(itemstack1, 3, 38, false))
            {
                return null;
            }
            if(itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            } else
            {
                slot.onSlotChanged();
            }
            if(itemstack1.stackSize != itemstack.stackSize)
            {
                slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
            } else
            {
                return null;
            }
        }
        return itemstack;
    }
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
