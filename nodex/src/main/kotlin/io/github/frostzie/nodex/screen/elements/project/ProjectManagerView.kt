package io.github.frostzie.nodex.screen.elements.project

import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.MainWindowClose
import io.github.frostzie.nodex.events.SettingsWindowOpen
import io.github.frostzie.nodex.modules.project.ProjectManagerViewModel
import io.github.frostzie.nodex.project.Project
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.utils.IconUtils
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.file.DirectoryChooseUtils
import io.github.frostzie.nodex.utils.OpenLinks
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Hyperlink
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.Separator
import javafx.scene.control.Tooltip
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ

class ProjectManagerView : BorderPane() {
    private val logger = LoggerProvider.getLogger("ProjectManagerView")
    private val viewModel = ProjectManagerViewModel()
    
    // Exposed for WindowDrag
    val dragTarget: VBox

    init {
        styleClass.add("start-screen")

        // Header: Name + Window Controls
        dragTarget = createHeader()
        top = dragTarget

        // Left Side: Recent Projects
        val recentPane = createRecentProjectsPane()
        left = recentPane
        setMargin(recentPane, Insets(20.0))

        // Center: Actions
        val actionsPane = createActionsPane()
        center = actionsPane
        setMargin(actionsPane, Insets(20.0))
    }

    private fun createHeader(): VBox {
        val root = VBox()

        val hBox = HBox(10.0)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.styleClass.add("start-screen-header")
        hBox.padding = Insets(10.0)
        hBox.minHeight = 40.0

        val logo = IconUtils.createIcon(IconSource.SvgIcon("/assets/nodex/svg/icon.svg", 40))

        val appTitle = Label("DataPack IDE")
        appTitle.styleClass.add(Styles.TITLE_3)

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        val closeBtn = Button()
        closeBtn.graphic = FontIcon(Material2AL.CLOSE)
        closeBtn.styleClass.addAll(Styles.FLAT, Styles.DANGER)
        closeBtn.tooltip = Tooltip("Close")
        closeBtn.setOnAction {
            EventBus.post(MainWindowClose()) 
        }

        val settingsBtn = Button()
        settingsBtn.graphic = FontIcon(Material2MZ.SETTINGS)
        settingsBtn.styleClass.addAll((Styles.FLAT))
        settingsBtn.tooltip = Tooltip("Settings")
        settingsBtn.setOnAction {
            EventBus.post(SettingsWindowOpen())
        }

        hBox.children.addAll(logo, appTitle, spacer, settingsBtn, closeBtn)
        
        root.children.addAll(hBox, Separator())
        return root
    }

    private fun createRecentProjectsPane(): VBox {
        val vbox = VBox(10.0)
        vbox.prefWidth = 300.0
        vbox.styleClass.add("recent-projects-pane")

        val title = Label("Recent Projects")
        title.styleClass.add(Styles.TITLE_3)

        val listView = ListView<Project>()
        listView.items = viewModel.recentProjects
        listView.styleClass.add("edge-to-edge")
        listView.fixedCellSize = 60.0
        listView.placeholder = Label("No recent projects").apply { styleClass.add(Styles.TEXT_MUTED) }
        VBox.setVgrow(listView, Priority.ALWAYS)

        listView.setCellFactory { RecentProjectListCell() }

        // Hide scrollbars
        listView.skinProperty().addListener { _, _, _ ->
            Platform.runLater {
                listView.lookupAll(".scroll-bar").forEach {
                    it.style = "-fx-opacity: 0; -fx-padding: -10;"
                }
            }
        }

        listView.setOnMouseClicked { event ->
            if (event.clickCount == 2) {
                val selected = listView.selectionModel.selectedItem
                if (selected != null) {
                    viewModel.openProject(selected)
                }
            }
        }

        vbox.children.addAll(title, listView)
        return vbox
    }

    private fun createActionsPane(): VBox {
        val vBox = VBox(20.0)
        vBox.styleClass.add("actions-pane")
        vBox.alignment = Pos.CENTER

        val topSpacer = Region()
        VBox.setVgrow(topSpacer, Priority.ALWAYS)

        val actionsBox = VBox(10.0)
        actionsBox.alignment = Pos.CENTER
        actionsBox.styleClass.add("actions-box")
        actionsBox.maxWidth = 300.0

        val newProjectBtn = createActionButton("New Project", Material2AL.ADD) {
             // TODO: Open New Project
             logger.info("New Project clicked (Not Implemented)")
        }
        newProjectBtn.isDisable = true

        val openButtonsBox = HBox(10.0)
        openButtonsBox.alignment = Pos.CENTER

        val openFolderBtn = Button("Folder")
        openFolderBtn.graphic = FontIcon(Material2AL.FOLDER_OPEN)
        openFolderBtn.styleClass.add(Styles.ACCENT)
        openFolderBtn.prefWidth = 95.0
        openFolderBtn.prefHeight = 40.0
        openFolderBtn.setOnAction {
            DirectoryChooseUtils.promptOpenProject(scene.window)
        }

        val openZipBtn = Button("Zip")
        openZipBtn.graphic = FontIcon(Material2AL.ARCHIVE)
        openZipBtn.styleClass.add(Styles.ACCENT)
        openZipBtn.prefWidth = 95.0
        openZipBtn.prefHeight = 40.0
        openZipBtn.setOnAction {
            DirectoryChooseUtils.promptOpenZip(scene.window)
        }
        openButtonsBox.children.addAll(openFolderBtn, openZipBtn)

        val openWorldBtn = createActionButton("Open from World", Material2MZ.PUBLIC) {
             logger.info("Open from World clicked (Not Implemented)")
        }
        openWorldBtn.isDisable = true

        actionsBox.children.addAll(newProjectBtn, openButtonsBox, openWorldBtn)

        // TODO: Implement more tips (just filling the empty space a bit)
        val tipLabel = Label("Tip: There will be tips soon!")
        tipLabel.styleClass.add(Styles.TEXT_MUTED)

        val bottomSpacer = Region()
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS)

        val footer = createFooter()

        vBox.children.addAll(topSpacer, actionsBox, tipLabel, bottomSpacer, footer)
        return vBox
    }

    private fun createFooter(): HBox {
        val hBox = HBox(15.0)
        hBox.alignment = Pos.CENTER
        hBox.padding = Insets(10.0)

        val github = Hyperlink("GitHub").apply { setOnAction { OpenLinks.gitHubLink() } }
        val discord = Hyperlink("Discord").apply { setOnAction { OpenLinks.discordLink() } }
        val issues = Hyperlink("Report Bug").apply { setOnAction { OpenLinks.reportBugLink() } }
        val modrinth = Hyperlink("Modrinth").apply { setOnAction { OpenLinks.modrinthLink() } }
        val buyMeACoffee = Hyperlink("Buy Me a Coffee").apply { setOnAction { OpenLinks.buyMeACoffeeLink() } }

        hBox.children.addAll(github, discord, modrinth, buyMeACoffee, issues)
        return hBox
    }

    private fun createActionButton(text: String, iconCode: Ikon, action: () -> Unit): Button {
        val btn = Button(text)
        btn.graphic = FontIcon(iconCode)
        btn.styleClass.add(Styles.ACCENT)
        btn.prefWidth = 200.0
        btn.prefHeight = 40.0
        btn.setOnAction { action() }
        return btn
    }
}