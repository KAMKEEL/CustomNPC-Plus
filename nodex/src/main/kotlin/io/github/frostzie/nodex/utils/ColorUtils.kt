package io.github.frostzie.nodex.utils

import javafx.scene.effect.ColorAdjust
import javafx.scene.paint.Color

/**
 * Utility class for color manipulation and conversion to ColorAdjust parameters
 */
object ColorUtils {
    private val logger = LoggerProvider.getLogger("ColorUtils")

    /**
     * Creates a ColorAdjust effect to transform a WHITE icon to a target color.
     * This logic is based on the standard method for colorizing white sources,
     * which requires inverting brightness and offsetting hue.
     *
     * @param hexColor Color in hex format (e.g., "#f0eded", "#FF0000")
     * @return ColorAdjust effect that will transform white pixels to the target color
     */
    fun createColorAdjustForWhiteIcon(hexColor: String): ColorAdjust {
        val color = parseHexColor(hexColor)

        val hueAdjust = (((color.hue + 180) % 360) / 180.0) - 1.0
        val saturationAdjust = color.saturation

        val brightnessAdjust = color.brightness - 1.0

        return ColorAdjust().apply {
            this.hue = hueAdjust
            this.saturation = saturationAdjust
            this.brightness = brightnessAdjust
            contrast = 0.0
        }
    }

    /**
     * Parses hex color string to JavaFX Color
     */
    private fun parseHexColor(hexColor: String): Color {
        return try {
            val cleanHex = hexColor.removePrefix("#")
            when (cleanHex.length) {
                3 -> {
                    val r = cleanHex[0].toString().repeat(2)
                    val g = cleanHex[1].toString().repeat(2)
                    val b = cleanHex[2].toString().repeat(2)
                    Color.web("#$r$g$b")
                }
                6 -> Color.web("#$cleanHex")
                8 -> Color.web("#$cleanHex")
                else -> {
                    logger.warn("Invalid hex color format: $hexColor, using default")
                    Color.WHITE
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to parse hex color: $hexColor", e)
            Color.WHITE
        }
    }
}