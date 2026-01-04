package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.config.ConfigMarket;

/**
 * Stores player currency balance.
 * This data is NOT profile slot bound - all profile slots share the same currency pool.
 * When Vault API is available, this serves as a cache/fallback.
 */
public class PlayerCurrencyData {
    private long balance;
    private long lifetimeEarned;
    private long lifetimeSpent;

    public PlayerCurrencyData() {
        this.balance = ConfigMarket.StartingBalance;
        this.lifetimeEarned = 0;
        this.lifetimeSpent = 0;
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
        compound.setLong("CurrencyBalance", this.balance);
        compound.setLong("CurrencyLifetimeEarned", this.lifetimeEarned);
        compound.setLong("CurrencyLifetimeSpent", this.lifetimeSpent);
    }

    /**
     * Get the current balance
     */
    public long getBalance() {
        return balance;
    }

    /**
     * Set the balance directly (use with caution)
     */
    public void setBalance(long balance) {
        if (balance < 0) {
            balance = 0;
        }
        if (balance > ConfigMarket.MaxBalance) {
            balance = ConfigMarket.MaxBalance;
        }
        this.balance = balance;
    }

    /**
     * Add currency to balance (deposit)
     * @param amount Amount to add
     * @return true if successful, false if would exceed max balance
     */
    public boolean deposit(long amount) {
        if (amount <= 0) {
            return false;
        }
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
     * @param amount Amount to remove
     * @return true if successful, false if insufficient funds
     */
    public boolean withdraw(long amount) {
        if (amount <= 0) {
            return false;
        }
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
    public boolean canAfford(long amount) {
        return this.balance >= amount;
    }

    /**
     * Get lifetime earned currency
     */
    public long getLifetimeEarned() {
        return lifetimeEarned;
    }

    /**
     * Get lifetime spent currency
     */
    public long getLifetimeSpent() {
        return lifetimeSpent;
    }

    /**
     * Format the balance for display
     */
    public String formatBalance() {
        return formatAmount(this.balance);
    }

    /**
     * Format an amount for display
     */
    public static String formatAmount(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }
}
