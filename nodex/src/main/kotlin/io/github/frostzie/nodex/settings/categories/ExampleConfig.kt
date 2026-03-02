package io.github.frostzie.nodex.settings.categories

import io.github.frostzie.nodex.features.dev.showcase.MessageShowcase
import io.github.frostzie.nodex.features.dev.showcase.NotificationShowcase
import io.github.frostzie.nodex.settings.KeyCombination
import io.github.frostzie.nodex.settings.annotations.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode

@Suppress("unused")
object ExampleConfig {

    @Expose
    @ConfigCategory(name = "Example Settings", desc = "Example description")
    @ConfigOption(name = "Info", desc = "Pray observe, dear user, this most distinguished informational tile. Though it performs no remarkable feats nor commands the slightest hint of wizardry, it stands proudly as a beacon of gentle guidance. One might employ this panel to impart notes, advisories, or particularly polite warnings to future travellers of the settings menu. Should you choose to bestow upon it a purpose of genuine importance, it shall accept such responsibility with quiet dignity. Until then, let it loiter here in graceful idleness, murmuring softly of its own exemplary usefulness.")
    @ConfigEditorInfo
    val info = ""

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Boolean", desc = "Description")
    @ConfigEditorBoolean
    val bool = SimpleBooleanProperty(true)

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Keybinds", desc = "Description")
    @ConfigEditorKeybind
    val key = SimpleObjectProperty(KeyCombination(key = KeyCode.S, ctrl = true, alt = true))

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Dropdown", desc = "Description")
    @ConfigEditorDropdown(values = ["Option 1", "Option 2", "Option 3", "Option 4"])
    val combo = SimpleStringProperty("Option 3")
    
    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Text Area", desc = "Description")
    @ConfigEditorTextArea
    val text = SimpleStringProperty("Text Area...")

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Slider", desc = "Description")
    @ConfigEditorSlider(minValue = 5.0, maxValue = 300.0, stepSize = 5.0)
    val slider = SimpleDoubleProperty(30.0)

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Spinner", desc = "Description")
    @ConfigEditorSpinner(minValue = 1, maxValue = 10)
    val spinner = SimpleIntegerProperty(5)

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Button", desc = "Description")
    @ConfigEditorButton(text = "Button")
    val button: () -> Unit = {}

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Color Picker", desc = "Description")
    @ConfigEditorColorPicker
    val color = SimpleStringProperty("#FF0000")

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Text Field", desc = "Description")
    @ConfigEditorTextField
    val textField = SimpleStringProperty("Text Field...")

    @Expose
    @ConfigCategory(name = "Example Settings")
    @ConfigOption(name = "Folder Selector", desc = "Description")
    @ConfigEditorFolder
    val folder = SimpleStringProperty("")

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Title", desc = "Title of the showcase message" +
            "\nYes Art I know you can put inf text here but whats the point of doing that? That's why there is no limit. (Applies to desc too.)")
    @ConfigEditorTextField
    val messageTitle = SimpleStringProperty("Test Title")

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Description", desc = "Body text of the showcase message")
    @ConfigEditorTextArea
    val messageDescription = SimpleStringProperty("Description area for the message.")

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Severity", desc = "Visual style of the message")
    @ConfigEditorDropdown(values = ["REGULAR", "INFO", "SUCCESS", "WARNING", "DANGER"])
    val messageSeverity = SimpleStringProperty("INFO")

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Position", desc = "Screen corner for the message")
    @ConfigEditorDropdown(values = ["TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"])
    val messagePosition = SimpleStringProperty("BOTTOM_RIGHT")

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Duration (ms)", desc = "How long the message stays visible")
    @ConfigEditorSlider(minValue = 500.0, maxValue = 10000.0, stepSize = 500.0)
    val messageDuration = SimpleDoubleProperty(3000.0)

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Width", desc = "Custom width (0 = default)")
    @ConfigEditorSlider(minValue = 0.0, maxValue = 600.0, stepSize = 25.0)
    val messageWidth = SimpleDoubleProperty(0.0)

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Height", desc = "Custom height (0 = default)")
    @ConfigEditorSlider(minValue = 0.0, maxValue = 600.0, stepSize = 25.0)
    val messageHeight = SimpleDoubleProperty(0.0)

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Icon", desc = "Icon name (e.g. STAR, WARNING, CHECK) Using Ikonli Material2OutlinedMZ icon pack.")
    @ConfigEditorTextField
    val messageIcon = SimpleStringProperty("")

    @Expose
    @ConfigCategory(name = "Message Showcase")
    @ConfigOption(name = "Trigger", desc = "Click to show the message")
    @ConfigEditorButton(text = "Show Message")
    val showMessage: () -> Unit = { MessageShowcase.showExampleMessage() }

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Text", desc = "Text content of the notification\nYeah Yeah same thing here art it's fine...")
    @ConfigEditorTextArea
    val notificationText = SimpleStringProperty("This is a notification.")

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Severity", desc = "Visual style of the notification")
    @ConfigEditorDropdown(values = ["REGULAR", "INFO", "SUCCESS", "WARNING", "DANGER"])
    val notificationSeverity = SimpleStringProperty("INFO")

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Position", desc = "Screen corner for the notification")
    @ConfigEditorDropdown(values = ["TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT"])
    val notificationPosition = SimpleStringProperty("BOTTOM_RIGHT")

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Duration (ms)", desc = "How long the notification stays visible")
    @ConfigEditorSlider(minValue = 500.0, maxValue = 10000.0, stepSize = 500.0)
    val notificationDuration = SimpleDoubleProperty(3000.0)

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Width", desc = "Custom width (0 = default)")
    @ConfigEditorSlider(minValue = 0.0, maxValue = 600.0, stepSize = 25.0)
    val notificationWidth = SimpleDoubleProperty(0.0)

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Height", desc = "Custom height (0 = default)")
    @ConfigEditorSlider(minValue = 0.0, maxValue = 600.0, stepSize = 25.0)
    val notificationHeight = SimpleDoubleProperty(0.0)

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Icon", desc = "Icon name (e.g. STAR, WARNING, CHECK) Using Ikonli Material2OutlinedMZ icon pack.")
    @ConfigEditorTextField
    val notificationIcon = SimpleStringProperty("")

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Show Actions", desc = "Add example buttons and menu items")
    @ConfigEditorBoolean
    val notificationShowActions = SimpleBooleanProperty(false)

    @Expose
    @ConfigCategory(name = "Notification Showcase")
    @ConfigOption(name = "Trigger", desc = "Click to show the notification")
    @ConfigEditorButton(text = "Show Notification")
    val showNotification: () -> Unit = { NotificationShowcase.showExampleNotification() }
}