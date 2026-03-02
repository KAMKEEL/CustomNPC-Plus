package io.github.frostzie.nodex.screen.elements.main

import atlantafx.base.controls.Tab
import atlantafx.base.controls.TabLine
import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.features.FeatureRegistry
import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.RichJsonFX
import io.github.frostzie.nodex.settings.categories.ThemeConfig
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.LineNumberFactory
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import javafx.util.Duration
import java.nio.file.Path

/**
 * View for the text editor that displays multiple tabs using AtlantaFX TabLine.
 * Each tab contains a CodeArea with the text editor.
 */
class TextEditorView : VBox() {

    companion object {
        private val logger = LoggerProvider.getLogger("TextEditorView")
    }

    internal val viewModel = TextEditorViewModel()
    private val tabLine = TabLine()
    private val contentArea = StackPane()
    private val decoratorCleanups = mutableMapOf<String, MutableList<() -> Unit>>()
    private val tabCodeAreas = mutableMapOf<String, CodeArea>()
    private var currentThemeStyleSheet: String? = null

    init {
        styleClass.add("text-editor-container")

        setupTabLine()
        setupContentArea()
        setupListeners()
        setupThemeListener()

        children.addAll(tabLine, contentArea)
        setVgrow(contentArea, Priority.ALWAYS)
    }

    // Temp for Json syntax
    private fun setupThemeListener() {
        val updateListener = InvalidationListener { updateThemeColors() }
        ThemeConfig.jsonStartObjectColor.addListener(updateListener)
        ThemeConfig.jsonPropertyColor.addListener(updateListener)
        ThemeConfig.jsonStringColor.addListener(updateListener)
        ThemeConfig.jsonArrayColor.addListener(updateListener)
        ThemeConfig.jsonFloatColor.addListener(updateListener)
        ThemeConfig.jsonIntColor.addListener(updateListener)
        ThemeConfig.jsonNullColor.addListener(updateListener)
        ThemeConfig.jsonEmbeddedColor.addListener(updateListener)
        ThemeConfig.jsonTrueColor.addListener(updateListener)
        ThemeConfig.jsonFalseColor.addListener(updateListener)

        updateThemeColors()
    }

    private fun updateThemeColors() {
        val css = """
            .code-area .json-start-object, .code-area .json-end-object { -fx-fill: ${ThemeConfig.jsonStartObjectColor.get()}; }
            .code-area .json-property { -fx-fill: ${ThemeConfig.jsonPropertyColor.get()}; }
            .code-area .json-string { -fx-fill: ${ThemeConfig.jsonStringColor.get()}; }
            .code-area .json-start-array, .code-area .json-end-array { -fx-fill: ${ThemeConfig.jsonArrayColor.get()}; }
            .code-area .json-float { -fx-fill: ${ThemeConfig.jsonFloatColor.get()}; }
            .code-area .json-int { -fx-fill: ${ThemeConfig.jsonIntColor.get()}; }
            .code-area .json-null { -fx-fill: ${ThemeConfig.jsonNullColor.get()}; }
            .code-area .json-embedded { -fx-fill: ${ThemeConfig.jsonEmbeddedColor.get()}; }
            .code-area .json-true { -fx-fill: ${ThemeConfig.jsonTrueColor.get()}; }
            .code-area .json-false { -fx-fill: ${ThemeConfig.jsonFalseColor.get()}; }
        """.trimIndent()

        val dataUri = "data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(css.toByteArray())

        if (currentThemeStyleSheet != null) {
            this.stylesheets.remove(currentThemeStyleSheet)
        }

        this.stylesheets.add(dataUri)
        currentThemeStyleSheet = dataUri
    }

    /**
     * Configures the TabLine with AtlantaFX styles and policies
     */
    private fun setupTabLine() {
        tabLine.styleClass.add(Styles.TABS_BORDER_TOP)
        tabLine.animated = false // No more PowerPoint Animation yeay lol
        tabLine.setTabDragPolicy(Tab.DragPolicy.REORDER)
        tabLine.setTabResizePolicy(Tab.ResizePolicy.COMPUTED_WIDTH)
        tabLine.setTabClosingPolicy(Tab.ClosingPolicy.SELECTED_TAB) //TODO: Possibly? add hover closing
    }

    private fun setupContentArea() {
        contentArea.styleClass.add("editor-content-area")
        setVgrow(contentArea, Priority.ALWAYS)
    }

    /**
     * Sets up listeners for ViewModel changes
     */
    private fun setupListeners() {
        // Listen for new tabs being added
        viewModel.tabs.addListener { change: ListChangeListener.Change<out TextEditorViewModel.TabData> ->
            while (change.next()) {
                // Handle removals first. In a replacement event (like file move), wasRemoved and wasAdded are both true.
                // We must remove the old tab first to avoid ID conflicts or index shifting issues.
                if (change.wasRemoved()) {
                    change.removed.forEach { tabData ->
                        removeTab(tabData)
                    }
                }
                if (change.wasAdded()) {
                    var index = change.from
                    change.addedSubList.forEach { tabData ->
                        addTab(tabData, index)
                        index++
                    }
                }
            }
        }

        // Listen for active tab changes
        viewModel.activeTab.addListener { _, _, newTab ->
            if (newTab != null) {
                switchToTab(newTab)
            } else {
                contentArea.children.clear()
            }
        }

        // Listen for tab selection changes in the UI
        tabLine.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            if (newTab != null) {
                val tabData = viewModel.tabs.find { it.id == newTab.id }
                if (tabData != null && viewModel.activeTab.get() != tabData) {
                    viewModel.activeTab.set(tabData)
                }
            }
        }

        // Listen for editor actions (Undo, Redo, etc.)
        viewModel.currentAction.addListener { _, _, action ->
            if (action != null) {
                handleEditorAction(action)
                Platform.runLater { viewModel.currentAction.set(null) }
            }
        }
    }

    /**
     * Adds a new tab to the TabLine for the given TabData
     */
    private fun addTab(tabData: TextEditorViewModel.TabData, index: Int = -1) {
        // Create CodeArea and binding logic
        val codeArea = CodeArea(tabData.content.get())
        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
        codeArea.styleClass.add("code-area")

        // Temp for json syntax
        if (isJsonFile(tabData.filePath)) {
            setupJsonSyntaxHighlighting(codeArea)
        }
        
        // Listener to sync CodeArea -> ViewModel (User typing)
        val textListener = InvalidationListener {
            if (codeArea.text != tabData.content.get()) {
                tabData.content.set(codeArea.text)
                if (!tabData.isDirty.get()) {
                    tabData.isDirty.set(true)
                }
            }
        }
        codeArea.textProperty().addListener(textListener)
        
        // Listener to sync ViewModel -> CodeArea (External reload)
        val contentListener = InvalidationListener {
             if (codeArea.text != tabData.content.get()) {
                 codeArea.replaceText(tabData.content.get())
             }
        }
        tabData.content.addListener(contentListener)
        
        // Listener for caret position
        val caretListener = InvalidationListener {
             if (viewModel.activeTab.get() == tabData) {
                 viewModel.updateLineAndColumn(codeArea.currentParagraph + 1, codeArea.caretColumn + 1)
             }
        }
        codeArea.caretPositionProperty().addListener(caretListener)
        
        tabCodeAreas[tabData.id] = codeArea

        // Create a custom graphic for the tab content, allowing direct access to the label for styling
        val tabLabel = Label(tabData.displayName)
        val tabIcon = FontIcon(Material2AL.FOLDER)

        // An invisible placeholder that reserves space for the close button.
        // The width is an estimate of the close button's size. If you find a more exact one, change pls.
        val closeButtonPlaceholder = Region().apply {
            prefWidth = 22.0
        }

        val graphic = HBox(tabIcon, tabLabel, closeButtonPlaceholder).apply {
            alignment = Pos.CENTER_LEFT
            spacing = 5.0 // Space between icon and label
        }

        // We pass null for text and use our custom graphic instead
        val tab = Tab(tabData.id, null, graphic)
        tab.tooltip = Tooltip(tabData.filePath.toString())

        // When the tab is selected, the close button appears, so we hide the placeholder.
        // When it's deselected, we show the placeholder to keep the tab width consistent.
        val selectionListener = ChangeListener<Boolean> { _, _, isSelected ->
            closeButtonPlaceholder.isVisible = !isSelected
            closeButtonPlaceholder.isManaged = !isSelected
        }
        tab.selectedProperty().addListener(selectionListener)

        val cleanups = mutableListOf<() -> Unit>()
        FeatureRegistry.editorTabDecorators.forEach { decorator ->
            cleanups.add(decorator.decorate(tab, codeArea, tabData))
        }
        decoratorCleanups[tabData.id] = cleanups

        tab.setOnCloseRequest { event ->
            tab.selectedProperty().removeListener(selectionListener) // Clean up listener
            viewModel.closeTab(tabData)
            event.consume()
        }

        if (index >= 0 && index <= tabLine.tabs.size) {
            tabLine.tabs.add(index, tab)
        } else {
            tabLine.tabs.add(tab)
        }
        tabLine.selectionModel.select(tab)

        logger.debug("Added tab: ${tabData.displayName}, ID: ${tabData.id}")
    }

    // Temp Json syntax highlighting
    private fun isJsonFile(path: Path): Boolean {
        val fileName = path.fileName.toString().lowercase()
        return fileName.endsWith(".json") || fileName.endsWith(".mcmeta")
    }

    private fun setupJsonSyntaxHighlighting(codeArea: CodeArea) {
        val highlighter = RichJsonFX()
        val debounce = PauseTransition(Duration.millis(300.0))

        fun applyHighlighting() {
            try {
                highlighter.highlightCodeArea(codeArea)
            } catch (e: Exception) {
                // Ignore parsing errors while typing (e.g. incomplete JSON)
            }
        }

        debounce.setOnFinished { applyHighlighting() }

        codeArea.textProperty().addListener { _, _, _ ->
            debounce.playFromStart()
        }

        // Initial highlight
        applyHighlighting()
    }

    /**
     * Removes a tab from the TabLine
     */
    private fun removeTab(tabData: TextEditorViewModel.TabData) {
        tabLine.tabs.find { it.id == tabData.id }?.let {
            tabLine.tabs.remove(it)
        }
        
        tabCodeAreas.remove(tabData.id)

        // Execute and remove all cleanup functions associated with the closed tab
        decoratorCleanups.remove(tabData.id)?.forEach { cleanup ->
            try {
                cleanup()
            } catch (e: Exception) {
                logger.error("Error during tab decorator cleanup for ${tabData.displayName}", e)
            }
        }
        logger.debug("Removed tab and cleaned up decorators: ${tabData.displayName}, ID: ${tabData.id}")
    }

    /**
     * Switches the content area to display the CodeArea for the given tab
     */
    private fun switchToTab(tabData: TextEditorViewModel.TabData) {
        contentArea.children.clear()
        val codeArea = tabCodeAreas[tabData.id] ?: return
        
        contentArea.children.add(VirtualizedScrollPane(codeArea))
        
        // Update status bar for this tab
        viewModel.updateLineAndColumn(codeArea.currentParagraph + 1, codeArea.caretColumn + 1)

        // Select the corresponding tab in the TabLine
        val tab = tabLine.tabs.find { it.id == tabData.id }
        if (tab != null && tabLine.selectionModel.selectedItem != tab) {
            tabLine.selectionModel.select(tab)
        }

        Platform.runLater {
            codeArea.requestFocus()
        }

        logger.debug("Switched to tab: ${tabData.displayName}, ID: ${tabData.id}")
    }

    private fun handleEditorAction(action: TextEditorViewModel.EditorAction) {
        val activeTab = viewModel.activeTab.get() ?: return
        val codeArea = tabCodeAreas[activeTab.id] ?: return

        when (action) {
            TextEditorViewModel.EditorAction.UNDO -> codeArea.undo()
            TextEditorViewModel.EditorAction.REDO -> codeArea.redo()
            TextEditorViewModel.EditorAction.CUT -> codeArea.cut()
            TextEditorViewModel.EditorAction.COPY -> codeArea.copy()
            TextEditorViewModel.EditorAction.PASTE -> codeArea.paste()
            TextEditorViewModel.EditorAction.SELECT_ALL -> codeArea.selectAll()
        }
        codeArea.requestFocus()
    }

    /**
     * Request focus for the active tab's CodeArea
     */
    override fun requestFocus() {
        super.requestFocus()
        val active = viewModel.activeTab.get() ?: return
        tabCodeAreas[active.id]?.requestFocus()
    }

    /**
     * Cleanup method
     */
    fun cleanup() {
        viewModel.cleanup()
        logger.info("TextEditorView closed")
    }
}