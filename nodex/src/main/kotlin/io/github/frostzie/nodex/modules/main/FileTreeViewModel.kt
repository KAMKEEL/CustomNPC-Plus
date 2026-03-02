package io.github.frostzie.nodex.modules.main

import io.github.frostzie.nodex.events.DirectorySelected
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.FileMoved
import io.github.frostzie.nodex.events.MoveFile
import io.github.frostzie.nodex.events.ToggleFileTree
import io.github.frostzie.nodex.events.WorkspaceUpdated
import io.github.frostzie.nodex.project.Project
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.screen.elements.main.FileTreeItem
import io.github.frostzie.nodex.services.FileService
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.file.FileSystemUpdate
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import java.nio.file.Files
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

class FileTreeViewModel {
    private val logger = LoggerProvider.getLogger("FileTreeViewModel")
    
    // The invisible root of the TreeView. Its children are the Project roots.
    val root = SimpleObjectProperty<TreeItem<FileTreeItem>>()
    val isVisible = SimpleBooleanProperty(true)
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Track expanded paths for persistence
    private val expandedPaths = mutableSetOf<Path>()
    private var isRestoringExpansion = false

    init {
        val dummyRoot = TreeItem(FileTreeItem(Paths.get("Workspace"), "Workspace"))
        dummyRoot.isExpanded = true
        root.set(dummyRoot)

        EventBus.register(this)
        
        // Initial load if workspace is already ready
        updateWorkspace(WorkspaceManager.workspace.projects)
    }

    fun cleanup() {
        scope.cancel()
        EventBus.unregister(this)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onToggleFileTree(event: ToggleFileTree) {
        isVisible.set(!isVisible.get())
    }

    /**
     * Handles the "Open Folder" action.
     */
    @Suppress("unused")
    @SubscribeEvent
    fun onDirectorySelected(event: DirectorySelected) {
        logger.info("Opening single directory as project: ${event.directoryPath}")
        WorkspaceManager.openSingleProject(event.directoryPath)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onWorkspaceUpdated(event: WorkspaceUpdated) {
        updateWorkspace(event.workspace.projects)
    }

    private fun updateWorkspace(projects: List<Project>) {
        // Run on background thread to avoid blocking UI with IO
        scope.launch {
            // Load saved expansion state
            val state = WorkspaceManager.getCurrentState()
            isRestoringExpansion = true
            expandedPaths.clear()
            expandedPaths.addAll(state.expandedPaths)

            // 2. Update the UI Tree
            Platform.runLater {
                val rootNode = root.get()
                
                // Remove nodes that are no longer in the project list
                rootNode.children.removeIf { item ->
                    val path = item.value?.path
                    path != null && projects.none { it.path == path }
                }

                // Add or Update nodes
                projects.forEach { project ->
                    val existingNode = rootNode.children.find { it.value?.path == project.path }
                    if (existingNode == null) {
                        // Create new project node
                        val projectNode = TreeItem(FileTreeItem(project.path, project.name))
                        projectNode.isExpanded = true // Auto-expand project roots
                        
                        // Load children asynchronously
                        scope.launch {
                            val children = loadChildren(project.path)
                            Platform.runLater {
                                projectNode.children.setAll(children)
                                restoreExpandedPaths(projectNode) // Try to restore
                            }
                        }
                        rootNode.children.add(projectNode)
                        
                        // Add listener to project root itself
                        addExpansionListener(projectNode, project.path)
                    }
                }
                isRestoringExpansion = false
            }
        }
    }
    
    private fun restoreExpandedPaths(node: TreeItem<FileTreeItem>) {
        if (node.value == null) return
        
        // Check children
        node.children.forEach { child ->
            val childPath = child.value?.path
            if (childPath != null && childPath in expandedPaths) {
                child.isExpanded = true
                restoreExpandedPaths(child)
            }
        }
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onFileMoved(event: MoveFile) {
        logger.debug("Moving file from {}\nto {}", event.sourcePath, event.targetPath)
        try {
            WorkspaceManager.ignoreWatcherPath(event.sourcePath)
            WorkspaceManager.ignoreWatcherPath(event.targetPath)

            FileService.move(event.sourcePath, event.targetPath)
            
            EventBus.post(FileMoved(event.sourcePath, event.targetPath))

            refreshNode(event.sourcePath.parent)
            refreshNode(event.targetPath.parent)

        } catch (e: Exception) {
            logger.error("Failed to move file: ${event.sourcePath}", e)
        }
    }

    @SubscribeEvent
    fun onFileSystemUpdate(event: FileSystemUpdate) {
        refreshNode(event.path)
    }

    private fun refreshNode(path: Path?) {
        if (path == null) return

        // If the path to refresh doesn't exist (it was just renamed/deleted)
        // refresh its parent to update the tree structure.
        if (!FileService.isDirectory(path) && !Files.exists(path)) {
            refreshNode(path.parent)
            return
        }

        Platform.runLater {
            var currentPath = path
            var targetNode: TreeItem<FileTreeItem>? = null

            // Find the closest existing ancestor node
            while (currentPath != null && targetNode == null) {
                targetNode = findNode(root.get(), currentPath)
                if (targetNode == null) {
                    currentPath = currentPath.parent
                }
            }

            if (targetNode != null && targetNode.isExpanded) {
                val nodeToRefresh = targetNode
                val pathToRefresh = nodeToRefresh.value.path

                scope.launch {
                    val children = loadChildren(pathToRefresh)
                    Platform.runLater {
                        nodeToRefresh.children.setAll(children)
                        restoreExpandedPaths(nodeToRefresh)
                    }
                }
            }
        }
    }

    private fun findNode(root: TreeItem<FileTreeItem>, path: Path): TreeItem<FileTreeItem>? {
        if (root.value?.path == path) return root

        // Optimization: Check if path belongs to this branch
        if (root.value?.displayName == "Workspace") {
            for (child in root.children) {
                if (path.startsWith(child.value.path)) {
                    return findNode(child, path)
                }
            }
            return null
        }

        for (child in root.children) {
            val childPath = child.value?.path ?: continue
            if (path == childPath) return child
            if (path.startsWith(childPath)) {
                return findNode(child, path)
            }
        }
        return null
    }

    /**
     * Loads the children for a given directory, sorting them and compacting empty parent directories.
     */
    private fun loadChildren(directory: Path): List<TreeItem<FileTreeItem>> {
        return try {
            // This set tracks paths that have been processed as part of a compacted directory
            // to avoid adding them as duplicate, separate entries in the tree.
            val processedPaths = mutableSetOf<Path>()
            FileService.listDirectory(directory)
                .sortedWith(compareBy<Path> { !it.isDirectory() }.thenComparator { a, b ->
                    naturalOrderComparator.compare(a.fileName.toString(), b.fileName.toString())
                })
                .mapNotNull { entry ->
                    if (entry in processedPaths) return@mapNotNull null

                    if (entry.isDirectory()) {
                        val (finalPath, displayName) = findCompactedPath(entry)
                        var current = finalPath
                        while (current != entry.parent) {
                            if (current != entry) processedPaths.add(current)
                            current = current.parent ?: break
                        }
                        createNode(FileTreeItem(finalPath, displayName))
                    } else {
                        createNode(FileTreeItem(entry, entry.fileName.toString()))
                    }
                }
        } catch (e: Exception) {
            logger.error("Failed to load children for directory: $directory", e)
            emptyList()
        }
    }

    /**
     * Compacts chains of single-child directories into a single, dot-separated display name.
     * For example, a structure like `src/main/kotlin` where `src` and `main` only contain
     * one directory will be treated as a single logical node with the display name "src.main.kotlin".
     *
     * @param startPath The initial directory to begin compaction from.
     * @return A Pair containing the final, deepest path in the chain and the compacted display name.
     */ //TODO: Add settings to change separation character
    private fun findCompactedPath(startPath: Path): Pair<Path, String> {
        var currentPath = startPath
        val nameParts = mutableListOf(startPath.fileName.toString())

        while (true) {
            val entries = FileService.listDirectory(currentPath)
            if (entries.size == 1 && entries.first().isDirectory()) {
                currentPath = entries.first()
                nameParts.add(currentPath.fileName.toString())
            } else {
                break
            }
        }
        return Pair(currentPath, nameParts.joinToString("."))
    }

    private fun createNode(itemData: FileTreeItem): TreeItem<FileTreeItem> {
        val treeItem = TreeItem(itemData)
        
        addExpansionListener(treeItem, itemData.path)

        if (itemData.path.isDirectory()) {
            treeItem.children.add(TreeItem()) // Fake item for expandability

            treeItem.expandedProperty().addListener { _, _, isExpanded ->
                if (isExpanded && treeItem.children.firstOrNull()?.value == null) {
                    scope.launch {
                        val children = loadChildren(itemData.path)
                        Platform.runLater {
                            treeItem.children.setAll(children)
                            restoreExpandedPaths(treeItem) // Restore children state
                        }
                    }
                }
            }
        }
        return treeItem
    }
    
    private fun addExpansionListener(item: TreeItem<FileTreeItem>, path: Path) {
        item.expandedProperty().addListener { _, _, isExpanded ->
            if (isRestoringExpansion) return@addListener
            
            if (isExpanded) {
                expandedPaths.add(path)
            } else {
                expandedPaths.remove(path)
            }
            WorkspaceManager.updateExpandedPaths(expandedPaths)
        }
    }

    //TODO: Move to utils
    private val naturalOrderComparator = Comparator<String> { a, b ->
        var i = 0
        var j = 0

        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]

            if (ca.isDigit() && cb.isDigit()) {
                var na = StringBuilder()
                var nb = StringBuilder()

                while (i < a.length && a[i].isDigit()) {
                    na.append(a[i])
                    i++
                }
                while (j < b.length && b[j].isDigit()) {
                    nb.append(b[j])
                    j++
                }

                val lenDiff = na.length - nb.length
                val diff = if (lenDiff != 0 ) lenDiff else na.toString().compareTo(nb.toString())
                if (diff != 0) return@Comparator diff
            } else {
                val diff = ca.lowercaseChar() - cb.lowercaseChar()
                if (diff != 0) return@Comparator diff
                i++
                j++
            }
        }

        a.length - b.length
    }
}