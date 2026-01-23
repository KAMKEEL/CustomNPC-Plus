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

            UseVault = config.get(CURRENCY, "Use Vault", false,
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

        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its market configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
