package io.github.frostzie.nodex.screen.elements.bars

import atlantafx.base.theme.Styles
import io.github.frostzie.nodex.events.ToggleFileTree
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.UIConstants
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.feather.Feather
import org.kordamp.ikonli.javafx.FontIcon

class LeftBarView : ToolBar() {

    companion object {
        private val logger = LoggerProvider.getLogger("LeftBarView")
    }

    init {
        setupLeftBar()

        val directoryChooseButton = createLeftBarButton(
            Feather.FOLDER,
            "Toggle File Tree"
        ) {
            EventBus.post(ToggleFileTree())
        }

        val searchButton = createLeftBarButton(
            Feather.SEARCH,
            "Search"
        ) {
            logger.warn("Search button not implemented yet!")
        }.apply {
            isDisable = true
        }

        val spacer = Region().apply {
            VBox.setVgrow(this, Priority.ALWAYS)
            styleClass.add("left-bar-spacer")
        }

        items.addAll(directoryChooseButton, searchButton, spacer)
    }

    private fun setupLeftBar() {
        styleClass.add("left-bar")
        orientation = Orientation.VERTICAL
        prefWidth = UIConstants.LEFT_BAR_WIDTH
        minWidth = UIConstants.LEFT_BAR_WIDTH
        maxWidth = UIConstants.LEFT_BAR_WIDTH
        logger.debug("Left bar initialized")
    }

    private fun createLeftBarButton(icon: Ikon, tooltipText: String, vararg styleClasses: String, action: () -> Unit): Button {
        return Button().apply {
            graphic = FontIcon(icon)
            tooltip = Tooltip(tooltipText)
            styleClass.addAll(Styles.FLAT, *styleClasses)

            val size = UIConstants.TOP_BAR_BUTTON_SIZE
            prefWidth = size
            minWidth = size
            maxWidth = size
            prefHeight = size
            minHeight = size
            maxHeight = size

            setOnAction { action() }
        }
    }
}