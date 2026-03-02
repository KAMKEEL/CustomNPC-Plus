package io.github.frostzie.nodex.handlers.popup.settings

import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.modules.popup.settings.ThemeModule
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent

class ThemeHandler(private val themeModule: ThemeModule) {

    @SubscribeEvent
    fun onThemeChange(event: ThemeChangeEvent) {
        themeModule.changeTheme(event.themeName)
    }

    @SubscribeEvent
    fun onReloadTheme(event: ReloadThemeEvent) {
        themeModule.reloadStyles()
    }

    @SubscribeEvent
    fun onOpenTheme(event: OpenThemeEvent) {
        themeModule.openTheme(event)
    }

    @SubscribeEvent
    fun onThemeEditingSessionClosed(event: ThemeEditingSessionClosedEvent) {
        themeModule.closeEditingSession()
    }

    @SubscribeEvent
    fun onActiveTabChanged(event: ActiveTabChangedEvent) {
        themeModule.onActiveTabChanged(event.path)
    }
}
