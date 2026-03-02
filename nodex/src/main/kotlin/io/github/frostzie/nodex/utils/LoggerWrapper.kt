package io.github.frostzie.nodex.utils

import io.github.frostzie.nodex.loader.minecraft.ChatMessageBuilder
import org.slf4j.Logger
import org.slf4j.helpers.MessageFormatter

/**
 * A wrapper around a standard SLF4J [Logger] that displays messages in the chat.
 *
 * This class uses the decorator pattern and Kotlin's delegation feature (`by delegate`)
 * to automatically pass all standard logging calls to the underlying logger instance. It then
 * specifically overrides the `warn` and `error` methods to add the functionality of sending
 * a formatted message to the [ChatMessageBuilder].
 *
 * @param delegate The underlying SLF4J [Logger] instance to which all logging calls are forwarded.
 */
class LoggerWrapper(private val delegate: Logger) : Logger by delegate {

    /**
     * Constructs a warning message and sends it to the in-game chat.
     * @param message The primary warning text.
     * @param throwable An optional exception, whose stack trace will be included in the copyable text.
     */
    private fun logWarningToChat(message: String?, throwable: Throwable? = null) {
        val finalMessage = message ?: "Warning with exception"
        val copyableText = if (throwable != null) {
            "$finalMessage\n${throwable.stackTraceToString()}"
        } else {
            finalMessage
        }
        ChatMessageBuilder.warning(finalMessage, copyableText)
    }

    /**
     * Constructs an error message and sends it to the in-game chat.
     * @param message The primary error text.
     * @param throwable An optional exception, whose stack trace will be included in the copyable text.
     */
    private fun logErrorToChat(message: String?, throwable: Throwable? = null) {
        val finalMessage = message ?: "Error with exception"
        val copyableText = if (throwable != null) {
            "$finalMessage\n${throwable.stackTraceToString()}"
        } else {
            finalMessage
        }
        ChatMessageBuilder.error(finalMessage, copyableText)
    }

    // --- WARN ---

    override fun warn(msg: String?) {
        delegate.warn(msg)
        msg?.let { logWarningToChat(it) }
    }

    override fun warn(format: String?, arg: Any?) {
        delegate.warn(format, arg)
        MessageFormatter.format(format, arg).message?.let { logWarningToChat(it) }
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        delegate.warn(format, arg1, arg2)
        MessageFormatter.format(format, arg1, arg2).message?.let { logWarningToChat(it) }
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        delegate.warn(format, *arguments)
        MessageFormatter.arrayFormat(format, arguments).message?.let { logWarningToChat(it) }
    }

    override fun warn(msg: String?, t: Throwable?) {
        delegate.warn(msg, t)
        logWarningToChat(msg, t)
    }

    // --- ERROR ---

    override fun error(msg: String?) {
        delegate.error(msg)
        msg?.let { logErrorToChat(it) }
    }

    override fun error(format: String?, arg: Any?) {
        delegate.error(format, arg)
        MessageFormatter.format(format, arg).message?.let { logErrorToChat(it) }
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        delegate.error(format, arg1, arg2)
        MessageFormatter.format(format, arg1, arg2).message?.let { logErrorToChat(it) }
    }

    override fun error(format: String?, vararg arguments: Any?) {
        delegate.error(format, *arguments)
        MessageFormatter.arrayFormat(format, arguments).message?.let { logErrorToChat(it) }
    }

    override fun error(msg: String?, t: Throwable?) {
        delegate.error(msg, t)
        logErrorToChat(msg, t)
    }
}
