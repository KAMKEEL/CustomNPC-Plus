/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents an auction claim (item or currency to be collected).
  * @javaFqn noppes.npcs.api.handler.data.IAuctionClaim
*/
export interface IAuctionClaim {
    /**
     * Get the unique claim ID.
     * @return the unique claim ID
     */
    getId(): String;
    /**
     * Get the player's UUID who can claim this.
     * @return the UUID of the player who can claim this
     */
    getPlayerUUID(): String;
    /**
     * Get the player's display name.
     * @return the name of the player who can claim this
     */
    getPlayerName(): String;
    /**
     * Get the related listing ID.
     * @return the ID of the original auction listing
     */
    getListingId(): String;
    /**
     * Get the claim type.
     *
     * @return 0 = Item, 1 = Currency (sale proceeds), 2 = Refund (outbid)
     */
    getType(): import('./int').int;
    /**
     * Check if this is an item claim.
     * @return true if this claim contains an item to collect
     */
    isItemClaim(): import('./boolean').boolean;
    /**
     * Check if this is a currency claim (sale proceeds or refund).
     * @return true if this claim contains currency to collect
     */
    isCurrencyClaim(): import('./boolean').boolean;
    /**
     * Check if this is a refund claim (outbid).
     * @return true if this is a refund from an outbid or cancelled auction
     */
    isRefundClaim(): import('./boolean').boolean;
    /**
     * Get the item to claim (for item claims only).
     *
     * @return The item, or null for currency claims
     */
    getItem(): import('../../item/IItemStack').IItemStack;
    /**
     * Get the item display name (for currency/refund claims tooltip).
     * @return the display name of the claimed item
     */
    getItemName(): String;
    /**
     * Get the currency amount (for currency/refund claims).
     * @return the currency amount to claim
     */
    getCurrency(): import('./long').long;
    /**
     * Get the other player's name involved.
     * For currency claims: the buyer's name.
     * For refund claims: the player who outbid.
     * @return the name of the other party in the transaction
     */
    getOtherPlayerName(): String;
    /**
     * Get when this claim was created (Unix timestamp in ms).
     * @return the timestamp when this claim was created
     */
    getCreatedTime(): import('./long').long;
    /**
     * Check if this claim has been claimed.
     * @return true if this claim has already been collected
     */
    isClaimed(): import('./boolean').boolean;
    /**
     * Check if this is a returned item (expired/cancelled listing) vs won item.
     * Only relevant for item claims.
     * @return true if this is a returned unsold item
     */
    isReturnedItem(): import('./boolean').boolean;
    /**
     * Get days until this claim expires.
     * @return the number of days until this claim expires
     */
    getDaysUntilExpiration(): import('./long').long;
    /**
     * Check if this claim has expired.
     * @return true if this claim has expired
     */
    isExpired(): import('./boolean').boolean;
    id: String;
    playerUUID: import('./UUID').UUID;
    playerName: String;
    listingId: String;
    type: import('./EnumClaimType').EnumClaimType;
    item: import('./ItemStack').ItemStack;
    itemName: String;
    otherPlayerName: String;
    currency: import('./long').long;
    createdTime: import('./long').long;
    claimed: import('./boolean').boolean;
    isReturned: import('./boolean').boolean;
}
