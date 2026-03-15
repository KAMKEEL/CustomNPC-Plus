package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IAuctionClaim;

/**
 * Interface for player trade data (currency and auction claims).
 * Note: If Vault integration is enabled in config, currency operations
 * will use the Vault economy instead of CNPC+ built-in currency.
 */
public interface IPlayerTradeData {

    /**
     * Get the current balance
     *
     * @return the player's current currency balance
     */
    long getBalance();

    /**
     * Set the balance directly
     *
     * @param balance The new balance
     */
    void setBalance(long balance);

    /**
     * Add currency to balance (deposit)
     *
     * @param amount Amount to add
     * @return true if successful, false if would exceed max balance
     */
    boolean deposit(long amount);

    /**
     * Remove currency from balance (withdraw)
     *
     * @param amount Amount to remove
     * @return true if successful, false if insufficient funds
     */
    boolean withdraw(long amount);

    /**
     * Check if player can afford an amount
     *
     * @param amount Amount to check
     * @return true if player has enough balance
     */
    boolean canAfford(long amount);

    /**
     * Get lifetime earned currency
     *
     * @return the total currency earned over the player's lifetime
     */
    long getLifetimeEarned();

    /**
     * Get lifetime spent currency
     *
     * @return the total currency spent over the player's lifetime
     */
    long getLifetimeSpent();

    /**
     * Format the balance for display
     *
     * @return the balance formatted as a human-readable string
     */
    String formatBalance();

    /**
     * Check if Vault is being used for currency operations
     *
     * @return true if Vault is handling currency, false if using CNPC+ built-in
     */
    boolean isUsingVault();

    /**
     * Get the number of pending auction claims
     *
     * @return the number of pending auction claims
     */
    int getClaimCount();

    /**
     * Check if player has any pending auction claims
     *
     * @return true if the player has pending auction claims
     */
    boolean hasClaims();

    /**
     * Get all pending auction claims for this player.
     *
     * @return array of the player's pending auction claims
     */
    IAuctionClaim[] getClaims();

    /**
     * Get a specific claim by ID.
     *
     * @param claimId The claim ID
     * @return The claim, or null if not found
     */
    IAuctionClaim getClaim(String claimId);
}
