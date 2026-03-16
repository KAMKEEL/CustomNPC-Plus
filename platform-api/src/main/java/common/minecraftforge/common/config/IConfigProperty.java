package common.minecraftforge.common.config;

/**
 * Platform abstraction for Forge's Configuration property.
 * MC 1.7.10: net.minecraftforge.common.config.Property
 * MC 1.12+: net.minecraftforge.common.config.Property
 * MC 1.14+: ForgeConfigSpec.ConfigValue (adapter needed)
 */
public interface IConfigProperty {
    String getString();
    int getInt();
    boolean getBoolean();
    double getDouble();
    String[] getStringList();
    void set(String value);
    void set(int value);
    void set(boolean value);
    void set(double value);
    String getComment();
    String getName();
}
