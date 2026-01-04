package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigMarket {
    public static Configuration config;

    public final static String MARKET = "Market";
    public final static String CURRENCY = "Market.Currency";
    public final static String AUCTION = "Market.Auction";
    public final static String TRADER = "Market.Trader";

    // =========================================
    // Market General
    // =========================================
    public static boolean MarketEnabled = true;

    // =========================================
    // Currency Settings
    // =========================================
    public static boolean UseVaultIfAvailable = true;
    public static String CurrencyName = "Gold";
    public static String FallbackCurrencyItem = "customnpcs:npcCoinGold";
    public static long FallbackItemRatio = 1;
    public static long StartingBalance = 1000;
    public static long MaxBalance = 999999999999L;

    // =========================================
    // Auction Settings
    // =========================================
    public static boolean AuctionEnabled = true;
    public static int DefaultMaxListings = 5;
    public static double ListingFeePercent = 0.0;
    public static double SalesTaxPercent = 0.05;
    public static boolean RequireAuctioneerNPC = true;
    public static int ClaimExpirationDays = 30;
    public static double MinBidIncrementPercent = 0.05;
    public static int SnipeProtectionMinutes = 5;
    public static int SnipeProtectionThreshold = 5;

    // Auction Duration Settings (in hours)
    public static int AuctionDurationShort = 2;
    public static int AuctionDurationMedium = 12;
    public static int AuctionDurationLong = 24;
    public static int AuctionDurationVeryLong = 48;

    // Auction Duration Fees (flat currency amount)
    public static long AuctionFeeShort = 0;
    public static long AuctionFeeMedium = 0;
    public static long AuctionFeeLong = 0;
    public static long AuctionFeeVeryLong = 0;

    // =========================================
    // Trader Settings
    // =========================================
    public static boolean EnableStockByDefault = false;
    public static String DefaultResetType = "NONE";

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
            config.setCategoryComment(CURRENCY, "Currency system settings for the virtual economy");

            UseVaultIfAvailable = config.get(CURRENCY, "Use Vault If Available", true,
                "Use VaultAPI if available on Bukkit hybrid servers").getBoolean(true);

            CurrencyName = config.get(CURRENCY, "Currency Name", "Gold",
                "Display name for the currency").getString();

            FallbackCurrencyItem = config.get(CURRENCY, "Fallback Currency Item", "customnpcs:npcCoinGold",
                "Item registry name to use as currency when Vault is unavailable. Leave empty to disable.").getString();

            FallbackItemRatio = config.get(CURRENCY, "Fallback Item Ratio", 1,
                "Conversion ratio: 1 fallback item = X virtual currency").getInt(1);

            StartingBalance = config.get(CURRENCY, "Starting Balance", 1000,
                "Starting balance for new players (only used when Vault is not available)").getInt(1000);

            MaxBalance = config.get(CURRENCY, "Maximum Balance", 999999999999L,
                "Maximum balance a player can have (only used when Vault is not available)").getLong(999999999999L);

            // =========================================
            // Auction Settings
            // =========================================
            config.setCategoryComment(AUCTION, "Auction house settings");

            AuctionEnabled = config.get(AUCTION, "Enable Auction House", true,
                "Enable/disable the auction house system").getBoolean(true);

            DefaultMaxListings = config.get(AUCTION, "Default Max Listings", 5,
                "Default maximum auction listings per player (without permission override)").getInt(5);

            ListingFeePercent = config.get(AUCTION, "Listing Fee Percent", 0.0,
                "Listing fee as percentage of starting price (0.0 = no fee)").getDouble(0.0);

            SalesTaxPercent = config.get(AUCTION, "Sales Tax Percent", 0.05,
                "Sales tax as percentage taken from final sale price (0.05 = 5%)").getDouble(0.05);

            RequireAuctioneerNPC = config.get(AUCTION, "Require Auctioneer NPC", true,
                "Require an Auctioneer NPC to access auction house").getBoolean(true);

            ClaimExpirationDays = config.get(AUCTION, "Claim Expiration Days", 30,
                "Claim expiration in days (claims auto-expire after this period, 0 = never)").getInt(30);

            MinBidIncrementPercent = config.get(AUCTION, "Min Bid Increment Percent", 0.05,
                "Minimum bid increment as percentage (0.05 = 5% minimum increase)").getDouble(0.05);

            SnipeProtectionMinutes = config.get(AUCTION, "Snipe Protection Minutes", 5,
                "Extend auction TO this many minutes when bid placed near end. Set to 0 to disable.").getInt(5);

            SnipeProtectionThreshold = config.get(AUCTION, "Snipe Protection Threshold", 5,
                "How many minutes before end triggers snipe protection").getInt(5);

            // Duration settings
            AuctionDurationShort = config.get(AUCTION, "Duration Short Hours", 2,
                "Short auction duration in hours").getInt(2);

            AuctionDurationMedium = config.get(AUCTION, "Duration Medium Hours", 12,
                "Medium auction duration in hours").getInt(12);

            AuctionDurationLong = config.get(AUCTION, "Duration Long Hours", 24,
                "Long auction duration in hours").getInt(24);

            AuctionDurationVeryLong = config.get(AUCTION, "Duration Very Long Hours", 48,
                "Very long auction duration in hours").getInt(48);

            // Duration fees
            AuctionFeeShort = config.get(AUCTION, "Fee Short Duration", 0,
                "Flat fee for short duration auctions").getInt(0);

            AuctionFeeMedium = config.get(AUCTION, "Fee Medium Duration", 0,
                "Flat fee for medium duration auctions").getInt(0);

            AuctionFeeLong = config.get(AUCTION, "Fee Long Duration", 0,
                "Flat fee for long duration auctions").getInt(0);

            AuctionFeeVeryLong = config.get(AUCTION, "Fee Very Long Duration", 0,
                "Flat fee for very long duration auctions").getInt(0);

            // =========================================
            // Trader Settings
            // =========================================
            config.setCategoryComment(TRADER, "Trader stock system settings");

            EnableStockByDefault = config.get(TRADER, "Enable Stock By Default", false,
                "Enable stock system by default for new traders").getBoolean(false);

            DefaultResetType = config.get(TRADER, "Default Reset Type", "NONE",
                "Default stock reset type (NONE, MCDAILY, MCWEEKLY, RLDAILY, RLWEEKLY)").getString();

            // Set category order
            config.setCategoryPropertyOrder(MARKET, new ArrayList<>(Arrays.asList(
                "Enable Market System"
            )));

            config.setCategoryPropertyOrder(CURRENCY, new ArrayList<>(Arrays.asList(
                "Use Vault If Available",
                "Currency Name",
                "Fallback Currency Item",
                "Fallback Item Ratio",
                "Starting Balance",
                "Maximum Balance"
            )));

            config.setCategoryPropertyOrder(AUCTION, new ArrayList<>(Arrays.asList(
                "Enable Auction House",
                "Default Max Listings",
                "Listing Fee Percent",
                "Sales Tax Percent",
                "Require Auctioneer NPC",
                "Claim Expiration Days",
                "Min Bid Increment Percent",
                "Snipe Protection Minutes",
                "Snipe Protection Threshold",
                "Duration Short Hours",
                "Duration Medium Hours",
                "Duration Long Hours",
                "Duration Very Long Hours",
                "Fee Short Duration",
                "Fee Medium Duration",
                "Fee Long Duration",
                "Fee Very Long Duration"
            )));

            config.setCategoryPropertyOrder(TRADER, new ArrayList<>(Arrays.asList(
                "Enable Stock By Default",
                "Default Reset Type"
            )));

            // Validation
            if (DefaultMaxListings < 1) {
                DefaultMaxListings = 1;
            }
            if (ClaimExpirationDays < 0) {
                ClaimExpirationDays = 0;
            }
            if (SnipeProtectionMinutes < 0) {
                SnipeProtectionMinutes = 0;
            }
            if (SnipeProtectionThreshold < 0) {
                SnipeProtectionThreshold = 0;
            }
            if (StartingBalance < 0) {
                StartingBalance = 0;
            }
            if (MaxBalance < 1) {
                MaxBalance = 1;
            }

        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its market configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
