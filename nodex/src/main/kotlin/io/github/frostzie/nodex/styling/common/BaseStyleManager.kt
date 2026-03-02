package io.github.frostzie.nodex.styling.common

import io.github.frostzie.nodex.utils.LoggerProvider

/**
 * A generic, abstract base class that provides the core for any style manager.
 *
 * This class contains the reusable logic for registering rules, evaluating them based on priority,
 * and merging the resulting styles. It is designed to be extended by concrete, singleton
 * objects (like `TabStyleManager`) that provide the specific types and merge implementation.
 *
 * @param C The data context type (e.g., `TextEditorViewModel.TabData`).
 * @param S The style model type (e.g., `TabStyle`), which must implement [DynamicStyle].
 * @property emptyStyle An instance of the style model representing a "no style" state.
 * @property mergeStyles A lambda function that knows how to merge two instances of the style model.
 */
abstract class BaseStyleManager<C, S : DynamicStyle>(
    private val emptyStyle: S,
    private val mergeStyles: (current: S, new: S) -> S
) {
    private val logger = LoggerProvider.getLogger(this::class.simpleName ?: "BaseStyleManager")
    private val rules = mutableListOf<StyleRule<C, S>>()

    /**
     * Registers a new style rule with this manager.
     * @param rule The rule to add to the registry.
     */
    fun registerRule(rule: StyleRule<C, S>) {
        rules.add(rule)
        logger.debug("Registered rule: ${rule::class.simpleName} with priority ${rule.priority}")
    }

    /**
     * Evaluates all applicable rules for the given context and merges them into a single style.
     *
     * @param context The data context for the component to be styled.
     * @return The final, consolidated style model.
     */
    open fun evaluate(context: C): S {
        val matchingRules = rules
            .filter { it.appliesTo(context) }
            .sortedBy { it.priority }

        if (matchingRules.isEmpty()) return emptyStyle

        // Reduce the list of styles into a single, merged style using the provided merge function.
        return matchingRules.fold(emptyStyle) { merged, rule ->
            mergeStyles(merged, rule.getStyle(context))
        }
    }
}