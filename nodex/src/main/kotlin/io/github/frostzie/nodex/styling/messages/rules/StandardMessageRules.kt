package io.github.frostzie.nodex.styling.messages.rules

import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.styling.common.StylePriority
import io.github.frostzie.nodex.styling.messages.MessageContext
import io.github.frostzie.nodex.styling.messages.MessageSeverity
import io.github.frostzie.nodex.styling.messages.MessageStyle
import io.github.frostzie.nodex.styling.messages.MessageStyleRule

/**
 * The base rule that applies to all messages.
 */
class DefaultMessageRule : MessageStyleRule {
    override val priority: Int = StylePriority.BASE

    override fun appliesTo(context: MessageContext): Boolean = true

    override fun getStyle(context: MessageContext): MessageStyle {
        return MessageStyle( iconSource = null )
    }
}

/**
 * Applies a custom icon if one is provided in the context.
 */
class CustomIconRule : MessageStyleRule {
    override val priority: Int = StylePriority.SPECIFIC_RULES

    override fun appliesTo(context: MessageContext): Boolean {
        return context.customIcon != null
    }

    override fun getStyle(context: MessageContext): MessageStyle {
        return MessageStyle(iconSource = context.customIcon)
    }
}

/**
 * Applies styles based on the message severity (Info, Success, Warning, Danger).
 */
class SeverityMessageRule : MessageStyleRule {
    override val priority: Int = StylePriority.DEFAULT_RULES

    override fun appliesTo(context: MessageContext): Boolean {
        // Apply to all, so we can handle REGULAR explicitly if needed
        return true
    }

    override fun getStyle(context: MessageContext): MessageStyle {
        return when (context.severity) {
            MessageSeverity.INFO -> MessageStyle(
                styleClasses = setOf(Styles.ACCENT)
            )
            MessageSeverity.SUCCESS -> MessageStyle(
                styleClasses = setOf(Styles.SUCCESS)
            )
            MessageSeverity.WARNING -> MessageStyle(
                styleClasses = setOf(Styles.WARNING)
            )
            MessageSeverity.DANGER -> MessageStyle(
                styleClasses = setOf(Styles.DANGER)
            )
            MessageSeverity.REGULAR -> MessageStyle()
        }
    }
}