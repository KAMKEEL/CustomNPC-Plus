package io.github.frostzie.nodex.styling.notifications

import io.github.frostzie.nodex.styling.common.DynamicStyle
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.common.StyleRule
import io.github.frostzie.nodex.styling.messages.MessageSeverity

/**
 * Defines the context/data available to rules when styling a notification.
 */
data class NotificationContext(
    val text: String,
    val severity: MessageSeverity = MessageSeverity.REGULAR,
    val customIcon: IconSource? = null
)

/**
 * A data class representing the visual style properties of a Notification.
 *
 * @property styleClasses List of CSS style classes to add (e.g., "accent", "success").
 * @property iconSource The icon to display.
 */
data class NotificationStyle(
    val styleClasses: Set<String> = emptySet(),
    val iconSource: IconSource? = null,
) : DynamicStyle

/**
 * A typealias for a [StyleRule] specifically for styling notifications.
 */
typealias NotificationStyleRule = StyleRule<NotificationContext, NotificationStyle>