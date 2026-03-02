package io.github.frostzie.nodex.screen.elements.main

import atlantafx.base.theme.Tweaks
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.OpenFile
import io.github.frostzie.nodex.events.RequestMoveConfirmation
import io.github.frostzie.nodex.modules.main.FileTreeViewModel
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.settings.categories.MainConfig
import io.github.frostzie.nodex.settings.categories.ThemeConfig
import io.github.frostzie.nodex.utils.UIConstants
import javafx.beans.InvalidationListener
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.nio.file.Path
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import kotlin.io.path.isDirectory

/**
 * The View for the file tree. This class is responsible for displaying the tree.
 */
class FileTreeView : VBox() {
    internal val viewModel = FileTreeViewModel()
    private val treeView = TreeView<FileTreeItem>()

    // A custom DataFormat used to identify drag-and-drop operations initiated from this file tree.
    // This ensures that the tree only handles drops that it originated, preventing conflicts with
    // other drag-and-drop sources. The string is a unique identifier, conventionally using a package name format.
    private val dragDataFormat = DataFormat("io.github.frostzie.nodex.FileTreeItem")

    init {
        styleClass.add("file-tree-container")

        prefWidth = UIConstants.FILE_TREE_DEFAULT_WIDTH
        minWidth = UIConstants.FILE_TREE_MIN_WIDTH
        maxWidth = UIConstants.FILE_TREE_MAX_WIDTH
        setVgrow(treeView, Priority.ALWAYS)
        children.add(treeView)

        visibleProperty().bind(viewModel.isVisible)
        managedProperty().bind(viewModel.isVisible)

        treeView.rootProperty().bind(viewModel.root)
        treeView.styleClass.add(Tweaks.EDGE_TO_EDGE)
        treeView.styleClass.add(Tweaks.ALT_ICON)

        MainConfig.showFileIcons.addListener { _ -> treeView.refresh() }

        treeView.isShowRoot = false

        // A cell factory is used to customize each cell in the tree. This includes setting up
        // mouse click listeners for opening files and handling all drag-and-drop gestures.
        treeView.setCellFactory { _ ->
            object : TreeCell<FileTreeItem>() {
                private val invalidationListener = InvalidationListener { updateStyle() }

                init {
                    WorkspaceManager.dirtyFiles.addListener(invalidationListener)
                    MainConfig.dirtyFileColor.addListener(invalidationListener)

                    setOnMouseClicked { event ->
                        if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                            val currentItem = item ?: return@setOnMouseClicked
                            if (!currentItem.path.isDirectory()) {
                                EventBus.post(OpenFile(currentItem.path))
                            }
                        }
                    }

                    setOnDragDetected { event ->
                        if (item == null) return@setOnDragDetected
                        val db = startDragAndDrop(TransferMode.MOVE)
                        val content = ClipboardContent()
                        content[dragDataFormat] = item.path.toString()
                        db.setContent(content)
                        event.consume()
                    }

                    setOnDragOver { event ->
                        if (isValidDropTarget(event)) {
                            event.acceptTransferModes(TransferMode.MOVE)
                        }
                        event.consume()
                    }

                    setOnDragEntered { event ->
                        if (isValidDropTarget(event)) {
                            styleClass.add("drag-over")
                        }
                    }

                    setOnDragExited {
                        styleClass.remove("drag-over")
                    }

                    setOnDragDropped { event ->
                        styleClass.remove("drag-over")
                        val db = event.dragboard
                        var success = false
                        if (db.hasContent(dragDataFormat)) {
                            val targetItem = item ?: return@setOnDragDropped
                            val sourcePath = Path.of(db.getContent(dragDataFormat) as String)
                            val targetPath = targetItem.path.resolve(sourcePath.fileName)

                            EventBus.post(RequestMoveConfirmation(sourcePath, targetPath))
                            success = true
                        }
                        event.isDropCompleted = success
                        event.consume()
                    }
                }

                override fun updateItem(item: FileTreeItem?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (empty || item == null) {
                        text = null
                        graphic = null
                        style = "" // Reset style
                    } else {
                        text = item.toString()
                        graphic = if (MainConfig.showFileIcons.get()) {
                            val iconCode = if (item.path.isDirectory()) Material2AL.FOLDER else Material2AL.DESCRIPTION
                            FontIcon(iconCode).apply { iconSize = ThemeConfig.fontSize.get() }
                        } else {
                            null
                        }
                        updateStyle()
                    }
                }

                private fun updateStyle() {
                    if (isEmpty || item == null) {
                        style = ""
                        return
                    }

                    style = if (item.path in WorkspaceManager.dirtyFiles) {
                        "-fx-text-fill: ${MainConfig.dirtyFileColor.get()};"
                    } else {
                        "" // Reset to default
                    }
                }

                private fun isValidDropTarget(event: DragEvent): Boolean {
                    if (event.gestureSource == this || !event.dragboard.hasContent(dragDataFormat)) {
                        return false
                    }

                    val targetItem = item ?: return false
                    val sourcePath = Path.of(event.dragboard.getContent(dragDataFormat) as String)

                    // Valid if the target is a directory, not the source itself, and not a child of the source
                    return targetItem.path.isDirectory() && sourcePath != targetItem.path && !targetItem.path.startsWith(sourcePath)
                }
            }
        }
    }
}