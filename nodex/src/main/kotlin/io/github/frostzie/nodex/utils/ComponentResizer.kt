package io.github.frostzie.nodex.utils

import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region

/**
 * A utility to make a Region component horizontally resizable by dragging its right edge.
 */ //TODO: Add other edges for resizing
object ComponentResizer {

    /**
     * Installs resizing handlers on a given Region.
     *
     * @param region The Region to make resizable.
     * @param handleWidth The width of the sensitive area on the right edge for resizing.
     * @param minWidth The minimum width the region can be resized to.
     * @param maxWidth The maximum width the region can be resized to.
     */
    fun install(
        region: Region,
        handleWidth: Double = UIConstants.FILE_TREE_RESIZER_WIDTH,
        minWidth: Double,
        maxWidth: Double
    ) {
        var isResizing = false
        var resizeStartX = 0.0
        var resizeStartWidth = 0.0

        fun isOverResizeArea(x: Double): Boolean {
            return x >= region.width - handleWidth
        }

        region.addEventFilter(MouseEvent.MOUSE_MOVED) { event ->
            if (!event.isPrimaryButtonDown && isResizing) {
                isResizing = false
            }

            if (!isResizing) {
                region.cursor = if (isOverResizeArea(event.x)) Cursor.E_RESIZE else Cursor.DEFAULT
            }
        }

        region.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            if (isOverResizeArea(event.x)) {
                isResizing = true
                resizeStartX = event.screenX
                resizeStartWidth = if (region.prefWidth > 0) region.prefWidth else region.width
                region.cursor = Cursor.E_RESIZE
                event.consume()
            }
        }

        region.addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            if (isResizing) {
                val deltaX = event.screenX - resizeStartX
                var newWidth = resizeStartWidth + deltaX

                newWidth = newWidth.coerceIn(minWidth, maxWidth)

                region.prefWidth = newWidth
                event.consume()
            }
        }

        region.addEventHandler(MouseEvent.MOUSE_RELEASED) { event ->
            if (isResizing) {
                isResizing = false
                region.cursor = if (isOverResizeArea(event.x)) Cursor.E_RESIZE else Cursor.DEFAULT
                event.consume()
            }
        }

        region.addEventHandler(MouseEvent.MOUSE_EXITED) {
            if (!isResizing) {
                region.cursor = Cursor.DEFAULT
            }
        }
    }
}