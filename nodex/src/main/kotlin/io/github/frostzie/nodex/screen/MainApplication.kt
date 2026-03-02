package io.github.frostzie.nodex.screen

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.WorkspaceUpdated
import io.github.frostzie.nodex.handlers.bars.BottomBarHandler
import io.github.frostzie.nodex.handlers.bars.LeftBarHandler
import io.github.frostzie.nodex.handlers.bars.top.TopBarHandler
import io.github.frostzie.nodex.handlers.popup.file.FilePopupHandler
import io.github.frostzie.nodex.handlers.popup.settings.SettingsHandler
import io.github.frostzie.nodex.modules.bars.BottomBarModule
import io.github.frostzie.nodex.modules.bars.LeftBarModule
import io.github.frostzie.nodex.modules.bars.top.TopBarViewModel
import io.github.frostzie.nodex.modules.popup.file.FilePopupModule
import io.github.frostzie.nodex.modules.popup.settings.SettingsModule
import io.github.frostzie.nodex.handlers.popup.settings.ThemeHandler
import io.github.frostzie.nodex.modules.popup.settings.ThemeModule
import io.github.frostzie.nodex.styling.common.NotificationMessageArea
import io.github.frostzie.nodex.screen.elements.bars.BottomBarView
import io.github.frostzie.nodex.screen.elements.bars.LeftBarView
import io.github.frostzie.nodex.screen.elements.bars.top.TopBarView
import io.github.frostzie.nodex.screen.elements.main.FileTreeView
import io.github.frostzie.nodex.screen.elements.main.TextEditorView
import io.github.frostzie.nodex.screen.elements.popup.settings.SettingsView
import io.github.frostzie.nodex.screen.elements.project.ProjectManagerView
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.utils.JavaFXInitializer
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.WindowResizer
import io.github.frostzie.nodex.utils.dev.DebugManager
import javafx.scene.layout.Pane
import io.github.frostzie.nodex.utils.CSSManager
import io.github.frostzie.nodex.utils.WindowDrag
import io.github.frostzie.nodex.utils.UIConstants
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.control.SplitPane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import io.github.frostzie.nodex.settings.categories.ThemeConfig
import io.github.frostzie.nodex.utils.ThemeUtils
import io.github.frostzie.nodex.config.LayoutManager

class MainApplication {

    companion object {
        private val logger = LoggerProvider.getLogger("MainApplication")
        private var primaryStage: Stage? = null
        private var fxInitialized = false

        // UI Components
        private var topBarView: TopBarView? = null
        private var leftBarView: LeftBarView? = null
        private var fileTreeView: FileTreeView? = null
        private var bottomBarView: BottomBarView? = null
        private var settingsView: SettingsView? = null
        private var textEditorView: TextEditorView? = null
        private var contentArea: SplitPane? = null
        
        // View Containers
        private var projectManagerView: ProjectManagerView? = null
        private var ideLayout: BorderPane? = null
        private var rootContainer: StackPane? = null

        // Modules and Handlers
        private var topBarViewModel: TopBarViewModel? = null
        private var topBarHandler: TopBarHandler? = null

        private var leftBarModule: LeftBarModule? = null
        private var leftBarHandler: LeftBarHandler? = null

        private var bottomBarModule: BottomBarModule? = null
        private var bottomBarHandler: BottomBarHandler? = null

        private var settingsModule: SettingsModule? = null
        private var settingsHandler: SettingsHandler? = null

        private var filePopupModule: FilePopupModule? = null
        private var filePopupHandler: FilePopupHandler? = null

        private var themeModule: ThemeModule? = null
        private var themeHandler: ThemeHandler? = null
        
        // Drag cleanup callback
        private var dragCleanup: (() -> Unit)? = null
        
        private val workspaceHandler = object {
            @SubscribeEvent
            fun onWorkspaceUpdated(event: WorkspaceUpdated) {
                JavaFXInitializer.runLater {
                    updateMainView()
                }
            }
        }

        fun initializeJavaFX() {

            if (fxInitialized) return
            fxInitialized = true

            System.setProperty("javafx.allowSystemPropertiesAccess", "true")
            System.setProperty("prism.allowhidpi", "false")

            try {
                JavaFXInitializer.startup {
                    ThemeUtils.applyTheme(ThemeConfig.theme.get())
                    ThemeConfig.theme.addListener { _, _, newTheme -> ThemeUtils.applyTheme(newTheme) }
                    ThemeConfig.fontSize.addListener { _, _, _ ->
                        primaryStage?.scene?.root?.style = "-fx-font-size: ${ThemeConfig.fontSize.get()}px;"
                    }
                    JavaFXInitializer.setImplicitExit(false)
                    createMainWindow()
                    logger.info("JavaFX Platform initialized and main window pre-created!")
                }
            } catch (e: Exception) {
                // If it's already initialized, JavaFXInitializer.startup handles it and runs the runnable
                // This is only if truly bad
                logger.error("Failed to initialize JavaFX", e)
            }
        }

        private fun createMainUI(stage: Stage): Pane {
            // IDE Layout Construction
            ideLayout = BorderPane()
            stage.icons.add(Image("assets/nodex/icon.png"))

            topBarViewModel = TopBarViewModel(stage)
            topBarView = TopBarView(topBarViewModel!!)
            topBarHandler = TopBarHandler(topBarViewModel!!)

            leftBarView = LeftBarView()
            leftBarModule = LeftBarModule(stage)
            leftBarHandler = LeftBarHandler(leftBarModule!!)

            bottomBarView = BottomBarView()
            bottomBarModule = BottomBarModule
            bottomBarHandler = BottomBarHandler(bottomBarModule!!)

            textEditorView = TextEditorView()
            fileTreeView = FileTreeView()

            themeModule = ThemeModule()
            themeHandler = ThemeHandler(themeModule!!)

            settingsModule = SettingsModule(stage)
            settingsHandler = SettingsHandler(settingsModule!!)

            filePopupModule = FilePopupModule(stage)
            filePopupHandler = FilePopupHandler(filePopupModule!!)

            setupEventHandlers()

            val defaultDividerPosition = UIConstants.FILE_TREE_DEFAULT_WIDTH / (UIConstants.DEFAULT_WINDOW_WIDTH - UIConstants.LEFT_BAR_WIDTH)

            contentArea = SplitPane().apply {
                items.addAll(fileTreeView, textEditorView)

                setDividerPosition(0, defaultDividerPosition)

                SplitPane.setResizableWithParent(fileTreeView, false)
                SplitPane.setResizableWithParent(textEditorView, true)
            }

            var lastDividerPosition = defaultDividerPosition
            fileTreeView!!.viewModel.isVisible.addListener { _, _, isVisible ->
                val splitPane = contentArea ?: return@addListener
                if (isVisible) {
                    if (!splitPane.items.contains(fileTreeView)) {
                        splitPane.items.add(0, fileTreeView)
                        Platform.runLater { splitPane.setDividerPositions(lastDividerPosition) }
                    }
                } else {
                    if (splitPane.items.contains(fileTreeView)) {
                        if (splitPane.dividers.isNotEmpty()) lastDividerPosition = splitPane.dividers[0].position
                        splitPane.items.remove(fileTreeView)
                    }
                }
            }

            val centerContent = HBox().apply {
                children.addAll(leftBarView, contentArea)
                HBox.setHgrow(contentArea, Priority.ALWAYS)
            }

            ideLayout!!.top = topBarView
            ideLayout!!.center = centerContent
            ideLayout!!.bottom = bottomBarView
            
            // Start Screen Construction
            projectManagerView = ProjectManagerView()

            // Root Container
            rootContainer = StackPane()
            rootContainer!!.styleClass.add("window")
            // Wrap in modal pane stack
            val rootStack = StackPane(rootContainer!!, NotificationMessageArea)
            setupStageDimensions(stage, rootContainer!!)

            // The resizable wrapper should wrap the entire root stack
            val resizableWrapper = WindowResizer.install(stage, rootStack, ::saveCurrentLayout)
            DebugManager.initialize(resizableWrapper)

            return resizableWrapper
        }
        
        private fun saveCurrentLayout() {
            val stage = primaryStage ?: return
            
            if (stage.isIconified || topBarViewModel?.isMaximized == true) {
                return
            }
            
            val isProjectManagerView = rootContainer?.children?.getOrNull(0) == projectManagerView
            if (isProjectManagerView) {
                LayoutManager.config.projectManagerViewX = stage.x
                LayoutManager.config.projectManagerViewY = stage.y
                LayoutManager.config.projectManagerViewWidth = stage.width
                LayoutManager.config.projectManagerViewHeight = stage.height
            } else {
                LayoutManager.config.x = stage.x
                LayoutManager.config.y = stage.y
                LayoutManager.config.width = stage.width
                LayoutManager.config.height = stage.height
            }
            LayoutManager.save()
        }

        private fun updateMainView() {
            if (rootContainer == null || primaryStage == null) return

            val stage = primaryStage!!
            val hasProjects = WorkspaceManager.workspace.projects.isNotEmpty()
            logger.debug("Updating Main View. Has Projects: $hasProjects")

            // Clear previous drag listener
            dragCleanup?.invoke()
            dragCleanup = null

            if (hasProjects) {
                if (rootContainer!!.children.isEmpty() || rootContainer!!.children[0] != ideLayout) {

                    // Save Start Screen State before switching
                    if (rootContainer!!.children.isNotEmpty() && rootContainer!!.children[0] == projectManagerView) {
                         saveCurrentLayout()
                    }

                    rootContainer!!.children.setAll(ideLayout)

                    // Apply IDE State
                    stage.width = LayoutManager.config.width
                    stage.height = LayoutManager.config.height
                    if (LayoutManager.config.x != -1.0) {
                        stage.x = LayoutManager.config.x
                        stage.y = LayoutManager.config.y
                    } else {
                        stage.centerOnScreen()
                    }

                    // Enable Drag for TopBar
                    dragCleanup = WindowDrag.makeDraggable(stage, topBarView!!, ::saveCurrentLayout) { event ->
                        topBarView!!.isOverDraggableArea(event)
                    }
                }
            } else {
                if (rootContainer!!.children.isEmpty() || rootContainer!!.children[0] != projectManagerView) {

                    // Save IDE State before switching
                    if (rootContainer!!.children.isNotEmpty() && rootContainer!!.children[0] == ideLayout) {
                         saveCurrentLayout()
                    }

                    rootContainer!!.children.setAll(projectManagerView)

                    // Apply Start Screen State
                    stage.width = LayoutManager.config.projectManagerViewWidth
                    stage.height = LayoutManager.config.projectManagerViewHeight
                    if (LayoutManager.config.projectManagerViewX != -1.0) {
                        stage.x = LayoutManager.config.projectManagerViewX
                        stage.y = LayoutManager.config.projectManagerViewY
                    } else {
                        stage.centerOnScreen()
                    }

                    // Enable Drag for Start Screen Header
                    dragCleanup = WindowDrag.makeDraggable(stage, projectManagerView!!.dragTarget, ::saveCurrentLayout)
                }
            }
        }

        private fun setupStageDimensions(stage: Stage, root: Pane) {
            val minContentWidth = UIConstants.MIN_CONTENT_WIDTH
            val minContentHeight = UIConstants.MIN_CONTENT_HEIGHT
            val maxContentWidth = Double.MAX_VALUE
            val maxContentHeight = Double.MAX_VALUE

            val borderWidth = UIConstants.WINDOW_BORDER_WIDTH
            val topBarHeight = UIConstants.TOP_BAR_HEIGHT
            val statusBarHeight = UIConstants.BOTTOM_BAR_HEIGHT
            
            root.minWidth = minContentWidth + borderWidth
            root.maxWidth = maxContentWidth + borderWidth
            root.minHeight = minContentHeight + topBarHeight + statusBarHeight + borderWidth
            root.maxHeight = maxContentHeight + topBarHeight + statusBarHeight + borderWidth

            stage.minWidth = root.minWidth + UIConstants.STAGE_BORDER_WIDTH
            stage.minHeight = root.minHeight + UIConstants.STAGE_BORDER_WIDTH
            stage.maxWidth = root.maxWidth + UIConstants.STAGE_BORDER_WIDTH
            stage.maxHeight = root.maxHeight + UIConstants.STAGE_BORDER_WIDTH

            logger.debug("Stage dimensions set: min=${stage.minWidth}x${stage.minHeight}, max=${stage.maxWidth}x${stage.maxHeight}")
        }

        private fun setupEventHandlers() {
            // EventBus registrations - only for handlers, not views
            EventBus.register(topBarHandler!!)
            topBarView?.let { EventBus.register(it) }

            EventBus.register(leftBarHandler!!)
            leftBarView?.let { EventBus.register(it) }

            EventBus.register(bottomBarHandler!!)
            bottomBarView?.let { EventBus.register(it) }

            EventBus.register(settingsHandler!!)
            settingsView?.let { EventBus.register(it) }

            EventBus.register(filePopupHandler!!)

            EventBus.register(themeHandler!!)
            
            EventBus.register(workspaceHandler)

            logger.debug("Event handlers initialized")
        }

        fun showMainWindow() {
            if (!fxInitialized) {
                initializeJavaFX()
                return
            }
            JavaFXInitializer.runLater {
                if (primaryStage == null) {
                    createMainWindow()
                }
                if (primaryStage?.isIconified == true) {
                    primaryStage?.isIconified = false
                }
                primaryStage?.show()
                primaryStage?.toFront()
                textEditorView?.requestFocus()
                logger.info("Main IDE Window shown!")
            }
        }

        private fun createMainWindow() {
            if (primaryStage != null) return

            try {
                val stage = Stage()
                stage.initStyle(StageStyle.UNDECORATED)

                val mainUI = createMainUI(stage)
                mainUI.style = "-fx-font-size: ${ThemeConfig.fontSize.get()}px;"
                val scene = Scene(mainUI, LayoutManager.config.width, LayoutManager.config.height)

                CSSManager.applyAllStyles(scene)
                themeModule?.scenes?.add(scene)
                stage.scene = scene
                stage.title = "DataPack IDE"
                stage.width = LayoutManager.config.width
                stage.height = LayoutManager.config.height
                stage.isResizable = true

                if (LayoutManager.config.x > 0 && LayoutManager.config.y > 0) {
                    stage.x = LayoutManager.config.x
                    stage.y = LayoutManager.config.y
                } else {
                    stage.centerOnScreen()
                }

                stage.setOnCloseRequest { e ->
                    e.consume()
                    hideMainWindow()
                }

                stage.focusedProperty().addListener { _, _, focused ->
                    WorkspaceManager.setWindowFocused(focused)
                }

                primaryStage = stage
                updateMainView()
                logger.info("Main IDE Window created with ResizeHandler (hidden)!")
            } catch (e: Exception) {
                logger.error("Failed to create main window: ${e.message}", e)
            }
        }

        fun hideMainWindow() {
            JavaFXInitializer.runLater {
                primaryStage?.takeIf { it.isShowing }?.let { stage ->
                    saveCurrentLayout()
                    stage.hide()
                    logger.debug("Main IDE Window hidden via hideMainWindow()!")
                }
            }
        }

        fun toggleMainWindow() {
            if (!fxInitialized) {
                logger.info("JavaFX not initialized yet, initializing...")
                initializeJavaFX()
                return
            }
            Platform.runLater {
                if (primaryStage == null) {
                    createMainWindow()
                }
                primaryStage?.let { stage ->
                    if (stage.isShowing) {
                        logger.info("Window is showing, hiding it...")
                        stage.hide()
                        logger.info("Main IDE Window hidden!")
                    } else {
                        stage.show()
                        stage.toFront()
                        textEditorView?.requestFocus()
                        logger.info("Main IDE Window shown from hidden!")
                    }
                }
            }
        }
    }
}