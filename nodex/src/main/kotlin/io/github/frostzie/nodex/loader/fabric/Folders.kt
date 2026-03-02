package io.github.frostzie.nodex.loader.fabric

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

/**
 * Simple wrapper to not need to depend on fabric
 */
object Folders {
    val configDir: Path
        get() = FabricLoader.getInstance().configDir
}