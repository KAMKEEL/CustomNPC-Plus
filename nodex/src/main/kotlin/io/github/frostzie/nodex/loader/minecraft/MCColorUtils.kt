package io.github.frostzie.nodex.loader.minecraft

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

object MCColorUtils {

    /**
     * Prefix for chat messages sent by the mod.
     * Looks like: "[Nodex] message
     */
    fun nodexPrefixChat(): MutableComponent {
        val text = "Nodex"
        val colors = listOf(
            0x14EE72, // N
            0x1AE188, // o
            0x1FD49E, // d
            0x25C6B4, // e
            0x2AB9CA, // x
        )
        val result = Component.empty()
        result.append(Component.literal("§7["))
        for (i in text.indices) {
            result.append(
                Component.literal(text[i].toString())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colors[i])))
            )
        }
        result.append(Component.literal("§7]"))
        return result
    }
}