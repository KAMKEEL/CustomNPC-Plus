package io.github.frostzie.nodex.loader.fabric

import io.github.frostzie.nodex.loader.minecraft.CaseInsensitiveLiteralCommandNode
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

/**
 * A DSL wrapper around Brigadier's LiteralArgumentBuilder to simplify command registration
 * and enforce case-insensitive matching by default.
 */
class CommandBuilder(internal val builder: CaseInsensitiveLiteralCommandNode.Builder<FabricClientCommandSource>) {

    /**
     * Adds a child literal command node.
     */
    fun literal(name: String, block: CommandBuilder.() -> Unit) {
        val childBuilder = CommandUtils.caseInsensitiveLiteral(name)
        val childWrapper = CommandBuilder(childBuilder)
        childWrapper.block()
        builder.then(childWrapper.builder)
    }

    /**
     * Sets the execution logic for this command node.
     * The action should return 1 for success, 0 for failure (standard Brigadier convention).
     */
    fun executes(action: () -> Int) {
        builder.executes { _ -> action() }
    }
}

object CommandRegistration {
    /**
     * Registers a new root command with the given name.
     */
    fun register(name: String, block: CommandBuilder.() -> Unit) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val rootBuilder = CommandUtils.caseInsensitiveLiteral(name)
            val wrapper = CommandBuilder(rootBuilder)
            wrapper.block()
            dispatcher.register(rootBuilder)
        }
    }
}
