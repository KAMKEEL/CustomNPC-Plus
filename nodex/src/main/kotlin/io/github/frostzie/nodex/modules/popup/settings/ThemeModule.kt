package io.github.frostzie.nodex.modules.popup.settings

import atlantafx.base.controls.Notification
import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.settings.categories.ThemeConfig
import io.github.frostzie.nodex.styling.messages.MessageSeverity
import io.github.frostzie.nodex.styling.notifications.NotificationFactory
import io.github.frostzie.nodex.utils.ThemeManager
import io.github.frostzie.nodex.utils.ThemeUtils
import javafx.scene.Scene
import javafx.scene.control.Button
import java.nio.file.Files
import java.nio.file.Path

class ThemeModule {
    val scenes = mutableSetOf<Scene>()
    private var editingNotification: Notification? = null
    private var editingSessionPath: Path? = null
    private var editingSessionTitle: String? = null

    fun changeTheme(themeName: String) {
        ThemeUtils.applyTheme(themeName)
    }

    fun reloadStyles() {
        changeTheme(ThemeConfig.theme.get())
    }

    fun openTheme(event: OpenThemeEvent) {
        ThemeManager.openTheme(event)

        val themeName = ThemeConfig.theme.get()
        val customThemePath = ThemeManager.customThemesDir.toPath().resolve("$themeName.css")

        if (Files.exists(customThemePath)) {
            startEditingSession("Theme: $themeName", customThemePath)
        }
    }

    fun closeEditingSession() {
        editingNotification?.let { NotificationFactory.hide(it) }
        editingNotification = null
        editingSessionPath = null
        editingSessionTitle = null
    }

    private fun startEditingSession(title: String, path: Path) {
        editingSessionTitle = title
        editingSessionPath = path
        showEditingNotification(title, path)
    }

    fun onActiveTabChanged(path: Path?) {
        if (editingSessionPath != null) {
            if (path == editingSessionPath) {
                if (editingNotification == null) {
                    showEditingNotification(editingSessionTitle ?: "", editingSessionPath!!)
                }
            } else {
                editingNotification?.let { NotificationFactory.hide(it) }
                editingNotification = null
            }
        }
    }

    private fun showEditingNotification(title: String, targetPath: Path) {
        editingNotification?.let { NotificationFactory.hide(it) }

        val btn = Button("Save & Refresh")
        btn.setOnAction {
            EventBus.post(SaveFileEvent(targetPath))
            EventBus.post(ReloadThemeEvent())
        }

        val notification = NotificationFactory.createAndShow(
            text = "Editing $title",
            severity = MessageSeverity.INFO,
            durationMillis = -1
        )
        notification.setPrimaryActions(btn)
        notification.setOnClose(null)
        editingNotification = notification
    }
}