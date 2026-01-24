package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigMarket {
    public static Configuration config;

    public final static String MARKET = "Market";
    public final static String CURRENCY = "Market.Currency";
    public final static String TRADER = "Market.Trader";
    public final static String AUCTION = "Market.Auction";
    public final static String AUCTION_LOGGING = "Market.Auction.Logging";

    // =========================================
    // Market General
    // =========================================
    public static boolean MarketEnabled = true;

    // =========================================
    // Currency Settings
    // =========================================
    public static boolean UseVault = false;
    public static String CurrencyName = "Coins";
    public static long StartingBalance = 0;
    public static long MaxBalance = Long.MAX_VALUE;

    // =========================================
    // Trader Settings
    // =========================================
    public static boolean EnableStockByDefault = false;
    public static String DefaultResetType = "NONE";

    // =========================================
    // Auction Settings
    // =========================================
    public static boolean AuctionEnabled = true;
    public static int AuctionDurationHours = 24;
    public static long ListingFee = 10;
    public static double SalesTaxPercent = 0.05;
    public static int DefaultMaxListings = 5;
    public static int SnipeProtectionMinutes = 2;
    public static int ClaimExpirationDays = 20;
    public static double MinBidIncrementPercent = 0.05;
    public static double CancellationPenaltyPercent = 0.10;

    // =========================================
    // Auction Logging Settings
    // =========================================
    public static boolean AuctionLoggingEnabled = false;
    public static boolean LogAuctionCreated = true;
    public static boolean LogAuctionBid = true;
    public static boolean LogAuctionBuyout = true;
    public static boolean LogAuctionSold = true;
    public static boolean LogAuctionExpired = true;
    public static boolean LogAuctionCancelled = true;
    public static boolean LogAuctionClaimed = true;

    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // =========================================
            // Market General
            // =========================================
            MarketEnabled = config.get(MARKET, "Enable Market System", true,
                "Enable the market/economy system").getBoolean(true);

            // =========================================
            // Currency Settings
            // =========================================
            config.setCategoryComment(CURRENCY, "CNPC+ Currency system settings. When UseVault is enabled, all currency operations use Vault instead of the built-in system.");

            UseVault = config.get(CURRENCY, "Use Vault", true,
                "If true, use Vault API for all currency operations instead of CNPC+ built-in currency. The built-in currency data will be preserved but unused.").getBoolean(false);

            CurrencyName = config.get(CURRENCY, "Currency Name", "Coins",
                "Display name for the currency (e.g. 'Coins', 'Gold', 'Credits')").getString();

            StartingBalance = config.get(CURRENCY, "Starting Balance", 0,
                "Starting currency balance for new players").getInt(0);

            MaxBalance = config.get(CURRENCY, "Max Balance", Integer.MAX_VALUE,
                "Maximum currency balance a player can have").getInt(Integer.MAX_VALUE);

            // =========================================
            // Trader Settings
            // =========================================
            config.setCategoryComment(TRADER, "Trader stock system settings");

            EnableStockByDefault = config.get(TRADER, "Enable Stock By Default", false,
                "Enable stock system by default for new traders").getBoolean(false);

            DefaultResetType = config.get(TRADER, "Default Reset Type", "NONE",
                "Default stock reset type (NONE, MCDAILY, MCWEEKLY, RLDAILY, RLWEEKLY)").getString();

            // =========================================
            // Auction Settings
            // =========================================
            config.setCategoryComment(AUCTION, "Auction House system settings. The Auction House allows players to list items for sale with bidding and buyout options.");

            AuctionEnabled = config.get(AUCTION, "Enable Auction House", true,
                "Enable the Auction House system").getBoolean(true);

            AuctionDurationHours = config.get(AUCTION, "Auction Duration Hours", 24,
                "Default duration for auctions in hours").getInt(24);

            ListingFee = config.get(AUCTION, "Listing Fee", 10,
                "Flat fee charged when creating a listing").getInt(10);

            SalesTaxPercent = config.get(AUCTION, "Sales Tax Percent", 0.05,
                "Percentage of sale price taken as tax (0.05 = 5%). Tax is deleted as a currency sink.").getDouble(0.05);

            DefaultMaxListings = config.get(AUCTION, "Default Max Listings", 5,
                "Default maximum number of active listings per player").getInt(5);

            SnipeProtectionMinutes = config.get(AUCTION, "Snipe Protection Minutes", 2,
                "When a bid is placed with less than this many minutes remaining, the auction is extended to this duration").getInt(2);

            ClaimExpirationDays = config.get(AUCTION, "Claim Expiration Days", 20,
                "Number of days before unclaimed items/currency are deleted").getInt(20);

            MinBidIncrementPercent = config.get(AUCTION, "Min Bid Increment Percent", 0.05,
                "Minimum bid increment as a percentage of current bid (0.05 = 5%)").getDouble(0.05);

            CancellationPenaltyPercent = config.get(AUCTION, "Cancellation Penalty Percent", 0.10,
                "Percentage of current bid taken as penalty when seller cancels an auction with bids (0.10 = 10%)").getDouble(0.10);

            // =========================================
            // Auction Logging Settings
            // =========================================
            config.setCategoryComment(AUCTION_LOGGING, "Auction House logging settings. Enable specific log types to track auction activity.");

            AuctionLoggingEnabled = config.get(AUCTION_LOGGING, "Enable Auction Logging", false,
                "Master switch for auction logging. If false, no auction events are logged.").getBoolean(false);

            LogAuctionCreated = config.get(AUCTION_LOGGING, "Log Created", true,
                "Log when auctions are created").getBoolean(true);

            LogAuctionBid = config.get(AUCTION_LOGGING, "Log Bid", true,
                "Log when bids are placed").getBoolean(true);

            LogAuctionBuyout = config.get(AUCTION_LOGGING, "Log Buyout", true,
                "Log when auctions are bought out").getBoolean(true);

            LogAuctionSold = config.get(AUCTION_LOGGING, "Log Sold", true,
                "Log when auctions end with a winner").getBoolean(true);

            LogAuctionExpired = config.get(AUCTION_LOGGING, "Log Expired", true,
                "Log when auctions expire with no bids").getBoolean(true);

            LogAuctionCancelled = config.get(AUCTION_LOGGING, "Log Cancelled", true,
                "Log when auctions are cancelled").getBoolean(true);

            LogAuctionClaimed = config.get(AUCTION_LOGGING, "Log Claimed", true,
                "Log when claims are collected").getBoolean(true);

            // Set category order
            config.setCategoryPropertyOrder(MARKET, new ArrayList<>(Arrays.asList(
                "Enable Market System"
            )));

            config.setCategoryPropertyOrder(CURRENCY, new ArrayList<>(Arrays.asList(
                "Use Vault",
                "Currency Name",
                "Starting Balance",
                "Max Balance"
            )));

            config.setCategoryPropertyOrder(TRADER, new ArrayList<>(Arrays.asList(
                "Enable Stock By Default",
                "Default Reset Type"
            )));

            config.setCategoryPropertyOrder(AUCTION, new ArrayList<>(Arrays.asList(
                "Enable Auction House",
                "Auction Duration Hours",
                "Listing Fee",
                "Sales Tax Percent",
                "Default Max Listings",
                "Snipe Protection Minutes",
                "Claim Expiration Days",
                "Min Bid Increment Percent",
                "Cancellation Penalty Percent"
            )));

            config.setCategoryPropertyOrder(AUCTION_LOGGING, new ArrayList<>(Arrays.asList(
                "Enable Auction Logging",
                "Log Created",
                "Log Bid",
                "Log Buyout",
                "Log Sold",
                "Log Expired",
                "Log Cancelled",
                "Log Claimed"
            )));

        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its market configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
