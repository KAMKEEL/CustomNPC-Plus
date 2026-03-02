package io.github.frostzie.nodex.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.frostzie.nodex.config.ConfigManager
import io.github.frostzie.nodex.settings.annotations.*
import io.github.frostzie.nodex.settings.data.*
import io.github.frostzie.nodex.utils.LoggerProvider
import java.io.FileReader
import java.io.FileWriter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * Manages the registration, initialization, loading, and saving of application settings.
 * Settings are defined in various configuration classes (e.g., `MainConfig`, `ThemeConfig`)
 * and are exposed via annotations to be managed and displayed in the UI.
 */
object SettingsManager {
    private val logger = LoggerProvider.getLogger("SettingsManager")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val settingsFile get() = ConfigManager.configDir.resolve("settings.json").toFile()
    private val defaultValues = mutableMapOf<KProperty1<*, *>, Any?>()

    // Stores a list of registered configuration categories, mapping a category name to its KClass.
    private val configClasses = mutableListOf<Pair<String, KClass<*>>>()

    /**
     * Registers a new settings category with the manager.
     * This allows the settings defined within the [configClass] to be managed and displayed.
     *
     * @param categoryName A user-friendly name for the settings category (e.g., "Main", "Theme").
     * @param configClass The Kotlin class (KClass) that holds the setting properties.
     */
    fun register(categoryName: String, configClass: KClass<*>) {
        configClasses.add(categoryName to configClass)
        logger.debug("Registered settings category '$categoryName' with ${configClass.simpleName}")
    }

    /**
     * Initializes the settings manager by caching default values and loading any existing settings
     * from the settings file.
     */
    fun initialize() {
        logger.debug("Initializing SettingsManager...")
        cacheDefaultValues()
        loadSettings()
    }

    /**
     * Iterates through all registered configuration classes and caches the default values
     * of all exposed setting properties. These default values are used if a setting
     * is not found in the loaded settings file or needs to be reset.
     */
    private fun cacheDefaultValues() {
        logger.debug("Caching default setting values...")
        configClasses.forEach { (_, configClass) ->
            getConfigFields(configClass).forEach { field ->
                when (field) {
                    is ButtonConfigField -> { /* Buttons don't have a value to cache */ }
                    is InfoConfigField -> { /* Info fields don't have a value to cache */ }
                    is BooleanConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is DropdownConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is KeybindConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is SliderConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is TextAreaConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is SpinnerConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is ColorPickerConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is TextFieldConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                    is FolderConfigField -> defaultValues[field.property] = field.property.get(field.objectInstance).value
                }
            }
        }
        logger.debug("Cached ${defaultValues.size} default values.")
    }

    /**
     * Retrieves the cached default value for a given setting property.
     *
     * @param property The KProperty1 representing the setting.
     * @return The default value of the property, or `null` if not found or not applicable (e.g., for buttons).
     */
    fun getDefaultValue(property: KProperty1<*, *>): Any? = defaultValues[property]

    /**
     * Returns a list of all registered top-level settings categories.
     *
     * @return A list of pairs, where each pair contains the category name and its corresponding [KClass].
     */
    fun getConfigCategories(): List<Pair<String, KClass<*>>> = configClasses

    /**
     * Retrieves all [ConfigField]s (individual setting properties) from a given configuration class.
     * It uses reflection to find properties annotated with `@Expose` and `@ConfigOption`,
     * ensuring the order of fields is maintained as declared in the source file.
     *
     * @param configClass The [KClass] of the configuration object (e.g., `MainConfig::class`).
     * @return A list of [ConfigField] objects representing the settings, in declaration order.
     */
    @Suppress("UNCHECKED_CAST") // Only to not show warning in IntelliJ
    fun getConfigFields(configClass: KClass<*>): List<ConfigField> {
        val objectInstance = configClass.objectInstance ?: return emptyList()
        val propertiesByName = configClass.declaredMemberProperties.associateBy { it.name }

        return configClass.java.declaredFields.mapNotNull { field -> propertiesByName[field.name] }
            .mapNotNull { property ->
                val expose = property.findAnnotation<Expose>()
                val option = property.findAnnotation<ConfigOption>()

                if (expose != null && option != null) {
                    val p = property as KProperty1<Any, Any>
                    ConfigFieldManager.create(objectInstance, p, option)
                } else null
            }
    }

    /**
     * Organizes the [ConfigField]s from a given configuration class into nested categories.
     * The categories are determined by the `@ConfigCategory` annotation on each field.
     *
     * The order of categories and fields within them is preserved based on their declaration
     * order in the source file. If a field does not have a `@ConfigCategory` annotation,
     * or if its name is blank, it will be grouped under the "General" category.
     *
     * @param configClass The [KClass] of the configuration object.
     * @return A [Map] where keys are category names (String) and values are lists of [ConfigField]s
     *         belonging to that category. The map preserves the insertion order of categories.
     */
    fun getNestedCategories(configClass: KClass<*>): Map<String, List<ConfigField>> {
        val fields = getConfigFields(configClass)
        val categories = LinkedHashMap<String, MutableList<ConfigField>>()
        fields.forEach { field ->
            val categoryName = field.category?.name?.takeIf { it.isNotBlank() } ?: "General" //TODO: Prob change to simply not give a sub category
            categories.getOrPut(categoryName) { mutableListOf() }.add(field)
        }
        return categories
    }

    /**
     * Saves all current setting values to a JSON file (`settings.json`).
     * Only properties corresponding to [ConfigField]s (excluding buttons and info fields)
     * are saved.
     */
    fun saveSettings() {
        try {
            val jsonObject = JsonObject()

            configClasses.forEach { (categoryName, configClass) ->
                val categoryObject = JsonObject()
                val objectInstance = configClass.objectInstance

                if (objectInstance != null) {
                    getConfigFields(configClass).forEach { field ->
                        when (field) {
                            is ButtonConfigField -> { /* Skip buttons */ }
                            is InfoConfigField -> { /* Skip info */ }
                            is BooleanConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is DropdownConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is KeybindConfigField -> categoryObject.add(field.property.name, gson.toJsonTree(field.property.get(objectInstance).value))
                            is SliderConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is TextAreaConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is SpinnerConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is ColorPickerConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is TextFieldConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                            is FolderConfigField -> categoryObject.addProperty(field.property.name, field.property.get(objectInstance).value)
                        }
                    }
                }

                jsonObject.add(categoryName, categoryObject)
            }

            FileWriter(settingsFile).use { writer ->
                gson.toJson(jsonObject, writer)
            }

                        logger.debug("Settings saved to ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to save settings", e)
        }
    }

    /**
     * Loads setting values from the `settings.json` file into the corresponding
     * properties of the registered configuration classes. If the file does not exist,
     * default values remain.
     */
    fun loadSettings() {
        if (!settingsFile.exists()) {
            logger.info("Settings file doesn't exist, creating with defaults")
            return
        }

        try {
            FileReader(settingsFile).use { reader ->
                val jsonObject = gson.fromJson(reader, JsonObject::class.java)

                configClasses.forEach { (categoryName, configClass) ->
                    val categoryObject = jsonObject.getAsJsonObject(categoryName)
                    val objectInstance = configClass.objectInstance

                    if (categoryObject != null && objectInstance != null) {
                        getConfigFields(configClass).forEach { field ->
                            val jsonElement = categoryObject.get(field.property.name)
                            if (jsonElement != null && !jsonElement.isJsonNull) {
                                try {
                                    when (field) {
                                        is ButtonConfigField -> { /* Skip buttons */ }
                                        is InfoConfigField -> { /* Skip info */ }
                                        is BooleanConfigField -> field.property.get(objectInstance).value = jsonElement.asBoolean
                                        is DropdownConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                        is KeybindConfigField -> field.property.get(objectInstance).value = gson.fromJson(jsonElement, KeyCombination::class.java)
                                        is SliderConfigField -> field.property.get(objectInstance).value = jsonElement.asDouble
                                        is TextAreaConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                        is SpinnerConfigField -> field.property.get(objectInstance).value = jsonElement.asInt
                                        is ColorPickerConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                        is TextFieldConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                        is FolderConfigField -> field.property.get(objectInstance).value = jsonElement.asString
                                    }
                                    logger.debug("Loaded setting: {} = {}", field.name, jsonElement)
                                } catch (e: Exception) {
                                    logger.warn("Failed to load setting ${field.name}: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }

                        logger.debug("Settings loaded from ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to load settings", e)
        }
    }
}