package io.github.frostzie.nodex.styling.tabs.rules

import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.settings.categories.MainConfig
import io.github.frostzie.nodex.styling.common.StylePriority
import io.github.frostzie.nodex.styling.tabs.TabStyle
import io.github.frostzie.nodex.styling.tabs.TabStyleRule

/**
 * A style rule that applies a specific text color to editor tabs for files
 * that have unsaved changes (are "dirty").
 */
class DirtyStateRule : TabStyleRule {
    /**
     * Uses the standardized priority for default application rules.
     */
    override val priority: Int = StylePriority.DEFAULT_RULES

    /**
     * This rule applies only if the tab's underlying file is dirty.
     */
    override fun appliesTo(context: TextEditorViewModel.TabData): Boolean {
        return context.isDirty.get()
    }

    /**
     * Returns a [TabStyle] with the text color set to the user-configured 'dirty file color'.
     */
    override fun getStyle(context: TextEditorViewModel.TabData): TabStyle {
        return TabStyle(textColor = MainConfig.dirtyFileColor.get()) //TODO: Add bg color?
    }
}
