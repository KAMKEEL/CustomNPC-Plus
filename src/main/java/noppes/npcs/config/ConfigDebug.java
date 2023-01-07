package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumScriptType;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigDebug
{
    public static Configuration config;

    public static boolean PlayerLogging = false;

    public static boolean ScriptLogging = false;
    public static int ScriptFrequency = 20;
    public static int ScriptIgnoreTime = 2000;
    public static String ScriptLogIgnoreType = "TICK";

    public final static String LOGGING = "Visual";

    public static void init(File configFile)
    {
        config = new Configuration(configFile);

        try
        {
            config.load();
            PlayerLogging = config.get(LOGGING, "Enable Player Logging", false, "Enables if Player Information (WAND-USE) should be printed to CustomNPCs Logs. IF on Server \n" +
                    "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3").getBoolean(false);

            ScriptLogging = config.get(LOGGING, "Enable Script Logging", false, "Enables if Scripting Information should be printed to CustomNPCs Logs. IF on Server \n" +
                    "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3").getBoolean(false);
            ScriptFrequency = config.get(LOGGING, "Script Log Frequency Limit", 20, "Amount of Messages marked as SPAM [5, 3000]. Lower Number means MORE accurate messages \n" +
                    "This frequency will determine if the log will print a line with [SPAM] to warn the console.").getInt(20);
            if (ScriptFrequency < 5)
                ScriptFrequency = 5;
            if (ScriptFrequency > 3000)
                ScriptFrequency = 3000;

            ScriptIgnoreTime = config.get(LOGGING, "Script Ignore Time Buffer", 2000, "IN Milliseconds 1s = 1000s. If a recent LOG of the same event is SENT within this threshold it will be ignored.").getInt(2000);
            if (ScriptIgnoreTime < 0)
                ScriptIgnoreTime = 0;

            ScriptLogIgnoreType = config.get(LOGGING, "Script Type Ignore", "TICK", "Comma separated list of NPC Script Types that will omit these from the logs,\n" +
                    "INIT,TICK,INTERACT,DIALOG,DAMAGED,KILLED,ATTACK,TARGET,COLLIDE,KILLS,DIALOG_CLOSE,TIMER").getString();
            try {
                String[] ignoreTypes = ScriptLogIgnoreType.split(",");
                for (String s : ignoreTypes) {
                    EnumScriptType type = EnumScriptType.valueOfIgnoreCase(s);
                    if(type != null){
                        switch (type){
                            case INIT:
                                CustomNpcs.InitIgnore = true;
                                break;
                            case TICK:
                                CustomNpcs.TickIgnore = true;
                                break;
                            case INTERACT:
                                CustomNpcs.InteractIgnore = true;
                                break;
                            case DIALOG:
                                CustomNpcs.DialogIgnore = true;
                                break;
                            case DAMAGED:
                                CustomNpcs.DamagedIgnore = true;
                                break;
                            case KILLED:
                                CustomNpcs.KilledIgnore = true;
                                break;
                            case ATTACK:
                                CustomNpcs.AttackIgnore = true;
                                break;
                            case TARGET:
                                CustomNpcs.TargetIgnore = true;
                                break;
                            case COLLIDE:
                                CustomNpcs.CollideIgnore = true;
                                break;
                            case KILLS:
                                CustomNpcs.KillsIgnore = true;
                                break;
                            case DIALOG_CLOSE:
                                CustomNpcs.DialogCloseIgnore = true;
                                break;
                            case TIMER:
                                CustomNpcs.TimerIgnore = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        catch (Exception e)
        {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its debug configuration");
        }
        finally
        {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}