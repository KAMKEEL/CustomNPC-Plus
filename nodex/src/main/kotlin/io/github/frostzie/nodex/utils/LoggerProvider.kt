package io.github.frostzie.nodex.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    private const val MAIN = "Nodex"
    private val loggers = mutableMapOf<String, Logger>()

    fun getLogger(name: String): Logger {
        val fullName = "$MAIN:$name"
        return loggers.getOrPut(fullName) {
            LoggerWrapper(LoggerFactory.getLogger(fullName))
        }
    }
}