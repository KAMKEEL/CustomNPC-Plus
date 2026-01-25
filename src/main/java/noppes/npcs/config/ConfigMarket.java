package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigMarket {
    public static Configuration config;

    public final static String CURRENCY = "Market.Currency";
    public final static String AUCTION = "Market.Auction";
    public final static String AUCTION_BLACKLIST = "Market.Auction.Blacklist";
    public final static String AUCTION_LOGGING = "Market.Auction.Logging";

    // Max trades grid = 9x5 = 45 slots
    public static final int MAX_TRADE_SLOTS = 45;
    public static final int MIN_TRADE_SLOTS = 1;

    // =========================================
    // Currency Settings
    // =========================================
    public static boolean UseVault = false;
    public static String CurrencyName = "Coins";
    public static long StartingBalance = 0;
    public static long MaxBalance = Long.MAX_VALUE;

    // =========================================
    // Auction Settings
    // =========================================
    public static boolean AuctionEnabled = true;
    public static int AuctionDurationHours = 24;
    public static long ListingFee = 10;
    public static long MinimumListingPrice = 1;
    public static double SalesTaxPercent = 0.05;
    public static int DefaultMaxTrades = 8;
    public static int SnipeProtectionMinutes = 2;
    public static int ClaimExpirationDays = 20;
    public static double MinBidIncrementPercent = 0.05;
    public static double CancellationPenaltyPercent = 0.10;

    // =========================================
    // Auction Blacklist Settings
    // =========================================
    public static boolean BlacklistEnabled = true;
    public static String[] BlacklistedItems = new String[]{};
    public static String[] BlacklistedMods = new String[]{};
    public static String[] BlacklistedNBTTags = new String[]{};

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
            // Currency Settings
            // =========================================
            config.setCategoryComment(CURRENCY, "CNPC+ Currency system settings. When UseVault is enabled, all currency operations use Vault instead of the built-in system.");

            UseVault = config.get(CURRENCY, "Use Vault", false,
                "If true, use Vault API for all currency operations instead of CNPC+ built-in currency. The built-in currency data will be preserved but unused.").getBoolean(false);

            CurrencyName = config.get(CURRENCY, "Currency Name", "Coins",
                "Display name for the currency (e.g. 'Coins', 'Gold', 'Credits')").getString();

            StartingBalance = config.get(CURRENCY, "Starting Balance", 0,
                "Starting currency balance for new players").getInt(0);

            MaxBalance = config.get(CURRENCY, "Max Balance", Integer.MAX_VALUE,
                "Maximum currency balance a player can have").getInt(Integer.MAX_VALUE);

            // =========================================
            // Auction Settings
            // =========================================
            config.setCategoryComment(AUCTION, "Auction system settings. The Auction allows players to list items for sale with bidding and buyout options.");

            AuctionEnabled = config.get(AUCTION, "Enable Auction", true,
                "Enable the Auction system").getBoolean(true);

            AuctionDurationHours = config.get(AUCTION, "Auction Duration Hours", 24,
                "Default duration for auctions in hours").getInt(24);

            ListingFee = config.get(AUCTION, "Listing Fee", 10,
                "Flat fee charged when creating a listing").getInt(10);

            MinimumListingPrice = config.get(AUCTION, "Minimum Listing Price", 1,
                "Minimum starting price for auction listings").getInt(1);
            if (MinimumListingPrice < 1) MinimumListingPrice = 1;

            SalesTaxPercent = config.get(AUCTION, "Sales Tax Percent", 0.05,
                "Percentage of sale price taken as tax (0.05 = 5%). Tax is deleted as a currency sink.").getDouble(0.05);

            int maxTrades = config.get(AUCTION, "Default Max Trades", 8,
                "Default maximum number of trade slots per player (listings + bids + claims). Min: 1, Max: 45. Players can have more via customnpcs.auction.trades.X permissions.").getInt(8);
            DefaultMaxTrades = Math.max(MIN_TRADE_SLOTS, Math.min(MAX_TRADE_SLOTS, maxTrades));

            SnipeProtectionMinutes = config.get(AUCTION, "Snipe Protection Minutes", 2,
                "When a bid is placed with less than this many minutes remaining, the auction is extended to this duration").getInt(2);

            ClaimExpirationDays = config.get(AUCTION, "Claim Expiration Days", 20,
                "Number of days before unclaimed items/currency are deleted").getInt(20);

            MinBidIncrementPercent = config.get(AUCTION, "Min Bid Increment Percent", 0.05,
                "Minimum bid increment as a percentage of current bid (0.05 = 5%)").getDouble(0.05);

            CancellationPenaltyPercent = config.get(AUCTION, "Cancellation Penalty Percent", 0.10,
                "Percentage of current bid taken as penalty when seller cancels an auction with bids (0.10 = 10%)").getDouble(0.10);

            // =========================================
            // Auction Blacklist Settings
            // =========================================
            config.setCategoryComment(AUCTION_BLACKLIST,
                "Item Blacklist settings for the Auction House.\n" +
                "Prevents specific items, mods, or items with certain NBT tags from being listed.\n\n" +
                "ITEM FORMAT: Use 'modid:itemname' format (e.g., 'minecraft:bedrock', 'customnpcs:npcWand')\n" +
                "WILDCARDS: Use * for wildcards (e.g., 'customnpcs:npc*' blocks all items starting with 'npc')\n" +
                "MOD FORMAT: Use just the mod ID (e.g., 'projecte' blocks all items from that mod)\n" +
                "NBT FORMAT: Use the NBT tag key name (e.g., 'AdminOnly' blocks items with that tag)");

            BlacklistEnabled = config.get(AUCTION_BLACKLIST, "Enable Blacklist", true,
                "Enable item blacklist checking when creating listings").getBoolean(true);

            BlacklistedItems = config.get(AUCTION_BLACKLIST, "Blacklisted Items", new String[]{
                    "minecraft:bedrock",
                    "minecraft:command_block",
                    "customnpcs:npcWand",
                    "customnpcs:npcMobCloner",
                    "customnpcs:npcScripter",
                    "customnpcs:npcMovingPath",
                    "customnpcs:npcMounter",
                    "customnpcs:npcTeleporter",
                    "customnpcs:npcTool",
                    "customnpcs:npcSoulstoneFilled"
                },
                "Items that cannot be listed on the Auction House.\n" +
                "Format: modid:itemname (supports * wildcards)\n" +
                "Example: 'minecraft:diamond_sword' or 'customnpcs:npc*'").getStringList();

            BlacklistedMods = config.get(AUCTION_BLACKLIST, "Blacklisted Mods", new String[]{},
                "All items from these mods are blocked from the Auction House.\n" +
                "Format: modid (e.g., 'projecte', 'thaumcraft')").getStringList();

            BlacklistedNBTTags = config.get(AUCTION_BLACKLIST, "Blacklisted NBT Tags", new String[]{},
                "Items containing any of these NBT tag keys are blocked.\n" +
                "Checks the root level of the item's NBT compound.\n" +
                "Example: 'AdminOnly', 'CreativeMode'").getStringList();

            // =========================================
            // Auction Logging Settings
            // =========================================
            config.setCategoryComment(AUCTION_LOGGING, "Auction logging settings. Enable specific log types to track auction activity.");

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
            config.setCategoryPropertyOrder(CURRENCY, new ArrayList<>(Arrays.asList(
                "Use Vault",
                "Currency Name",
                "Starting Balance",
                "Max Balance"
            )));

            config.setCategoryPropertyOrder(AUCTION, new ArrayList<>(Arrays.asList(
                "Enable Auction",
                "Auction Duration Hours",
                "Listing Fee",
                "Minimum Listing Price",
                "Sales Tax Percent",
                "Default Max Trades",
                "Snipe Protection Minutes",
                "Claim Expiration Days",
                "Min Bid Increment Percent",
                "Cancellation Penalty Percent"
            )));

            config.setCategoryPropertyOrder(AUCTION_BLACKLIST, new ArrayList<>(Arrays.asList(
                "Enable Blacklist",
                "Blacklisted Items",
                "Blacklisted Mods",
                "Blacklisted NBT Tags"
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
