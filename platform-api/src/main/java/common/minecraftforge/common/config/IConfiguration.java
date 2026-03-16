package common.minecraftforge.common.config;

/**
 * Platform abstraction for Forge's Configuration file handler.
 * MC 1.7.10: net.minecraftforge.common.config.Configuration
 * MC 1.12+: net.minecraftforge.common.config.Configuration
 * MC 1.14+: ForgeConfigSpec (different pattern, adapter needed)
 */
public interface IConfiguration {
    IConfigProperty get(String category, String key, String defaultValue, String comment);
    IConfigProperty get(String category, String key, int defaultValue, String comment);
    IConfigProperty get(String category, String key, boolean defaultValue, String comment);
    IConfigProperty get(String category, String key, double defaultValue, String comment);
    IConfigProperty get(String category, String key, String defaultValue, String comment, String[] validValues);
    IConfigProperty get(String category, String key, int defaultValue, String comment, int minValue, int maxValue);
    IConfigProperty get(String category, String key, double defaultValue, String comment, double minValue, double maxValue);
    void save();
    void load();
    boolean hasKey(String category, String key);
    boolean hasCategory(String category);
    boolean hasChanged();
}
