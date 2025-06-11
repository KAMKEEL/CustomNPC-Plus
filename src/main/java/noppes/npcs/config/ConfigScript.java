package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import kamkeel.npcs.developer.Developer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class ConfigScript {
    public static Configuration config;

    public final static String GENERAL = "Scripting General";
    public final static String CUSTOMIZATION = "Scripting Customization";

    /**
     * General Properties
     **/
    public static Property ScriptingEnabledProperty;
    public static boolean ScriptingEnabled = true;

    public static Property ScriptOpsOnlyProperty;
    public static boolean ScriptOpsOnly = false;

    public static Property GlobalPlayerScriptsProperty;
    public static boolean GlobalPlayerScripts = true;

    public static Property GlobalForgeScriptsProperty;
    public static boolean GlobalForgeScripts = true;

    public static Property GlobalNPCScriptsProperty;
    public static boolean GlobalNPCScripts = false;

    /**
     * Customization Properties
     **/
    public static Property ScriptingECMA6Property;
    public static boolean ScriptingECMA6 = false;

    public static Property ExpandedScriptLimitProperty;
    public static int ExpandedScriptLimit = 2;

    public static Property ScriptDevIDsProperty;
    public static String ScriptDevIDs = "";

    public static Property EnableBannedClassesProperty;
    public static boolean EnableBannedClasses = false;

    public static Property RunLoadedScriptsFirstProperty;
    public static boolean RunLoadedScriptsFirst = true;

    public static Property BannedClassesProperty;
    public final static HashSet<String> BannedClasses = new HashSet<>();

    public static int ActionManagerTickDefault = 5;

    public static boolean IndividualPlayerScripts = false;

    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // General
            ScriptingEnabledProperty = config.get(GENERAL, "Enable Scripting", true, "Enables/Disables ALL scripting. You can still see and write code in the scripter, but these scripts won't run. True by default");
            ScriptingEnabled = ScriptingEnabledProperty.getBoolean(true);

            ScriptOpsOnlyProperty = config.get(GENERAL, "OPs Only", false, "Only ops can see and edit scripts");
            ScriptOpsOnly = ScriptOpsOnlyProperty.getBoolean(false);

            GlobalPlayerScriptsProperty = config.get(GENERAL, "Enable Global Player Scripts", true, "Enables global player event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.");
            GlobalPlayerScripts = GlobalPlayerScriptsProperty.getBoolean(true);

            GlobalForgeScriptsProperty = config.get(GENERAL, "Enable Global Forge Event Scripts", true, "Enables global forge event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.");
            GlobalForgeScripts = GlobalForgeScriptsProperty.getBoolean(true);

            GlobalNPCScriptsProperty = config.get(GENERAL, "Enable Global NPC Scripts", false, "Enables global NPC scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. False by default, use with caution!");
            GlobalNPCScripts = GlobalNPCScriptsProperty.getBoolean(false);

            // Customization
            ScriptingECMA6Property = config.get(CUSTOMIZATION, "ECMA6 Scripting Language", false,
                "Enables/Disables the use of the the ECMA6 Javascript standard instead of ECMA5.1." +
                    "\nEnabling this adds many more features to JS in scripts. Only use if on Java 8 or higher." +
                    "\nNot all ECMA 6 language is supported through this functionality.");
            ScriptingECMA6 = ScriptingECMA6Property.getBoolean(false);

            ExpandedScriptLimitProperty = config.get(CUSTOMIZATION, "Script Tag Limit", 2,
                "If scripts are too long (>65535 characters), they normally won't be saved in NBT data.\n" +
                    "This config adds additional compound tags to scripts that need it, so you can store much larger scripts!\n" +
                    "Every additional compound tag adds 65535 more characters to your script length limit. Use incrementally, with caution.");
            ExpandedScriptLimit = ExpandedScriptLimitProperty.getInt(2);

            ScriptDevIDsProperty = config.get(CUSTOMIZATION, "Script Dev UUIDs", "",
                "Comma separated list of player UUIDs that can see and edit scripts. If ScriptsOpsOnly is true,\n" +
                    "ops and players with these IDs can see and edit scripts. Example:\n" +
                    "b876ec32-e396-476b-a115-8438d83c67d4,069a79f4-44e9-4726-a5be-fca90e38aaf5,be951074-c7ea-4f02-a725-bf017bc88650\n" +
                    "Get a player's UUID from a site like NameMC or the API IPlayer.getUniqueID() function!\n" +
                    "If left empty and ScriptsOpsOnly is false, anyone can see and edit scripts with a scripter.");
            ScriptDevIDs = ScriptDevIDsProperty.getString();

            EnableBannedClassesProperty = config.get(CUSTOMIZATION, "Enable Banned Classes", false, "Enables the Banned Classes Functionality");
            EnableBannedClasses = EnableBannedClassesProperty.getBoolean(false);

            RunLoadedScriptsFirstProperty = config.get(CUSTOMIZATION, "Run Loaded Scripts First", true,
                "If scripts have been loaded from the scripting GUI, the script engine will evaluates them by\n" +
                    "merging your loaded scripts with your main script, then running the combined script.\n" +
                    "This config determines the order in while the loaded scripts are merged with your main scripts: before (true), or after (false).");
            RunLoadedScriptsFirst = RunLoadedScriptsFirstProperty.getBoolean(true);

            BannedClassesProperty = config.get(CUSTOMIZATION, "Banned Classes", "java.net.URL,java.net.URI",
                "Comma separated list of classes that cannot be used in scripts through Java.for().\n" +
                    "Classes must be fully written out with library names preceding them.\n" +
                    "This is a feature ONLY AVAILABLE ON NASHORN.");


            IndividualPlayerScripts = config.get(CUSTOMIZATION, "Individual Player Scripts", false, "Acts similar to CNPC 1.12 where Player Scripts like Init are run PER Player").getBoolean(false);
            ActionManagerTickDefault = config.get(CUSTOMIZATION, "Action Manager Tick Default", ActionManagerTickDefault, "How frequent to update the action manager ticking tasks").getInt(ActionManagerTickDefault);

            // Convert to Legacy
            if (CustomNpcs.legacyExist) {
                ScriptingEnabled = LegacyConfig.ScriptingEnabled;
                ScriptingEnabledProperty.set(ScriptingEnabled);

                ScriptOpsOnly = LegacyConfig.ScriptOpsOnly;
                ScriptOpsOnlyProperty.set(ScriptOpsOnly);

                GlobalPlayerScripts = LegacyConfig.GlobalPlayerScripts;
                GlobalPlayerScriptsProperty.set(GlobalPlayerScripts);

                GlobalForgeScripts = LegacyConfig.GlobalForgeScripts;
                GlobalForgeScriptsProperty.set(GlobalForgeScripts);

                GlobalNPCScripts = LegacyConfig.GlobalNPCScripts;
                GlobalNPCScriptsProperty.set(GlobalNPCScripts);

                ScriptingECMA6 = LegacyConfig.ScriptingECMA6;
                ScriptingECMA6Property.set(ScriptingECMA6);

                ExpandedScriptLimit = LegacyConfig.ExpandedScriptLimit;
                ExpandedScriptLimitProperty.set(ExpandedScriptLimit);

                ScriptDevIDs = LegacyConfig.ScriptDevIDs;
                ScriptDevIDsProperty.set(ScriptDevIDs);
            }

            if (ExpandedScriptLimit < 0)
                ExpandedScriptLimit = 0;

            if(ActionManagerTickDefault < 0){
                ActionManagerTickDefault = 1;
            }

            try {
                Developer.ScriptUser.clear();
                String[] uuidStrings = ScriptDevIDs.split(",");
                for (String s : uuidStrings) {
                    Developer.ScriptUser.add(UUID.fromString(s));
                }
            } catch (Exception ignored) {
            }

            String bannedClassesString = BannedClassesProperty.getString();
            try {
                BannedClasses.clear();
                BannedClasses.addAll(Arrays.asList(bannedClassesString.split(",")));
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its scripting configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static boolean isScriptDev(EntityPlayer player) {
        return Developer.ScriptUser.contains(player.getUniqueID()) || Developer.Universal.contains(player.getUniqueID());
    }

    public static boolean canScript(EntityPlayer player, CustomNpcsPermissions.Permission perm) {
        boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
        boolean scriptDev = isScriptDev(player);
        if (ScriptOpsOnly) {
            return isOp || scriptDev;
        }

        return scriptDev || CustomNpcsPermissions.hasPermission(player, perm);
    }
}
