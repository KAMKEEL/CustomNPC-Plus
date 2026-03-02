package io.github.frostzie.nodex.services

import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * A singleton service for performing file I/O operations.
 * Centralizes error handling and logging for filesystem interactions.
 */
object FileService {
    private val logger = LoggerProvider.getLogger("FileService")

    /**
     * Reads the content of a text file.
     * @throws Exception if reading fails.
     */
    fun readText(path: Path): String {
        try {
            return path.readText()
        } catch (e: Exception) {
            logger.error("Failed to read text from file: $path", e)
            throw e
        }
    }

    /**
     * Writes content to a text file.
     * @throws Exception if writing fails.
     */
    fun writeText(path: Path, content: String) {
        try {
            path.writeText(content)
        } catch (e: Exception) {
            logger.error("Failed to write text to file: $path", e)
            throw e
        }
    }

    /**
     * Moves a file or directory from source to target.
     * Tries atomic move first, falls back to standard copy-delete.
     * @throws Exception if the move fails.
     */
    fun move(source: Path, target: Path) {
        try {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: AtomicMoveNotSupportedException) {
                logger.warn("Atomic move not supported, falling back to standard move for: $source -> $target")
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
            }
            logger.info("Moved file: {} -> {}", source, target)
        } catch (e: Exception) {
            logger.error("Failed to move file from $source to $target", e)
            throw e
        }
    }

    /**
     * Deletes a file or directory (recursively not guaranteed here, standard delete).
     * @throws Exception if deletion fails.
     */
    fun delete(path: Path) {
        try {
            Files.delete(path)
            logger.info("Deleted file: {}", path)
        } catch (e: Exception) {
            logger.error("Failed to delete file: $path", e)
            throw e
        }
    }
    
    /**
     * Creates a directory.
     */
    fun createDirectory(path: Path) {
        try {
            Files.createDirectories(path)
            logger.info("Created directory: {}", path)
        } catch (e: Exception) {
            logger.error("Failed to create directory: $path", e)
            throw e
        }
    }

    /**
     * Lists directory entries safely.
     * @return List of paths, or empty list if failed.
     */
    fun listDirectory(path: Path): List<Path> {
        return try {
            path.listDirectoryEntries()
        } catch (e: Exception) {
            logger.error("Failed to list directory entries: $path", e)
            emptyList()
        }
    }

    /**
     * Checks if a path is a directory.
     */
    fun isDirectory(path: Path): Boolean {
        return try {
            path.isDirectory()
        } catch (_: Exception) {
            false
        }
    }
}
