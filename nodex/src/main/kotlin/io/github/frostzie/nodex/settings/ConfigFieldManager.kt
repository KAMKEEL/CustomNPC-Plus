package io.github.frostzie.nodex.settings

import io.github.frostzie.nodex.settings.annotations.*
import io.github.frostzie.nodex.settings.data.*
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.beans.property.Property
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

internal object ConfigFieldManager {
    private val logger = LoggerProvider.getLogger("ConfigFieldManager")

    fun create(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption): ConfigField? {
        val propValue = property.get(instance)

        return when {
            property.findAnnotation<ConfigEditorBoolean>() != null -> createBooleanField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorTextArea>() != null -> createTextAreaField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorSlider>() != null -> createSliderField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorDropdown>() != null -> createDropdownField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorButton>() != null -> createButtonField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorKeybind>() != null -> createKeybindField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorInfo>() != null -> createInfoField(instance, property, option)
            property.findAnnotation<ConfigEditorSpinner>() != null -> createSpinnerField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorColorPicker>() != null -> createColorPickerField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorTextField>() != null -> createTextField(instance, property, option, propValue)
            property.findAnnotation<ConfigEditorFolder>() != null -> createFolderField(instance, property, option, propValue)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createBooleanField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): BooleanConfigField? {
        if (propValue is Property<*> && propValue.value is Boolean) {
            return BooleanConfigField(
                instance, property as KProperty1<Any, Property<Boolean>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<Boolean> for Boolean.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createTextAreaField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): TextAreaConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return TextAreaConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String> for TextArea.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createSliderField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): SliderConfigField? {
        if (propValue is Property<*> && propValue.value is Number) {
            return SliderConfigField(
                instance, property as KProperty1<Any, Property<Number>>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<Number> for Slider.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createDropdownField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): DropdownConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return DropdownConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String> for Dropdown.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createButtonField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): ButtonConfigField? {
        if (propValue is Function0<*>) {
            return ButtonConfigField(
                instance, property as KProperty1<Any, () -> Unit>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected () -> Unit.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createKeybindField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): KeybindConfigField? {
        if (propValue is Property<*> && propValue.value is KeyCombination) {
            return KeybindConfigField(
                instance,
                property as KProperty1<Any, Property<KeyCombination>>,
                option.name,
                option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<KeyCombination> for Keybind.")
        return null
    }

    private fun createInfoField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption): InfoConfigField {
        return InfoConfigField(
            instance, property, option.name, option.desc,
            property.findAnnotation()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun createSpinnerField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): SpinnerConfigField? {
        if (propValue is Property<*> && propValue.value is Int) {
            return SpinnerConfigField(
                instance, property as KProperty1<Any, Property<Int>>, option.name, option.desc,
                property.findAnnotation(),
                property.findAnnotation()!!
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<Int> for Spinner.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createColorPickerField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): ColorPickerConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return ColorPickerConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String> for ColorPicker.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createTextField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): TextFieldConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return TextFieldConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String> for TextField.")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun createFolderField(instance: Any, property: KProperty1<Any, Any>, option: ConfigOption, propValue: Any): FolderConfigField? {
        if (propValue is Property<*> && propValue.value is String) {
            return FolderConfigField(
                instance, property as KProperty1<Any, Property<String>>, option.name, option.desc,
                property.findAnnotation()
            )
        }
        logger.warn("Mismatched annotation/type for ${property.name} in ${instance::class.simpleName}. Expected Property<String> for Folder.")
        return null
    }
}