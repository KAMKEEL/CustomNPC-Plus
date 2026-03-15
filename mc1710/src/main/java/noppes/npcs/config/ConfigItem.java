package noppes.npcs.config;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.LegacyConfig;
import org.apache.logging.log4j.Level;

import java.io.File;

public class ConfigItem {
    public static Configuration config;

    public final static String ALL = "All";
    public final static String GUN = "Gun";
    /**
     * Item Properties
     **/
    public static Property DisableExtraBlockProperty;
    public static boolean DisableExtraBlock = false;
    public static Property DisableExtraItemsProperty;
    public static boolean DisableExtraItems = false;
    public static Property GunsEnabledProperty;
    public static boolean GunsEnabled = true;
    public static int MachineGunAmmo = 8;
    public static int MachineGunTickSpeed = 6;

    /**
     * Item Properties
     **/


    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // ITEMS
            DisableExtraBlockProperty = config.get(ALL, "Disable Extra Blocks", false);
            DisableExtraBlock = DisableExtraBlockProperty.getBoolean(false);

            DisableExtraItemsProperty = config.get(ALL, "Disable Extra Items", false);
            DisableExtraItems = DisableExtraItemsProperty.getBoolean(false);

            // Gun
            GunsEnabledProperty = config.get(GUN, "Guns Enabled", true, "Set to false if you want to disable guns");
            GunsEnabled = GunsEnabledProperty.getBoolean(true);
            MachineGunAmmo = config.get(GUN, "Machine Gun Ammo", 8, "Max Clip of a Machine Gun").getInt(8);
            MachineGunTickSpeed = config.get(GUN, "Machine Gun Fire Rate", 6, "How many ticks to shoot bullets at").getInt(6);

            // Convert to Legacy
            if (CustomNpcs.legacyExist) {
                DisableExtraBlock = LegacyConfig.DisableExtraBlock;
                DisableExtraBlockProperty.set(DisableExtraBlock);

                DisableExtraItems = LegacyConfig.DisableExtraItems;
                DisableExtraItemsProperty.set(DisableExtraItems);

                GunsEnabled = LegacyConfig.GunsEnabled;
                GunsEnabledProperty.set(GunsEnabled);
            }
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "CNPC+ has had a problem loading its item configuration");
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
