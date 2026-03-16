package noppes.npcs.roles;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import foxz.utils.Market;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.MarketRegistry;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.TraderStock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

    // Currency cost per trade slot (ADDITIVE to item costs, 0 = no currency required)
    public long[] currencyCost = new long[18];

    // Local viewer tracking for non-linked traders (per-NPC sync)
    private final Set<IPlayer> localViewers = new HashSet<>();

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
    public INbt writeToNBT(INbt INbt) {
        INbt.setString("TraderMarket", marketName);
        writeNBT(INbt);
        return INbt;
    }

    public INbt writeNBT(INbt INbt) {
        INbt.setTag("TraderCurrency", inventoryCurrency.getToNBT());
        INbt.setTag("TraderSold", inventorySold.getToNBT());
        INbt.setBoolean("TraderIgnoreDamage", ignoreDamage);
        INbt.setBoolean("TraderIgnoreNBT", ignoreNBT);
        INbt.setBoolean("RecordHistory", recordHistory);
        INbt.setIntArray("DisableSlot", disableSlot);
        INbt.setTag("PlayerDisableSlot", NBTTags.nbtStringIntegerArrayMap(playerDisableSlot));
        if (recordHistory) {
            INbt.setIntArray("Purchases", purchases);
            INbt.setTag("PlayerPurchases", NBTTags.nbtStringIntegerArrayMap(playerPurchases));
        }

        // Stock system
        INbt.setTag("Stock", ((NBTWrapper) stock.writeToNBT(new NBTWrapper(new INbt()))).getMCTag());

        // Currency cost per slot (additive to item costs)
        INbtList currencyList = new INbtList();
        for (long cost : currencyCost) {
            INbt costTag = new INbt();
            costTag.setLong("Cost", cost);
            currencyList.appendTag(costTag);
        }
        INbt.setTag("CurrencyCost", currencyList);

        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        String oldMarket = marketName;
        marketName = INbt.getString("TraderMarket");
        readNBT(INbt);

        // Register with MarketRegistry for linked market sync
        MarketRegistry.updateTraderMarket(oldMarket, marketName, this);

        try {
            Market.getMarket(this, marketName);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readNBT(INbt INbt) {
        inventoryCurrency.setFromNBT(INbt.getCompoundTag("TraderCurrency"));
        inventorySold.setFromNBT(INbt.getCompoundTag("TraderSold"));
        ignoreDamage = INbt.getBoolean("TraderIgnoreDamage");
        ignoreNBT = INbt.getBoolean("TraderIgnoreNBT");
        recordHistory = INbt.getBoolean("RecordHistory");
        disableSlot = INbt.getIntArray("DisableSlot");
        playerDisableSlot = NBTTags.getStringIntegerArrayMap(INbt.getTagList("PlayerDisableSlot", 10), 18);
        if (recordHistory) {
            purchases = INbt.getIntArray("Purchases");
            playerPurchases = NBTTags.getStringIntegerArrayMap(INbt.getTagList("PlayerPurchases", 10), 18);
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
        if (INbt.hasKey("Stock")) {
            stock.readFromNBT(new NBTWrapper(INbt.getCompoundTag("Stock")));
        }

        // Currency cost per slot (additive to item costs)
        if (INbt.hasKey("CurrencyCost")) {
            INbtList currencyList = INbt.getTagList("CurrencyCost", 10);
            for (int i = 0; i < currencyList.tagCount() && i < 18; i++) {
                currencyCost[i] = currencyList.getCompoundTagAt(i).getLong("Cost");
            }
        }
    }

    @Override
    public void interact(IPlayer player) {
        npc.say(player, npc.advanced.getInteractLine());

        // Load from market FIRST to get latest shared data
        try {
            Market.getMarket(this, marketName);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        // THEN check for stock reset (after loading latest market data)
        if (stock.enableStock) {
            long currentTime = stock.resetType.isRealTime()
                ? System.currentTimeMillis()
                : player.worldObj.getTotalWorldTime();

            boolean needsSave = false;

            if (stock.shouldReset(currentTime)) {
                stock.resetStock(currentTime);
                needsSave = true;
            }

            // Validate stock levels (clamp current to max if exceeded)
            if (stock.validateStock()) {
                needsSave = true;
            }

            // Save to market if using shared market and changes were made
            if (needsSave && !marketName.isEmpty()) {
                Market.save(this, marketName);
            }
        }

        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
    }

    public boolean hasCurrency(IItemStack IItemStack) {
        if (IItemStack == null)
            return false;
        for (IItemStack item : inventoryCurrency.items.values()) {
            if (item != null && NoppesUtilPlayer.compareItems(item, IItemStack, ignoreDamage, ignoreNBT)) {
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
     * Consume stock after purchase.
     * For shared stock, syncs all viewers (market-wide for linked markets, per-NPC otherwise).
     *
     * @param slot       The slot purchased from
     * @param playerName The player making the purchase
     * @param amount     Amount to consume
     * @return true if stock was consumed successfully
     */
    public boolean consumeStock(int slot, String playerName, int amount) {
        if (!stock.enableStock) {
            return true;  // Stock system disabled
        }
        boolean consumed = stock.consumeStock(slot, playerName, amount);
        if (consumed) {
            // Save to market if linked
            if (!marketName.isEmpty()) {
                Market.save(this, marketName);
            }

            // Sync viewers for shared stock (not per-player)
            if (!stock.perPlayer) {
                if (!marketName.isEmpty()) {
                    // Linked market: sync all traders and viewers market-wide
                    MarketRegistry.syncMarket(marketName);
                } else {
                    // No linked market: sync only this NPC's viewers
                    syncLocalViewers();
                }
            }
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

    /**
     * Get time until stock reset in real-time milliseconds.
     * Converts MC ticks to approximate real-time for client countdown display.
     *
     * @return Remaining time in milliseconds, or -1 if no reset scheduled
     */
    public long getResetTimeRemainingMillis() {
        if (!stock.enableStock || stock.resetType == noppes.npcs.constants.EnumStockReset.NONE) {
            return -1;
        }

        long currentTime;
        long remaining;

        if (stock.resetType.isRealTime()) {
            // Real time - already in milliseconds
            currentTime = System.currentTimeMillis();
            remaining = stock.getTimeUntilReset(currentTime);
        } else {
            // MC time - convert ticks to real milliseconds (1 tick = 50ms)
            currentTime = (npc != null && npc.worldObj != null ? npc.worldObj.getTotalWorldTime() : 0);
            long remainingTicks = stock.getTimeUntilReset(currentTime);
            remaining = remainingTicks * 50L;
        }

        return remaining;
    }

    /**
     * Reset the stock cooldown timer (for admin use)
     */
    public void resetCooldown() {
        stock.lastResetTime = 0;
    }

    // ==================== Currency Cost Methods ====================

    /**
     * Get the currency cost for a slot (additive to item costs)
     */
    public long getCurrencyCost(int slot) {
        if (slot < 0 || slot >= 18) return 0;
        return currencyCost[slot];
    }

    /**
     * Set the currency cost for a slot (additive to item costs)
     */
    public void setCurrencyCost(int slot, long cost) {
        if (slot >= 0 && slot < 18) {
            currencyCost[slot] = Math.max(0, cost);
        }
    }

    /**
     * Check if a slot has a currency cost (greater than 0)
     */
    public boolean hasCurrencyCost(int slot) {
        return slot >= 0 && slot < 18 && currencyCost[slot] > 0;
    }

    // ==================== Viewer Tracking Methods ====================

    /**
     * Register a player as viewing this trader's GUI.
     * - For linked markets: uses MarketRegistry (market-wide sync)
     * - For normal traders: uses local viewer set (per-NPC sync)
     */
    public void registerViewer(IPlayer player) {
        if (player == null) return;

        if (!marketName.isEmpty()) {
            // Linked market: use MarketRegistry for market-wide tracking
            MarketRegistry.registerViewer(marketName, player);
        } else {
            // No linked market: use local viewer set
            localViewers.add(player);
        }
    }

    /**
     * Unregister a player from viewing this trader's GUI.
     * - For linked markets: uses MarketRegistry
     * - For normal traders: uses local viewer set
     */
    public void unregisterViewer(IPlayer player) {
        if (player == null) return;

        if (!marketName.isEmpty()) {
            MarketRegistry.unregisterViewer(marketName, player);
        } else {
            localViewers.remove(player);
        }
    }

    /**
     * Sync trader data to all players viewing this trader.
     * - For linked markets: syncs ALL viewers across ALL NPCs sharing the market
     * - For normal traders: syncs only viewers of THIS specific NPC
     */
    public void syncAllViewers() {
        if (!marketName.isEmpty()) {
            // Linked market: sync all viewers across all NPCs with this market
            MarketRegistry.syncMarketViewers(marketName);
        } else {
            // No linked market: sync only local viewers
            syncLocalViewers();
        }
    }

    /**
     * Sync to all players viewing this specific NPC (for non-linked traders).
     */
    private void syncLocalViewers() {
        // Remove disconnected players
        localViewers.removeIf(player -> player.playerNetServerHandler == null);

        for (IPlayer viewer : localViewers) {
            syncToPlayer(viewer);
        }
    }

    /**
     * Called when this trader is unloaded (NPC death, chunk unload, etc.)
     * Cleans up MarketRegistry registration.
     */
    public void onUnload() {
        if (!marketName.isEmpty()) {
            MarketRegistry.unregisterTrader(marketName, this);
        }
        localViewers.clear();
    }

    /**
     * Send trader data to a specific player
     */
    public void syncToPlayer(IPlayer player) {
        if (player == null || player.playerNetServerHandler == null) {
            return;
        }

        INbt compound = new INbt();

        // Player balance
        PlayerData data = PlayerData.get(player);
        compound.setLong("Balance", data.tradeData.getBalance());

        // Stock info
        compound.setBoolean("StockEnabled", stock.enableStock);
        compound.setString("ResetTime", getTimeUntilResetFormatted());

        // Available stock per slot (player-specific if perPlayer mode)
        String playerName = player.getCommandSenderName();
        int[] stockArr = new int[18];
        for (int i = 0; i < 18; i++) {
            stockArr[i] = getAvailableStock(i, playerName);
        }
        compound.setIntArray("Stock", stockArr);

        // Currency cost per slot
        for (int i = 0; i < 18; i++) {
            compound.setLong("Cost" + i, getCurrencyCost(i));
        }

        GuiDataPacket.sendGuiData(player, compound);
    }

    @Override
    public void delete() {
        onUnload();
        super.delete();
    }
}
