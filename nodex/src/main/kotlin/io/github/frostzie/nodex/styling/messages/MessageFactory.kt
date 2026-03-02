package io.github.frostzie.nodex.styling.messages

import atlantafx.base.controls.Message
import io.github.frostzie.nodex.features.FeatureRegistry
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.messages.appliers.MessageStyleApplier
import io.github.frostzie.nodex.styling.common.NotificationMessageArea

object MessageFactory {

    init {
        // Ensure FeatureRegistry is initialized to register default rules
        FeatureRegistry
    }

    /**
     * Creates a new [Message] with styles applied dynamically based on the provided context.
     *
     * @param title The message title.
     * @param description The message body.
     * @param severity The severity of the message.
     * @param icon Custom icon.
     * @return A styled AtlantaFX Message control.
     */
    fun create(
        title: String,
        description: String,
        severity: MessageSeverity = MessageSeverity.REGULAR,
        icon: IconSource? = null
    ): Message {
        val message = Message(title, description)
        
        val context = MessageContext(
            title = title,
            description = description,
            severity = severity,
            customIcon = icon
        )

        val style = MessageStyleManager.evaluate(context)
        MessageStyleApplier.apply(message, style)

        return message
    }

    /**
     * Creates and immediately shows a message in the notification overlay.
     *
     * @param title The title for message.
     * @param description The description of message.
     * @param severity The severity of the message.
     * @param icon Custom icon.
     * @param position The corner to display the message in.
     * @param durationMillis Duration in milliseconds to show. -1 for indefinite.
     * @param width Optional fixed width for the message (Max 400).
     * @param height Optional fixed height for the message (Max 400).
     * @param maxMessages The maximum number of messages allowed in the container before old ones are removed.
     */
    fun createAndShow(
        title: String,
        description: String,
        severity: MessageSeverity = MessageSeverity.REGULAR,
        icon: IconSource? = null,
        position: NotificationPosition = NotificationPosition.BOTTOM_RIGHT,
        durationMillis: Long = 5000,
        width: Double? = null,
        height: Double? = null,
        maxMessages: Int = 5
    ): Message {
        val message = create(title, description, severity, icon)
        
        // Use provided width or default to 350.0
        val finalWidth = width ?: 350.0
        message.prefWidth = finalWidth
        message.minWidth = finalWidth
        message.maxWidth = finalWidth

        if (height != null) {
            val maxHeightLimit = 350.0
            val finalHeight = if (height > maxHeightLimit) maxHeightLimit else height

            message.prefHeight = finalHeight
            message.minHeight = finalHeight
            message.maxHeight = finalHeight
        }

        show(message, position, durationMillis, maxMessages)
        return message
    }

    /**
     * Shows an existing message in the notification overlay.
     */
    fun show(message: Message, position: NotificationPosition, durationMillis: Long = 5000, maxMessages: Int = 5) {
        NotificationMessageArea.show(message, position, durationMillis, maxMessages)
    }
}