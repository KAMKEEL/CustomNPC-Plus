package io.github.frostzie.nodex.settings.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigEditorSpinner(
    val minValue: Int = 0,
    val maxValue: Int = 100
)
