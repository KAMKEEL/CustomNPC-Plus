package io.github.frostzie.nodex.modules.popup.file

import io.github.frostzie.nodex.screen.elements.popup.file.FileOverrideDialogView
import io.github.frostzie.nodex.screen.elements.popup.file.MoveConfirmationView
import javafx.application.Platform
import javafx.stage.Stage
import java.nio.file.Path

/**
 * A module responsible for creating and managing file-operation-related popups.
 *
 * This class acts as a factory for various file dialogs, [MoveConfirmationView]. (More to come)
 * It is responsible for creating the necessary ViewModels and Views, ensuring that the logic is
 * decoupled from the event handlers that trigger these dialogs.
 *
 * @param parentStage The main application stage, used as the owner for the popups.
 */
class FilePopupModule(private val parentStage: Stage) {
    /**
     * Creates a dialog to confirm a file or directory moval.
     *
     * This function sets up the [MoveConfirmationViewModel] with the initial paths
     * and then constructs and shows the [MoveConfirmationView].
     *
     * @param sourcePath The path of the file or directory being moved.
     * @param targetPath The initial proposed destination path for the item.
     */
    fun showMoveConfirmation(sourcePath: Path, targetPath: Path) {
        Platform.runLater {
            val viewModel = MoveConfirmationViewModel(sourcePath)
            viewModel.targetDirectory.set(targetPath.parent.toString())
            MoveConfirmationView(viewModel, parentStage).show()
        }
    }

    fun showFileOverrideDialog(sourcePath: Path, targetPath: Path) {
        Platform.runLater {
            val viewModel = FileOverrideDialogViewModel(sourcePath, targetPath)
            FileOverrideDialogView(viewModel, parentStage).show()
        }
    }
}
