package io.github.frostzie.nodex.handlers.bars

import io.github.frostzie.nodex.events.EditorCursorPosition
import io.github.frostzie.nodex.modules.bars.BottomBarModule
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent

@Suppress("unused")
class BottomBarHandler(private val bottomBarModule: BottomBarModule) {
    @SubscribeEvent
    fun onCursorPositionChanged(event: EditorCursorPosition) {
        bottomBarModule.updateCursorPosition(event.line, event.column)
    }
}