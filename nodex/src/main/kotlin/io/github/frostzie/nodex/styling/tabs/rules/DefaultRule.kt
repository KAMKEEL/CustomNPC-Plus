package io.github.frostzie.nodex.styling.tabs.rules

import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.styling.common.StylePriority
import io.github.frostzie.nodex.styling.tabs.TabStyle
import io.github.frostzie.nodex.styling.tabs.TabStyleRule

class DefaultRule : TabStyleRule {

    override val priority: Int = StylePriority.BASE

    /**
     * This rule applies to every tab, unconditionally.
     */
    override fun appliesTo(context: TextEditorViewModel.TabData): Boolean {
        return true
    }

    override fun getStyle(context: TextEditorViewModel.TabData): TabStyle {
        return TabStyle(
            textColor = "-color-fg-default",
            isBold = false,
            isItalic = false,
            isUnderline = false
        )
    }
}
