package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigEnergy {
    public static Configuration config;

    public final static String DOMES = "Domes";
    public final static String EXPLOSIONS = "Explosions";

    public static String[] DomeItemBlacklist;
    /**
     * Server-side toggle for energy projectile terrain destruction.
     * Disabled by default to avoid block grief.
     */
    public static boolean EnableEnergyExplosionBlockDamage;

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

            EnableEnergyExplosionBlockDamage = config.getBoolean(
                "Enable Energy Explosion Block Damage", EXPLOSIONS,
                false,
                "If true, explosive ENERGY projectile abilities can destroy terrain.\n"
                    + "This is intended for server-side use and is disabled by default."
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
