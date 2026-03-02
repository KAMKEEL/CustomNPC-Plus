package io.github.frostzie.nodex.loader.minecraft

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextColor
import java.net.URI

object ChatMessageBuilder {
    fun warning(warningMessage: String, copyableText: String = warningMessage) {
        val fullMessage = buildMessage(warningMessage, copyableText, "WARN", 0xFFA500, "warning")

        MCInterface.runOnRenderThread {
            MCInterface.displayClientMessage(fullMessage, false)
        }
    }

    fun error(errorMessage: String, copyableText: String = errorMessage) {
        val fullMessage = buildMessage(errorMessage, copyableText, "ERROR", 0xFF5555, "error")

        MCInterface.runOnRenderThread {
            MCInterface.displayClientMessage(fullMessage, false)
        }
    }

    //TODO: Allow loading not just current project
    fun promptUniversalLoad(projectName: String) {
        val command = "/nodex internal mirror_current"

        val clickable = Component.literal(" [Load into World]")
            .setStyle(Style.EMPTY
                .withColor(TextColor.fromRgb(0x55FF55)) // Green
                .withBold(true)
                .withClickEvent(ClickEvent.RunCommand(command))
                .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to mirror '$projectName' to this world")))
            )

        val fullMessage = buildPrefix()
            .append(Component.literal("Detected Universal Pack: '§b$projectName§r'."))
            .append(clickable)

        MCInterface.runOnRenderThread {
            MCInterface.displayClientMessage(fullMessage, false)
        }
    }

    fun testWarning() {
        warning("This is a test warning message.", "Test Warning Copy Text")
    }

    fun testError() {
        error("This is a test error message.", "Test Error Copy Text")
    }

    private fun buildMessage(message: String, copyableText: String, level: String, color: Int, type: String): Component {
        val styledMessage = Component.literal(message)
            .setStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(color))
                    .withClickEvent(ClickEvent.CopyToClipboard(copyableText))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to copy $type message")))
            )
        val builder = buildPrefix()
            .append(Component.literal("[$level]§r ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))))
            .append(styledMessage)

            if (level == "ERROR") {
            builder.append(
                Component.literal("\nPlease report this in the discord server!")
                    .setStyle(
                        Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))
                            .withClickEvent(ClickEvent.OpenUrl(URI("https://discord.gg/qZ885qTvkx")))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§eClick to open the Discord server invite")))
                    )
            )
        }
        return builder
    }

    private fun buildPrefix(): MutableComponent {
        return Component.empty()
            .append(MCColorUtils.nodexPrefixChat())
            .append(Component.literal("§r "))
    }
}