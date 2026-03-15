package noppes.npcs.api.handler;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAuctionListing;
import noppes.npcs.api.item.IItemStack;

/**
 * Handler for the Auction system.
 * Access via API.getAuctions()
 */
public interface IAuctionHandler {

    /**
     * Check if the auction system is enabled.
     * @return true if the auction system is enabled
     */
    boolean isEnabled();

    /**
     * Get all active listings.
     * @return array of all currently active auction listings
     */
    IAuctionListing[] getActiveListings();

    /**
     * Get a specific listing by ID.
     *
     * @param listingId The listing ID
     * @return The listing, or null if not found
     */
    IAuctionListing getListing(String listingId);

    /**
     * Get all active listings by a specific seller.
     *
     * @param sellerUUID The seller's UUID as a string
     * @return array of listings created by this seller
     */
    IAuctionListing[] getListingsBySeller(String sellerUUID);

    /**
     * Get all listings where a player is the current high bidder.
     *
     * @param bidderUUID The bidder's UUID as a string
     * @return array of listings this player has bid on
     */
    IAuctionListing[] getListingsByBidder(String bidderUUID);

    /**
     * Get total count of active listings.
     * @return the number of currently active listings
     */
    int getActiveListingCount();

    /**
     * Create a new listing.
     *
     * @param player        The seller
     * @param item          The item to sell
     * @param startingPrice The starting bid price
     * @param buyoutPrice   The buyout price (0 for no buyout)
     * @return The created listing, or null if failed
     */
    IAuctionListing createListing(IPlayer player, IItemStack item, long startingPrice, long buyoutPrice);

    /**
     * Place a bid on a listing.
     *
     * @param listingId The listing ID
     * @param player    The bidder
     * @param amount    The bid amount
     * @return null on success, error message on failure
     */
    String placeBid(String listingId, IPlayer player, long amount);

    /**
     * Buyout a listing instantly.
     *
     * @param listingId The listing ID
     * @param player    The buyer
     * @return null on success, error message on failure
     */
    String buyout(String listingId, IPlayer player);

    /**
     * Cancel a listing (seller only, unless admin).
     *
     * @param listingId The listing ID
     * @param player    The player cancelling
     * @param isAdmin   Whether to bypass ownership check
     * @return null on success, error message on failure
     */
    String cancelListing(String listingId, IPlayer player, boolean isAdmin);

    /**
     * Get the listing fee amount.
     * @return the currency fee charged when creating a listing
     */
    long getListingFee();

    /**
     * Get the sales tax percentage (0.0 to 1.0).
     * @return the sales tax percentage applied to completed sales
     */
    double getSalesTaxPercent();

    /**
     * Get the minimum bid increment percentage (0.0 to 1.0).
     * @return the minimum bid increment as a percentage of current bid
     */
    double getMinBidIncrementPercent();

    /**
     * Get the auction duration in hours.
     * @return the default auction duration in hours
     */
    int getAuctionDurationHours();

    /**
     * Get the snipe protection time in minutes.
     * @return the snipe protection window in minutes
     */
    int getSnipeProtectionMinutes();

    /**
     * Get the currency name used by the auction system.
     * @return the display name of the auction currency
     */
    String getCurrencyName();

    /**
     * Get the minimum listing price.
     * @return the minimum starting price for new listings
     */
    long getMinimumListingPrice();

    /**
     * Search listings by item name or seller name.
     *
     * @param searchText The search text
     * @return Matching active listings
     */
    IAuctionListing[] searchListings(String searchText);

    /**
     * Force save auction data.
     */
    void save();
}
