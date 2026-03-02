package io.github.frostzie.nodex.screen.elements.popup.file

import io.github.frostzie.nodex.modules.popup.file.FileOverrideDialogViewModel
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.stage.Stage

class FileOverrideDialogView(
    private val viewModel: FileOverrideDialogViewModel,
    private val parentStage: Stage?
) {
    fun show() {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "File Conflict"
        alert.headerText = viewModel.headerText.get()
        alert.contentText = viewModel.contentText.get()
        alert.initOwner(parentStage)

        val overrideButton = ButtonType("Override", ButtonBar.ButtonData.OK_DONE)
        val cancelButton = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

        alert.buttonTypes.setAll(overrideButton, cancelButton)
        alert.dialogPane.styleClass.add("file-override-dialog")

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == overrideButton) {
            viewModel.confirm()
        }
    }
}