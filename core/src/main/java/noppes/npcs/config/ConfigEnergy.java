package noppes.npcs.config;

import common.minecraftforge.common.config.IConfiguration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigEnergy {
    public static IConfiguration config;

    public final static String DOMES = "Domes";
    public final static String EXPLOSIONS = "Explosions";

    public static String[] DomeItemBlacklist;
    /**
     * Server-side toggle for energy projectile terrain destruction.
     * Disabled by default to avoid block grief.
     */
    public static boolean EnableEnergyExplosionBlockDamage;

    public static void init(File configFile) {
        config = new IConfiguration(configFile);

        try {
            config.load();

            DomeItemBlacklist = config.getStringList(
                "Item Blacklist", DOMES,
                new String[]{"minecraft:ender_pearl"},
                "Items that cannot be used while inside an Energy Dome.\n"
                    + "Format: modid:itemname (e.g., minecraft:ender_pearl)"
            );

            EnableEnergyExplosionBlockDamage = config.getBoolean(
                "Enable Energy IExplosion Block Damage", EXPLOSIONS,
                false,
                "If true, explosive ENERGY projectile abilities can destroy terrain.\n"
                    + "This is intended for server-side use and is disabled by default."
            );

        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CustomNPC+ has had a problem loading its energy IConfiguration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
