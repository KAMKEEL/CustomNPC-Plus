package noppes.npcs.controllers.data;

import kamkeel.npcs.util.VaultUtil;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.IPlayerCurrencyData;
import noppes.npcs.config.ConfigMarket;

/**
 * Stores player currency balance.
 * This data is NOT profile slot bound - all profile slots share the same currency pool.
 * When Vault API is available and UseVault config is enabled, operations delegate to Vault.
 * The built-in CNPC+ currency data is always preserved in NBT, but unused when Vault is active.
 */
public class PlayerCurrencyData implements IPlayerCurrencyData {
    private final PlayerData playerData;

    // Built-in CNPC+ currency (always saved, used as fallback)
    private long balance;
    private long lifetimeEarned;
    private long lifetimeSpent;

    public PlayerCurrencyData(PlayerData playerData) {
        this.playerData = playerData;
        this.balance = ConfigMarket.StartingBalance;
        this.lifetimeEarned = 0;
        this.lifetimeSpent = 0;
    }

    /**
     * Check if Vault should be used for currency operations.
     * Requires both config enabled AND Vault actually available.
     */
    private boolean shouldUseVault() {
        return ConfigMarket.UseVault && VaultUtil.isEnabled();
    }

    /**
     * Get player name for Vault operations
     */
    private String getPlayerName() {
        if (playerData != null && playerData.player != null) {
            return playerData.player.getCommandSenderName();
        }
        return playerData != null ? playerData.playername : null;
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("CurrencyBalance")) {
            this.balance = compound.getLong("CurrencyBalance");
        } else {
            this.balance = ConfigMarket.StartingBalance;
        }
        this.lifetimeEarned = compound.getLong("CurrencyLifetimeEarned");
        this.lifetimeSpent = compound.getLong("CurrencyLifetimeSpent");
    }

    public void writeToNBT(NBTTagCompound compound) {
        // Always save built-in currency data (preserved even when using Vault)
        compound.setLong("CurrencyBalance", this.balance);
        compound.setLong("CurrencyLifetimeEarned", this.lifetimeEarned);
        compound.setLong("CurrencyLifetimeSpent", this.lifetimeSpent);
    }

    /**
     * Get the current balance
     */
    @Override
    public long getBalance() {
        if (shouldUseVault()) {
            String playerName = getPlayerName();
            if (playerName != null) {
                return (long) VaultUtil.getBalance(playerName);
            }
        }
        return balance;
    }

    /**
     * Set the balance directly (use with caution)
     */
    @Override
    public void setBalance(long balance) {
        if (balance < 0) {
            balance = 0;
        }
        if (balance > ConfigMarket.MaxBalance) {
            balance = ConfigMarket.MaxBalance;
        }

        if (shouldUseVault()) {
            String playerName = getPlayerName();
            if (playerName != null) {
                VaultUtil.setBalance(playerName, balance);
                return;
            }
        }
        this.balance = balance;
    }

    /**
     * Add currency to balance (deposit)
     *
     * @param amount Amount to add
     * @return true if successful, false if would exceed max balance
     */
    @Override
    public boolean deposit(long amount) {
        if (amount <= 0) {
            return false;
        }

        if (shouldUseVault()) {
            String playerName = getPlayerName();
            if (playerName != null) {
                boolean success = VaultUtil.addMoney(playerName, amount);
                if (success) {
                    this.lifetimeEarned += amount;
                }
                return success;
            }
        }

        // Built-in currency
        long newBalance = this.balance + amount;
        if (newBalance > ConfigMarket.MaxBalance) {
            return false;
        }
        if (newBalance < this.balance) {
            // Overflow protection
            return false;
        }
        this.balance = newBalance;
        this.lifetimeEarned += amount;
        return true;
    }

    /**
     * Remove currency from balance (withdraw)
     *
     * @param amount Amount to remove
     * @return true if successful, false if insufficient funds
     */
    @Override
    public boolean withdraw(long amount) {
        if (amount <= 0) {
            return false;
        }

        if (shouldUseVault()) {
            String playerName = getPlayerName();
            if (playerName != null) {
                boolean success = VaultUtil.withdrawMoney(playerName, amount);
                if (success) {
                    this.lifetimeSpent += amount;
                }
                return success;
            }
        }

        // Built-in currency
        if (this.balance < amount) {
            return false;
        }
        this.balance -= amount;
        this.lifetimeSpent += amount;
        return true;
    }

    /**
     * Check if player can afford an amount
     */
    @Override
    public boolean canAfford(long amount) {
        if (shouldUseVault()) {
            String playerName = getPlayerName();
            if (playerName != null) {
                return VaultUtil.has(playerName, amount);
            }
        }
        return this.balance >= amount;
    }

    /**
     * Get lifetime earned currency
     */
    @Override
    public long getLifetimeEarned() {
        return lifetimeEarned;
    }

    /**
     * Get lifetime spent currency
     */
    @Override
    public long getLifetimeSpent() {
        return lifetimeSpent;
    }

    /**
     * Format the balance for display
     */
    @Override
    public String formatBalance() {
        if (shouldUseVault()) {
            return VaultUtil.format(getBalance());
        }
        return formatAmount(this.balance);
    }

    /**
     * Format an amount for display
     */
    public static String formatAmount(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }

    /**
     * Check if Vault is being used for currency operations.
     * Returns true only if config is enabled AND Vault is actually available.
     */
    @Override
    public boolean isUsingVault() {
        return shouldUseVault();
    }

    /**
     * Check if Vault is configured (regardless of availability)
     */
    public boolean isVaultConfigured() {
        return ConfigMarket.UseVault;
    }
}
