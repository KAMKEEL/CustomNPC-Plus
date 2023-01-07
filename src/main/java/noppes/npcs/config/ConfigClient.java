package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigClient
{
    public static Configuration config;

    public static boolean EnableChatBubbles = true;
    public static boolean InventoryGuiEnabled = true;
    public static String FontType = "Default";
    public static int FontSize = 18;
    public static boolean EntityRendererMixin = true;

    public static int TrackingInfoAlignment = 3;
    public static int TrackingInfoX = 0;
    public static int TrackingInfoY = 0;

    public final static String VISUAL = "Visual";

    public final static String QUESTING = "Questing";

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();
            EnableChatBubbles = config.get(VISUAL, "Enable Chat Bubbles", true, "Enable/Disable Chat Bubbles").getBoolean(true);
            InventoryGuiEnabled = config.get(VISUAL, "Enable Inventory Tabs", true, "Enable/Disable Inventory Tabs").getBoolean(true);
            FontType = config.get(VISUAL, "Font Type", "Default", "When set to Minecraft it will use minecrafts font, when Default it will use OpenSans. Can only use fonts installed on your PC").getString();
            FontSize = config.get(VISUAL, "Font Size", 18, "Font size for custom fonts (doesn't work with minecrafts font)").getInt(18);
            EntityRendererMixin = config.get(VISUAL, "Entity Render Mixin", true, "Enables Overlay Mixins for Conflicts relating to Optifine or other Skin Renderers. If crashes occur, please disable.").getBoolean(true);

            TrackingInfoAlignment = config.get(QUESTING, "Tracking Info Alignment", 3, "Client sided! Determines where tracking quest info shows up on the screen based on a number from 0 to 8. Default: 3").getInt(3);
            if (TrackingInfoAlignment < 0)
                TrackingInfoAlignment = 0;
            if (TrackingInfoAlignment > 8)
                TrackingInfoAlignment = 8;

            TrackingInfoX = config.get(QUESTING, "Tracking Info X", 0, "Client sided! Offsets the tracking info GUI by this amount in the X direction.").getInt(0);
            TrackingInfoY = config.get(QUESTING, "Tracking Info Y", 0, "Client sided! Offsets the tracking info GUI by this amount in the Y direction.").getInt(0);


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