package io.github.frostzie.nodex.modules.project.preview

import io.github.frostzie.nodex.project.WorkspaceManager
import io.github.frostzie.nodex.project.metadata.DatapackMetadata
import io.github.frostzie.nodex.services.ArchiveService
import javafx.beans.property.SimpleObjectProperty
import java.nio.file.Files
import java.nio.file.Path

sealed class ProjectPreviewViewModel {
    abstract val title: String
    abstract val confirmButtonText: String
    abstract val isWarning: Boolean
    
    abstract fun onConfirm(): Path
}

class SingleProjectPreviewViewModel(
    val path: Path,
    val metadata: DatapackMetadata
) : ProjectPreviewViewModel() {
    override val title = "Import Project"
    override val confirmButtonText = "Open Project"
    override val isWarning = false

    override fun onConfirm(): Path = path
}

class ZipProjectPreviewViewModel(
    val path: Path,
    val metadata: DatapackMetadata
) : ProjectPreviewViewModel() {
    override val title = "Import Zip Project"
    override val confirmButtonText = "Import & Open"
    override val isWarning = false
    
    val targetDirectory = SimpleObjectProperty(path.parent)
    
    fun calculateTargetDir(): Path {
        val parent = targetDirectory.get() ?: path.parent
        val folderName = path.fileName.toString().removeSuffix(".zip")
        return parent.resolve(folderName)
    }
    
    fun targetExists(): Boolean {
        return calculateTargetDir().toFile().exists()
    }

    override fun onConfirm(): Path {
        val target = targetDirectory.get() ?: path.parent
        
        val finalDir = calculateTargetDir()
        if (Files.exists(finalDir)) {
            WorkspaceManager.clearProjectHistory(finalDir)
        }
        
        return ArchiveService.unzip(path, target)
    }
}

class WorkspaceProjectPreviewViewModel(
    val root: Path,
    val projects: List<Path>
) : ProjectPreviewViewModel() {
    val isZip = root.toString().endsWith(".zip")

    override val title = if (isZip) "Import Workspace Zip" else "Multi-Project Workspace"
    override val confirmButtonText = if (isZip) "Import & Open" else "Open Workspace"
    override val isWarning = false

    val targetDirectory = SimpleObjectProperty(root.parent)

    fun calculateTargetDir(): Path {
        val parent = targetDirectory.get() ?: root.parent
        val folderName = root.fileName.toString().removeSuffix(".zip")
        return parent.resolve(folderName)
    }

    fun targetExists(): Boolean {
        return calculateTargetDir().toFile().exists()
    }

    override fun onConfirm(): Path {
        if (isZip) {
            val target = targetDirectory.get() ?: root.parent
            val finalDir = calculateTargetDir()
            if (Files.exists(finalDir)) {
                WorkspaceManager.clearProjectHistory(finalDir)
            }
            return ArchiveService.unzip(root, target)
        }
        return root
    }
}

class InvalidProjectPreviewViewModel(
    val path: Path,
    val reason: String
) : ProjectPreviewViewModel() {
    val isZip = path.toString().endsWith(".zip")

    override val title = "Validation Failed"
    override val confirmButtonText = if (isZip) "Ignore & Import" else "Ignore & Open"
    override val isWarning = true

    val targetDirectory = SimpleObjectProperty(path.parent)

    fun calculateTargetDir(): Path {
        val parent = targetDirectory.get() ?: path.parent
        val folderName = path.fileName.toString().removeSuffix(".zip")
        return parent.resolve(folderName)
    }

    fun targetExists(): Boolean {
        return calculateTargetDir().toFile().exists()
    }

    override fun onConfirm(): Path {
        if (isZip) {
            val target = targetDirectory.get() ?: path.parent
            val finalDir = calculateTargetDir()
            if (Files.exists(finalDir)) {
                WorkspaceManager.clearProjectHistory(finalDir)
            }
            return ArchiveService.unzip(path, target)
        }
        return path
    }
}