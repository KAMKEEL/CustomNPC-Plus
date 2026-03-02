package io.github.frostzie.nodex.handlers.popup.file

import io.github.frostzie.nodex.events.RequestFileOverride
import io.github.frostzie.nodex.events.RequestMoveConfirmation
import io.github.frostzie.nodex.modules.popup.file.FilePopupModule
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent

class FilePopupHandler(private val filePopupModule: FilePopupModule) {
    @SubscribeEvent
    fun onMoveFileRequested(event: RequestMoveConfirmation) {
        filePopupModule.showMoveConfirmation(event.sourcePath, event.targetPath)
    }

    @SubscribeEvent
    fun onFileOverrideRequested(event: RequestFileOverride) {
        filePopupModule.showFileOverrideDialog(event.sourcePath, event.targetPath)
    }
}
