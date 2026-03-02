package io.github.frostzie.nodex.screen.elements.bars

import io.github.frostzie.nodex.modules.bars.BottomBarModule
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.ToolBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region

class BottomBarView() : ToolBar() {

    private val cursorLabel = Label()
    private val encodingLabel = Label()
    private val ideVersionLabel = Label()

    init {
        styleClass.add("status-bar")
        createStatusElements()
        bindProperties()
    }

    private fun createStatusElements() {
        cursorLabel.styleClass.add("status-label")
        encodingLabel.styleClass.add("status-label")
        ideVersionLabel.styleClass.add("status-label")

        val spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
            styleClass.add("status-spacer")
        }

        items.addAll(
            cursorLabel,
            createSeparator(),
            encodingLabel,
            createSeparator(),
            spacer,
            ideVersionLabel
        )
    }

    private fun bindProperties() {
        cursorLabel.textProperty().bind(BottomBarModule.cursorPositionProperty)
        encodingLabel.textProperty().bind(BottomBarModule.encodingProperty)
        ideVersionLabel.textProperty().bind(BottomBarModule.ideVersionProperty)
    }

    private fun createSeparator(): Separator {
        return Separator(javafx.geometry.Orientation.VERTICAL).apply {
            styleClass.add("status-separator")
        }
    }
}