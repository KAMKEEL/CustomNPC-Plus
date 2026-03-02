package io.github.frostzie.nodex.styling.tabs.rules

import io.github.frostzie.nodex.config.ConfigManager
import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.styling.common.StylePriority
import io.github.frostzie.nodex.styling.tabs.TabStyle
import io.github.frostzie.nodex.styling.tabs.TabStyleRule

/**
 * A style rule that applies a distinct visual style to tabs representing
 * files inside the Datapack-ide config folder
 */
class ConfigRule : TabStyleRule {
    override val priority: Int = StylePriority.SPECIFIC_RULES

    private val configDirPath = ConfigManager.configDir.toAbsolutePath().toString()

    override fun appliesTo(context: TextEditorViewModel.TabData): Boolean {
        val filePath = context.filePath.toAbsolutePath().toString()
        return filePath.startsWith(configDirPath)
    }

    override fun getStyle(context: TextEditorViewModel.TabData): TabStyle {
        // Using a hardcoded color for now. Might allow changing in configs later on.
        return TabStyle(textColor = "#ffffff", isItalic = true, backgroundColor = "#755709")
    }
}