package io.github.frostzie.nodex.styling.notifications

import io.github.frostzie.nodex.styling.common.BaseStyleManager
import io.github.frostzie.nodex.styling.notifications.rules.CustomIconNotificationRule
import io.github.frostzie.nodex.styling.notifications.rules.DefaultNotificationRule
import io.github.frostzie.nodex.styling.notifications.rules.SeverityNotificationRule
import io.github.frostzie.nodex.utils.LoggerProvider

/**
 * Style manager for application notifications.
 */
object NotificationStyleManager : BaseStyleManager<NotificationContext, NotificationStyle>(
    emptyStyle = NotificationStyle(),
    mergeStyles = { current, new ->
        current.copy(
            styleClasses = current.styleClasses + new.styleClasses,
            iconSource = new.iconSource ?: current.iconSource
        )
    }
) {
    private val logger = LoggerProvider.getLogger("NotificationStyleManager")

    init {
        registerRule(DefaultNotificationRule())
        registerRule(SeverityNotificationRule())
        registerRule(CustomIconNotificationRule())
    }

    override fun evaluate(context: NotificationContext): NotificationStyle {
        val result = super.evaluate(context)
        logger.debug("Evaluated notification style for context: {} -> {}", context, result)
        return result
    }
}