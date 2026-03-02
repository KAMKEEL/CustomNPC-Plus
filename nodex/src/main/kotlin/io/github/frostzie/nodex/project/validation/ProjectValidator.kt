package io.github.frostzie.nodex.project.validation

import io.github.frostzie.nodex.project.metadata.DatapackMetadata
import io.github.frostzie.nodex.project.metadata.DatapackParser
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

sealed class ValidationResult {
    data class ValidSingle(val path: Path, val metadata: DatapackMetadata) : ValidationResult()
    data class ValidZip(val path: Path, val metadata: DatapackMetadata) : ValidationResult()
    data class ValidWorkspace(val root: Path, val projects: List<Path>) : ValidationResult()
    data class Invalid(val path: Path, val reason: String) : ValidationResult()
}

object ProjectValidator {
    private val logger = LoggerProvider.getLogger("ProjectValidator")

    /**
     * Validates if a path is a valid Datapack Project or a Workspace containing Datapacks.
     */
    fun validate(path: Path): ValidationResult {
        if (!Files.exists(path)) {
            return ValidationResult.Invalid(path, "Path does not exist.")
        }

        // Zip
        if (path.extension.lowercase() == "zip") {
            return validateZip(path)
        }

        // Single Datapack (Root has pack.mcmeta)
        val metadata = DatapackParser.parse(path)
        if (metadata != null) {
            return ValidationResult.ValidSingle(path, metadata)
        }

        // Workspace Detection (1 level deep by default)
        if (path.isDirectory()) {
            val subProjects = mutableListOf<Path>()
            try {
                val children = path.listDirectoryEntries()
                
                // Safety check for massive directories
                if (children.size > 2000) {
                     return ValidationResult.Invalid(path, "Directory contains too many items (${children.size}). Please select a specific folder.")
                }

                for (child in children) {
                    if (child.isDirectory() && !child.name.startsWith(".")) {
                        if (DatapackParser.parse(child) != null) {
                            subProjects.add(child)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to scan directory: $path", e)
                return ValidationResult.Invalid(path, "Failed to read directory.")
            }

            if (subProjects.isNotEmpty()) {
                return ValidationResult.ValidWorkspace(path, subProjects)
            }
        }

        return ValidationResult.Invalid(path, "No 'pack.mcmeta' found in this folder or its immediate subfolders.")
    }

    private fun validateZip(path: Path): ValidationResult {
        return try {
            FileSystems.newFileSystem(path, null as ClassLoader?).use { fs ->
                val root = fs.getPath("/")
                val metadata = DatapackParser.parse(root)
                if (metadata != null) {
                    ValidationResult.ValidZip(path, metadata)
                } else {
                    ValidationResult.Invalid(path, "Zip file does not contain a valid 'pack.mcmeta' at the root.")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to open zip: $path", e)
            ValidationResult.Invalid(path, "Failed to open zip file.")
        }
    }
}
