package io.github.frostzie.nodex.features.editor

import atlantafx.base.controls.Tab
import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.settings.categories.MainConfig
import javafx.beans.value.ChangeListener
import javafx.scene.layout.HBox
import org.fxmisc.richtext.CodeArea
import org.kordamp.ikonli.javafx.FontIcon

/**
 * An implementation of [EditorTabDecorator] that toggles the visibility of the file
 * icon in a tab based on the "Show File Icons" setting.
 */
class FileIconDecorator : EditorTabDecorator {
    override fun decorate(tab: Tab, codeArea: CodeArea, tabData: TextEditorViewModel.TabData): () -> Unit {
        val graphic = tab.graphic as? HBox ?: return { }
        val icon = graphic.children.find { it is FontIcon } as? FontIcon ?: return { }

        val listener = ChangeListener { _, _, show ->
            icon.isVisible = show
            icon.isManaged = show
        }
        MainConfig.showFileIcons.addListener(listener)

        val initialVisibility = MainConfig.showFileIcons.get()
        icon.isVisible = initialVisibility
        icon.isManaged = initialVisibility

        return {
            MainConfig.showFileIcons.removeListener(listener)
        }
    }
}
