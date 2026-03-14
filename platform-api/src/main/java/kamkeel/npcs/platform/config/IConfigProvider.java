package kamkeel.npcs.platform.config;

/**
 * Version-independent abstraction over Minecraft Forge's Configuration system.
 *
 * Maps to:
 *  - 1.7.10 / 1.12: net.minecraftforge.common.config.Configuration
 *  - 1.16+:         net.minecraftforge.common.ForgeConfigSpec (different pattern)
 *
 * CORE config classes use this to load/save values without
 * depending on Forge APIs.
 */
public interface IConfigProvider {

    boolean getBoolean(String category, String key, boolean defaultValue, String comment);

    int getInt(String category, String key, int defaultValue, String comment);

    int getInt(String category, String key, int defaultValue, int min, int max, String comment);

    double getDouble(String category, String key, double defaultValue, String comment);

    String getString(String category, String key, String defaultValue, String comment);

    /**
     * Gets a string value from a set of valid values.
     */
    String getString(String category, String key, String defaultValue, String comment, String[] validValues);

    /**
     * Checks if the config has been changed since last save.
     */
    boolean hasChanged();

    /**
     * Saves the config file to disk.
     */
    void save();
}
