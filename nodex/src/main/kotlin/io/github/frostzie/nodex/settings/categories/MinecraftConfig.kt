package io.github.frostzie.nodex.settings.categories

import io.github.frostzie.nodex.settings.annotations.ConfigCategory
import io.github.frostzie.nodex.settings.annotations.ConfigEditorBoolean
import io.github.frostzie.nodex.settings.annotations.ConfigEditorInfo
import io.github.frostzie.nodex.settings.annotations.ConfigOption
import io.github.frostzie.nodex.settings.annotations.Expose
import javafx.beans.property.SimpleBooleanProperty

object MinecraftConfig {
    @Expose
    @ConfigCategory(name = "")
    @ConfigOption(name = "Info", desc = "This category will contain every settings that directly changes minecraft in someway. Changes might be simply UI or some more impactful.\nAll settings in this category will be by default disabled to keep the vanilla experience intact.")
    @ConfigEditorInfo
    val info = ""

    @Expose
    @ConfigCategory(name = "")
    @ConfigOption(name = "On Screen Button", desc = "Adds an on screen IDE button to access the in-game editor from the title screen or pause menu.")
    @ConfigEditorBoolean
    val onScreenButton = SimpleBooleanProperty(false)
}