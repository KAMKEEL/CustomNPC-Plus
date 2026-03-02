package io.github.frostzie.nodex.settings.categories

import io.github.frostzie.nodex.events.EventBus
import io.github.frostzie.nodex.events.ImportThemeEvent
import io.github.frostzie.nodex.events.OpenThemeEvent
import io.github.frostzie.nodex.settings.annotations.*
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty

object ThemeConfig {

    @Expose
    @ConfigCategory(name = "Theme")
    @ConfigOption(name = "Theme Selection", desc = "Select the application theme.")
    @ConfigEditorDropdown(
        values = [
            "Primer Light",
            "Primer Dark",
            "Nord Light",
            "Nord Dark",
            "Cupertino Light",
            "Cupertino Dark",
            "Dracula"
        ]
    )
    val theme = SimpleStringProperty("Primer Dark")

    @Expose
    @ConfigCategory(name = "Theme")
    @ConfigOption(name = "Font Size", desc = "Adjust the base font size for the UI. (Default: 13)")
    @ConfigEditorSpinner(minValue = 8, maxValue = 24)
    val fontSize = SimpleIntegerProperty(13)

    @Expose
    @ConfigCategory(name = "Custom Themes", desc = "Manage your custom themes.")
    @ConfigOption(name = "Import Custom Theme", desc = "Select a .css theme file to import and apply.")
    @ConfigEditorButton(text = "Import")
    val importTheme: () -> Unit = { EventBus.post(ImportThemeEvent()) }

    @Expose
    @ConfigCategory(name = "Custom Themes")
    @ConfigOption(name = "Edit Selected Theme", desc = "Open the selected custom theme's CSS file in the editor.")
    @ConfigEditorButton(text = "Edit")
    val editTheme: () -> Unit = { EventBus.post(OpenThemeEvent()) }

    // Temp
    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Json Syntax Info", desc = "This is a temporary setting until LSP support is added!\nExpect this to be removed and/or changed at a later date!")
    @ConfigEditorInfo
    val jsonColorInfo = ""

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Start/End Object", desc = "Default: #FFD700")
    @ConfigEditorColorPicker
    val jsonStartObjectColor = SimpleStringProperty("#FFD700")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Property", desc = "Default: #00FFFF")
    @ConfigEditorColorPicker
    val jsonPropertyColor = SimpleStringProperty("#00FFFF")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "String", desc = "Default: #ADFF2F")
    @ConfigEditorColorPicker
    val jsonStringColor = SimpleStringProperty("#ADFF2F")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Array Start/End", desc = "Default: #7FFFD4")
    @ConfigEditorColorPicker
    val jsonArrayColor = SimpleStringProperty("#7FFFD4")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Float", desc = "Default: #FF8C00")
    @ConfigEditorColorPicker
    val jsonFloatColor = SimpleStringProperty("#FF8C00")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Int", desc = "Default: #FF4500")
    @ConfigEditorColorPicker
    val jsonIntColor = SimpleStringProperty("#FF4500")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Null", desc = "Default: #00008B")
    @ConfigEditorColorPicker
    val jsonNullColor = SimpleStringProperty("#00008B")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "Embedded", desc = "Default: #FF00FF")
    @ConfigEditorColorPicker
    val jsonEmbeddedColor = SimpleStringProperty("#FF00FF")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "True", desc = "Default: #32CD32")
    @ConfigEditorColorPicker
    val jsonTrueColor = SimpleStringProperty("#32CD32")

    @Expose
    @ConfigCategory(name = "Json Colors")
    @ConfigOption(name = "False", desc = "Default: #FF6347")
    @ConfigEditorColorPicker
    val jsonFalseColor = SimpleStringProperty("#FF6347")
}