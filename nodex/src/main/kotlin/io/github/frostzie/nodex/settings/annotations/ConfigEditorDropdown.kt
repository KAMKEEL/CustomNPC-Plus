package io.github.frostzie.nodex.settings.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigEditorDropdown(
    val values: Array<String>
)