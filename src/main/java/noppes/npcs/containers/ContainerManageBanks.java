package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.Bank;

public class ContainerManageBanks extends Container
{
	public Bank bank;
	
    public ContainerManageBanks(EntityPlayer player)
    {
   		bank = new Bank();
   		
        for(int i = 0; i < 6; i++)
        {
        	int x =  36;
        	int y = 38;
        	y += i * 22;
        	addSlotToContainer(new Slot(bank.currencyInventory, i, x, y));
        }   
        
        for(int i = 0; i < 6; i++)
        {
        	int x =  142;
        	int y = 38;
        	y += i * 22;
        	addSlotToContainer(new Slot(bank.upgradeInventory, i, x, y));
        }   

        for(int j1 = 0; j1 < 9; j1++)
        {
        	addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 171));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
        return null;
    }
    
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void setBank(Bank bank2) {
		for(int i = 0; i< 6; i++){
			bank.currencyInventory.setInventorySlotContents(i, bank2.currencyInventory.getStackInSlot(i));
			bank.upgradeInventory.setInventorySlotContents(i, bank2.upgradeInventory.getStackInSlot(i));
		}
	}
}

