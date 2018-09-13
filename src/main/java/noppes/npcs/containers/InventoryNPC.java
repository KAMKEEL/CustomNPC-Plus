package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryNPC
    implements IInventory
{

    private String inventoryTitle;
    private int slotsCount;
    private ItemStack inventoryContents[];
    private Container con;

    public InventoryNPC(String s, int i,Container con)
    {
    	this.con = con;
        inventoryTitle = s;
        slotsCount = i;
        inventoryContents = new ItemStack[i];
    }
    @Override
    public ItemStack getStackInSlot(int i)
    {
        return inventoryContents[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if(inventoryContents[i] != null)
        {
            if(inventoryContents[i].stackSize <= j)
            {
                ItemStack itemstack = inventoryContents[i];
                inventoryContents[i] = null;
                //onInventoryChanged();
                return itemstack;
            }
            ItemStack itemstack1 = inventoryContents[i].splitStack(j);
            if(inventoryContents[i].stackSize == 0)
            {
                inventoryContents[i] = null;
            }
            //onInventoryChanged();
            return itemstack1;
        } else
        {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        inventoryContents[i] = itemstack;
        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
        //onInventoryChanged();
    }

    @Override
    public int getSizeInventory()
    {
        return slotsCount;
    }
    
    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return false;
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
