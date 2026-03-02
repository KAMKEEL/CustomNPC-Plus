package io.github.frostzie.nodex.settings.data

import io.github.frostzie.nodex.settings.KeyCombination
import io.github.frostzie.nodex.settings.annotations.*
import javafx.beans.property.Property
import kotlin.reflect.KProperty1

sealed interface ConfigField {
    val objectInstance: Any
    val property: KProperty1<Any, *>
    val name: String
    val description: String
    val editorType: EditorType
    val category: ConfigCategory?
}

data class BooleanConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<Boolean>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.BOOLEAN
}

data class TextAreaConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.TEXT
}

data class SliderConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<Number>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val sliderAnnotation: ConfigEditorSlider
) : ConfigField {
    override val editorType: EditorType = EditorType.SLIDER
}

data class DropdownConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val dropdownAnnotation: ConfigEditorDropdown
) : ConfigField {
    override val editorType: EditorType = EditorType.DROPDOWN
}

data class KeybindConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<KeyCombination>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.KEYBIND
}

data class ButtonConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, () -> Unit>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val buttonAnnotation: ConfigEditorButton
) : ConfigField {
    override val editorType: EditorType = EditorType.BUTTON
}

data class InfoConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, *>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.INFO
}

data class SpinnerConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<Int>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?,
    val spinnerAnnotation: ConfigEditorSpinner
) : ConfigField {
    override val editorType: EditorType = EditorType.SPINNER
}

data class ColorPickerConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.COLOR_PICKER
}

data class TextFieldConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.TEXT_FIELD
}

data class FolderConfigField(
    override val objectInstance: Any,
    override val property: KProperty1<Any, Property<String>>,
    override val name: String,
    override val description: String,
    override val category: ConfigCategory?
) : ConfigField {
    override val editorType: EditorType = EditorType.FOLDER
}

enum class EditorType {
    BOOLEAN, TEXT, SLIDER, DROPDOWN, BUTTON, KEYBIND, INFO, SPINNER, COLOR_PICKER, TEXT_FIELD, FOLDER
}