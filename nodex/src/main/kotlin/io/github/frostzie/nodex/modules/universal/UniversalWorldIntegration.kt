package io.github.frostzie.nodex.modules.universal

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.WorkspaceUpdated
import io.github.frostzie.nodex.loader.fabric.WorldDetection
import io.github.frostzie.nodex.loader.minecraft.ChatMessageBuilder
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.settings.categories.MainConfig
import kotlin.io.path.name

object UniversalWorldIntegration {

    fun initialize() {
        EventBus.register(this)

        // Listen for when the player joins a world
        WorldDetection.registerWorldJoinListener {
            checkAndPrompt()
        }
    }

    @SubscribeEvent
    fun onWorkspaceUpdated(event: WorkspaceUpdated) {
        // Listen for when the user switches projects while already in a world
        checkAndPrompt()
    }

    private fun checkAndPrompt() {
        if (!WorldDetection.isSingleplayer()) return

        if (!MainConfig.universalFolderToggle.get() || !MainConfig.universalDatapackToggle.get()) return

        val root = WorkspaceManager.currentWorkspaceRoot ?: return

        // Only prompt if it is a Universal Project
        if (UniversalPackManager.isUniversalProject(root)) {
            ChatMessageBuilder.promptUniversalLoad(root.name)
        }
    }
}