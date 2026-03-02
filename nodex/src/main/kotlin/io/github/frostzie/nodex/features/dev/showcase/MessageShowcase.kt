package io.github.frostzie.nodex.features.dev.showcase

import io.github.frostzie.nodex.settings.categories.ExampleConfig
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.messages.MessageFactory
import io.github.frostzie.nodex.styling.messages.MessageSeverity
import io.github.frostzie.nodex.styling.messages.NotificationPosition
import org.kordamp.ikonli.material2.Material2OutlinedMZ

object MessageShowcase {

    fun showExampleMessage() {
        val title = ExampleConfig.messageTitle.get()
        val description = ExampleConfig.messageDescription.get()

        val severity = try {
            MessageSeverity.valueOf(ExampleConfig.messageSeverity.get())
        } catch (e: Exception) {
            MessageSeverity.REGULAR
        }

        val position = try {
            NotificationPosition.valueOf(ExampleConfig.messagePosition.get())
        } catch (e: Exception) {
            NotificationPosition.BOTTOM_RIGHT
        }

        val duration = ExampleConfig.messageDuration.get().toLong()

        val widthVal = ExampleConfig.messageWidth.get()
        val width = if (widthVal > 0.0) widthVal else null

        val heightVal = ExampleConfig.messageHeight.get()
        val height = if (heightVal > 0.0) heightVal else null

        val iconName = ExampleConfig.messageIcon.get()
        val icon = try {
            if (iconName.isNotBlank()) {
                IconSource.IkonIcon(Material2OutlinedMZ.valueOf(iconName.trim().uppercase()))
            } else null
        } catch (e: Exception) {
            null
        }

        MessageFactory.createAndShow(
            title = title,
            description = description,
            severity = severity,
            position = position,
            durationMillis = duration,
            width = width,
            height = height,
            icon = icon
        )
    }
}