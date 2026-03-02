package io.github.frostzie.nodex.styling.messages

import io.github.frostzie.nodex.styling.common.BaseStyleManager
import io.github.frostzie.nodex.utils.LoggerProvider

/**
 * Style manager for application messages/notifications.
 */
object MessageStyleManager : BaseStyleManager<MessageContext, MessageStyle>(
    emptyStyle = MessageStyle(),
    mergeStyles = { current, new ->
        current.copy(
            // Union of style classes to allow composition (e.g., "accent" + "rounded")
            styleClasses = current.styleClasses + new.styleClasses,
            // New icon overrides old icon
            iconSource = new.iconSource ?: current.iconSource
        )
    }
) {
    private val logger = LoggerProvider.getLogger("MessageStyleManager")

    override fun evaluate(context: MessageContext): MessageStyle {
        val result = super.evaluate(context)
        logger.debug("Evaluated message style for context: {} -> {}", context, result)
        return result
    }
}