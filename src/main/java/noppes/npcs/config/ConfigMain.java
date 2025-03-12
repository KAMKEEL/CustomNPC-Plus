package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigMain
{
    public static Configuration config;

    public final static String GENERAL = "General";
    public final static String NPC = "NPC";
    public final static String UPDATE = "Update";
    public final static String PARTY = "PARTY";
    public final static String PROFILES = "Profile";
    public final static String ATTRIBUTES = "Attributes";
    public final static String ITEM = "Item";

    /**
     *  General Main Properties
     **/

    public static Property EnableUpdateCheckerProperty;
    public static boolean EnableUpdateChecker = true;

    public static Property DisableEnchantsProperty;
    public static boolean DisableEnchants = false;

    public static Property EnchantStartIdProperty;
    public static int EnchantStartId = 100;

    public static Property LeavesDecayEnabledProperty;
    public static boolean LeavesDecayEnabled = true;

    public static Property VineGrowthEnabledProperty;
    public static boolean VineGrowthEnabled = true;

    public static Property IceMeltsEnabledProperty;
    public static boolean IceMeltsEnabled = true;

    public static Property SoulStoneAnimalsProperty;
    public static boolean SoulStoneAnimals = true;

    public static Property SoulStoneVillagersProperty;
    public static boolean SoulStoneVillagers = false;

    public static Property SoulStoneNPCsProperty;
    public static boolean SoulStoneNPCs = false;

    public static Property SoulStoneFriendlyNPCsProperty;
    public static boolean SoulStoneFriendlyNPCs = false;

    public static Property DatFormatProperty;
    public static boolean DatFormat = false;

    public static Property MarketDatFormatProperty;
    public static boolean MarketDatFormat = false;

    public static Property PartiesEnabledProperty;
    public static boolean PartiesEnabled;

    public static Property PartyFriendlyFireEnabledProperty;
    public static boolean PartyFriendlyFireEnabled;

    public static Property DefaultMinPartySizeProperty;
    public static int DefaultMinPartySize = 1;

    public static Property DefaultMaxPartySizeProperty;
    public static int DefaultMaxPartySize = 4;

    /**
     *  Attribute Properties
     **/
    public static boolean AttributesEnabled = true;
    public static int AttributesCriticalBoost = 100;

    /**
     *  Profile Properties
     **/
    public static boolean ProfilesEnabled = true;
    public static boolean AllowProfileBackups = true;
    public static int ProfileBackupAmount = 5;

    public static Property RegionProfileSwitchingProperty;
    public static boolean RegionProfileSwitching = false;

    public static Property RestrictedProfileRegionsProperty;
    public static List<List<Integer>> RestrictedProfileRegions = new ArrayList<>();


    /**
     *  General NPC Properties
     **/

    public static Property OpsOnlyProperty;
    public static boolean OpsOnly = false;

    public static Property NpcNavRangeProperty;
    public static int NpcNavRange = 32;

    public static Property NpcSizeLimitProperty;
    public static int NpcSizeLimit = 100;

    public static Property DefaultInteractLineProperty;
    public static String DefaultInteractLine = "Hello @p";

    public static Property ChunkLoadersProperty;
    public static int ChunkLoaders = 20;

    public static Property DialogImageLimitProperty;
    public static int DialogImageLimit = 10;

    public static Property SkinOverlayLimitProperty;
    public static int SkinOverlayLimit = 10;

    public static Property NpcUseOpCommandsProperty;
    public static boolean NpcUseOpCommands = false;

    public static Property HitBoxScaleMaxProperty;
    public static int HitBoxScaleMax = 15;

    /**
     *  Update Properties
     **/

    public static Property TrackedQuestUpdateFrequencyProperty;
    public static int TrackedQuestUpdateFrequency = 5;

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();

            // General
            EnableUpdateCheckerProperty = config.get(GENERAL, "Enables Update Checker", true);
            EnableUpdateChecker = EnableUpdateCheckerProperty.getBoolean(true);

            DisableEnchantsProperty = config.get(GENERAL, "Disable Enchants", false);
            DisableEnchants = DisableEnchantsProperty.getBoolean(false);

            EnchantStartIdProperty = config.get(GENERAL, "Enchant Start ID", 100, "Start ID for enchants. IDs can only range from 0-256");
            EnchantStartId = EnchantStartIdProperty.getInt(100);

            LeavesDecayEnabledProperty = config.get(GENERAL, "Leaves Decay Enabled", true);
            LeavesDecayEnabled = LeavesDecayEnabledProperty.getBoolean(true);

            VineGrowthEnabledProperty = config.get(GENERAL, "Vine Growth Enabled", true);
            VineGrowthEnabled = VineGrowthEnabledProperty.getBoolean(true);

            IceMeltsEnabledProperty = config.get(GENERAL, "Ice Melt Enabled", true);
            IceMeltsEnabled = IceMeltsEnabledProperty.getBoolean(true);

            SoulStoneAnimalsProperty = config.get(GENERAL, "Normal playes can use soulstone on Animals", true);
            SoulStoneAnimals = SoulStoneAnimalsProperty.getBoolean(true);

            SoulStoneVillagersProperty = config.get(GENERAL, "Normal playes can use soulstone on Villagers", true);
            SoulStoneVillagers = SoulStoneVillagersProperty.getBoolean(true);

            SoulStoneNPCsProperty = config.get(GENERAL, "Normal playes can use soulstone on NPCs", false);
            SoulStoneNPCs = SoulStoneNPCsProperty.getBoolean(false);

            SoulStoneFriendlyNPCsProperty = config.get(GENERAL, "Normal playes can use soulstone on Friendly NPCs", false);
            SoulStoneFriendlyNPCs = SoulStoneFriendlyNPCsProperty.getBoolean(false);

            DatFormatProperty = config.get(GENERAL, "Dat Format for PlayerData", false, "You need to use '/kamkeel config playerdata' to convert existing playerdata to new format.");
            DatFormat = DatFormatProperty.getBoolean(false);

            MarketDatFormatProperty = config.get(GENERAL, "Dat Format for Market", false, "You need to use '/kamkeel config market' to convert existing market to new format.");
            MarketDatFormat = MarketDatFormatProperty.getBoolean(false);

            // NPC
            NpcNavRangeProperty = config.get(NPC, "NPC Navigation Range", 32, "Navigation search range for NPCs. Not recommended to increase if you have a slow pc or on a server. Minimum of 16, maximum of 96.");
            NpcNavRange = NpcNavRangeProperty.getInt(32);

            OpsOnlyProperty = config.get(NPC, "Only Ops Edit NPCs", false, "Only ops can create and edit npcs");
            OpsOnly = OpsOnlyProperty.getBoolean(false);

            NpcUseOpCommandsProperty = config.get(NPC, "NPC Use Op Commands", false, "Set to true if you want the dialog command option to be able to use op commands like tp etc");
            NpcUseOpCommands = NpcUseOpCommandsProperty.getBoolean(false);

            DefaultInteractLineProperty = config.get(NPC, "Default Interaction Line", "Hello @p", "Default interact line. Leave empty to not have one");
            DefaultInteractLine = DefaultInteractLineProperty.getString();

            ChunkLoadersProperty = config.get(NPC, "Chunk Loader Limit", 20, "Number of chunk loading npcs that can be active at the same time");
            ChunkLoaders = ChunkLoadersProperty.getInt(20);

            DialogImageLimitProperty = config.get(NPC, "Dialog Image Limit", 10, "The maximum number of images any dialog can hold.");
            DialogImageLimit = DialogImageLimitProperty.getInt(10);

            NpcSizeLimitProperty = config.get(NPC, "NPC Size Limit", 100, "Size limit for NPCs. Default 100, larger sizes may cause lag on clients and servers that can't take it!");
            NpcSizeLimit = NpcSizeLimitProperty.getInt(100);

            SkinOverlayLimitProperty = config.get(NPC, "Skin Overlay Limit", 10, "The maximum number of overlays any npc/player can hold.");
            SkinOverlayLimit = SkinOverlayLimitProperty.getInt(10);

            HitBoxScaleMaxProperty = config.get(NPC, "HitBox Scale Limit", 15, "The maximum scale factor for a custom hitbox");
            HitBoxScaleMax = HitBoxScaleMaxProperty.getInt(15);

            //PARTY
            PartiesEnabledProperty = config.get(PARTY, "Parties Enabled", true, "Determines whether the party system is enabled or not.");
            PartiesEnabled = PartiesEnabledProperty.getBoolean();

            PartyFriendlyFireEnabledProperty = config.get(PARTY, "Party Friendly Fire", true, "Determines whether friendly fire can be toggled in parties.");
            PartyFriendlyFireEnabled = PartyFriendlyFireEnabledProperty.getBoolean();

            DefaultMinPartySizeProperty = config.get(PARTY, "Default Min Party Size", 1, "When creating a new Quest sets the default min party size");
            DefaultMinPartySize = DefaultMinPartySizeProperty.getInt(4);

            DefaultMaxPartySizeProperty = config.get(PARTY, "Default Max Party Size", 4, "When creating a new Quest sets the default max party size");
            DefaultMaxPartySize = DefaultMaxPartySizeProperty.getInt(4);

            // Update
            TrackedQuestUpdateFrequencyProperty = config.get(UPDATE, "Tracked Quest Update Frequency", 5, "How often in seconds to update a players tracked quest. [Only applies to Item Quest currently]");
            TrackedQuestUpdateFrequency = TrackedQuestUpdateFrequencyProperty.getInt(5);

            // PROFILES
            ProfilesEnabled = config.get(PROFILES, "Enable Profiles", true, "Allow the use of character Profiles").getBoolean(true);
            AllowProfileBackups = config.get(PROFILES, "Enable Profile Backups", true, "Will create backups of profile changes").getBoolean(true);
            ProfileBackupAmount = config.get(PROFILES, "Number of Backups", 5, "How many backups per player to save").getInt(5);

            RegionProfileSwitchingProperty = config.get(PROFILES, "Region Profile Switching", false, "If true, only allows profile switching in certain regions.");
            RegionProfileSwitching = RegionProfileSwitchingProperty.getBoolean(false);

            RestrictedProfileRegionsProperty = config.get(PROFILES, "Restricted Profile Regions", new String[]{
                "0, 100, 64, 100, 200, 80, 200",
                "1, 50, 60, 50, 150, 75, 150"
            }, "List of restricted regions where profile switching is enabled. Format: DIM, X1, Y1, Z1, X2, Y2, Z2");

            RestrictedProfileRegions.clear();
            for (String region : RestrictedProfileRegionsProperty.getStringList()) {
                String[] parts = region.split(", ");
                List<Integer> regionList = new ArrayList<>();
                for (String part : parts) {
                    regionList.add(Integer.parseInt(part));
                }
                RestrictedProfileRegions.add(regionList);
            }

            config.setCategoryPropertyOrder(ATTRIBUTES, new ArrayList<>(Arrays.asList(new String[]{
                "Enable Attributes",
                "Critical Amount"
            })));

            // Attributes
            AttributesEnabled = config.get(ATTRIBUTES, "Enable Attributes", true, "Allows Attributes to be applied to Items and Armors").getBoolean(true);
            AttributesCriticalBoost = config.get(ATTRIBUTES, "Critical Amount", 100, "The boost in damage received by achieving a critical hit. Takes an number so 100 is 100% extra damage").getInt(100);

            // Convert to Legacy
            if(CustomNpcs.legacyExist){
                EnableUpdateChecker = LegacyConfig.EnableUpdateChecker;
                EnableUpdateCheckerProperty.set(EnableUpdateChecker);

                DisableEnchants = LegacyConfig.DisableEnchants;
                DisableEnchantsProperty.set(DisableEnchants);

                EnchantStartId = LegacyConfig.EnchantStartId;
                EnchantStartIdProperty.set(EnchantStartId);

                LeavesDecayEnabled = LegacyConfig.LeavesDecayEnabled;
                LeavesDecayEnabledProperty.set(LeavesDecayEnabled);

                VineGrowthEnabled = LegacyConfig.VineGrowthEnabled;
                VineGrowthEnabledProperty.set(VineGrowthEnabled);

                IceMeltsEnabled = LegacyConfig.IceMeltsEnabled;
                IceMeltsEnabledProperty.set(IceMeltsEnabled);

                SoulStoneAnimals = LegacyConfig.SoulStoneAnimals;
                SoulStoneAnimalsProperty.set(SoulStoneAnimals);

                SoulStoneVillagers = LegacyConfig.SoulStoneVillagers;
                SoulStoneVillagersProperty.set(SoulStoneVillagers);

                SoulStoneNPCs = LegacyConfig.SoulStoneNPCs;
                SoulStoneNPCsProperty.set(SoulStoneNPCs);

                SoulStoneFriendlyNPCs = LegacyConfig.SoulStoneFriendlyNPCs;
                SoulStoneFriendlyNPCsProperty.set(SoulStoneFriendlyNPCs);

                OpsOnly = LegacyConfig.OpsOnly;
                OpsOnlyProperty.set(OpsOnly);

                NpcNavRange = LegacyConfig.NpcNavRange;
                NpcNavRangeProperty.set(NpcNavRange);

                NpcSizeLimit = LegacyConfig.NpcSizeLimit;
                NpcSizeLimitProperty.set(NpcSizeLimit);

                DefaultInteractLine = LegacyConfig.DefaultInteractLine;
                DefaultInteractLineProperty.set(DefaultInteractLine);

                ChunkLoaders = LegacyConfig.ChunkLoaders;
                ChunkLoadersProperty.set(ChunkLoaders);

                DialogImageLimit = LegacyConfig.DialogImageLimit;
                DialogImageLimitProperty.set(DialogImageLimit);

                SkinOverlayLimit = LegacyConfig.SkinOverlayLimit;
                SkinOverlayLimitProperty.set(SkinOverlayLimit);

                NpcUseOpCommands = LegacyConfig.NpcUseOpCommands;
                NpcUseOpCommandsProperty.set(NpcUseOpCommands);
            }

            if (NpcNavRange < 16) {
                NpcNavRange = 16;
            }
            if (NpcNavRange > 96) {
                NpcNavRange = 96;
            }

            if(NpcSizeLimit < 1){
                NpcSizeLimit = 1;
            }

            if (DialogImageLimit < 0) {
                DialogImageLimit = 0;
            }

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
