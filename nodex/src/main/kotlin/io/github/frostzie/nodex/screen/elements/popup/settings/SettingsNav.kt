package io.github.frostzie.nodex.screen.elements.popup.settings

import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.settings.SettingsManager
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.settings.data.CategoryItem
import io.github.frostzie.nodex.settings.data.CategoryType
import io.github.frostzie.nodex.utils.UIConstants
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class SettingsNav : VBox() {
    private lateinit var searchField: TextField
    private lateinit var categoryTreeView: TreeView<CategoryItem>
    private var originalRoot: TreeItem<CategoryItem>? = null

    init {
        styleClass.add("left-panel")
        minWidth = UIConstants.SETTINGS_SIDE_PANEL_MIN_WIDTH
        maxWidth = UIConstants.SETTINGS_SIDE_PANEL_MAX_WIDTH

        createViewComponents()

        children.addAll(
            createSearchSection(),
            categoryTreeView
        )

        EventBus.register(this)
    }

    private fun createSearchSection(): HBox {
        searchField = TextField().apply {
            styleClass.add("search-field")
            promptText = "Search"
            textFormatter = TextFormatter<String> { change -> // Limits to 20 char should be enough and no more crash by art
                if (change.controlNewText.length > 20) {
                    null
                } else {
                    change
                }
            }
            textProperty().addListener(ChangeListener { _, _, newValue ->
                EventBus.post(SettingsSearchQueryChanged(newValue))
            })
        }

        return HBox().apply {
            styleClass.add("search-section")
            children.add(searchField)
            HBox.setHgrow(searchField, Priority.ALWAYS)
        }
    }

    private fun createViewComponents() {
        categoryTreeView = TreeView<CategoryItem>().apply {
            styleClass.add("category-tree")
            isShowRoot = false
            setVgrow(this, Priority.ALWAYS)
            selectionModel.selectedItemProperty().addListener { _, _, newItem ->
                newItem?.value?.let { EventBus.post(SettingsCategorySelected(it)) }
            }

            setCellFactory {
                object : TreeCell<CategoryItem>() {
                    init { isWrapText = true }
                    override fun updateItem(item: CategoryItem?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty) null else item?.name
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onCategoriesAvailable(event: SettingsCategoriesAvailable) {
        val rootItem = TreeItem(CategoryItem("Settings", CategoryType.ROOT))
        rootItem.isExpanded = true

        event.categories.forEach { categoryData ->
            val categoryItem = TreeItem(
                CategoryItem(
                    categoryData.name,
                    CategoryType.MAIN_CATEGORY,
                    categoryData.configClass
                )
            )
            categoryItem.isExpanded = true

            categoryData.subCategories.forEach { subCategoryName ->
                val subItem = TreeItem(
                    CategoryItem(
                        subCategoryName,
                        CategoryType.SUB_CATEGORY,
                        categoryData.configClass,
                        subCategoryName
                    )
                )
                categoryItem.children.add(subItem)
            }
            rootItem.children.add(categoryItem)
        }

        originalRoot = rootItem
        categoryTreeView.root = originalRoot

        Platform.runLater {
            categoryTreeView.selectionModel.select(1)
        }
    }

    @SubscribeEvent
    fun onSearchResultsAvailable(event: SettingsSearchResultsAvailable) {
        if (event.query.isBlank()) {
            categoryTreeView.root = originalRoot
            if (originalRoot?.children?.isNotEmpty() == true) {
                categoryTreeView.selectionModel.select(1)
            }
            return
        }

        val matchingFields = event.results.map { it.field }.toSet()
        val filteredRoot = TreeItem(CategoryItem("Settings", CategoryType.ROOT))

        originalRoot?.children?.forEach { mainCategoryItem ->
            val newMainCategoryItem = TreeItem(mainCategoryItem.value)

            mainCategoryItem.children.forEach { subCategoryItem ->
                val configClass = subCategoryItem.value.configClass
                val subCategory = subCategoryItem.value.subCategory
                val fieldsForSubCategory = if (configClass != null && subCategory != null) {
                    SettingsManager.getNestedCategories(configClass)[subCategory]
                } else null
                val hasMatch = fieldsForSubCategory?.any { it in matchingFields } == true

                if (hasMatch) {
                    newMainCategoryItem.children.add(TreeItem(subCategoryItem.value))
                }
            }

            if (newMainCategoryItem.children.isNotEmpty()) {
                newMainCategoryItem.isExpanded = true
                filteredRoot.children.add(newMainCategoryItem)
            }
        }

        categoryTreeView.root = filteredRoot

        if (filteredRoot.children.isEmpty()) {
            EventBus.post(SettingsContentUpdate("No Results Found", emptyList()))
        } else {
            val firstItem = filteredRoot.children.first()
            val itemToSelect = if (firstItem.children.isNotEmpty()) firstItem.children.first() else firstItem
            categoryTreeView.selectionModel.select(itemToSelect)
        }
    }

    fun cleanup() {
        EventBus.unregister(this)
    }
}