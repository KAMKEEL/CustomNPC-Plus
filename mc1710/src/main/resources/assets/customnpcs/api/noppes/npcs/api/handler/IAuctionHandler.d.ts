/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handler for the Auction system.
 * Access via API.getAuctions()
  * @javaFqn noppes.npcs.api.handler.IAuctionHandler
*/
export interface IAuctionHandler {
    /**
     * Check if the auction system is enabled.
     * @return true if the auction system is enabled
     */
    isEnabled(): import('./boolean').boolean;
    /**
     * Get all active listings.
     * @return array of all currently active auction listings
     */
    getActiveListings(): import('./data/IAuctionListing').IAuctionListing[];
    /**
     * Get a specific listing by ID.
     *
     * @param listingId The listing ID
     * @return The listing, or null if not found
     */
    getListing(listingId: String): import('./data/IAuctionListing').IAuctionListing;
    /**
     * Get all active listings by a specific seller.
     *
     * @param sellerUUID The seller's UUID as a string
     * @return array of listings created by this seller
     */
    getListingsBySeller(sellerUUID: String): import('./data/IAuctionListing').IAuctionListing[];
    /**
     * Get all listings where a player is the current high bidder.
     *
     * @param bidderUUID The bidder's UUID as a string
     * @return array of listings this player has bid on
     */
    getListingsByBidder(bidderUUID: String): import('./data/IAuctionListing').IAuctionListing[];
    /**
     * Get total count of active listings.
     * @return the number of currently active listings
     */
    getActiveListingCount(): import('./int').int;
    /**
     * Create a new listing.
     *
     * @param player        The seller
     * @param item          The item to sell
     * @param startingPrice The starting bid price
     * @param buyoutPrice   The buyout price (0 for no buyout)
     * @return The created listing, or null if failed
     */
    createListing(player: import('../entity/IPlayer').IPlayer, item: import('../item/IItemStack').IItemStack, startingPrice: import('./long').long, buyoutPrice: import('./long').long): import('./data/IAuctionListing').IAuctionListing;
    /**
     * Place a bid on a listing.
     *
     * @param listingId The listing ID
     * @param player    The bidder
     * @param amount    The bid amount
     * @return null on success, error message on failure
     */
    placeBid(listingId: String, player: import('../entity/IPlayer').IPlayer, amount: import('./long').long): String;
    /**
     * Buyout a listing instantly.
     *
     * @param listingId The listing ID
     * @param player    The buyer
     * @return null on success, error message on failure
     */
    buyout(listingId: String, player: import('../entity/IPlayer').IPlayer): String;
    /**
     * Cancel a listing (seller only, unless admin).
     *
     * @param listingId The listing ID
     * @param player    The player cancelling
     * @param isAdmin   Whether to bypass ownership check
     * @return null on success, error message on failure
     */
    cancelListing(listingId: String, player: import('../entity/IPlayer').IPlayer, isAdmin: import('./boolean').boolean): String;
    /**
     * Get the listing fee amount.
     * @return the currency fee charged when creating a listing
     */
    getListingFee(): import('./long').long;
    /**
     * Get the sales tax percentage (0.0 to 1.0).
     * @return the sales tax percentage applied to completed sales
     */
    getSalesTaxPercent(): import('./double').double;
    /**
     * Get the minimum bid increment percentage (0.0 to 1.0).
     * @return the minimum bid increment as a percentage of current bid
     */
    getMinBidIncrementPercent(): import('./double').double;
    /**
     * Get the auction duration in hours.
     * @return the default auction duration in hours
     */
    getAuctionDurationHours(): import('./int').int;
    /**
     * Get the snipe protection time in minutes.
     * @return the snipe protection window in minutes
     */
    getSnipeProtectionMinutes(): import('./int').int;
    /**
     * Get the currency name used by the auction system.
     * @return the display name of the auction currency
     */
    getCurrencyName(): String;
    /**
     * Get the minimum listing price.
     * @return the minimum starting price for new listings
     */
    getMinimumListingPrice(): import('./long').long;
    /**
     * Search listings by item name or seller name.
     *
     * @param searchText The search text
     * @return Matching active listings
     */
    searchListings(searchText: String): import('./data/IAuctionListing').IAuctionListing[];
    /**
     * Force save auction data.
     */
    save(): import('./void').void;
}
