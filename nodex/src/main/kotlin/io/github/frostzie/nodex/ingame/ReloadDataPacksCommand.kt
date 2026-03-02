package io.github.frostzie.nodex.ingame

import io.github.frostzie.nodex.loader.minecraft.MCInterface
import io.github.frostzie.nodex.utils.LoggerProvider

object ReloadDataPacksCommand {

    private val logger = LoggerProvider.getLogger("ReloadDataPacksCommand")

    /**
     * Executes the `/reload` command in Minecraft.
     */
    fun reload() {
        MCInterface.sendCommand("reload")
        logger.debug("Sent /reload with button press")
    }

    /**
     * Placeholder for executing the `/reload` command via a hotkey.
     */
    fun executeCommandHotKey() {
        //TODO: Add a hotkey to execute the command
    }
}