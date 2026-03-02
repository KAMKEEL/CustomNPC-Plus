package io.github.frostzie.nodex.modules.popup.file

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.FileOverrideConfirmed
import io.github.frostzie.nodex.events.MoveFile
import javafx.beans.property.SimpleStringProperty
import java.nio.file.Path

class FileOverrideDialogViewModel(
    private val sourcePath: Path,
    private val targetPath: Path
) {
    val headerText = SimpleStringProperty("Confirm Overwrite")
    val contentText = SimpleStringProperty()

    init {
        val fileName = targetPath.fileName?.toString() ?: targetPath.toString()
        contentText.set("The destination already has a file named \"$fileName\".\nDo you want to replace it?")
    }

    fun confirm() {
        EventBus.post(FileOverrideConfirmed(sourcePath, targetPath))
        EventBus.post(MoveFile(sourcePath, targetPath))
    }
}