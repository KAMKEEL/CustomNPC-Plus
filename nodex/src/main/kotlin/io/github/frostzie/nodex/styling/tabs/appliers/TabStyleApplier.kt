package io.github.frostzie.nodex.styling.tabs.appliers

import atlantafx.base.controls.Tab
import io.github.frostzie.nodex.styling.tabs.TabStyle
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.paint.Color

/**
 * A singleton object responsible for applying a [TabStyle] to the JavaFX nodes of an editor tab.
 */
object TabStyleApplier {

    /**
     * Applies the given style to the visual components of a tab.
     *
     * @param tab The target [Tab] whose graphic will be styled.
     * @param style The [TabStyle] containing the properties to apply.
     */
    fun apply(tab: Tab, style: TabStyle) {
        val graphic = tab.graphic as? HBox ?: return
        val label = graphic.children.find { it is Label } as? Label ?: return

        // Text Color
        val textColor = style.textColor
        if (textColor != null && !textColor.startsWith("-")) {
            label.textFill = Color.valueOf(textColor)
        }

        // Underline
        label.isUnderline = style.isUnderline ?: false

        // Background Color
        // We apply the background color to the Tab itself, not the graphic.
        // Applying to 'graphic' only colors the content box (label area), leaving gaps.
        
        // To avoid overwriting the structural background (e.g., the top line/border used by AtlantaFX),
        // we set the CSS variable '-color-bg-default' instead of '-fx-background-color'.
        if (style.backgroundColor != null) {
            tab.style = "-color-bg-default: ${style.backgroundColor};"
        } else {
            tab.style = ""
        }
        
        // Ensure the graphic doesn't have a conflicting background (though it should be empty/transparent by default)
        graphic.background = Background.EMPTY

        // CSS for Font Properties
        val fontCss = buildString {
            val fontWeight = if (style.isBold == true) "bold" else "normal"
            append("-fx-font-weight: $fontWeight;")

            val fontStyle = if (style.isItalic == true) "italic" else "normal"
            append("-fx-font-style: $fontStyle;")
            
            // Re-apply text fill via CSS if it was a variable (e.g. -color-fg-default)
            if (textColor != null && textColor.startsWith("-")) {
                append("-fx-text-fill: $textColor;")
            }
        }

        label.style = fontCss
    }
}