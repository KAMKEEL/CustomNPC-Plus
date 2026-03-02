package io.github.frostzie.nodex.settings

import io.github.frostzie.nodex.config.ConfigManager
import io.github.frostzie.nodex.config.LayoutManager
import io.github.frostzie.nodex.events.CloseSettingsEvent
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.OpenProjectManagerEvent
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.settings.categories.AdvancedConfig
import io.github.frostzie.nodex.settings.categories.ExampleConfig
import io.github.frostzie.nodex.settings.categories.MainConfig
import io.github.frostzie.nodex.settings.categories.MinecraftConfig
import io.github.frostzie.nodex.settings.categories.ThemeConfig
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object SettingsLoader {
    private val logger = LoggerProvider.getLogger("SettingsLoader")
    private var isReloading = false

    fun initialize() {
        SettingsManager.register("main", MainConfig::class)
        SettingsManager.register("theme", ThemeConfig::class)
        SettingsManager.register("minecraft", MinecraftConfig::class)
        SettingsManager.register("advanced", AdvancedConfig::class)
        SettingsManager.register("example", ExampleConfig::class)

        SettingsManager.initialize()

        setupUniversalConfigListeners()

        MainConfig.syncConfigFiles = { reloadSystem() }
    }


    // Start of universal config logic

    private fun setupUniversalConfigListeners() {
        // The listener that triggers when any Universal Folder setting changes (Toggle or Path).
        // Wrap it to check 'isReloading' to prevent infinite loops during system resets (when loadSettings() changes values).
        val listener = { 
            if (!isReloading) updateConfigLocation() 
        }

        // Attach the listener to the UI toggles and text field (Overall need a better system for detecting setting changes in future)
        MainConfig.universalFolderToggle.addListener { _, _, _ -> listener() }
        MainConfig.universalConfigToggle.addListener { _, _, _ -> listener() }
        MainConfig.universalFolderPath.addListener { _, _, _ -> listener() }
    }

    /**
     * Determine where the config should be based on the UI.
     * This updates the 'config_location.json' pointer file.
     * It handles creating the folder, copying files if empty, and triggering a system reload.
     */
    private fun updateConfigLocation() {
        val useUniversal = MainConfig.universalFolderToggle.get() && MainConfig.universalConfigToggle.get()
        val defaultPath = Folders.configDir.resolve("nodex").toAbsolutePath()
        val currentConfigPath = ConfigManager.configDir.toAbsolutePath()

        // Only switch to the custom path if BOTH the Master Toggle and Config Toggle are ON
        if (useUniversal) {
            val pathStr = MainConfig.universalFolderPath.get()
            if (!pathStr.isNullOrBlank()) {
                try {
                    val rootPath = Paths.get(pathStr)
                    val nodexPath = rootPath.resolve("Nodex").resolve("config").toAbsolutePath()

                    // Prevents unnecessary reloads if the user chooses the same path
                    if (currentConfigPath == nodexPath) {
                        return
                    }

                    var isEmpty = false

                    // Create the directory if it doesn't exist yet
                    if (!Files.exists(nodexPath)) {
                        Files.createDirectories(nodexPath)
                        isEmpty = true
                    } else {
                        // Check if the directory is empty
                        Files.list(nodexPath).use { stream ->
                            isEmpty = !stream.findAny().isPresent
                        }
                    }

                    // Only copy configs if the target folder is empty
                    if (isEmpty) {
                        copyConfigs(nodexPath)
                    }

                    // Update the pointer file and reload the system to use the new location
                    ConfigManager.setCustomConfigPath(nodexPath)
                    reloadSystem()
                } catch (e: Exception) {
                    logger.error("Failed to set custom config path.", e)
                }
            }
        } else {
            // If toggles are OFF, revert pointer to NULL (Default Dir)
            
            // Check if we are already in default.
            if (currentConfigPath == defaultPath) {
                return
            }

            ConfigManager.setCustomConfigPath(null)
            // Reload the system to go back to default
            reloadSystem()
        }
    }

    /**
     * Copies files from the Default directory to the target directory.
     */
    private fun copyConfigs(targetDir: Path) {
        val sourceDir = Folders.configDir.resolve("nodex")

        if (!Files.exists(sourceDir)) {
            return
        }

        try {
            Files.walk(sourceDir).use { stream ->
                stream.forEach { source ->
                    val destination = targetDir.resolve(sourceDir.relativize(source))
                    try {
                        if (Files.isDirectory(source)) {
                            if (!Files.exists(destination)) {
                                Files.createDirectories(destination)
                            }
                        } else {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to copy file: $source", e)
                    }
                }
            }
            logger.debug("Successfully moved configs to {}", targetDir)
        } catch (e: Exception) {
            logger.error("Failed to move configs.", e)
        }
    }

    /**
     * Hot-reloads the application state.
     * 1. Close UI/Projects
     * 2. Re-resolve the Config Directory
     * 3. Load settings from the new directory
     * 4. Sync the UI toggles to match reality
     */
    private fun reloadSystem() {
        // Prevent recursive calls if loadSettings() triggers listeners
        if (isReloading) return
        isReloading = true
        try {
            // 1. Close active components
            EventBus.post(CloseSettingsEvent()) // Close settings to not cause de-sync
            EventBus.post(OpenProjectManagerEvent()) // To not cause any possible de-sync, we simply save/close
            
            // 2. Tell ConfigManager to read config_location.json again
            ConfigManager.reload()
            
            // 3. Load all data from the NEW directory
            SettingsManager.loadSettings()
            LayoutManager.load()
            WorkspaceManager.load()

            // 4. Sync UI with Reality
            // Sets universal toggle to false when detected inside default configs to not run into a loop
            val defaultPath = Folders.configDir.resolve("nodex").toAbsolutePath()
            val currentPath = ConfigManager.configDir.toAbsolutePath()

            if (currentPath == defaultPath) {
                if (MainConfig.universalFolderToggle.get()) {
                    MainConfig.universalFolderToggle.set(false)
                    // Save immediately so the file on disk is corrected for the next restart
                    SettingsManager.saveSettings()
                }
            } else {
                // We are in Custom (Universal). Toggle MUST be True to reflect that we are using the custom path.
                if (!MainConfig.universalFolderToggle.get()) {
                    MainConfig.universalFolderToggle.set(true)
                    SettingsManager.saveSettings()
                }
            }
        } finally {
            // Release the lock so user interactions work again
            isReloading = false
        }
    }
}