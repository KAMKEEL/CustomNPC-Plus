package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import noppes.npcs.constants.EnumScriptType;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigDebug {
    public static Configuration config;

    public final static String LOGGING = "Logging";

    /**
     * Logging Properties
     **/
    public static Property PlayerLoggingProperty;
    public static boolean PlayerLogging = false;

    public static Property ScriptLoggingProperty;
    public static boolean ScriptLogging = false;

    public static Property ScriptFrequencyProperty;
    public static int ScriptFrequency = 20;

    public static Property ScriptIgnoreTimeProperty;
    public static int ScriptIgnoreTime = 2000;

    public static Property ScriptLogIgnoreTypeProperty;
    public static String ScriptLogIgnoreType = "TICK";

    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // Logging
            PlayerLoggingProperty = config.get(LOGGING, "Enable Player Logging", false, "Enables if Player Information (WAND-USE) should be printed to CustomNPCs Logs. IF on Server \n" +
                "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3");
            PlayerLogging = PlayerLoggingProperty.getBoolean(false);

            ScriptLoggingProperty = config.get(LOGGING, "Enable Script Logging", false, "Enables if Scripting Information should be printed to CustomNPCs Logs. IF on Server \n" +
                "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3");
            ScriptLogging = ScriptLoggingProperty.getBoolean(false);

            ScriptFrequencyProperty = config.get(LOGGING, "Script Log Frequency Limit", 20, "Amount of Messages marked as SPAM [5, 3000]. Lower Number means MORE accurate messages \n" +
                "This frequency will determine if the log will print a line with [SPAM] to warn the console.");
            ScriptFrequency = ScriptFrequencyProperty.getInt(20);

            ScriptIgnoreTimeProperty = config.get(LOGGING, "Script Ignore Time Buffer", 2000, "IN Milliseconds 1s = 1000s. If a recent LOG of the same event is SENT within this threshold it will be ignored.");
            ScriptIgnoreTime = ScriptIgnoreTimeProperty.getInt(2000);

            ScriptLogIgnoreTypeProperty = config.get(LOGGING, "Script Type Ignore", "TICK", "Comma separated list of NPC Script Types that will omit these from the logs,\n" +
                "INIT,TICK,INTERACT,DIALOG,DAMAGED,KILLED,ATTACK,TARGET,COLLIDE,KILLS,DIALOG_CLOSE,TIMER");
            ScriptLogIgnoreType = ScriptLogIgnoreTypeProperty.getString();

            // Convert to Legacy
            if (CustomNpcs.legacyExist) {
                PlayerLogging = LegacyConfig.PlayerLogging;
                PlayerLoggingProperty.set(PlayerLogging);

                ScriptLogging = LegacyConfig.ScriptLogging;
                ScriptLoggingProperty.set(ScriptLogging);

                ScriptFrequency = LegacyConfig.ScriptFrequency;
                ScriptFrequencyProperty.set(ScriptFrequency);

                ScriptIgnoreTime = LegacyConfig.ScriptIgnoreTime;
                ScriptIgnoreTimeProperty.set(ScriptIgnoreTime);

                ScriptLogIgnoreType = LegacyConfig.ScriptLogIgnoreType;
                ScriptLogIgnoreTypeProperty.set(ScriptLogIgnoreType);
            }

            if (ScriptFrequency < 5)
                ScriptFrequency = 5;

            if (ScriptFrequency > 3000)
                ScriptFrequency = 3000;

            if (ScriptIgnoreTime < 0)
                ScriptIgnoreTime = 0;

            try {
                String[] ignoreTypes = ScriptLogIgnoreType.split(",");
                for (String s : ignoreTypes) {
                    EnumScriptType type = EnumScriptType.valueOfIgnoreCase(s);
                    if (type != null) {
                        switch (type) {
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
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its debug configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
