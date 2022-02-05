package noppes.npcs.quests;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NpcMiscInventory;

public class QuestItem extends QuestInterface{
	public NpcMiscInventory items = new NpcMiscInventory(3);
	public boolean leaveItems = false;
	public boolean ignoreDamage = false;
	public boolean ignoreNBT = false;

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		items.setFromNBT(compound.getCompoundTag("Items"));
		leaveItems = compound.getBoolean("LeaveItems");
		ignoreDamage = compound.getBoolean("IgnoreDamage");
		ignoreNBT = compound.getBoolean("IgnoreNBT");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("Items", items.getToNBT());
		compound.setBoolean("LeaveItems", leaveItems);
		compound.setBoolean("IgnoreDamage", ignoreDamage);
		compound.setBoolean("IgnoreNBT", ignoreNBT);
	}

	@Override
	public boolean isCompleted(EntityPlayer player) {
		HashMap<Integer,ItemStack> map = getProcessSet(player);
		for(ItemStack reqItem : items.items.values()){
			boolean done = false;
			for(ItemStack item : map.values()){
				if(NoppesUtilPlayer.compareItems(reqItem, item, ignoreDamage, ignoreNBT) && item.stackSize >= reqItem.stackSize){
					done = true;
					break;
				}
			}
			if(!done)
				return false;
		}
		
		
		return true;
	}
	public HashMap<Integer,ItemStack> getProcessSet(EntityPlayer player){
		HashMap<Integer,ItemStack> map = new HashMap<Integer,ItemStack>();
		for(int slot : items.items.keySet()){
			ItemStack item = items.items.get(slot);
			if(item == null)
				continue;
			ItemStack is = item.copy();
			is.stackSize = 0;
			map.put(slot,is);
		}
		for(ItemStack item : player.inventory.mainInventory){
			if(item == null)
				continue;
			for(ItemStack questItem : map.values()){
				if(NoppesUtilPlayer.compareItems(questItem, item, ignoreDamage, ignoreNBT)){
					questItem.stackSize += item.stackSize;
				}
			}
		}
		return map;
	}
	@Override
	public void handleComplete(EntityPlayer player) {
		if(leaveItems)
			return;
		for(ItemStack questitem : items.items.values()){
			int stacksize = questitem.stackSize;
			for(int i = 0; i < player.inventory.mainInventory.length; i++){
				ItemStack item = player.inventory.mainInventory[i];
				if(item == null)
					continue;
				if(NoppesUtilPlayer.compareItems(item, questitem, ignoreDamage, ignoreNBT)){
					int size = item.stackSize;
					if(stacksize - size >= 0){
						player.inventory.setInventorySlotContents(i, null);
						item.splitStack(size);
					}
					else{
						item.splitStack(stacksize);
					}
					stacksize -= size;
					if(stacksize <= 0)
						break;
				}
			}
		}
	}

	@Override
	public Vector<String> getQuestLogStatus(EntityPlayer player) {
		Vector<String> vec = new Vector<String>();

		HashMap<Integer,ItemStack> map = getProcessSet(player);
		for(int slot : map.keySet()){
			ItemStack item = map.get(slot);
			ItemStack quest = items.items.get(slot);
			if(item == null)
				continue;
			String process = item.stackSize + "";
			if(item.stackSize > quest.stackSize)
				process = quest.stackSize + "";
			process += "/" + quest.stackSize + "";
			if(item.hasDisplayName())
				vec.add(item.getDisplayName() + ": " + process);
			else
				vec.add(item.getUnlocalizedName() + ".name" + ": " + process);
		}
		return vec;
	}


}
