package io.github.frostzie.nodex.utils

import atlantafx.base.theme.*
import io.github.frostzie.nodex.config.ConfigManager
import javafx.application.Application
import kotlin.io.path.exists

object ThemeUtils {

    fun applyTheme(themeName: String) {
        val themeStylesheetUrl = when (themeName) {
            "Primer Light" -> PrimerLight().userAgentStylesheet
            "Primer Dark" -> PrimerDark().userAgentStylesheet
            "Nord Light" -> NordLight().userAgentStylesheet
            "Nord Dark" -> NordDark().userAgentStylesheet
            "Cupertino Light" -> CupertinoLight().userAgentStylesheet
            "Cupertino Dark" -> CupertinoDark().userAgentStylesheet
            "Dracula" -> Dracula().userAgentStylesheet
            else -> {
                val customThemeFile = ConfigManager.configDir.resolve("themes").resolve("$themeName.css")
                if (customThemeFile.exists()) {
                    customThemeFile.toUri().toString()
                } else {
                    // Default to Primer Dark if custom theme not found
                    //TODO: Show notif of error
                    PrimerDark().userAgentStylesheet
                }
            }
        }
        Application.setUserAgentStylesheet(themeStylesheetUrl)
    }
}