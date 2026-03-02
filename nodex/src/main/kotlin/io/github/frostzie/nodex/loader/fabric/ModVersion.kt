package io.github.frostzie.nodex.loader.fabric

import net.fabricmc.loader.api.FabricLoader

object ModVersion {
    val current: String by lazy {
        FabricLoader.getInstance()
            .getModContainer("nodex")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }
}