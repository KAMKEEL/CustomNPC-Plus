package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryNpcTrader
    implements IInventory
{

    private String inventoryTitle;
    private int slotsCount;
    private ItemStack inventoryContents[];
    private ContainerNPCTrader con;

    public InventoryNpcTrader(String s, int i,ContainerNPCTrader con)
    {
    	this.con = con;
        inventoryTitle = s;
        slotsCount = i;
        inventoryContents = new ItemStack[i];
    }
    
    public ItemStack getStackInSlot(int i)
    {
        ItemStack toBuy = inventoryContents[i];
        if(toBuy == null)
        	return null;
        
        return ItemStack.copyItemStack(toBuy);
    }

    public ItemStack decrStackSize(int i, int j)
    {
        if(inventoryContents[i] != null)
        {
            ItemStack itemstack = inventoryContents[i];
            return  ItemStack.copyItemStack(itemstack);
        } else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
    	if(itemstack != null)
    		inventoryContents[i] = itemstack.copy();
        markDirty();
        
    }

    public int getSizeInventory()
    {
        return slotsCount;
    }


    public int getInventoryStackLimit()
    {
        return 64;
    }


    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }


	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}


	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}


	@Override
	public String getInventoryName() {
        return inventoryTitle;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public void markDirty() {
    	con.onCraftMatrixChanged(this);
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}
}
