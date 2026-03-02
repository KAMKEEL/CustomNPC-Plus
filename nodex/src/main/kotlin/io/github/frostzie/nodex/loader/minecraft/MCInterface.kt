package io.github.frostzie.nodex.loader.minecraft

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path

/**
 * Bridge to allow the main project to interact with Minecraft
 * without direct dependency on its classes.
 */
object MCInterface {
    /**
     * Sends a command as the current player.
     */
    fun sendCommand(command: String) {
        Minecraft.getInstance().player?.connection?.sendCommand(command)
    }

    /**
     * Executes a task on the main Minecraft thread.
     */
    fun runOnRenderThread(action: () -> Unit) {
        Minecraft.getInstance()?.execute(action)
    }

    /**
     * Displays a client-side chat message to the player.
     */
    fun displayClientMessage(message: Component, actionBar: Boolean = false) {
        Minecraft.getInstance().player?.displayClientMessage(message, actionBar)
    }

    // World State Checks
    val isWorldOpen: Boolean get() = Minecraft.getInstance().level != null
    val isSingleplayer: Boolean get() = Minecraft.getInstance().singleplayerServer != null
    val isServer: Boolean get() = Minecraft.getInstance().currentServer != null

    /**
     * Gets the path to the current world (if in singleplayer).
     */
    fun getWorldPath(): Path? {
        return Minecraft.getInstance().singleplayerServer?.getWorldPath(LevelResource.ROOT)
    }

    /**
     * Gets the game directory path.
     */
    fun getGamePath(): Path? {
        return Minecraft.getInstance().gameDirectory?.toPath()
    }
}