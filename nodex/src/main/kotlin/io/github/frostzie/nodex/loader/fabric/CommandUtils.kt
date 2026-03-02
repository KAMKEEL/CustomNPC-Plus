package io.github.frostzie.nodex.loader.fabric

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.frostzie.nodex.loader.minecraft.CaseInsensitiveLiteralCommandNode
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object CommandUtils {

    fun caseInsensitiveLiteral(name: String): CaseInsensitiveLiteralCommandNode.Builder<FabricClientCommandSource> {
        return CaseInsensitiveLiteralCommandNode.Builder(name)
    }

    fun LiteralArgumentBuilder<FabricClientCommandSource>.caseInsensitive(): CaseInsensitiveLiteralCommandNode.Builder<FabricClientCommandSource> {
        val builder = CaseInsensitiveLiteralCommandNode.Builder<FabricClientCommandSource>(this.literal)

        if (this.command != null) {
            builder.executes(this.command)
        }

        if (this.requirement != null) {
            builder.requires(this.requirement)
        }

        if (this.redirect != null) {
            builder.forward(this.redirect, this.redirectModifier, this.isFork)
        }

        for (argument in this.arguments) {
            builder.then(argument)
        }

        return builder
    }
}