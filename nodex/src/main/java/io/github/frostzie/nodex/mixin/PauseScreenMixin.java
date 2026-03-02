package io.github.frostzie.nodex.mixin;

import io.github.frostzie.nodex.screen.MainApplication;
import io.github.frostzie.nodex.settings.categories.MinecraftConfig;
import io.github.frostzie.nodex.utils.JavaFXInitializer;
import io.github.frostzie.nodex.utils.LoggerProvider;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

    @Unique
    private final @NotNull Logger logger = LoggerProvider.INSTANCE.getLogger("PauseScreenMixin");
    protected PauseScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!MinecraftConfig.INSTANCE.getOnScreenButton().get()) {
            return;
        }

        PauseScreen self = (PauseScreen)(Object)this;

        Button button = Button.builder(Component.literal("IDE"), b -> {
            if (JavaFXInitializer.INSTANCE.isJavaFXAvailable()) {
                MainApplication.Companion.showMainWindow();
            } else {
                logger.error("Cannot open IDE window - JavaFX is not available");
            }
        }).bounds(self.width / 2 + 72, self.height / 4 - 16, 30, 20).build();

        this.addRenderableWidget(button);
    }
}