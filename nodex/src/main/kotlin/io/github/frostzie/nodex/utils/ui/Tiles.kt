package io.github.frostzie.nodex.utils.ui

import atlantafx.base.controls.ToggleSwitch
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.paint.Color
import javafx.scene.layout.Background.EMPTY
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
//TODO: cleanup eventually and add new config toggles and tiles
class Tiles {
    class DefaultTile(title: String, description: String?, toggleSwitch: ToggleSwitch) : HBox() {

        private val titleLabel = Label(title).apply {
            isWrapText = true
            styleClass.add("title")
        }
        private val descriptionLabel = Label(description).apply {
            isWrapText = true
            isVisible = !description.isNullOrBlank()
            isManaged = !description.isNullOrBlank()
            styleClass.add("description")
        }

        private val textContainer = VBox(titleLabel, descriptionLabel).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("text-container")
        }

        private val hoveredBackground = Background(BackgroundFill(Color.gray(0.5, 0.1), CornerRadii(4.0), Insets.EMPTY))

        init {
            styleClass.add("tile")
            alignment = Pos.CENTER_LEFT
            spacing = 10.0
            padding = Insets(10.0)

            textContainer.prefWidth = 600.0

            children.addAll(textContainer, toggleSwitch)

            addEventHandler(MouseEvent.MOUSE_ENTERED) {
                background = hoveredBackground
            }
            addEventHandler(MouseEvent.MOUSE_EXITED) {
                background = EMPTY
            }
        }
    }

    class LargeTile(title: String, description: String?, controlNode: Node) : HBox() {

        private val titleLabel = Label(title).apply {
            isWrapText = true
            styleClass.add("title")
        }
        private val descriptionLabel = Label(description).apply {
            isWrapText = true
            isVisible = !description.isNullOrBlank()
            isManaged = !description.isNullOrBlank()
            styleClass.add("description")
        }

        private val textContainer = VBox(titleLabel, descriptionLabel).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("text-container")
        }

        private val hoveredBackground = Background(BackgroundFill(Color.gray(0.5, 0.1), CornerRadii(4.0), Insets.EMPTY))

        init {
            styleClass.add("tile")
            alignment = Pos.CENTER_LEFT
            spacing = 10.0
            padding = Insets(10.0)

            // Made button alight to right.
            val spacer = Region()
            setHgrow(spacer, Priority.ALWAYS)

            children.addAll(textContainer, spacer, controlNode)

            if (controlNode is ComboBox<*>) {
                controlNode.prefWidth = 150.0
            }

            addEventHandler(MouseEvent.MOUSE_ENTERED) {
                background = hoveredBackground
            }
            addEventHandler(MouseEvent.MOUSE_EXITED) {
                background = EMPTY
            }
        }
    }

    class LowTile(title: String, description: String?, controlNode: Node) : HBox() {

        private val titleLabel = Label(title).apply {
            isWrapText = true
            styleClass.add("title")
        }
        private val descriptionLabel = Label(description).apply {
            isWrapText = true
            isVisible = !description.isNullOrBlank()
            isManaged = !description.isNullOrBlank()
            styleClass.add("description")
        }

        private val textContainer = VBox(titleLabel, descriptionLabel).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("text-container")
        }

        private val hoveredBackground = Background(BackgroundFill(Color.gray(0.5, 0.1), CornerRadii(4.0), Insets.EMPTY))

        init {
            styleClass.add("tile")
            alignment = Pos.TOP_LEFT
            spacing = 10.0
            padding = Insets(10.0)

            val stackedContent = VBox(textContainer, controlNode).apply {
                spacing = 5.0
                VBox.setVgrow(controlNode, Priority.ALWAYS)
            }

            setHgrow(stackedContent, Priority.ALWAYS)

            if (controlNode is TextArea) {
                controlNode.prefWidth = 400.0
                controlNode.prefHeight = 100.0
            }

            children.addAll(stackedContent)

            addEventHandler(MouseEvent.MOUSE_ENTERED) {
                background = hoveredBackground
            }
            addEventHandler(MouseEvent.MOUSE_EXITED) {
                background = EMPTY
            }
        }
    }

    class InfoTile(title: String, description: String?) : HBox() {

        private val titleLabel = Label(title).apply {
            isWrapText = true
            styleClass.add("title")
        }
        private val descriptionLabel = Label(description).apply {
            isWrapText = true
            isVisible = !description.isNullOrBlank()
            isManaged = !description.isNullOrBlank()
            styleClass.add("description")
        }

        private val textContainer = VBox(titleLabel, descriptionLabel).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("text-container")
        }

        private val hoveredBackground = Background(BackgroundFill(Color.gray(0.5, 0.1), CornerRadii(4.0), Insets.EMPTY))

        init {
            styleClass.add("tile")
            alignment = Pos.CENTER_LEFT
            spacing = 10.0
            padding = Insets(10.0)

            setHgrow(textContainer, Priority.ALWAYS)

            children.addAll(textContainer)

            addEventHandler(MouseEvent.MOUSE_ENTERED) {
                background = hoveredBackground
            }
            addEventHandler(MouseEvent.MOUSE_EXITED) {
                background = EMPTY
            }
        }
    }
}