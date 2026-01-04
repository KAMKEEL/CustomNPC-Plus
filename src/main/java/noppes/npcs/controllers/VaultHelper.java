package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDataController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

/**
 * Helper class for VaultAPI integration on Bukkit hybrid servers.
 * Uses reflection to integrate with Vault when available.
 * Falls back to internal currency system when Vault is not present.
 */
public class VaultHelper {
    private static final Logger logger = LogManager.getLogger(CustomNpcs.class);

    public static VaultHelper Instance;

    // Vault reflection references
    private boolean vaultAvailable = false;
    private Object economy = null;
    private Method getBalance;
    private Method depositPlayer;
    private Method withdrawPlayer;
    private Method hasAccount;
    private Method createPlayerAccount;
    private Method responseSuccess;

    // Bukkit references for getting offline player
    private Class<?> bukkitClass;
    private Method getOfflinePlayer;

    public VaultHelper() {
        Instance = this;

        if (!ConfigMarket.UseVaultIfAvailable) {
            logger.info("Vault integration disabled by config");
            return;
        }

        try {
            // Check for Bukkit first
            bukkitClass = Class.forName("org.bukkit.Bukkit");
            getOfflinePlayer = bukkitClass.getMethod("getOfflinePlayer", String.class);

            // Check for Vault
            Class<?> vaultClass = Class.forName("net.milkbowl.vault.economy.Economy");

            // Get the Economy service from Bukkit's ServicesManager
            Method getServicesManager = bukkitClass.getMethod("getServicesManager");
            Object servicesManager = getServicesManager.invoke(null);

            Method getRegistration = servicesManager.getClass().getMethod("getRegistration", Class.class);
            Object registration = getRegistration.invoke(servicesManager, vaultClass);

            if (registration != null) {
                Method getProvider = registration.getClass().getMethod("getProvider");
                economy = getProvider.invoke(registration);

                if (economy != null) {
                    // Get Economy methods - using OfflinePlayer versions for broader compatibility
                    Class<?> offlinePlayerClass = Class.forName("org.bukkit.OfflinePlayer");
                    getBalance = economy.getClass().getMethod("getBalance", offlinePlayerClass);
                    depositPlayer = economy.getClass().getMethod("depositPlayer", offlinePlayerClass, double.class);
                    withdrawPlayer = economy.getClass().getMethod("withdrawPlayer", offlinePlayerClass, double.class);
                    hasAccount = economy.getClass().getMethod("hasAccount", offlinePlayerClass);
                    createPlayerAccount = economy.getClass().getMethod("createPlayerAccount", offlinePlayerClass);

                    // EconomyResponse.transactionSuccess() method
                    Class<?> economyResponseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
                    responseSuccess = economyResponseClass.getField("transactionSuccess").getClass().getMethod("booleanValue");

                    vaultAvailable = true;
                    logger.info("Vault Economy integration enabled - using external economy");
                }
            }

            if (!vaultAvailable) {
                logger.info("Vault found but no Economy provider registered - using internal currency");
            }
        } catch (ClassNotFoundException e) {
            // Vault not present - use internal currency
            logger.info("Vault not found - using internal currency system");
        } catch (Exception e) {
            logger.warn("Error initializing Vault integration - using internal currency: " + e.getMessage());
        }
    }

    /**
     * Check if Vault economy is available and enabled
     */
    public boolean isVaultEnabled() {
        return vaultAvailable && ConfigMarket.UseVaultIfAvailable;
    }

    /**
     * Get player's balance
     * Uses Vault if available, otherwise internal currency
     */
    public long getBalance(EntityPlayer player) {
        if (isVaultEnabled()) {
            try {
                Object offlinePlayer = getOfflinePlayer.invoke(null, player.getCommandSenderName());
                Double balance = (Double) getBalance.invoke(economy, offlinePlayer);
                return balance.longValue();
            } catch (Exception e) {
                logger.warn("Vault getBalance failed, falling back to internal: " + e.getMessage());
            }
        }

        // Fall back to internal currency
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        return data.currencyData.getBalance();
    }

    /**
     * Deposit currency to player's balance
     * Uses Vault if available, otherwise internal currency
     * @return true if successful
     */
    public boolean deposit(EntityPlayer player, long amount) {
        if (amount <= 0) {
            return false;
        }

        if (isVaultEnabled()) {
            try {
                Object offlinePlayer = getOfflinePlayer.invoke(null, player.getCommandSenderName());

                // Ensure account exists
                Boolean hasAcc = (Boolean) hasAccount.invoke(economy, offlinePlayer);
                if (!hasAcc) {
                    createPlayerAccount.invoke(economy, offlinePlayer);
                }

                Object response = depositPlayer.invoke(economy, offlinePlayer, (double) amount);
                // Check if transaction succeeded by checking the transactionSuccess field
                Boolean success = (Boolean) response.getClass().getField("transactionSuccess").get(response);

                if (success) {
                    // Also update internal tracking for statistics
                    PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                    data.currencyData.setBalance(getBalance(player));
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.warn("Vault deposit failed, falling back to internal: " + e.getMessage());
            }
        }

        // Fall back to internal currency
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        boolean success = data.currencyData.deposit(amount);
        if (success) {
            data.save();
        }
        return success;
    }

    /**
     * Withdraw currency from player's balance
     * Uses Vault if available, otherwise internal currency
     * @return true if successful
     */
    public boolean withdraw(EntityPlayer player, long amount) {
        if (amount <= 0) {
            return false;
        }

        if (isVaultEnabled()) {
            try {
                Object offlinePlayer = getOfflinePlayer.invoke(null, player.getCommandSenderName());

                // Check if player has enough
                Double currentBalance = (Double) getBalance.invoke(economy, offlinePlayer);
                if (currentBalance < amount) {
                    return false;
                }

                Object response = withdrawPlayer.invoke(economy, offlinePlayer, (double) amount);
                Boolean success = (Boolean) response.getClass().getField("transactionSuccess").get(response);

                if (success) {
                    // Also update internal tracking for statistics
                    PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                    data.currencyData.setBalance(getBalance(player));
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.warn("Vault withdraw failed, falling back to internal: " + e.getMessage());
            }
        }

        // Fall back to internal currency
        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        boolean success = data.currencyData.withdraw(amount);
        if (success) {
            data.save();
        }
        return success;
    }

    /**
     * Check if player can afford an amount
     */
    public boolean canAfford(EntityPlayer player, long amount) {
        return getBalance(player) >= amount;
    }

    /**
     * Transfer currency from one player to another
     * @return true if successful
     */
    public boolean transfer(EntityPlayer from, EntityPlayer to, long amount) {
        if (amount <= 0) {
            return false;
        }

        if (!canAfford(from, amount)) {
            return false;
        }

        // Withdraw from sender first
        if (!withdraw(from, amount)) {
            return false;
        }

        // Deposit to receiver
        if (!deposit(to, amount)) {
            // Rollback - deposit back to sender
            deposit(from, amount);
            return false;
        }

        return true;
    }

    /**
     * Set player's balance directly (admin operation)
     * Only works with internal currency - Vault manages its own balances
     */
    public void setBalance(EntityPlayer player, long amount) {
        if (isVaultEnabled()) {
            logger.warn("Cannot set balance directly when using Vault - use Vault's economy commands");
            return;
        }

        PlayerData data = PlayerDataController.Instance.getPlayerData(player);
        data.currencyData.setBalance(amount);
        data.save();
    }

    /**
     * Format an amount for display with currency name
     */
    public String formatAmount(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }

    /**
     * Get the currency name
     */
    public String getCurrencyName() {
        return ConfigMarket.CurrencyName;
    }
}
