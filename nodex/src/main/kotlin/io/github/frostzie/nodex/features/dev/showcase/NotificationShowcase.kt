package io.github.frostzie.nodex.features.dev.showcase

import io.github.frostzie.nodex.settings.categories.ExampleConfig
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.messages.MessageSeverity
import io.github.frostzie.nodex.styling.messages.NotificationPosition
import io.github.frostzie.nodex.styling.notifications.NotificationFactory
import javafx.scene.control.Button
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.material2.Material2OutlinedMZ

object NotificationShowcase {

    fun showExampleNotification() {
        val text = ExampleConfig.notificationText.get()

        val severity = try {
            MessageSeverity.valueOf(ExampleConfig.notificationSeverity.get())
        } catch (e: Exception) {
            MessageSeverity.REGULAR
        }

        val position = try {
            NotificationPosition.valueOf(ExampleConfig.notificationPosition.get())
        } catch (e: Exception) {
            NotificationPosition.BOTTOM_RIGHT
        }

        val duration = ExampleConfig.notificationDuration.get().toLong()

        val widthVal = ExampleConfig.notificationWidth.get()
        val width = if (widthVal > 0.0) widthVal else null

        val heightVal = ExampleConfig.notificationHeight.get()
        val height = if (heightVal > 0.0) heightVal else null

        val iconName = ExampleConfig.notificationIcon.get()
        val icon = try {
            if (iconName.isNotBlank()) {
                IconSource.IkonIcon(Material2OutlinedMZ.valueOf(iconName.trim().uppercase()))
            } else null
        } catch (e: Exception) {
            null
        }

        val msg = NotificationFactory.createAndShow(
            text = text,
            severity = severity,
            position = position,
            durationMillis = duration,
            width = width,
            height = height,
            icon = icon
        )

        if (ExampleConfig.notificationShowActions.get()) {
            val yesBtn = Button("Yes")
            yesBtn.isDefaultButton = true

            val noBtn = Button("No")

            msg.setPrimaryActions(yesBtn, noBtn)
            msg.setSecondaryActions(
                MenuItem("Item 1"),
                MenuItem("Item 2")
            )
        }
    }
}