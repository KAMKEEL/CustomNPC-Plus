package io.github.frostzie.nodex.styling.messages

import io.github.frostzie.nodex.styling.common.DynamicStyle
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.common.StyleRule

/**
 * Defines the context/data available to rules when styling a message.
 * Plugins can inspect this to decide if they should apply their style.
 *
 * @property title The header text of the message.
 * @property description The satisfied text of the message.
 * @property severity The semantic type of the message (Info, Warning, etc.).
 * @property customIcon Custom icon.
 */
data class MessageContext(
    val title: String,
    val description: String,
    val severity: MessageSeverity = MessageSeverity.REGULAR,
    val customIcon: IconSource? = null
)

/**
 * Represents the semantic severity of a message.
 */
enum class MessageSeverity {
    REGULAR,
    INFO,
    SUCCESS,
    WARNING,
    DANGER
}

/**
 * Defines where notifications should appear on the screen.
 */
enum class NotificationPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

/**
 * A data class representing the visual style properties of a Message.
 *
 * @property styleClasses List of CSS style classes to add (e.g., "accent", "success").
 * @property iconSource The icon to display.
 */
data class MessageStyle(
    val styleClasses: Set<String> = emptySet(),
    val iconSource: IconSource? = null,
) : DynamicStyle

/**
 * A typealias for a [StyleRule] specifically for styling messages.
 */
typealias MessageStyleRule = StyleRule<MessageContext, MessageStyle>