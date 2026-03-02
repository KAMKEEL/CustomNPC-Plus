package io.github.frostzie.nodex.screen.elements.main

import java.nio.file.Path

/**
 * A simple data class representing a single item in the file tree.
 */
data class FileTreeItem(
    val path: Path,
    val displayName: String
) {
    /**
     * Overriding toString() provides a default, human-readable name
     * that the TreeView can display. Use the custom displayName here.
     */
    override fun toString(): String {
        return displayName
    }
}