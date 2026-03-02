package io.github.frostzie.nodex.loader.fabric

import com.mojang.blaze3d.platform.InputConstants
import io.github.frostzie.nodex.screen.MainApplication
import io.github.frostzie.nodex.utils.JavaFXInitializer
import io.github.frostzie.nodex.utils.LoggerProvider
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

//TODO: Wrap keybind building and move to ingame dir
object Keybinds {
    private val logger = LoggerProvider.getLogger("Keybinds")
    private var toggleIDEKey: KeyMapping? = null

    fun register() {
        //? if <=1.21.8 {
        /*toggleIDEKey = KeyBindingHelper.registerKeyBinding(KeyMapping(
            "key.nodex.toggle_ide",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.nodex.general"
        ))
        *///?} else {
        toggleIDEKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.nodex.toggle_ide",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("nodex", "general"))
        ))
        //?}

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (toggleIDEKey?.consumeClick() == true) {
                logger.info("IDE toggle keybind pressed!")

                if (JavaFXInitializer.isJavaFXAvailable()) {
                    MainApplication.Companion.showMainWindow()
                } else {
                    logger.error("Cannot open IDE window - JavaFX is not available")
                }
            }
        }
    }
}