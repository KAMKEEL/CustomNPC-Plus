package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents an auction claim (item or currency to be collected).
 */
public interface IAuctionClaim {

    /**
     * Get the unique claim ID.
     * @return the unique claim ID
     */
    String getId();

    /**
     * Get the player's UUID who can claim this.
     * @return the UUID of the player who can claim this
     */
    String getPlayerUUID();

    /**
     * Get the player's display name.
     * @return the name of the player who can claim this
     */
    String getPlayerName();

    /**
     * Get the related listing ID.
     * @return the ID of the original auction listing
     */
    String getListingId();

    /**
     * Get the claim type.
     *
     * @return 0 = Item, 1 = Currency (sale proceeds), 2 = Refund (outbid)
     */
    int getType();

    /**
     * Check if this is an item claim.
     * @return true if this claim contains an item to collect
     */
    boolean isItemClaim();

    /**
     * Check if this is a currency claim (sale proceeds or refund).
     * @return true if this claim contains currency to collect
     */
    boolean isCurrencyClaim();

    /**
     * Check if this is a refund claim (outbid).
     * @return true if this is a refund from an outbid or cancelled auction
     */
    boolean isRefundClaim();

    /**
     * Get the item to claim (for item claims only).
     *
     * @return The item, or null for currency claims
     */
    IItemStack getItem();

    /**
     * Get the item display name (for currency/refund claims tooltip).
     * @return the display name of the claimed item
     */
    String getItemName();

    /**
     * Get the currency amount (for currency/refund claims).
     * @return the currency amount to claim
     */
    long getCurrency();

    /**
     * Get the other player's name involved.
     * For currency claims: the buyer's name.
     * For refund claims: the player who outbid.
     * @return the name of the other party in the transaction
     */
    String getOtherPlayerName();

    /**
     * Get when this claim was created (Unix timestamp in ms).
     * @return the timestamp when this claim was created
     */
    long getCreatedTime();

    /**
     * Check if this claim has been claimed.
     * @return true if this claim has already been collected
     */
    boolean isClaimed();

    /**
     * Check if this is a returned item (expired/cancelled listing) vs won item.
     * Only relevant for item claims.
     * @return true if this is a returned unsold item
     */
    boolean isReturnedItem();

    /**
     * Get days until this claim expires.
     * @return the number of days until this claim expires
     */
    long getDaysUntilExpiration();

    /**
     * Check if this claim has expired.
     * @return true if this claim has expired
     */
    boolean isExpired();
}
