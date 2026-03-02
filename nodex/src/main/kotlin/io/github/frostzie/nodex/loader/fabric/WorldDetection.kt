package io.github.frostzie.nodex.loader.fabric

import io.github.frostzie.nodex.loader.minecraft.MCInterface
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import java.nio.file.Path

object WorldDetection {
    /**
     * Check if player connected to a world
     */
    fun isWorldOpen(): Boolean {
        return MCInterface.isWorldOpen
    }

    /**
     * Check if player connected to singleplayer
     */
    fun isSingleplayer(): Boolean {
        return MCInterface.isSingleplayer
    }

    /**
     * Check if player connected to server
     */
    fun isServer(): Boolean {
        return MCInterface.isServer
    }

    /**
     * Get world path
     */
    fun getWorldPath(): Path? {
        return MCInterface.getWorldPath()
    }

    /**
     * Register a listener for world join events
     */
    fun registerWorldJoinListener(listener: () -> Unit) {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            listener()
        }
    }
}