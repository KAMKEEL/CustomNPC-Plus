package io.github.frostzie.nodex.settings.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigCategory(
    val name: String,
    val desc: String = ""
)