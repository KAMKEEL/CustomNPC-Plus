package io.github.frostzie.nodex.styling.notifications.rules

import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.styling.common.StylePriority
import io.github.frostzie.nodex.styling.messages.MessageSeverity
import io.github.frostzie.nodex.styling.notifications.NotificationContext
import io.github.frostzie.nodex.styling.notifications.NotificationStyle
import io.github.frostzie.nodex.styling.notifications.NotificationStyleRule

/**
 * The base rule that applies to all notifications.
 * Adds ELEVATED_1 by default as per standard styling.
 */
class DefaultNotificationRule : NotificationStyleRule {
    override val priority: Int = StylePriority.BASE

    override fun appliesTo(context: NotificationContext): Boolean = true

    override fun getStyle(context: NotificationContext): NotificationStyle {
        return NotificationStyle(
            styleClasses = setOf(Styles.ELEVATED_1),
            iconSource = null
        )
    }
}

/**
 * Applies a custom icon if one is provided in the context.
 */
class CustomIconNotificationRule : NotificationStyleRule {
    override val priority: Int = StylePriority.SPECIFIC_RULES

    override fun appliesTo(context: NotificationContext): Boolean {
        return context.customIcon != null
    }

    override fun getStyle(context: NotificationContext): NotificationStyle {
        return NotificationStyle(iconSource = context.customIcon)
    }
}

/**
 * Applies styles based on the notification severity.
 */
class SeverityNotificationRule : NotificationStyleRule {
    override val priority: Int = StylePriority.DEFAULT_RULES

    override fun appliesTo(context: NotificationContext): Boolean = true

    override fun getStyle(context: NotificationContext): NotificationStyle {
        return when (context.severity) {
            MessageSeverity.INFO -> NotificationStyle(styleClasses = setOf(Styles.ACCENT))
            MessageSeverity.SUCCESS -> NotificationStyle(styleClasses = setOf(Styles.SUCCESS))
            MessageSeverity.WARNING -> NotificationStyle(styleClasses = setOf(Styles.WARNING))
            MessageSeverity.DANGER -> NotificationStyle(styleClasses = setOf(Styles.DANGER))
            MessageSeverity.REGULAR -> NotificationStyle()
        }
    }
}