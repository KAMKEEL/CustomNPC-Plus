/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents an auction listing.
  * @javaFqn noppes.npcs.api.handler.data.IAuctionListing
*/
export interface IAuctionListing {
    /**
     * Get the unique listing ID.
     * @return the unique listing ID
     */
    getId(): String;
    /**
     * Get the seller's UUID as a string.
     * @return the seller's UUID
     */
    getSellerUUID(): String;
    /**
     * Get the seller's display name.
     * @return the seller's display name
     */
    getSellerName(): String;
    /**
     * Get the item being sold.
     * @return the item being auctioned
     */
    getItem(): import('../../item/IItemStack').IItemStack;
    /**
     * Get the starting price.
     * @return the starting bid price in currency
     */
    getStartingPrice(): import('./long').long;
    /**
     * Get the buyout price (0 if no buyout).
     * @return the instant buyout price in currency
     */
    getBuyoutPrice(): import('./long').long;
    /**
     * Check if this listing has a buyout option.
     * @return true if this listing has a buyout option
     */
    hasBuyout(): import('./boolean').boolean;
    /**
     * Get the current highest bid.
     * @return the current highest bid amount
     */
    getCurrentBid(): import('./long').long;
    /**
     * Get the high bidder's UUID as a string (null if no bids).
     * @return the UUID of the current highest bidder
     */
    getHighBidderUUID(): String;
    /**
     * Get the high bidder's display name (null if no bids).
     * @return the name of the current highest bidder
     */
    getHighBidderName(): String;
    /**
     * Check if there are any bids on this listing.
     * @return true if any bids have been placed
     */
    hasBids(): import('./boolean').boolean;
    /**
     * Get the total number of bids placed.
     * @return the total number of bids placed
     */
    getBidCount(): import('./int').int;
    /**
     * Get when the listing was created (Unix timestamp in ms).
     * @return the timestamp when this listing was created
     */
    getCreatedTime(): import('./long').long;
    /**
     * Get when the auction ends (Unix timestamp in ms).
     * @return the timestamp when this listing ends
     */
    getEndTime(): import('./long').long;
    /**
     * Get the remaining time in milliseconds.
     * @return the remaining time in milliseconds
     */
    getRemainingTime(): import('./long').long;
    /**
     * Check if the auction has expired.
     * @return true if the listing has ended
     */
    isExpired(): import('./boolean').boolean;
    /**
     * Check if the auction is still active (can be bid on).
     * @return true if the listing is still accepting bids
     */
    isActive(): import('./boolean').boolean;
    /**
     * Get the auction status.
     *
     * @return 0 = Active, 1 = Ended, 2 = Cancelled, 3 = Claimed
     */
    getStatus(): import('./int').int;
    /**
     * Get the minimum bid amount (current bid + increment).
     * @return the minimum bid amount required
     */
    getMinimumBid(): import('./long').long;
    /**
     * Check if a player UUID is the seller.
     * @param playerUUID the player UUID to check
     * @return true if this player is the seller
     */
    isSeller(playerUUID: String): import('./boolean').boolean;
    /**
     * Check if a player UUID is the current high bidder.
     * @param playerUUID the player UUID to check
     * @return true if this player is the current highest bidder
     */
    isHighBidder(playerUUID: String): import('./boolean').boolean;
    /**
     * Get the remaining time formatted as a string (e.g., "2h 30m").
     * @return the remaining time as a human-readable string
     */
    getRemainingTimeFormatted(): String;
    id: String;
    sellerUUID: import('./UUID').UUID;
    sellerName: String;
    item: import('./ItemStack').ItemStack;
    startingPrice: import('./long').long;
    buyoutPrice: import('./long').long;
    currentBid: import('./long').long;
    highBidderUUID: import('./UUID').UUID;
    highBidderName: String;
    bidCount: import('./int').int;
    createdTime: import('./long').long;
    endTime: import('./long').long;
    status: import('./EnumAuctionStatus').EnumAuctionStatus;
    isGlobalListing: import('./boolean').boolean;
}
