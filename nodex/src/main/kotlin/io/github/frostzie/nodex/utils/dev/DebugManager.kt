package io.github.frostzie.nodex.utils.dev

import io.github.frostzie.nodex.settings.categories.AdvancedConfig
import javafx.beans.property.BooleanProperty
import javafx.scene.layout.Pane

object DebugManager {
    private var isInitialized = false

    fun initialize(root: Pane) {
        if (isInitialized) return
        isInitialized = true
        applyStyleOnToggle(root, AdvancedConfig.debugLayoutBounds, "debug-layout")
        applyStyleOnToggle(root, AdvancedConfig.debugResizeHandles, "debug-resize-handles")
    }

    private fun applyStyleOnToggle(pane: Pane, property: BooleanProperty, styleClass: String) {
        property.addListener { _, _, newValue ->
            if (newValue) {
                pane.styleClass.add(styleClass)
            } else {
                pane.styleClass.remove(styleClass)
            }
        }
        if (property.get()) {
            pane.styleClass.add(styleClass)
        }
    }
}