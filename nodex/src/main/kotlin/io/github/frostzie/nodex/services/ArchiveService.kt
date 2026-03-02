package io.github.frostzie.nodex.services

import io.github.frostzie.nodex.utils.LoggerProvider
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipFile

object ArchiveService {
    private val logger = LoggerProvider.getLogger("ArchiveService")

    fun unzip(zipPath: Path, targetDir: Path): Path {
        val folderName = zipPath.fileName.toString().removeSuffix(".zip")
        val finalDir = targetDir.resolve(folderName)

        if (Files.exists(finalDir)) {
            logger.info("Deleting existing directory for override: $finalDir")
            try {
                deleteDirectory(finalDir)
            } catch (e: Exception) {
                logger.error("Failed to delete directory: $finalDir", e)
                throw IOException("Failed to delete existing project folder. Please ensure it is not open in another program.", e)
            }
        }
        
        // Small delay to ensure OS releases handles (Windows fix)
        try { Thread.sleep(50) } catch (_: InterruptedException) {}

        Files.createDirectories(finalDir)

        logger.info("Unzipping $zipPath to $finalDir")
        ZipFile(zipPath.toFile()).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                // Prevent Zip Slip
                val entryPath = finalDir.resolve(entry.name).normalize()
                if (!entryPath.startsWith(finalDir)) {
                    throw IOException("Zip entry is outside of the target dir: ${entry.name}")
                }
                
                if (entry.isDirectory) {
                    Files.createDirectories(entryPath)
                } else {
                    if (entryPath.parent != null && !Files.exists(entryPath.parent)) {
                        Files.createDirectories(entryPath.parent)
                    }
                    Files.copy(zip.getInputStream(entry), entryPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
        return finalDir
    }

    private fun deleteDirectory(path: Path) {
        if (!Files.exists(path)) return
        
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (exc != null) throw exc
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }
}