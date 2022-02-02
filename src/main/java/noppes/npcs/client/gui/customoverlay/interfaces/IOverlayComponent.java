package noppes.npcs.client.gui.customoverlay.interfaces;

import net.minecraft.client.Minecraft;

public interface IOverlayComponent {
    void onRender(Minecraft mc, float partialTicks);
}
