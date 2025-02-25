package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigClient
{
    public static Configuration config;

    public final static String GENERAL = "General";
    public final static String VISUAL = "Visual";
    public final static String MODEL = "Model";
    public final static String TEXTURE = "Texture";
    public final static String HUD = "Hud";


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

    public static Property ChatBubblesFontTypeProperty;
    public static boolean ChatBubblesFontType = false;

    public static Property HideEffectsBarProperty;
    public static boolean HideEffectsBar = false;

    /**
     *  Questing Properties
     **/
    public static Property DialogSpeedProperty;
    public static int DialogSpeed = 10;

    public static Property DialogSoundProperty;
    public static boolean DialogSound = true;

    public static Property ChatAlertsProperty;
    public static boolean ChatAlerts = true;

    public static Property BannerAlertsProperty;
    public static boolean BannerAlerts = true;

    // HUD PROPERTIES
    public static Property QuestOverlayXProperty;
    public static int QuestOverlayX = 100;

    public static Property QuestOverlayYProperty;
    public static int QuestOverlayY = 100;

    public static Property QuestOverlayScaleProperty;
    public static int QuestOverlayScale = 100;

    public static Property QuestOverlayTextAlignProperty;
    public static int QuestOverlayTextAlign = 1; // 0: Left, 1: Center, 2: Right

    public static Property CompassEnabledProperty;
    public static boolean CompassEnabled;

    public static Property CompassOverlayXProperty;
    public static int CompassOverlayX;

    public static Property CompassOverlayYProperty;
    public static int CompassOverlayY;

    public static Property CompassOverlayScaleProperty;
    public static int CompassOverlayScale;

    public static Property CompassOverlayWidthProperty;
    public static int CompassOverlayWidth;

    /**
     *  Texture Properties
     **/
    public static boolean WoodTextures = false;

    /**
     *  Model Properties
     **/
    public static boolean LegacyCampfire = false;
    public static boolean LegacyBanner = false;
    public static boolean LegacyChair = false;
    public static boolean LegacyStool = false;
    public static boolean LegacyCouch = false;
    public static boolean LegacyTable = false;
    public static boolean LegacyBarrel = false;
    public static boolean LegacyCarpentryBench = false;
    public static boolean LegacyAnvil = false;
    public static boolean LegacyLantern = false;
    public static boolean LegacyCandle = false;
    public static boolean LegacyTallLamp = false;
    public static boolean LegacyPedestal = false;

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();

            // Hud Quest Overlay settings
            QuestOverlayXProperty = config.get(HUD, "Quest Hud X", 100, "X position of the quest overlay.");
            QuestOverlayX = QuestOverlayXProperty.getInt(100);

            QuestOverlayYProperty = config.get(HUD, "Quest Hud Y", 100, "Y position of the quest overlay.");
            QuestOverlayY = QuestOverlayYProperty.getInt(100);

            QuestOverlayScaleProperty = config.get(HUD, "Quest Hud Scale", 100, "Scale percentage of the quest overlay.");
            QuestOverlayScale = QuestOverlayScaleProperty.getInt(100);

            QuestOverlayTextAlignProperty = config.get(HUD, "Quest Hud Text Alignment", 1, "Text alignment in quest overlay (0: Left, 1: Center, 2: Right).");
            QuestOverlayTextAlign = QuestOverlayTextAlignProperty.getInt(1);

            CompassEnabledProperty = config.get(
                HUD,
                "Compass Hud Enabled",
                true,
                "Enable Compass Hud Component"
            );
            CompassEnabled = CompassEnabledProperty.getBoolean();

            // Compass HUD Configs
            CompassOverlayXProperty = config.get(
                HUD,
                "Compass Hud X",
                50,
                "Horizontal position of compass overlay (0-100 percentage)",
                0,
                100
            );
            CompassOverlayX = CompassOverlayXProperty.getInt();

            CompassOverlayYProperty = config.get(
                HUD,
                "Compass Hud Y",
                5,
                "Vertical position of compass overlay (0-100 percentage)",
                0,
                100
            );
            CompassOverlayY = CompassOverlayYProperty.getInt();

            CompassOverlayScaleProperty = config.get(
                HUD,
                "Compass Hud Scale",
                100,
                "Scale percentage of compass overlay",
                50,
                300
            );
            CompassOverlayScale = CompassOverlayScaleProperty.getInt();

            CompassOverlayWidthProperty = config.get(
                HUD,
                "Compass Hud Width",
                200,
                "Base width of compass bar in pixels",
                100,
                1000
            );
            CompassOverlayWidth = CompassOverlayWidthProperty.getInt();


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

            ChatBubblesFontTypeProperty = config.get(VISUAL, "Chat Bubbles Font Type", false, "Enable the use of Custom Font for Chat Bubbles");
            ChatBubblesFontType = ChatBubblesFontTypeProperty.getBoolean(false);

            HideEffectsBarProperty = config.get(VISUAL, "Hide Effects Bar", false, "Hides CNPC+ Inventory Effects Bar");
            HideEffectsBar = HideEffectsBarProperty.getBoolean(false);

            DialogSpeedProperty = config.get(VISUAL, "Dialog Speed", true, "Only set for gradual dialogs");
            DialogSpeed = DialogSpeedProperty.getInt(10);

            DialogSoundProperty = config.get(VISUAL, "Dialog Sound", true, "Only for dialogs with sounds");
            DialogSound = DialogSoundProperty.getBoolean(true);

            ChatAlertsProperty = config.get(VISUAL, "All Chat Alerts", true, "Universal enable/disable for Banner Alerts");
            ChatAlerts = ChatAlertsProperty.getBoolean(true);

            BannerAlertsProperty = config.get(VISUAL, "All Banner Alerts", true, "Universal enable/disable for Banner Alerts");
            BannerAlerts = BannerAlertsProperty.getBoolean(true);

            WoodTextures = config.get(TEXTURE, "Wood Textures", false, "Models like Chairs and Stools will use default MC Wood Textures").getBoolean(false);

            LegacyCampfire = config.get(MODEL, "Legacy Campfire Model", false).getBoolean(false);
            LegacyBanner = config.get(MODEL, "Legacy Banner Model", false).getBoolean(false);
            LegacyBarrel = config.get(MODEL, "Legacy Barrel Model", false).getBoolean(false);
            LegacyChair = config.get(MODEL, "Legacy Chair Model", false).getBoolean(false);
            LegacyStool = config.get(MODEL, "Legacy Stool Model", false).getBoolean(false);
            LegacyCouch = config.get(MODEL, "Legacy Couch Model", false).getBoolean(false);
            LegacyTable = config.get(MODEL, "Legacy Table Model", false).getBoolean(false);
            LegacyAnvil = config.get(MODEL, "Legacy Anvil Model", false).getBoolean(false);
            LegacyCarpentryBench = config.get(MODEL, "Legacy Carpentry Bench Model", false).getBoolean(false);
            LegacyLantern = config.get(MODEL, "Legacy Lantern Model", false).getBoolean(false);
            LegacyCandle = config.get(MODEL, "Legacy Candle Model", false).getBoolean(false);
            LegacyTallLamp = config.get(MODEL, "Legacy Tall Lamp Model", false).getBoolean(false);
            LegacyPedestal = config.get(MODEL, "Legacy Pedestal Model", false).getBoolean(false);

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
            }
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
