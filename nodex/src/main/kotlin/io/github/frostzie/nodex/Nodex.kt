package io.github.frostzie.nodex

import io.github.frostzie.nodex.ingame.DefaultCommands
import io.github.frostzie.nodex.config.ConfigManager
import io.github.frostzie.nodex.loader.fabric.Keybinds
import io.github.frostzie.nodex.modules.universal.UniversalWorldIntegration
import io.github.frostzie.nodex.screen.MainApplication
import io.github.frostzie.nodex.settings.SettingsLoader
import io.github.frostzie.nodex.utils.JavaFXInitializer
import io.github.frostzie.nodex.utils.LoggerProvider
import net.fabricmc.api.ModInitializer

class Nodex : ModInitializer {
    private val logger = LoggerProvider.getLogger("Nodex")

    override fun onInitialize() {
        System.setProperty("javafx.allowSystemPropertiesAccess", "true")
        System.setProperty("javafx.platform", "desktop")

        if (JavaFXInitializer.isJavaFXAvailable()) {
            logger.info("Pre-initializing JavaFX platform...")
            MainApplication.initializeJavaFX()
        } else {
            logger.warn("JavaFX is not available - GUI features will be disabled")
        }

        ConfigManager.initialize() // Loads config file management and Layout system and Workspace data
        SettingsLoader.initialize() // Loads settings and their builder and universal logic
        DefaultCommands.registerCommands() // Loads commands
        Keybinds.register() // Loads Minecraft (Fabric) keybinds
        UniversalWorldIntegration.initialize() // Loads world detection for universal datapacks
    }
}