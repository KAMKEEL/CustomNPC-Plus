package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.NBTTags;
import noppes.npcs.NpcMiscInventory;

import java.util.HashMap;


public class Bank {
    public int id = -1;
    public String name = "";
    public HashMap<Integer, Integer> slotTypes;
    public int startSlots = 1;
    public int maxSlots = 6;
    public NpcMiscInventory currencyInventory;
    public NpcMiscInventory upgradeInventory;

    public Bank() {
        slotTypes = new HashMap<Integer, Integer>();
        currencyInventory = new NpcMiscInventory(6);
        upgradeInventory = new NpcMiscInventory(6);
        for (int i = 0; i < 6; i++)
            slotTypes.put(i, 0);
    }

    public void writeEntityToNBT(INbt INbt) {
        INbt.setInteger("BankID", id);
        INbt.setTag("BankCurrency", currencyInventory.getToNBT());
        INbt.setTag("BankUpgrade", upgradeInventory.getToNBT());
        INbt.setString("Username", name);
        INbt.setInteger("MaxSlots", maxSlots);
        INbt.setInteger("StartSlots", startSlots);
        INbt.setTag("BankTypes", NBTTags.nbtIntegerIntegerMap(slotTypes));
    }

    public void readEntityFromNBT(INbt INbt) {
        id = INbt.getInteger("BankID");
        name = INbt.getString("Username");
        startSlots = INbt.getInteger("StartSlots");
        maxSlots = INbt.getInteger("MaxSlots");
        slotTypes = NBTTags.getIntegerIntegerMap(INbt.getTagList("BankTypes", 10));
        currencyInventory.setFromNBT(INbt.getCompoundTag("BankCurrency"));
        upgradeInventory.setFromNBT(INbt.getCompoundTag("BankUpgrade"));
    }

    public boolean isUpgraded(int slot) {
        return slotTypes.get(slot) != null && slotTypes.get(slot) == 2;
    }

    public boolean canBeUpgraded(int slot) {
        if (upgradeInventory.getStackInSlot(slot) == null)
            return false;
        return slotTypes.get(slot) == null || slotTypes.get(slot) == 0;
    }

    public int getMaxSlots() {
        for (int i = 0; i < maxSlots; i++) {
            if (currencyInventory.getStackInSlot(i) == null && i > startSlots - 1)
                return i;
        }
        return maxSlots;
    }
}
