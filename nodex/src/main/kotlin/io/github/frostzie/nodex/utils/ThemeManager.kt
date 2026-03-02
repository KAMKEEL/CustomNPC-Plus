package io.github.frostzie.nodex.utils

import io.github.frostzie.nodex.config.ConfigManager
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.CloseSettingsEvent
import io.github.frostzie.nodex.events.OpenFile
import io.github.frostzie.nodex.events.OpenThemeEvent
import io.github.frostzie.nodex.events.ThemeChangeEvent
import io.github.frostzie.nodex.settings.categories.ThemeConfig.theme
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File
import java.nio.file.Files

/**
 * Manages application themes, including built-in and custom themes.
 * This object handles loading, importing, and applying themes.
 */
object ThemeManager {
    private val logger = LoggerProvider.getLogger("ThemeManager")
    val customThemesDir: File = ConfigManager.configDir.resolve("themes").toFile()
    val availableThemes: ObservableList<String> = FXCollections.observableArrayList()

    private val builtInThemes = listOf(
        "Primer Light",
        "Primer Dark",
        "Nord Light",
        "Nord Dark",
        "Cupertino Light",
        "Cupertino Dark",
        "Dracula"
    )

    init {
        loadThemes()
    }

    /**
     * Loads all available themes, both built-in and custom, into the [availableThemes] list.
     */
    private fun loadThemes() {
        availableThemes.clear()
        availableThemes.addAll(builtInThemes)

        if (!customThemesDir.exists()) {
            customThemesDir.mkdirs()
        }

        val customThemes = customThemesDir.listFiles { _, name -> name.endsWith(".css", ignoreCase = true) }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()

        availableThemes.addAll(customThemes)
    }

    /**
     * Opens a file chooser to allow the user to import a custom CSS theme file.
     * The selected file is copied to the [customThemesDir] and the theme is applied.
     *
     * @param owner The parent window for the file chooser dialog.
     */
    fun importTheme(owner: Window) {
        val fileChooser = FileChooser()
        fileChooser.title = "Select CSS Theme File"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("CSS files (*.css)", "*.css"))
        val selectedFile = fileChooser.showOpenDialog(owner)

        selectedFile?.let { file ->
            try {
                val destFile = customThemesDir.resolve(file.name)
                file.copyTo(destFile, overwrite = true)
                logger.debug("Copied theme file to ${destFile.absolutePath}")

                val newThemeName = destFile.nameWithoutExtension
                if (!availableThemes.contains(newThemeName)) {
                    availableThemes.add(newThemeName)
                }

                // Set the new theme as active in the config and trigger change event
                theme.set(newThemeName)
                EventBus.post(ThemeChangeEvent(newThemeName))

                Alert(Alert.AlertType.INFORMATION).apply {
                    title = "Theme Imported"
                    headerText = "Successfully imported '${file.name}'."
                    contentText = "The new theme has been applied."
                    initOwner(owner)
                    showAndWait()
                }

            } catch (e: Exception) {
                logger.error("Failed to import theme '${file.name}'", e)
                Alert(Alert.AlertType.ERROR).apply {
                    title = "Import Failed"
                    headerText = "Could not import '${file.name}'."
                    contentText = "An error occurred: ${e.message}"
                    initOwner(owner)
                    showAndWait()
                }
                // Revert to a default theme if something went wrong
                EventBus.post(ThemeChangeEvent("Primer Dark"))
            }
        }
    }

    /**
     * Handles the [OpenThemeEvent] to open the CSS file of the currently selected custom theme in the editor.
     * If the selected theme is a built-in theme, it shows a warning to the user.
     *
     * @param event The [OpenThemeEvent] that triggered this action.
     */
    fun openTheme(event: OpenThemeEvent) {
        val themeName = theme.get()
        val customThemePath = customThemesDir.toPath().resolve("$themeName.css")
        if (Files.exists(customThemePath)) {
            EventBus.post(CloseSettingsEvent())
            EventBus.post(OpenFile(customThemePath))
        } else {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.title = "Cannot Edit Theme"
            alert.headerText = "The selected theme '$themeName' is a built-in theme and cannot be edited."
            alert.showAndWait()
        }
    }
}
