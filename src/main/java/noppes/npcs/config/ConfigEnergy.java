package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigEnergy {
    public static Configuration config;

    public final static String DOMES = "Domes";

    public static String[] DomeItemBlacklist;

    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            DomeItemBlacklist = config.getStringList(
                "Item Blacklist", DOMES,
                new String[]{"minecraft:ender_pearl"},
                "Items that cannot be used while inside an Energy Dome.\n"
                    + "Format: modid:itemname (e.g., minecraft:ender_pearl)"
            );

        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CustomNPC+ has had a problem loading its energy configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
