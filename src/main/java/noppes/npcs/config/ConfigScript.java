package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import noppes.npcs.CustomNpcs;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.UUID;

public class ConfigScript
{
    public static Configuration config;

    public static boolean ScriptingEnabled = true;
    public static boolean ScriptingECMA6 = false;
    public static boolean GlobalPlayerScripts = true;
    public static boolean GlobalForgeScripts = true;
    public static boolean GlobalNPCScripts = false;
    public static int ExpandedScriptLimit = 2;
    public static boolean ScriptOpsOnly = false;
    public static String ScriptDevIDs = "";

    public final static String GENERAL = "Scripting General";
    public final static String CUSTOMIZATION = "Scripting Customization";

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();
            ScriptingEnabled = config.get(GENERAL, "Enable Scripting", true, "Enables/Disables ALL scripting. You can still see and write code in the scripter, but these scripts won't run. True by default").getBoolean(true);
            ScriptOpsOnly = config.get(GENERAL, "OPs Only", false, "Only ops can see and edit scripts").getBoolean(false);
            GlobalPlayerScripts = config.get(GENERAL, "Enable Global Player Scripts", true, "Enables global player event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.").getBoolean(true);
            GlobalForgeScripts = config.get(GENERAL, "Enable Global Forge Event Scripts", true, "Enables global forge event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.").getBoolean(true);
            GlobalNPCScripts = config.get(GENERAL, "Enable Global NPC Scripts", false, "Enables global NPC scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. False by default, use with caution!").getBoolean(false);

            ScriptingECMA6 = config.get(CUSTOMIZATION, "ECMA6 Scripting Language", false,
                    "Enables/Disables the use of the the ECMA6 Javascript standard instead of ECMA5.1." +
                            "\nEnabling this adds many more features to JS in scripts. Only use if on Java 8 or higher." +
                            "\nNot all ECMA 6 language is supported through this functionality.").getBoolean(false);
            ExpandedScriptLimit = config.get(CUSTOMIZATION, "Script Tag Limit", 2,
                    "If scripts are too long (>65535 characters), they normally won't be saved in NBT data.\n" +
                    "This config adds additional compound tags to scripts that need it, so you can store much larger scripts!\n" +
                    "Every additional compound tag adds 65535 more characters to your script length limit. Use incrementally, with caution.").getInt(2);
            if (ExpandedScriptLimit < 0)
                ExpandedScriptLimit = 0;

            ScriptDevIDs = config.get(CUSTOMIZATION, "Script Dev UUIDs", "",
                    "Comma separated list of player UUIDs that can see and edit scripts. If ScriptsOpsOnly is true,\n" +
                            "ops and players with these IDs can see and edit scripts. Example:\n" +
                            "b876ec32-e396-476b-a115-8438d83c67d4,069a79f4-44e9-4726-a5be-fca90e38aaf5,be951074-c7ea-4f02-a725-bf017bc88650\n" +
                            "Get a player's UUID from a site like NameMC or the API IPlayer.getUniqueID() function!\n" +
                            "If left empty and ScriptsOpsOnly is false, anyone can see and edit scripts with a scripter.").getString();
            try {
                CustomNpcs.ScriptDevs.clear();
                String[] uuidStrings = ScriptDevIDs.split(",");
                for (String s : uuidStrings) {
                    CustomNpcs.ScriptDevs.add(UUID.fromString(s));
                }
            } catch (Exception ignored) {}

        }
        catch (Exception e)
        {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its scripting configuration");
        }
        finally
        {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static boolean isScriptDev(EntityPlayer player) {
        if(ScriptOpsOnly && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()) ||
                CustomNpcs.ScriptDevs.contains(player.getUniqueID())){
            return true;
        } else return ScriptDevIDs.isEmpty();
    }
}