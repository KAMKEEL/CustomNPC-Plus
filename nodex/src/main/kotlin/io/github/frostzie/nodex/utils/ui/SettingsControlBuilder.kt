package io.github.frostzie.nodex.utils.ui

import io.github.frostzie.nodex.settings.annotations.ConfigEditorSlider
import io.github.frostzie.nodex.settings.categories.ThemeConfig
import io.github.frostzie.nodex.settings.data.*
import io.github.frostzie.nodex.utils.LoggerProvider
import atlantafx.base.controls.ToggleSwitch
import io.github.frostzie.nodex.utils.ThemeManager
import io.github.frostzie.nodex.utils.ui.controls.KeybindInputButton
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import java.io.File

/**
 * Builder for creating JavaFX controls for different setting types.
 */
object SettingsControlBuilder {
    private val logger = LoggerProvider.getLogger("SettingsControlBuilder")

    fun createSliderValueLabel(field: SliderConfigField): Label {
        val prop = field.property.get(field.objectInstance)
        val sliderAnnotation = field.sliderAnnotation
        return Label().apply {
            styleClass.add("slider-value")
            text = formatSliderLabel(prop.value.toDouble(), sliderAnnotation)
            prop.addListener { _, _, newValue ->
                text = formatSliderLabel(newValue.toDouble(), sliderAnnotation)
            }
        }
    }

    private fun formatSliderLabel(value: Double, annotation: ConfigEditorSlider): String {
        val hasDecimals = annotation.stepSize < 1.0
        return if (hasDecimals) {
            "Value: %.2f".format(value)
        }
        else {
            "Value: %d".format(value.toInt()) 
        }
    }

    /**
     * Creates a setting tile UI component based on the provided [ConfigField].
     *
     * This function inspects the type of the [ConfigField] and constructs an appropriate
     * JavaFX control (e.g., a toggle switch for a boolean, a slider for a number)
     * wrapped in a standardized tile layout.
     *
     * @param field The configuration field for which to create a UI tile.
     * @return An [HBox] containing the complete UI for the setting.
     */
    fun createSettingTile(field: ConfigField): HBox {
        return when (field) {
            is BooleanConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val toggleSwitch = ToggleSwitch().apply {
                    selectedProperty().bindBidirectional(prop)
                }
                Tiles.DefaultTile(field.name, field.description.takeIf { it.isNotEmpty() }, toggleSwitch)
            }

            is KeybindConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val keybindInputButton = KeybindInputButton().apply {
                    keybindProperty.bindBidirectional(prop)
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, keybindInputButton)
            }

            is DropdownConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val comboBox = ComboBox<String>().apply {
                    //TODO: Remove this by make the Theme have their own nicer screen rather then just a dropdown.
                    if (field.objectInstance is ThemeConfig && field.property.name == "theme") {
                        items = ThemeManager.availableThemes
                    } else {
                        items.addAll(*field.dropdownAnnotation.values)
                    }
                    valueProperty().bindBidirectional(prop)
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, comboBox)
            }

            is TextAreaConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val textArea = TextArea().apply {
                    textFormatter = TextFormatter<String> { change ->
                        if (change.controlNewText.length > 1000) {
                            null
                        } else {
                            change
                        }
                    }
                    textProperty().bindBidirectional(prop)
                    isWrapText = true
                }
                Tiles.LowTile(field.name, field.description.takeIf { it.isNotEmpty() }, textArea)
            }

            is SliderConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val sliderAnnotation = field.sliderAnnotation
                val slider = Slider().apply {
                    min = sliderAnnotation.minValue
                    max = sliderAnnotation.maxValue
                    blockIncrement = sliderAnnotation.stepSize
                    valueProperty().bindBidirectional(prop)
                    prefWidth = 400.0
                }
                val valueLabel = createSliderValueLabel(field)

                val sliderControl = HBox(slider, valueLabel).apply {
                    spacing = 5.0
                    alignment = Pos.CENTER_LEFT
                }
                Tiles.LowTile(field.name, field.description.takeIf { it.isNotEmpty() }, sliderControl)
            }

            is ButtonConfigField -> {
                val action = field.property.get(field.objectInstance)
                val button = Button(field.buttonAnnotation.text).apply {
                    styleClass.add("field-button")
                    minWidth = Region.USE_PREF_SIZE // Makes it so a text can't push buttons
                    setOnAction {
                        try {
                            action.invoke()
                            logger.debug("Action button '{}' executed.", field.name)
                        } catch (e: Exception) {
                            logger.error("Error executing action for button '{}'", field.name, e)
                        }
                    }
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, button)
            }

            is InfoConfigField -> {
                Tiles.InfoTile(field.name, field.description.takeIf { it.isNotEmpty() })
            }

            is SpinnerConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val spinnerAnnotation = field.spinnerAnnotation
                val spinner = Spinner<Int>(spinnerAnnotation.minValue, spinnerAnnotation.maxValue, prop.value).apply {
                    isEditable = true
                    prefWidth = 120.0
                }

                spinner.valueProperty().addListener { _, _, newValue ->
                    if (prop.value != newValue) {
                        prop.value = newValue
                    }
                }
                prop.addListener { _, _, newValue ->
                    if (spinner.value != newValue) {
                        spinner.valueFactory.value = newValue
                    }
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, spinner)
            }

            is ColorPickerConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val initialColor = try {
                    Color.valueOf(prop.value)
                } catch (e: Exception) {
                    logger.warn("Invalid initial color string in settings: ${prop.value}", e)
                    Color.WHITE
                }
                val colorPicker = ColorPicker(initialColor)

                // Bind the ColorPicker to the StringProperty
                colorPicker.valueProperty().addListener { _, _, newColor ->
                    val hex = "#%02X%02X%02X".format(
                        (newColor.red * 255).toInt(),
                        (newColor.green * 255).toInt(),
                        (newColor.blue * 255).toInt()
                    )
                    if (prop.value != hex) {
                        prop.value = hex
                    }
                }

                // Bind the StringProperty to the ColorPicker
                prop.addListener { _, _, newHex ->
                    try {
                        val newColor = Color.valueOf(newHex)
                        if (colorPicker.value != newColor) {
                            colorPicker.value = newColor
                        }
                    } catch (e: Exception) {
                        logger.warn("Invalid color string in settings: $newHex", e)
                    }
                }
                Tiles.LargeTile(field.name, field.description.takeIf { it.isNotEmpty() }, colorPicker)
            }

            is TextFieldConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val textField = TextField().apply {
                    textFormatter = TextFormatter<String> { change ->
                        if (change.controlNewText.length > 1000) {
                            null
                        } else {
                            change
                        }
                    }
                    textProperty().bindBidirectional(prop)
                }
                Tiles.LowTile(field.name, field.description.takeIf { it.isNotEmpty() }, textField)
            }

            is FolderConfigField -> {
                val prop = field.property.get(field.objectInstance)
                val textField = TextField().apply {
                    textProperty().bindBidirectional(prop)
                    isDisable = true
                    HBox.setHgrow(this, Priority.ALWAYS)
                }

                // Yeah yeah using an icon in code kill me now lol, but for now it's fine until I fix IconSource not* working on buttons
                val button = Button("📁").apply { // Also yeah the reason way I changed it was since the UI otherwise gets way too big
                    styleClass.add("field-button")
                    setOnAction {
                        val dirChooser = DirectoryChooser()
                        dirChooser.title = "Select Folder"
                        
                        val currentPath = prop.value
                        if (!currentPath.isNullOrBlank()) {
                            val file = File(currentPath)
                            if (file.exists() && file.isDirectory) {
                                dirChooser.initialDirectory = file
                            }
                        }

                        val selectedFile = dirChooser.showDialog(scene.window)
                        if (selectedFile != null) {
                            prop.value = selectedFile.absolutePath
                        }
                    }
                }

                val container = HBox(textField, button).apply {
                    spacing = 5.0
                    alignment = Pos.CENTER_LEFT
                }
                Tiles.LowTile(field.name, field.description.takeIf { it.isNotEmpty() }, container)
            }
        }
    }
}