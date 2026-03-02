package io.github.frostzie.nodex.settings.categories

import io.github.frostzie.nodex.settings.annotations.*
import io.github.frostzie.nodex.utils.OpenLinks
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty

object MainConfig {

    // UI Category
    @Expose
    @ConfigCategory(name = "UI")
    @ConfigOption(
        name = "Modified Indicator",
        desc = "Suffix added on a file in tab when it has unsaved changes."
    )
    @ConfigEditorTextField
    val dirtyIndicator = SimpleStringProperty(" ●")

    @Expose
    @ConfigCategory(name = "UI")
    @ConfigOption(
        name = "Modified File Color",
        desc = "Changes the color of the name of a file that has unsaved changes."
    )
    @ConfigEditorColorPicker
    val dirtyFileColor = SimpleStringProperty("#f7aeae")

    @Expose //TODO: Fix font size not impacting icon size
    @ConfigCategory(name = "UI")
    @ConfigOption(
        name = "Show File Icons",
        desc = "Toggles the visibility of file and folder icons in the File Tree and Editor Tabs. \n Not recommend currently due to incorrect icon sizes!"
    )
    @ConfigEditorBoolean
    val showFileIcons = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "UI")
    @ConfigOption(name = "Enable Caret Color", desc = "When enable the caret will change to the color selected below.")
    @ConfigEditorBoolean
    val enableCaretColor = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "UI")
    @ConfigOption(name = "Caret Color", desc = "Change the color of the caret (Typing mouse curser).")
    @ConfigEditorColorPicker
    val caretColor = SimpleStringProperty("#000000")

    // File Category
    @Expose
    @ConfigCategory(name = "File")
    @ConfigOption(name = "", desc = "")
    @ConfigEditorInfo
    val infoFile = ""

    @Expose
    @ConfigCategory(name = "File")
    @ConfigOption(name = "Universal Folder", desc = "Toggles Universal Folder system. When enabled mod will save to directory selected below.")
    @ConfigEditorBoolean
    val universalFolderToggle = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "File")
    @ConfigOption(name = "Folder Path", desc = "Select where the universal folder would be located.\nIt will create a folder named ``Nodex`` inside selected path, with configs in ``Nodex/config``.")
    @ConfigEditorFolder
    val universalFolderPath = SimpleStringProperty("")

    @Expose
    @ConfigCategory(name = "File")
    @ConfigOption(name = "Universal Config", desc = "Select if Config files should be saved to universal folder.")
    @ConfigEditorBoolean
    val universalConfigToggle = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "File")
    @ConfigOption(name = "Universal Datapack", desc = "Select if Datapack Files should be saved to universal folder.")
    @ConfigEditorBoolean
    val universalDatapackToggle = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "File")
    @ConfigOption(name = "Sync Configs", desc = "Press to sync with files if universal path has been used.")
    @ConfigEditorButton("Sync")
    var syncConfigFiles: () -> Unit = {}
    
    // Link Category
    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Discord Server", desc = "Join our Discord server for support and community!")
    @ConfigEditorButton(text = "Discord")
    val discordLink: () -> Unit = { OpenLinks.discordLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Github", desc = "Check out the source code and contribute on GitHub!")
    @ConfigEditorButton(text = "GitHub")
    val githubLink: () -> Unit = { OpenLinks.gitHubLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Report Issues", desc = "Report bugs or issues you encounter!")
    @ConfigEditorButton(text = "Report Issue")
    val reportBugLink: () -> Unit = { OpenLinks.reportBugLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Support Me", desc = "Support the development of this mod!")
    @ConfigEditorButton(text = "Buy Me A Coffee")
    val buyMeACoffeeLink: () -> Unit = { OpenLinks.buyMeACoffeeLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Modrinth", desc = "Check out the project on Modrinth!")
    @ConfigEditorButton(text = "Modrinth")
    val modrinthLink: () -> Unit = { OpenLinks.modrinthLink() }
}
