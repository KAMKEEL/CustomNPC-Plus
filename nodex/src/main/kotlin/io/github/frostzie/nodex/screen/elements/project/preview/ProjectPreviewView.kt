package io.github.frostzie.nodex.screen.elements.project.preview

import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.modules.project.preview.*
import io.github.frostzie.nodex.project.metadata.DatapackMetadata
import io.github.frostzie.nodex.utils.ProjectIconUtils
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.StageStyle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import java.nio.file.Path

class ProjectPreviewView(val viewModel: ProjectPreviewViewModel) : Dialog<ButtonType>() {

    init {
        initStyle(StageStyle.UNDECORATED)
        
        val content = when (viewModel) {
            is SingleProjectPreviewViewModel -> createValidView(viewModel.path, viewModel.metadata)
            is ZipProjectPreviewViewModel -> createZipView(viewModel)
            is WorkspaceProjectPreviewViewModel -> createWorkspaceView(viewModel)
            is InvalidProjectPreviewViewModel -> createInvalidView(viewModel)
        }

        dialogPane.content = content
        dialogPane.padding = Insets(20.0)
        dialogPane.style = "-fx-border-color: -color-border-default; -fx-border-width: 1px;"

        val openType = ButtonType(viewModel.confirmButtonText, ButtonBar.ButtonData.OK_DONE)
        val cancelType = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

        dialogPane.buttonTypes.addAll(openType, cancelType)

        val openBtn = dialogPane.lookupButton(openType) as? Button
        openBtn?.styleClass?.add(if (viewModel.isWarning) Styles.DANGER else Styles.ACCENT)

        if (viewModel is ZipProjectPreviewViewModel) {
            openBtn?.addEventFilter(ActionEvent.ACTION) { event ->
                if (viewModel.targetExists()) {
                    val folderName = viewModel.calculateTargetDir().fileName.toString()
                    val alert = Alert(Alert.AlertType.CONFIRMATION)
                    alert.initOwner(dialogPane.scene.window)
                    alert.title = "Folder Exists"
                    alert.headerText = "Folder '$folderName' already exists."
                    alert.contentText = "Do you want to overwrite it? This will delete the existing folder."

                    val res = alert.showAndWait()
                    if (res.isPresent && res.get() != ButtonType.OK) {
                        event.consume()
                    }
                }
            }
        }

        if (viewModel is WorkspaceProjectPreviewViewModel && viewModel.isZip) {
            openBtn?.addEventFilter(ActionEvent.ACTION) { event ->
                if (viewModel.targetExists()) {
                    val folderName = viewModel.calculateTargetDir().fileName.toString()
                    val alert = Alert(Alert.AlertType.CONFIRMATION)
                    alert.initOwner(dialogPane.scene.window)
                    alert.title = "Folder Exists"
                    alert.headerText = "Folder '$folderName' already exists."
                    alert.contentText = "Do you want to overwrite it? This will delete the existing folder."

                    val res = alert.showAndWait()
                    if (res.isPresent && res.get() != ButtonType.OK) {
                        event.consume()
                    }
                }
            }
        }

        if (viewModel is InvalidProjectPreviewViewModel && viewModel.isZip) {
            openBtn?.addEventFilter(ActionEvent.ACTION) { event ->
                if (viewModel.targetExists()) {
                    val folderName = viewModel.calculateTargetDir().fileName.toString()
                    val alert = Alert(Alert.AlertType.CONFIRMATION)
                    alert.initOwner(dialogPane.scene.window)
                    alert.title = "Folder Exists"
                    alert.headerText = "Folder '$folderName' already exists."
                    alert.contentText = "Do you want to overwrite it? This will delete the existing folder."

                    val res = alert.showAndWait()
                    if (res.isPresent && res.get() != ButtonType.OK) {
                        event.consume()
                    }
                }
            }
        }
    }

    private fun createValidView(path: Path, metadata: DatapackMetadata): VBox {
        val root = VBox(10.0)
        root.prefWidth = 450.0

        val infoBox = HBox(15.0)
        infoBox.alignment = Pos.CENTER_LEFT

        val iconNode = ProjectIconUtils.getIcon(path, 64.0)

        val textContainer = VBox(5.0)
        textContainer.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(textContainer, Priority.ALWAYS)

        val nameLabel = Label(path.fileName.toString()).apply { styleClass.add(Styles.TITLE_3) }
        textContainer.children.add(nameLabel)

        val fullDesc = metadata.description.trim()
        if (fullDesc.isNotBlank()) {
            val lines = fullDesc.split("\n")
            val header = lines[0].trim()
            val rest = lines.drop(1).joinToString(" ").trim()

            if (header.isNotBlank()) {
                textContainer.children.add(Label(header.take(60).let { if (it.length == 60) "$it..." else it }))
            }
            if (rest.isNotBlank()) {
                textContainer.children.add(Label(rest.take(60).let { if (it.length == 60) "$it..." else it }))
            }
            Tooltip.install(
                textContainer,
                Tooltip("${path.fileName}\n$fullDesc").apply { isWrapText = true; maxWidth = 400.0 }
            )
        }

        infoBox.children.addAll(iconNode, textContainer)
        root.children.add(infoBox)
        return root
    }

    private fun createZipView(vm: ZipProjectPreviewViewModel): VBox {
        val root = createValidView(vm.path, vm.metadata)
        
        root.children.add(createUnzipBox(vm.targetDirectory, vm.path.parent))
        return root
    }

    private fun createWorkspaceView(vm: WorkspaceProjectPreviewViewModel): VBox {
        val vbox = VBox(15.0).apply { alignment = Pos.CENTER; prefWidth = 400.0 }
        vbox.children.addAll(
            FontIcon(Material2AL.LAYERS).apply { iconSize = 64 },
            Label("Multi-Project Workspace").apply { styleClass.add(Styles.TITLE_3) },
            Label("Found ${vm.projects.size} datapacks in this folder.")
        )
        
        if (vm.isZip) {
            vbox.children.add(createUnzipBox(vm.targetDirectory, vm.root.parent))
        }
        
        return vbox
    }

    private fun createInvalidView(vm: InvalidProjectPreviewViewModel): VBox {
        val vbox = VBox(15.0).apply { alignment = Pos.CENTER; prefWidth = 400.0 }
        vbox.children.addAll(
            FontIcon(Material2AL.ERROR_OUTLINE).apply { iconSize = 64; styleClass.add(Styles.DANGER) },
            Label("Validation Failed").apply { styleClass.addAll(Styles.TITLE_3, Styles.DANGER) },
            Label(vm.reason).apply { isWrapText = true; alignment = Pos.CENTER },
            Label("This folder does not appear to be a valid Datapack.").apply { styleClass.add(Styles.TEXT_MUTED); isWrapText = true; alignment = Pos.CENTER }
        )
        
        if (vm.isZip) {
            vbox.children.add(createUnzipBox(vm.targetDirectory, vm.path.parent))
        }
        
        return vbox
    }

    private fun createUnzipBox(targetDirectory: SimpleObjectProperty<Path>, defaultParent: Path): VBox {
        val unzipBox = VBox(5.0)
        val label = Label("Unzip to:")
        label.styleClass.add(Styles.TEXT_SMALL)

        val pathField = TextField(targetDirectory.get()?.toString())
        pathField.isEditable = false

        targetDirectory.addListener { _, _, newVal -> pathField.text = newVal?.toString() }

        val browseBtn = Button(null, FontIcon(Material2AL.FOLDER_OPEN))
        browseBtn.setOnAction {
            val dc = DirectoryChooser()
            dc.title = "Select Unzip Location"
            dc.initialDirectory = targetDirectory.get()?.toFile() ?: defaultParent.toFile()
            val selected = dc.showDialog(dialogPane.scene.window)
            if (selected != null) {
                targetDirectory.set(selected.toPath())
            }
        }

        val row = HBox(5.0)
        row.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(pathField, Priority.ALWAYS)
        row.children.addAll(pathField, browseBtn)

        unzipBox.children.addAll(label, row)
        return unzipBox
    }
}