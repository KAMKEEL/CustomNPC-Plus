package io.github.frostzie.nodex.modules.bars

import io.github.frostzie.nodex.events.DirectorySelected
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.file.DirectoryChooseUtils
import javafx.stage.DirectoryChooser
import javafx.stage.Stage

class LeftBarModule(private val parentStage: Stage) {
    private val logger = LoggerProvider.getLogger("LeftBarModule")

    fun openDirectoryChooser() {
        val directoryChooser = DirectoryChooser().apply {
            try {
                val datapackPath = DirectoryChooseUtils.getDatapackPath()
                if (DirectoryChooseUtils.isSingleplayer() && datapackPath != null) {
                    initialDirectory = datapackPath.toFile()
                } else {
                    initialDirectory = DirectoryChooseUtils.getInstancePath()?.toFile()
                }
            } catch (e: Exception) {
                logger.warn("Could not set initial directory", e)
            }
        }

        val selectedDirectory = directoryChooser.showDialog(parentStage)
        if (selectedDirectory != null) {
            EventBus.post(DirectorySelected(selectedDirectory.toPath()))
        }
    }
}