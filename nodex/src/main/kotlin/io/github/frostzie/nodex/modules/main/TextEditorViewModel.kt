package io.github.frostzie.nodex.modules.main

import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.modules.bars.BottomBarModule
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.services.FileService
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.beans.value.ChangeListener
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

/**
 * ViewModel for the text editor that manages multiple tabs.
 * Each tab represents an open file with its own CodeArea instance.
 */
class TextEditorViewModel {
    private val logger = LoggerProvider.getLogger("TextEditorViewModel")

    enum class EditorAction {
        UNDO, REDO, CUT, COPY, PASTE, SELECT_ALL
    }

    val currentAction = SimpleObjectProperty<EditorAction?>(null)

    /**
     * Data class representing a single editor tab
     */
    data class TabData(
        val id: String = UUID.randomUUID().toString(),
        val filePath: Path,
        val displayName: String,
        val content: StringProperty = SimpleStringProperty(""),
        val isDirty: BooleanProperty = SimpleBooleanProperty(false),
        // Listeners to be managed for cleanup
        var isDirtyListener: ChangeListener<Boolean>? = null
    )

    // Observable list of all open tabs
    val tabs: ObservableList<TabData> = FXCollections.observableArrayList()

    // Currently active tab //TODO: Fix: (External renaming) FileX -> FileY then FolderX -> FolderY then focus app = dir as tab...
    val activeTab = SimpleObjectProperty<TabData?>()

    // Current line and column of the caret in the active editor
    val currentLine: IntegerProperty = SimpleIntegerProperty(1)
    val currentColumn: IntegerProperty = SimpleIntegerProperty(1)

    private var isRestoringSession = false

    init {
        EventBus.register(this)
        
        // Listen for tab changes to persist state
        tabs.addListener(InvalidationListener {
             persistState()
        })
        
        activeTab.addListener { _, _, newTab ->
             persistState()
             EventBus.post(ActiveTabChangedEvent(newTab?.filePath))
        }

        // Initial session restoration for when the VM is created after WorkspaceManager is already ready
        Platform.runLater {
            restoreSession()
        }
    }

    @SubscribeEvent @Suppress("unused")
    fun onWorkspaceUpdated(event: WorkspaceUpdated) {
        // Reload open files from session state
        if (isRestoringSession) return
        
        Platform.runLater {
            restoreSession()
        }
    }

    private fun restoreSession() {
        isRestoringSession = true
        try {
            val state = WorkspaceManager.getCurrentState()
            val savedFiles = state.openFiles
            val lastActive = state.activeFile
            
            // Close existing tabs that are not in the new session (e.g. on reset or project switch)
            val toClose = tabs.filter { it.filePath !in savedFiles }
            
            // Close removed tabs
            toClose.forEach { closeTab(it, persist = false) }
            
            // Close tabs that are in the session but missing on disk (prevent resurrection)
            val missingTabs = tabs.filter { it.filePath in savedFiles && !Files.exists(it.filePath) }
            missingTabs.forEach { closeTab(it, persist = false, save = false) }
            
            val currentPaths = tabs.map { it.filePath }.toSet()
            
            // Open new files
            savedFiles.forEach { path ->
                 if (path !in currentPaths && Files.exists(path)) {
                     createNewTab(path)
                 }
            }
            
            // Restore active tab
            if (lastActive != null) {
                val tab = tabs.find { it.filePath == lastActive }
                if (tab != null) {
                    activeTab.set(tab)
                }
            }
        } finally {
            isRestoringSession = false
        }
    }
    
    private fun persistState() {
        if (isRestoringSession) return
        
        val openPaths = tabs.map { it.filePath }.toSet()
        val activePath = activeTab.get()?.filePath

        val currentState = WorkspaceManager.getCurrentState()
        if (openPaths != currentState.openFiles || activePath != currentState.activeFile) {
             WorkspaceManager.updateOpenFiles(openPaths, activePath)
        }
    }

    /**
     * Opens a file in the editor. If the file is already open, switch to that tab.
     * Otherwise, create a new tab.
     */
    @SubscribeEvent @Suppress("unused")
    fun onOpenFile(event: OpenFile) {
        Platform.runLater {
            val existingTab = tabs.find { it.filePath == event.path }
            if (existingTab != null) {
                logger.info("File already open, switching to tab: ${event.path.fileName}")
                activeTab.set(existingTab)
                return@runLater
            }

            logger.debug("Opening new file in tab: {}", event.path.fileName)
            createNewTab(event.path)
        }
    }

    /**
     * Saves all modified files
     */
    @SubscribeEvent @Suppress("unused")
    fun onSaveAll(event: SaveAllFiles) {
        logger.debug("Saving all modified files...")
        tabs.filter { it.isDirty.get() }.forEach { saveFile(it) }
    }

    // Currently only used for while editing Theme, but overall file saving should move out of this
    //TODO: should be stricter MVVM
    @SubscribeEvent @Suppress("unused")
    fun onSaveFile(event: SaveFileEvent) {
        Platform.runLater {
            val tab = tabs.find { it.filePath == event.path }
            if (tab != null) {
                saveFile(tab)
            }
        }
    }

    @SubscribeEvent @Suppress("unused")
    fun onEditorUndo(event: EditorUndo) {
        currentAction.set(EditorAction.UNDO)
    }

    @SubscribeEvent @Suppress("unused")
    fun onEditorRedo(event: EditorRedo) {
        currentAction.set(EditorAction.REDO)
    }

    @SubscribeEvent @Suppress("unused")
    fun onEditorCut(event: EditorCut) {
        currentAction.set(EditorAction.CUT)
    }

    @SubscribeEvent @Suppress("unused")
    fun onEditorCopy(event: EditorCopy) {
        currentAction.set(EditorAction.COPY)
    }

    @SubscribeEvent @Suppress("unused")
    fun onEditorPaste(event: EditorPaste) {
        currentAction.set(EditorAction.PASTE)
    }

    @SubscribeEvent @Suppress("unused")
    fun onEditorSelectAll(event: EditorSelectAll) {
        currentAction.set(EditorAction.SELECT_ALL)
    }

    @SubscribeEvent @Suppress("unused")
    fun onFileModified(event: FileModified) {
        Platform.runLater {
            val tab = tabs.find { it.filePath == event.path } ?: return@runLater
            // Only reload if not dirty to avoid overwriting user changes (Maybe add de-sync notification)
            if (!tab.isDirty.get()) {
                try {
                    val newContent = FileService.readText(event.path)
                    tab.content.set(newContent)
                    // Reset dirty state since this is a sync with disk
                    tab.isDirty.set(false)
                } catch (e: Exception) {
                    logger.error("Failed to reload modified file: ${event.path}", e)
                }
            }
        }
    }

    @SubscribeEvent @Suppress("unused")
    fun onFileDeleted(event: FileDeleted) {
        Platform.runLater {
            val tab = tabs.find { it.filePath == event.path } ?: return@runLater
            // Close the tab without trying to save, as the file is gone
            closeTab(tab, save = false)
        }
    }

    @SubscribeEvent @Suppress("unused")
    fun onFileMoved(event: FileMoved) {
        Platform.runLater {
            val oldTab = tabs.find { it.filePath == event.oldPath } ?: return@runLater

            // Detach old listeners
            oldTab.isDirtyListener?.let { oldTab.isDirty.removeListener(it) }

            // Create new TabData with an updated path but same CodeArea/ID
            val newTab = oldTab.copy(
                filePath = event.newPath,
                displayName = event.newPath.fileName.toString(),
                isDirtyListener = null
            )

            // Setup listeners on new tab data
            setupTabListeners(newTab)

            // Update dirtyFiles set if it was dirty
            if (oldTab.isDirty.get()) {
                WorkspaceManager.dirtyFiles.remove(oldTab.filePath)
                WorkspaceManager.dirtyFiles.add(newTab.filePath)
            }

            val index = tabs.indexOf(oldTab)
            if (index != -1) {
                tabs[index] = newTab
            }

            // Note: TextEditorView's addTab automatically selects the new tab, so activeTab update is handled there.
        }
    }

    /**
     * Creates a new tab for the given file path
     */
    private fun createNewTab(filePath: Path) {
        try {
            // Safety Guard: Never open a directory as a tab
            if (FileService.isDirectory(filePath)) {
                logger.warn("Attempted to open a directory as a tab: $filePath")
                return
            }

            // Read file content
            val contentText = FileService.readText(filePath)
            logger.debug("Read file content: {} ({} characters)", filePath.fileName, contentText.length)

            val tabData = TabData(
                filePath = filePath,
                displayName = filePath.fileName.toString(),
                content = SimpleStringProperty(contentText)
            )

            setupTabListeners(tabData)

            // Insert after the currently active tab, or at the end if none is active
            val active = activeTab.get()
            val index = if (active != null) {
                val activeIndex = tabs.indexOf(active)
                if (activeIndex != -1) activeIndex + 1 else tabs.size
            } else {
                tabs.size
            }

            tabs.add(index.coerceIn(0, tabs.size), tabData)

            activeTab.set(tabData)

            logger.debug("Tab created for file: {}", filePath.fileName)

        } catch (e: Exception) {
            logger.error("Failed to create tab for file: ${filePath.fileName}", e)
        }
    }

    private fun setupTabListeners(tabData: TabData) {
        tabData.isDirtyListener = ChangeListener { _, _, isDirty ->
            if (isDirty) {
                WorkspaceManager.dirtyFiles.add(tabData.filePath)
            } else {
                WorkspaceManager.dirtyFiles.remove(tabData.filePath)
            }
        }.also { tabData.isDirty.addListener(it) }
    }

    /**
     * Updates the line and column properties.
     */
    fun updateLineAndColumn(line: Int, column: Int) {
        currentLine.set(line)
        currentColumn.set(column)
        BottomBarModule.updateCursorPosition(line, column)
    }

    /**
     * Closes the specified tab and auto-saves before closing
     */
    fun closeTab(tabData: TabData, persist: Boolean = true, save: Boolean = true) {
        logger.debug("Closing tab: ${tabData.displayName}")

        if (save) {
            // Auto-save before closing
            try {
                saveFile(tabData)
                logger.debug("Auto-saved file before closing: ${tabData.displayName}")
            } catch (e: Exception) {
                logger.error("Failed to auto-save before closing: ${tabData.displayName}", e)
            }
        }

        // Ensure the path is removed from the global dirty set
        WorkspaceManager.dirtyFiles.remove(tabData.filePath)

        // Remove listeners to prevent memory leaks
        tabData.isDirtyListener?.let { tabData.isDirty.removeListener(it) }
        tabData.isDirtyListener = null

        tabs.remove(tabData)

        // If the closed tab was active, switch to another tab
        if (activeTab.get() == tabData) {
            activeTab.set(tabs.firstOrNull())
        }
        
        if (persist) persistState()
    }
    
    // Overload for API compatibility if needed elsewhere
    fun closeTab(tabData: TabData) {
        closeTab(tabData, persist = true, save = true)
    }

    /**
     * Saves the active tab's content
     */
    fun saveActiveTab() {
        val tab = activeTab.get() ?: return
        saveFile(tab)
        logger.info("Manually saved file: ${tab.displayName}")
    }

    private fun saveFile(tabData: TabData) {
        try {
            // Safety guard: Never try to write content to a directory path
            // This prevents crashes if a tab accidentally points to a directory
            if (FileService.isDirectory(tabData.filePath)) {
                logger.warn("Attempted to save to a directory path: ${tabData.filePath}. Aborting save!")
                tabData.isDirty.set(false)
                return
            }

            val content = tabData.content.get()
            WorkspaceManager.ignoreWatcherPath(tabData.filePath)
            FileService.writeText(tabData.filePath, content)
            tabData.isDirty.set(false)
            logger.debug("File saved: {} ({} characters)", tabData.filePath.fileName, content.length)
        } catch (e: Exception) {
            logger.error("Failed to save file: ${tabData.filePath.fileName}", e)
            // TODO: Show error notification to user
        }
    }

    /**
     * Cleanup method to be called when the editor is closed
     */
    fun cleanup() {
        // Auto-save all tabs before cleanup
        tabs.forEach { tab ->
            try {
                saveFile(tab)
                logger.debug("Auto-saved during cleanup: ${tab.displayName}")
            } catch (e: Exception) {
                logger.error("Failed to auto-save during cleanup: ${tab.displayName}", e)
            }
        }

        tabs.clear()
        activeTab.set(null)
        EventBus.unregister(this)
        logger.info("TextEditorViewModel cleaned up")
    }
}