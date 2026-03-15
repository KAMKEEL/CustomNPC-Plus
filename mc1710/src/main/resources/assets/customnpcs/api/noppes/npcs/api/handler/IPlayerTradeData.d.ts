/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Interface for player trade data (currency and auction claims).
 * Note: If Vault integration is enabled in config, currency operations
 * will use the Vault economy instead of CNPC+ built-in currency.
  * @javaFqn noppes.npcs.api.handler.IPlayerTradeData
*/
export interface IPlayerTradeData {
    /**
     * Get the current balance
     *
     * @return the player's current currency balance
     */
    getBalance(): import('./long').long;
    /**
     * Set the balance directly
     *
     * @param balance The new balance
     */
    setBalance(balance: import('./long').long): import('./void').void;
    /**
     * Add currency to balance (deposit)
     *
     * @param amount Amount to add
     * @return true if successful, false if would exceed max balance
     */
    deposit(amount: import('./long').long): import('./boolean').boolean;
    /**
     * Remove currency from balance (withdraw)
     *
     * @param amount Amount to remove
     * @return true if successful, false if insufficient funds
     */
    withdraw(amount: import('./long').long): import('./boolean').boolean;
    /**
     * Check if player can afford an amount
     *
     * @param amount Amount to check
     * @return true if player has enough balance
     */
    canAfford(amount: import('./long').long): import('./boolean').boolean;
    /**
     * Get lifetime earned currency
     *
     * @return the total currency earned over the player's lifetime
     */
    getLifetimeEarned(): import('./long').long;
    /**
     * Get lifetime spent currency
     *
     * @return the total currency spent over the player's lifetime
     */
    getLifetimeSpent(): import('./long').long;
    /**
     * Format the balance for display
     *
     * @return the balance formatted as a human-readable string
     */
    formatBalance(): String;
    /**
     * Check if Vault is being used for currency operations
     *
     * @return true if Vault is handling currency, false if using CNPC+ built-in
     */
    isUsingVault(): import('./boolean').boolean;
    /**
     * Get the number of pending auction claims
     *
     * @return the number of pending auction claims
     */
    getClaimCount(): import('./int').int;
    /**
     * Check if player has any pending auction claims
     *
     * @return true if the player has pending auction claims
     */
    hasClaims(): import('./boolean').boolean;
    /**
     * Get all pending auction claims for this player.
     *
     * @return array of the player's pending auction claims
     */
    getClaims(): import('./data/IAuctionClaim').IAuctionClaim[];
    /**
     * Get a specific claim by ID.
     *
     * @param claimId The claim ID
     * @return The claim, or null if not found
     */
    getClaim(claimId: String): import('./data/IAuctionClaim').IAuctionClaim;
}
