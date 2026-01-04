package noppes.npcs.roles;

import foxz.utils.Market;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.TraderStock;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoleTrader extends RoleInterface {

    public String marketName = "";
    public NpcMiscInventory inventoryCurrency;
    public NpcMiscInventory inventorySold;

    public boolean ignoreDamage = false;
    public boolean ignoreNBT = false;
    public boolean recordHistory = false;

    public int[] purchases;
    public int[] disableSlot;
    public HashMap<String, int[]> playerPurchases;
    public HashMap<String, int[]> playerDisableSlot;

    // Stock system
    public TraderStock stock = new TraderStock();

    // Currency system
    public boolean useCurrency = false;       // Use virtual currency instead of items
    public long[] slotPrices = new long[18];  // Currency prices per slot

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
        return nbttagcompound;
    }

    public NBTTagCompound writeNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setTag("TraderCurrency", inventoryCurrency.getToNBT());
        nbttagcompound.setTag("TraderSold", inventorySold.getToNBT());
        nbttagcompound.setBoolean("TraderIgnoreDamage", ignoreDamage);
        nbttagcompound.setBoolean("TraderIgnoreNBT", ignoreNBT);
        nbttagcompound.setBoolean("RecordHistory", recordHistory);
        nbttagcompound.setIntArray("DisableSlot", disableSlot);
        nbttagcompound.setTag("PlayerDisableSlot", NBTTags.nbtStringIntegerArrayMap(playerDisableSlot));
        if (recordHistory) {
            nbttagcompound.setIntArray("Purchases", purchases);
            nbttagcompound.setTag("PlayerPurchases", NBTTags.nbtStringIntegerArrayMap(playerPurchases));
        }

        // Stock system
        nbttagcompound.setTag("Stock", stock.writeToNBT(new NBTTagCompound()));

        // Currency system
        nbttagcompound.setBoolean("UseCurrency", useCurrency);
        NBTTagList priceList = new NBTTagList();
        for (long price : slotPrices) {
            NBTTagCompound priceTag = new NBTTagCompound();
            priceTag.setLong("Price", price);
            priceList.appendTag(priceTag);
        }
        nbttagcompound.setTag("SlotPrices", priceList);

        return nbttagcompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        marketName = nbttagcompound.getString("TraderMarket");
        readNBT(nbttagcompound);

        try {
            Market.getMarket(this, marketName);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readNBT(NBTTagCompound nbttagcompound) {
        inventoryCurrency.setFromNBT(nbttagcompound.getCompoundTag("TraderCurrency"));
        inventorySold.setFromNBT(nbttagcompound.getCompoundTag("TraderSold"));
        ignoreDamage = nbttagcompound.getBoolean("TraderIgnoreDamage");
        ignoreNBT = nbttagcompound.getBoolean("TraderIgnoreNBT");
        recordHistory = nbttagcompound.getBoolean("RecordHistory");
        disableSlot = nbttagcompound.getIntArray("DisableSlot");
        playerDisableSlot = NBTTags.getStringIntegerArrayMap(nbttagcompound.getTagList("PlayerDisableSlot", 10), 18);
        if (recordHistory) {
            purchases = nbttagcompound.getIntArray("Purchases");
            playerPurchases = NBTTags.getStringIntegerArrayMap(nbttagcompound.getTagList("PlayerPurchases", 10), 18);
        }
        if (purchases == null || purchases.length != 18) {
            purchases = new int[18];
            for (int i = 0; i < purchases.length; ++i) purchases[i] = 0;
        }
        if (disableSlot == null || disableSlot.length != 18) {
            disableSlot = new int[18];
            for (int i = 0; i < disableSlot.length; ++i) disableSlot[i] = 0;
        }

        // Stock system
        if (nbttagcompound.hasKey("Stock")) {
            stock.readFromNBT(nbttagcompound.getCompoundTag("Stock"));
        }

        // Currency system
        useCurrency = nbttagcompound.getBoolean("UseCurrency");
        if (nbttagcompound.hasKey("SlotPrices")) {
            NBTTagList priceList = nbttagcompound.getTagList("SlotPrices", 10);
            for (int i = 0; i < priceList.tagCount() && i < 18; i++) {
                slotPrices[i] = priceList.getCompoundTagAt(i).getLong("Price");
            }
        }
    }

    @Override
    public void interact(EntityPlayer player) {
        // Check for stock reset before opening GUI
        if (stock.enableStock) {
            long currentTime = stock.resetType.isRealTime()
                ? System.currentTimeMillis()
                : player.worldObj.getTotalWorldTime();

            if (stock.shouldReset(currentTime)) {
                stock.resetStock(currentTime);
                // Save to market if using shared market
                if (!marketName.isEmpty()) {
                    Market.save(this, marketName);
                }
            }
        }

        npc.say(player, npc.advanced.getInteractLine());
        try {
            Market.getMarket(this, marketName);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
    }

    public boolean hasCurrency(ItemStack itemstack) {
        if (itemstack == null)
            return false;
        for (ItemStack item : inventoryCurrency.items.values()) {
            if (item != null && NoppesUtilPlayer.compareItems(item, itemstack, ignoreDamage, ignoreNBT)) {
                return true;
            }
        }
        return false;
    }

    public void addPurchase(int slot, String playerName) {
        if (slot >= 18 || slot < 0) return;
        ++purchases[slot];
        ++getArrayByName(playerName, playerPurchases)[slot];

        if (recordHistory)
            Market.save(this, marketName);
    }

    public boolean isSlotEnabled(int slot, String playerName) {
        if (slot >= 18 || slot < 0) return false;
        if (disableSlot[slot] > 0) return false;
        return getArrayByName(playerName, playerDisableSlot)[slot] <= 0;
    }

    public int[] getArrayByName(String name, HashMap<String, int[]> map) {
        map.computeIfAbsent(name, k -> new int[18]);
        return map.get(name);
    }

    // ==================== Stock System Methods ====================

    /**
     * Check if there is stock available for a slot
     */
    public boolean hasStock(int slot, String playerName, int amount) {
        if (!stock.enableStock) {
            return true;  // Stock system disabled, always available
        }
        return stock.hasStock(slot, playerName, amount);
    }

    /**
     * Get available stock for a slot
     */
    public int getAvailableStock(int slot, String playerName) {
        if (!stock.enableStock) {
            return Integer.MAX_VALUE;  // Unlimited
        }
        return stock.getAvailableStock(slot, playerName);
    }

    /**
     * Consume stock after purchase
     */
    public boolean consumeStock(int slot, String playerName, int amount) {
        if (!stock.enableStock) {
            return true;  // Stock system disabled
        }
        boolean consumed = stock.consumeStock(slot, playerName, amount);
        if (consumed && !marketName.isEmpty()) {
            Market.save(this, marketName);
        }
        return consumed;
    }

    /**
     * Get time until stock reset formatted for display
     */
    public String getTimeUntilResetFormatted() {
        if (!stock.enableStock || stock.resetType == noppes.npcs.constants.EnumStockReset.NONE) {
            return "";
        }
        long currentTime = stock.resetType.isRealTime()
            ? System.currentTimeMillis()
            : (npc != null && npc.worldObj != null ? npc.worldObj.getTotalWorldTime() : 0);
        return stock.getTimeUntilResetFormatted(currentTime);
    }

    // ==================== Currency System Methods ====================

    /**
     * Get the currency price for a slot
     */
    public long getSlotPrice(int slot) {
        if (slot < 0 || slot >= 18) return 0;
        return slotPrices[slot];
    }

    /**
     * Set the currency price for a slot
     */
    public void setSlotPrice(int slot, long price) {
        if (slot >= 0 && slot < 18) {
            slotPrices[slot] = Math.max(0, price);
        }
    }

    /**
     * Check if a slot has a currency price set
     */
    public boolean hasSlotPrice(int slot) {
        return useCurrency && slot >= 0 && slot < 18 && slotPrices[slot] > 0;
    }
}
