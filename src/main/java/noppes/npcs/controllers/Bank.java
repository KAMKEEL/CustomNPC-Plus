package noppes.npcs.controllers;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NpcMiscInventory;


public class Bank {
	public int id = -1;
	public String name = "";
	public HashMap<Integer,Integer> slotTypes;
	public int startSlots = 1;
	public int maxSlots = 6;
	public NpcMiscInventory currencyInventory;
	public NpcMiscInventory upgradeInventory;
	public Bank(){
		slotTypes = new HashMap<Integer, Integer>();
		currencyInventory = new NpcMiscInventory(6);
		upgradeInventory = new NpcMiscInventory(6);
		for(int i = 0; i < 6; i++)
			slotTypes.put(i, 0);
	}
	
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("BankID", id);
    	nbttagcompound.setTag("BankCurrency", currencyInventory.getToNBT());
    	nbttagcompound.setTag("BankUpgrade", upgradeInventory.getToNBT());
    	nbttagcompound.setString("Username", name);
    	nbttagcompound.setInteger("MaxSlots", maxSlots);
    	nbttagcompound.setInteger("StartSlots", startSlots);
    	nbttagcompound.setTag("BankTypes", NBTTags.nbtIntegerIntegerMap(slotTypes));
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		id = nbttagcompound.getInteger("BankID");
        name = nbttagcompound.getString("Username");
        startSlots = nbttagcompound.getInteger("StartSlots");
        maxSlots = nbttagcompound.getInteger("MaxSlots");
        slotTypes = NBTTags.getIntegerIntegerMap(nbttagcompound.getTagList("BankTypes", 10));
		currencyInventory.setFromNBT(nbttagcompound.getCompoundTag("BankCurrency"));
		upgradeInventory.setFromNBT(nbttagcompound.getCompoundTag("BankUpgrade"));
	}
	
	public boolean isUpgraded(int slot){
    	return slotTypes.get(slot) != null && slotTypes.get(slot) == 2;
	}

	public boolean canBeUpgraded(int slot) {
    	if(upgradeInventory.getStackInSlot(slot) == null)
    		return false;
    	return slotTypes.get(slot) == null || slotTypes.get(slot) == 0;
	}

	public int getMaxSlots() {
		for(int i = 0; i < maxSlots; i++){
	    	if(currencyInventory.getStackInSlot(i) == null && i > startSlots-1)
	    		return i;
		}
		return maxSlots;
	}
}
