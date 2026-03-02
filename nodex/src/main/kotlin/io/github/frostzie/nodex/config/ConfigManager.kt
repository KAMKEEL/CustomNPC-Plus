package io.github.frostzie.nodex.config

import com.google.gson.GsonBuilder
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.utils.LoggerProvider
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * The Main configuration manager for DataPack IDE
 * Handles initialization and management of all config subsystems
 */
object ConfigManager {

    private val logger = LoggerProvider.getLogger("ConfigManager")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    // The default configuration dir
    private val defaultConfigDir: Path = Folders.configDir.resolve("nodex")
    
    // File that determines the actual config location
    private val locationFile: Path = defaultConfigDir.resolve("config_location.json")

    // The active configuration directory
    private var _configDir: Path? = null
    val configDir: Path
        get() {
            if (_configDir == null) _configDir = resolveConfigDir()
            return _configDir!!
        }

    private data class LocationConfig(var customConfigPath: String? = null)

    private fun resolveConfigDir(): Path {
        // Ensure the default directory exists so we can read/write the locationFile
        if (!defaultConfigDir.exists()) {
            defaultConfigDir.toFile().mkdirs()
        }

        if (locationFile.exists()) {
            try {
                FileReader(locationFile.toFile()).use { reader ->
                    val config = gson.fromJson(reader, LocationConfig::class.java)
                    if (!config.customConfigPath.isNullOrBlank()) {
                        val customPath = Paths.get(config.customConfigPath!!)
                        logger.debug("Using custom config directory: {}", customPath)
                        return customPath
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to read config location file, falling back to default.", e)
            }
        }
        return defaultConfigDir
    }

    fun initialize() {
        // TODO: Enable migration when switching to "Nodex"
        // migrateLegacyConfig()

        if (!configDir.toFile().exists()) {
            configDir.toFile().mkdirs()
            logger.debug("Created config directory: {}", configDir)
        }

        LayoutManager.initialize()
        WorkspaceManager.initialize()
    }

    fun reload() {
        _configDir = resolveConfigDir()
        if (!configDir.toFile().exists()) {
            configDir.toFile().mkdirs()
        }
        logger.debug("Config directory reloaded: {}", configDir)
    }

    /**
     * Sets a custom configuration path and saves it to the locationFile.
     */
    fun setCustomConfigPath(path: Path?) {
        try {
            if (!defaultConfigDir.exists()) defaultConfigDir.toFile().mkdirs()
            
            val config = LocationConfig(path?.toAbsolutePath()?.toString())
            FileWriter(locationFile.toFile()).use { writer ->
                gson.toJson(config, writer)
            }
            logger.debug("Updated config location. New path: {}", path ?: "Default")
        } catch (e: Exception) {
            logger.error("Failed to save config location file.", e)
        }
    }

    private fun migrateLegacyConfig() {
        val legacyDir = Folders.configDir.resolve("datapack-ide")
        val newDir = Folders.configDir.resolve("Nodex")

        if (legacyDir.exists() && !newDir.exists()) {
            try {
                Files.move(legacyDir, newDir)
                logger.info("Migrated legacy config directory from 'datapack-ide' to 'Nodex'")
            } catch (e: Exception) {
                logger.error("Failed to migrate legacy config directory", e)
            }
        }
    }
}