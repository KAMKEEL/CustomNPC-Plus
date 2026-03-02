package io.github.frostzie.nodex.settings

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * A data class to represent a key combination in a structured way.
 *
 * @param key The primary, non-modifier key.
 * @param ctrl Whether the Control key is required.
 * @param alt Whether the Alt key is required.
 * @param shift Whether the Shift key is required.
 * @param meta Whether the Meta (e.g., Command on macOS) key is required.
 */
data class KeyCombination(
    val key: KeyCode,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false,
    val meta: Boolean = false
) {
    /**
     * Provides a human-readable string representation for display purposes.
     * Example: "Ctrl + Alt + S"
     */
    override fun toString(): String {
        val parts = mutableListOf<String>()
        if (ctrl) parts.add("Ctrl")
        if (alt) parts.add("Alt")
        if (shift) parts.add("Shift")
        if (meta) parts.add("Meta")
        parts.add(key.getName())
        return parts.joinToString(" + ")
    }

    companion object {
        /**
         * Creates a [KeyCombination] instance from a JavaFX [KeyEvent].
         */
        fun fromEvent(event: KeyEvent): KeyCombination {
            return KeyCombination(
                key = event.code,
                ctrl = event.isControlDown,
                alt = event.isAltDown,
                shift = event.isShiftDown,
                meta = event.isMetaDown
            )
        }
    }
}