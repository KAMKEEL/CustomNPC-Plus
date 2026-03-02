package io.github.frostzie.nodex.styling.notifications

import atlantafx.base.controls.Notification
import io.github.frostzie.nodex.features.FeatureRegistry
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.common.NotificationMessageArea
import io.github.frostzie.nodex.styling.messages.MessageSeverity
import io.github.frostzie.nodex.styling.messages.NotificationPosition
import io.github.frostzie.nodex.styling.notifications.appliers.NotificationStyleApplier
import javafx.scene.Node
import javafx.scene.layout.Pane
import org.kordamp.ikonli.javafx.FontIcon

object NotificationFactory {

    init {
        // Ensure FeatureRegistry is initialized to register default rules
        FeatureRegistry
    }

    /**
     * Creates a new [Notification] with styles applied dynamically based on the provided context.
     */
    fun create(
        text: String,
        severity: MessageSeverity = MessageSeverity.REGULAR,
        icon: IconSource? = null
    ): Notification {
        val context = NotificationContext(
            text = text,
            severity = severity,
            customIcon = icon
        )

        val style = NotificationStyleManager.evaluate(context)

        val graphicNode: Node? = when (val source = style.iconSource) {
            is IconSource.IkonIcon -> {
                val fontIcon = FontIcon(source.ikon)
                fontIcon.iconSize = 24
                fontIcon
            }
            is IconSource.SvgIcon -> { null } // TODO: Implement SVG icon support
            null -> null
        }

        val notification = if (graphicNode != null) Notification(text, graphicNode) else Notification(text)

        NotificationStyleApplier.apply(notification, style)

        return notification
    }

    /**
     * Creates and immediately shows a notification in the notification overlay.
     */
    fun createAndShow(
        text: String,
        severity: MessageSeverity = MessageSeverity.REGULAR,
        icon: IconSource? = null,
        position: NotificationPosition = NotificationPosition.BOTTOM_RIGHT,
        durationMillis: Long = 5000,
        width: Double? = null,
        height: Double? = null,
        maxMessages: Int = 5
    ): Notification {
        val notification = create(text, severity, icon)
        
        val finalWidth = width ?: 350.0
        notification.prefWidth = finalWidth
        notification.minWidth = finalWidth
        notification.maxWidth = finalWidth

        if (height != null) {
            val maxHeightLimit = 350.0
            val finalHeight = if (height > maxHeightLimit) maxHeightLimit else height

            notification.prefHeight = finalHeight
            notification.minHeight = finalHeight
            notification.maxHeight = finalHeight
        }

        show(notification, position, durationMillis, maxMessages)
        return notification
    }

    /**
     * Shows an existing notification in the notification overlay.
     */
    fun show(notification: Notification, position: NotificationPosition, durationMillis: Long = 5000, maxMessages: Int = 5) {
        NotificationMessageArea.show(notification, position, durationMillis, maxMessages)
    }

    /**
     * Hides an existing notification from the notification overlay.
     */
    fun hide(notification: Notification) {
        (notification.parent as? Pane)?.children?.remove(notification)
    }
}