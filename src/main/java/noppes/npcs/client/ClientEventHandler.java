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
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            for (OverlayCustom overlayCustom : Client.customOverlays.values()) {
                overlayCustom.renderGameOverlay(event.partialTicks);
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
        }
    }
}
