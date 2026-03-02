package io.github.frostzie.nodex.utils

import javafx.scene.Scene

/**
 * Handles loading and applying stylesheets to scenes and components
 */
object CSSManager {
    private val logger = LoggerProvider.getLogger("CSSManager")

    private val cssFiles = listOf(
        "Debug.css",
        "Override.css",
        "FileTreeView.css",
        "TextEditorView.css"
    )

    /**
     * Applies all known CSS files to a scene. This is the primary method for styling the main application window.
     */
    fun applyAllStyles(scene: Scene) {
        logger.debug("Applying all ${cssFiles.size} application CSS styles to the scene...")
        scene.stylesheets.clear()
        applyStyles(scene, *cssFiles.toTypedArray())
    }


    /**
     * Apply specific CSS files to a scene. This is now an internal helper.
     */
    private fun applyStyles(scene: Scene, vararg styleNames: String) {
        styleNames.forEach { styleName ->
            val cssFile = if (styleName.endsWith(".css")) styleName else "$styleName.css"
            val resourcePath = "/assets/nodex/themes/styles/$cssFile"
            val url = CSSManager::class.java.getResource(resourcePath)
            if (url != null) {
                scene.stylesheets.add(url.toExternalForm())
            } else {
                logger.error("CSS resource not found: $resourcePath")
            }
        }
        logger.debug("Applied ${styleNames.size} CSS styles to scene: ${styleNames.joinToString()}")
    }

    /**
     * Apply specific popup window styles.
     * Use this for individual popup windows to avoid loading all application styles.
     */
    fun applyPopupStyles(scene: Scene, vararg styleNames: String) {
        applyStyles(scene, *styleNames)
        logger.debug("Applied popup styles to scene: ${styleNames.joinToString()}")
    }

    /**
     * Reloads all CSS files for one or more scenes efficiently.
     * It reads each CSS file only once and applies the result to all provided scenes.
     */
    fun reloadAllStyles(vararg scenes: Scene) {
        if (scenes.isEmpty()) return
        logger.info("Reloading all CSS styles for ${scenes.size} scene(s)...")

        scenes.forEach { scene ->
            scene.stylesheets.clear()
            applyAllStyles(scene)
        }

        logger.info("All styles reloaded successfully for ${scenes.size} scene(s).")
    }
}