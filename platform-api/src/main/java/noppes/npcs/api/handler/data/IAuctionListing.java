package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents an auction listing.
 */
public interface IAuctionListing {

    /**
     * Get the unique listing ID.
     * @return the unique listing ID
     */
    String getId();

    /**
     * Get the seller's UUID as a string.
     * @return the seller's UUID
     */
    String getSellerUUID();

    /**
     * Get the seller's display name.
     * @return the seller's display name
     */
    String getSellerName();

    /**
     * Get the item being sold.
     * @return the item being auctioned
     */
    IItemStack getItem();

    /**
     * Get the starting price.
     * @return the starting bid price in currency
     */
    long getStartingPrice();

    /**
     * Get the buyout price (0 if no buyout).
     * @return the instant buyout price in currency
     */
    long getBuyoutPrice();

    /**
     * Check if this listing has a buyout option.
     * @return true if this listing has a buyout option
     */
    boolean hasBuyout();

    /**
     * Get the current highest bid.
     * @return the current highest bid amount
     */
    long getCurrentBid();

    /**
     * Get the high bidder's UUID as a string (null if no bids).
     * @return the UUID of the current highest bidder
     */
    String getHighBidderUUID();

    /**
     * Get the high bidder's display name (null if no bids).
     * @return the name of the current highest bidder
     */
    String getHighBidderName();

    /**
     * Check if there are any bids on this listing.
     * @return true if any bids have been placed
     */
    boolean hasBids();

    /**
     * Get the total number of bids placed.
     * @return the total number of bids placed
     */
    int getBidCount();

    /**
     * Get when the listing was created (Unix timestamp in ms).
     * @return the timestamp when this listing was created
     */
    long getCreatedTime();

    /**
     * Get when the auction ends (Unix timestamp in ms).
     * @return the timestamp when this listing ends
     */
    long getEndTime();

    /**
     * Get the remaining time in milliseconds.
     * @return the remaining time in milliseconds
     */
    long getRemainingTime();

    /**
     * Check if the auction has expired.
     * @return true if the listing has ended
     */
    boolean isExpired();

    /**
     * Check if the auction is still active (can be bid on).
     * @return true if the listing is still accepting bids
     */
    boolean isActive();

    /**
     * Get the auction status.
     *
     * @return 0 = Active, 1 = Ended, 2 = Cancelled, 3 = Claimed
     */
    int getStatus();

    /**
     * Get the minimum bid amount (current bid + increment).
     * @return the minimum bid amount required
     */
    long getMinimumBid();

    /**
     * Check if a player UUID is the seller.
     * @param playerUUID the player UUID to check
     * @return true if this player is the seller
     */
    boolean isSeller(String playerUUID);

    /**
     * Check if a player UUID is the current high bidder.
     * @param playerUUID the player UUID to check
     * @return true if this player is the current highest bidder
     */
    boolean isHighBidder(String playerUUID);

    /**
     * Get the remaining time formatted as a string (e.g., "2h 30m").
     * @return the remaining time as a human-readable string
     */
    String getRemainingTimeFormatted();
}
