package kamkeel.npcs.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;

/**
 * Utility class for Vault Economy integration.
 * Uses reflection to access Vault API when available.
 * Requires BukkitUtil to be initialized first.
 */
public class VaultUtil {
    private static final Logger logger = LogManager.getLogger(VaultUtil.class);

    private static boolean initialized = false;
    private static boolean vaultEnabled = false;
//
//    // Vault classes
//    private static Class<?> economyClass;
//    private static Class<?> economyResponseClass;
//
//    // Economy methods
//    private static Method isEnabled;
//    private static Method getName;
//    private static Method hasAccount;
//    private static Method hasAccountOffline;
//    private static Method getBalance;
//    private static Method getBalanceOffline;
//    private static Method has;
//    private static Method hasOffline;
//    private static Method withdrawPlayer;
//    private static Method withdrawPlayerOffline;
//    private static Method depositPlayer;
//    private static Method depositPlayerOffline;
//    private static Method format;
//    private static Method currencyNamePlural;
//    private static Method currencyNameSingular;
//    private static Method createPlayerAccount;
//    private static Method createPlayerAccountOffline;
//    private static Method hasBankSupport;
//    private static Method getBanks;
//    private static Method bankBalance;
//    private static Method bankHas;
//    private static Method bankWithdraw;
//    private static Method bankDeposit;
//
//    // EconomyResponse methods
//    private static Method transactionSuccess;
//
//    // Cached economy instance
//    private static Object economyInstance;

    private static Economy economyInstance;

    /**
     * Initializes the Vault integration using reflection.
     * Called automatically by BukkitUtil after Bukkit is confirmed available.
     */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (!BukkitUtil.isEnabled()) {
            logger.debug("Bukkit not available, Vault integration disabled");
            return;
        }

        try {
            // Get the economy instance
            RegisteredServiceProvider<Economy> economyRegistration = Bukkit.getServicesManager().getRegistration(Economy.class);
            economyInstance = economyRegistration.getProvider();
            if (economyInstance != null) {
                vaultEnabled = true;
                logger.info("Vault Economy integration enabled - Provider: " + getEconomyName());
            } else {
                logger.info("Vault found but no Economy provider registered");
            }

        } catch (Exception e) {
            logger.error("Error initializing Vault integration", e);
        }
    }

    /**
     * Refreshes the economy provider. Call this if the economy plugin loads after initialization.
     */
    public static void refreshEconomyProvider() {
        if (!initialized || !BukkitUtil.isEnabled()) {
            return;
        }

        try {
            RegisteredServiceProvider<Economy> economyRegistration = Bukkit.getServicesManager().getRegistration(Economy.class);
            economyInstance = economyRegistration.getProvider();
            if (economyInstance != null) {
                vaultEnabled = true;
                logger.info("Vault Economy provider refreshed: " + getEconomyName());
            }
        } catch (Exception e) {
            logger.error("Error refreshing Vault economy provider", e);
        }
    }

    /**
     * @return true if Vault economy integration is enabled and working
     */
    public static boolean isEnabled() {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            return economyInstance.isEnabled();
        } catch (Exception e) {
            logger.error("Error checking if economy is enabled", e);
            return false;
        }
    }

    /**
     * @return the name of the economy provider, or null if not available
     */
    public static String getEconomyName() {
        if (!vaultEnabled || economyInstance == null) return null;

        try {
            return economyInstance.getName();
        } catch (Exception e) {
            logger.error("Error getting economy name", e);
            return null;
        }
    }

    /**
     * Checks if a player has an account in the economy system.
     * @param player the player to check
     * @return true if the player has an account
     */
    public static boolean hasAccount(EntityPlayer player) {
        return hasAccount(player.getCommandSenderName());
    }

    /**
     * Checks if a player has an account in the economy system.
     * @param playerName the player name to check
     * @return true if the player has an account
     */
    public static boolean hasAccount(String playerName) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            OfflinePlayer offlinePlayer = (OfflinePlayer) BukkitUtil.getOfflinePlayer(playerName);
            if (offlinePlayer != null) {
                return economyInstance.hasAccount(offlinePlayer);
            }
            return economyInstance.hasAccount(playerName);
        } catch (Exception e) {
            logger.error("Error checking if player has account: " + playerName, e);
            return false;
        }
    }

    /**
     * Gets the balance of a player.
     * @param player the player
     * @return the player's balance, or 0 if not available
     */
    public static double getBalance(EntityPlayer player) {
        return getBalance(player.getCommandSenderName());
    }

    /**
     * Gets the balance of a player by name.
     * @param playerName the player name
     * @return the player's balance, or 0 if not available
     */
    public static double getBalance(String playerName) {
        if (!vaultEnabled || economyInstance == null) return 0;

        try {
            OfflinePlayer offlinePlayer = (OfflinePlayer) BukkitUtil.getOfflinePlayer(playerName);
            if (offlinePlayer != null) {
                return economyInstance.getBalance(offlinePlayer);
            }
            return economyInstance.getBalance(playerName);
        } catch (Exception e) {
            logger.error("Error getting balance for player: " + playerName, e);
            return 0;
        }
    }

    /**
     * Checks if a player has at least the specified amount.
     * @param player the player
     * @param amount the amount to check
     * @return true if the player has at least the specified amount
     */
    public static boolean has(EntityPlayer player, double amount) {
        return has(player.getCommandSenderName(), amount);
    }

    /**
     * Checks if a player has at least the specified amount.
     * @param playerName the player name
     * @param amount the amount to check
     * @return true if the player has at least the specified amount
     */
    public static boolean has(String playerName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            OfflinePlayer offlinePlayer = (OfflinePlayer) BukkitUtil.getOfflinePlayer(playerName);
            if (offlinePlayer != null) {
                return economyInstance.has(offlinePlayer, amount);
            }
            return economyInstance.has(playerName, amount);
        } catch (Exception e) {
            logger.error("Error checking if player has amount: " + playerName, e);
            return false;
        }
    }

    /**
     * Withdraws money from a player's account.
     * @param player the player
     * @param amount the amount to withdraw
     * @return true if the transaction was successful
     */
    public static boolean withdrawMoney(EntityPlayer player, double amount) {
        return withdrawMoney(player.getCommandSenderName(), amount);
    }

    /**
     * Withdraws money from a player's account.
     * @param playerName the player name
     * @param amount the amount to withdraw
     * @return true if the transaction was successful
     */
    public static boolean withdrawMoney(String playerName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            OfflinePlayer offlinePlayer = (OfflinePlayer) BukkitUtil.getOfflinePlayer(playerName);
            EconomyResponse response;
            if (offlinePlayer != null) {
                response = economyInstance.withdrawPlayer(offlinePlayer, amount);
            } else {
                response = economyInstance.withdrawPlayer(playerName, amount);
            }
            return response.transactionSuccess();
        } catch (Exception e) {
            logger.error("Error withdrawing money from player: " + playerName, e);
            return false;
        }
    }

    /**
     * Adds money to a player's account (deposit).
     * @param player the player
     * @param amount the amount to add
     * @return true if the transaction was successful
     */
    public static boolean addMoney(EntityPlayer player, double amount) {
        return addMoney(player.getCommandSenderName(), amount);
    }

    /**
     * Adds money to a player's account (deposit).
     * @param playerName the player name
     * @param amount the amount to add
     * @return true if the transaction was successful
     */
    public static boolean addMoney(String playerName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            OfflinePlayer offlinePlayer = (OfflinePlayer) BukkitUtil.getOfflinePlayer(playerName);
            EconomyResponse response;
            if (offlinePlayer != null) {
                response = economyInstance.depositPlayer(offlinePlayer, amount);
            } else {
                response = economyInstance.depositPlayer(playerName, amount);
            }
            return response.transactionSuccess();
        } catch (Exception e) {
            logger.error("Error adding money to player: " + playerName, e);
            return false;
        }
    }

    /**
     * Sets a player's balance to the specified amount.
     * This withdraws all money first, then deposits the new amount.
     * @param player the player
     * @param amount the new balance
     * @return true if the transaction was successful
     */
    public static boolean setBalance(EntityPlayer player, double amount) {
        return setBalance(player.getCommandSenderName(), amount);
    }

    /**
     * Sets a player's balance to the specified amount.
     * This withdraws all money first, then deposits the new amount.
     * @param playerName the player name
     * @param amount the new balance
     * @return true if the transaction was successful
     */
    public static boolean setBalance(String playerName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        double currentBalance = getBalance(playerName);
        if (currentBalance > 0) {
            if (!withdrawMoney(playerName, currentBalance)) {
                return false;
            }
        }
        if (amount > 0) {
            return addMoney(playerName, amount);
        }
        return true;
    }

    /**
     * Formats an amount according to the economy plugin's settings.
     * @param amount the amount to format
     * @return the formatted string, or the raw number as string if not available
     */
    public static String format(double amount) {
        if (!vaultEnabled || economyInstance == null) return String.valueOf(amount);

        try {
            return economyInstance.format(amount);
        } catch (Exception e) {
            logger.error("Error formatting amount", e);
            return String.valueOf(amount);
        }
    }

    /**
     * Gets the plural currency name (e.g., "Dollars").
     * @return the plural currency name, or "coins" if not available
     */
    public static String getCurrencyNamePlural() {
        if (!vaultEnabled || economyInstance == null) return "coins";

        try {
            return economyInstance.currencyNamePlural();
        } catch (Exception e) {
            logger.error("Error getting currency name plural", e);
            return "coins";
        }
    }

    /**
     * Gets the singular currency name (e.g., "Dollar").
     * @return the singular currency name, or "coin" if not available
     */
    public static String getCurrencyNameSingular() {
        if (!vaultEnabled || economyInstance == null) return "coin";

        try {
            return economyInstance.currencyNameSingular();
        } catch (Exception e) {
            logger.error("Error getting currency name singular", e);
            return "coin";
        }
    }

    /**
     * Creates an account for the player if one doesn't exist.
     * @param player the player
     * @return true if the account was created or already exists
     */
    public static boolean createAccount(EntityPlayer player) {
        return createAccount(player.getCommandSenderName());
    }

    /**
     * Creates an account for the player if one doesn't exist.
     * @param playerName the player name
     * @return true if the account was created or already exists
     */
    public static boolean createAccount(String playerName) {
        if (!vaultEnabled || economyInstance == null) return false;

        if (hasAccount(playerName)) return true;

        try {
            OfflinePlayer offlinePlayer = (OfflinePlayer) BukkitUtil.getOfflinePlayer(playerName);
            if (offlinePlayer != null) {
                return economyInstance.createPlayerAccount(offlinePlayer);
            }
            return economyInstance.createPlayerAccount(playerName);
        } catch (Exception e) {
            logger.error("Error creating account for player: " + playerName, e);
            return false;
        }
    }

    /**
     * @return true if the economy supports bank accounts
     */
    public static boolean hasBankSupport() {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            return economyInstance.hasBankSupport();
        } catch (Exception e) {
            logger.error("Error checking bank support", e);
            return false;
        }
    }

    /**
     * Gets a list of all bank names.
     * @return list of bank names, or empty list if not available
     */
    @SuppressWarnings("unchecked")
    public static java.util.List<String> getBanks() {
        if (!vaultEnabled || economyInstance == null) return java.util.Collections.emptyList();

        try {
            return economyInstance.getBanks();
        } catch (Exception e) {
            logger.error("Error getting banks", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Gets the balance of a bank.
     * @param bankName the bank name
     * @return the bank balance, or 0 if not available
     */
    public static double getBankBalance(String bankName) {
        if (!vaultEnabled || economyInstance == null) return 0;

        try {
            EconomyResponse response = economyInstance.bankBalance(bankName);
            return response.balance;
        } catch (Exception e) {
            logger.error("Error getting bank balance: " + bankName, e);
            return 0;
        }
    }

    /**
     * Checks if a bank has at least the specified amount.
     * @param bankName the bank name
     * @param amount the amount to check
     * @return true if the bank has at least the specified amount
     */
    public static boolean bankHas(String bankName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            EconomyResponse response = economyInstance.bankHas(bankName, amount);
            return response.transactionSuccess();
        } catch (Exception e) {
            logger.error("Error checking bank balance: " + bankName, e);
            return false;
        }
    }

    /**
     * Withdraws money from a bank.
     * @param bankName the bank name
     * @param amount the amount to withdraw
     * @return true if the transaction was successful
     */
    public static boolean bankWithdraw(String bankName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            EconomyResponse response = economyInstance.bankWithdraw(bankName, amount);
            return response.transactionSuccess();
        } catch (Exception e) {
            logger.error("Error withdrawing from bank: " + bankName, e);
            return false;
        }
    }

    /**
     * Deposits money into a bank.
     * @param bankName the bank name
     * @param amount the amount to deposit
     * @return true if the transaction was successful
     */
    public static boolean bankDeposit(String bankName, double amount) {
        if (!vaultEnabled || economyInstance == null) return false;

        try {
            EconomyResponse response = economyInstance.bankDeposit(bankName, amount);
            return response.transactionSuccess();
        } catch (Exception e) {
            logger.error("Error depositing to bank: " + bankName, e);
            return false;
        }
    }

    /**
     * Transfers money from one player to another.
     * @param from the player to take money from
     * @param to the player to give money to
     * @param amount the amount to transfer
     * @return true if the transfer was successful
     */
    public static boolean transfer(EntityPlayer from, EntityPlayer to, double amount) {
        return transfer(from.getCommandSenderName(), to.getCommandSenderName(), amount);
    }

    /**
     * Transfers money from one player to another.
     * @param fromPlayerName the player name to take money from
     * @param toPlayerName the player name to give money to
     * @param amount the amount to transfer
     * @return true if the transfer was successful
     */
    public static boolean transfer(String fromPlayerName, String toPlayerName, double amount) {
        if (!has(fromPlayerName, amount)) {
            return false;
        }
        if (!withdrawMoney(fromPlayerName, amount)) {
            return false;
        }
        if (!addMoney(toPlayerName, amount)) {
            // Rollback
            addMoney(fromPlayerName, amount);
            return false;
        }
        return true;
    }
}
