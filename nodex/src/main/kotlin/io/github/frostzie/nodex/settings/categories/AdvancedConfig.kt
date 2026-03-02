package io.github.frostzie.nodex.settings.categories

import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.loader.minecraft.ChatMessageBuilder
import io.github.frostzie.nodex.settings.annotations.*
import javafx.beans.property.SimpleBooleanProperty

object AdvancedConfig {

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Reload Theme", desc = "Reloads theme file")
    @ConfigEditorButton(text = "Reload All Styles")
    val reloadStyles: () -> Unit = { EventBus.post(ReloadThemeEvent()) }

    @Expose
    @ConfigCategory(name = "Dev")
    @ConfigOption(name = "Reset Workspace", desc = "Resets ALL workspace history and returns to start screen")
    @ConfigEditorButton(text = "Reset Workspace")
    val resetWorkspace: () -> Unit = { EventBus.post(ResetWorkspaceEvent()) }

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show TreeView Hitboxes", desc = "Shows visual borders around File Tree components for debugging layout")
    @ConfigEditorBoolean
    val debugTreeViewHitbox = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show Layout Bounds", desc = "Shows visual borders around major UI components for debugging layout")
    @ConfigEditorBoolean
    val debugLayoutBounds = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Show Resize Handles", desc = "Shows visual borders for window resize handles")
    @ConfigEditorBoolean
    val debugResizeHandles = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Test Warning", desc = "Sends a test warning message to chat")
    @ConfigEditorButton(text = "Test Warning")
    val testWarning: () -> Unit = { ChatMessageBuilder.testWarning() }

    @Expose
    @ConfigCategory(name = "Debug")
    @ConfigOption(name = "Test Error", desc = "Sends a test error message to chat")
    @ConfigEditorButton(text = "Test Error")
    val testError: () -> Unit = { ChatMessageBuilder.testError() }
}