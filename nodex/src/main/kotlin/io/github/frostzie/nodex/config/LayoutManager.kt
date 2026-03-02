package io.github.frostzie.nodex.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.UIConstants
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files

data class LayoutConfig(
    // Main IDE Window
    var x: Double = -1.0,
    var y: Double = -1.0,
    var width: Double = UIConstants.DEFAULT_WINDOW_WIDTH,
    var height: Double = UIConstants.DEFAULT_WINDOW_HEIGHT,
    
    // Project Manager Screen
    var projectManagerViewX: Double = -1.0,
    var projectManagerViewY: Double = -1.0,
    var projectManagerViewWidth: Double = 810.0,
    var projectManagerViewHeight: Double = 812.0
)

object LayoutManager {
    private val logger = LoggerProvider.getLogger("LayoutManager")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val layoutFile get() = ConfigManager.configDir.resolve("layout.json")

    var config = LayoutConfig()
        private set

    fun initialize() {
        load()
    }

    fun load() {
        if (Files.exists(layoutFile)) {
            try {
                FileReader(layoutFile.toFile()).use { reader ->
                    val loadedConfig = gson.fromJson(reader, LayoutConfig::class.java)
                    if (loadedConfig != null) {
                        config = loadedConfig
                    }
                    logger.debug("Loaded layout from {}", layoutFile)
                }
            } catch (e: Exception) {
                logger.error("Failed to load layout file, using defaults.", e)
                config = LayoutConfig()
            }
        } else {
            logger.info("Layout file not found, using defaults.")
            config = LayoutConfig()
        }
    }

    fun save() {
        try {
            FileWriter(layoutFile.toFile()).use { writer ->
                gson.toJson(config, writer)
                logger.debug("Saved layout to {}", layoutFile)
            }
        } catch (e: Exception) {
            logger.error("Failed to save layout file.", e)
        }
    }
}
