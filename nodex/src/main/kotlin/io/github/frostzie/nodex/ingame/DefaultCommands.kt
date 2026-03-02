package io.github.frostzie.nodex.ingame

import io.github.frostzie.nodex.loader.fabric.CommandRegistration
import io.github.frostzie.nodex.loader.minecraft.MCInterface
import io.github.frostzie.nodex.modules.universal.UniversalPackManager
import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.screen.MainApplication
import io.github.frostzie.nodex.settings.categories.MainConfig

object DefaultCommands {
    fun registerCommands() {
        CommandRegistration.register("nodex") {
            executes {
                MainApplication.showMainWindow()
                1
            }

            // Mirrors current project to world. //TODO: Support any universal project mirroring
            literal("internal") {
                literal("mirror_current") {
                    executes {
                        if (!MainConfig.universalFolderToggle.get() || !MainConfig.universalDatapackToggle.get()) return@executes 0

                        val root = WorkspaceManager.currentWorkspaceRoot
                        if (root != null && UniversalPackManager.isUniversalProject(root)) {
                            if (UniversalPackManager.mirrorToWorld(root)) {
                                // Execute reload to apply changes
                                MCInterface.sendCommand("reload")
                            }
                        }
                        1
                    }
                }
            }
        }
    }
}