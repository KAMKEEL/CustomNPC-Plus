package noppes.npcs.controllers;

import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionStatus;
import noppes.npcs.controllers.data.AuctionListing;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles auction history logging to the CNPC+ logging system.
 * All auction events are recorded for administrative review.
 */
public class AuctionLogger {
    private static final String PREFIX = "[Auction] ";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Log when a new auction listing is created
     */
    public static void logListingCreated(AuctionListing listing) {
        String msg = String.format("%sLISTING_CREATED: ID=%d, Seller=%s (%s), Item=%s x%d, StartPrice=%d, Buyout=%s, Duration=%s",
            PREFIX,
            listing.id,
            listing.sellerName,
            listing.sellerUUID.toString(),
            listing.item.getDisplayName(),
            listing.item.stackSize,
            listing.startingPrice,
            listing.buyoutPrice > 0 ? String.valueOf(listing.buyoutPrice) : "None",
            formatDuration(listing.expirationTime - listing.listingTime)
        );
        LogWriter.info(msg);
    }

    /**
     * Log when a bid is placed
     */
    public static void logBidPlaced(AuctionListing listing, String bidderName, long bidAmount) {
        String msg = String.format("%sBID_PLACED: ID=%d, Item=%s, Bidder=%s, Amount=%d %s, Previous=%d %s",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            bidderName,
            bidAmount,
            ConfigMarket.CurrencyName,
            listing.currentBid,
            ConfigMarket.CurrencyName
        );
        LogWriter.info(msg);
    }

    /**
     * Log when a player is outbid
     */
    public static void logOutbid(AuctionListing listing, String outbidPlayerName, long previousBid,
                                  String newBidderName, long newBid) {
        String msg = String.format("%sOUTBID: ID=%d, Item=%s, OutbidPlayer=%s (had %d), NewBidder=%s (bid %d)",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            outbidPlayerName,
            previousBid,
            newBidderName,
            newBid
        );
        LogWriter.info(msg);
    }

    /**
     * Log when a buyout occurs
     */
    public static void logBuyout(AuctionListing listing, String buyerName) {
        String msg = String.format("%sBUYOUT: ID=%d, Item=%s, Buyer=%s, Price=%d %s, Seller=%s",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            buyerName,
            listing.buyoutPrice,
            ConfigMarket.CurrencyName,
            listing.sellerName
        );
        LogWriter.info(msg);
    }

    /**
     * Log when an auction is won (ended with bids)
     */
    public static void logAuctionWon(AuctionListing listing) {
        String msg = String.format("%sAUCTION_WON: ID=%d, Item=%s, Winner=%s (%s), FinalPrice=%d %s, Seller=%s, SellerProceeds=%d %s",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            listing.highBidderName,
            listing.highBidderUUID != null ? listing.highBidderUUID.toString() : "N/A",
            listing.currentBid,
            ConfigMarket.CurrencyName,
            listing.sellerName,
            listing.getSellerProceeds(),
            ConfigMarket.CurrencyName
        );
        LogWriter.info(msg);
    }

    /**
     * Log when an auction sells (same as won, for notification controller compatibility)
     */
    public static void logAuctionSold(AuctionListing listing) {
        String msg = String.format("%sAUCTION_SOLD: ID=%d, Item=%s, Buyer=%s, SalePrice=%d %s, Seller=%s (%s), Proceeds=%d %s",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            listing.highBidderName,
            listing.currentBid,
            ConfigMarket.CurrencyName,
            listing.sellerName,
            listing.sellerUUID.toString(),
            listing.getSellerProceeds(),
            ConfigMarket.CurrencyName
        );
        LogWriter.info(msg);
    }

    /**
     * Log when an auction expires with no bids
     */
    public static void logAuctionExpired(AuctionListing listing) {
        String msg = String.format("%sAUCTION_EXPIRED: ID=%d, Item=%s, Seller=%s (%s), StartPrice=%d %s, Duration=%s",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            listing.sellerName,
            listing.sellerUUID.toString(),
            listing.startingPrice,
            ConfigMarket.CurrencyName,
            formatDuration(listing.expirationTime - listing.listingTime)
        );
        LogWriter.info(msg);
    }

    /**
     * Log when an auction is cancelled
     */
    public static void logAuctionCancelled(AuctionListing listing, boolean byAdmin) {
        String msg = String.format("%sAUCTION_CANCELLED: ID=%d, Item=%s, Seller=%s, CancelledBy=%s, HadBids=%b",
            PREFIX,
            listing.id,
            listing.item.getDisplayName(),
            listing.sellerName,
            byAdmin ? "ADMIN" : "SELLER",
            listing.currentBid > 0
        );
        LogWriter.info(msg);
    }

    /**
     * Log when an item is claimed
     */
    public static void logItemClaimed(AuctionListing listing, String claimerName, boolean isSeller) {
        String claimType = isSeller ?
            (listing.status == EnumAuctionStatus.EXPIRED ? "RECLAIMED_EXPIRED" : "CLAIMED_PROCEEDS") :
            "CLAIMED_WON_ITEM";

        String msg = String.format("%s%s: ID=%d, Item=%s, Claimer=%s, Status=%s",
            PREFIX,
            claimType,
            listing.id,
            listing.item.getDisplayName(),
            claimerName,
            listing.status.name()
        );
        LogWriter.info(msg);
    }

    /**
     * Log currency claimed from auction
     */
    public static void logCurrencyClaimed(AuctionListing listing, String claimerName, long amount) {
        String msg = String.format("%sCURRENCY_CLAIMED: ID=%d, Claimer=%s, Amount=%d %s",
            PREFIX,
            listing.id,
            claimerName,
            amount,
            ConfigMarket.CurrencyName
        );
        LogWriter.info(msg);
    }

    /**
     * Log refund of bid to outbid player
     */
    public static void logBidRefunded(AuctionListing listing, String playerName, long amount) {
        String msg = String.format("%sBID_REFUNDED: ID=%d, Player=%s, Amount=%d %s",
            PREFIX,
            listing.id,
            playerName,
            amount,
            ConfigMarket.CurrencyName
        );
        LogWriter.info(msg);
    }

    /**
     * Log admin actions on auctions
     */
    public static void logAdminAction(String adminName, String action, AuctionListing listing) {
        String msg = String.format("%sADMIN_ACTION: Admin=%s, Action=%s, ListingID=%d, Item=%s, Seller=%s",
            PREFIX,
            adminName,
            action,
            listing.id,
            listing.item.getDisplayName(),
            listing.sellerName
        );
        LogWriter.info(msg);
    }

    /**
     * Log auction system startup
     */
    public static void logSystemStartup(int activeListings, int pendingClaims) {
        String msg = String.format("%sSYSTEM_STARTUP: ActiveListings=%d, PendingClaims=%d, Timestamp=%s",
            PREFIX,
            activeListings,
            pendingClaims,
            dateFormat.format(new Date())
        );
        LogWriter.info(msg);
    }

    /**
     * Log auction processing tick
     */
    public static void logProcessingTick(int expired, int completed) {
        if (expired > 0 || completed > 0) {
            String msg = String.format("%sPROCESSING_TICK: ExpiredListings=%d, CompletedAuctions=%d",
                PREFIX,
                expired,
                completed
            );
            LogWriter.info(msg);
        }
    }

    /**
     * Log errors in the auction system
     */
    public static void logError(String context, Exception e) {
        String msg = String.format("%sERROR: Context=%s, Message=%s",
            PREFIX,
            context,
            e.getMessage()
        );
        LogWriter.error(msg, e);
    }

    /**
     * Format duration in a human-readable format
     */
    private static String formatDuration(long millis) {
        long hours = millis / (1000 * 60 * 60);
        if (hours >= 24) {
            long days = hours / 24;
            return days + "d " + (hours % 24) + "h";
        }
        return hours + "h";
    }
}
