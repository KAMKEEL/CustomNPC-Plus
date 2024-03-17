package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;
import tv.twitch.chat.Chat;

import java.io.File;

public class ConfigClient
{
    public static Configuration config;

    public final static String GENERAL = "General";
    public final static String VISUAL = "Visual";
    public final static String QUESTING = "Questing";


    /**
     *  General Properties
     **/
    public static Property CacheLifeProperty;
    public static int CacheLife = 10;

    /**
     *  Visual Properties
     **/
    public static Property EnableChatBubblesProperty;
    public static boolean EnableChatBubbles = true;

    public static Property InventoryGuiEnabledProperty;
    public static boolean InventoryGuiEnabled = true;

    public static Property enableFactionTabProperty;
    public static boolean enableFactionTab = true;

    public static Property FontTypeProperty;
    public static String FontType = "Default";

    public static Property FontSizeProperty;
    public static int FontSize = 18;

    /**
     *  Questing Properties
     **/
    public static Property TrackingInfoAlignmentProperty;
    public static int TrackingInfoAlignment = 3;

    public static Property TrackingInfoXProperty;
    public static int TrackingInfoX = 0;

    public static Property TrackingInfoYProperty;
    public static int TrackingInfoY = 0;

    public static Property TrackingScaleProperty;
    public static int TrackingScale = 100;

    public static Property DialogSpeedProperty;
    public static int DialogSpeed = 10;

    public static Property DialogSoundProperty;
    public static boolean DialogSound = true;

    public static Property ChatAlertsProperty;
    public static boolean ChatAlerts = true;

    public static Property BannerAlertsProperty;
    public static boolean BannerAlerts = true;

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();

            // General
            CacheLifeProperty = config.get(GENERAL, "Cache Life", 10, "How long should downloaded imagery data be saved client side? (In minutes)");
            CacheLife = CacheLifeProperty.getInt(10);

            // Visual
            EnableChatBubblesProperty = config.get(VISUAL, "Enable Chat Bubbles", true, "Enable/Disable Chat Bubbles");
            EnableChatBubbles = EnableChatBubblesProperty.getBoolean(true);

            InventoryGuiEnabledProperty = config.get(VISUAL, "Enable Inventory Tabs", true, "Enable/Disable Inventory Tabs");
            InventoryGuiEnabled = InventoryGuiEnabledProperty.getBoolean(true);

            enableFactionTabProperty = config.get(VISUAL, "Enable Faction Tab", true, "Enable the Faction Tab");
            enableFactionTab = enableFactionTabProperty.getBoolean(true);

            FontTypeProperty = config.get(VISUAL, "Font Type", "Default", "When set to Minecraft it will use minecrafts font, when Default it will use OpenSans. Can only use fonts installed on your PC");
            FontType = FontTypeProperty.getString();

            FontSizeProperty = config.get(VISUAL, "Font Size", 18, "Font size for custom fonts (doesn't work with minecrafts font)");
            FontSize = FontSizeProperty.getInt(18);

            // Questing
            TrackingInfoAlignmentProperty = config.get(QUESTING, "Tracking Info Alignment", 3, "Client sided! Determines where tracking quest info shows up on the screen based on a number from 0 to 8. Default: 3");
            TrackingInfoAlignment = TrackingInfoAlignmentProperty.getInt(3);

            TrackingInfoXProperty = config.get(QUESTING, "Tracking Info X", 0, "Client sided! Offsets the tracking info GUI by this amount in the X direction.");
            TrackingInfoX = TrackingInfoXProperty.getInt(0);

            TrackingInfoYProperty = config.get(QUESTING, "Tracking Info Y", 0, "Client sided! Offsets the tracking info GUI by this amount in the Y direction.");
            TrackingInfoY = TrackingInfoYProperty.getInt(0);

            TrackingScaleProperty = config.get(QUESTING, "Tracking Scale", 100, "Client sided! Adjusts the scaling of the Quest Tracking");
            TrackingScale = TrackingScaleProperty.getInt(100);

            DialogSpeedProperty = config.get(VISUAL, "Dialog Speed", true, "Only set for gradual dialogs");
            DialogSpeed = DialogSpeedProperty.getInt(10);

            DialogSoundProperty = config.get(VISUAL, "Dialog Sound", true, "Only for dialogs with sounds");
            DialogSound = DialogSoundProperty.getBoolean(true);

            ChatAlertsProperty = config.get(VISUAL, "All Chat Alerts", true, "Universal enable/disable for Banner Alerts");
            ChatAlerts = ChatAlertsProperty.getBoolean(true);

            BannerAlertsProperty = config.get(VISUAL, "All Banner Alerts", true, "Universal enable/disable for Banner Alerts");
            BannerAlerts = BannerAlertsProperty.getBoolean(true);

            // Convert to Legacy
            if(CustomNpcs.legacyExist){
                EnableChatBubbles = LegacyConfig.EnableChatBubbles;
                EnableChatBubblesProperty.set(EnableChatBubbles);

                FontType = LegacyConfig.FontType;
                FontTypeProperty.set(FontType);

                InventoryGuiEnabled = LegacyConfig.InventoryGuiEnabled;
                InventoryGuiEnabledProperty.set(InventoryGuiEnabled);

                FontSize = LegacyConfig.FontSize;
                FontSizeProperty.set(FontSize);

                TrackingInfoAlignment = LegacyConfig.TrackingInfoAlignment;
                TrackingInfoAlignmentProperty.set(TrackingInfoAlignment);

                TrackingInfoX = LegacyConfig.TrackingInfoX;
                TrackingInfoXProperty.set(TrackingInfoX);

                TrackingInfoY = LegacyConfig.TrackingInfoY;
                TrackingInfoYProperty.set(TrackingInfoY);
            }

            if (TrackingInfoAlignment < 0)
                TrackingInfoAlignment = 0;
            if (TrackingInfoAlignment > 8)
                TrackingInfoAlignment = 8;
        }
        catch (Exception e)
        {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its client configuration");
        }
        finally
        {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
