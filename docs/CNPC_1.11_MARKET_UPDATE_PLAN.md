# CustomNPC+ 1.11 Market Update - Implementation Plan

## Overview

This document outlines the implementation plan for the CustomNPC+ 1.11 update, focusing on three major features:

1. **Auction House** - A new NPC role allowing players to access a global auction system
2. **Currency System** - Built-in currency integration with VaultAPI for Bukkit hybrid servers
3. **Trader Enhancements** - Stock/Inventory system with per-player/per-server options and reset timers

---

## Design Decisions (Confirmed)

### 1. Auction Refund System
**Decision:** When a player is outbid, the refund goes to **claims** (not direct to balance).
- Players can manage their bids from claims
- If auction ends while player is offline, they can claim their money later
- This keeps the flow consistent with item claims
- **Modify Bid Feature**: Players can use a "Modify Bid" button that auto-reclaims their money and applies it to a higher bid amount

### 2. Permission System
**Decision:** Use the **existing CustomNPC+ permission system** used for Profiles.
- Pattern: `customnpcs.auction.slots.X` where X is the max slot count
- Implementation mirrors `ProfileController.allowSlotPermission()` approach
- Uses `CustomNpcsPermissions.hasCustomPermission(player, permissionString)`
- **Default**: 5 auction slots per player (configurable)

### 3. Virtual Currency Storage
**Decision:** Currency is **NOT profile slot bound** - all profile slots share the same currency pool per player.
- Stored in PlayerData outside of slot components
- Balance persists across profile slot switches
- Balance viewing system to be added later (for now, visible via GUI only)

### 4. Currency Display Icon
**Decision:** Use the **existing gold coin** from the mod (`CustomItems.coinGold`).
- Already has textures and localization for all supported languages
- Available coin types: Wood, Stone, Bronze, Iron, Gold, Diamond, Emerald

### 5. Trader Stock Reset Timer Display
**Decision:** Show "Stocks reset in X hours" as a **banner ABOVE the GUI**.
- Positioned at a higher Y level than the main GUI
- Not overlayed on the GUI content
- Displayed when opening the trader interface

### 6. Item Trading Restrictions
**Decision:** Implement the following restrictions for auction listings:
- **Untradeable Attribute**: New item attribute (`CNPC_Untradeable`) that prevents auction listing
- **ProfileSlot Bound Items**: Use existing `cnpc_profile_slot` NBT tag (from `ProfileSlotRequirement.java`)
- **Soulbound Items**: Use existing `cnpc_soulbind` NBT tag (from `SoulbindRequirement.java`)
- **Auction House Blacklist**: Configurable list of items that cannot be auctioned

### 7. Admin Auction Management
**Decision:** Create an **Auction Management system** with a Global Admin button.
- Admins can modify, remove, and edit auction listings in real-time
- Accessible via special admin GUI
- Permission-gated for server operators

### 8. Auction Timing & Bidding Rules
**Decision:** Configurable auction timing with snipe protection.
- **Claim Expiration**: 30 days by default (configurable) - claims auto-expire after this period
- **Bid Increment**: Percentage-based minimum increment (configurable, e.g., 5%)
- **Snipe Protection**: When a bid is placed near auction end, extend time TO 5 minutes remaining (never higher)
  - Extension amount is configurable
  - Example: If 2 minutes left and bid placed, extend to 5 minutes. If 10 minutes left, no extension.
- **Auction Durations**: Configurable default options (2h, 12h, 24h, 48h)

---

## Table of Contents

1. [Design Decisions](#design-decisions-confirmed)
2. [Architecture Overview](#architecture-overview)
3. [Feature 1: Currency System](#feature-1-currency-system)
4. [Feature 2: Trader Enhancements](#feature-2-trader-enhancements)
5. [Feature 3: Auction House](#feature-3-auction-house)
6. [Feature 4: Item Attributes & Restrictions](#feature-4-item-attributes--restrictions)
7. [Feature 5: Admin Auction Management](#feature-5-admin-auction-management)
8. [Configuration Reference](#configuration-reference)
9. [Implementation Order](#implementation-order)
10. [File Summary](#file-summary)
11. [Testing Strategy](#testing-strategy)

---

## Architecture Overview

### Current Codebase Patterns

The implementation will follow existing patterns found in the codebase:

| Pattern | Used By | Will Be Applied To |
|---------|---------|-------------------|
| Singleton Controller | `BankController`, `QuestController` | `AuctionController`, `CurrencyController` |
| Role Interface | `RoleTrader`, `RoleBank` | `RoleAuctioneer` |
| Player Data Extension | `PlayerBankData`, `PlayerQuestData` | `PlayerAuctionData`, `PlayerCurrencyData` |
| Cooldown System | `Quest.repeat`, `JobItemGiver` | Trader Stock Reset |
| Optional Addon | `PixelmonHelper`, `DBCAddon` | `VaultHelper` |
| NBT Serialization | All data classes | All new data classes |
| Cache System | `Market.java`, `CacheHashMap` | Auction listings cache |

### Package Structure for New Features

```
noppes/npcs/
├── controllers/
│   ├── AuctionController.java          # Manages global auction house
│   ├── CurrencyController.java         # Manages currency settings
│   └── data/
│       ├── AuctionListing.java         # Single auction listing
│       ├── AuctionData.java            # World auction data
│       ├── PlayerAuctionData.java      # Per-player auction history
│       ├── PlayerCurrencyData.java     # Per-player currency balance
│       └── TraderStock.java            # Trader stock configuration
├── roles/
│   └── RoleAuctioneer.java             # Auctioneer NPC role
├── containers/
│   ├── ContainerAuction*.java          # Auction GUI containers
│   └── ContainerNPCTraderStock.java    # Stock management container
├── client/gui/
│   ├── player/
│   │   └── GuiAuction*.java            # Player-facing auction GUIs
│   └── roles/
│       └── GuiNpcAuctioneerSetup.java  # Auctioneer configuration GUI

kamkeel/npcs/
├── controllers/
│   └── CurrencyController.java         # Currency management
├── network/packets/
│   ├── data/
│   │   ├── AuctionSyncPacket.java      # Sync auction data
│   │   └── CurrencySyncPacket.java     # Sync currency data
│   └── player/
│       ├── AuctionActionPacket.java    # Auction actions (bid, buy, sell)
│       └── CurrencyActionPacket.java   # Currency transactions
└── compat/
    └── VaultHelper.java                # VaultAPI integration
```

---

## Feature 1: Currency System

### 1.1 Overview

The currency system provides a unified economy layer that can:
- Use VaultAPI when available on Bukkit hybrid servers (Cauldron, Thermos, Mohist)
- Fall back to an internal item-based currency when VaultAPI is unavailable
- Be configured per-world or globally

### 1.2 VaultAPI Integration

#### 1.2.1 Dependency Setup

**File: `dependencies.gradle`**
```gradle
// VaultAPI - compile-only for optional integration
compileOnly("net.milkbowl.vault:VaultAPI:1.4") {
    transitive = false
}
```

**File: `repositories.gradle`**
```gradle
maven {
    name = "vault-repo"
    url = "https://nexus.hc.to/content/repositories/pub_releases/"
}
```

> **Note:** VaultAPI 1.4.1 works with 1.7.10 Bukkit hybrid servers. The dependency is compile-only so the mod works without Vault present.

#### 1.2.2 VaultHelper Class

**File: `src/main/java/kamkeel/npcs/compat/VaultHelper.java`**

```java
package kamkeel.npcs.compat;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.LogWriter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHelper {
    public static boolean Enabled = false;
    public static boolean Available = false;

    // Cached via reflection
    private static Object economy = null;
    private static Class<?> economyClass = null;
    private static java.lang.reflect.Method getBalance;
    private static java.lang.reflect.Method withdrawPlayer;
    private static java.lang.reflect.Method depositPlayer;
    private static java.lang.reflect.Method hasMethod;
    private static java.lang.reflect.Method format;

    public static void load() {
        // Check if we're on a Bukkit hybrid server
        try {
            Class.forName("org.bukkit.Bukkit");
            Available = true;
        } catch (ClassNotFoundException e) {
            Available = false;
            return;
        }

        // Try to hook into Vault
        try {
            Class<?> vaultClass = Class.forName("net.milkbowl.vault.economy.Economy");
            economyClass = vaultClass;

            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager()
                .getRegistration(vaultClass);

            if (rsp != null) {
                economy = rsp.getProvider();

                // Cache reflection methods
                getBalance = economyClass.getMethod("getBalance", String.class);
                withdrawPlayer = economyClass.getMethod("withdrawPlayer",
                    String.class, double.class);
                depositPlayer = economyClass.getMethod("depositPlayer",
                    String.class, double.class);
                hasMethod = economyClass.getMethod("has", String.class, double.class);
                format = economyClass.getMethod("format", double.class);

                Enabled = true;
                LogWriter.info("VaultAPI Economy hooked successfully!");
            }
        } catch (Exception e) {
            LogWriter.info("VaultAPI not available: " + e.getMessage());
            Enabled = false;
        }
    }

    // Economy Methods
    public static double getBalance(EntityPlayer player) {
        if (!Enabled) return 0.0;
        try {
            return (Double) getBalance.invoke(economy, player.getCommandSenderName());
        } catch (Exception e) {
            LogWriter.except(e);
            return 0.0;
        }
    }

    public static boolean withdraw(EntityPlayer player, double amount) {
        if (!Enabled) return false;
        try {
            Object response = withdrawPlayer.invoke(economy,
                player.getCommandSenderName(), amount);
            return (Boolean) response.getClass()
                .getMethod("transactionSuccess").invoke(response);
        } catch (Exception e) {
            LogWriter.except(e);
            return false;
        }
    }

    public static boolean deposit(EntityPlayer player, double amount) {
        if (!Enabled) return false;
        try {
            Object response = depositPlayer.invoke(economy,
                player.getCommandSenderName(), amount);
            return (Boolean) response.getClass()
                .getMethod("transactionSuccess").invoke(response);
        } catch (Exception e) {
            LogWriter.except(e);
            return false;
        }
    }

    public static boolean has(EntityPlayer player, double amount) {
        if (!Enabled) return false;
        try {
            return (Boolean) hasMethod.invoke(economy,
                player.getCommandSenderName(), amount);
        } catch (Exception e) {
            LogWriter.except(e);
            return false;
        }
    }

    public static String formatCurrency(double amount) {
        if (!Enabled) return String.format("%.2f", amount);
        try {
            return (String) format.invoke(economy, amount);
        } catch (Exception e) {
            return String.format("%.2f", amount);
        }
    }
}
```

#### 1.2.3 Initialization Hook

**File: `src/main/java/noppes/npcs/CustomNpcs.java`**

Add to `load()` method after AddonManager:
```java
// Initialize VaultAPI integration (for Bukkit hybrid servers)
VaultHelper.load();
```

### 1.3 Currency Controller

**File: `src/main/java/kamkeel/npcs/controllers/CurrencyController.java`**

```java
package kamkeel.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import kamkeel.npcs.compat.VaultHelper;
import noppes.npcs.NoppesUtilPlayer;

public class CurrencyController {
    private static CurrencyController instance;

    // Configuration
    public boolean useVaultIfAvailable = true;
    public ItemStack fallbackCurrency = null;  // Item to use when Vault unavailable
    public double itemToVaultRatio = 1.0;      // 1 item = X vault currency

    public static CurrencyController getInstance() {
        if (instance == null) {
            instance = new CurrencyController();
        }
        return instance;
    }

    /**
     * Check if Vault economy is being used
     */
    public boolean isUsingVault() {
        return useVaultIfAvailable && VaultHelper.Enabled;
    }

    /**
     * Get player's balance (Vault or item count)
     */
    public double getBalance(EntityPlayer player) {
        if (isUsingVault()) {
            return VaultHelper.getBalance(player);
        }
        // Fall back to counting items
        if (fallbackCurrency != null) {
            return NoppesUtilPlayer.countItems(player, fallbackCurrency, false, false);
        }
        return 0.0;
    }

    /**
     * Check if player can afford amount
     */
    public boolean canAfford(EntityPlayer player, double amount) {
        if (isUsingVault()) {
            return VaultHelper.has(player, amount);
        }
        return getBalance(player) >= amount;
    }

    /**
     * Withdraw currency from player
     */
    public boolean withdraw(EntityPlayer player, double amount) {
        if (isUsingVault()) {
            return VaultHelper.withdraw(player, amount);
        }
        // Remove items
        if (fallbackCurrency != null) {
            ItemStack toRemove = fallbackCurrency.copy();
            toRemove.stackSize = (int) Math.ceil(amount / itemToVaultRatio);
            return NoppesUtilPlayer.consumeItem(player, toRemove, false, false);
        }
        return false;
    }

    /**
     * Deposit currency to player
     */
    public boolean deposit(EntityPlayer player, double amount) {
        if (isUsingVault()) {
            return VaultHelper.deposit(player, amount);
        }
        // Give items
        if (fallbackCurrency != null) {
            ItemStack toGive = fallbackCurrency.copy();
            toGive.stackSize = (int) Math.floor(amount / itemToVaultRatio);
            return player.inventory.addItemStackToInventory(toGive);
        }
        return false;
    }

    /**
     * Format currency for display
     */
    public String format(double amount) {
        if (isUsingVault()) {
            return VaultHelper.formatCurrency(amount);
        }
        return String.format("%.0f", amount);
    }

    // NBT Persistence
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("UseVault", useVaultIfAvailable);
        compound.setDouble("ItemRatio", itemToVaultRatio);
        if (fallbackCurrency != null) {
            compound.setTag("FallbackItem", fallbackCurrency.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        useVaultIfAvailable = compound.getBoolean("UseVault");
        itemToVaultRatio = compound.getDouble("ItemRatio");
        if (compound.hasKey("FallbackItem")) {
            fallbackCurrency = ItemStack.loadItemStackFromNBT(
                compound.getCompoundTag("FallbackItem"));
        }
    }
}
```

### 1.4 Player Currency Data

**File: `src/main/java/noppes/npcs/controllers/data/PlayerCurrencyData.java`**

```java
package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import java.util.HashMap;

public class PlayerCurrencyData {
    private final PlayerData parent;

    // Transaction history (for auditing/display)
    public HashMap<Long, Double> transactionHistory = new HashMap<>();
    public double totalEarned = 0.0;
    public double totalSpent = 0.0;

    public PlayerCurrencyData(PlayerData parent) {
        this.parent = parent;
    }

    public void recordTransaction(double amount, boolean isEarning) {
        long timestamp = System.currentTimeMillis();
        transactionHistory.put(timestamp, isEarning ? amount : -amount);
        if (isEarning) {
            totalEarned += amount;
        } else {
            totalSpent += amount;
        }
        // Limit history size
        if (transactionHistory.size() > 100) {
            // Remove oldest entries
            Long oldest = transactionHistory.keySet().stream()
                .min(Long::compare).orElse(null);
            if (oldest != null) {
                transactionHistory.remove(oldest);
            }
        }
    }

    public void loadNBTData(NBTTagCompound compound) {
        if (compound.hasKey("CurrencyData")) {
            NBTTagCompound data = compound.getCompoundTag("CurrencyData");
            totalEarned = data.getDouble("TotalEarned");
            totalSpent = data.getDouble("TotalSpent");
        }
    }

    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound data = new NBTTagCompound();
        data.setDouble("TotalEarned", totalEarned);
        data.setDouble("TotalSpent", totalSpent);
        compound.setTag("CurrencyData", data);
    }
}
```

### 1.5 Configuration UI

Add currency configuration to the global settings GUI:

**File: `src/main/java/noppes/npcs/client/gui/global/GuiNPCManageCurrency.java`**

This GUI will allow server operators to:
- Enable/disable Vault integration
- Set fallback currency item
- Configure item-to-Vault ratio
- View server economy statistics

---

## Feature 2: Trader Enhancements

### 2.1 Overview

Enhance the existing `RoleTrader` with:
- **Stock System**: Limited inventory that depletes when players buy
- **Per-Player vs Per-Server Stock**: Configure whether stock is shared or individual
- **Stock Reset Timer**: Automatic restocking based on configurable cooldown (like quest cooldowns)
- **Currency Integration**: Use Vault currency alongside or instead of item-based trading

### 2.2 Trader Stock Configuration

#### 2.2.1 Stock Data Class

**File: `src/main/java/noppes/npcs/controllers/data/TraderStock.java`**

```java
package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.HashMap;

public class TraderStock {
    // Stock configuration
    public boolean enableStock = false;
    public boolean perPlayer = false;  // false = per-server, true = per-player

    // Reset configuration (mirrors Quest cooldown system)
    public EnumStockReset resetType = EnumStockReset.NONE;
    public long customResetTime = 0;  // For MCCUSTOM/RLCUSTOM types

    // Stock quantities per slot (max stock)
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
     */
    public int getAvailableStock(int slot, String playerName) {
        if (!enableStock || maxStock[slot] < 0) {
            return Integer.MAX_VALUE;  // Unlimited
        }

        if (perPlayer) {
            PlayerTraderStock pStock = playerStock.get(playerName);
            if (pStock == null) {
                return maxStock[slot];  // Not purchased yet
            }
            return pStock.getStock(slot, maxStock[slot]);
        }

        return currentStock[slot];
    }

    /**
     * Consume stock when purchase is made
     */
    public boolean consumeStock(int slot, String playerName, int amount) {
        if (!enableStock || maxStock[slot] < 0) {
            return true;  // Unlimited
        }

        if (perPlayer) {
            PlayerTraderStock pStock = playerStock.computeIfAbsent(
                playerName, k -> new PlayerTraderStock());
            return pStock.consumeStock(slot, maxStock[slot], amount);
        }

        if (currentStock[slot] >= amount) {
            currentStock[slot] -= amount;
            return true;
        }
        return false;
    }

    /**
     * Check if stock should reset
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
     */
    public void resetStock(long currentTime) {
        lastResetTime = currentTime;

        for (int i = 0; i < 18; i++) {
            currentStock[i] = maxStock[i];
        }

        // Clear per-player stock
        playerStock.clear();
    }

    /**
     * Get reset interval in milliseconds
     */
    public long getResetInterval() {
        switch (resetType) {
            case MCDAILY:
                return 24000 * 50;  // 24000 ticks * 50ms
            case MCWEEKLY:
                return 168000 * 50;
            case MCCUSTOM:
                return customResetTime * 50;
            case RLDAILY:
                return 86400000L;  // 24 hours in ms
            case RLWEEKLY:
                return 604800000L;  // 7 days in ms
            case RLCUSTOM:
                return customResetTime;
            default:
                return Long.MAX_VALUE;
        }
    }

    /**
     * Get time until next reset (for display)
     */
    public long getTimeUntilReset(long currentTime) {
        if (resetType == EnumStockReset.NONE) {
            return -1;
        }
        long elapsed = currentTime - lastResetTime;
        long interval = getResetInterval();
        return Math.max(0, interval - elapsed);
    }

    // NBT Serialization
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
        resetType = EnumStockReset.values()[compound.getInteger("ResetType")];
        customResetTime = compound.getLong("CustomResetTime");
        maxStock = compound.getIntArray("MaxStock");
        currentStock = compound.getIntArray("CurrentStock");
        lastResetTime = compound.getLong("LastResetTime");

        // Load per-player stock
        NBTTagList playerList = compound.getTagList("PlayerStock", 10);
        for (int i = 0; i < playerList.tagCount(); i++) {
            NBTTagCompound playerTag = playerList.getCompoundTagAt(i);
            String playerName = playerTag.getString("Player");
            PlayerTraderStock pStock = new PlayerTraderStock();
            pStock.readFromNBT(playerTag);
            playerStock.put(playerName, pStock);
        }
    }
}

// Inner class for per-player stock tracking
class PlayerTraderStock {
    public int[] purchasedAmounts = new int[18];
    public long lastPurchaseTime = 0;

    public int getStock(int slot, int maxStock) {
        return Math.max(0, maxStock - purchasedAmounts[slot]);
    }

    public boolean consumeStock(int slot, int maxStock, int amount) {
        if (getStock(slot, maxStock) >= amount) {
            purchasedAmounts[slot] += amount;
            lastPurchaseTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setIntArray("Purchased", purchasedAmounts);
        compound.setLong("LastPurchase", lastPurchaseTime);
    }

    public void readFromNBT(NBTTagCompound compound) {
        purchasedAmounts = compound.getIntArray("Purchased");
        lastPurchaseTime = compound.getLong("LastPurchase");
    }
}
```

#### 2.2.2 Stock Reset Enum

**File: `src/main/java/noppes/npcs/constants/EnumStockReset.java`**

```java
package noppes.npcs.constants;

public enum EnumStockReset {
    NONE,        // Never resets
    MCDAILY,     // Every 24000 MC ticks (1 MC day)
    MCWEEKLY,    // Every 168000 MC ticks (7 MC days)
    MCCUSTOM,    // Custom MC tick interval
    RLDAILY,     // Every 24 real hours
    RLWEEKLY,    // Every 7 real days
    RLCUSTOM     // Custom real-time interval
}
```

### 2.3 RoleTrader Modifications

**File: `src/main/java/noppes/npcs/roles/RoleTrader.java`**

Add the following to `RoleTrader`:

```java
// Add new fields
public TraderStock stock = new TraderStock();
public boolean useCurrency = false;       // Use Vault/Currency system
public double[] slotPrices = new double[18];  // Currency prices per slot

// Modify interact() to check stock reset
@Override
public void interact(EntityPlayer player) {
    // Check for stock reset before opening GUI
    if (stock.enableStock) {
        long currentTime = stock.resetType.name().startsWith("RL")
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
    NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, npc);
}

// Modify writeToNBT
@Override
public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
    // ... existing code ...

    // Add stock data
    nbttagcompound.setTag("Stock", stock.writeToNBT(new NBTTagCompound()));
    nbttagcompound.setBoolean("UseCurrency", useCurrency);

    // Save currency prices
    NBTTagList priceList = new NBTTagList();
    for (double price : slotPrices) {
        NBTTagCompound priceTag = new NBTTagCompound();
        priceTag.setDouble("Price", price);
        priceList.appendTag(priceTag);
    }
    nbttagcompound.setTag("SlotPrices", priceList);

    return nbttagcompound;
}

// Modify readFromNBT
@Override
public void readFromNBT(NBTTagCompound nbttagcompound) {
    // ... existing code ...

    // Load stock data
    if (nbttagcompound.hasKey("Stock")) {
        stock.readFromNBT(nbttagcompound.getCompoundTag("Stock"));
    }
    useCurrency = nbttagcompound.getBoolean("UseCurrency");

    // Load currency prices
    if (nbttagcompound.hasKey("SlotPrices")) {
        NBTTagList priceList = nbttagcompound.getTagList("SlotPrices", 10);
        for (int i = 0; i < priceList.tagCount() && i < 18; i++) {
            slotPrices[i] = priceList.getCompoundTagAt(i).getDouble("Price");
        }
    }
}
```

### 2.4 ContainerNPCTrader Modifications

**File: `src/main/java/noppes/npcs/containers/ContainerNPCTrader.java`**

Modify `slotClick()` to handle stock and currency:

```java
public ItemStack slotClick(int slotIndex, EntityPlayer player) {
    RoleTrader role = (RoleTrader) npc.roleInterface;
    String playerName = player.getCommandSenderName();

    // ... existing validation ...

    // Check stock availability
    if (role.stock.enableStock) {
        int available = role.stock.getAvailableStock(slotIndex, playerName);
        if (available <= 0) {
            // Out of stock - send message to player
            return null;
        }
    }

    // Handle currency-based purchase
    if (role.useCurrency && role.slotPrices[slotIndex] > 0) {
        double price = role.slotPrices[slotIndex];
        CurrencyController currency = CurrencyController.getInstance();

        if (!currency.canAfford(player, price)) {
            // Can't afford - send message
            return null;
        }

        // Withdraw currency
        if (!currency.withdraw(player, price)) {
            return null;
        }
    } else {
        // ... existing item-based currency logic ...
    }

    // Consume stock
    if (role.stock.enableStock) {
        role.stock.consumeStock(slotIndex, playerName, 1);
    }

    // ... existing item delivery logic ...

    // Save if using market
    if (!role.marketName.isEmpty()) {
        Market.save(role, role.marketName);
    }

    return soldItem;
}
```

### 2.5 Trader Setup GUI Additions

**File: `src/main/java/noppes/npcs/client/gui/roles/GuiNpcTraderSetup.java`**

Add new configuration options:

```java
// Add buttons/fields for:
// - "Enable Stock" toggle
// - "Per Player" / "Per Server" radio buttons
// - "Reset Type" dropdown (None, MC Daily, MC Weekly, etc.)
// - "Custom Reset Time" field
// - "Use Currency" toggle
// - Per-slot stock quantity inputs
// - Per-slot currency price inputs

private void initStockGui() {
    addButton(new GuiNpcButton(20, x, y, 80, 20,
        "Enable Stock: " + (role.stock.enableStock ? "Yes" : "No")));

    addButton(new GuiNpcButton(21, x, y + 22, 80, 20,
        role.stock.perPlayer ? "Per Player" : "Per Server"));

    String[] resetTypes = {"None", "MC Daily", "MC Weekly", "MC Custom",
                           "RL Daily", "RL Weekly", "RL Custom"};
    addButton(new GuiNpcButton(22, x, y + 44, 80, 20, resetTypes,
        role.stock.resetType.ordinal()));

    addButton(new GuiNpcButton(23, x, y + 66, 80, 20,
        "Use Currency: " + (role.useCurrency ? "Yes" : "No")));
}
```

---

## Feature 3: Auction House

### 3.1 Overview

The Auction House is a global trading system where players can:
- **List items for sale** at fixed prices or as auctions with bidding
- **Browse listings** by category, search, and filter
- **Bid on auctions** with automatic outbid notifications
- **Buy instantly** for buy-it-now prices
- **Manage their listings** (cancel, modify)
- **Collect proceeds and expired items** via mailbox or direct claim

### 3.2 Auction Data Structures

#### 3.2.1 AuctionListing

**File: `src/main/java/noppes/npcs/controllers/data/AuctionListing.java`**

```java
package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.util.UUID;

public class AuctionListing {
    public int id;
    public UUID sellerUUID;
    public String sellerName;
    public ItemStack item;

    // Pricing
    public double startingPrice;
    public double buyoutPrice;  // 0 = no buyout
    public double currentBid;
    public UUID highBidderUUID;
    public String highBidderName;

    // Timing
    public long listingTime;
    public long expirationTime;
    public EnumAuctionDuration duration;

    // Status
    public EnumAuctionStatus status = EnumAuctionStatus.ACTIVE;

    // Category
    public EnumAuctionCategory category = EnumAuctionCategory.MISC;

    public AuctionListing() {}

    public AuctionListing(UUID seller, String sellerName, ItemStack item,
                          double startPrice, double buyout, EnumAuctionDuration duration) {
        this.sellerUUID = seller;
        this.sellerName = sellerName;
        this.item = item.copy();
        this.startingPrice = startPrice;
        this.buyoutPrice = buyout;
        this.currentBid = startPrice;
        this.duration = duration;
        this.listingTime = System.currentTimeMillis();
        this.expirationTime = listingTime + duration.getMillis();
        this.category = categorizeItem(item);
    }

    /**
     * Check if auction has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    /**
     * Get time remaining in milliseconds
     */
    public long getTimeRemaining() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }

    /**
     * Place a bid
     */
    public boolean placeBid(UUID bidder, String bidderName, double amount) {
        if (status != EnumAuctionStatus.ACTIVE) return false;
        if (amount <= currentBid) return false;
        if (bidder.equals(sellerUUID)) return false;  // Can't bid on own item

        // Minimum bid increment (5% or 1, whichever is greater)
        double minIncrement = Math.max(1, currentBid * 0.05);
        if (amount < currentBid + minIncrement) return false;

        highBidderUUID = bidder;
        highBidderName = bidderName;
        currentBid = amount;

        return true;
    }

    /**
     * Execute buyout
     */
    public boolean buyout(UUID buyer, String buyerName) {
        if (status != EnumAuctionStatus.ACTIVE) return false;
        if (buyoutPrice <= 0) return false;  // No buyout available
        if (buyer.equals(sellerUUID)) return false;

        highBidderUUID = buyer;
        highBidderName = buyerName;
        currentBid = buyoutPrice;
        status = EnumAuctionStatus.SOLD;

        return true;
    }

    /**
     * Cancel listing (seller only, before any bids)
     */
    public boolean cancel(UUID requester) {
        if (!requester.equals(sellerUUID)) return false;
        if (highBidderUUID != null) return false;  // Has bids, can't cancel

        status = EnumAuctionStatus.CANCELLED;
        return true;
    }

    /**
     * Auto-categorize item based on type
     */
    private EnumAuctionCategory categorizeItem(ItemStack item) {
        // Implement item categorization logic
        // Check for weapons, armor, tools, food, blocks, etc.
        return EnumAuctionCategory.MISC;
    }

    // NBT Serialization
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("Id", id);
        compound.setString("SellerUUID", sellerUUID.toString());
        compound.setString("SellerName", sellerName);
        compound.setTag("Item", item.writeToNBT(new NBTTagCompound()));
        compound.setDouble("StartPrice", startingPrice);
        compound.setDouble("BuyoutPrice", buyoutPrice);
        compound.setDouble("CurrentBid", currentBid);
        if (highBidderUUID != null) {
            compound.setString("BidderUUID", highBidderUUID.toString());
            compound.setString("BidderName", highBidderName);
        }
        compound.setLong("ListingTime", listingTime);
        compound.setLong("ExpirationTime", expirationTime);
        compound.setInteger("Duration", duration.ordinal());
        compound.setInteger("Status", status.ordinal());
        compound.setInteger("Category", category.ordinal());
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        id = compound.getInteger("Id");
        sellerUUID = UUID.fromString(compound.getString("SellerUUID"));
        sellerName = compound.getString("SellerName");
        item = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Item"));
        startingPrice = compound.getDouble("StartPrice");
        buyoutPrice = compound.getDouble("BuyoutPrice");
        currentBid = compound.getDouble("CurrentBid");
        if (compound.hasKey("BidderUUID")) {
            highBidderUUID = UUID.fromString(compound.getString("BidderUUID"));
            highBidderName = compound.getString("BidderName");
        }
        listingTime = compound.getLong("ListingTime");
        expirationTime = compound.getLong("ExpirationTime");
        duration = EnumAuctionDuration.values()[compound.getInteger("Duration")];
        status = EnumAuctionStatus.values()[compound.getInteger("Status")];
        category = EnumAuctionCategory.values()[compound.getInteger("Category")];
    }
}
```

#### 3.2.2 Supporting Enums

**File: `src/main/java/noppes/npcs/constants/EnumAuctionDuration.java`**

```java
package noppes.npcs.constants;

public enum EnumAuctionDuration {
    SHORT(2 * 60 * 60 * 1000L),      // 2 hours
    MEDIUM(12 * 60 * 60 * 1000L),    // 12 hours
    LONG(24 * 60 * 60 * 1000L),      // 24 hours
    VERY_LONG(48 * 60 * 60 * 1000L); // 48 hours

    private final long millis;

    EnumAuctionDuration(long millis) {
        this.millis = millis;
    }

    public long getMillis() {
        return millis;
    }

    public String getDisplayName() {
        switch (this) {
            case SHORT: return "2 Hours";
            case MEDIUM: return "12 Hours";
            case LONG: return "24 Hours";
            case VERY_LONG: return "48 Hours";
            default: return "Unknown";
        }
    }
}
```

**File: `src/main/java/noppes/npcs/constants/EnumAuctionStatus.java`**

```java
package noppes.npcs.constants;

public enum EnumAuctionStatus {
    ACTIVE,      // Currently listed
    SOLD,        // Successfully sold (buyout or auction end with bids)
    EXPIRED,     // Ended without bids
    CANCELLED,   // Cancelled by seller
    CLAIMED      // Item/money has been claimed
}
```

**File: `src/main/java/noppes/npcs/constants/EnumAuctionCategory.java`**

```java
package noppes.npcs.constants;

public enum EnumAuctionCategory {
    WEAPONS,
    ARMOR,
    TOOLS,
    FOOD,
    POTIONS,
    BLOCKS,
    MATERIALS,
    ENCHANTED,
    MISC
}
```

### 3.3 Auction Controller

**File: `src/main/java/noppes/npcs/controllers/AuctionController.java`**

```java
package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.constants.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AuctionController {
    private static AuctionController instance;

    // All listings (thread-safe for async operations)
    public ConcurrentHashMap<Integer, AuctionListing> listings = new ConcurrentHashMap<>();

    // Index by seller for quick lookup
    private Map<UUID, Set<Integer>> sellerIndex = new ConcurrentHashMap<>();

    // Pending claims (seller -> items/money to claim)
    public Map<UUID, List<AuctionClaim>> pendingClaims = new ConcurrentHashMap<>();

    private int nextId = 1;
    private long lastSaveTime = 0;
    private static final long SAVE_INTERVAL = 60000; // Save every minute

    // Configuration
    public double listingFee = 0.0;           // Fee to list item
    public double saleTax = 0.05;             // 5% tax on sales
    public int maxListingsPerPlayer = 20;
    public boolean requireAuctioneer = true;  // Require Auctioneer NPC to access

    public static AuctionController getInstance() {
        if (instance == null) {
            instance = new AuctionController();
            instance.load();
        }
        return instance;
    }

    /**
     * Create a new auction listing
     */
    public AuctionListing createListing(EntityPlayer seller, ItemStack item,
                                         double startPrice, double buyout,
                                         EnumAuctionDuration duration) {
        UUID sellerUUID = seller.getUniqueID();

        // Check max listings
        Set<Integer> sellerListings = sellerIndex.get(sellerUUID);
        if (sellerListings != null && sellerListings.size() >= maxListingsPerPlayer) {
            return null;
        }

        // Charge listing fee
        if (listingFee > 0) {
            CurrencyController currency = CurrencyController.getInstance();
            if (!currency.withdraw(seller, listingFee)) {
                return null;
            }
        }

        // Create listing
        AuctionListing listing = new AuctionListing(
            sellerUUID, seller.getCommandSenderName(),
            item, startPrice, buyout, duration
        );
        listing.id = nextId++;

        // Add to indexes
        listings.put(listing.id, listing);
        sellerIndex.computeIfAbsent(sellerUUID, k -> new HashSet<>()).add(listing.id);

        // Remove item from seller inventory
        item.stackSize = 0;

        scheduleSave();
        return listing;
    }

    /**
     * Place a bid on an auction
     */
    public boolean placeBid(EntityPlayer bidder, int listingId, double amount) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || listing.isExpired()) return false;

        CurrencyController currency = CurrencyController.getInstance();

        // Check if bidder can afford
        if (!currency.canAfford(bidder, amount)) return false;

        // Refund previous bidder
        if (listing.highBidderUUID != null) {
            addClaim(listing.highBidderUUID, listing.highBidderName,
                    null, listing.currentBid, "Outbid refund for: " +
                    listing.item.getDisplayName());
        }

        // Place bid
        if (!listing.placeBid(bidder.getUniqueID(),
                             bidder.getCommandSenderName(), amount)) {
            return false;
        }

        // Hold currency from bidder
        currency.withdraw(bidder, amount);

        scheduleSave();
        return true;
    }

    /**
     * Buyout an auction
     */
    public boolean buyout(EntityPlayer buyer, int listingId) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || listing.buyoutPrice <= 0) return false;

        CurrencyController currency = CurrencyController.getInstance();

        // Check if buyer can afford
        if (!currency.canAfford(buyer, listing.buyoutPrice)) return false;

        // Refund previous bidder if any
        if (listing.highBidderUUID != null) {
            addClaim(listing.highBidderUUID, listing.highBidderName,
                    null, listing.currentBid, "Buyout refund for: " +
                    listing.item.getDisplayName());
        }

        // Execute buyout
        if (!listing.buyout(buyer.getUniqueID(), buyer.getCommandSenderName())) {
            return false;
        }

        // Withdraw from buyer
        currency.withdraw(buyer, listing.buyoutPrice);

        // Calculate seller proceeds (minus tax)
        double proceeds = listing.buyoutPrice * (1 - saleTax);

        // Add claims for both parties
        addClaim(listing.sellerUUID, listing.sellerName, null, proceeds,
                "Sale of: " + listing.item.getDisplayName());
        addClaim(buyer.getUniqueID(), buyer.getCommandSenderName(),
                listing.item, 0, "Purchased: " + listing.item.getDisplayName());

        scheduleSave();
        return true;
    }

    /**
     * Cancel a listing (seller only)
     */
    public boolean cancelListing(EntityPlayer player, int listingId) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) return false;

        if (!listing.cancel(player.getUniqueID())) return false;

        // Return item to seller
        addClaim(listing.sellerUUID, listing.sellerName, listing.item, 0,
                "Cancelled listing: " + listing.item.getDisplayName());

        scheduleSave();
        return true;
    }

    /**
     * Process expired auctions (called periodically)
     */
    public void processExpiredAuctions() {
        long now = System.currentTimeMillis();

        for (AuctionListing listing : listings.values()) {
            if (listing.status != EnumAuctionStatus.ACTIVE) continue;
            if (!listing.isExpired()) continue;

            if (listing.highBidderUUID != null) {
                // Auction successful - has bids
                listing.status = EnumAuctionStatus.SOLD;

                double proceeds = listing.currentBid * (1 - saleTax);
                addClaim(listing.sellerUUID, listing.sellerName, null, proceeds,
                        "Auction sold: " + listing.item.getDisplayName());
                addClaim(listing.highBidderUUID, listing.highBidderName,
                        listing.item, 0, "Won auction: " + listing.item.getDisplayName());
            } else {
                // No bids - return to seller
                listing.status = EnumAuctionStatus.EXPIRED;
                addClaim(listing.sellerUUID, listing.sellerName, listing.item, 0,
                        "Expired listing: " + listing.item.getDisplayName());
            }
        }

        scheduleSave();
    }

    /**
     * Get active listings with optional filters
     */
    public List<AuctionListing> getActiveListings(EnumAuctionCategory category,
                                                   String search,
                                                   int page, int pageSize) {
        return listings.values().stream()
            .filter(l -> l.status == EnumAuctionStatus.ACTIVE)
            .filter(l -> !l.isExpired())
            .filter(l -> category == null || l.category == category)
            .filter(l -> search == null || search.isEmpty() ||
                        l.item.getDisplayName().toLowerCase()
                              .contains(search.toLowerCase()))
            .sorted(Comparator.comparingLong(l -> l.expirationTime))
            .skip((long) page * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());
    }

    /**
     * Get player's own listings
     */
    public List<AuctionListing> getPlayerListings(UUID playerUUID) {
        Set<Integer> ids = sellerIndex.get(playerUUID);
        if (ids == null) return Collections.emptyList();

        return ids.stream()
            .map(listings::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Get pending claims for a player
     */
    public List<AuctionClaim> getPendingClaims(UUID playerUUID) {
        return pendingClaims.getOrDefault(playerUUID, Collections.emptyList());
    }

    /**
     * Claim pending items/money
     */
    public boolean claim(EntityPlayer player, int claimIndex) {
        List<AuctionClaim> claims = pendingClaims.get(player.getUniqueID());
        if (claims == null || claimIndex >= claims.size()) return false;

        AuctionClaim claim = claims.get(claimIndex);

        // Give item or money
        if (claim.item != null) {
            if (!player.inventory.addItemStackToInventory(claim.item.copy())) {
                return false;  // Inventory full
            }
        }
        if (claim.money > 0) {
            CurrencyController.getInstance().deposit(player, claim.money);
        }

        claims.remove(claimIndex);
        scheduleSave();
        return true;
    }

    private void addClaim(UUID playerUUID, String playerName,
                          ItemStack item, double money, String description) {
        AuctionClaim claim = new AuctionClaim(item, money, description);
        pendingClaims.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(claim);
    }

    // Persistence
    private void scheduleSave() {
        long now = System.currentTimeMillis();
        if (now - lastSaveTime > SAVE_INTERVAL) {
            save();
            lastSaveTime = now;
        }
    }

    public void save() {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("NextId", nextId);
            compound.setDouble("ListingFee", listingFee);
            compound.setDouble("SaleTax", saleTax);
            compound.setInteger("MaxListings", maxListingsPerPlayer);

            // Save listings
            NBTTagList listingList = new NBTTagList();
            for (AuctionListing listing : listings.values()) {
                listingList.appendTag(listing.writeToNBT(new NBTTagCompound()));
            }
            compound.setTag("Listings", listingList);

            // Save claims
            NBTTagList claimList = new NBTTagList();
            for (Map.Entry<UUID, List<AuctionClaim>> entry : pendingClaims.entrySet()) {
                NBTTagCompound playerClaims = new NBTTagCompound();
                playerClaims.setString("UUID", entry.getKey().toString());
                NBTTagList claims = new NBTTagList();
                for (AuctionClaim claim : entry.getValue()) {
                    claims.appendTag(claim.writeToNBT(new NBTTagCompound()));
                }
                playerClaims.setTag("Claims", claims);
                claimList.appendTag(playerClaims);
            }
            compound.setTag("PendingClaims", claimList);

            File file = new File(CustomNpcs.getWorldSaveDirectory(), "auction.dat");
            CompressedStreamTools.writeCompressed(compound,
                new java.io.FileOutputStream(file));

        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public void load() {
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), "auction.dat");
            if (!file.exists()) return;

            NBTTagCompound compound = CompressedStreamTools.readCompressed(
                new java.io.FileInputStream(file));

            nextId = compound.getInteger("NextId");
            listingFee = compound.getDouble("ListingFee");
            saleTax = compound.getDouble("SaleTax");
            maxListingsPerPlayer = compound.getInteger("MaxListings");

            // Load listings
            NBTTagList listingList = compound.getTagList("Listings", 10);
            for (int i = 0; i < listingList.tagCount(); i++) {
                AuctionListing listing = new AuctionListing();
                listing.readFromNBT(listingList.getCompoundTagAt(i));
                listings.put(listing.id, listing);
                sellerIndex.computeIfAbsent(listing.sellerUUID, k -> new HashSet<>())
                          .add(listing.id);
            }

            // Load claims
            NBTTagList claimList = compound.getTagList("PendingClaims", 10);
            for (int i = 0; i < claimList.tagCount(); i++) {
                NBTTagCompound playerClaims = claimList.getCompoundTagAt(i);
                UUID playerUUID = UUID.fromString(playerClaims.getString("UUID"));
                NBTTagList claims = playerClaims.getTagList("Claims", 10);
                List<AuctionClaim> claimsList = new ArrayList<>();
                for (int j = 0; j < claims.tagCount(); j++) {
                    AuctionClaim claim = new AuctionClaim();
                    claim.readFromNBT(claims.getCompoundTagAt(j));
                    claimsList.add(claim);
                }
                pendingClaims.put(playerUUID, claimsList);
            }

        } catch (Exception e) {
            LogWriter.except(e);
        }
    }
}

// Claim data structure
class AuctionClaim {
    public ItemStack item;
    public double money;
    public String description;
    public long timestamp;

    public AuctionClaim() {}

    public AuctionClaim(ItemStack item, double money, String description) {
        this.item = item != null ? item.copy() : null;
        this.money = money;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (item != null) {
            compound.setTag("Item", item.writeToNBT(new NBTTagCompound()));
        }
        compound.setDouble("Money", money);
        compound.setString("Description", description);
        compound.setLong("Timestamp", timestamp);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Item")) {
            item = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Item"));
        }
        money = compound.getDouble("Money");
        description = compound.getString("Description");
        timestamp = compound.getLong("Timestamp");
    }
}
```

### 3.4 Auctioneer Role

**File: `src/main/java/noppes/npcs/roles/RoleAuctioneer.java`**

```java
package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleAuctioneer extends RoleInterface {

    // Configuration
    public String auctionHouseName = "Global";  // For future multi-auction support
    public boolean allowSelling = true;
    public boolean allowBuying = true;
    public boolean showExpiredListings = true;

    // Custom messages
    public String welcomeMessage = "";
    public String sellDisabledMessage = "This auctioneer does not accept listings.";
    public String buyDisabledMessage = "This auctioneer does not sell items.";

    public RoleAuctioneer(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("AuctionHouse", auctionHouseName);
        compound.setBoolean("AllowSelling", allowSelling);
        compound.setBoolean("AllowBuying", allowBuying);
        compound.setBoolean("ShowExpired", showExpiredListings);
        compound.setString("WelcomeMsg", welcomeMessage);
        compound.setString("SellDisabledMsg", sellDisabledMessage);
        compound.setString("BuyDisabledMsg", buyDisabledMessage);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        auctionHouseName = compound.getString("AuctionHouse");
        allowSelling = compound.getBoolean("AllowSelling");
        allowBuying = compound.getBoolean("AllowBuying");
        showExpiredListings = compound.getBoolean("ShowExpired");
        welcomeMessage = compound.getString("WelcomeMsg");
        sellDisabledMessage = compound.getString("SellDisabledMsg");
        buyDisabledMessage = compound.getString("BuyDisabledMsg");
    }

    @Override
    public void interact(EntityPlayer player) {
        // Show welcome message if set
        if (!welcomeMessage.isEmpty()) {
            npc.say(player, npc.advanced.getInteractLine());
        }

        // Open auction house GUI
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerAuction, npc);
    }
}
```

### 3.5 Update EnumRoleType

**File: `src/main/java/noppes/npcs/constants/EnumRoleType.java`**

```java
public enum EnumRoleType {
    None,
    Trader,
    Follower,
    Bank,
    Transporter,
    Postman,
    Companion,
    Mount,
    Auctioneer  // Add this
}
```

### 3.6 Update DataAdvanced

**File: `src/main/java/noppes/npcs/DataAdvanced.java`**

Add to `setRole()` method:

```java
else if (role == EnumRoleType.Auctioneer && !(npc.roleInterface instanceof RoleAuctioneer))
    npc.roleInterface = new RoleAuctioneer(npc);
```

### 3.7 Update EnumGuiType

**File: `src/main/java/noppes/npcs/constants/EnumGuiType.java`**

Add new GUI types:

```java
// Auction House GUIs
PlayerAuction,           // Main auction browse/buy interface
PlayerAuctionSell,       // Create new listing
PlayerAuctionMyListings, // View own listings
PlayerAuctionClaims,     // Claim items/money
AuctioneerSetup,         // NPC configuration
ManageAuctions           // Admin management
```

### 3.8 Auction GUI System

The auction GUI system will consist of multiple interconnected screens:

#### 3.8.1 Main Auction Browser

**File: `src/main/java/noppes/npcs/client/gui/player/GuiAuctionBrowse.java`**

Features:
- Category filter tabs (Weapons, Armor, Tools, etc.)
- Search bar for item names
- Sort options (Time remaining, Price, Recently listed)
- Paginated listing display
- Quick buy/bid buttons

#### 3.8.2 Create Listing GUI

**File: `src/main/java/noppes/npcs/client/gui/player/GuiAuctionSell.java`**

Features:
- Item slot for the item to sell
- Starting price input
- Buyout price input (optional)
- Duration selector
- Listing fee display
- Confirm button

#### 3.8.3 My Listings GUI

**File: `src/main/java/noppes/npcs/client/gui/player/GuiAuctionMyListings.java`**

Features:
- List of player's active listings
- Current bid status
- Time remaining
- Cancel button (if no bids)

#### 3.8.4 Claims GUI

**File: `src/main/java/noppes/npcs/client/gui/player/GuiAuctionClaims.java`**

Features:
- List of pending claims (items won, money from sales, refunds)
- Claim button for each item
- Claim all button

### 3.9 Network Packets

#### 3.9.1 Auction Action Packet

**File: `src/main/java/kamkeel/npcs/network/packets/player/AuctionActionPacket.java`**

```java
package kamkeel.npcs.network.packets.player;

public class AuctionActionPacket extends AbstractPacket {

    public enum Action {
        CREATE_LISTING,
        PLACE_BID,
        BUYOUT,
        CANCEL,
        CLAIM,
        CLAIM_ALL,
        BROWSE,
        MY_LISTINGS
    }

    private Action action;
    private int listingId;
    private double amount;
    private NBTTagCompound itemData;
    private int duration;
    private double buyoutPrice;
    private int category;
    private String search;
    private int page;

    // Implement sendData/receiveData...
}
```

#### 3.9.2 Auction Sync Packet

**File: `src/main/java/kamkeel/npcs/network/packets/data/AuctionSyncPacket.java`**

Used to send auction listing data to clients for GUI display.

### 3.10 Periodic Tasks

Add to server tick handler to process expired auctions:

**File: `src/main/java/noppes/npcs/ServerTickHandler.java`**

```java
private int auctionCheckCounter = 0;

@SubscribeEvent
public void onServerTick(TickEvent.ServerTickEvent event) {
    // ... existing code ...

    // Check expired auctions every 5 minutes (6000 ticks)
    if (++auctionCheckCounter >= 6000) {
        auctionCheckCounter = 0;
        AuctionController.getInstance().processExpiredAuctions();
    }
}
```

---

## Feature 4: Item Attributes & Restrictions

### 4.1 Overview

Implement item attribute system to control which items can be traded/auctioned. Items with certain attributes will be blocked from the auction house.

### 4.2 Existing Item Requirements (Reuse)

The codebase already has requirement types that we can leverage:

| Requirement | NBT Key | File | Purpose |
|-------------|---------|------|---------|
| ProfileSlot Bound | `cnpc_profile_slot` | `ProfileSlotRequirement.java` | Item bound to specific profile slot |
| Soulbound | `cnpc_soulbind` | `SoulbindRequirement.java` | Item bound to specific player UUID |

### 4.3 New Untradeable Attribute

A new NBT-based item attribute that marks items as untradeable.

**File: `src/main/java/kamkeel/npcs/controllers/data/attribute/ItemTradeAttribute.java`**

```java
package kamkeel.npcs.controllers.data.attribute;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemTradeAttribute {
    // New untradeable attribute
    public static final String TAG_UNTRADEABLE = "CNPC_Untradeable";

    // Existing requirement keys (from ProfileSlotRequirement and SoulbindRequirement)
    public static final String TAG_PROFILE_SLOT = "cnpc_profile_slot";
    public static final String TAG_SOULBIND = "cnpc_soulbind";

    /**
     * Check if item can be traded/auctioned
     */
    public static boolean canTrade(ItemStack item, EntityPlayer player) {
        if (item == null || !item.hasTagCompound()) {
            return true;
        }
        NBTTagCompound tag = item.getTagCompound();

        // Check untradeable flag
        if (tag.getBoolean(TAG_UNTRADEABLE)) return false;

        // Check profile slot bound (item tied to specific profile slot)
        if (tag.hasKey(TAG_PROFILE_SLOT)) return false;

        // Check soulbound (item tied to specific player)
        if (tag.hasKey(TAG_SOULBIND)) return false;

        return true;
    }

    /**
     * Mark item as untradeable
     */
    public static void setUntradeable(ItemStack item, boolean untradeable) {
        if (item == null) return;
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        if (untradeable) {
            item.getTagCompound().setBoolean(TAG_UNTRADEABLE, true);
        } else {
            item.getTagCompound().removeTag(TAG_UNTRADEABLE);
        }
    }

    public static boolean isUntradeable(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;
        return item.getTagCompound().getBoolean(TAG_UNTRADEABLE);
    }

    public static boolean isProfileSlotBound(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;
        return item.getTagCompound().hasKey(TAG_PROFILE_SLOT);
    }

    public static boolean isSoulbound(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return false;
        return item.getTagCompound().hasKey(TAG_SOULBIND);
    }

    /**
     * Get reason why item cannot be traded (for UI display)
     */
    public static String getTradeBlockReason(ItemStack item) {
        if (item == null || !item.hasTagCompound()) return null;
        NBTTagCompound tag = item.getTagCompound();

        if (tag.getBoolean(TAG_UNTRADEABLE)) return "gui.auction.untradeable";
        if (tag.hasKey(TAG_PROFILE_SLOT)) return "gui.auction.profileslotbound";
        if (tag.hasKey(TAG_SOULBIND)) return "gui.auction.soulbound";

        return null;
    }
}
```

### 4.4 Auction House Item Blacklist

A configurable system to blacklist specific items from being auctioned.

**File: `src/main/java/noppes/npcs/controllers/AuctionBlacklist.java`**

```java
package noppes.npcs.controllers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class AuctionBlacklist {
    private static AuctionBlacklist instance;

    // Blacklist by item registry name (e.g., "minecraft:diamond_sword")
    private Set<String> blacklistedItems = new HashSet<>();

    // Blacklist by mod ID (e.g., "minecraft" blocks all vanilla items)
    private Set<String> blacklistedMods = new HashSet<>();

    // Blacklist by NBT tag presence (e.g., items with specific custom data)
    private Set<String> blacklistedNBTTags = new HashSet<>();

    public static AuctionBlacklist getInstance() {
        if (instance == null) {
            instance = new AuctionBlacklist();
            instance.load();
        }
        return instance;
    }

    /**
     * Check if item is blacklisted
     */
    public boolean isBlacklisted(ItemStack item) {
        if (item == null) return false;

        // Check item registry name
        String itemName = Item.itemRegistry.getNameForObject(item.getItem());
        if (itemName != null && blacklistedItems.contains(itemName)) {
            return true;
        }

        // Check mod ID
        if (itemName != null) {
            String modId = itemName.split(":")[0];
            if (blacklistedMods.contains(modId)) {
                return true;
            }
        }

        // Check NBT tags
        if (item.hasTagCompound()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String nbtTag : blacklistedNBTTags) {
                if (tag.hasKey(nbtTag)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Blacklist management methods
    public void addItem(String itemName) {
        blacklistedItems.add(itemName);
        save();
    }

    public void removeItem(String itemName) {
        blacklistedItems.remove(itemName);
        save();
    }

    public void addMod(String modId) {
        blacklistedMods.add(modId);
        save();
    }

    public void removeMod(String modId) {
        blacklistedMods.remove(modId);
        save();
    }

    public void addNBTTag(String tag) {
        blacklistedNBTTags.add(tag);
        save();
    }

    public void removeNBTTag(String tag) {
        blacklistedNBTTags.remove(tag);
        save();
    }

    public Set<String> getBlacklistedItems() {
        return new HashSet<>(blacklistedItems);
    }

    public Set<String> getBlacklistedMods() {
        return new HashSet<>(blacklistedMods);
    }

    public Set<String> getBlacklistedNBTTags() {
        return new HashSet<>(blacklistedNBTTags);
    }

    // Persistence
    public void save() {
        // Save to auction_blacklist.dat
    }

    public void load() {
        // Load from auction_blacklist.dat
    }
}
```

### 4.5 Integration with Auction Listing

Modify `AuctionController.createListing()` to check restrictions:

```java
public AuctionListing createListing(EntityPlayer seller, ItemStack item, ...) {
    // Check item attribute restrictions
    if (!ItemTradeAttribute.canTrade(item)) {
        String reason = ItemTradeAttribute.getTradeBlockReason(item);
        // Send message to player with reason
        return null;
    }

    // Check blacklist
    if (AuctionBlacklist.getInstance().isBlacklisted(item)) {
        // Send "This item cannot be auctioned" message
        return null;
    }

    // ... rest of listing creation
}
```

---

## Feature 5: Admin Auction Management

### 5.1 Overview

A comprehensive admin system for managing the auction house in real-time.

### 5.2 Admin Permissions

**File: `src/main/java/noppes/npcs/CustomNpcsPermissions.java`**

Add new permissions:

```java
// Auction Administration
public static final Permission AUCTION_ADMIN = new Permission("customnpcs.auction.admin");
public static final Permission AUCTION_MANAGE = new Permission("customnpcs.auction.manage");
public static final Permission AUCTION_BLACKLIST = new Permission("customnpcs.auction.blacklist");
```

### 5.3 Admin GUI

**File: `src/main/java/noppes/npcs/client/gui/global/GuiAuctionAdmin.java`**

Features:
- **View All Listings**: See all active, expired, and cancelled listings
- **Search/Filter**: Find listings by seller, item, status
- **Cancel Any Listing**: Force-cancel listings (returns item to seller)
- **Modify Listing**: Edit prices, duration (with warning)
- **End Auction Early**: Force-end with current highest bidder winning
- **Refund Bidders**: Manually refund specific bidders
- **View Blacklist**: Manage item blacklist
- **Audit Log**: View recent auction actions

```java
public class GuiAuctionAdmin extends GuiNPCInterface {
    private List<AuctionListing> allListings;
    private int currentPage = 0;
    private EnumAuctionStatus filterStatus = null;
    private String searchQuery = "";

    // Admin action buttons
    private static final int BTN_CANCEL = 10;
    private static final int BTN_END_EARLY = 11;
    private static final int BTN_EDIT = 12;
    private static final int BTN_REFUND = 13;
    private static final int BTN_BLACKLIST = 14;
    private static final int BTN_AUDIT = 15;

    @Override
    public void initGui() {
        // Tab buttons: All | Active | Sold | Expired | Cancelled
        // Search bar
        // Listing table with columns: ID, Seller, Item, Price, Bids, Status, Time
        // Action buttons for selected listing
        // Blacklist management button
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BTN_CANCEL:
                adminCancelListing(selectedListing);
                break;
            case BTN_END_EARLY:
                adminEndAuction(selectedListing);
                break;
            case BTN_EDIT:
                openEditDialog(selectedListing);
                break;
            case BTN_REFUND:
                openRefundDialog(selectedListing);
                break;
            case BTN_BLACKLIST:
                openBlacklistGui();
                break;
            case BTN_AUDIT:
                openAuditLog();
                break;
        }
    }
}
```

### 5.4 Admin Actions in AuctionController

```java
// Admin-only methods
public boolean adminCancelListing(EntityPlayer admin, int listingId, String reason) {
    if (!CustomNpcsPermissions.hasPermission(admin, AUCTION_MANAGE)) {
        return false;
    }

    AuctionListing listing = listings.get(listingId);
    if (listing == null) return false;

    // Refund current bidder if any
    if (listing.highBidderUUID != null) {
        addClaim(listing.highBidderUUID, listing.highBidderName,
                null, listing.currentBid, "Admin cancelled auction: " + reason);
    }

    // Return item to seller
    addClaim(listing.sellerUUID, listing.sellerName, listing.item, 0,
            "Listing cancelled by admin: " + reason);

    listing.status = EnumAuctionStatus.CANCELLED;

    // Log action
    logAdminAction(admin, "CANCEL", listingId, reason);

    return true;
}

public boolean adminEndAuction(EntityPlayer admin, int listingId) {
    if (!CustomNpcsPermissions.hasPermission(admin, AUCTION_MANAGE)) {
        return false;
    }

    AuctionListing listing = listings.get(listingId);
    if (listing == null || listing.status != EnumAuctionStatus.ACTIVE) {
        return false;
    }

    // End as if time expired
    listing.expirationTime = System.currentTimeMillis();
    processExpiredAuctions();

    // Log action
    logAdminAction(admin, "END_EARLY", listingId, "");

    return true;
}

public boolean adminEditListing(EntityPlayer admin, int listingId,
                                 double newStartPrice, double newBuyout,
                                 long newExpiration) {
    if (!CustomNpcsPermissions.hasPermission(admin, AUCTION_MANAGE)) {
        return false;
    }

    AuctionListing listing = listings.get(listingId);
    if (listing == null || listing.status != EnumAuctionStatus.ACTIVE) {
        return false;
    }

    // Only allow if no bids placed
    if (listing.highBidderUUID != null) {
        return false; // Cannot edit listing with bids
    }

    listing.startingPrice = newStartPrice;
    listing.currentBid = newStartPrice;
    listing.buyoutPrice = newBuyout;
    listing.expirationTime = newExpiration;

    // Log action
    logAdminAction(admin, "EDIT", listingId,
        String.format("price=%.2f,buyout=%.2f", newStartPrice, newBuyout));

    return true;
}

// Audit logging
private void logAdminAction(EntityPlayer admin, String action, int listingId, String details) {
    // Log to file: timestamp, admin name, action, listing id, details
    LogWriter.info(String.format("[AUCTION ADMIN] %s performed %s on listing %d: %s",
        admin.getCommandSenderName(), action, listingId, details));
}
```

### 5.5 Blacklist Management GUI

**File: `src/main/java/noppes/npcs/client/gui/global/GuiAuctionBlacklist.java`**

Features:
- Add item by holding it and clicking "Add"
- Add by typing item registry name
- Add entire mod by mod ID
- Add NBT tag pattern
- View current blacklist with remove buttons
- Test item against blacklist

### 5.6 Access Points

The Admin GUI can be accessed via:
1. **Global Menu**: New "Auction Management" button (requires permission)
2. **Auctioneer NPC**: Admin gear icon when editing NPC role (requires permission)
3. **Command**: `/cnpc auction admin` (requires OP or permission)

---

## Configuration Reference

All auction and market settings will be added to the configuration system.

### Auction Configuration

**File: `config/customnpcs/auction.cfg`** (or section in existing config)

```properties
# ===========================================
# Auction House Configuration
# ===========================================

# General Settings
# ----------------
# Enable/disable the auction house system
B:AuctionEnabled=true

# Default maximum auction listings per player (without permission override)
I:DefaultMaxListings=5

# Listing fee as percentage of starting price (0.0 = no fee)
D:ListingFeePercent=0.0

# Sales tax as percentage taken from final sale price (0.05 = 5%)
D:SalesTaxPercent=0.05

# Require an Auctioneer NPC to access auction house
B:RequireAuctioneerNPC=true

# Timing Settings
# ---------------
# Available auction durations in hours (comma-separated)
S:AvailableDurations=2,12,24,48

# Default auction duration in hours
I:DefaultDurationHours=24

# Claim expiration in days (claims auto-expire after this period, 0 = never)
I:ClaimExpirationDays=30

# Bidding Rules
# -------------
# Minimum bid increment as percentage (0.05 = 5% minimum increase)
D:MinBidIncrementPercent=0.05

# Snipe protection: extend auction TO this many minutes when bid placed near end
# Set to 0 to disable snipe protection
I:SnipeProtectionMinutes=5

# How many minutes before end triggers snipe protection
# (bids placed with less than this remaining will extend the auction)
I:SnipeProtectionThreshold=5

# Permissions
# -----------
# Permission pattern for auction slot limits: customnpcs.auction.slots.X
# Players without any permission get DefaultMaxListings
# Example: Player with customnpcs.auction.slots.10 can have 10 listings
```

### Currency Configuration

```properties
# ===========================================
# Currency System Configuration
# ===========================================

# Use VaultAPI if available on Bukkit hybrid servers
B:UseVaultIfAvailable=true

# Fallback currency item (registry name) when Vault unavailable
# Leave empty to disable fallback currency
S:FallbackCurrencyItem=customnpcs:npcCoinGold

# Conversion ratio: 1 fallback item = X virtual currency
D:FallbackItemRatio=1.0
```

### Trader Stock Configuration

```properties
# ===========================================
# Trader Stock Configuration
# ===========================================

# Enable stock system by default for new traders
B:EnableStockByDefault=false

# Default stock reset type (NONE, MCDAILY, MCWEEKLY, RLDAILY, RLWEEKLY)
S:DefaultResetType=NONE
```

### Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| `customnpcs.auction.use` | Access auction house | true |
| `customnpcs.auction.sell` | Create auction listings | true |
| `customnpcs.auction.buy` | Bid and buyout auctions | true |
| `customnpcs.auction.slots.X` | Have X maximum listings | - |
| `customnpcs.auction.admin` | Access admin management | op |
| `customnpcs.auction.manage` | Modify/cancel any listing | op |
| `customnpcs.auction.blacklist` | Manage item blacklist | op |
| `customnpcs.auction.bypass.fee` | Bypass listing fees | op |
| `customnpcs.auction.bypass.tax` | Bypass sales tax | op |

---

## Implementation Order

The features should be implemented in this order due to dependencies:

### Phase 1: Currency System (Foundation)
1. Add VaultAPI dependency to build files
2. Create `VaultHelper.java` with reflection-based integration
3. Create `CurrencyController.java`
4. Add initialization hook in `CustomNpcs.java`
5. Create `PlayerCurrencyData.java` and integrate with `PlayerData`
6. Create currency configuration GUI
7. Test with and without Vault present

### Phase 2: Trader Enhancements
1. Create `EnumStockReset.java`
2. Create `TraderStock.java`
3. Modify `RoleTrader.java` to add stock and currency fields
4. Modify `ContainerNPCTrader.java` to handle stock/currency
5. Update `GuiNpcTraderSetup.java` with new options
6. Update network packets for stock synchronization
7. Test stock depletion and reset timers

### Phase 3: Item Attributes & Restrictions
1. Create `ItemTradeAttribute.java` with Untradeable, SlotBound, SoulBound tags
2. Create `AuctionBlacklist.java` controller
3. Add API methods to scripting interface for setting item attributes
4. Create blacklist persistence (load/save to file)
5. Test attribute blocking in listing creation

### Phase 4: Auction House
1. Create auction enums (`EnumAuctionDuration`, `EnumAuctionStatus`, `EnumAuctionCategory`)
2. Create `AuctionListing.java` and `AuctionClaim` classes
3. Create `AuctionController.java` with item restriction checks
4. Update `EnumRoleType.java` to add Auctioneer
5. Create `RoleAuctioneer.java`
6. Update `DataAdvanced.java` for role instantiation
7. Update `EnumGuiType.java` with auction GUIs
8. Create auction containers
9. Create auction GUI classes
10. Create network packets
11. Add periodic task for expired auctions
12. Create auctioneer setup GUI
13. Comprehensive testing

### Phase 5: Admin Auction Management
1. Add auction admin permissions to `CustomNpcsPermissions.java`
2. Create `GuiAuctionAdmin.java` admin interface
3. Create `GuiAuctionBlacklist.java` blacklist management
4. Add admin methods to `AuctionController.java`
5. Add audit logging
6. Create admin network packets
7. Add command handler for `/cnpc auction admin`
8. Test admin operations

---

## File Summary

### New Files to Create

| File | Description |
|------|-------------|
| `kamkeel/npcs/compat/VaultHelper.java` | VaultAPI integration |
| `kamkeel/npcs/controllers/CurrencyController.java` | Currency management |
| `noppes/npcs/controllers/data/PlayerCurrencyData.java` | Per-player currency data (NOT slot-bound) |
| `noppes/npcs/controllers/data/TraderStock.java` | Trader stock configuration |
| `noppes/npcs/constants/EnumStockReset.java` | Stock reset types |
| `kamkeel/npcs/controllers/data/attribute/ItemTradeAttribute.java` | Untradeable/SlotBound/SoulBound item attributes |
| `noppes/npcs/controllers/AuctionBlacklist.java` | Auction item blacklist controller |
| `noppes/npcs/controllers/AuctionController.java` | Auction house management |
| `noppes/npcs/controllers/data/AuctionListing.java` | Auction listing data |
| `noppes/npcs/constants/EnumAuctionDuration.java` | Auction duration options |
| `noppes/npcs/constants/EnumAuctionStatus.java` | Auction status states |
| `noppes/npcs/constants/EnumAuctionCategory.java` | Item categories |
| `noppes/npcs/roles/RoleAuctioneer.java` | Auctioneer NPC role |
| `noppes/npcs/containers/ContainerAuction*.java` | Auction containers |
| `noppes/npcs/client/gui/player/GuiAuction*.java` | Auction GUIs |
| `noppes/npcs/client/gui/roles/GuiNpcAuctioneerSetup.java` | Auctioneer config |
| `noppes/npcs/client/gui/global/GuiAuctionAdmin.java` | Admin auction management |
| `noppes/npcs/client/gui/global/GuiAuctionBlacklist.java` | Blacklist management GUI |
| `kamkeel/npcs/network/packets/player/AuctionActionPacket.java` | Auction actions |
| `kamkeel/npcs/network/packets/data/AuctionSyncPacket.java` | Auction data sync |
| `kamkeel/npcs/network/packets/admin/AuctionAdminPacket.java` | Admin auction actions |

### Files to Modify

| File | Changes |
|------|---------|
| `dependencies.gradle` | Add VaultAPI dependency |
| `repositories.gradle` | Add Vault repository |
| `CustomNpcs.java` | Initialize VaultHelper, AuctionController, AuctionBlacklist |
| `CustomNpcsPermissions.java` | Add auction admin permissions |
| `EnumRoleType.java` | Add Auctioneer |
| `EnumGuiType.java` | Add auction GUI types, admin GUIs |
| `DataAdvanced.java` | Handle Auctioneer role |
| `RoleTrader.java` | Add stock and currency support |
| `ContainerNPCTrader.java` | Handle stock/currency transactions |
| `GuiNpcTrader.java` | Add stock reset timer banner above GUI |
| `GuiNpcTraderSetup.java` | Add stock/currency UI |
| `PlayerData.java` | Add PlayerCurrencyData (NOT in slot components - shared across slots) |
| `CommonProxy.java` | Register auction containers |
| `ServerTickHandler.java` | Add auction expiration check |
| `PacketHandler.java` | Register auction packets and admin packets |
| `CommandKamkeel.java` | Add `/cnpc auction admin` command |

---

## Testing Strategy

### Unit Testing
- Currency operations (deposit, withdraw, balance)
- Currency shared across profile slots (not slot-bound)
- Stock depletion and reset calculations
- Auction bid validation
- Auction expiration handling
- Item attribute checks (Untradeable, SlotBound, SoulBound)
- Blacklist matching (by item, mod, NBT tag)

### Integration Testing
- VaultAPI integration on hybrid servers
- Fallback to item currency when Vault unavailable
- Stock persistence across server restarts
- Auction persistence and recovery
- Profile slot switching with shared currency
- Outbid refunds going to claims correctly
- Admin actions affecting live auctions

### User Acceptance Testing
- Complete trading flow with stock limits
- Complete auction flow (list → bid → win → claim)
- Currency display formatting with gold coin icon
- Stock reset timer banner display above trader GUI
- GUI usability
- Admin management workflow

### Edge Cases
- Negative currency amounts
- Concurrent bids on same auction
- Server crash during transaction
- Player offline when auction ends (claim on next login)
- Maximum listings per player (permission-based)
- Inventory full when claiming
- Trying to auction Untradeable/SlotBound/SoulBound items
- Trying to auction blacklisted items
- Admin cancelling auction with active bids
- Profile slot switch while having pending claims

---

## API/Scripting Extensions

Consider adding these scripting hooks:

```java
// ICurrency interface for scripts
double getBalance(IPlayer player);
boolean withdraw(IPlayer player, double amount);
boolean deposit(IPlayer player, double amount);
String format(double amount);

// IItemStack extensions for trade attributes
boolean isUntradeable();
void setUntradeable(boolean value);
boolean isSlotBound();
void setSlotBound(boolean value);
boolean isSoulBound();
void setSoulBound(boolean value);
boolean canTrade();  // Returns false if any blocking attribute is set
String getTradeBlockReason();  // Returns localization key for reason

// IAuction interface for scripts
IAuctionListing createListing(IPlayer seller, IItemStack item,
                               double startPrice, double buyout, int durationHours);
boolean cancelListing(int listingId);
List<IAuctionListing> getPlayerListings(IPlayer player);
boolean isItemBlacklisted(IItemStack item);

// IAuctionBlacklist interface for scripts (admin)
void addItemToBlacklist(String registryName);
void removeItemFromBlacklist(String registryName);
void addModToBlacklist(String modId);
void removeModFromBlacklist(String modId);
boolean isBlacklisted(IItemStack item);

// Events
onAuctionCreated(IAuctionListing listing);
onAuctionBid(IAuctionListing listing, IPlayer bidder, double amount);
onAuctionOutbid(IAuctionListing listing, IPlayer previousBidder, double refundAmount);
onAuctionSold(IAuctionListing listing, IPlayer buyer, double price);
onAuctionExpired(IAuctionListing listing);
onAuctionCancelled(IAuctionListing listing, boolean byAdmin);
onTraderPurchase(ITrader trader, IPlayer buyer, int slot, int stockRemaining);
onTraderStockReset(ITrader trader);
onCurrencyTransaction(IPlayer player, double amount, boolean isDeposit);
```

---

## Conclusion

This implementation plan provides a comprehensive roadmap for the CustomNPC+ 1.11 Market Update. The five features are designed to work together seamlessly:

1. **Currency System** provides the foundation for monetary transactions (shared across profile slots)
2. **Trader Enhancements** modernize the existing trader with stock, currency support, and reset timer banners
3. **Item Attributes & Restrictions** control which items can be traded via Untradeable, ProfileSlot Bound, and Soulbound attributes plus blacklists
4. **Auction House** creates a global marketplace with claims-based refunds, bid modification, and permission-based slot limits
5. **Admin Management** provides comprehensive tools for server operators to manage the auction house in real-time

The implementation follows existing codebase patterns (permission system from Profiles, gold coin from CustomItems, existing requirement types) and can be developed incrementally, with each phase building on the previous one.

### Key Design Decisions Summary
- Outbid refunds → Claims (not direct to balance) with **Modify Bid** feature
- Currency → Shared across all profile slots per player
- Permissions → Uses existing CustomNPC+ permission pattern (default: 5 slots)
- Display icon → Existing `CustomItems.coinGold`
- Stock reset timer → Banner displayed ABOVE the trader GUI
- Trade restrictions → New Untradeable + existing ProfileSlotRequirement/SoulbindRequirement + Blacklist
- Claim expiration → 30 days default (configurable)
- Bid increment → Percentage-based minimum (configurable, e.g., 5%)
- Snipe protection → Extend TO 5 minutes when bid placed near end (configurable)
- Auction durations → 2h, 12h, 24h, 48h (configurable)
- Admin tools → Full management GUI with audit logging
