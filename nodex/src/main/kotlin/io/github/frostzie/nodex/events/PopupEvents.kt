package io.github.frostzie.nodex.events

import io.github.frostzie.nodex.settings.data.CategoryItem
import io.github.frostzie.nodex.settings.data.ConfigField
import io.github.frostzie.nodex.settings.data.SearchResult
import kotlin.reflect.KClass

class SettingsWindowOpen

// Settings events
class SettingsSave
class CloseSettingsEvent
data class SettingsSearchQueryChanged(val query: String)
data class SettingsSearchResultsAvailable(val query: String, val results: List<SearchResult>)
data class SettingsSearchResultSelected(val result: SearchResult)

data class SettingsCategorySelected(val item: CategoryItem)
data class SelectTreeItem(val categoryIndex: Int, val subCategory: String?)

class RequestSettingsCategories
data class CategoryData(val name: String, val configClass: KClass<*>, val subCategories: List<String>)
data class SettingsCategoriesAvailable(val categories: List<CategoryData>)

data class SectionData(val name: String, val description: String?, val fields: List<ConfigField>)
data class SettingsContentUpdate(val title: String, val sections: List<SectionData>, val filterFields: Set<ConfigField>? = null)

data class HighlightField(val field: ConfigField)