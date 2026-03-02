package io.github.frostzie.nodex.utils

import net.minecraft.Util
import java.net.URI

object OpenLinks {

    fun discordLink() {
        Util.getPlatform().openUri(URI("https://discord.gg/qZ885qTvkx"))
    }

    fun gitHubLink() {
        Util.getPlatform().openUri(URI("https://github.com/Frostzie/Nodex"))
    }

    fun reportBugLink() {
        Util.getPlatform().openUri(URI("https://github.com/Frostzie/Nodex/issues"))
    }

    fun buyMeACoffeeLink() {
        Util.getPlatform().openUri(URI("https://www.buymeacoffee.com/frostzie"))
    }

    fun modrinthLink() {
        Util.getPlatform().openUri(URI("https://modrinth.com/project/XlilVGvF"))
    }
}