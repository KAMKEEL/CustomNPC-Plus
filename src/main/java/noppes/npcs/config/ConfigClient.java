package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigClient {
    public static Configuration config;

    public final static String GENERAL = "General";
    public final static String VISUAL = "Visual";
    public final static String MODEL = "Model";
    public final static String TEXTURE = "Texture";
    public final static String HUD = "Hud";
    public final static String RENDERING = "Rendering";


    /**
     * General Properties
     **/
    public static Property AllowClientScriptsProperty;
    public static boolean AllowClientScripts = true;

    public static Property CacheLifeProperty;
    public static int CacheLife = 10;

    /**
     * Visual Properties
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

    // Rendering Properties
    public static Property LowResExplosionProperty;
    public static boolean LowResExplosion = false;

    // Proximity Alpha (owner-only fade for energy projectiles near camera)
    public static Property ProximityAlphaMinProperty;
    public static float ProximityAlphaMin = 0.15f;

    public static Property ProximityAlphaDistanceProperty;
    public static float ProximityAlphaDistance = 7.0f;

    public static Property ProximityAlphaAgeTicksProperty;
    public static int ProximityAlphaAgeTicks = 60;

    /**
     * Questing Properties
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
    public static float QuestOverlayX = 0;

    public static Property QuestOverlayYProperty;
    public static float QuestOverlayY = 31;

    public static Property QuestOverlayScaleProperty;
    public static int QuestOverlayScale = 223;

    public static Property QuestOverlayTextAlignProperty;
    public static int QuestOverlayTextAlign = 0; // 0: Left, 1: Center, 2: Right

    public static Property CompassEnabledProperty;
    public static boolean CompassEnabled = true;

    public static Property CompassOverlayXProperty;
    public static float CompassOverlayX = 37;

    public static Property CompassOverlayYProperty;
    public static float CompassOverlayY = 0;

    public static Property CompassOverlayScaleProperty;
    public static int CompassOverlayScale = 276;

    public static Property CompassOverlayWidthProperty;
    public static int CompassOverlayWidth = 200;

    // Ability Hotbar HUD
    public static Property AbilityHotbarEnabledProperty;
    public static boolean AbilityHotbarEnabled = true;

    public static Property AbilityHotbarXProperty;
    public static float AbilityHotbarX = 3;

    public static Property AbilityHotbarYProperty;
    public static float AbilityHotbarY = 50;

    public static Property AbilityHotbarScaleProperty;
    public static int AbilityHotbarScale = 100;

    public static Property AbilityHotbarHorizontalProperty;
    public static boolean AbilityHotbarHorizontal = false;

    public static Property AbilityHotbarAltTextureProperty;
    public static boolean AbilityHotbarAltTexture = false;

    // 1=Above(H)/Left(V), 2=Below(H)/Right(V)
    public static Property AbilityHotbarTextPositionProperty;
    public static int AbilityHotbarTextPosition = 2;

    // Max visible slots: 3, 5, or 7
    public static Property AbilityHotbarVisibleSlotsProperty;
    public static int AbilityHotbarVisibleSlots = 5;

    // Show Always: true = always visible, false = only visible while HUD key held
    public static Property AbilityHotbarShowAlwaysProperty;
    public static boolean AbilityHotbarShowAlways = true;

    // Text Visibility: 0=Shown, 1=Hidden, 2=Held (only while HUD key held)
    public static Property AbilityHotbarTextVisibilityProperty;
    public static int AbilityHotbarTextVisibility = 0;

    /**
     * Texture Properties
     **/
    public static boolean WoodTextures = false;

    /**
     * Model Properties
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
    public static boolean LegacyMailbox = false;
    public static boolean ImprovedImageDownloadConnection = true;

    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // Hud Quest Overlay settings
            QuestOverlayXProperty = config.get(HUD, "Quest Hud X", 0.0, "X position of the quest overlay.");
            QuestOverlayX = (float) QuestOverlayXProperty.getDouble(0);

            QuestOverlayYProperty = config.get(HUD, "Quest Hud Y", 31.0, "Y position of the quest overlay.");
            QuestOverlayY = (float) QuestOverlayYProperty.getDouble(31);

            QuestOverlayScaleProperty = config.get(HUD, "Quest Hud Scale", 223, "Scale percentage of the quest overlay.");
            QuestOverlayScale = QuestOverlayScaleProperty.getInt(223);

            QuestOverlayTextAlignProperty = config.get(HUD, "Quest Hud Text Alignment", 0, "Text alignment in quest overlay (0: Left, 1: Center, 2: Right).");
            QuestOverlayTextAlign = QuestOverlayTextAlignProperty.getInt(0);

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
                37.0,
                "Horizontal position of compass overlay (0-100 percentage)"
            );
            CompassOverlayX = (float) CompassOverlayXProperty.getDouble();

            CompassOverlayYProperty = config.get(
                HUD,
                "Compass Hud Y",
                0.0,
                "Vertical position of compass overlay (0-100 percentage)"
            );
            CompassOverlayY = (float) CompassOverlayYProperty.getDouble();

            CompassOverlayScaleProperty = config.get(
                HUD,
                "Compass Hud Scale",
                276,
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

            // Ability Hotbar HUD Configs
            AbilityHotbarEnabledProperty = config.get(HUD, "Ability Hotbar Enabled", true, "Enable Ability Hotbar HUD Component");
            AbilityHotbarEnabled = AbilityHotbarEnabledProperty.getBoolean();

            AbilityHotbarXProperty = config.get(HUD, "Ability Hotbar X", 3.0, "Horizontal position (0-100 percentage)");
            AbilityHotbarX = (float) AbilityHotbarXProperty.getDouble();

            AbilityHotbarYProperty = config.get(HUD, "Ability Hotbar Y", 50.0, "Vertical position (0-100 percentage)");
            AbilityHotbarY = (float) AbilityHotbarYProperty.getDouble();

            AbilityHotbarScaleProperty = config.get(HUD, "Ability Hotbar Scale", 100, "Scale percentage", 50, 300);
            AbilityHotbarScale = AbilityHotbarScaleProperty.getInt();

            AbilityHotbarHorizontalProperty = config.get(HUD, "Ability Hotbar Horizontal", false, "Display horizontally instead of vertically");
            AbilityHotbarHorizontal = AbilityHotbarHorizontalProperty.getBoolean();

            AbilityHotbarAltTextureProperty = config.get(HUD, "Ability Hotbar Alt Texture", false, "Use rounded square instead of circle slots");
            AbilityHotbarAltTexture = AbilityHotbarAltTextureProperty.getBoolean();

            AbilityHotbarTextPositionProperty = config.get(HUD, "Ability Hotbar Text Position", 2, "Text label position (1=Above/Left, 2=Below/Right)");
            AbilityHotbarTextPosition = AbilityHotbarTextPositionProperty.getInt(2);

            AbilityHotbarVisibleSlotsProperty = config.get(HUD, "Ability Hotbar Visible Slots", 5, "Max visible slots in hotbar (3, 5, or 7)");
            AbilityHotbarVisibleSlots = AbilityHotbarVisibleSlotsProperty.getInt(5);

            AbilityHotbarShowAlwaysProperty = config.get(HUD, "Ability Hotbar Show Always", true, "Always show hotbar (false = only while HUD key is held)");
            AbilityHotbarShowAlways = AbilityHotbarShowAlwaysProperty.getBoolean(true);

            AbilityHotbarTextVisibilityProperty = config.get(HUD, "Ability Hotbar Text Visibility", 0, "Text visibility (0=Shown, 1=Hidden, 2=Held)");
            AbilityHotbarTextVisibility = AbilityHotbarTextVisibilityProperty.getInt(0);

            // General
            AllowClientScriptsProperty = config.get(GENERAL, "Allow Client Scripts", true, "Allow the server to run scripts on the client. If disabled, no server scripts will execute client-side.");
            AllowClientScripts = AllowClientScriptsProperty.getBoolean(true);

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

            LowResExplosionProperty = config.get(
                RENDERING,
                "Low Res Explosion",
                false,
                "Disables most Energy Explosion voxel rendering and keeps particle effects only."
            );
            LowResExplosion = LowResExplosionProperty.getBoolean(false);

            ProximityAlphaMinProperty = config.get(
                RENDERING,
                "Proximity Alpha Min",
                0.15,
                "Minimum alpha for owner's energy projectiles when very close to camera. Set to 1.0 to disable proximity fade."
            );
            ProximityAlphaMin = (float) Math.max(0.0, Math.min(1.0, ProximityAlphaMinProperty.getDouble(0.15)));

            ProximityAlphaDistanceProperty = config.get(
                RENDERING,
                "Proximity Alpha Distance",
                7.0,
                "Distance in blocks at which owner's energy projectiles reach full alpha."
            );
            ProximityAlphaDistance = (float) Math.max(1.0, ProximityAlphaDistanceProperty.getDouble(7.0));

            ProximityAlphaAgeTicksProperty = config.get(
                RENDERING,
                "Proximity Alpha Age Ticks",
                60,
                "After this many ticks alive, proximity alpha fade is disabled (projectile always renders at full alpha). Does not apply while charging or to attached beams/lasers."
            );
            ProximityAlphaAgeTicks = Math.max(0, ProximityAlphaAgeTicksProperty.getInt(60));

            DialogSpeedProperty = config.get(VISUAL, "Dialog Speed", true, "Only set for gradual dialogs");
            DialogSpeed = DialogSpeedProperty.getInt(10);

            DialogSoundProperty = config.get(VISUAL, "Dialog Sound", true, "Only for dialogs with sounds");
            DialogSound = DialogSoundProperty.getBoolean(true);

            ChatAlertsProperty = config.get(VISUAL, "All Chat Alerts", true, "Universal enable/disable for Banner Alerts");
            ChatAlerts = ChatAlertsProperty.getBoolean(true);

            BannerAlertsProperty = config.get(VISUAL, "All Banner Alerts", true, "Universal enable/disable for Banner Alerts");
            BannerAlerts = BannerAlertsProperty.getBoolean(true);

            WoodTextures = config.get(TEXTURE, "Wood Textures", false, "Models like Chairs and Stools will use default MC Wood Textures").getBoolean(false);
            ImprovedImageDownloadConnection = config.get(TEXTURE, "DEBUG: Better handling of image downloads: ", true).getBoolean(true);

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
            LegacyMailbox = config.get(MODEL, "Legacy Mailbox Model", false).getBoolean(false);

            // Convert to Legacy
            if (CustomNpcs.legacyExist) {
                EnableChatBubbles = LegacyConfig.EnableChatBubbles;
                EnableChatBubblesProperty.set(EnableChatBubbles);

                FontType = LegacyConfig.FontType;
                FontTypeProperty.set(FontType);

                InventoryGuiEnabled = LegacyConfig.InventoryGuiEnabled;
                InventoryGuiEnabledProperty.set(InventoryGuiEnabled);

                FontSize = LegacyConfig.FontSize;
                FontSizeProperty.set(FontSize);
            }

        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its client configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
