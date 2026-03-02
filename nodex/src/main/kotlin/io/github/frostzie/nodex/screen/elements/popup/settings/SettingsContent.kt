package io.github.frostzie.nodex.screen.elements.popup.settings

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.SettingsContentUpdate
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.settings.data.ConfigField
import io.github.frostzie.nodex.utils.ui.SettingsControlBuilder
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.HBox.setHgrow
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class SettingsContent : VBox() {
    init {
        styleClass.add("settings-content-area")
        setVgrow(this, Priority.ALWAYS)
        setHgrow(this, Priority.ALWAYS)
        EventBus.register(this)
    }

    @Suppress("unused")
    @SubscribeEvent
    fun onContentUpdate(event: SettingsContentUpdate) {
        if (event.sections.isEmpty()) {
            val noResultsLabel = Label("No results found.").apply {
                styleClass.add("no-results-label")
            }
            val container = VBox(noResultsLabel).apply {
                styleClass.add("category-content")
                alignment = Pos.CENTER
            }
            setVgrow(container, Priority.ALWAYS)
            children.setAll(container)
            return
        }

        val content = VBox().apply {
            styleClass.add("category-content")
            spacing = 15.0

            val categoryTitle = Label(event.title).apply {
                styleClass.add("category-title")
            }
            children.add(categoryTitle)

            event.sections.forEachIndexed { index, sectionData ->
                val section = createSubCategorySection(sectionData.name, sectionData.description, sectionData.fields, event.filterFields)
                if (section.children.any { it.isVisible }) {
                    children.add(section)
                }
            }
        }

        val scrollPane = ScrollPane(content).apply {
            styleClass.add("category-scroll")
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }
        setVgrow(scrollPane, Priority.ALWAYS)
        children.setAll(scrollPane)
    }


    private fun createSubCategorySection(subCategoryName: String, description: String?, fields: List<ConfigField>, filterFields: Set<ConfigField>?): VBox {
        return VBox().apply {
            styleClass.add("subcategory-section")
            spacing = 15.0

            val subCategoryHeader = HBox().apply {
                styleClass.add("subcategory-header")

                val subTitle = Label(subCategoryName).apply {
                    styleClass.add("subcategory-title")
                }
                children.addAll(subTitle)
            }

            children.add(subCategoryHeader)

            if (!description.isNullOrBlank()) {
                val descLabel = Label(description).apply {
                    styleClass.add("subcategory-description")
                    isWrapText = true
                }
                children.add(descLabel)
            }

            val fieldsToShow = if (filterFields != null) fields.filter { it in filterFields } else fields

            val fieldVBox = VBox().apply {
                styleClass.add("fields-container")
            }
            fieldsToShow.forEach { field ->
                fieldVBox.children.add(SettingsControlBuilder.createSettingTile(field))
            }
            children.add(fieldVBox)

            val hasVisibleFields = fieldsToShow.isNotEmpty()
            this.isVisible = hasVisibleFields
            this.isManaged = hasVisibleFields
        }
    }
}