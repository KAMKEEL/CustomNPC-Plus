package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigExperimental {
    public static Configuration config;

    public final static String CLIENT = "Client";
    public final static String SERVER = "Server";
    public static Property ModernGuiSystemProperty;
    public static boolean ModernGuiSystem = false;


    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            ConfigExperimental.ModernGuiSystemProperty = config.get(CLIENT, "Experimental Modern GUI", false, "Enables the new CNPC+ Modern GUI for Dialog and Quest information");
            ConfigExperimental.ModernGuiSystem = ConfigExperimental.ModernGuiSystemProperty.getBoolean(false);
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its experimental configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
