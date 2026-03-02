package io.github.frostzie.nodex.styling.messages.appliers

import atlantafx.base.controls.Message
import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.messages.MessageStyle
import io.github.frostzie.nodex.utils.LoggerProvider
import org.kordamp.ikonli.javafx.FontIcon

import javafx.css.PseudoClass

/**
 * Applies a [MessageStyle] to an AtlantaFX [Message] control.
 */
object MessageStyleApplier {

    private val logger = LoggerProvider.getLogger("MessageStyleApplier")

    private val ACCENT = PseudoClass.getPseudoClass("accent")
    private val SUCCESS = PseudoClass.getPseudoClass("success")
    private val WARNING = PseudoClass.getPseudoClass("warning")
    private val DANGER = PseudoClass.getPseudoClass("danger")

    fun apply(message: Message, style: MessageStyle) {
        logger.debug("Applying style to message. Classes: {}, Icon: {}",
            style.styleClasses,
            style.iconSource
        )

        if (style.styleClasses.isNotEmpty()) {
            message.styleClass.addAll(style.styleClasses)
            
            // Also toggle pseudo-classes if the style class matches standard AtlantaFX names
            message.pseudoClassStateChanged(ACCENT, style.styleClasses.contains(Styles.ACCENT))
            message.pseudoClassStateChanged(SUCCESS, style.styleClasses.contains(Styles.SUCCESS))
            message.pseudoClassStateChanged(WARNING, style.styleClasses.contains(Styles.WARNING))
            message.pseudoClassStateChanged(DANGER, style.styleClasses.contains(Styles.DANGER))
        }

        // Apply Icon
        when (val icon = style.iconSource) {
            is IconSource.IkonIcon -> {
                val fontIcon = FontIcon(icon.ikon)
                fontIcon.iconSize = 24 // Ensure a visible size
                message.graphic = fontIcon
            }
            is IconSource.SvgIcon -> {
                // TODO: Implement SVG icon support
            }
            null -> {
                logger.debug("No icon source provided in style. Clearing graphic.")
                message.graphic = null
            }
        }
    }
}
