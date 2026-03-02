package io.github.frostzie.nodex.modules.bars.top

import io.github.frostzie.nodex.ingame.ReloadDataPacksCommand
import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.MainWindowMaximizedStateChanged
import io.github.frostzie.nodex.modules.universal.UniversalPackManager
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.screen.MainApplication
import io.github.frostzie.nodex.settings.categories.MainConfig
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import javafx.stage.Stage
import net.minecraft.Util
import java.nio.file.Path

class TopBarViewModel(private val stage: Stage?) {

    private var previousBounds: Rectangle2D? = null
    var isMaximized: Boolean = false

    fun minimize() {
        stage?.isIconified = true
    }

    fun maximize() {
        stage?.let {
            val screen = Screen.getScreensForRectangle(it.x, it.y, it.width, it.height).firstOrNull() ?: Screen.getPrimary()
            val visualBounds = screen.visualBounds

            previousBounds = Rectangle2D(it.x, it.y, it.width, it.height)
            it.x = visualBounds.minX
            it.y = visualBounds.minY
            it.width = visualBounds.width
            it.height = visualBounds.height

            isMaximized = true
            EventBus.post(MainWindowMaximizedStateChanged(true))
        }
    }

    fun restore() {
        previousBounds?.let { bounds ->
            stage?.let { stg ->
                stg.x = bounds.minX
                stg.y = bounds.minY
                stg.width = bounds.width
                stg.height = bounds.height

                isMaximized = false
                EventBus.post(MainWindowMaximizedStateChanged(false))
            }
        }
    }

    fun toggleMaximize() {
        if (isMaximized) {
            restore()
        } else {
            maximize()
        }
    }

    fun hideWindow() {
        MainApplication.hideMainWindow()
    }

    fun reloadDatapacks() {
        val root = WorkspaceManager.currentWorkspaceRoot
        if (root != null && MainConfig.universalFolderToggle.get() && MainConfig.universalDatapackToggle.get()) {
            if (UniversalPackManager.isUniversalProject(root)) {
                UniversalPackManager.mirrorToWorld(root)
            }
        }
        ReloadDataPacksCommand.reload()
    }

    fun openWorkspaceFolder() {
        WorkspaceManager.currentWorkspaceRoot?.let {
            Util.getPlatform().openFile(it.toFile())
        }
    }

    fun importProject(path: Path) {
        WorkspaceManager.addProject(path)
    }
}