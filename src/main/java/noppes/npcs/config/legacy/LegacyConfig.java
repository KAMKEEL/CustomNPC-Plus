package noppes.npcs.config.legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.io.File;

public class LegacyConfig {
    @ConfigProp(info = "Disable Chat Bubbles")
    public static boolean EnableChatBubbles = true;

    @ConfigProp(info = "Enables/Disables ALL scripting. You can still see and write code in the scripter, but these scripts won't run. True by default")
    public static boolean ScriptingEnabled = true;

    @ConfigProp(info = "Enables/Disables the use of the the ECMA6 Javascript standard instead of ECMA5.1." +
        "\nEnabling this adds many more features to JS in scripts. Only use if on Java 8 or higher." +
        "\nNot all ECMA 6 language is supported through this functionality.")
    public static boolean ScriptingECMA6 = false;

    @ConfigProp(info = "Enables global player event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.")
    public static boolean GlobalPlayerScripts = true;

    @ConfigProp(info = "Enables global forge event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.")
    public static boolean GlobalForgeScripts = true;

    @ConfigProp(info = "Enables global NPC scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. False by default, use with caution!")
    public static boolean GlobalNPCScripts = false;

    @ConfigProp(info = "If scripts are too long (>65535 characters), they normally won't be saved in NBT data.\n" +
        "This config adds additional compound tags to scripts that need it, so you can store much larger scripts!\n" +
        "Every additional compound tag adds 65535 more characters to your script length limit. Use incrementally, with caution.")
    public static int ExpandedScriptLimit = 2;

    @ConfigProp(info = "Enables if Player Information (WAND-USE) should be printed to CustomNPCs Logs. IF on Server \n" +
        "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3")
    public static boolean PlayerLogging = false;

    @ConfigProp(info = "Enables if Scripting Information should be printed to CustomNPCs Logs. IF on Server \n" +
        "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3")
    public static boolean ScriptLogging = false;

    @ConfigProp(info = "Amount of Messages marked as SPAM [5, 3000]. Lower Number means MORE accurate messages \n" +
        "This frequency will determine if the log will print a line with [SPAM] to warn the console.")
    public static int ScriptFrequency = 20;

    @ConfigProp(info = "IN Milliseconds 1s = 1000s. If a recent LOG of the same event is SENT within this threshold it will be ignored.")
    public static int ScriptIgnoreTime = 2000;

    @ConfigProp(info = "Comma separated list of NPC Script Types that will omit these from the logs,\n" +
        "INIT,TICK,INTERACT,DIALOG,DAMAGED,KILLED,ATTACK,TARGET,COLLIDE,KILLS,DIALOG_CLOSE,TIMER")
    public static String ScriptLogIgnoreType = "TICK";

    @ConfigProp(info = "Navigation search range for NPCs. Not recommended to increase if you have a slow pc or on a server. Minimum of 16, maximum of 96.")
    public static int NpcNavRange = 32;

    @ConfigProp(info = "Size limit for NPCs. Default 100, larger sizes may cause lag on clients and servers that can't take it!")
    public static int NpcSizeLimit = 100;

    @ConfigProp(info = "Set to true if you want the dialog command option to be able to use op commands like tp etc")
    public static boolean NpcUseOpCommands = false;

    @ConfigProp(info = "Client sided! Determines where tracking quest info shows up on the screen based on a number from 0 to 8. Default: 3")
    public static int TrackingInfoAlignment = 3;

    @ConfigProp(info = "Client sided! Offsets the tracking info GUI by this amount in the X direction.")
    public static int TrackingInfoX = 0;

    @ConfigProp(info = "Client sided! Offsets the tracking info GUI by this amount in the Y direction.")
    public static int TrackingInfoY = 0;

    @ConfigProp
    public static boolean InventoryGuiEnabled = true;

    @ConfigProp
    public static boolean DisableExtraItems = false;

    @ConfigProp
    public static boolean DisableExtraBlock = false;

    @ConfigProp(info = "Only ops can create and edit npcs")
    public static boolean OpsOnly = false;

    @ConfigProp(info = "Only ops can see and edit scripts")
    public static boolean ScriptOpsOnly = false;

    @ConfigProp(info = "Comma separated list of player UUIDs that can see and edit scripts. If ScriptsOpsOnly is true,\n" +
        "ops and players with these IDs can see and edit scripts. Example:\n" +
        "b876ec32-e396-476b-a115-8438d83c67d4,069a79f4-44e9-4726-a5be-fca90e38aaf5,be951074-c7ea-4f02-a725-bf017bc88650\n" +
        "Get a player's UUID from a site like NameMC or the API IPlayer.getUniqueID() function!\n" +
        "If left empty and ScriptsOpsOnly is false, anyone can see and edit scripts with a scripter.")
    public static String ScriptDevIDs = "";

    @ConfigProp(info = "Default interact line. Leave empty to not have one")
    public static String DefaultInteractLine = "Hello @p";

    @ConfigProp
    public static boolean DisableEnchants = false;
    @ConfigProp(info = "Start Id for enchants. IDs can only range from 0-256")
    public static int EnchantStartId = 100;

    @ConfigProp(info = "Number of chunk loading npcs that can be active at the same time")
    public static int ChunkLoaders = 20;

    @ConfigProp(info = "The maximum number of images any dialog can hold.")
    public static int DialogImageLimit = 10;

    @ConfigProp(info = "The maximum number of overlays any npc/player can hold.")
    public static int SkinOverlayLimit = 10;

    @ConfigProp(info = "Set to false if you want to disable guns")
    public static boolean GunsEnabled = true;

    @ConfigProp(info = "Enables leaves decay")
    public static boolean LeavesDecayEnabled = true;

    @ConfigProp(info = "Enables Vine Growth")
    public static boolean VineGrowthEnabled = true;

    @ConfigProp(info = "Enables Ice Melting")
    public static boolean IceMeltsEnabled = true;

    @ConfigProp(info = "Normal players can use soulstone on animals")
    public static boolean SoulStoneAnimals = true;

    @ConfigProp(info = "Normal players can use soulstone on villagers")
    public static boolean SoulStoneVillagers = false;

    @ConfigProp(info = "Normal players can use soulstone on all npcs")
    public static boolean SoulStoneNPCs = false;

    @ConfigProp(info = "Normal players can use soulstone on friendly npcs")
    public static boolean SoulStoneFriendlyNPCs = false;

    @ConfigProp(info = "When set to Minecraft it will use minecrafts font, when Default it will use OpenSans. Can only use fonts installed on your PC")
    public static String FontType = "Default";

    @ConfigProp(info = "Font size for custom fonts (doesn't work with minecrafts font)")
    public static int FontSize = 18;

    @ConfigProp(info = "Enables Overlay Mixins for Conflicts relating to Optifine or other Skin Renderers. If crashes occur, please disable.")
    public static boolean EntityRendererMixin = true;

    @ConfigProp(info = "Enables CustomNpcs startup update message")
    public static boolean EnableUpdateChecker = true;

    public static LegacyLoader Config;

    public void init() {
        MinecraftServer server = MinecraftServer.getServer();
        String dir = "";
        if (server != null) {
            dir = new File(".").getAbsolutePath();
        } else {
            dir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
        }

        Config = new LegacyLoader(this.getClass(), new File(dir, "config"), "CustomNpcs");
        Config.loadConfig();
    }
}
