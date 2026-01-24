package noppes.npcs.controllers.data;

import kamkeel.npcs.util.VaultUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.IPlayerTradeData;
import noppes.npcs.api.handler.data.IAuctionClaim;
import noppes.npcs.config.ConfigMarket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores player's trade-related data: currency and auction claims.
 * This data is shared across all profile slots.
 * Uses CopyOnWriteArrayList for thread-safety when adding claims asynchronously.
 */
public class PlayerTradeData implements IPlayerTradeData {
    // NBT key used by trade data - excluded from per-slot profile saving
    public static final String NBT_KEY = "TradeData";

    private final PlayerData playerData;

    // =========================================
    // Currency Data
    // =========================================

    // Built-in CNPC+ currency (always saved, used as fallback)
    private long balance;
    private long lifetimeEarned;
    private long lifetimeSpent;

    // =========================================
    // Auction Claims Data
    // =========================================

    // Thread-safe list for claims - can be modified from CNPC+ thread
    private final CopyOnWriteArrayList<AuctionClaim> claims = new CopyOnWriteArrayList<>();

    public PlayerTradeData(PlayerData playerData) {
        this.playerData = playerData;
        this.balance = ConfigMarket.StartingBalance;
        this.lifetimeEarned = 0;
        this.lifetimeSpent = 0;
    }

    // =========================================
    // Currency Methods
    // =========================================

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

    // =========================================
    // Auction Claims Methods
    // =========================================

    /**
     * Add a claim to this player's pending claims.
     * Thread-safe - can be called from any thread.
     */
    public void addClaim(AuctionClaim claim) {
        if (claim != null && !claim.claimed) {
            claims.add(claim);
        }
    }

    /**
     * Get all unclaimed claims for this player (internal use).
     * Returns a copy of the list for safe iteration.
     */
    public List<AuctionClaim> getClaimsList() {
        List<AuctionClaim> result = new ArrayList<>();
        for (AuctionClaim claim : claims) {
            if (!claim.claimed) {
                result.add(claim);
            }
        }
        return result;
    }

    /**
     * Get all unclaimed claims for this player (API).
     */
    @Override
    public IAuctionClaim[] getClaims() {
        List<AuctionClaim> result = getClaimsList();
        return result.toArray(new IAuctionClaim[0]);
    }

    /**
     * Get a specific claim by ID (internal use).
     */
    public AuctionClaim getClaimInternal(String claimId) {
        for (AuctionClaim claim : claims) {
            if (claim.id.equals(claimId) && !claim.claimed) {
                return claim;
            }
        }
        return null;
    }

    /**
     * Get a specific claim by ID (API).
     */
    @Override
    public IAuctionClaim getClaim(String claimId) {
        return getClaimInternal(claimId);
    }

    /**
     * Mark a claim as claimed and remove it.
     */
    public boolean claimAndRemove(String claimId) {
        for (AuctionClaim claim : claims) {
            if (claim.id.equals(claimId) && !claim.claimed) {
                claim.claimed = true;
                claims.remove(claim);
                return true;
            }
        }
        return false;
    }

    /**
     * Get count of unclaimed claims.
     */
    @Override
    public int getClaimCount() {
        int count = 0;
        for (AuctionClaim claim : claims) {
            if (!claim.claimed) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if player has any unclaimed claims.
     */
    @Override
    public boolean hasClaims() {
        for (AuctionClaim claim : claims) {
            if (!claim.claimed) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process expired claims - removes claims older than expiration days.
     * Returns number of claims removed.
     */
    public int processExpiredClaims() {
        int expirationDays = ConfigMarket.ClaimExpirationDays;
        int removed = 0;

        Iterator<AuctionClaim> iterator = claims.iterator();
        while (iterator.hasNext()) {
            AuctionClaim claim = iterator.next();
            if (!claim.claimed && claim.isExpired(expirationDays)) {
                claims.remove(claim);
                removed++;
            }
        }

        return removed;
    }

    // =========================================
    // NBT Serialization
    // =========================================

    public void readFromNBT(NBTTagCompound compound) {
        NBTTagCompound tradeData = compound.getCompoundTag(NBT_KEY);
        if (tradeData == null || tradeData.hasNoTags()) {
            // No trade data found, use defaults
            this.balance = ConfigMarket.StartingBalance;
            this.lifetimeEarned = 0;
            this.lifetimeSpent = 0;
            claims.clear();
            return;
        }

        // Currency
        if (tradeData.hasKey("CurrencyBalance")) {
            this.balance = tradeData.getLong("CurrencyBalance");
        } else {
            this.balance = ConfigMarket.StartingBalance;
        }
        this.lifetimeEarned = tradeData.getLong("CurrencyLifetimeEarned");
        this.lifetimeSpent = tradeData.getLong("CurrencyLifetimeSpent");

        // Claims
        claims.clear();
        if (tradeData.hasKey("AuctionClaims")) {
            NBTTagList claimsList = tradeData.getTagList("AuctionClaims", 10);
            for (int i = 0; i < claimsList.tagCount(); i++) {
                AuctionClaim claim = AuctionClaim.fromNBT(claimsList.getCompoundTagAt(i));
                if (!claim.claimed) {
                    claims.add(claim);
                }
            }
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagCompound tradeData = new NBTTagCompound();

        // Currency - always save built-in currency data (preserved even when using Vault)
        tradeData.setLong("CurrencyBalance", this.balance);
        tradeData.setLong("CurrencyLifetimeEarned", this.lifetimeEarned);
        tradeData.setLong("CurrencyLifetimeSpent", this.lifetimeSpent);

        // Claims
        NBTTagList claimsList = new NBTTagList();
        for (AuctionClaim claim : claims) {
            if (!claim.claimed) {
                claimsList.appendTag(claim.writeToNBT(new NBTTagCompound()));
            }
        }
        tradeData.setTag("AuctionClaims", claimsList);

        compound.setTag(NBT_KEY, tradeData);
    }
}
