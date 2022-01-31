package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import org.lwjgl.opengl.GL11;

public class ClientEventHandler {
    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Post event){
        if(event.type == RenderGameOverlayEvent.ElementType.ALL) {
            for (OverlayCustom overlayCustom : Client.customOverlays.values()) {
                overlayCustom.renderGameOverlay(event.partialTicks);
            }
        }
    }
}
