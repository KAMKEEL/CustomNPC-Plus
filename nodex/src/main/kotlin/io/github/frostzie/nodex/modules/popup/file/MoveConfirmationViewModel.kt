package io.github.frostzie.nodex.modules.popup.file

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.MoveFile
import io.github.frostzie.nodex.events.RequestFileOverride
import javafx.beans.property.SimpleStringProperty
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isDirectory

class MoveConfirmationViewModel(
    private val sourcePath: Path
) {
    val sourcePathLabel = SimpleStringProperty()
    val targetDirectory = SimpleStringProperty()
    val error = SimpleStringProperty()

    init {
        sourcePathLabel.set(buildSourcePathLabel())
    }

    fun confirm(): Boolean {
        error.set(null)
        try {
            val targetDirValue = targetDirectory.get()?.takeIf { it.isNotBlank() } ?: run {
                error.set("Target directory cannot be empty")
                return false
            }
            val newTargetDirPath = Path.of(targetDirValue)
            if (Files.isDirectory(newTargetDirPath)) {
                val fileName = sourcePath.fileName ?: run {
                    error.set("Invalid file name")
                    return false
                }
                val newTargetPath = newTargetDirPath.resolve(fileName)
                if (Files.exists(newTargetPath)) {
                    EventBus.post(RequestFileOverride(sourcePath, newTargetPath))
                    return true
                }
                EventBus.post(MoveFile(sourcePath, newTargetPath))
                return true
            } else {
                //TODO: Add Error and Refactoring move.
                error.set("Invalid directory: Path does not exist or is not a directory.")
                return false
            }
        } catch (_: InvalidPathException) {
            error.set("Invalid path format.")
            return false
        }
    }

    private fun buildSourcePathLabel(): String {
        val label = if (sourcePath.isDirectory()) "Current directory:" else "Current file:"
        return "$label $sourcePath"
    }
}