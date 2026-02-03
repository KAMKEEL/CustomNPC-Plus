package kamkeel.npcs.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Utility class for Bukkit integration detection and common operations.
 * Must be initialized after the server has fully started to ensure Bukkit plugins are loaded.
 */
public class BukkitUtil {
    private static final Logger logger = LogManager.getLogger(BukkitUtil.class);

    private static boolean initialized = false;
    private static boolean bukkitEnabled = false;

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
            // Make sure all necessary bukkit methods and classes are available.
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Class.forName("org.bukkit.Server");
            Class<?> servicesManagerClass =Class.forName("org.bukkit.plugin.ServicesManager");
            Class<?> registeredServiceProviderClass = Class.forName("org.bukkit.plugin.RegisteredServiceProvider");
            Class.forName("org.bukkit.OfflinePlayer");
            Class.forName("org.bukkit.entity.Player");

            bukkitClass.getMethod("getServer");
            bukkitClass.getMethod("getServicesManager");
            servicesManagerClass.getMethod("getRegistration", Class.class);
            registeredServiceProviderClass.getMethod("getProvider");
            bukkitClass.getMethod("getOfflinePlayer", String.class);
            bukkitClass.getMethod("getPlayer", String.class);
            bukkitClass.getMethod("getPluginManager");

            Class<?> pluginManagerClass = Class.forName("org.bukkit.plugin.PluginManager");
            pluginManagerClass.getMethod("isPluginEnabled", String.class);

            bukkitEnabled = true;
            logger.info("Bukkit integration enabled");

            // Initialize Vault after Bukkit is confirmed
            VaultUtil.init();

        } catch (ClassNotFoundException | NoClassDefFoundError e) {
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
        if (!isEnabled()) return false;

        try {
            return Bukkit.getPluginManager().isPluginEnabled(pluginName);
        } catch (Exception e) {
            logger.error("Error checking if plugin is enabled: {}", pluginName, e);
            return false;
        }
    }

    /**
     * @param pluginName name of the plugin to get.
     * @return {@linkplain org.bukkit.plugin.Plugin Plugin} object for the name. Cast to correct class manually.
     */
    public static Object getPlugin(String pluginName) {
        if (!isEnabled()) return null;

        try {
            return Bukkit.getPluginManager().getPlugin(pluginName);
        } catch (Exception e) {
            logger.error("Error getting plugin object: {}", pluginName, e);
            return null;
        }
    }

    /**
     * Gets a Bukkit OfflinePlayer by name.
     *
     * @param playerName the player name
     * @return the {@link org.bukkit.OfflinePlayer} object, or null if not available
     */
    @SuppressWarnings({"deprecation"})
    public static Object getOfflinePlayer(String playerName) {
        if (!isEnabled()) return null;

        try {
            return Bukkit.getOfflinePlayer(playerName);
        } catch (Exception e) {
            logger.error("Error getting OfflinePlayer: {}", playerName, e);
            return null;
        }
    }

    /**
     * Gets a Bukkit Player by name (online players only).
     *
     * @param playerName the player name
     * @return the {@link org.bukkit.entity.Player} object, or null if not online or not available
     */
    public static Object getPlayer(String playerName) {
        if (!isEnabled()) return null;

        try {
            return Bukkit.getPlayer(playerName);
        } catch (Exception e) {
            logger.error("Error getting Player: {}", playerName, e);
            return null;
        }
    }

    /**
     * Gets a service provider from Bukkit's ServicesManager.
     *
     * @param serviceClass the service class to get
     * @return the service provider, or null if not available
     */
    public static <T> T getServiceProvider(Class<T> serviceClass) {
        if (!isEnabled()) return null;

        try {
            RegisteredServiceProvider<T> registration = Bukkit.getServicesManager().getRegistration(serviceClass);
            if (registration != null) {
                return registration.getProvider();
            }
        } catch (Exception e) {
            logger.error("Error getting service provider: {}", serviceClass.getName(), e);
        }
        return null;
    }
}
