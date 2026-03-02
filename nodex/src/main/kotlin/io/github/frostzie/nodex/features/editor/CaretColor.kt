package io.github.frostzie.nodex.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.settings.categories.MainConfig
import javafx.beans.value.ChangeListener
import org.fxmisc.richtext.CodeArea

/**
 * An implementation of [EditorTabDecorator] that changes the caret color
 * based on user settings.
 */
class CaretColor : EditorTabDecorator {

    override fun decorate(tab: Tab, codeArea: CodeArea, tabData: TextEditorViewModel.TabData): () -> Unit {
        val configListener = ChangeListener<Any> { _, _, _ ->
            // Only update if it has focus.
            if (codeArea.isFocused) {
                updateCaretColor(codeArea)
            }
        }

        val focusListener = ChangeListener<Boolean> { _, _, isFocused ->
            if (isFocused) {
                updateCaretColor(codeArea)
            }
        }

        MainConfig.enableCaretColor.addListener(configListener)
        MainConfig.caretColor.addListener(configListener)
        codeArea.focusedProperty().addListener(focusListener)

        // Set initial color if it's already focused (e.g. first tab)
        if (codeArea.isFocused) {
            updateCaretColor(codeArea)
        }

        // Return cleanup function
        return {
            MainConfig.enableCaretColor.removeListener(configListener)
            MainConfig.caretColor.removeListener(configListener)
            codeArea.focusedProperty().removeListener(focusListener)
        }
    }

    private fun updateCaretColor(codeArea: CodeArea) {
        val caret = codeArea.lookup(".caret")
        if (MainConfig.enableCaretColor.get()) {
            val color = MainConfig.caretColor.get()
            caret?.style = "-fx-stroke: $color;"
        } else {
            caret?.style = ""
        }
    }
}