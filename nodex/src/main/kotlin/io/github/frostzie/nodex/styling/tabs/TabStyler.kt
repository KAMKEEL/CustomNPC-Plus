package io.github.frostzie.nodex.styling.tabs

import atlantafx.base.controls.Tab
import io.github.frostzie.nodex.features.editor.EditorTabDecorator
import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.styling.tabs.appliers.TabStyleApplier
import javafx.beans.value.ChangeListener
import org.fxmisc.richtext.CodeArea

/**
 * An [EditorTabDecorator] that orchestrates the dynamic styling of an editor tab.
 *
 * This decorator acts as the link between the tab's data and the styling engine.
 * It listens for changes in the tab's state (e.g., its dirty status) and triggers
 * a re-evaluation of the styles, applying the result to the tab's UI nodes.
 */
class TabStyler : EditorTabDecorator {

    /**
     * Applies dynamic styling to the tab and sets up listeners for state changes.
     */
    override fun decorate(tab: Tab, codeArea: CodeArea, tabData: TextEditorViewModel.TabData): () -> Unit {
        val listener = ChangeListener<Any> { _, _, _ ->
            updateTabStyle(tab, tabData)
        }

        updateTabStyle(tab, tabData)

        tabData.isDirty.addListener(listener)
        // Add listeners for other relevant properties as new rules are created.

        return {
            tabData.isDirty.removeListener(listener)
            // Reset the style to default to ensure no styles linger if the tab is reused.
            TabStyleApplier.apply(tab, TabStyle())
        }
    }

    /**
     * Evaluates the styles for the given tab data and applies them to the tab's UI.
     */
    private fun updateTabStyle(tab: Tab, tabData: TextEditorViewModel.TabData) {
        val finalStyle = TabStyleManager.evaluate(tabData)
        TabStyleApplier.apply(tab, finalStyle)
    }
}
