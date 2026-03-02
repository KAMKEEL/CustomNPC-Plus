package io.github.frostzie.nodex.modules.universal

import io.github.frostzie.nodex.config.ConfigManager
import io.github.frostzie.nodex.loader.fabric.Folders
import io.github.frostzie.nodex.loader.fabric.WorldDetection
import io.github.frostzie.nodex.services.ArchiveService
import io.github.frostzie.nodex.utils.LoggerProvider
import io.github.frostzie.nodex.utils.file.DirectoryChooseUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.name

object UniversalPackManager {
    private val logger = LoggerProvider.getLogger("UniversalPackManager")

    // Get the root folder: .../Nodex/datapacks
    private val universalRoot: Path
        get() {
            return if (ConfigManager.configDir.endsWith("config")) {
                ConfigManager.configDir.parent.resolve("datapacks")
            } else {
                // Fallback if structure is different
                Folders.configDir.resolve("Nodex").resolve("datapacks")
            }
        }

    init {
        if (!universalRoot.exists()) {
            Files.createDirectories(universalRoot)
        }
    }

    /**
     * Checks if a specific project path is inside the Universal Folder.
     */
    fun isUniversalProject(path: Path): Boolean {
        // Ensure absolute paths for comparison
        return path.toAbsolutePath().startsWith(universalRoot.toAbsolutePath())
    }

    /**
     * Checks if a pack with this name already exists in the Universal Folder.
     */
    fun existsInUniversal(folderName: String): Boolean {
        return universalRoot.resolve(folderName).exists()
    }

    /**
     * Imports a project (Folder or Zip) into the Universal Folder.
     * @param source The original path (zip or folder).
     * @param targetName The name to use in the universal folder.
     * @return The new Path in the universal folder.
     */
    fun importProject(source: Path, targetName: String): Path {
        val targetDir = universalRoot.resolve(targetName)

        logger.info("Importing project from $source to $targetDir")

        if (Files.isDirectory(source)) {
            // Copy Directory
            copyDirectory(source, targetDir)
        } else {
            // Unzip
            ArchiveService.unzip(source, universalRoot)
            // ArchiveService.unzip uses the zip name. If renamed, need to rename the result.
            val unzippedPath = universalRoot.resolve(source.fileName.toString().removeSuffix(".zip"))
            if (unzippedPath != targetDir) {
                Files.move(unzippedPath, targetDir, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        return targetDir
    }

    /**
     * Mirrors the given project path to the current World's datapack folder.
     */
    fun mirrorToWorld(projectPath: Path): Boolean {
        if (!WorldDetection.isSingleplayer()) {
            logger.warn("Cannot mirror: Not in singleplayer.")
            return false
        }

        val datapacksDir = DirectoryChooseUtils.getDatapackPath()
        if (datapacksDir == null || !datapacksDir.exists()) {
            logger.error("Cannot find world datapacks directory.")
            return false
        }

        val targetDir = datapacksDir.resolve(projectPath.name)

        try {
            logger.debug("Mirroring {} to {}", projectPath, targetDir)

            // 1. Clean target if exists
            if (targetDir.exists()) {
                // Recursive delete
                targetDir.toFile().deleteRecursively()
            }

            // 2. Copy
            copyDirectory(projectPath, targetDir)

            return true
        } catch (e: Exception) {
            logger.error("Failed to mirror project to world.", e)
            return false
        }
    }

    private fun copyDirectory(source: Path, target: Path) {
        source.toFile().walkTopDown().forEach { sourceFile ->
            val relativePath = source.relativize(sourceFile.toPath())
            val targetFile = target.resolve(relativePath)

            if (sourceFile.isDirectory) {
                if (!targetFile.exists()) Files.createDirectories(targetFile)
            } else {
                Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}