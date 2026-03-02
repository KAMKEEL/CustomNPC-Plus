package io.github.frostzie.nodex.screen.elements.popup.file

import io.github.frostzie.nodex.modules.popup.file.MoveConfirmationViewModel
import io.github.frostzie.nodex.utils.UIConstants
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage

//TODO: Make this actually look okay lol
class MoveConfirmationView(
    private val viewModel: MoveConfirmationViewModel,
    private val parentStage: Stage?
) {
    fun show() {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Confirm Move"
        dialog.initOwner(parentStage)

        dialog.dialogPane.prefWidth = UIConstants.MOVE_CONFIRMATION_WIDTH
        dialog.dialogPane.prefHeight = UIConstants.MOVE_CONFIRMATION_HEIGHT
        dialog.dialogPane.styleClass.add("move-confirmation-dialog")

        val sourceLabel = Label()
        sourceLabel.textProperty().bind(viewModel.sourcePathLabel)
        sourceLabel.textOverrun = OverrunStyle.CENTER_ELLIPSIS

        val targetDirTextField = TextField()
        targetDirTextField.textProperty().bindBidirectional(viewModel.targetDirectory)

        val targetDirHBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label("To directory:"),
                targetDirTextField
            )
            HBox.setHgrow(targetDirTextField, Priority.ALWAYS)
        }

        val content = VBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            children.addAll(
                sourceLabel,
                targetDirHBox
            )
        }
        dialog.dialogPane.content = content

        val confirmButton = ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE) // For making pressing Enter confirm
        val cancelButton = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE) // For making pressing Esc cancel
        dialog.dialogPane.buttonTypes.addAll(confirmButton, cancelButton)

        viewModel.error.addListener { _, _, newValue ->
            if (!newValue.isNullOrBlank()) {
                showError(newValue)
            }
        }

        dialog.setResultConverter { button ->
            if (button == confirmButton) {
                if (viewModel.confirm()) {
                    return@setResultConverter button
                } else {
                    return@setResultConverter null
                }
            }
            null
        }

        dialog.showAndWait()
    }

    // TODO: Improve / refactor instead of error in most cases
    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR).apply {
            title = "Invalid Path"
            headerText = null
            contentText = message
            initOwner(parentStage)
        }
        alert.showAndWait()
    }
}