package io.github.frostzie.nodex.utils

import io.github.frostzie.nodex.styling.common.IconSource
import io.github.frostzie.nodex.svg_icon.SVGImage
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import org.kordamp.ikonli.javafx.FontIcon

object IconUtils {
    private val logger = LoggerProvider.getLogger("IconUtils")

    fun createIcon(source: IconSource): Node {
        return when (source) {
            is IconSource.IkonIcon -> {
                val icon = FontIcon(source.ikon)
                icon.iconSize = source.size
                icon
            }

            is IconSource.SvgIcon -> {
                try {
                    val url = IconUtils::class.java.getResource(source.path)
                    if (url == null) {
                        logger.warn("SVG resource not found: ${source.path}")
                        return Region().apply {
                            prefWidth = source.size.toDouble()
                            prefHeight = source.size.toDouble()
                        }
                    }
                    val svgImage = SVGImage.of(url)
                    val image = svgImage.toImage(source.size, source.size)
                    ImageView(image).apply {
                        fitWidth = source.size.toDouble()
                        fitHeight = source.size.toDouble()
                        isPreserveRatio = true
                    }
                } catch (e: Exception) {
                    logger.error("Failed to load SVG icon: ${source.path}", e)
                    Region().apply {
                        prefWidth = source.size.toDouble()
                        prefHeight = source.size.toDouble()
                    }
                }
            }
        }
    }
}