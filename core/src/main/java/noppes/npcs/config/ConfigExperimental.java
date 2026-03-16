package noppes.npcs.config;

import common.minecraftforge.common.config.IConfigProperty;
import common.minecraftforge.common.config.IConfiguration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigExperimental {
    public static IConfiguration config;

    public final static String CLIENT = "Client";
    public final static String SERVER = "Server";
    public static IConfigProperty ModernGuiSystemProperty;
    public static boolean ModernGuiSystem = false;


    public static void init(File configFile) {
        config = new IConfiguration(configFile);

        try {
            config.load();

            ModernGuiSystemProperty = config.get(CLIENT, "Experimental Dialog GUI", false, "Enables the new CNPC+ Modern GUI for Dialog and Quest information");
            ModernGuiSystem = ModernGuiSystemProperty.getBoolean(false);
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its experimental IConfiguration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
