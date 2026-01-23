package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumStockReset;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages trader stock system including:
 * - Per-slot stock limits
 * - Per-player vs per-server stock tracking
 * - Automatic stock reset timers
 */
public class TraderStock {
    // Stock configuration
    public boolean enableStock = false;
    public boolean perPlayer = false;  // false = per-server, true = per-player

    // Reset configuration (mirrors Quest cooldown system)
    public EnumStockReset resetType = EnumStockReset.NONE;
    public long customResetTime = 0;  // For MCCUSTOM/RLCUSTOM types (in ticks or ms)

    // Stock quantities per slot (max stock, -1 = unlimited)
    public int[] maxStock = new int[18];

    // Current stock (per-server mode)
    public int[] currentStock = new int[18];
    public long lastResetTime = 0;

    // Per-player stock tracking
    public HashMap<String, PlayerTraderStock> playerStock = new HashMap<>();

    public TraderStock() {
        for (int i = 0; i < 18; i++) {
            maxStock[i] = -1;  // -1 = unlimited
            currentStock[i] = -1;
        }
    }

    /**
     * Get available stock for a slot
     *
     * @param slot       The slot index
     * @param playerName The player name (for per-player mode)
     * @return Available stock count, or Integer.MAX_VALUE if unlimited
     */
    public synchronized int getAvailableStock(int slot, String playerName) {
        if (!enableStock || slot < 0 || slot >= 18 || maxStock[slot] < 0) {
            return Integer.MAX_VALUE;  // Unlimited
        }

        if (perPlayer) {
            PlayerTraderStock pStock = playerStock.get(playerName);
            if (pStock == null) {
                return maxStock[slot];  // Not purchased yet
            }
            return pStock.getStock(slot, maxStock[slot]);
        }

        if (currentStock[slot] < 0) {
            return maxStock[slot];
        }
        return Math.max(0, currentStock[slot]);
    }

    /**
     * Check if stock is available for purchase
     */
    public boolean hasStock(int slot, String playerName, int amount) {
        return getAvailableStock(slot, playerName) >= amount;
    }

    /**
     * Consume stock when purchase is made
     *
     * @return true if successful, false if insufficient stock
     */
    public synchronized boolean consumeStock(int slot, String playerName, int amount) {
        if (!enableStock || slot < 0 || slot >= 18 || maxStock[slot] < 0) {
            return true;  // Unlimited, always succeeds
        }

        if (perPlayer) {
            PlayerTraderStock pStock = playerStock.computeIfAbsent(
                playerName, k -> new PlayerTraderStock());
            return pStock.consumeStock(slot, maxStock[slot], amount);
        }

        if (currentStock[slot] < 0) {
            currentStock[slot] = maxStock[slot];
        }
        if (currentStock[slot] >= amount) {
            currentStock[slot] -= amount;
            return true;
        }
        return false;
    }

    /**
     * Check if stock should reset based on current time
     *
     * @param currentTime Current MC world time (ticks) or real time (ms) depending on reset type
     */
    public boolean shouldReset(long currentTime) {
        if (resetType == EnumStockReset.NONE) {
            return false;
        }

        long elapsed = currentTime - lastResetTime;
        long resetInterval = getResetInterval();

        return elapsed >= resetInterval;
    }

    /**
     * Reset all stock to max values
     *
     * @param currentTime Current time to record as reset time
     */
    public synchronized void resetStock(long currentTime) {
        lastResetTime = currentTime;

        // Reset per-server stock
        for (int i = 0; i < 18; i++) {
            currentStock[i] = maxStock[i];
        }

        // Clear per-player stock (cleanup per-player data on reset)
        playerStock.clear();
    }

    /**
     * Get reset interval based on reset type
     *
     * @return Interval in ticks (MC time) or milliseconds (real time)
     */
    public long getResetInterval() {
        switch (resetType) {
            case MCDAILY:
                return 24000;  // 1 MC day in ticks
            case MCWEEKLY:
                return 168000; // 7 MC days in ticks
            case MCCUSTOM:
                return customResetTime;  // Custom ticks
            case RLDAILY:
                return 86400000L;  // 24 hours in ms
            case RLWEEKLY:
                return 604800000L; // 7 days in ms
            case RLCUSTOM:
                return customResetTime;  // Custom ms
            default:
                return Long.MAX_VALUE;
        }
    }

    /**
     * Get time until next reset
     *
     * @param currentTime Current time
     * @return Time remaining until reset, or -1 if no reset scheduled
     */
    public long getTimeUntilReset(long currentTime) {
        if (resetType == EnumStockReset.NONE) {
            return -1;
        }
        long elapsed = currentTime - lastResetTime;
        long interval = getResetInterval();
        return Math.max(0, interval - elapsed);
    }

    /**
     * Format time until reset for display
     *
     * @param currentTime Current time
     * @return Formatted string like "2 hours" or "1 day, 5 hours"
     */
    public String getTimeUntilResetFormatted(long currentTime) {
        long remaining = getTimeUntilReset(currentTime);
        if (remaining < 0) {
            return "";
        }

        if (resetType.isRealTime()) {
            // Convert ms to readable format
            long hours = remaining / 3600000L;
            long days = hours / 24;
            hours = hours % 24;

            if (days > 0) {
                return days + " day" + (days != 1 ? "s" : "") +
                    (hours > 0 ? ", " + hours + " hour" + (hours != 1 ? "s" : "") : "");
            } else if (hours > 0) {
                return hours + " hour" + (hours != 1 ? "s" : "");
            } else {
                long minutes = remaining / 60000L;
                return minutes + " minute" + (minutes != 1 ? "s" : "");
            }
        } else {
            // Convert ticks to MC time format
            long mcHours = remaining / 1000;  // 1000 ticks = 1 MC hour
            long mcDays = mcHours / 24;
            mcHours = mcHours % 24;

            if (mcDays > 0) {
                return mcDays + " MC day" + (mcDays != 1 ? "s" : "") +
                    (mcHours > 0 ? ", " + mcHours + " MC hour" + (mcHours != 1 ? "s" : "") : "");
            } else {
                return mcHours + " MC hour" + (mcHours != 1 ? "s" : "");
            }
        }
    }

    /**
     * Set stock for a specific slot
     */
    public void setMaxStock(int slot, int amount) {
        if (slot >= 0 && slot < 18) {
            maxStock[slot] = amount;
            if (currentStock[slot] < 0 || currentStock[slot] > amount) {
                currentStock[slot] = amount;
            }
        }
    }

    /**
     * Validate and clamp all current stock values to not exceed max stock.
     * Should be called when trader GUI opens to ensure consistency.
     *
     * @return true if any stock was clamped
     */
    public boolean validateStock() {
        boolean changed = false;
        for (int i = 0; i < 18; i++) {
            if (maxStock[i] >= 0) {
                // Has a max limit
                if (currentStock[i] < 0) {
                    // Not initialized, set to max
                    currentStock[i] = maxStock[i];
                    changed = true;
                } else if (currentStock[i] > maxStock[i]) {
                    // Exceeds max, clamp down
                    currentStock[i] = maxStock[i];
                    changed = true;
                }
            }
        }

        // Also validate per-player stock
        for (PlayerTraderStock pStock : playerStock.values()) {
            for (int i = 0; i < 18; i++) {
                if (maxStock[i] >= 0) {
                    int available = pStock.getStock(i, maxStock[i]);
                    if (available < 0) {
                        // Over-purchased somehow, reset
                        pStock.purchasedAmounts[i] = maxStock[i];
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    // ==================== NBT Serialization ====================

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("EnableStock", enableStock);
        compound.setBoolean("PerPlayer", perPlayer);
        compound.setInteger("ResetType", resetType.ordinal());
        compound.setLong("CustomResetTime", customResetTime);
        compound.setIntArray("MaxStock", maxStock);
        compound.setIntArray("CurrentStock", currentStock);
        compound.setLong("LastResetTime", lastResetTime);

        // Save per-player stock
        NBTTagList playerList = new NBTTagList();
        for (Map.Entry<String, PlayerTraderStock> entry : playerStock.entrySet()) {
            NBTTagCompound playerTag = new NBTTagCompound();
            playerTag.setString("Player", entry.getKey());
            entry.getValue().writeToNBT(playerTag);
            playerList.appendTag(playerTag);
        }
        compound.setTag("PlayerStock", playerList);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        enableStock = compound.getBoolean("EnableStock");
        perPlayer = compound.getBoolean("PerPlayer");

        int resetOrdinal = compound.getInteger("ResetType");
        if (resetOrdinal >= 0 && resetOrdinal < EnumStockReset.values().length) {
            resetType = EnumStockReset.values()[resetOrdinal];
        }

        customResetTime = compound.getLong("CustomResetTime");

        int[] loadedMax = compound.getIntArray("MaxStock");
        int[] loadedCurrent = compound.getIntArray("CurrentStock");

        if (loadedMax != null && loadedMax.length == 18) {
            maxStock = loadedMax;
        }
        if (loadedCurrent != null && loadedCurrent.length == 18) {
            currentStock = loadedCurrent;
        }

        lastResetTime = compound.getLong("LastResetTime");

        // Load per-player stock
        playerStock.clear();
        NBTTagList playerList = compound.getTagList("PlayerStock", 10);
        for (int i = 0; i < playerList.tagCount(); i++) {
            NBTTagCompound playerTag = playerList.getCompoundTagAt(i);
            String playerName = playerTag.getString("Player");
            PlayerTraderStock pStock = new PlayerTraderStock();
            pStock.readFromNBT(playerTag);
            playerStock.put(playerName, pStock);
        }
    }

    // ==================== Inner Class for Per-Player Stock ====================

    public static class PlayerTraderStock {
        public int[] purchasedAmounts = new int[18];

        public PlayerTraderStock() {
            for (int i = 0; i < 18; i++) {
                purchasedAmounts[i] = 0;
            }
        }

        public int getStock(int slot, int maxStock) {
            if (slot < 0 || slot >= 18) return 0;
            return Math.max(0, maxStock - purchasedAmounts[slot]);
        }

        public boolean consumeStock(int slot, int maxStock, int amount) {
            if (slot < 0 || slot >= 18) return false;
            if (getStock(slot, maxStock) >= amount) {
                purchasedAmounts[slot] += amount;
                return true;
            }
            return false;
        }

        public void writeToNBT(NBTTagCompound compound) {
            compound.setIntArray("Purchased", purchasedAmounts);
        }

        public void readFromNBT(NBTTagCompound compound) {
            int[] loaded = compound.getIntArray("Purchased");
            if (loaded != null && loaded.length == 18) {
                purchasedAmounts = loaded;
            }
        }
    }
}
