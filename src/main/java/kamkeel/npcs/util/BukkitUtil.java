package kamkeel.npcs.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

/**
 * Utility class for Bukkit integration detection and common operations.
 * Must be initialized after the server has fully started to ensure Bukkit plugins are loaded.
 */
public class BukkitUtil {
    private static final Logger logger = LogManager.getLogger(BukkitUtil.class);

    private static boolean initialized = false;
    private static boolean bukkitEnabled = false;

    // Bukkit classes
    private static Class<?> bukkitClass;
    private static Class<?> serverClass;
    private static Class<?> servicesManagerClass;
    private static Class<?> registeredServiceProviderClass;
    private static Class<?> offlinePlayerClass;
    private static Class<?> playerClass;

    // Bukkit methods
    private static Method getServer;
    private static Method getServicesManager;
    private static Method getRegistration;
    private static Method getProvider;
    private static Method getOfflinePlayer;
    private static Method getPlayer;
    private static Method getPluginManager;
    private static Method isPluginEnabled;

    /**
     * Initializes Bukkit integration using reflection.
     * Should be called during FMLServerStartedEvent to ensure Bukkit is fully loaded.
     */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            // Load Bukkit classes
            bukkitClass = Class.forName("org.bukkit.Bukkit");
            serverClass = Class.forName("org.bukkit.Server");
            servicesManagerClass = Class.forName("org.bukkit.plugin.ServicesManager");
            registeredServiceProviderClass = Class.forName("org.bukkit.plugin.RegisteredServiceProvider");
            offlinePlayerClass = Class.forName("org.bukkit.OfflinePlayer");
            playerClass = Class.forName("org.bukkit.entity.Player");

            // Get Bukkit methods
            getServer = bukkitClass.getMethod("getServer");
            getServicesManager = bukkitClass.getMethod("getServicesManager");
            getRegistration = servicesManagerClass.getMethod("getRegistration", Class.class);
            getProvider = registeredServiceProviderClass.getMethod("getProvider");
            getOfflinePlayer = bukkitClass.getMethod("getOfflinePlayer", String.class);
            getPlayer = bukkitClass.getMethod("getPlayer", String.class);
            getPluginManager = bukkitClass.getMethod("getPluginManager");

            Class<?> pluginManagerClass = Class.forName("org.bukkit.plugin.PluginManager");
            isPluginEnabled = pluginManagerClass.getMethod("isPluginEnabled", String.class);

            bukkitEnabled = true;
            logger.info("Bukkit integration enabled");

            // Initialize Vault after Bukkit is confirmed
            VaultUtil.init();

        } catch (ClassNotFoundException e) {
            logger.debug("Bukkit not found, Bukkit integration disabled");
        } catch (NoSuchMethodException e) {
            logger.error("Bukkit API method not found", e);
        } catch (Exception e) {
            logger.error("Error initializing Bukkit integration", e);
        }
    }

    /**
     * @return true if Bukkit integration is enabled
     */
    public static boolean isEnabled() {
        return bukkitEnabled;
    }

    /**
     * @return true if Bukkit has been initialized (may or may not be enabled)
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Checks if a specific Bukkit plugin is enabled.
     *
     * @param pluginName the plugin name to check
     * @return true if the plugin is enabled
     */
    public static boolean isPluginEnabled(String pluginName) {
        if (!bukkitEnabled) return false;

        try {
            Object pluginManager = getPluginManager.invoke(null);
            return (Boolean) isPluginEnabled.invoke(pluginManager, pluginName);
        } catch (Exception e) {
            logger.error("Error checking if plugin is enabled: " + pluginName, e);
            return false;
        }
    }

    /**
     * Gets a Bukkit OfflinePlayer by name.
     *
     * @param playerName the player name
     * @return the OfflinePlayer object, or null if not available
     */
    public static Object getOfflinePlayer(String playerName) {
        if (!bukkitEnabled) return null;

        try {
            return getOfflinePlayer.invoke(null, playerName);
        } catch (Exception e) {
            logger.error("Error getting OfflinePlayer: " + playerName, e);
            return null;
        }
    }

    /**
     * Gets a Bukkit Player by name (online players only).
     *
     * @param playerName the player name
     * @return the Player object, or null if not online or not available
     */
    public static Object getPlayer(String playerName) {
        if (!bukkitEnabled) return null;

        try {
            return getPlayer.invoke(null, playerName);
        } catch (Exception e) {
            logger.error("Error getting Player: " + playerName, e);
            return null;
        }
    }

    /**
     * Gets a service provider from Bukkit's ServicesManager.
     *
     * @param serviceClass the service class to get
     * @return the service provider, or null if not available
     */
    public static Object getServiceProvider(Class<?> serviceClass) {
        if (!bukkitEnabled) return null;

        try {
            Object servicesManager = getServicesManager.invoke(null);
            Object registration = getRegistration.invoke(servicesManager, serviceClass);
            if (registration != null) {
                return getProvider.invoke(registration);
            }
        } catch (Exception e) {
            logger.error("Error getting service provider: " + serviceClass.getName(), e);
        }
        return null;
    }

    // Getters for classes (for use by other utils like VaultUtil)
    public static Class<?> getBukkitClass() {
        return bukkitClass;
    }

    public static Class<?> getServicesManagerClass() {
        return servicesManagerClass;
    }

    public static Class<?> getRegisteredServiceProviderClass() {
        return registeredServiceProviderClass;
    }

    public static Class<?> getOfflinePlayerClass() {
        return offlinePlayerClass;
    }

    public static Class<?> getPlayerClass() {
        return playerClass;
    }
}
