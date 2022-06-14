package noppes.npcs.roles;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import foxz.utils.Market;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleTrader extends RoleInterface{

    public String marketName = "";
	public NpcMiscInventory inventoryCurrency;
	public NpcMiscInventory inventorySold;

	public boolean ignoreDamage = false;
	public boolean ignoreNBT = false;
	
	public boolean toSave = false;
	
	public int[] purchases;
	public int[] disableSlot;
	public HashMap<String, int[]> playerPurchases;
	public HashMap<String, int[]> playerDisableSlot;
	
	public RoleTrader(EntityNPCInterface npc) {
		super(npc);
		inventoryCurrency = new NpcMiscInventory(36);
		inventorySold = new NpcMiscInventory(18);
		purchases = new int[18];
		disableSlot = new int[18];
		playerPurchases = new HashMap<String, int[]>();
		playerDisableSlot = new HashMap<String, int[]>();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("TraderMarket", marketName);
		writeNBT(nbttagcompound);
        if (toSave && !npc.isRemote()) {
            Market.save(this, marketName);
        }
        toSave = false;
        return nbttagcompound;
	}

	public NBTTagCompound writeNBT(NBTTagCompound nbttagcompound) {
    	nbttagcompound.setTag("TraderCurrency", inventoryCurrency.getToNBT());
    	nbttagcompound.setTag("TraderSold", inventorySold.getToNBT());
        nbttagcompound.setBoolean("TraderIgnoreDamage", ignoreDamage);
        nbttagcompound.setBoolean("TraderIgnoreNBT", ignoreNBT);
        nbttagcompound.setIntArray("Purchases", purchases);
        nbttagcompound.setIntArray("DisableSlot", disableSlot);
        nbttagcompound.setTag("PlayerPurchases", NBTTags.nbtStringIntegerArrayMap(playerPurchases));
        nbttagcompound.setTag("PlayerDisableSlot", NBTTags.nbtStringIntegerArrayMap(playerDisableSlot));
        return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
        marketName = nbttagcompound.getString("TraderMarket");
		readNBT(nbttagcompound);
        
    	try {
			Market.load(this, marketName);
		} catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
			
		}
	}

	public void readNBT(NBTTagCompound nbttagcompound) {
		inventoryCurrency.setFromNBT(nbttagcompound.getCompoundTag("TraderCurrency"));
		inventorySold.setFromNBT(nbttagcompound.getCompoundTag("TraderSold"));
        ignoreDamage = nbttagcompound.getBoolean("TraderIgnoreDamage");
        ignoreNBT = nbttagcompound.getBoolean("TraderIgnoreNBT");
        purchases = nbttagcompound.getIntArray("Purchases");
        if (purchases == null || purchases.length != 18) {
        	purchases = new int[18];
        	for (int i = 0; i < purchases.length; ++i) purchases[i] = 0;
        }
        disableSlot = nbttagcompound.getIntArray("DisableSlot");
        if (disableSlot == null || disableSlot.length != 18) {
        	disableSlot = new int[18];
        	for (int i = 0; i < disableSlot.length; ++i) disableSlot[i] = 0;
        }
        playerPurchases = NBTTags.getStringIntegerArrayMap(nbttagcompound.getTagList("PlayerPurchases", 10), 18);
        playerDisableSlot = NBTTags.getStringIntegerArrayMap(nbttagcompound.getTagList("PlayerDisableSlot", 10), 18);
	}
	
	@Override
	public void interact(EntityPlayer player) {
		npc.say(player, npc.advanced.getInteractLine());
        try {
        	Market.load(this, marketName);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
	}

	public boolean hasCurrency(ItemStack itemstack) {
		if(itemstack == null)
			return false;
		for(ItemStack item : inventoryCurrency.items.values()){
			if(item != null && NoppesUtilPlayer.compareItems(item, itemstack, ignoreDamage, ignoreNBT)){
				return true;
			}
		}
		return false;
	}
	
	public void addPurchase(int slot, String playerName) {
		if(slot >= 18 || slot < 0) return;
		++purchases[slot];
		++getArrayByName(playerName, playerPurchases)[slot];
	}
	
	public boolean isSlotEnabled(int slot, String playerName) {
		if(slot >= 18 || slot < 0) return false;
		if (disableSlot[slot] > 0) return false;
		return getArrayByName(playerName, playerDisableSlot)[slot] <= 0;
	}
	
	public int[] getArrayByName(String name, HashMap<String, int[]> map) {
		map.computeIfAbsent(name, k -> new int[18]);
		return map.get(name);
	}

}
