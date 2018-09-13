package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobItemGiver;

public class ContainerNpcItemGiver extends Container
{    
    private JobItemGiver role;

    public ContainerNpcItemGiver(EntityNPCInterface npc,EntityPlayer player)
    {
    	this.role = (JobItemGiver) npc.jobInterface;
        for(int j1 = 0; j1 < 9; j1++)
        {
        	addSlotToContainer(new Slot(role.inventory, j1, 6 + j1 * 18, 90));
        }

        for(int i1 = 0; i1 < 3; i1++)
        {
            for(int l1 = 0; l1 < 9; l1++)
            {
            	addSlotToContainer(new Slot(player.inventory, l1 + i1 * 9 + 9, 6 + l1 * 18, 116 + i1 * 18));
            }

        }

        for(int j1 = 0; j1 < 9; j1++)
        {
        	addSlotToContainer(new Slot(player.inventory, j1, 6 + j1 * 18, 174));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i)
    {
        return null;
    }
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}
