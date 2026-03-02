package io.github.frostzie.nodex.settings.data

import kotlin.reflect.KClass

/**
 * Search result for settings search functionality
 */
data class SearchResult(
    val mainCategory: String,
    val subCategory: String,
    val field: ConfigField,
    val relevanceScore: Int
)

/**
 * Type of category in the settings tree
 */
enum class CategoryType {
    ROOT,
    MAIN_CATEGORY,
    SUB_CATEGORY
}

/**
 * Item in the settings category tree
 */
data class CategoryItem(
    val name: String,
    val type: CategoryType,
    val configClass: KClass<*>? = null,
    val subCategory: String? = null
) {
    override fun toString(): String = name
}