package io.github.frostzie.nodex.utils.file

import io.github.frostzie.nodex.events.DirectorySelected
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.loader.minecraft.MCInterface
import io.github.frostzie.nodex.modules.universal.UniversalPackManager
import io.github.frostzie.nodex.settings.categories.MainConfig
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.stage.DirectoryChooser
import javafx.stage.Window
import java.nio.file.Path
import io.github.frostzie.nodex.project.validation.ProjectValidator
import io.github.frostzie.nodex.project.validation.ValidationResult
import io.github.frostzie.nodex.modules.project.preview.*
import io.github.frostzie.nodex.screen.elements.project.preview.ProjectPreviewView
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.TextInputDialog
import javafx.stage.FileChooser
import javafx.concurrent.Task
import kotlin.io.path.name

object DirectoryChooseUtils {
    private val logger = LoggerProvider.getLogger("DirectoryChooseUtils")

    /**
     * Shows the user to select a project (Folder or Zip), validates it, and fires DirectorySelected.
     */
    fun promptOpenProject(ownerWindow: Window?) {
        val directoryChooser = DirectoryChooser().apply {
            title = "Open Project Folder"
            try {
                val datapackPath = getDatapackPath()
                initialDirectory = if (isSingleplayer() && datapackPath != null) datapackPath.toFile() else getInstancePath()?.toFile()
            } catch (e: Exception) { logger.warn("Could not set initial directory", e) }
        }

        val selectedFile = directoryChooser.showDialog(ownerWindow)
        if (selectedFile != null) {
            handleSelection(selectedFile.toPath())
        }
    }
    
    /**
     * Shows the user to select a project Zip file, validates it, and fires DirectorySelected.
     */
    fun promptOpenZip(ownerWindow: Window?) {
        val fileChooser = FileChooser().apply {
            title = "Open Project Zip"
            extensionFilters.add(FileChooser.ExtensionFilter("Zip Files", "*.zip"))
            try {
                val datapackPath = getDatapackPath()
                initialDirectory = if (isSingleplayer() && datapackPath != null) datapackPath.toFile() else getInstancePath()?.toFile()
            } catch (e: Exception) { logger.warn("Could not set initial directory", e) }
        }

        val selectedFile = fileChooser.showOpenDialog(ownerWindow)
        if (selectedFile != null) {
            handleSelection(selectedFile.toPath())
        }
    }

    /**
     * Call this when a path is selected (from chooser or drag-drop).
     */
    fun handleSelection(path: Path) {
        val result = ProjectValidator.validate(path)
        
        val viewModel = when (result) {
            is ValidationResult.ValidSingle -> SingleProjectPreviewViewModel(result.path, result.metadata)
            is ValidationResult.ValidZip -> ZipProjectPreviewViewModel(result.path, result.metadata)
            is ValidationResult.ValidWorkspace -> WorkspaceProjectPreviewViewModel(result.root, result.projects)
            is ValidationResult.Invalid -> InvalidProjectPreviewViewModel(result.path, result.reason)
        }

        val dialog = ProjectPreviewView(viewModel)
        
        val button = dialog.showAndWait()
        if (button.isPresent && button.get().buttonData == ButtonBar.ButtonData.OK_DONE) {
            val task = object : Task<Path>() {
                override fun call(): Path {
                    return viewModel.onConfirm()
                }
            }

            task.setOnSucceeded {
                var finalPath = task.value

                // Universal Import Logic
                if (MainConfig.universalDatapackToggle.get() && !UniversalPackManager.isUniversalProject(finalPath)) {
                    val confirm = Alert(Alert.AlertType.CONFIRMATION).apply {
                        title = "Universal Import"
                        headerText = "Import to Universal Folder?"
                        contentText = "Do you want to import '${finalPath.name}' to the Universal Datapack folder?"
                        buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
                    }.showAndWait()

                    if (confirm.isPresent && confirm.get() == ButtonType.YES) {
                        try {
                            finalPath = handleUniversalImport(finalPath) ?: finalPath
                        } catch (e: Exception) {
                            logger.error("Failed to import project", e)
                            Alert(Alert.AlertType.ERROR).apply {
                                title = "Import Failed"
                                headerText = "Failed to import project"
                                contentText = e.message
                            }.showAndWait()
                        }
                    }
                }

                EventBus.post(DirectorySelected(finalPath))
            }
            
            task.setOnFailed {
                logger.error("Failed to open/import project", task.exception)
            }

            Thread(task).start() //TODO: Add loading indicator
        }
    }

    //TODO: Move UI out of here
    private fun handleUniversalImport(source: Path): Path? {
        val folderName = source.name

        if (UniversalPackManager.existsInUniversal(folderName)) {
            val renameBtn = ButtonType("Rename")
            val overwriteBtn = ButtonType("Overwrite")
            val cancelBtn = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

            val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
                title = "Conflict"
                headerText = "Project already exists"
                contentText = "A project named '$folderName' already exists in the Universal Folder."
                buttonTypes.setAll(renameBtn, overwriteBtn, cancelBtn)
            }

            val result = alert.showAndWait()
            if (result.isEmpty || result.get().buttonData == ButtonBar.ButtonData.CANCEL_CLOSE) {
                return null
            }

            if (result.get() == renameBtn) {
                val input = TextInputDialog("$folderName (1)").apply {
                    title = "Rename Project"
                    headerText = "Enter a new name for the project:"
                    graphic = null
                }
                val nameResult = input.showAndWait()
                if (nameResult.isPresent) {
                    return UniversalPackManager.importProject(source, nameResult.get())
                }
                return null
            }
        }

        return UniversalPackManager.importProject(source, folderName)
    }

    /**
     * Gets the instance folder path.
     */
    fun getInstancePath(): Path? {
        return MCInterface.getGamePath()
    }

    /**
     * Gets the datapack folder path for the current world.
     * Returns null if not in singleplayer.
     */
    fun getDatapackPath(): Path? {
        return MCInterface.getWorldPath()?.resolve("datapacks")
    }

    /**
     * Checks if the player is in singleplayer (integrated server).
     */
    fun isSingleplayer(): Boolean {
        return MCInterface.isSingleplayer
    }
}