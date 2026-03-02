package io.github.frostzie.nodex.utils

import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.exists

object ProjectIconUtils {

    fun getIcon(path: Path, size: Double): Node {
        try {
            val isZip = path.toString().endsWith(".zip")
            val image = if (isZip) loadFromZip(path, size) else loadFromFolder(path, size)

            if (image != null && !image.isError) {
                return ImageView(image).apply {
                    fitWidth = size
                    fitHeight = size
                }
            }
        } catch (_: Exception) {
            // Fallback
        }
        
        val iconCode = if (path.toString().endsWith(".zip")) Material2AL.ARCHIVE else Material2AL.FOLDER
        return FontIcon(iconCode).apply { iconSize = size.toInt() }
    }

    private fun loadFromZip(path: Path, size: Double): Image? {
        var entryName: String? = null
        try {
            ZipFile(path.toFile()).use { zip ->
                if (zip.getEntry("pack.png") != null) entryName = "pack.png"
                else if (zip.getEntry("icon.png") != null) entryName = "icon.png"
            }
        } catch (_: Exception) { return null }
        
        return if (entryName != null) {
            Image("jar:${path.toUri()}!/$entryName", size, size, true, true)
        } else null
    }

    private fun loadFromFolder(path: Path, size: Double): Image? {
        val iconFile = path.resolve("pack.png").takeIf { it.exists() } 
            ?: path.resolve("icon.png").takeIf { it.exists() }
            
        return if (iconFile != null) {
            Image(iconFile.toUri().toString(), size, size, true, true)
        } else null
    }
}