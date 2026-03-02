package io.github.frostzie.nodex.styling.common

import io.github.frostzie.nodex.styling.tabs.TabStyle
import org.kordamp.ikonli.Ikon

/**
 * A marker interface for all style models (e.g., [TabStyle]).
 * This provides a common base type for generic constraints.
 */
interface DynamicStyle

/**
 * A generic interface for a style rule, parameterized by context and style types.
 *
 * This is the core contract for all styling logic.
 *
 * @param C The data context type (e.g., `TextEditorViewModel.TabData`).
 * @param S The style model type (e.g., `TabStyle`).
 */
interface StyleRule<C, S> {
    /** The priority of the rule, used for sorting and resolving conflicts. */
    val priority: Int

    /** Determines if this rule should be applied to the given context. */
    fun appliesTo(context: C): Boolean

    /** Gets the style to be applied if the rule matches. */
    fun getStyle(context: C): S
}

/**
 * Represents the source of an icon, allowing Ikonli font icons or svgs.
 */
sealed class IconSource {
    abstract val size: Int

    /** An icon from the Ikonli font pack. */
    data class IkonIcon(val ikon: Ikon, override val size: Int = 16) : IconSource()

    /**
     * An SVG icon, represented by its path to it.
     */
    data class SvgIcon(val path: String, override val size: Int = 16) : IconSource()
}

/**
 * Defines standardized priority levels for style rules.
 * This creates a contract for the core application, user settings, and plugins
 * to ensure styling is applied in a predictable order.
 */
object StylePriority {
    /** Applied first, intended for base/default styles. */
    const val BASE = 0
    /** For generic, application-wide rules (e.g., dirty state). */
    const val DEFAULT_RULES = 1
    /** For more specific, application-level rules (e.g., config files, asset files). */
    const val SPECIFIC_RULES = 2
    /** The default priority for a typical plugin rule. */
    const val PLUGIN_DEFAULT = 10
    /** For a plugin that wishes to forcefully override most other styles. */
    const val PLUGIN_OVERRIDE = 20
    /** The absolute highest priority, used internally for "locked" styles. */
    const val FRAMEWORK_FINAL = Int.MAX_VALUE
}
