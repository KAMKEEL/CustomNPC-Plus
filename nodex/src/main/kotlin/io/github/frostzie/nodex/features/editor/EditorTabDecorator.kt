package io.github.frostzie.nodex.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import org.fxmisc.richtext.CodeArea

/**
 * Defines a contract for features that wish to decorate or add behavior to an editor tab.
 */
interface EditorTabDecorator {
    /**
     * Applies decorations to the given tab based on the tab's data.
     *
     * @param tab The UI Tab control from AtlantaFX.
     * @param codeArea The CodeArea containing the text editor for this tab.
     * @param tabData The view model's data for this tab.
     * @return A cleanup function (lambda) that will be called when the tab is closed.
     *         This is for removing listeners and preventing memory leaks.
     */
    fun decorate(tab: Tab, codeArea: CodeArea, tabData: TextEditorViewModel.TabData): () -> Unit
}
