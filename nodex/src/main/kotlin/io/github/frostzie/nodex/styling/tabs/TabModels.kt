package io.github.frostzie.nodex.styling.tabs

import io.github.frostzie.nodex.modules.main.TextEditorViewModel
import io.github.frostzie.nodex.styling.common.DynamicStyle
import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.styling.common.StyleRule

/**
 * A typealias for a [StyleRule] specifically for styling text editor tabs.
 * It uses [TextEditorViewModel.TabData] as its context and [TabStyle] as its style model.
 */
typealias TabStyleRule = StyleRule<TextEditorViewModel.TabData, TabStyle>

/**
 * A data class representing the visual style properties specific to an editor tab.
 *
 * @property textColor The color of the tab's text.
 * @property backgroundColor The background color of the tab's main content area.
 * @property isBold Whether the tab's text should be bold.
 * @property isItalic Whether the tab's text should be italic.
 * @property isUnderline Whether to add an underline to the tab's text.
 * @property iconSource The source for the tab's icon (e.g., Ikonli or SVG).
 * @property prefix A string to prepend to the tab's label.
 * @property isFinal A "lock" from being overridden by lower-priority rules.
 */
data class TabStyle(
    val textColor: String? = null,
    val backgroundColor: String? = null,
    val isBold: Boolean? = null,
    val isItalic: Boolean? = null,
    val isUnderline: Boolean? = null,
    val iconSource: IconSource? = null,
    val prefix: String? = null,
    val isFinal: Boolean = false
) : DynamicStyle