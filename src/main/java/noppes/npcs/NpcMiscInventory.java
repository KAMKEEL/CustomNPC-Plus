package noppes.npcs;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NpcMiscInventory implements IInventory {
	public HashMap<Integer,ItemStack> items = new HashMap<Integer,ItemStack>();
	public int stackLimit = 64;

	private int size;
	
	public NpcMiscInventory(int size){
		this.size = size;
	}
	
	public NBTTagCompound getToNBT(){
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("NpcMiscInv", NBTTags.nbtItemStackList(items));
		return nbttagcompound;
	}
	public void setFromNBT(NBTTagCompound nbttagcompound){
		items = NBTTags.getItemStackList(nbttagcompound.getTagList("NpcMiscInv", 10));
	}
	@Override
	public int getSizeInventory() {
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return items.get(var1);
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2) {
        if (items.get(par1) == null)
        	return null;

        ItemStack var4 = null;
        if (items.get(par1).stackSize <= par2){
            var4 = items.get(par1);
            items.put(par1,null);
        }
        else{
            var4 = items.get(par1).splitStack(par2);

            if (items.get(par1).stackSize == 0){
            	items.put(par1,null);
            }
        }
        
		return var4;
	}


	public boolean decrStackSize(ItemStack eating, int decrease) {
		for(int slot : items.keySet()){
			ItemStack item = items.get(slot);
			if(items != null && eating == item && item.stackSize >= decrease){
				item.splitStack(decrease);
				if(item.stackSize <= 0)
	            	items.put(slot, null);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
        if (items.get(var1) != null)
        {
            ItemStack var3 = items.get(var1);
            items.put(var1,null);
            return var3;
        }
        return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		if(var1 >= getSizeInventory())
			return;
        items.put(var1,var2);
	}

	@Override
	public int getInventoryStackLimit() {
		return stackLimit;
	}


	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}
	
	@Override
	public String getInventoryName() {
		return "Npc Misc Inventory";
	}
	
	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}
	
	@Override
	public void markDirty() {
		
	}
	@Override
	public void openInventory() {
		
	}
	@Override
	public void closeInventory() {
		
	}
	public boolean addItemStack(ItemStack item) {
		ItemStack mergable;
		boolean merged = false;
		while((mergable = getMergableItem(item)) != null && mergable.stackSize > 0){
			int size = mergable.getMaxStackSize() - mergable.stackSize;
			if(size > item.stackSize){
				mergable.stackSize = mergable.getMaxStackSize();
				item.stackSize -= size;
				merged = true;
			}
			else{
				mergable.stackSize += item.stackSize;
				item.stackSize = 0;
			}
		}
		if(item.stackSize <= 0)
			return true;
		int slot = firstFreeSlot();
		if(slot >= 0){
			items.put(slot, item.copy());
			item.stackSize = 0;
			return true;
		}
		return merged;
	}
	
	public ItemStack getMergableItem(ItemStack item){
		for(ItemStack is : items.values()){
			if(NoppesUtilPlayer.compareItems(item, is, false, false) && is.stackSize < is.getMaxStackSize()){
				return is;
			}
		}
		return null;
	}
	
	public int firstFreeSlot(){
		for(int i = 0; i < getSizeInventory(); i++){
			if(items.get(i) == null)
				return i;
		}
		return -1;
	}
	public void setSize(int i) {
		size = i;
	}
}
