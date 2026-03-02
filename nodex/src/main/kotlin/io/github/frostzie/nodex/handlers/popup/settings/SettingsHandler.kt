package io.github.frostzie.nodex.handlers.popup.settings

import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.modules.popup.settings.SettingsModule
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent

@Suppress("unused")
class SettingsHandler(private val settingsModule: SettingsModule) {

    @SubscribeEvent
    fun onShowSettingsWindow(event: SettingsWindowOpen) {
        settingsModule.showSettingsWindow()
    }

    @SubscribeEvent
    fun onSettingsSearchQueryChanged(event: SettingsSearchQueryChanged) {
        settingsModule.search(event.query)
    }

    @SubscribeEvent
    fun onSettingsCategorySelected(event: SettingsCategorySelected) {
        settingsModule.selectCategory(event.item)
    }

    @SubscribeEvent
    fun onSettingsSearchResultSelected(event: SettingsSearchResultSelected) {
        settingsModule.selectSearchResult(event.result)
    }

    @SubscribeEvent
    fun onSettingsSave(event: SettingsSave) {
        settingsModule.saveSettings()
    }

    @SubscribeEvent
    fun onRequestSettingsCategories(event: RequestSettingsCategories) {
        settingsModule.loadAndSendCategories()
    }
    
    @SubscribeEvent
    fun onOpenProjectManager(event: OpenProjectManagerEvent) {
        settingsModule.close()
    }

    @SubscribeEvent
    fun onResetWorkspace(event: ResetWorkspaceEvent) {
        settingsModule.close()
    }
}