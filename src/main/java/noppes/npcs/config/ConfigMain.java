package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigMain
{
    public static Configuration config;

    public static boolean EnableUpdateChecker = true;

    public static boolean DisableExtraBlock = false;
    public static boolean DisableExtraItems = false;
    public static boolean DisableEnchants = false;
    public static boolean GunsEnabled = true;
    public static int EnchantStartId = 100;

    public static boolean LeavesDecayEnabled = true;
    public static boolean VineGrowthEnabled = true;
    public static boolean IceMeltsEnabled = true;
    public static boolean SoulStoneAnimals = true;
    public static boolean SoulStoneVillagers = false;
    public static boolean SoulStoneNPCs = false;
    public static boolean SoulStoneFriendlyNPCs = false;

    public static boolean OpsOnly = false;
    public static int NpcNavRange = 32;
    public static int NpcSizeLimit = 100;
    public static String DefaultInteractLine = "Hello @p";
    public static int ChunkLoaders = 20;
    public static int DialogImageLimit = 10;
    public static int SkinOverlayLimit = 10;

    public static boolean NpcUseOpCommands = false;

    public final static String GENERAL = "NPC";
    public final static String NPC = "NPC";

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();
            EnableUpdateChecker = config.get(GENERAL, "Enables Update Checker", true).getBoolean(true);

            DisableExtraBlock = config.get(GENERAL, "Disable Extra Blocks", false).getBoolean(false);
            DisableExtraItems = config.get(GENERAL, "Disable Extra Items", false).getBoolean(false);
            GunsEnabled = config.get(GENERAL, "Guns Enabled", true, "Set to false if you want to disable guns").getBoolean(true);
            DisableEnchants = config.get(GENERAL, "Disable Enchants", false).getBoolean(false);
            EnchantStartId = config.get(GENERAL, "Enchant Start ID", 100, "Start ID for enchants. IDs can only range from 0-256").getInt(100);
            LeavesDecayEnabled = config.get(GENERAL, "Leaves Decay Enabled", true).getBoolean(true);
            VineGrowthEnabled = config.get(GENERAL, "Vine Growth Enabled", true).getBoolean(true);
            IceMeltsEnabled = config.get(GENERAL, "Ice Melt Enabled", true).getBoolean(true);
            SoulStoneAnimals = config.get(GENERAL, "Normal playes can use soulstone on Animals", true).getBoolean(true);
            SoulStoneVillagers = config.get(GENERAL, "Normal playes can use soulstone on Villagers", true).getBoolean(true);
            SoulStoneNPCs = config.get(GENERAL, "Normal playes can use soulstone on NPCs", false).getBoolean(false);
            SoulStoneFriendlyNPCs = config.get(GENERAL, "Normal playes can use soulstone on Friendly NPCs", false).getBoolean(false);

            NpcNavRange = config.get(NPC, "NPC Navigation Range", 32, "Navigation search range for NPCs. Not recommended to increase if you have a slow pc or on a server. Minimum of 16, maximum of 96.").getInt(32);
            if (NpcNavRange < 16) {
                NpcNavRange = 16;
            }
            if (NpcNavRange > 96) {
                NpcNavRange = 96;
            }

            NpcSizeLimit = config.get(NPC, "NPC Size Limit", 100, "Size limit for NPCs. Default 100, larger sizes may cause lag on clients and servers that can't take it!").getInt(100);
            if(NpcSizeLimit < 1)
                NpcSizeLimit = 1;

            OpsOnly = config.get(NPC, "Only Ops Edit NPCs", false, "Only ops can create and edit npcs").getBoolean(false);
            NpcUseOpCommands = config.get(NPC, "NPC Use Op Commands", false, "Set to true if you want the dialog command option to be able to use op commands like tp etc").getBoolean(false);
            DefaultInteractLine = config.get(NPC, "Default Interaction Line", "Hello @p", "Default interact line. Leave empty to not have one").getString();
            ChunkLoaders = config.get(NPC, "Chunk Loader Limit", 20, "Number of chunk loading npcs that can be active at the same time").getInt(20);

            DialogImageLimit = config.get(NPC, "Dialog Image Limit", 10, "The maximum number of images any dialog can hold.").getInt(10);
            if (DialogImageLimit < 0) {
                DialogImageLimit = 0;
            }

            SkinOverlayLimit = config.get(NPC, "Skin Overlay Limit", 10, "The maximum number of overlays any npc/player can hold.").getInt(10);
            if (SkinOverlayLimit < 0) {
                SkinOverlayLimit = 0;
            }
        }
        catch (Exception e)
        {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its main configuration");
        }
        finally
        {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}