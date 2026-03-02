package io.github.frostzie.nodex.styling.common

import atlantafx.base.controls.Message
import atlantafx.base.controls.Notification
import io.github.frostzie.nodex.styling.messages.NotificationPosition
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * A singleton overlay that manages the display of notifications and Messages in all four corners.
 */
object NotificationMessageArea : StackPane() {

    private val topLeftContainer = createContainer(Pos.TOP_LEFT)
    private val topRightContainer = createContainer(Pos.TOP_RIGHT)
    private val bottomLeftContainer = createContainer(Pos.BOTTOM_LEFT)
    private val bottomRightContainer = createContainer(Pos.BOTTOM_RIGHT)

    init {
        // Allow mouse events to pass through the empty parts of the overlay
        this.isPickOnBounds = false
        this.styleClass.add("notification-overlay")

        children.addAll(topLeftContainer, topRightContainer, bottomLeftContainer, bottomRightContainer)
    }

    /**
     * Creates a [VBox] container for a specific corner of the screen.
     *
     * @param alignment The position ([Pos]) where the container will be aligned.
     * @return A configured [VBox] ready to hold notifications.
     */
    private fun createContainer(alignment: Pos): VBox {
        val box = VBox(10.0) // 10px spacing
        box.isPickOnBounds = false
        box.styleClass.add("notification-container")
        box.padding = Insets(50.0, 10.0, 30.0, 50.0)

        box.maxHeight = 400.0
        box.maxWidth = 400.0

        box.alignment = alignment
        setAlignment(box, alignment)
        return box
    }

    /**
     * Shows a message in the overlay.
     * @param message The styled Message control.
     * @param position The corner to display the message in.
     * @param durationMillis Duration in milliseconds to show. -1 for indefinite.
     * @param maxMessages The maximum number of messages allowed in the container before old ones are removed.
     */
    fun show(message: Message, position: NotificationPosition, durationMillis: Long = 5000, maxMessages: Int = 5) {
        // Ensure UI updates happen on JavaFX thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater { show(message, position, durationMillis, maxMessages) }
            return
        }

        val container = getContainer(position)

        // Enforce container size limits on the message
        if (container.maxWidth > 0) {
            if (message.minWidth > container.maxWidth) message.minWidth = container.maxWidth
            if (message.prefWidth > container.maxWidth) message.prefWidth = container.maxWidth
            if (message.maxWidth > container.maxWidth) message.maxWidth = container.maxWidth
        }

        if (container.maxHeight > 0) {
            if (message.minHeight > container.maxHeight) message.minHeight = container.maxHeight
            if (message.prefHeight > container.maxHeight) message.prefHeight = container.maxHeight
            if (message.maxHeight > container.maxHeight) message.maxHeight = container.maxHeight
        }

        // Manage Capacity (Count)
        manageCapacity(container, position, maxMessages)

        // Manage Capacity (Height/Bounds)
        manageBounds(container, message, position)

        // Add to container
        // Top: Newest on top.
        // Bottom: Newest on bottom.

        if (position == NotificationPosition.TOP_LEFT || position == NotificationPosition.TOP_RIGHT) {
             container.children.add(0, message)
        } else {
             container.children.add(message) // Adds to bottom
        }

        message.setOnClose {
            closeMessage(container, message)
        }

        if (durationMillis > 0) {
            Timer().schedule(durationMillis) {
                Platform.runLater {
                    if (container.children.contains(message)) {
                        closeMessage(container, message)
                    }
                }
            }
        }
    }

    /**
     * Shows a notification in the overlay.
     */
    fun show(notification: Notification, position: NotificationPosition, durationMillis: Long = 5000, maxMessages: Int = 5) {
        // Ensure UI updates happen on JavaFX thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater { show(notification, position, durationMillis, maxMessages) }
            return
        }

        val container = getContainer(position)

        // Enforce container size limits on the notification
        if (container.maxWidth > 0) {
            if (notification.minWidth > container.maxWidth) notification.minWidth = container.maxWidth
            if (notification.prefWidth > container.maxWidth) notification.prefWidth = container.maxWidth
            if (notification.maxWidth > container.maxWidth) notification.maxWidth = container.maxWidth
        }

        if (container.maxHeight > 0) {
            if (notification.minHeight > container.maxHeight) notification.minHeight = container.maxHeight
            if (notification.prefHeight > container.maxHeight) notification.prefHeight = container.maxHeight
            if (notification.maxHeight > container.maxHeight) notification.maxHeight = container.maxHeight
        }

        // Manage Capacity (Count)
        manageCapacity(container, position, maxMessages)

        // Manage Capacity (Height/Bounds)
        manageBounds(container, notification, position)

        // Add to container
        if (position == NotificationPosition.TOP_LEFT || position == NotificationPosition.TOP_RIGHT) {
            container.children.add(0, notification)
        } else {
            container.children.add(notification)
        }

        notification.setOnClose {
            closeMessage(container, notification)
        }

        if (durationMillis > 0) {
            Timer().schedule(durationMillis) {
                Platform.runLater {
                    if (container.children.contains(notification)) {
                        closeMessage(container, notification)
                    }
                }
            }
        }
    }

    private fun getContainer(position: NotificationPosition): VBox = when (position) {
        NotificationPosition.TOP_LEFT -> topLeftContainer
        NotificationPosition.TOP_RIGHT -> topRightContainer
        NotificationPosition.BOTTOM_LEFT -> bottomLeftContainer
        NotificationPosition.BOTTOM_RIGHT -> bottomRightContainer
    }

    /**
     * Manages the number of messages in a container, removing the oldest ones if the `maxMessages` limit is exceeded.
     *
     * @param container The [VBox] holding the messages.
     * @param position The corner position, used to determine which message is "oldest".
     * @param maxMessages The maximum number of messages allowed.
     */
    private fun manageCapacity(container: VBox, position: NotificationPosition, maxMessages: Int) {
        while (container.children.size >= maxMessages) {
            removeOldest(container, position)
        }
    }

    /**
     * Manages the total height of messages in a container, removing old messages if adding a new one
     * would exceed the container's `maxHeight`.
     *
     * @param container The [VBox] holding the messages.
     * @param newMessage The new [Node] being added.
     * @param position The corner position, used to determine which message is "oldest".
     */
    private fun manageBounds(container: VBox, newMessage: Node, position: NotificationPosition) {
        val maxHeight = container.maxHeight
        if (maxHeight <= 0) return // No limit

        val spacing = container.spacing
        val newHeight = getEstimatedHeight(newMessage)

        // Calculate current height estimation
        var currentHeight = container.children.sumOf { getEstimatedHeight(it) }
        if (container.children.isNotEmpty()) currentHeight += (container.children.size - 1) * spacing

        var neededSpace = newHeight
        if (container.children.isNotEmpty()) neededSpace += spacing

        // If adding this message exceeds max height, remove old ones
        while (currentHeight + neededSpace > maxHeight && container.children.isNotEmpty()) {
            removeOldest(container, position)

            // Recalculate height
            currentHeight = container.children.sumOf { getEstimatedHeight(it) }
            if (container.children.isNotEmpty()) currentHeight += (container.children.size - 1) * spacing
        }
    }

    /**
     * Removes the oldest message from a container.
     * For top-aligned containers, the oldest is at the bottom.
     * For bottom-aligned containers, the oldest is at the top.
     *
     * @param container The [VBox] holding the messages.
     * @param position The corner position to determine the removal order.
     */
    private fun removeOldest(container: VBox, position: NotificationPosition) {
        val isTop = (position == NotificationPosition.TOP_LEFT || position == NotificationPosition.TOP_RIGHT)

        // Top: Oldest at end
        // Bottom: Oldest at start
        val index = if (isTop) container.children.size - 1 else 0
        if (index in container.children.indices) {
            closeMessage(container, container.children[index])
        }
    }

    /**
     * Estimates the height of a [Node]. It prioritizes `prefHeight` if available.
     *
     * @param node The node to measure.
     * @return The estimated height of the node.
     */
    private fun getEstimatedHeight(node: Node): Double {
        if (node is Region && node.prefHeight != USE_COMPUTED_SIZE) {
            return node.prefHeight
        }
        return node.prefHeight(-1.0)
    }

    /**
     * Removes a message from its container.
     *
     * @param container The parent [VBox].
     * @param node The [Node] to remove.
     */
    private fun closeMessage(container: VBox, node: Node) {
        // TODO: Fade out animation
        container.children.remove(node)
    }
}