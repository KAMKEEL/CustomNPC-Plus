package io.github.frostzie.nodex.utils.ui.controls

import io.github.frostzie.nodex.settings.KeyCombination
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 * A custom button that listens for and captures a [KeyCombination].
 * The captured combination is stored in the [keybindProperty].
 */
class KeybindInputButton : Button() {

    val keybindProperty = SimpleObjectProperty<KeyCombination>()

    private var isListening = false

    private val defaultFontSize = 15.0
    private val minFontSize = 8.0

    init {
        styleClass.add("keybind-input-button")

        keybindProperty.addListener { _, _, newKeybind ->
            text = newKeybind?.toString() ?: "None"
            adjustFontSize()
        }

        text = keybindProperty.get()?.toString() ?: "None"
        adjustFontSize()

        // Not sure if this is the best way of doing it but didn't find much else.
        // Set a preferred width to prevent the button from resizing with the text.
        // We'll compute it once based on a reasonably long string.
        val tempText = Text("Ctrl + Alt + Shift + W")
        tempText.font = Font.font(font.family, defaultFontSize)
        prefWidth = tempText.layoutBounds.width + padding.left + padding.right + 10 // buffer

        widthProperty().addListener { _, _, _ -> adjustFontSize() }

        setOnAction { event ->
            if (!isListening) {
                startListening()
            }
        }

        setOnKeyPressed { event ->
            if (isListening) {
                event.consume()

                if (event.code == KeyCode.ESCAPE) {
                    stopListening(false)
                    return@setOnKeyPressed
                }

                if (event.code.isModifierKey || event.code == KeyCode.UNDEFINED || event.code == KeyCode.CAPS) {
                    return@setOnKeyPressed
                }

                val newKeybind = KeyCombination.fromEvent(event)
                keybindProperty.set(newKeybind)
                stopListening(true)
            }
        }

        focusedProperty().addListener { _, _, hasFocus ->
            if (!hasFocus && isListening) {
                stopListening(false)
            }
        }
    }

    private fun startListening() {
        isListening = true
        text = "Press any key..."
        adjustFontSize()
        styleClass.add("listening")
    }

    private fun stopListening(wasCompleted: Boolean) {
        isListening = false

        if (!wasCompleted) {
            text = keybindProperty.get()?.toString() ?: "None"
        }
        adjustFontSize()
        styleClass.remove("listening")
    }

    private fun adjustFontSize() {
        val currentText = text
        if (currentText.isNullOrEmpty()) {
            font = Font.font(defaultFontSize) // Reset to default if no text
            return
        }

        var newFontSize = defaultFontSize
        val tempTextNode = Text(currentText)

        // Iterate to find a fitting font size
        while (newFontSize >= minFontSize) {
            tempTextNode.font = Font.font(font.family, newFontSize)
            // Subtract padding from width for accurate text fitting
            if (tempTextNode.layoutBounds.width <= width - (padding.left + padding.right)) {
                break
            }
            newFontSize -= 0.5
        }

        font = Font.font(font.family, newFontSize)
    }
}