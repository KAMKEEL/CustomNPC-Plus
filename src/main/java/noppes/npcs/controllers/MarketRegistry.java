package noppes.npcs.controllers;

import foxz.utils.Market;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.roles.RoleTrader;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for linked market synchronization.
 * Tracks which RoleTrader instances use which markets,
 * and which players are viewing each market.
 *
 * This enables immediate sync across all traders sharing
 * the same market when a purchase occurs.
 */
public class MarketRegistry {

    // marketName -> Set of traders using this market (WeakHashMap for auto-cleanup)
    private static final Map<String, Set<RoleTrader>> marketTraders = new ConcurrentHashMap<>();

    // marketName -> Set of players viewing this market (across all NPCs)
    private static final Map<String, Set<EntityPlayerMP>> marketViewers = new ConcurrentHashMap<>();

    // ==================== Trader Registration ====================

    /**
     * Register a trader as using a linked market.
     * Should be called when NPC loads or marketName is set.
     */
    public static void registerTrader(String marketName, RoleTrader trader) {
        if (marketName == null || marketName.isEmpty() || trader == null) return;

        Set<RoleTrader> traders = marketTraders.computeIfAbsent(marketName,
            k -> Collections.newSetFromMap(new WeakHashMap<>()));

        synchronized (traders) {
            traders.add(trader);
        }
    }

    /**
     * Unregister a trader from a linked market.
     * Should be called when NPC unloads or marketName changes.
     */
    public static void unregisterTrader(String marketName, RoleTrader trader) {
        if (marketName == null || marketName.isEmpty() || trader == null) return;

        Set<RoleTrader> traders = marketTraders.get(marketName);
        if (traders != null) {
            synchronized (traders) {
                traders.remove(trader);
                if (traders.isEmpty()) {
                    marketTraders.remove(marketName);
                }
            }
        }
    }

    /**
     * Change a trader's market registration.
     * Handles unregistering from old market and registering to new.
     */
    public static void updateTraderMarket(String oldMarket, String newMarket, RoleTrader trader) {
        if (trader == null) return;

        if (oldMarket != null && !oldMarket.isEmpty()) {
            unregisterTrader(oldMarket, trader);
        }
        if (newMarket != null && !newMarket.isEmpty()) {
            registerTrader(newMarket, trader);
        }
    }

    // ==================== Viewer Registration ====================

    /**
     * Register a player as viewing a market (via any NPC).
     * Should be called when player opens trader GUI with linked market.
     */
    public static void registerViewer(String marketName, EntityPlayerMP player) {
        if (marketName == null || marketName.isEmpty() || player == null) return;

        Set<EntityPlayerMP> viewers = marketViewers.computeIfAbsent(marketName,
            k -> Collections.newSetFromMap(new WeakHashMap<>()));

        synchronized (viewers) {
            viewers.add(player);
        }
    }

    /**
     * Unregister a player from viewing a market.
     * Should be called when player closes trader GUI.
     */
    public static void unregisterViewer(String marketName, EntityPlayerMP player) {
        if (marketName == null || marketName.isEmpty() || player == null) return;

        Set<EntityPlayerMP> viewers = marketViewers.get(marketName);
        if (viewers != null) {
            synchronized (viewers) {
                viewers.remove(player);
                if (viewers.isEmpty()) {
                    marketViewers.remove(marketName);
                }
            }
        }
    }

    // ==================== Synchronization ====================

    /**
     * Sync stock data to all traders and viewers of a market.
     * Called after a purchase to update all linked traders and their viewers.
     *
     * @param marketName The market to sync
     */
    public static void syncMarket(String marketName) {
        if (marketName == null || marketName.isEmpty()) return;

        // Get latest data from cache
        NBTTagCompound marketData = Market.getMarketCache(marketName);
        if (marketData == null) return;

        // Update all traders' internal stock data
        Set<RoleTrader> traders = marketTraders.get(marketName);
        if (traders != null) {
            NBTTagCompound stockData = marketData.getCompoundTag("Stock");

            synchronized (traders) {
                // Clean up any null references (WeakHashMap cleanup)
                traders.removeIf(t -> t == null || t.npc == null);

                for (RoleTrader trader : traders) {
                    // Update stock data from cache
                    if (stockData != null && stockData.hasKey("EnableStock")) {
                        trader.stock.readFromNBT(stockData);
                    }
                }
            }
        }

        // Sync all viewers (across all NPCs for this market)
        syncMarketViewers(marketName);
    }

    /**
     * Sync stock data to all players viewing a specific market.
     * Each viewer gets data relative to their own player (for per-player stock display).
     */
    public static void syncMarketViewers(String marketName) {
        if (marketName == null || marketName.isEmpty()) return;

        Set<EntityPlayerMP> viewers = marketViewers.get(marketName);
        if (viewers == null || viewers.isEmpty()) return;

        // Find any trader with this market to use for sync (they all have same data now)
        Set<RoleTrader> traders = marketTraders.get(marketName);
        if (traders == null || traders.isEmpty()) return;

        RoleTrader syncSource = null;
        synchronized (traders) {
            for (RoleTrader trader : traders) {
                if (trader != null && trader.npc != null) {
                    syncSource = trader;
                    break;
                }
            }
        }

        if (syncSource == null) return;

        // Sync to all viewers
        synchronized (viewers) {
            // Remove disconnected players
            viewers.removeIf(p -> p == null || p.playerNetServerHandler == null);

            for (EntityPlayerMP viewer : viewers) {
                syncSource.syncToPlayer(viewer);
            }
        }
    }

    // ==================== Utility ====================

    /**
     * Get count of traders registered to a market (for debugging/info).
     */
    public static int getTraderCount(String marketName) {
        Set<RoleTrader> traders = marketTraders.get(marketName);
        return traders != null ? traders.size() : 0;
    }

    /**
     * Get count of viewers for a market (for debugging/info).
     */
    public static int getViewerCount(String marketName) {
        Set<EntityPlayerMP> viewers = marketViewers.get(marketName);
        return viewers != null ? viewers.size() : 0;
    }

    /**
     * Clear all registrations (for server shutdown/reload).
     */
    public static void clear() {
        marketTraders.clear();
        marketViewers.clear();
    }
}
