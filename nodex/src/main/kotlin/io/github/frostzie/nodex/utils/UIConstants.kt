package io.github.frostzie.nodex.utils

/**
 * A central place for all UI dimension constants.
 * This helps in maintaining a consistent look and feel and makes it easy to adjust the layout.
 * Possible that this will no longer be kept and instead, the user could modify the sizes of each element.
 */
object UIConstants {

    // Main Window Layout
    const val TOP_BAR_HEIGHT = 40.0
    const val BOTTOM_BAR_HEIGHT = 24.0
    const val LEFT_BAR_WIDTH = 40.0
    const val MIN_CONTENT_WIDTH = 800.0
    const val MIN_CONTENT_HEIGHT = 600.0
    const val DEFAULT_WINDOW_WIDTH = 1200.0
    const val DEFAULT_WINDOW_HEIGHT = 800.0
    const val STAGE_BORDER_WIDTH = 4.0

    // File Tree View
    const val FILE_TREE_DEFAULT_WIDTH = 250.0
    const val FILE_TREE_MIN_WIDTH = 150.0
    const val FILE_TREE_MAX_WIDTH = 600.0
    const val FILE_TREE_RESIZER_WIDTH = 4.0
    const val FILE_TREE_NODE_HEIGHT = 22.0
    const val FILE_TREE_NODE_INDENT = 22.0
    const val FILE_TREE_SPACING = 1.0
    const val FILE_TREE_PADDING = 20.0
    const val FILE_TREE_CELL_SIZE = 25.0

    // Undecorated Window Resizing
    const val WINDOW_RESIZE_BORDER_DEPTH = 7.0
    const val WINDOW_SHADOW_INDENTATION = 0.0
    const val WINDOW_BORDER_WIDTH = 2.0

    // Settings Window
    const val SETTINGS_WIDTH = 900.0
    const val SETTINGS_HEIGHT = 700.0
    const val SETTINGS_MIN_WIDTH = 800.0
    const val SETTINGS_MIN_HEIGHT = 600.0
    const val SETTINGS_SIDE_PANEL_MIN_WIDTH = 180.0
    const val SETTINGS_SIDE_PANEL_MAX_WIDTH = 400.0

    // New File Window
    const val NEW_FILE_WINDOW_FIXED_CELL_SIZE = 28.0

    // Move Confirmation Dialog
    const val MOVE_CONFIRMATION_WIDTH = 700.0
    const val MOVE_CONFIRMATION_HEIGHT = 200.0

    // Top Bar
    const val TOP_BAR_BUTTON_SIZE = 30.0
}